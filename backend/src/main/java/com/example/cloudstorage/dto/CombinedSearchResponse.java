package com.example.cloudstorage.dto;

import org.springframework.data.domain.Page;

public class CombinedSearchResponse {

    private Page<FolderResponse> folders;
    private Page<FileResponse> files;

    public Page<FolderResponse> getFolders() {
        return folders;
    }

    public void setFolders(Page<FolderResponse> folders) {
        this.folders = folders;
    }

    public Page<FileResponse> getFiles() {
        return files;
    }

    public void setFiles(Page<FileResponse> files) {
        this.files = files;
    }
}
