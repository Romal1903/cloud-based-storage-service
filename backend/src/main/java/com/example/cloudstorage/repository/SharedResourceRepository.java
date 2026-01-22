package com.example.cloudstorage.repository;

import com.example.cloudstorage.entity.SharedResource;
import com.example.cloudstorage.entity.User;
import com.example.cloudstorage.enums.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SharedResourceRepository
        extends JpaRepository<SharedResource, Long> {

        Optional<SharedResource> findByToken(String token);

        Optional<SharedResource> findByTokenAndExpiresAtAfter(
                String token,
                LocalDateTime now
        );

        @Query("""
                SELECT s FROM SharedResource s
                WHERE s.resourceId = :resourceId
                AND s.resourceType = :type
                AND s.sharedWith.id = :userId
                AND (s.expiresAt IS NULL OR s.expiresAt > :now)
        """)
        Optional<SharedResource> findActiveUserShare(
                Long resourceId,
                ResourceType type,
                Long userId,
                LocalDateTime now
        );

        @Query("""
                SELECT s FROM SharedResource s
                WHERE s.sharedWith = :user
                AND (s.expiresAt IS NULL OR s.expiresAt > :now)
        """)
        List<SharedResource> findActiveSharesForUser(
                User user,
                LocalDateTime now
        );

        List<SharedResource> findByOwner(User owner);

        List<SharedResource> findByOwnerAndResourceIdAndResourceType(
                User owner,
                Long resourceId,
                ResourceType resourceType
        );

        List<SharedResource> findBySharedWithAndResourceType(User user, ResourceType type);

        Optional<SharedResource> findByTokenAndResourceTypeAndResourceId(
                String token,
                ResourceType resourceType,
                Long resourceId
        );
}
