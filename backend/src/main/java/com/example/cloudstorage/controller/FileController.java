package com.example.cloudstorage.controller;

import com.example.cloudstorage.dto.*;
import com.example.cloudstorage.enums.PermissionLevel;
import com.example.cloudstorage.enums.ResourceType;
import com.example.cloudstorage.security.PermissionResolver;
import com.example.cloudstorage.service.FileService;
import com.example.cloudstorage.service.ShareService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;
    private final ShareService shareService;
    private final PermissionResolver permissionResolver;

    public FileController(
            FileService fileService,
            ShareService shareService,
            PermissionResolver permissionResolver
    ) {
        this.fileService = fileService;
        this.shareService = shareService;
        this.permissionResolver = permissionResolver;
    }

    @PostMapping("/init-upload")
    public ResponseEntity<?> initUpload(
            @RequestBody FileUploadInitRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                fileService.initUpload(request, authentication.getName())
        );
    }

    @GetMapping
    public ResponseEntity<?> listFiles(
            @RequestParam(required = false) Long folderId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                fileService.listFiles(authentication.getName(), folderId)
        );
    }

    @GetMapping("/{id}/preview")
    public ResponseEntity<?> previewFile(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                fileService.previewFile(id, authentication.getName())
        );
    }

    @PutMapping("/{id}/move")
    public ResponseEntity<?> moveFile(
            @PathVariable Long id,
            @RequestBody MoveFileRequest request,
            Authentication authentication
    ) {
        fileService.moveFile(id, request.getFolderId(), authentication.getName());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/rename")
    public ResponseEntity<?> renameFile(
            @PathVariable Long id,
            @RequestBody RenameRequest request,
            Authentication authentication
    ) {
        fileService.renameFile(id, request.getName(), authentication.getName());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFile(
            @PathVariable Long id,
            Authentication authentication
    ) {
        fileService.softDeleteFile(id, authentication.getName());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/recycle-bin/{id}/restore")
    public ResponseEntity<?> restoreFile(
            @PathVariable Long id,
            Authentication authentication
    ) {
        fileService.restoreFile(id, authentication.getName());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/recycle-bin/{id}/permanent")
    public ResponseEntity<?> permanentDeleteFile(
            @PathVariable Long id,
            Authentication authentication
    ) {
        fileService.permanentDeleteFile(id, authentication.getName());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/share")
    public ResponseEntity<?> shareFileWithUser(
            @PathVariable Long id,
            @RequestBody ShareWithUserRequest request,
            Authentication authentication
    ) {
        shareService.shareWithUser(
                id,
                ResourceType.FILE,
                request,
                authentication.getName()
        );
        return ResponseEntity.ok().build();
    }

        @GetMapping("/public/files/{id}")
        public ResponseEntity<?> previewViaShareLink(
                @PathVariable Long id,
                @RequestParam String token
        ) {
        PermissionLevel level =
                permissionResolver.resolveViaToken(token, ResourceType.FILE, id);

        permissionResolver.requireViewerOrAbove(level);

        return ResponseEntity.ok(fileService.previewPublicFile(id));
        }

    @PutMapping("/public/files/{id}/rename")
    public ResponseEntity<?> renameViaShare(
            @PathVariable Long id,
            @RequestParam String token,
            @RequestBody RenameRequest request
    ) {
        PermissionLevel level =
                permissionResolver.resolveViaToken(token, ResourceType.FILE, id);

        permissionResolver.requireEditorOrAbove(level);

        fileService.renamePublicFile(id, request.getName());
        return ResponseEntity.ok().build();
    }
}
