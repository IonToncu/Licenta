package com.upt.easysign.service;

import com.upt.easysign.model.file.StackFolder;

import java.util.List;

public interface PublicStackFolderService {

    StackFolder post(StackFolder folder) throws Exception;
    StackFolder getByFolderId(long id) throws Exception;
    StackFolder getById(long id);

    List<StackFolder> getAll();

    void delete(StackFolder stackFolder);
}
