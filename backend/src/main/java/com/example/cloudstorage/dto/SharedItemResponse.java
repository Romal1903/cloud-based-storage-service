package com.example.cloudstorage.dto;

import com.example.cloudstorage.enums.PermissionLevel;
import com.example.cloudstorage.enums.ResourceType;

import lombok.Data;

@Data
public class SharedItemResponse {
    private Long id;
    private String name;
    private ResourceType type;
    private PermissionLevel permission;
    private Long folderId;
    private String contentType;
    private Long size;
}
