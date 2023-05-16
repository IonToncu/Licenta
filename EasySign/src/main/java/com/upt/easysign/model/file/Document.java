package com.upt.easysign.model.file;

import com.upt.easysign.dto.DocumentDto;
import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "documents")
@Getter
@Setter
@ToString(callSuper = false)
@RequiredArgsConstructor
public class Document extends File{
    @ToString.Exclude
    @Column(name = "file")
    private byte[] file;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Document document = (Document) o;
        return getId() != null && Objects.equals(getId(), document.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public DocumentDto toDto(){
        DocumentDto documentDto = new DocumentDto();
        documentDto.setId(getId());
        documentDto.setFileName(getFileName());
        documentDto.setFile("/api/v1/admin/candidate/doc/" + getId());
        documentDto.setFileStatus(getStatus());
        return documentDto;
    }
}
