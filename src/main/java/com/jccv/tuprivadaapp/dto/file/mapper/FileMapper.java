package com.jccv.tuprivadaapp.dto.file.mapper;

import com.jccv.tuprivadaapp.dto.file.FileDto;
import com.jccv.tuprivadaapp.model.file.File;
import com.jccv.tuprivadaapp.service.condominium.CondominiumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FileMapper {

    private final CondominiumService condominiumService;

    @Autowired
    public FileMapper(CondominiumService condominiumService) {
        this.condominiumService = condominiumService;
    }

    public FileDto toDTO(File file) {
        FileDto dto = new FileDto();
        dto.setId(file.getId());
        dto.setFileName(file.getFileName());
        dto.setFileType(file.getFileType());
        dto.setSize(file.getSize());
        dto.setDescription(file.getDescription());
        dto.setCondominiumId(file.getCondominium().getId());
        return dto;
    }

    public File toEntity(FileDto dto) {
        File file = new File();
        file.setId(dto.getId());
        file.setFileName(dto.getFileName());
        file.setFileType(dto.getFileType());
        file.setSize(dto.getSize());
        file.setDescription(dto.getDescription());
        file.setCondominium(condominiumService.findById(dto.getCondominiumId()));
        return file;
    }
}
