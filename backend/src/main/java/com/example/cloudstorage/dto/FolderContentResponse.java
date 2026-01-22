package com.example.cloudstorage.dto;

import java.util.List;

public class FolderContentResponse {

    private List<FolderResponse> folders;
    private List<FileResponse> files;

    public FolderContentResponse(
            List<FolderResponse> folders,
            List<FileResponse> files
    ) {
        this.folders = folders;
        this.files = files;
    }

    public List<FolderResponse> getFolders() {
        return folders;
    }

    public List<FileResponse> getFiles() {
        return files;
    }
}
