package com.example.DocProject.Controller;

import com.example.DocProject.Service.PdfService;
import com.example.DocProject.Service.PdfServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    PdfService pdfService = new PdfServiceImpl();

    @PostMapping("/create")
    public ResponseEntity<String> createPdf(@RequestParam String fileName) {
        pdfService.createPdf(fileName + ".pdf");
        return ResponseEntity.ok("PDF created successfully at " + fileName);
    }

    @PostMapping("/modify")
    public ResponseEntity<String> modifyPdf(@RequestParam String fileName, @RequestParam String newFileName) {

        try {
            pdfService.modifyPdf(fileName + ".pdf", newFileName + ".pdf");
            return ResponseEntity.ok("PDF modified successfully and saved to " + newFileName);
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("Failed to modify PDF: " + e.getMessage());
        }
    }

    @PostMapping("/extractText")
    public ResponseEntity<String> extractTextFromPdf(@RequestParam String pdfFileName, @RequestParam String txtFileName) {
        try {
            pdfService.extractTextFromPdf(pdfFileName + ".pdf", txtFileName + ".txt");
            return ResponseEntity.ok("Text extracted successfully to " + txtFileName);
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("Failed to extract text from PDF: " + e.getMessage());
        }
    }
    @GetMapping("/files")
    public ResponseEntity<List<String>> getAllFiles() {
        List<String> fileNames = pdfService.getAllFiles();
        return new ResponseEntity<>(fileNames, HttpStatus.OK);
    }

    @GetMapping("/files/{fileName}")
    public ResponseEntity<byte[]> getFileByName(@PathVariable String fileName) {
        byte[] fileContent = pdfService.getFileByName(fileName);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                .body(fileContent);
    }


    @PostMapping("/pdf-to-image")
    public ResponseEntity<String> convertPdfToImages(@RequestParam String fileName) {
        try {
            pdfService.convertPdfToImages(fileName);
            return ResponseEntity.ok("PDF converted to images successfully.");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to convert PDF to images: " + e.getMessage());
        }
    }
}
