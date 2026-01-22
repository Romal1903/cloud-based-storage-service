package com.example.cloudstorage.dto;

import com.example.cloudstorage.enums.PermissionLevel;

import lombok.Data;

@Data
public class FolderResponse {
    private Long id;
    private String name;
    private Long parentFolderId;
    private PermissionLevel permission;
}
