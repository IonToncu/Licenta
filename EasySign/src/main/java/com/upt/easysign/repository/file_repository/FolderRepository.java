package com.upt.easysign.repository.file_repository;

import com.upt.easysign.model.file.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FolderRepository extends JpaRepository<Folder, Long> {
    Folder findById(long id);
}
