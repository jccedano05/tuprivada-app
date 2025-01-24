package com.jccv.tuprivadaapp.service.file;

import com.jccv.tuprivadaapp.dto.file.FileDto;

import java.util.List;

public interface FileService {
    FileDto uploadFile(FileDto fileDto);
    FileDto getFileById(Long id);
    List<FileDto> getFilesByCondominium(Long condominiumId);  // Cambiado a List
    void deleteFile(Long id);
}
