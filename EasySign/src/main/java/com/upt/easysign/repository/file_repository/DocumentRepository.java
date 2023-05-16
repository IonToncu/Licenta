package com.upt.easysign.repository.file_repository;

import com.upt.easysign.model.file.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    Document findById(long id);
}
