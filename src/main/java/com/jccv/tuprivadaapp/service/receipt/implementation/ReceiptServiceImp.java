package com.jccv.tuprivadaapp.service.receipt.implementation;


import com.jccv.tuprivadaapp.dto.receipt.ReceiptDto;
import com.jccv.tuprivadaapp.dto.receipt.mapper.ReceiptMapper;
import com.jccv.tuprivadaapp.exception.BadRequestException;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.receipt.EnumTypesReceipt;
import com.jccv.tuprivadaapp.model.receipt.Receipt;
import com.jccv.tuprivadaapp.repository.receipt.ReceiptRepository;
import com.jccv.tuprivadaapp.service.payment.DepositPaymentService;
import com.jccv.tuprivadaapp.service.payment.PaymentService;
import com.jccv.tuprivadaapp.service.receipt.ReceiptService;
import com.jccv.tuprivadaapp.utils.PdfGenerator;
import com.jccv.tuprivadaapp.utils.QrCodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Random;

@Service
public class ReceiptServiceImp implements ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final ReceiptMapper receiptMapper;
    private final QrCodeGenerator qrCodeGenerator;
    private final PdfGenerator pdfGenerator;
    private final DepositPaymentService depositPaymentService;
    private final PaymentService paymentService;

    @Autowired
    public ReceiptServiceImp(ReceiptRepository receiptRepository, ReceiptMapper receiptMapper, QrCodeGenerator qrCodeGenerator, PdfGenerator pdfGenerator, DepositPaymentService depositPaymentService, PaymentService paymentService) {
        this.receiptRepository = receiptRepository;
        this.receiptMapper = receiptMapper;
        this.qrCodeGenerator = qrCodeGenerator;
        this.pdfGenerator = pdfGenerator;
        this.depositPaymentService = depositPaymentService;
        this.paymentService = paymentService;
    }

    @Override
    public ReceiptDto createReceipt(Long id, EnumTypesReceipt typesReceipt) {

        Receipt receipt = null;

        switch (typesReceipt){
            case PAYMENT -> {
                receipt = paymentService.generatePaymentReceiptData(id);
                break;
            }
            case DEPOSIT_PAYMENT -> {
                receipt = depositPaymentService.generateDepositPaymentReceiptData(id);
                break;
            }
        }

        if(receipt == null){
            throw new BadRequestException("Error al crear el recibo");
        }

        receipt.setOperationCode(generateUniqueOperationCode());
        receipt.setQrCode(generateQRCode(receipt));


        Receipt savedReceipt = receiptRepository.save(receipt);
        return receiptMapper.toDTO(savedReceipt);
    }


    @Override
    public Receipt getReceiptById(Long id) {
       return receiptRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("No existe recibo con el id: " + id));
    }

    @Override
    public byte[] getReceiptPdfById(Long id) throws IOException {
        // Generar PDF
        Receipt receipt = receiptRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("No existe recibo con el id: " + id));
        return pdfGenerator.generateReceiptPdf(receipt);

    }

//    ***** CREAR FUNCION EN REPOSITORY PARA buscar el id del payment o deposit (dependiendo de el type) para ver si ya existe, si no, crear el receipt
    // si existe solo traerlo


    private String generateUniqueOperationCode() {
        return String.format("%04d", new Random().nextInt(10000));
    }

    private byte[] generateQRCode(Receipt receipt) {
        String qrData = "Recibo: " + receipt.getReceiptName() + " | Código: " + receipt.getOperationCode();
        try {
            return qrCodeGenerator.generateQRCodeImage(qrData, 200, 200);
        } catch (Exception e) {
            // Manejo de excepciones según tus necesidades
            return new byte[0];
        }
    }

    @Override
    @Transactional
    public byte[] getOrCreateReceiptPdf(Long id, EnumTypesReceipt typesReceipt) throws IOException {
        Receipt receipt;

        switch (typesReceipt) {
            case PAYMENT -> {
                System.out.println(id);
                System.out.println(typesReceipt);
                receipt = receiptRepository.findByPaymentId(id).orElse(null);
            }
            case DEPOSIT_PAYMENT -> {
                receipt = receiptRepository.findByDepositPaymentId(id).orElse(null);
            }
            default -> throw new BadRequestException("Tipo de recibo no válido");
        }

        if (receipt == null) {
            // Si no existe, lo creamos
            ReceiptDto created = createReceipt(id, typesReceipt);
            receipt = receiptRepository.findById(created.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Recibo creado pero no encontrado"));
        }

        return pdfGenerator.generateReceiptPdf(receipt);
    }

}
