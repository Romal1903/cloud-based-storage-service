package com.example.cloudstorage.repository;

import com.example.cloudstorage.entity.File;
import com.example.cloudstorage.entity.Folder;
import com.example.cloudstorage.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<File, Long> {

    List<File> findByOwnerAndFolderIsNullAndDeletedFalse(User owner);

    List<File> findByOwnerAndFolderIdAndDeletedFalse(User owner, Long folderId);

    List<File> findByFolder(Folder folder);

    List<File> findByOwnerAndDeletedTrue(User owner);

    Optional<File> findByIdAndDeletedTrue(Long id);

    List<File> findByDeletedTrueAndDeletedAtBefore(LocalDateTime cutoff);

    List<File> findByOwnerAndNameContainingIgnoreCaseAndDeletedFalse(
            User owner,
            String name
    );

    List<File> findByOwnerAndFolderIdAndNameContainingIgnoreCaseAndDeletedFalse(
            User owner,
            Long folderId,
            String name
    );

    Page<File> findByOwnerAndNameContainingIgnoreCaseAndDeletedFalse(
            User owner,
            String name,
            Pageable pageable
    );

    Page<File> findByOwnerAndFolderIdAndNameContainingIgnoreCaseAndDeletedFalse(
            User owner,
            Long folderId,
            String name,
            Pageable pageable
    );

    @Query("select coalesce(sum(f.size), 0) from File f where f.owner = :owner and f.deleted = false")
    Long sumFileSizeByOwner(@Param("owner") User owner);

}
