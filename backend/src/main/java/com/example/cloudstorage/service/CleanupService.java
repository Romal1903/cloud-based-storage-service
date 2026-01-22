package com.example.cloudstorage.service;

import com.example.cloudstorage.entity.File;
import com.example.cloudstorage.entity.Folder;
import com.example.cloudstorage.repository.FileRepository;
import com.example.cloudstorage.repository.FolderRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CleanupService {

    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final SupabaseStorageService storageService;

    public CleanupService(
            FileRepository fileRepository,
            FolderRepository folderRepository,
            SupabaseStorageService storageService
    ) {
        this.fileRepository = fileRepository;
        this.folderRepository = folderRepository;
        this.storageService = storageService;
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupExpiredTrash() {

        System.out.println("Recycle bin cleanup started");

        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);

        List<File> expiredFiles =
                fileRepository.findByDeletedTrueAndDeletedAtBefore(cutoff);

        for (File file : expiredFiles) {
            try {
                storageService.delete(file.getStorageKey());
                fileRepository.delete(file);
            } catch (Exception e) {
                System.err.println("Failed to delete file: " + file.getId());
            }
        }

        List<Folder> expiredFolders =
                folderRepository.findByDeletedTrueAndDeletedAtBefore(cutoff);

        for (Folder folder : expiredFolders) {
            deleteFolderRecursively(folder);
        }

        System.out.println("Recycle bin cleanup completed");
    }

    private void deleteFolderRecursively(Folder folder) {

        List<File> files = fileRepository.findByFolder(folder);
        for (File file : files) {
            storageService.delete(file.getStorageKey());
            fileRepository.delete(file);
        }

        List<Folder> children = folderRepository.findByParentFolder(folder);
        for (Folder child : children) {
            deleteFolderRecursively(child);
        }

        folderRepository.delete(folder);
    }
}
