package com.example.DocProject.Service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PdfServiceImpl implements PdfService {


    @Override
    public void createPdf(String pdfName) {
        String filePath = "uploads/" + pdfName;
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.newLineAtOffset(100, 700);
            contentStream.showText("Hello, PDFBox!");
            contentStream.endText();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            document.save(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                document.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void modifyPdf(String filePath, String outputFilePath) {
        try (PDDocument document = PDDocument.load(new File(filePath))) {
            PDPageContentStream contentStream = new PDPageContentStream(document, document.getPage(0), PDPageContentStream.AppendMode.APPEND, true, true);
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.newLineAtOffset(100, 650);
            contentStream.showText("Modified with PDFBox!");
            contentStream.endText();
            contentStream.close();
            document.save(outputFilePath);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error while modifying PDF", e);
        }
    }

    @Override
    public void extractTextFromPdf(String pdfFilePath, String txtFilePath) {
        try (PDDocument document = PDDocument.load(new File(pdfFilePath))) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String text = pdfStripper.getText(document);
            try (FileWriter writer = new FileWriter(txtFilePath)) {
                writer.write(text);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error while extracting text from PDF", e);
        }
    }
}
