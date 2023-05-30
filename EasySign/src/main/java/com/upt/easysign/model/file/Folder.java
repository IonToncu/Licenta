package com.upt.easysign.model.file;

import com.upt.easysign.dto.FolderDto;
import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "folders")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Folder extends File{
    @OneToMany
    private List<Document> documents;

    @Column(name = "is_posted")
    private Boolean isPosted;
    @Column(name = "is_shared")
    private Boolean isShared;

    public void addDocumentInList(Document document){
        if(documents == null) documents = new ArrayList<>();
        documents.add(document);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Folder folder = (Folder) o;
        return getId() != null && Objects.equals(getId(), folder.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    public FolderDto toFolderDto(){
        FolderDto folderDto = new FolderDto();
        folderDto.setId(getId());
        folderDto.setFileName(getFileName());
        folderDto.setFileStatus(getStatus());
        folderDto.setUpdated(getUpdated());
        folderDto.setIsPosted(getIsPosted());
        getDocuments().forEach(document -> {
            folderDto.addToDocuments(document.toDto());
        });
        return folderDto;
    }
}
