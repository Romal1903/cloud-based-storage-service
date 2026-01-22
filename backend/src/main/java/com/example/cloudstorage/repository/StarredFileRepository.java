package com.example.cloudstorage.repository;

import com.example.cloudstorage.entity.File;
import com.example.cloudstorage.entity.StarredFile;
import com.example.cloudstorage.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StarredFileRepository
        extends JpaRepository<StarredFile, Long> {

    boolean existsByUserAndFile(User user, File file);

    Optional<StarredFile> findByUserAndFile(User user, File file);

    List<StarredFile> findByUser(User user);
}
