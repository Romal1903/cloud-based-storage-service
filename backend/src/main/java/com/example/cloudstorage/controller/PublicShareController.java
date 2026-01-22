package com.example.cloudstorage.controller;

import com.example.cloudstorage.entity.SharedResource;
import com.example.cloudstorage.enums.ResourceType;
import com.example.cloudstorage.repository.SharedResourceRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/public/share")
public class PublicShareController {

    private final SharedResourceRepository repository;

    public PublicShareController( SharedResourceRepository repository ) {
        this.repository = repository;
    }

    @GetMapping("/{token}")
    public ResponseEntity<?> accessSharedResource(@PathVariable String token) {

        SharedResource share =
                repository.findByTokenAndExpiresAtAfter(token, LocalDateTime.now())
                        .orElseThrow(() -> new RuntimeException("Invalid or expired link"));

        if (share.getResourceType() == ResourceType.FILE) {
            return ResponseEntity.ok(Map.of(
                    "type", "FILE",
                    "resourceId", share.getResourceId()
            ));
        }

        return ResponseEntity.ok(Map.of(
                "type", "FOLDER",
                "resourceId", share.getResourceId()
        ));
    }
}
