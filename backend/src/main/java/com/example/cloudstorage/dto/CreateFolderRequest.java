package com.example.cloudstorage.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreateFolderRequest {

    private String name;
    private Long parentFolderId;
}
