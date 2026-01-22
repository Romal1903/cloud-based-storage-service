package com.example.cloudstorage.dto;

import com.example.cloudstorage.enums.PermissionLevel;
import com.example.cloudstorage.enums.ResourceType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShareRequest {
    private Long resourceId;
    private ResourceType resourceType;
    private PermissionLevel permission;
    private LocalDateTime expiresAt;
}
