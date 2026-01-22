package com.example.cloudstorage.service;

import com.example.cloudstorage.controller.ShareController.SharedFolderResponse;
import com.example.cloudstorage.dto.FileResponse;
import com.example.cloudstorage.dto.FolderResponse;
import com.example.cloudstorage.dto.MyShareResponse;
import com.example.cloudstorage.dto.ShareRequest;
import com.example.cloudstorage.dto.ShareWithUserRequest;
import com.example.cloudstorage.entity.File;
import com.example.cloudstorage.entity.Folder;
import com.example.cloudstorage.entity.SharedResource;
import com.example.cloudstorage.entity.User;
import com.example.cloudstorage.enums.PermissionLevel;
import com.example.cloudstorage.enums.ResourceType;
import com.example.cloudstorage.repository.FileRepository;
import com.example.cloudstorage.repository.FolderRepository;
import com.example.cloudstorage.repository.SharedResourceRepository;
import com.example.cloudstorage.repository.UserRepository;
import com.example.cloudstorage.security.PermissionResolver;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ShareService {

        private final SharedResourceRepository sharedRepo;
        private final UserRepository userRepo;
        private final PermissionResolver permissionResolver;
        private final FileRepository fileRepo;
        private final FolderRepository folderRepo;
        private final SupabaseStorageService storageService;

        public ShareService(
                SharedResourceRepository sharedRepo,
                UserRepository userRepo,
                PermissionResolver permissionResolver,
                FileRepository fileRepo,
                FolderRepository folderRepo,
                SupabaseStorageService storageService
        ) {
                this.sharedRepo = sharedRepo;
                this.userRepo = userRepo;
                this.permissionResolver = permissionResolver;
                this.fileRepo = fileRepo;
                this.folderRepo = folderRepo;
                this.storageService = storageService;
        }

        public void shareWithUser(
                Long resourceId,
                ResourceType type,
                ShareWithUserRequest request,
                String ownerEmail
        ) {
                if (request.getExpiresAt() == null) {
                        throw new RuntimeException("Expiry is required");
                }

                User owner = userRepo.findByEmail(ownerEmail)
                        .orElseThrow(() -> new RuntimeException("User not found"));

                if (permissionResolver.resolve(resourceId, type, owner)
                        != PermissionLevel.OWNER) {
                        throw new AccessDeniedException("Only owner can share");
                }

                User target = userRepo.findByEmail(request.getEmail())
                        .orElseThrow(() -> new RuntimeException("Target user not found"));

                SharedResource share = new SharedResource();
                share.setOwner(owner);
                share.setSharedWith(target);
                share.setResourceId(resourceId);
                share.setResourceType(type);
                share.setPermission(request.getPermission());
                share.setExpiresAt(request.getExpiresAt());
                share.setToken(null);

                sharedRepo.save(share);
        }

        public String createPublicShare(
                ShareRequest request,
                String ownerEmail
        ) {
                if (request.getExpiresAt() == null) {
                        throw new RuntimeException("Expiry is required");
                }

                User owner = userRepo.findByEmail(ownerEmail)
                        .orElseThrow(() -> new RuntimeException("User not found"));

                if (permissionResolver.resolve(
                        request.getResourceId(),
                        request.getResourceType(),
                        owner
                ) != PermissionLevel.OWNER) {
                        throw new AccessDeniedException("Only owner can create link");
                }

                SharedResource share = new SharedResource();
                share.setOwner(owner);
                share.setResourceId(request.getResourceId());
                share.setResourceType(request.getResourceType());
                share.setPermission(PermissionLevel.VIEWER);
                share.setExpiresAt(request.getExpiresAt());
                share.setToken(UUID.randomUUID().toString());
                share.setSharedWith(null);

                sharedRepo.save(share);
                return share.getToken();
        }

        public List<MyShareResponse> listMyShares(String email) {

                User owner = userRepo.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("User not found"));

                return sharedRepo.findByOwner(owner).stream().map(share -> {

                        MyShareResponse r = new MyShareResponse();

                        r.setId(share.getId());
                        r.setResourceId(share.getResourceId());
                        r.setResourceType(share.getResourceType());
                        r.setPermission(share.getPermission());
                        r.setExpiresAt(share.getExpiresAt());

                        if (share.getSharedWith() != null) {
                                r.setEmail(share.getSharedWith().getEmail());
                        } else {
                                r.setEmail("Public link");
                        }

                        if (share.getResourceType() == ResourceType.FILE) {
                                r.setName(
                                        fileRepo.findById(share.getResourceId())
                                                .map(File::getName)
                                                .orElse("Deleted file")
                                );
                                } else {
                                r.setName(
                                        folderRepo.findById(share.getResourceId())
                                                .map(Folder::getName)
                                                .orElse("Deleted folder")
                                );
                        }

                        return r;
                }).toList();
        }

        public void revokeShare(String email, Long shareId) {
                User owner = userRepo.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("User not found"));

                SharedResource share = sharedRepo.findById(shareId)
                        .orElseThrow(() -> new RuntimeException("Share not found"));

                if (!share.getOwner().getId().equals(owner.getId())) {
                        throw new AccessDeniedException("Only owner can revoke");
                }

                sharedRepo.delete(share);
        }

        public SharedFolderResponse listSharedFolder(Long folderId, String email) {

    User user = userRepo.findByEmail(email).orElseThrow();

    PermissionLevel level =
            permissionResolver.resolveFolderTreeAccess(folderId, user);

    if (level == null) {
        throw new AccessDeniedException("No access");
    }

    Folder folder = folderRepo.findById(folderId)
            .orElseThrow(() -> new RuntimeException("Folder not found"));

    List<Folder> subFolders =
            folderRepo.findByParentFolder(folder).stream()
                    .filter(f -> !Boolean.TRUE.equals(f.getDeleted()))
                    .toList();

    List<File> files =
            fileRepo.findByFolder(folder).stream()
                    .filter(f -> !Boolean.TRUE.equals(f.getDeleted()))
                    .toList();

    List<FolderResponse> folderResponses = subFolders.stream().map(f -> {
        FolderResponse r = new FolderResponse();
        r.setId(f.getId());
        r.setName(f.getName());
        r.setParentFolderId(folder.getId());
        r.setPermission(level);
        return r;
    }).toList();

    List<FileResponse> fileResponses = files.stream().map(file -> {
        FileResponse r = new FileResponse();
        r.setId(file.getId());
        r.setName(file.getName());
        r.setContentType(file.getContentType());
        r.setSize(file.getSize());
        r.setFolderId(folder.getId());
        r.setPermission(level);
        return r;
    }).toList();

    return new SharedFolderResponse(folderResponses, fileResponses);
}

        public Map<String, String> previewSharedFile(Long fileId, String email) {

                User user = userRepo.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("User not found"));

                PermissionLevel level =
                        permissionResolver.resolve(fileId, ResourceType.FILE, user);

                if (level == null) {
                        throw new AccessDeniedException("No access to file");
                }

                File file = fileRepo.findById(fileId)
                        .orElseThrow(() -> new RuntimeException("File not found"));

                if (Boolean.TRUE.equals(file.getDeleted())) {
                        throw new RuntimeException("File deleted");
                }

                return Map.of(
                        "previewUrl",
                        storageService.getSignedReadUrl(file.getStorageKey())
                );
        }

        public SharedFolderResponse listPublicFolder(
                Long folderId,
                String token
        ) {
        PermissionLevel level =
                permissionResolver.resolveViaToken(
                        token,
                        ResourceType.FOLDER,
                        folderId
                );

        Folder folder = folderRepo.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found"));

        List<FolderResponse> folders =
                folderRepo.findByParentFolder(folder).stream()
                        .filter(f -> !Boolean.TRUE.equals(f.getDeleted()))
                        .map(f -> {
                                FolderResponse r = new FolderResponse();
                                r.setId(f.getId());
                                r.setName(f.getName());
                                r.setParentFolderId(folderId);
                                r.setPermission(level);
                                return r;
                        })
                        .toList();

        List<FileResponse> files =
                fileRepo.findByFolder(folder).stream()
                        .filter(f -> !Boolean.TRUE.equals(f.getDeleted()))
                        .map(f -> {
                                FileResponse r = new FileResponse();
                                r.setId(f.getId());
                                r.setName(f.getName());
                                r.setContentType(f.getContentType());
                                r.setSize(f.getSize());
                                r.setPermission(level);
                                return r;
                        })
                        .toList();

        return new SharedFolderResponse(folders, files);
        }

}
