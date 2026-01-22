package com.example.cloudstorage.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RecycleBinFolderResponse {
    private Long id;
    private String name;
    private Long parentFolderId;
    private LocalDateTime deletedAt;
}
