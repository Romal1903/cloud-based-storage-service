package com.example.cloudstorage.dto;

import com.example.cloudstorage.enums.PermissionLevel;

import lombok.Data;

@Data
public class FileResponse {
    private Long id;
    private String name;
    private String contentType;
    private Long size;
    private Long folderId;
    private PermissionLevel permission;
}
