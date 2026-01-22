package com.example.cloudstorage.controller;

import com.example.cloudstorage.service.StarredFileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/starred")
public class StarredFileController {

    private final StarredFileService service;

    public StarredFileController(StarredFileService service) {
        this.service = service;
    }

    @PostMapping("/{fileId}")
    public ResponseEntity<?> star(
            @PathVariable Long fileId,
            Authentication authentication
    ) {
        service.starFile(fileId, authentication.getName());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<?> unstar(
            @PathVariable Long fileId,
            Authentication authentication
    ) {
        service.unstarFile(fileId, authentication.getName());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<?> list(Authentication authentication) {
        return ResponseEntity.ok(
                service.listStarredFiles(authentication.getName())
        );
    }
}
