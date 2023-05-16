package com.upt.easysign.service.impl;

import com.upt.easysign.model.file.Document;
import com.upt.easysign.repository.file_repository.DocumentRepository;
import com.upt.easysign.service.DocumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    private DocumentRepository documentRepository;
    @Autowired
    public DocumentServiceImpl(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @Override
    public Document save(Document document) {
        return documentRepository.save(document);
    }

    @Override
    public Document getById(long id) {
        return documentRepository.findById(id);
    }

    @Override
    public List<Document> getAll() {
        return documentRepository.findAll();
    }
}
