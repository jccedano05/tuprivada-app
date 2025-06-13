package com.jccv.tuprivadaapp.service.receipt;

import com.jccv.tuprivadaapp.dto.receipt.ReceiptDto;
import com.jccv.tuprivadaapp.model.receipt.EnumTypesReceipt;
import com.jccv.tuprivadaapp.model.receipt.Receipt;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public interface ReceiptService {
    public ReceiptDto createReceipt(Long id, EnumTypesReceipt typesReceipt);

    Receipt getReceiptById(Long id);

    byte[] getReceiptPdfById(Long id) throws IOException;

    byte[] getOrCreateReceiptPdf(Long id, EnumTypesReceipt typesReceipt) throws IOException;

}
