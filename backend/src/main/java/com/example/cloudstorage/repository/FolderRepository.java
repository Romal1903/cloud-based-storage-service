package com.example.cloudstorage.repository;

import com.example.cloudstorage.entity.Folder;
import com.example.cloudstorage.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FolderRepository extends JpaRepository<Folder, Long> {

    List<Folder> findByOwnerAndParentFolderIsNullAndDeletedIsFalse(User owner);

    List<Folder> findByOwnerAndParentFolderIdAndDeletedIsFalse(
            User owner,
            Long parentFolderId
    );

    List<Folder> findByParentFolder(Folder parent);

    List<Folder> findByOwnerAndDeletedTrue(User owner);

    List<Folder> findByDeletedTrueAndDeletedAtBefore(LocalDateTime cutoff);

    Optional<Folder> findByIdAndDeletedTrue(Long id);

    List<Folder> findByOwnerAndNameContainingIgnoreCaseAndDeletedIsFalse(
            User owner,
            String name
    );

    List<Folder> findByOwnerAndParentFolderIdAndNameContainingIgnoreCaseAndDeletedIsFalse(
            User owner,
            Long parentFolderId,
            String name
    );

    Page<Folder> findByOwnerAndNameContainingIgnoreCaseAndDeletedIsFalse(
            User owner,
            String name,
            Pageable pageable
    );

    Page<Folder> findByOwnerAndParentFolderIdAndNameContainingIgnoreCaseAndDeletedIsFalse(
            User owner,
            Long parentFolderId,
            String name,
            Pageable pageable
    );
}
