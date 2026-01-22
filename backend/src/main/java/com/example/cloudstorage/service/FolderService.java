package com.example.cloudstorage.service;

import com.example.cloudstorage.dto.CreateFolderRequest;
import com.example.cloudstorage.dto.FileResponse;
import com.example.cloudstorage.dto.FolderContentResponse;
import com.example.cloudstorage.dto.FolderResponse;
import com.example.cloudstorage.entity.File;
import com.example.cloudstorage.entity.Folder;
import com.example.cloudstorage.entity.User;
import com.example.cloudstorage.enums.PermissionLevel;
import com.example.cloudstorage.enums.ResourceType;
import com.example.cloudstorage.repository.FileRepository;
import com.example.cloudstorage.repository.FolderRepository;
import com.example.cloudstorage.repository.UserRepository;
import com.example.cloudstorage.security.PermissionChecker;
import com.example.cloudstorage.security.PermissionResolver;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FolderService {

    private final FolderRepository folderRepository;
    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final SupabaseStorageService storageService;
    private final PermissionResolver permissionResolver;
    private final PermissionChecker permissionChecker;

    public FolderService(
            FolderRepository folderRepository,
            UserRepository userRepository,
            FileRepository fileRepository,
            SupabaseStorageService storageService,
            PermissionResolver permissionResolver,
            PermissionChecker permissionChecker
    ) {
        this.folderRepository = folderRepository;
        this.userRepository = userRepository;
        this.fileRepository = fileRepository;
        this.storageService = storageService;
        this.permissionResolver = permissionResolver;
        this.permissionChecker = permissionChecker;
    }

    public FolderResponse createFolder(CreateFolderRequest request, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getParentFolderId() != null) {
            PermissionLevel level = permissionResolver.resolve(
                    request.getParentFolderId(),
                    ResourceType.FOLDER,
                    user
            );
            permissionChecker.requireEdit(level);
        }

        Folder folder = new Folder();
        folder.setName(request.getName());
        folder.setOwner(user);

        if (request.getParentFolderId() != null) {
            Folder parent = folderRepository.findById(request.getParentFolderId())
                    .orElseThrow(() -> new RuntimeException("Parent folder not found"));

            if (Boolean.TRUE.equals(parent.getDeleted())) {
                throw new RuntimeException("Parent folder is deleted");
            }

            folder.setParentFolder(parent);
        }

        Folder saved = folderRepository.save(folder);

        FolderResponse response = new FolderResponse();
        response.setId(saved.getId());
        response.setName(saved.getName());
        if (saved.getParentFolder() != null) {
            response.setParentFolderId(saved.getParentFolder().getId());
        }

        return response;
    }

    public List<FolderResponse> listFolders(String email, Long parentId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Folder> folders = (parentId == null)
                ? folderRepository.findByOwnerAndParentFolderIsNullAndDeletedIsFalse(user)
                : folderRepository.findByOwnerAndParentFolderIdAndDeletedIsFalse(user, parentId);

        return folders.stream().map(folder -> {
            FolderResponse r = new FolderResponse();
            r.setId(folder.getId());
            r.setName(folder.getName());

            if (folder.getParentFolder() != null) {
                r.setParentFolderId(folder.getParentFolder().getId());
            }

            PermissionLevel level = permissionResolver.resolve(
                    folder.getId(),
                    ResourceType.FOLDER,
                    user
            );
            r.setPermission(level);

            return r;
        }).toList();
    }

    public void moveFolder(Long folderId, Long parentFolderId, String email) {

        User user = userRepository.findByEmail(email).orElseThrow();

        PermissionLevel level = permissionResolver.resolve(
                folderId,
                ResourceType.FOLDER,
                user
        );
        permissionChecker.requireEdit(level);

        Folder folder = folderRepository.findById(folderId).orElseThrow();

        if (parentFolderId == null) {
            folder.setParentFolder(null);
            folderRepository.save(folder);
            return;
        }

        if (folderId.equals(parentFolderId)) {
            throw new RuntimeException("Folder cannot be its own parent");
        }

        Folder parent = folderRepository.findById(parentFolderId)
                .orElseThrow(() -> new RuntimeException("Parent folder not found"));

        if (Boolean.TRUE.equals(parent.getDeleted())) {
            throw new RuntimeException("Parent folder is deleted");
        }

        Folder current = parent;
        while (current != null) {
            if (current.getId().equals(folder.getId())) {
                throw new RuntimeException("Cannot move folder into its own subtree");
            }
            current = current.getParentFolder();
        }

        folder.setParentFolder(parent);
        folderRepository.save(folder);
    }

    public void softDeleteFolder(Long folderId, String email) {

        User user = userRepository.findByEmail(email).orElseThrow();

        PermissionLevel level = permissionResolver.resolve(
                folderId,
                ResourceType.FOLDER,
                user
        );
        permissionChecker.requireEdit(level);

        Folder folder = folderRepository.findById(folderId).orElseThrow();
        softDeleteRecursive(folder);
    }

    private void softDeleteRecursive(Folder folder) {

        for (File file : fileRepository.findByFolder(folder)) {
            file.setDeleted(true);
            file.setDeletedAt(LocalDateTime.now());
            fileRepository.save(file);
        }

        for (Folder child : folderRepository.findByParentFolder(folder)) {
            softDeleteRecursive(child);
        }

        folder.setDeleted(true);
        folder.setDeletedAt(LocalDateTime.now());
        folderRepository.save(folder);
    }

    public void restoreFolder(Long folderId, String email) {

        User user = userRepository.findByEmail(email).orElseThrow();

        PermissionLevel level = permissionResolver.resolve(
                folderId,
                ResourceType.FOLDER,
                user
        );
        permissionChecker.requireEdit(level);

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found"));

        if (!Boolean.TRUE.equals(folder.getDeleted())) {
            throw new RuntimeException("Folder is not deleted");
        }

        if (folder.getParentFolder() != null &&
                Boolean.TRUE.equals(folder.getParentFolder().getDeleted())) {
            folder.setParentFolder(null);
        }

        restoreRecursive(folder);
    }

    private void restoreRecursive(Folder folder) {

        folder.setDeleted(false);
        folder.setDeletedAt(null);
        folderRepository.save(folder);

        for (File file : fileRepository.findByFolder(folder)) {
            if (Boolean.TRUE.equals(file.getDeleted())) {
                file.setDeleted(false);
                file.setDeletedAt(null);
                fileRepository.save(file);
            }
        }

        for (Folder child : folderRepository.findByParentFolder(folder)) {
            if (Boolean.TRUE.equals(child.getDeleted())) {
                restoreRecursive(child);
            }
        }
    }

    public void permanentDeleteFolder(Long folderId, String email) {

        User user = userRepository.findByEmail(email).orElseThrow();

        PermissionLevel level = permissionResolver.resolve(
                folderId,
                ResourceType.FOLDER,
                user
        );
        permissionChecker.requireOwner(level);

        Folder folder = folderRepository.findByIdAndDeletedTrue(folderId)
                .orElseThrow(() -> new RuntimeException("Deleted folder not found"));

        permanentDeleteRecursive(folder);
    }

    private void permanentDeleteRecursive(Folder folder) {

        for (File file : fileRepository.findByFolder(folder)) {
            storageService.delete(file.getStorageKey());
            fileRepository.delete(file);
        }

        for (Folder child : folderRepository.findByParentFolder(folder)) {
            permanentDeleteRecursive(child);
        }

        folderRepository.delete(folder);
    }

    public void bulkPermanentDeleteFolders(List<Long> folderIds, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        for (Folder folder : folderRepository.findAllById(folderIds)) {

            PermissionLevel level = permissionResolver.resolve(
                    folder.getId(),
                    ResourceType.FOLDER,
                    user
            );
            permissionChecker.requireOwner(level);

            if (!Boolean.TRUE.equals(folder.getDeleted())) {
                throw new RuntimeException(
                        "Folder " + folder.getId() + " is not deleted"
                );
            }

            permanentDeleteRecursive(folder);
        }
    }

    public void renameFolder(Long folderId, String newName, String email) {

        if (newName == null || newName.trim().isEmpty()) {
            throw new RuntimeException("Folder name cannot be empty");
        }

        User user = userRepository.findByEmail(email).orElseThrow();

        PermissionLevel level = permissionResolver.resolve(
                folderId,
                ResourceType.FOLDER,
                user
        );
        permissionChecker.requireEdit(level);

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found"));

        if (Boolean.TRUE.equals(folder.getDeleted())) {
            throw new RuntimeException("Cannot rename deleted folder");
        }

        folder.setName(newName.trim());
        folderRepository.save(folder);
    }

    public FolderContentResponse listPublicFolder(Long folderId, Long ownerId) {

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found"));

        if (Boolean.TRUE.equals(folder.getDeleted())) {
            throw new RuntimeException("Folder deleted");
        }

        List<FolderResponse> folders =
                folderRepository.findByParentFolder(folder).stream()
                        .filter(f -> !Boolean.TRUE.equals(f.getDeleted()))
                        .filter(f -> f.getOwner().getId().equals(ownerId))
                        .map(f -> {
                            FolderResponse r = new FolderResponse();
                            r.setId(f.getId());
                            r.setName(f.getName());
                            r.setParentFolderId(folderId);
                            return r;
                        })
                        .toList();

        List<FileResponse> files =
                fileRepository.findByFolder(folder).stream()
                        .filter(f -> !Boolean.TRUE.equals(f.getDeleted()))
                        .filter(f -> f.getOwner().getId().equals(ownerId))
                        .map(f -> {
                            FileResponse r = new FileResponse();
                            r.setId(f.getId());
                            r.setName(f.getName());
                            r.setContentType(f.getContentType());
                            r.setSize(f.getSize());
                            r.setFolderId(folderId);
                            return r;
                        })
                        .toList();

        return new FolderContentResponse(folders, files);
    }
}
