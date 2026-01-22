package com.example.cloudstorage.service;

import com.example.cloudstorage.dto.*;
import com.example.cloudstorage.entity.File;
import com.example.cloudstorage.entity.Folder;
import com.example.cloudstorage.entity.User;
import com.example.cloudstorage.repository.FileRepository;
import com.example.cloudstorage.repository.FolderRepository;
import com.example.cloudstorage.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecycleBinService {

    private final UserRepository userRepository;
    private final FolderRepository folderRepository;
    private final FileRepository fileRepository;

    public RecycleBinService(
            UserRepository userRepository,
            FolderRepository folderRepository,
            FileRepository fileRepository
    ) {
        this.userRepository = userRepository;
        this.folderRepository = folderRepository;
        this.fileRepository = fileRepository;
    }

    public RecycleBinResponse getRecycleBin(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Folder> deletedFolders =
                folderRepository.findByOwnerAndDeletedTrue(user);

        List<File> deletedFiles =
                fileRepository.findByOwnerAndDeletedTrue(user);

        List<RecycleBinFolderResponse> folderResponses =
                deletedFolders.stream().map(folder -> {
                    RecycleBinFolderResponse r = new RecycleBinFolderResponse();
                    r.setId(folder.getId());
                    r.setName(folder.getName());
                    r.setDeletedAt(folder.getDeletedAt());
                    if (folder.getParentFolder() != null) {
                        r.setParentFolderId(folder.getParentFolder().getId());
                    }
                    return r;
                }).toList();

        List<RecycleBinFileResponse> fileResponses =
                deletedFiles.stream().map(file -> {
                    RecycleBinFileResponse r = new RecycleBinFileResponse();
                    r.setId(file.getId());
                    r.setName(file.getName());
                    r.setSize(file.getSize());
                    r.setContentType(file.getContentType());
                    r.setDeletedAt(file.getDeletedAt());
                    if (file.getFolder() != null) {
                        r.setFolderId(file.getFolder().getId());
                    }
                    return r;
                }).toList();

        RecycleBinResponse response = new RecycleBinResponse();
        response.setFolders(folderResponses);
        response.setFiles(fileResponses);

        return response;
    }
}
