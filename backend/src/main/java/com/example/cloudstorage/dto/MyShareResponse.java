package com.example.cloudstorage.dto;

import java.time.LocalDateTime;

import com.example.cloudstorage.enums.PermissionLevel;
import com.example.cloudstorage.enums.ResourceType;

import lombok.Data;

@Data
public class MyShareResponse {

    private Long id;

    private String name;

    private Long resourceId;
    private ResourceType resourceType;

    private String email;

    private PermissionLevel permission;
    private LocalDateTime expiresAt;
}
