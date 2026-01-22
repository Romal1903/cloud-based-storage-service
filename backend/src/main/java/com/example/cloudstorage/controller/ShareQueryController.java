package com.example.cloudstorage.controller;

import com.example.cloudstorage.dto.SharedItemResponse;
import com.example.cloudstorage.entity.File;
import com.example.cloudstorage.entity.Folder;
import com.example.cloudstorage.entity.SharedResource;
import com.example.cloudstorage.entity.User;
import com.example.cloudstorage.enums.ResourceType;
import com.example.cloudstorage.repository.FileRepository;
import com.example.cloudstorage.repository.FolderRepository;
import com.example.cloudstorage.repository.SharedResourceRepository;
import com.example.cloudstorage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/shares/query")
@RequiredArgsConstructor
public class ShareQueryController {

        private final SharedResourceRepository repo;
        private final UserRepository userRepo;
        private final FileRepository fileRepo;
        private final FolderRepository folderRepo;

        @GetMapping("/shared-with-me")
        public Map<String, Object> sharedWithMe(Authentication auth) {

                User user = userRepo.findByEmail(auth.getName())
                        .orElseThrow();

                List<SharedResource> shares =
                        repo.findActiveSharesForUser(user, LocalDateTime.now());

                List<SharedItemResponse> files = shares.stream()
                        .filter(s -> s.getResourceType() == ResourceType.FILE)
                        .map(s -> {
                                File f = fileRepo.findById(s.getResourceId()).orElse(null);
                                if (f == null) return null;

                                SharedItemResponse r = new SharedItemResponse();
                                r.setId(f.getId());
                                r.setName(f.getName());
                                r.setType(ResourceType.FILE);
                                r.setPermission(s.getPermission());
                                r.setContentType(f.getContentType());
                                r.setSize(f.getSize());
                                r.setFolderId(f.getFolder() != null ? f.getFolder().getId() : null);
                                return r;
                        })
                        .filter(Objects::nonNull)
                        .toList();

                List<SharedItemResponse> folders = shares.stream()
                        .filter(s -> s.getResourceType() == ResourceType.FOLDER)
                        .map(s -> {
                        Folder f = folderRepo.findById(s.getResourceId()).orElse(null);
                        if (f == null) return null;

                        SharedItemResponse r = new SharedItemResponse();
                        r.setId(f.getId());
                        r.setName(f.getName());
                        r.setType(ResourceType.FOLDER);
                        r.setPermission(s.getPermission());
                        r.setFolderId(f.getParentFolder() != null ? f.getParentFolder().getId() : null);
                        return r;
                        })
                        .filter(Objects::nonNull)
                        .toList();

                return Map.of("files", files, "folders", folders);
        }
    
}
