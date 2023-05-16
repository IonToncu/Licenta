package com.upt.easysign.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.upt.easysign.model.file.FileStatus;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentDto {
    private String fileName;
    private long id;
    private String file;
    private FileStatus fileStatus;
}
