package com.upt.easysign.repository.file_repository;

import com.upt.easysign.model.file.StackFolder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicStackFolderRepository extends JpaRepository<StackFolder, Long> {
    StackFolder findById(long id);
}
