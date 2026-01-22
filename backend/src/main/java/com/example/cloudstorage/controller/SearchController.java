package com.example.cloudstorage.controller;

import com.example.cloudstorage.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/files")
    public ResponseEntity<?> searchFiles(
            @RequestParam String q,
            @RequestParam(required = false) Long folderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                searchService.searchFiles(
                        authentication.getName(),
                        q,
                        folderId,
                        page,
                        size
                )
        );
    }

    @GetMapping("/folders")
    public ResponseEntity<?> searchFolders(
            @RequestParam String q,
            @RequestParam(required = false) Long parentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                searchService.searchFolders(
                        authentication.getName(),
                        q,
                        parentId,
                        page,
                        size
                )
        );
    }

    @GetMapping("/all")
    public ResponseEntity<?> combinedSearch(
            @RequestParam String q,
            @RequestParam(required = false) Long folderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                searchService.combinedSearch(
                        authentication.getName(),
                        q,
                        folderId,
                        page,
                        size
                )
        );
    }
}
