package com.example.cloudstorage.service;

import com.example.cloudstorage.dto.FileResponse;
import com.example.cloudstorage.dto.FileUploadInitRequest;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class FileService {

    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final SupabaseStorageService storageService;
    private final FolderRepository folderRepository;
    private final PermissionResolver permissionResolver;
    private final PermissionChecker permissionChecker;
    @Value("${supabase.service-key}")
    private String serviceKey;


    public FileService(
            UserRepository userRepository,
            FileRepository fileRepository,
            SupabaseStorageService storageService,
            FolderRepository folderRepository,
            PermissionResolver permissionResolver,
            PermissionChecker permissionChecker
    ) {
        this.userRepository = userRepository;
        this.fileRepository = fileRepository;
        this.storageService = storageService;
        this.folderRepository = folderRepository;
        this.permissionResolver = permissionResolver;
        this.permissionChecker = permissionChecker;
    }

    public Map<String, Object> initUpload(
            FileUploadInitRequest request,
            String email
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String storageKey =
                storageService.generateUploadPath(user.getId(), request.getName());

        File file = new File();
        file.setName(request.getName());
        file.setContentType(request.getContentType());
        file.setSize(request.getSize());
        file.setOwner(user);
        file.setStorageKey(storageKey);

        if (request.getFolderId() != null) {
            Folder folder = folderRepository.findById(request.getFolderId())
                    .orElseThrow(() -> new RuntimeException("Folder not found"));

            PermissionLevel level = permissionResolver.resolve(
                    folder.getId(),
                    ResourceType.FOLDER,
                    user
            );
            permissionChecker.requireEdit(level);

            if (Boolean.TRUE.equals(folder.getDeleted())) {
                throw new RuntimeException("Folder is deleted");
            }

            file.setFolder(folder);
        }

        fileRepository.save(file);

        return Map.of(
                "uploadUrl", storageService.getUploadEndpoint(storageKey),
                "fileId", file.getId().toString(),
                "headers", Map.of(
                        "Authorization", "Bearer " + serviceKey,
                        "apikey", serviceKey,
                        "Content-Type", request.getContentType(),
                        "x-upsert", "true"
                )
        );
    }

    public List<FileResponse> listFiles(String email, Long folderId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<File> files = (folderId == null)
                ? fileRepository.findByOwnerAndFolderIsNullAndDeletedFalse(user)
                : fileRepository.findByOwnerAndFolderIdAndDeletedFalse(user, folderId);

        return files.stream().map(file -> {
                FileResponse r = new FileResponse();
                r.setId(file.getId());
                r.setName(file.getName());
                r.setContentType(file.getContentType());
                r.setSize(file.getSize());

                if (file.getFolder() != null) {
                r.setFolderId(file.getFolder().getId());
                }

                PermissionLevel level = permissionResolver.resolve(
                        file.getId(),
                        ResourceType.FILE,
                        user
                );
                r.setPermission(level);

                return r;
        }).toList();
    }

    public void moveFile(Long fileId, Long folderId, String email) {

        User user = userRepository.findByEmail(email).orElseThrow();
        File file = fileRepository.findById(fileId).orElseThrow();

        PermissionLevel level = permissionResolver.resolve(
                fileId,
                ResourceType.FILE,
                user
        );
        permissionChecker.requireEdit(level);

        if (Boolean.TRUE.equals(file.getDeleted())) {
            throw new RuntimeException("Cannot move deleted file");
        }

        if (folderId == null) {
            file.setFolder(null);
        } else {
            Folder folder = folderRepository.findById(folderId)
                    .orElseThrow(() -> new RuntimeException("Folder not found"));

            if (Boolean.TRUE.equals(folder.getDeleted())) {
                throw new RuntimeException("Cannot move into deleted folder");
            }

            file.setFolder(folder);
        }

        fileRepository.save(file);
    }

    public void softDeleteFile(Long id, String email) {

        User user = userRepository.findByEmail(email).orElseThrow();
        File file = fileRepository.findById(id).orElseThrow();

        PermissionLevel level = permissionResolver.resolve(
                id,
                ResourceType.FILE,
                user
        );
        permissionChecker.requireEdit(level);

        file.setDeleted(true);
        file.setDeletedAt(LocalDateTime.now());
        fileRepository.save(file);
    }

    public void restoreFile(Long fileId, String email) {

        User user = userRepository.findByEmail(email).orElseThrow();

        File file = fileRepository.findByIdAndDeletedTrue(fileId)
                .orElseThrow(() -> new RuntimeException("Deleted file not found"));

        PermissionLevel level = permissionResolver.resolve(
                fileId,
                ResourceType.FILE,
                user
        );
        permissionChecker.requireEdit(level);

        if (file.getFolder() != null &&
                Boolean.TRUE.equals(file.getFolder().getDeleted())) {
            file.setFolder(null);
        }

        file.setDeleted(false);
        file.setDeletedAt(null);
        fileRepository.save(file);
    }

    public void permanentDeleteFile(Long fileId, String email) {

        User user = userRepository.findByEmail(email).orElseThrow();

        File file = fileRepository.findByIdAndDeletedTrue(fileId)
                .orElseThrow(() -> new RuntimeException("Deleted file not found"));

        PermissionLevel level = permissionResolver.resolve(
                fileId,
                ResourceType.FILE,
                user
        );
        permissionChecker.requireOwner(level);

        storageService.delete(file.getStorageKey());
        fileRepository.delete(file);
    }

    public void bulkPermanentDeleteFiles(List<Long> fileIds, String email) {

        User user = userRepository.findByEmail(email).orElseThrow();

        for (File file : fileRepository.findAllById(fileIds)) {

            PermissionLevel level = permissionResolver.resolve(
                    file.getId(),
                    ResourceType.FILE,
                    user
            );
            permissionChecker.requireOwner(level);

            if (!Boolean.TRUE.equals(file.getDeleted())) {
                throw new RuntimeException("File " + file.getId() + " is not deleted");
            }

            storageService.delete(file.getStorageKey());
            fileRepository.delete(file);
        }
    }

    public Map<String, String> previewFile(Long fileId, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        PermissionLevel level = permissionResolver.resolve(
                fileId,
                ResourceType.FILE,
                user
        );
        permissionChecker.requireView(level);

        if (Boolean.TRUE.equals(file.getDeleted())) {
            throw new RuntimeException("File deleted");
        }

        if (file.getSize() == null || file.getSize() == 0) {
                long size = storageService.fetchFileSize(file.getStorageKey());
                file.setSize(size);
                fileRepository.save(file);
        }

        return Map.of(
                "previewUrl",
                storageService.getSignedReadUrl(file.getStorageKey())
        );
    }

    public Map<String, String> previewPublicFile(Long fileId) {

        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (Boolean.TRUE.equals(file.getDeleted())) {
            throw new RuntimeException("File deleted");
        }

        return Map.of(
                "previewUrl",
                storageService.getPublicReadUrl(file.getStorageKey())
        );
    }

    public Map<String, String> downloadFile(Long fileId, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        PermissionLevel level = permissionResolver.resolve(
                fileId,
                ResourceType.FILE,
                user
        );
        permissionChecker.requireView(level);

        if (Boolean.TRUE.equals(file.getDeleted())) {
            throw new RuntimeException("File deleted");
        }

        if (file.getSize() == null || file.getSize() == 0) {
                long size = storageService.fetchFileSize(file.getStorageKey());
                file.setSize(size);
                fileRepository.save(file);
        }

        return Map.of(
                "downloadUrl",
                storageService.getSignedReadUrl(file.getStorageKey())
        );
    }

    public void renamePublicFile(Long fileId, String newName) {

        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (Boolean.TRUE.equals(file.getDeleted())) {
            throw new RuntimeException("File deleted");
        }

        file.setName(newName.trim());
        fileRepository.save(file);
    }

    public void renameFile(Long fileId, String newName, String email) {

        if (newName == null || newName.trim().isEmpty()) {
            throw new RuntimeException("File name cannot be empty");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        PermissionLevel level = permissionResolver.resolve(
                fileId,
                ResourceType.FILE,
                user
        );
        permissionChecker.requireEdit(level);

        if (Boolean.TRUE.equals(file.getDeleted())) {
            throw new RuntimeException("Cannot rename deleted file");
        }

        file.setName(newName.trim());
        fileRepository.save(file);
    }
}
