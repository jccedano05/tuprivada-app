package com.jccv.tuprivadaapp.dto.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileDto {
    private Long id;
    private String fileName;
    private String fileType;
    private Long size;
    private String description;
    private Long condominiumId;
}
