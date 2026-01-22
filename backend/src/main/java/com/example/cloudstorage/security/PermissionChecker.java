package com.example.cloudstorage.security;

import com.example.cloudstorage.enums.PermissionLevel;
import org.springframework.stereotype.Component;

@Component
public class PermissionChecker {

    public void requireView(PermissionLevel level) {
        if (level == null) {
            throw new RuntimeException("View permission required");
        }
    }

    public void requireEdit(PermissionLevel level) {
        if (level != PermissionLevel.OWNER && level != PermissionLevel.EDITOR) {
            throw new RuntimeException("Edit permission required");
        }
    }

    public void requireOwner(PermissionLevel level) {
        if (level != PermissionLevel.OWNER) {
            throw new RuntimeException("Owner permission required");
        }
    }
}
