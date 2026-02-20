package com.jccv.tuprivadaapp.service.payment.gateway.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.jccv.tuprivadaapp.exception.BadRequestException;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.payment.gateway.PaymentTransaction;
import com.jccv.tuprivadaapp.repository.payment.gateway.PaymentTransactionRepository;
import com.jccv.tuprivadaapp.service.payment.gateway.PaymentReceiptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Implementación del servicio de generación de comprobantes de pago en PDF.
 * Utiliza iText 9 para creación de PDFs y ZXing para códigos QR.
 * 
 * Características:
 * - Diseño profesional y limpio
 * - Código QR con referencia de transacción
 * - Desglose completo de montos y comisiones
 * - Información de residente y cargo
 * - Marca de agua con logo TuPrivada
 * 
 * @author TuPrivada Development Team
 * @version 1.0
 * @since 2026-01-21
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentReceiptServiceImpl implements PaymentReceiptService {
    
    private final PaymentTransactionRepository transactionRepository;
    
    // Formatters
    private static final DateTimeFormatter DATE_TIME_FORMATTER = 
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = 
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat CURRENCY_FORMATTER = 
            NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
    
    // Colores corporativos TuPrivada
    private static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(41, 128, 185); // Azul
    private static final DeviceRgb SECONDARY_COLOR = new DeviceRgb(52, 73, 94); // Gris oscuro
    private static final DeviceRgb SUCCESS_COLOR = new DeviceRgb(39, 174, 96);  // Verde
    private static final DeviceRgb TEXT_COLOR = new DeviceRgb(44, 62, 80);      // Gris texto
    
    @Override
    @Transactional(readOnly = true)
    public byte[] generateReceipt(String transactionReference) {
        
        log.info("[Receipts] Iniciando generación de comprobante para transacción {}", 
                transactionReference);
        
        try {
            // Buscar transacción
            PaymentTransaction transaction = transactionRepository
                    .findByTransactionReference(transactionReference)
                    .orElseThrow(() -> {
                        log.warn("[Receipts] Transacción {} no encontrada", transactionReference);
                        return new ResourceNotFoundException(
                                "No se encontró la transacción con referencia: " + transactionReference);
                    });
            
            // Validar que la transacción esté completada
            if (transaction.getStatus() != PaymentTransaction.TransactionStatus.SUCCEEDED &&
                transaction.getStatus() != PaymentTransaction.TransactionStatus.CAPTURED) {
                
                log.warn("[Receipts] Intento de generar comprobante para transacción {} con estado {}",
                        transactionReference, transaction.getStatus());
                
                throw new BadRequestException(
                        "Solo se pueden generar comprobantes para pagos exitosos. " +
                        "Estado actual: " + transaction.getStatus());
            }
            
            // Generar PDF
            byte[] pdfBytes = createPdfDocument(transaction);
            
            log.info("[Receipts] Comprobante generado exitosamente para transacción {} ({} bytes)",
                    transactionReference, pdfBytes.length);
            
            return pdfBytes;
            
        } catch (BadRequestException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Receipts] Error generando comprobante para transacción {}: {}",
                    transactionReference, e.getMessage(), e);
            throw new RuntimeException("Error generando comprobante de pago: " + e.getMessage(), e);
        }
    }
    
    /**
     * Crea el documento PDF completo del comprobante.
     */
    private byte[] createPdfDocument(PaymentTransaction transaction) throws IOException {
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try (PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdfDoc = new PdfDocument(writer);
             Document document = new Document(pdfDoc)) {
            
            // Configurar márgenes
            document.setMargins(40, 40, 40, 40);
            
            // Header con título
            addHeader(document, transaction);
            
            // Código QR
            addQRCode(document, transaction.getTransactionReference());
            
            // Información de la transacción
            addTransactionInfo(document, transaction);
            
            // Información del residente y cargo
            addResidentAndChargeInfo(document, transaction);
            
            // Desglose de montos
            addAmountBreakdown(document, transaction);
            
            // Footer con nota legal
            addFooter(document);
            
        } catch (Exception e) {
            log.error("[Receipts] Error creando documento PDF: {}", e.getMessage(), e);
            throw new IOException("Error generando PDF", e);
        }
        
        return baos.toByteArray();
    }
    
    /**
     * Agrega el encabezado del comprobante.
     */
    private void addHeader(Document document, PaymentTransaction transaction) {
        
        // Título principal
        Paragraph title = new Paragraph("COMPROBANTE DE PAGO")
                .setFontSize(24)
                .setFontColor(PRIMARY_COLOR)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5);
        document.add(title);
        
        // Subtítulo
        Paragraph subtitle = new Paragraph("TuPrivada - Gestión de Condominios")
                .setFontSize(12)
                .setFontColor(SECONDARY_COLOR)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(subtitle);
        
        // Badge de estado exitoso
        Paragraph statusBadge = new Paragraph("✓ PAGO EXITOSO")
                .setFontSize(14)
                .setFontColor(ColorConstants.WHITE)
                .setBackgroundColor(SUCCESS_COLOR)
                .setPadding(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(statusBadge);
        
        // Línea separadora
        document.add(new Paragraph().setBorder(new SolidBorder(PRIMARY_COLOR, 2))
                .setMarginBottom(15));
    }
    
    /**
     * Agrega el código QR con la referencia de transacción.
     */
    private void addQRCode(Document document, String transactionReference) {
        
        try {
            // Generar código QR
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);
            
            BitMatrix bitMatrix = qrCodeWriter.encode(
                    transactionReference, 
                    BarcodeFormat.QR_CODE, 
                    200, 
                    200, 
                    hints
            );
            
            // Convertir a imagen
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            ByteArrayOutputStream qrBaos = new ByteArrayOutputStream();
            ImageIO.write(qrImage, "PNG", qrBaos);
            
            // Agregar al documento
            Image qrImageElement = new Image(ImageDataFactory.create(qrBaos.toByteArray()))
                    .setWidth(150)
                    .setHorizontalAlignment(HorizontalAlignment.CENTER)
                    .setMarginBottom(10);
            
            document.add(qrImageElement);
            
            // Leyenda del QR
            Paragraph qrLabel = new Paragraph("Escanea para verificar el pago")
                    .setFontSize(9)
                    .setFontColor(SECONDARY_COLOR)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(qrLabel);
            
        } catch (WriterException | IOException e) {
            log.warn("[Receipts] Error generando código QR: {}", e.getMessage());
            // Continuar sin QR si falla
        }
    }
    
    /**
     * Agrega la información principal de la transacción.
     */
    private void addTransactionInfo(Document document, PaymentTransaction transaction) {
        
        // Título de sección
        addSectionTitle(document, "Información de la Transacción");
        
        // Tabla de información
        Table table = new Table(UnitValue.createPercentArray(new float[]{40, 60}))
                .useAllAvailableWidth()
                .setMarginBottom(15);
        
        addInfoRow(table, "Referencia de Pago:", transaction.getTransactionReference());
        addInfoRow(table, "ID Pasarela:", transaction.getGatewayTransactionId());
        addInfoRow(table, "Método de Pago:", formatPaymentMethod(transaction.getPaymentMethod()));
        addInfoRow(table, "Estado:", "Pagado");
        
        if (transaction.getConfirmedAt() != null) {
            addInfoRow(table, "Fecha de Acreditación:", 
                    transaction.getConfirmedAt().format(DATE_TIME_FORMATTER));
        }
        
        addInfoRow(table, "Fecha de Creación:", 
                transaction.getCreatedAt().format(DATE_TIME_FORMATTER));
        
        // Información específica de OXXO
        if (transaction.getReferenceNumber() != null) {
            addInfoRow(table, "Referencia OXXO:", transaction.getReferenceNumber());
        }
        
        document.add(table);
    }
    
    /**
     * Agrega información del residente y el cargo.
     */
    private void addResidentAndChargeInfo(Document document, PaymentTransaction transaction) {
        
        // Información del residente
        if (transaction.getPayment() != null && 
            transaction.getPayment().getResident() != null) {
            
            addSectionTitle(document, "Información del Residente");
            
            Table residentTable = new Table(UnitValue.createPercentArray(new float[]{40, 60}))
                    .useAllAvailableWidth()
                    .setMarginBottom(15);
            
            var resident = transaction.getPayment().getResident();
            if (resident.getUser() != null) {
                String fullName = resident.getUser().getFirstName() + " " + 
                                resident.getUser().getLastName();
                addInfoRow(residentTable, "Nombre:", fullName);
                addInfoRow(residentTable, "Email:", resident.getUser().getEmail());
            }
            
            String bankPersonalReference = null;
            if (resident != null && resident.getUser() != null) {
                bankPersonalReference = resident.getUser().getBankPersonalReference();
                if (bankPersonalReference == null) {
                    bankPersonalReference = "No especificada";
                }
            }
            addInfoRow(residentTable, "Referencia Personal:", bankPersonalReference);
            
            document.add(residentTable);
        }
        
        // Información del cargo
        if (transaction.getPayment() != null && 
            transaction.getPayment().getCharge() != null) {
            
            addSectionTitle(document, "Información del Cargo");
            
            Table chargeTable = new Table(UnitValue.createPercentArray(new float[]{40, 60}))
                    .useAllAvailableWidth()
                    .setMarginBottom(15);
            
            var charge = transaction.getPayment().getCharge();
            addInfoRow(chargeTable, "Concepto:", charge.getTitleTypePayment());
            
            if (charge.getDescription() != null) {
                addInfoRow(chargeTable, "Descripción:", charge.getDescription());
            }
            
            if (charge.getDueDate() != null) {
                addInfoRow(chargeTable, "Fecha de Vencimiento:", 
                        charge.getDueDate().format(DATE_FORMATTER));
            }
            
            document.add(chargeTable);
        }
    }
    
    /**
     * Agrega el desglose detallado de montos y comisiones.
     */
    private void addAmountBreakdown(Document document, PaymentTransaction transaction) {
        
        addSectionTitle(document, "Desglose de Montos");
        
        // Tabla con bordes para el desglose
        Table amountTable = new Table(UnitValue.createPercentArray(new float[]{70, 30}))
                .useAllAvailableWidth()
                .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 1))
                .setMarginBottom(20);
        
        // Monto base
        addAmountRow(amountTable, "Monto del Pago:", transaction.getAmount(), false);
        
        // Comisiones si aplican
        if (transaction.getGatewayFee() != null && 
            transaction.getGatewayFee().compareTo(BigDecimal.ZERO) > 0) {
            addAmountRow(amountTable, "Comisión de Pasarela:", 
                    transaction.getGatewayFee(), false);
        }
        
        if (transaction.getPlatformFee() != null && 
            transaction.getPlatformFee().compareTo(BigDecimal.ZERO) > 0) {
            addAmountRow(amountTable, "Comisión de Plataforma:", 
                    transaction.getPlatformFee(), false);
        }
        
        if (transaction.getTaxAmount() != null && 
            transaction.getTaxAmount().compareTo(BigDecimal.ZERO) > 0) {
            addAmountRow(amountTable, "IVA:", transaction.getTaxAmount(), false);
        }
        
        // Total (con resaltado)
        BigDecimal total = transaction.getAmount();
        addAmountRow(amountTable, "TOTAL PAGADO:", total, true);
        
        document.add(amountTable);
    }
    
    /**
     * Agrega el pie de página con nota legal.
     */
    private void addFooter(Document document) {
        
        // Espacio
        document.add(new Paragraph("\n"));
        
        // Línea separadora
        document.add(new Paragraph().setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 1))
                .setMarginBottom(10));
        
        // Nota legal
        Paragraph legalNote = new Paragraph(
                "Este comprobante es un documento válido que certifica el pago realizado. " +
                "Conserve este documento para futuras referencias. " +
                "Para cualquier aclaración, contacte a la administración de su condominio.")
                .setFontSize(8)
                .setFontColor(SECONDARY_COLOR)
                .setTextAlignment(TextAlignment.JUSTIFIED)
                .setMarginBottom(10);
        document.add(legalNote);
        
        // Fecha de generación
        Paragraph generationDate = new Paragraph(
                "Documento generado el: " + LocalDateTime.now().format(DATE_TIME_FORMATTER))
                .setFontSize(8)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(generationDate);
    }
    
    /**
     * Agrega un título de sección al documento.
     */
    private void addSectionTitle(Document document, String title) {
        Paragraph sectionTitle = new Paragraph(title)
                .setFontSize(14)
                .setFontColor(PRIMARY_COLOR)
                .setMarginTop(10)
                .setMarginBottom(8);
        document.add(sectionTitle);
    }
    
    /**
     * Agrega una fila de información a una tabla.
     */
    private void addInfoRow(Table table, String label, String value) {
        // Celda de etiqueta
        Cell labelCell = new Cell()
                .add(new Paragraph(label).setFontSize(10))
                .setBorder(Border.NO_BORDER)
                .setPaddingBottom(5)
                .setFontColor(SECONDARY_COLOR);
        
        // Celda de valor
        Cell valueCell = new Cell()
                .add(new Paragraph(value != null ? value : "N/A").setFontSize(10))
                .setBorder(Border.NO_BORDER)
                .setPaddingBottom(5)
                .setFontColor(TEXT_COLOR);
        
        table.addCell(labelCell);
        table.addCell(valueCell);
    }
    
    /**
     * Agrega una fila de monto a la tabla de desglose.
     */
    private void addAmountRow(Table table, String label, BigDecimal amount, boolean isTotal) {
        
        Cell labelCell = new Cell()
                .add(new Paragraph(label)
                        .setFontSize(isTotal ? 12 : 10))
                .setBorder(Border.NO_BORDER)
                .setPaddingTop(isTotal ? 8 : 3)
                .setPaddingBottom(isTotal ? 8 : 3)
                .setPaddingLeft(5)
                .setFontColor(isTotal ? PRIMARY_COLOR : TEXT_COLOR);
        
        if (isTotal) {
            labelCell.setBackgroundColor(new DeviceRgb(240, 248, 255));
        }
        
        Cell valueCell = new Cell()
                .add(new Paragraph(CURRENCY_FORMATTER.format(amount))
                        .setFontSize(isTotal ? 12 : 10))
                .setBorder(Border.NO_BORDER)
                .setPaddingTop(isTotal ? 8 : 3)
                .setPaddingBottom(isTotal ? 8 : 3)
                .setPaddingRight(5)
                .setTextAlignment(TextAlignment.RIGHT)
                .setFontColor(isTotal ? PRIMARY_COLOR : TEXT_COLOR);
        
        if (isTotal) {
            valueCell.setBackgroundColor(new DeviceRgb(240, 248, 255));
        }
        
        table.addCell(labelCell);
        table.addCell(valueCell);
    }
    
    /**
     * Formatea el método de pago para visualización.
     */
    private String formatPaymentMethod(PaymentTransaction.PaymentMethod method) {
        if (method == null) {
            return "N/A";
        }
        
        return switch (method) {
            case CARD -> "Tarjeta de Crédito/Débito";
            case OXXO -> "OXXO Pay";
            case OXXO_PAY -> "OXXO Pay";
            case SPEI -> "Transferencia SPEI";
            case BANK_TRANSFER -> "Transferencia Bancaria";
            case CASH -> "Efectivo";
            case PAYPAL -> "PayPal";
            case WALLET -> "Monedero Digital";
        };
    }
}
