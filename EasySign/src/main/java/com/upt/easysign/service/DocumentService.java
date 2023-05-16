package com.upt.easysign.service;

import com.upt.easysign.model.file.Document;

import java.util.List;

public interface DocumentService {
    Document save(Document document);
    Document getById(long id);
    List<Document> getAll();
}

