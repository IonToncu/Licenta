package com.upt.easysign.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.upt.easysign.model.file.FileStatus;
import com.upt.easysign.model.file.Folder;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FolderDto {
    private String fileName;
    private long id;
    private String ownerUsername;
    private List<DocumentDto> documentDtoList;
    private FileStatus fileStatus;

    public void addToDocuments(DocumentDto documentDto){
        if (this.documentDtoList == null) documentDtoList = new ArrayList<>();
        documentDtoList.add(documentDto);
    }
    public Folder toFolder(){
        Folder folder = new Folder();
        folder.setFileName(fileName);
        folder.setDocuments(new ArrayList<>());
        folder.setUpdated(new Date());
        folder.setCreated(new Date());
        folder.setStatus(FileStatus.PENDING);
        return folder;
    }
}
