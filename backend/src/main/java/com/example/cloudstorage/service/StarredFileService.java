package com.example.cloudstorage.service;

import com.example.cloudstorage.dto.FileResponse;
import com.example.cloudstorage.entity.File;
import com.example.cloudstorage.entity.StarredFile;
import com.example.cloudstorage.entity.User;
import com.example.cloudstorage.repository.FileRepository;
import com.example.cloudstorage.repository.StarredFileRepository;
import com.example.cloudstorage.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StarredFileService {

    private final StarredFileRepository repository;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;

    public StarredFileService(
            StarredFileRepository repository,
            FileRepository fileRepository,
            UserRepository userRepository
    ) {
        this.repository = repository;
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
    }

    public void starFile(Long fileId, String email) {

        User user = userRepository.findByEmail(email).orElseThrow();
        File file = fileRepository.findById(fileId).orElseThrow();

        if (repository.existsByUserAndFile(user, file)) {
            return;
        }

        StarredFile star = new StarredFile();
        star.setUser(user);
        star.setFile(file);

        repository.save(star);
    }

    public void unstarFile(Long fileId, String email) {

        User user = userRepository.findByEmail(email).orElseThrow();
        File file = fileRepository.findById(fileId).orElseThrow();

        repository.findByUserAndFile(user, file)
                .ifPresent(repository::delete);
    }

    public List<FileResponse> listStarredFiles(String email) {

        User user = userRepository.findByEmail(email).orElseThrow();

        return repository.findByUser(user)
                .stream()
                .map(star -> {
                    File f = star.getFile();
                    FileResponse r = new FileResponse();
                    r.setId(f.getId());
                    r.setName(f.getName());
                    r.setContentType(f.getContentType());
                    r.setSize(f.getSize());
                    if (f.getFolder() != null) {
                        r.setFolderId(f.getFolder().getId());
                    }
                    return r;
                })
                .toList();
    }
}
