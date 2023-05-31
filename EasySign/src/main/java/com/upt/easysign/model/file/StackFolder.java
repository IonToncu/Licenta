package com.upt.easysign.model.file;

import com.upt.easysign.dto.FolderDto;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.util.Date;
@Entity
@Table(name = "public_stack_folder")
@Data
public class StackFolder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(name = "created")
    private Date created;

    @LastModifiedDate
    @Column(name = "updated")
    private Date updated;

    @OneToOne
    private Folder folder;

    public static StackFolder createStackFolder(Folder folder){
        StackFolder stackFolder = new StackFolder();
        stackFolder.setFolder(folder);
        stackFolder.setCreated(new Date());
        stackFolder.setUpdated(new Date());
        return stackFolder;
    }

    public FolderDto toFolderDto(){
        FolderDto folderDto = new FolderDto();
        folderDto.setId(folder.getId());
        folderDto.setFileName(folder.getFileName());
        folderDto.setFileStatus(folder.getStatus());
        folder.getDocuments().forEach(document -> {
            folderDto.addToDocuments(document.toDto());
        });
        return folderDto;
    }


}
