package com.upt.easysign.service;

import com.upt.easysign.model.file.Document;
import com.upt.easysign.model.file.Folder;

import java.util.List;

public interface FolderService {
    Folder save(Folder folder);
    List<Folder> getAll();
    Folder findById(long id);

    void addDocumentInFolder(Folder folder, Document document);
}
