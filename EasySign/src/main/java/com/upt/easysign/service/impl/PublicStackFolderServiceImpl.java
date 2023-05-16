package com.upt.easysign.service.impl;

import com.upt.easysign.model.file.StackFolder;
import com.upt.easysign.repository.file_repository.PublicStackFolderRepository;
import com.upt.easysign.service.PublicStackFolderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class PublicStackFolderServiceImpl implements PublicStackFolderService {
    private final PublicStackFolderRepository publicStackFolderRepository;

    @Autowired
    public PublicStackFolderServiceImpl(PublicStackFolderRepository publicStackFolderRepository) {
        this.publicStackFolderRepository = publicStackFolderRepository;
    }

    @Override
    public StackFolder post(StackFolder folder) throws Exception{
        for (StackFolder stackFolder : publicStackFolderRepository.findAll()) {
            if (stackFolder.getFolder().getId() == folder.getFolder().getId()) {
                log.info("This folder is already exist in Public Stack Folder");
                throw new Exception("This folder is already exist in Public Stack Folder");
            }
        }
        return publicStackFolderRepository.save(folder);
    }

    @Override
    public StackFolder getByFolderId(long id) throws Exception {
        List<StackFolder> stackFolderList = publicStackFolderRepository.findAll();
        for (StackFolder stackFolder : stackFolderList) {
            if(stackFolder.getFolder().getId() == id) return stackFolder;
        }
        return null;
    }

    @Override
    public StackFolder getById(long id) {
        return publicStackFolderRepository.findById(id);
    }

    @Override
    public List<StackFolder> getAll() {
        return publicStackFolderRepository.findAll();
    }

    @Override
    public void delete(StackFolder stackFolder) {
        publicStackFolderRepository.delete(stackFolder);
    }
}
