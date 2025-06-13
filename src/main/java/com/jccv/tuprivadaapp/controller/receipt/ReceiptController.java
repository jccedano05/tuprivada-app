package com.jccv.tuprivadaapp.controller.receipt;

import com.jccv.tuprivadaapp.dto.receipt.ReceiptDto;
import com.jccv.tuprivadaapp.dto.receipt.mapper.ReceiptMapper;
import com.jccv.tuprivadaapp.exception.BadRequestException;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.receipt.EnumTypesReceipt;
import com.jccv.tuprivadaapp.model.receipt.Receipt;
import com.jccv.tuprivadaapp.service.receipt.ReceiptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/receipts")
public class ReceiptController {

    private final ReceiptService receiptService;
    private final ReceiptMapper receiptMapper;

    @Autowired
    public ReceiptController(ReceiptService receiptService, ReceiptMapper receiptMapper) {
        this.receiptService = receiptService;
        this.receiptMapper = receiptMapper;
    }

    @PostMapping
    public ResponseEntity<ReceiptDto> createReceipt(@RequestBody ReceiptDto receiptDTO) {
        ReceiptDto createdReceipt = receiptService.createReceipt(3414L, EnumTypesReceipt.PAYMENT);
        return ResponseEntity.ok(createdReceipt);
    }

    @GetMapping("/operation/{id}")
    public ResponseEntity<?> getReceiptByOperationCode(@PathVariable Long id) {
      try{
          ReceiptDto receiptDTO = receiptMapper.toDTO(receiptService.getReceiptById(id));
          return ResponseEntity.ok(receiptDTO);
      }catch (ResourceNotFoundException e){
          return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
      }catch (Exception e){
          return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<?> downloadReceiptPdf(@PathVariable Long id) {

        try{

            byte[] pdfData = receiptService.getReceiptPdfById(id);
        if (pdfData == null || pdfData.length == 0) {
            throw new ResourceNotFoundException("El recibo con id " + id + " no tiene un PDF generado.");
        }

        ByteArrayResource resource = new ByteArrayResource(pdfData);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=recibo_" + id + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(pdfData.length)
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }catch (BadRequestException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }catch (ResourceNotFoundException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }catch (IOException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
        }catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/generate-or-download")
    public ResponseEntity<?> generateOrDownloadReceipt(
            @RequestParam Long id,
            @RequestParam EnumTypesReceipt type
    ) {
        try {
            byte[] pdfData = receiptService.getOrCreateReceiptPdf(id, type);

            ByteArrayResource resource = new ByteArrayResource(pdfData);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=recibo_" + id + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(pdfData.length)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);
        } catch (BadRequestException e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (ResourceNotFoundException e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
