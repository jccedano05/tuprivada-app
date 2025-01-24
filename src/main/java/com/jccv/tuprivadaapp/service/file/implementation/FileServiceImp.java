package com.jccv.tuprivadaapp.service.file.implementation;

import com.jccv.tuprivadaapp.dto.file.FileDto;
import com.jccv.tuprivadaapp.dto.file.mapper.FileMapper;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.file.File;
import com.jccv.tuprivadaapp.repository.condominium.CondominiumRepository;
import com.jccv.tuprivadaapp.repository.file.FileRepository;
import com.jccv.tuprivadaapp.service.file.FileService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileServiceImp implements FileService {

    private final FileRepository fileRepository;
    private final FileMapper fileMapper;
    private final CondominiumRepository condominiumRepository;

    public FileServiceImp(FileRepository fileRepository, FileMapper fileMapper, CondominiumRepository condominiumRepository) {
        this.fileRepository = fileRepository;
        this.fileMapper = fileMapper;
        this.condominiumRepository = condominiumRepository;
    }

    @Override
    public FileDto uploadFile(FileDto fileDto) {
        if (!condominiumRepository.existsById(fileDto.getCondominiumId())) {
            throw new IllegalArgumentException("Condominium not found with id " + fileDto.getCondominiumId());
        }

        File file = fileMapper.toEntity(fileDto);
        File savedFile = fileRepository.save(file);
        return fileMapper.toDTO(savedFile);
    }

    @Override
    public FileDto getFileById(Long id) {
        File file = fileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with id " + id));
        return fileMapper.toDTO(file);
    }

    @Override
    public List<FileDto> getFilesByCondominium(Long condominiumId) {  // Cambiado a List<FileDto>
        List<File> files = fileRepository.findByCondominiumId(condominiumId);  // Sin paginación
        return files.stream().map(fileMapper::toDTO).collect(Collectors.toList());  // Mapeo a DTO
    }

    @Override
    public void deleteFile(Long id) {
        File file = fileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with id " + id));
        fileRepository.delete(file);
    }
}
