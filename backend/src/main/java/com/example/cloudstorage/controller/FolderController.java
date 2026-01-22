package com.example.cloudstorage.controller;

import com.example.cloudstorage.controller.ShareController.SharedFolderResponse;
import com.example.cloudstorage.dto.*;
import com.example.cloudstorage.enums.ResourceType;
import com.example.cloudstorage.repository.SharedResourceRepository;
import com.example.cloudstorage.service.FolderService;
import com.example.cloudstorage.service.ShareService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/folders")
public class FolderController {

    private final FolderService folderService;
    private final ShareService shareService;
    
    public FolderController(FolderService folderService, ShareService shareService, SharedResourceRepository sharedRepo) {
        this.folderService = folderService;
        this.shareService = shareService;
    }

    @PostMapping
    public ResponseEntity<?> createFolder(
            @RequestBody CreateFolderRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(
                folderService.createFolder(request, email)
        );
    }

    @GetMapping
    public ResponseEntity<?> listFolders(
            @RequestParam(required = false) Long parentId,
            Authentication authentication
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(
                folderService.listFolders(email, parentId)
        );
    }

    @PutMapping("/{id}/move")
    public ResponseEntity<?> moveFolder(
            @PathVariable Long id,
            @RequestBody MoveFolderRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        folderService.moveFolder(id, request.getParentFolderId(), email);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFolder(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String email = authentication.getName();
        folderService.softDeleteFolder(id, email);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/recycle-bin/{id}/restore")
    public ResponseEntity<?> restoreFolder(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String email = authentication.getName();
        folderService.restoreFolder(id, email);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/recycle-bin/{id}/permanent")
    public ResponseEntity<?> permanentDeleteFolder(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String email = authentication.getName();
        folderService.permanentDeleteFolder(id, email);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/recycle-bin/permanent/bulk")
    public ResponseEntity<?> bulkPermanentDeleteFolders(
            @RequestBody BulkDeleteRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        folderService.bulkPermanentDeleteFolders(request.getIds(), email);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/rename")
    public ResponseEntity<?> renameFolder(
            @PathVariable Long id,
            @RequestBody RenameRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        folderService.renameFolder(id, request.getName(), email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/share")
    public ResponseEntity<?> shareFolderWithUser(
            @PathVariable Long id,
            @RequestBody ShareWithUserRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        shareService.shareWithUser(
                id,
                ResourceType.FOLDER,
                request,
                email
        );
        return ResponseEntity.ok().build();
    }

        @GetMapping("/public/folders/{id}")
        public SharedFolderResponse openPublicFolder(
                @PathVariable Long id,
                @RequestParam String token
        ) {
        return shareService.listPublicFolder(id, token);
        }
}
