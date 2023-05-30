package com.upt.easysign.service;

import com.upt.easysign.model.file.Folder;
import com.upt.easysign.model.user.Notary;

import java.util.List;

public interface NotaryService {
    Notary register(Notary notary);
    List<Notary> getAll();
    Notary findByUsername(String username);
    Boolean containUserByEmail(String email);
    Notary getNotaryByEmail(String email);

    Folder getNotaryFolderById(String username, long folderId);
}

