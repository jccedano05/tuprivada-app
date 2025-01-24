package com.jccv.tuprivadaapp.controller.file;

import com.jccv.tuprivadaapp.dto.file.FileDto;
import com.jccv.tuprivadaapp.service.file.FileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.jccv.tuprivadaapp.utils.AuthorizationsLevel.CONDOMINIUM_LEVEL;
import static com.jccv.tuprivadaapp.utils.AuthorizationsLevel.USER_LEVEL;

@RestController
@RequestMapping("/files")
@PreAuthorize(CONDOMINIUM_LEVEL)
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping
    public ResponseEntity<?> uploadFile(@RequestBody FileDto fileDto) {
        try {
            FileDto uploadedFile = fileService.uploadFile(fileDto);
            return ResponseEntity.ok(uploadedFile);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize(USER_LEVEL)
    public ResponseEntity<FileDto> getFileById(@PathVariable Long id) {
        FileDto fileDto = fileService.getFileById(id);
        return ResponseEntity.ok(fileDto);
    }

    @GetMapping("/condominiums/{condominiumId}")
    @PreAuthorize(USER_LEVEL)
    public ResponseEntity<List<FileDto>> getFilesByCondominium(@PathVariable Long condominiumId) {
        List<FileDto> files = fileService.getFilesByCondominium(condominiumId);
        return ResponseEntity.ok(files);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long id) {
        fileService.deleteFile(id);
        return ResponseEntity.noContent().build();
    }
}
