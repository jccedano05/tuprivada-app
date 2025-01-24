//package com.jccv.tuprivadaapp.controller.aws.s3;
//
//import com.jccv.tuprivadaapp.service.aws.s3.FileUploadPreasignedService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.net.URL;
//
//import static com.jccv.tuprivadaapp.utils.AuthorizationsLevel.CONDOMINIUM_LEVEL;
//@RestController
//@RequestMapping("/files-storage")
//@PreAuthorize(CONDOMINIUM_LEVEL)
//public class FileUploadController {
//
//    private final FileUploadPreasignedService fileUploadPreasignedService;
//
//    @Autowired
//    public FileUploadController(FileUploadPreasignedService fileUploadPreasignedService) {
//        this.fileUploadPreasignedService = fileUploadPreasignedService;
//    }
//
//    @GetMapping("/generate-presigned-url")
//    public URL generatePresignedUrl(@RequestParam String fileName, @RequestParam String fileType) {
//        return fileUploadPreasignedService.generatePresignedUrl(fileName, fileType);
//    }
//
//
//
//}

package com.jccv.tuprivadaapp.controller.aws.s3;

import com.jccv.tuprivadaapp.exception.BadRequestException;
import com.jccv.tuprivadaapp.service.aws.s3.FileUploadPreasignedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URL;
import java.util.Map;

import static com.jccv.tuprivadaapp.utils.AuthorizationsLevel.CONDOMINIUM_LEVEL;

@RestController
@RequestMapping("/files-storage")
public class FileUploadController {

    private final FileUploadPreasignedService fileUploadPreasignedService;

    @Autowired
    public FileUploadController(FileUploadPreasignedService fileUploadPreasignedService) {
        this.fileUploadPreasignedService = fileUploadPreasignedService;
    }

    @PostMapping("/generate-presigned-url")
    public ResponseEntity<?> generatePresignedUrl(@RequestBody Map<String, String> payload) {
        try {
            String fileName = payload.get("fileName");
            String fileType = payload.get("fileType");

            System.out.println(fileName);
            System.out.println(fileType);
            URL presignedUrl = fileUploadPreasignedService.generatePresignedUrl(fileName, fileType);
            return ResponseEntity.ok(presignedUrl);
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/generate-download-url")
    public ResponseEntity<?> generateDownloadUrl(@RequestBody Map<String, String> payload) {
        try {
            String fileName = payload.get("fileName");

            if (fileName == null || fileName.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File name must not be empty");
            }

            URL presignedUrl = fileUploadPreasignedService.generatePresignedDownloadUrl(fileName);
            return ResponseEntity.ok(presignedUrl);
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // Manejo de excepciones global
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
