package com.example.cloudstorage.controller;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.cloudstorage.entity.User;
import com.example.cloudstorage.repository.FileRepository;
import com.example.cloudstorage.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
public class StorageController {

    private final FileRepository fileRepository;
    private final UserRepository userRepository;

    @GetMapping("/usage")
    public Map<String, Long> Usage(Authentication auth) {

        User user = userRepository
                .findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Long used = fileRepository.sumFileSizeByOwner(user);
        return Map.of("used", used == null ? 0 : used);
    }
}
