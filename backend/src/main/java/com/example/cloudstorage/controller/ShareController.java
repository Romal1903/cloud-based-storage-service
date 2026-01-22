package com.example.cloudstorage.controller;

import com.example.cloudstorage.dto.FileResponse;
import com.example.cloudstorage.dto.FolderResponse;
import com.example.cloudstorage.dto.MyShareResponse;
import com.example.cloudstorage.dto.ShareRequest;
import com.example.cloudstorage.dto.ShareWithUserRequest;
import com.example.cloudstorage.entity.SharedResource;
import com.example.cloudstorage.entity.User;
import com.example.cloudstorage.enums.PermissionLevel;
import com.example.cloudstorage.enums.ResourceType;
import com.example.cloudstorage.repository.SharedResourceRepository;
import com.example.cloudstorage.repository.UserRepository;
import com.example.cloudstorage.service.ShareService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shares")
public class ShareController {

    private final ShareService shareService;
    private final UserRepository userRepo;
    private final SharedResourceRepository sharedRepo;

    public ShareController(
            ShareService shareService,
            UserRepository userRepo,
            SharedResourceRepository sharedRepo
    ) {
        this.shareService = shareService;
        this.userRepo = userRepo;
        this.sharedRepo = sharedRepo;
    }

        @PostMapping
        public ResponseEntity<?> createPublicShare(
                @RequestBody ShareRequest request,
                Authentication authentication
        ) {
        String token = shareService.createPublicShare(
                request,
                authentication.getName()
        );

        return ResponseEntity.ok(Map.of("token", token));
        }

    @PostMapping("/{type}/{id}")
    public ResponseEntity<?> shareWithUser(
            @PathVariable ResourceType type,
            @PathVariable Long id,
            @RequestBody ShareWithUserRequest request,
            Authentication authentication
    ) {
        shareService.shareWithUser(
                id,
                type,
                request,
                authentication.getName()
        );
        return ResponseEntity.ok().build();
    }

        @GetMapping("/mine")
        public List<MyShareResponse> myShares(Authentication auth) {
                return shareService.listMyShares(auth.getName());
        }

    @GetMapping("/{type}/{id}")
    public List<SharedResource> sharesForResource(
            @PathVariable ResourceType type,
            @PathVariable Long id,
            Authentication auth
    ) {
        User owner = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return sharedRepo.findByOwnerAndResourceIdAndResourceType(
                owner,
                id,
                type
        );
    }

    @PutMapping("/{id}")
    public void updateShare(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication auth
    ) {
        SharedResource share = sharedRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Share not found"));

        if (!share.getOwner().getEmail().equals(auth.getName())) {
            throw new AccessDeniedException("Only owner can update");
        }

        if (share.getToken() == null && body.containsKey("permission")) {
            share.setPermission(
                    PermissionLevel.valueOf(body.get("permission"))
            );
        }

        if (!body.containsKey("expiresAt")) {
            throw new RuntimeException("Expiry is required");
        }

        share.setExpiresAt(
                LocalDateTime.parse(body.get("expiresAt"))
        );

        sharedRepo.save(share);
    }

    @DeleteMapping("/{id}")
    public void revoke(
            @PathVariable Long id,
            Authentication auth
    ) {
        shareService.revokeShare(auth.getName(), id);
    }

    public record SharedFolderResponse(
    List<FolderResponse> folders,
    List<FileResponse> files
) {}

@GetMapping("/shared-folder/{id}")
public SharedFolderResponse listSharedFolder(
        @PathVariable Long id,
        Authentication auth
) {
    return shareService.listSharedFolder(id, auth.getName());
}

@GetMapping("/file/{id}/preview")
public ResponseEntity<?> previewSharedFile(
        @PathVariable Long id,
        Authentication auth
) {
    return ResponseEntity.ok(
            shareService.previewSharedFile(id, auth.getName())
    );
}

}
