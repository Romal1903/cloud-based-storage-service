package com.example.cloudstorage.dto;

import java.time.LocalDateTime;

import com.example.cloudstorage.enums.PermissionLevel;
import lombok.Data;

@Data
public class ShareWithUserRequest {
    private String email;
    private PermissionLevel permission;
    private LocalDateTime expiresAt;
}
