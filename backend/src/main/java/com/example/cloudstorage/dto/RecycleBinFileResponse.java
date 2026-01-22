package com.example.cloudstorage.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RecycleBinFileResponse {
    private Long id;
    private String name;
    private Long folderId;
    private Long size;
    private String contentType;
    private LocalDateTime deletedAt;
}
