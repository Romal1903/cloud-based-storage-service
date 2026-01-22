package com.example.cloudstorage.security;

import com.example.cloudstorage.entity.File;
import com.example.cloudstorage.entity.Folder;
import com.example.cloudstorage.entity.SharedResource;
import com.example.cloudstorage.entity.User;
import com.example.cloudstorage.enums.PermissionLevel;
import com.example.cloudstorage.enums.ResourceType;
import com.example.cloudstorage.repository.FileRepository;
import com.example.cloudstorage.repository.FolderRepository;
import com.example.cloudstorage.repository.SharedResourceRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PermissionResolver {

    private final SharedResourceRepository sharedRepo;
    private final FileRepository fileRepo;
    private final FolderRepository folderRepo;

    public PermissionResolver(
            SharedResourceRepository sharedRepo,
            FileRepository fileRepo,
            FolderRepository folderRepo
    ) {
        this.sharedRepo = sharedRepo;
        this.fileRepo = fileRepo;
        this.folderRepo = folderRepo;
    }

    public PermissionLevel resolve(Long resourceId, ResourceType type, User user) {

        if (type == ResourceType.FILE) {
            File file = fileRepo.findById(resourceId)
                    .orElseThrow(() -> new RuntimeException("File not found"));

            if (file.getOwner().getId().equals(user.getId())) {
                return PermissionLevel.OWNER;
            }

            PermissionLevel direct =
                    sharedRepo.findActiveUserShare(
                            resourceId,
                            ResourceType.FILE,
                            user.getId(),
                            LocalDateTime.now()
                    ).map(SharedResource::getPermission).orElse(null);

            if (direct != null) {
                return direct;
            }

            if (file.getFolder() != null) {
                return resolveFolderTreeAccess(file.getFolder().getId(), user);
            }

            return null;
        }

        if (type == ResourceType.FOLDER) {
            Folder folder = folderRepo.findById(resourceId)
                    .orElseThrow(() -> new RuntimeException("Folder not found"));

            if (folder.getOwner().getId().equals(user.getId())) {
                return PermissionLevel.OWNER;
            }

            return resolveFolderTreeAccess(resourceId, user);
        }

        return null;
    }

    public PermissionLevel resolveViaToken(
        String token,
        ResourceType type,
        Long resourceId
    ) {
        SharedResource share = sharedRepo.findByToken(token)
                .orElseThrow(() -> new AccessDeniedException("Invalid link"));

        if (share.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AccessDeniedException("Link expired");
        }

        if (share.getResourceType() == type &&
            share.getResourceId().equals(resourceId)) {
            return share.getPermission();
        }

        if (share.getResourceType() == ResourceType.FOLDER) {

            if (type == ResourceType.FOLDER) {
                Folder folder = folderRepo.findById(resourceId)
                        .orElseThrow();

                Folder current = folder;
                while (current != null) {
                    if (current.getId().equals(share.getResourceId())) {
                        return share.getPermission();
                    }
                    current = current.getParentFolder();
                }
            }

            if (type == ResourceType.FILE) {
                File file = fileRepo.findById(resourceId)
                        .orElseThrow();

                Folder parent = file.getFolder();
                while (parent != null) {
                    if (parent.getId().equals(share.getResourceId())) {
                        return share.getPermission();
                    }
                    parent = parent.getParentFolder();
                }
            }
        }

        throw new AccessDeniedException("No access via public link");
    }

    public void requireViewerOrAbove(PermissionLevel level) {
        if (level == null) {
            throw new RuntimeException("Viewer permission required");
        }
    }

    public void requireEditorOrAbove(PermissionLevel level) {
        if (level != PermissionLevel.OWNER && level != PermissionLevel.EDITOR) {
            throw new RuntimeException("Editor permission required");
        }
    }

    public PermissionLevel resolveFolderTreeAccess(Long folderId, User user) {

        Folder folder = folderRepo.findById(folderId)
                .orElseThrow();

        if (folder.getOwner().getId().equals(user.getId())) {
            return PermissionLevel.OWNER;
        }

        Folder current = folder;
        while (current != null) {
            SharedResource share = sharedRepo
                .findActiveUserShare(
                    current.getId(),
                    ResourceType.FOLDER,
                    user.getId(),
                    LocalDateTime.now()
                )
                .orElse(null);

            if (share != null) {
                return share.getPermission();
            }

            current = current.getParentFolder();
        }

        return null;
    }

}
