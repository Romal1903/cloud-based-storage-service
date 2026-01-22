package com.example.cloudstorage.entity;

import com.example.cloudstorage.enums.PermissionLevel;
import com.example.cloudstorage.enums.ResourceType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "shared_resource")
public class SharedResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long resourceId;

    @Enumerated(EnumType.STRING)
    private ResourceType resourceType;

    @Column(unique = true)
    private String token;

    private LocalDateTime expiresAt;

    @ManyToOne
    private User sharedWith;

    @Enumerated(EnumType.STRING)
    private PermissionLevel permission;

    @ManyToOne
    private User owner;
}
