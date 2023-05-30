package com.upt.easysign.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.upt.easysign.model.file.FileStatus;
import lombok.Data;

import java.util.Date;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentDto {
    private String fileName;
    private long id;
    private FileStatus fileStatus;
    private Date updated;
}
