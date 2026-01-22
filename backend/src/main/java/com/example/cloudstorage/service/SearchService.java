package com.example.cloudstorage.service;

import com.example.cloudstorage.dto.CombinedSearchResponse;
import com.example.cloudstorage.dto.FileResponse;
import com.example.cloudstorage.dto.FolderResponse;
import com.example.cloudstorage.entity.File;
import com.example.cloudstorage.entity.Folder;
import com.example.cloudstorage.entity.User;
import com.example.cloudstorage.enums.PermissionLevel;
import com.example.cloudstorage.enums.ResourceType;
import com.example.cloudstorage.repository.FileRepository;
import com.example.cloudstorage.repository.FolderRepository;
import com.example.cloudstorage.repository.UserRepository;
import com.example.cloudstorage.security.PermissionResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchService {

    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final PermissionResolver permissionResolver;

    public SearchService(
            UserRepository userRepository,
            FileRepository fileRepository,
            FolderRepository folderRepository,
            PermissionResolver permissionResolver
    ) {
        this.userRepository = userRepository;
        this.fileRepository = fileRepository;
        this.folderRepository = folderRepository;
        this.permissionResolver = permissionResolver;
    }

    public List<FileResponse> searchFiles(String email, String query, Long folderId) {
        User user = getUser(email);

        List<File> files = (folderId == null)
                ? fileRepository.findByOwnerAndNameContainingIgnoreCaseAndDeletedFalse(user, query)
                : fileRepository.findByOwnerAndFolderIdAndNameContainingIgnoreCaseAndDeletedFalse(
                        user, folderId, query
                );

        return files.stream().map(file -> {
            FileResponse r = mapFile(file);
            PermissionLevel level = permissionResolver.resolve(
                    file.getId(),
                    ResourceType.FILE,
                    user
            );
            r.setPermission(level);
            return r;
        }).toList();
    }

    public Page<FileResponse> searchFiles(
            String email,
            String query,
            Long folderId,
            int page,
            int size
    ) {
        User user = getUser(email);
        Pageable pageable = PageRequest.of(page, size);

        Page<File> files = (folderId == null)
                ? fileRepository.findByOwnerAndNameContainingIgnoreCaseAndDeletedFalse(
                        user, query, pageable
                )
                : fileRepository.findByOwnerAndFolderIdAndNameContainingIgnoreCaseAndDeletedFalse(
                        user, folderId, query, pageable
                );

        return files.map(file -> {
            FileResponse r = mapFile(file);
            PermissionLevel level = permissionResolver.resolve(
                    file.getId(),
                    ResourceType.FILE,
                    user
            );
            r.setPermission(level);
            return r;
        });
    }

    public List<FolderResponse> searchFolders(String email, String query, Long parentId) {
        User user = getUser(email);

        List<Folder> folders = (parentId == null)
                ? folderRepository.findByOwnerAndNameContainingIgnoreCaseAndDeletedIsFalse(user, query)
                : folderRepository.findByOwnerAndParentFolderIdAndNameContainingIgnoreCaseAndDeletedIsFalse(
                        user, parentId, query
                );

        return folders.stream().map(folder -> {
            FolderResponse r = mapFolder(folder);
            PermissionLevel level = permissionResolver.resolve(
                    folder.getId(),
                    ResourceType.FOLDER,
                    user
            );
            r.setPermission(level);
            return r;
        }).toList();
    }

    public Page<FolderResponse> searchFolders(
            String email,
            String query,
            Long parentId,
            int page,
            int size
    ) {
        User user = getUser(email);
        Pageable pageable = PageRequest.of(page, size);

        Page<Folder> folders = (parentId == null)
                ? folderRepository.findByOwnerAndNameContainingIgnoreCaseAndDeletedIsFalse(
                        user, query, pageable
                )
                : folderRepository.findByOwnerAndParentFolderIdAndNameContainingIgnoreCaseAndDeletedIsFalse(
                        user, parentId, query, pageable
                );

        return folders.map(folder -> {
            FolderResponse r = mapFolder(folder);
            PermissionLevel level = permissionResolver.resolve(
                    folder.getId(),
                    ResourceType.FOLDER,
                    user
            );
            r.setPermission(level);
            return r;
        });
    }

    public CombinedSearchResponse combinedSearch(
            String email,
            String query,
            Long folderId,
            int page,
            int size
    ) {
        Page<FolderResponse> folders =
                searchFolders(email, query, folderId, page, size);

        Page<FileResponse> files =
                searchFiles(email, query, folderId, page, size);

        CombinedSearchResponse response = new CombinedSearchResponse();
        response.setFolders(folders);
        response.setFiles(files);
        return response;
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private FileResponse mapFile(File file) {
        FileResponse r = new FileResponse();
        r.setId(file.getId());
        r.setName(file.getName());
        r.setContentType(file.getContentType());
        r.setSize(file.getSize());
        if (file.getFolder() != null) {
            r.setFolderId(file.getFolder().getId());
        }
        return r;
    }

    private FolderResponse mapFolder(Folder folder) {
        FolderResponse r = new FolderResponse();
        r.setId(folder.getId());
        r.setName(folder.getName());
        if (folder.getParentFolder() != null) {
            r.setParentFolderId(folder.getParentFolder().getId());
        }
        return r;
    }
}
