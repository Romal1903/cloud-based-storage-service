package com.example.cloudstorage.dto;

import lombok.Data;

@Data
public class FileUploadInitRequest {
    private String name;
    private String contentType;
    private Long size;
    private Long folderId;
}
