package com.example.cloudstorage.controller;

import com.example.cloudstorage.service.RecycleBinService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recycle-bin")
public class RecycleBinController {

    private final RecycleBinService recycleBinService;

    public RecycleBinController(RecycleBinService recycleBinService) {
        this.recycleBinService = recycleBinService;
    }

    @GetMapping
    public ResponseEntity<?> listRecycleBin(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(
                recycleBinService.getRecycleBin(email)
        );
    }
}
