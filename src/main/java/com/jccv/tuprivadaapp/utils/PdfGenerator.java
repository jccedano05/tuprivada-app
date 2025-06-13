//package com.jccv.tuprivadaapp.utils;
//
//import com.itextpdf.io.font.constants.StandardFonts;
//import com.itextpdf.kernel.colors.ColorConstants;
//import com.itextpdf.kernel.font.PdfFontFactory;
//import com.itextpdf.kernel.pdf.PdfDocument;
//import com.itextpdf.kernel.pdf.PdfWriter;
//import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
//import com.itextpdf.layout.Document;
//import com.itextpdf.layout.borders.Border;
//import com.itextpdf.layout.borders.SolidBorder;
//import com.itextpdf.layout.element.*;
//import com.itextpdf.layout.properties.TextAlignment;
//import com.itextpdf.layout.properties.UnitValue;
//import com.jccv.tuprivadaapp.model.receipt.Receipt;
//import org.springframework.stereotype.Component;
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.time.format.DateTimeFormatter;
//
//@Component
//public class PdfGenerator {
//
//    public byte[] generateReceiptPdf(Receipt receipt) throws IOException {
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        PdfWriter writer = new PdfWriter(baos);
//        PdfDocument pdfDoc = new PdfDocument(writer);
//        Document document = new Document(pdfDoc);
//
//        // Fuente
//        var font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
//
//        // Título centrado
//        Paragraph title = new Paragraph("RECIBO DE PAGO")
//                .setFont(font)
//                .setFontSize(16)
//                .simulateBold()
//                .setTextAlignment(TextAlignment.CENTER);
//        document.add(title);
//
//        // Separador decorativo
//        SolidLine line = new SolidLine(1f);
//        LineSeparator ls = new LineSeparator(line);
//        ls.setMarginBottom(10);
//        document.add(ls);
//
//        // Datos generales
//        Table mainTable = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
//                .useAllAvailableWidth();
//
//        mainTable.addCell(getCell("Comunidad:", true));
//        mainTable.addCell(getCell(receipt.getCondominiumName(), false));
//
//        mainTable.addCell(getCell("Tipo de Recibo:", true));
//        mainTable.addCell(getCell(receipt.getReceiptName(), false));
//
//        mainTable.addCell(getCell("Código de Verificación:", true));
//        mainTable.addCell(getCell(receipt.getOperationCode(), false));
//
//        mainTable.addCell(getCell("Fecha de Pago:", true));
//        String formattedDate = receipt.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
//        mainTable.addCell(getCell(formattedDate, false));
//
//        mainTable.addCell(getCell("Monto:", true));
//        mainTable.addCell(getCell("MX$" + String.format("%,.2f", receipt.getAmount()), false));
//
//        document.add(mainTable.setMarginBottom(20));
//
//        // Concepto y Descripción
//        document.add(getLabeledSection("Concepto:", receipt.getTitle()));
//        document.add(getLabeledSection("Descripción:", receipt.getDescription()));
//
//        // Datos del residente
//        Paragraph residentTitle = new Paragraph("Datos del Residente:")
//                .simulateBold()
//                .setMarginTop(20);
//        document.add(residentTitle);
//
//        Table residentTable = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
//                .useAllAvailableWidth();
//
//        residentTable.addCell(getCell("Nombre:", true));
//        residentTable.addCell(getCell(receipt.getResidentName(), false));
//
//        residentTable.addCell(getCell("Dirección:", true));
//        residentTable.addCell(getCell(receipt.getResidentAddress(), false));
//
//        document.add(residentTable.setMarginBottom(30));
//
//        // Footer de agradecimiento
//        Paragraph footer = new Paragraph("Gracias por su pago puntual.")
//                .setFontSize(10)
//                .setFontColor(ColorConstants.GRAY)
//                .setTextAlignment(TextAlignment.CENTER);
//        document.add(footer);
//
//        document.close();
//        return baos.toByteArray();
//    }
//
//    // Helpers
//    private Cell getCell(String text, boolean bold) {
//        Paragraph p = new Paragraph(text);
//        if (bold) p.simulateBold();
//        return new Cell().add(p).setBorder(Border.NO_BORDER);
//    }
//
//    private Paragraph getLabeledSection(String label, String content) {
//        return new Paragraph()
//                .add(new Text(label).simulateBold())
//                .add(" " + content)
//                .setMarginBottom(10);
//    }
//}
package com.jccv.tuprivadaapp.utils;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.jccv.tuprivadaapp.model.receipt.Receipt;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Component
public class PdfGenerator {

    private static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(7, 100, 69);        // #076445
    private static final DeviceRgb SECONDARY_COLOR = new DeviceRgb(40, 48, 111);     // #28306F
    private static final DeviceRgb TEXT_GRAY = new DeviceRgb(100, 100, 100);

    public byte[] generateReceiptPdf(Receipt receipt) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        var font = PdfFontFactory.createFont(StandardFonts.HELVETICA);


        // Encabezado con logo izquierdo, título centrado y logo derecho
            // Logo izquierdo
//            var logoLeftPath = new ClassPathResource("static/images/logo.png");
//            Image logoLeft = new Image(ImageDataFactory.create(logoLeftPath.getInputStream().readAllBytes()));
//            logoLeft.scaleToFit(80, 60);

            Image logoLeft = null;
            try {
                var logoLeftPath = new ClassPathResource("static/images/logo.png");
                logoLeft = new Image(ImageDataFactory.create(logoLeftPath.getInputStream().readAllBytes()));
                logoLeft.scaleToFit(80, 60);
            } catch (Exception e) {
                System.err.println("No se pudo cargar el logo izquierdo: " + e.getMessage());
            }

            // Logo derecho (de la comunidad)
//            var logoRightPath = new ClassPathResource("static/images/logo_adco.jpeg");
//            Image logoRight = new Image(ImageDataFactory.create(logoRightPath.getInputStream().readAllBytes()));
//            logoRight.scaleToFit(80, 60);

            Image logoRight = null;
            if (receipt.getCondominium() != null && receipt.getCondominium().getLogoImageName() != null) {
                try {
                    var logoRightPath = new ClassPathResource("static/images/condominiumsLogo/" + receipt.getCondominium().getLogoImageName());
                    logoRight = new Image(ImageDataFactory.create(logoRightPath.getInputStream().readAllBytes()));
                    logoRight.scaleToFit(80, 60);
                } catch (Exception e) {
                    System.err.println("No se pudo cargar el logo del condominio: " + e.getMessage());
                }
            }

            // Título centrado
            Paragraph title = new Paragraph("RECIBO DE PAGO")
                    .setFont(font)
                    .setFontSize(18)
                    .simulateBold()
                    .setFontColor(SECONDARY_COLOR)
                    .setTextAlignment(TextAlignment.CENTER);

            // Tabla para logos y título
            Table headerTable = new Table(UnitValue.createPercentArray(new float[]{1, 2, 1}))
                    .useAllAvailableWidth()
                    .setMarginBottom(10);


        if(logoLeft != null) {
            headerTable.addCell(new Cell().add(logoLeft)
                    .setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE));
        }

            headerTable.addCell(new Cell().add(title)
                    .setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE));

           if(logoRight != null){
               headerTable.addCell(new Cell().add(logoRight)
                       .setBorder(Border.NO_BORDER)
                       .setTextAlignment(TextAlignment.RIGHT)
                       .setVerticalAlignment(VerticalAlignment.MIDDLE));
           }

            document.add(headerTable);



        // Línea divisoria
        document.add(new LineSeparator(new SolidLine(1f)).setMarginBottom(15));

        // Tabla principal
        Table mainTable = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
                .useAllAvailableWidth();

        mainTable.addCell(getCell("Comunidad:", true));
        mainTable.addCell(getCell(receipt.getCondominium().getName(), false));

        mainTable.addCell(getCell("Tipo de Recibo:", true));
        mainTable.addCell(getCell(receipt.getReceiptName(), false));

        mainTable.addCell(getCell("Código de Verificación:", true));
        mainTable.addCell(getCell(receipt.getOperationCode(), false));

        mainTable.addCell(getCell("Fecha de Pago:", true));
        mainTable.addCell(getCell(receipt.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), false));

        mainTable.addCell(getCell("Monto:", true));
        mainTable.addCell(getCell("MX$" + String.format("%,.2f", receipt.getAmount()), false));

        document.add(mainTable.setMarginBottom(15));

        // Concepto y descripción
        document.add(getLabeledSection("Concepto:", receipt.getTitle()));
        document.add(getLabeledSection("Descripción:", receipt.getDescription()));

        // Datos del residente
        Paragraph residentTitle = new Paragraph("Datos del Residente:")
                .simulateBold()
                .setFontColor(SECONDARY_COLOR)
                .setMarginTop(20);
        document.add(residentTitle);

        Table residentTable = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
                .useAllAvailableWidth();

        residentTable.addCell(getCell("Nombre:", true));
        residentTable.addCell(getCell(receipt.getResidentName(), false));

        residentTable.addCell(getCell("Dirección:", true));
        residentTable.addCell(getCell(receipt.getResidentAddress(), false));

        document.add(residentTable.setMarginBottom(20));

        // QR
        if (receipt.getQrCode() != null) {
            Image qrImage = new Image(ImageDataFactory.create(receipt.getQrCode()));
            qrImage.setWidth(100);
            qrImage.setHeight(100);
            qrImage.setHorizontalAlignment(HorizontalAlignment.CENTER);
            document.add(new Paragraph("Verificación QR:").setTextAlignment(TextAlignment.CENTER).setFontColor(TEXT_GRAY));
            document.add(qrImage.setMarginBottom(20));
        }

        Table footerTable = new Table(1)
                .useAllAvailableWidth();

        Cell footerCell = new Cell()
                .add(new Paragraph("Gracias por su pago.").setFontColor(DeviceRgb.WHITE).setTextAlignment(TextAlignment.CENTER).setFontSize(10))
                .setBackgroundColor(SECONDARY_COLOR)
                .setBorder(Border.NO_BORDER)
                .setPaddingTop(10)
                .setPaddingBottom(10);

        footerTable.addCell(footerCell);
        document.add(footerTable);

        document.close();
        return baos.toByteArray();
    }

    // Helpers
    private Cell getCell(String text, boolean bold) {
        Paragraph p = new Paragraph(text);
        if (bold) p.simulateBold().setFontColor(SECONDARY_COLOR);
        else p.setFontColor(DeviceRgb.BLACK);
        return new Cell().add(p).setBorder(Border.NO_BORDER);
    }

    private Paragraph getLabeledSection(String label, String content) {
        return new Paragraph()
                .add(new Text(label).simulateBold().setFontColor(PRIMARY_COLOR))
                .add(" " + content)
                .setMarginBottom(10);
    }
}
