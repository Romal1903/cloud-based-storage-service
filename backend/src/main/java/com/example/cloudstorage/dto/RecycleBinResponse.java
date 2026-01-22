package com.example.cloudstorage.dto;

import lombok.Data;

import java.util.List;

@Data
public class RecycleBinResponse {
    private List<RecycleBinFolderResponse> folders;
    private List<RecycleBinFileResponse> files;
}
