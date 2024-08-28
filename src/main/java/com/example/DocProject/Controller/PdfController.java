package com.example.DocProject.Controller;

import com.example.DocProject.Service.PdfService;
import lombok.AllArgsConstructor;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    PdfService pdfService;

    @PostMapping("/create")
    public ResponseEntity<String> createPdf(@RequestParam String fileName,
                                            @RequestParam String text,
                                            @RequestParam String fontName,
                                            @RequestParam int fontSize,
                                            @RequestParam(required = false) String imageName) {
        pdfService.createPdf(fileName, text, fontName, fontSize, imageName);
        return ResponseEntity.ok("PDF created successfully at " + fileName);
    }


    @PostMapping("/extractText")
    public ResponseEntity<String> extractTextFromPdf(@RequestParam String pdfFileName, @RequestParam String txtFileName) {
        try {
            pdfService.extractTextFromPdf(pdfFileName, txtFileName);
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


    @PostMapping("/sign")
    public String signPdf(@RequestParam String sourceFileName, @RequestParam String keystoreName, String password) {
        try {
            pdfService.signPdf(sourceFileName, keystoreName, password);
            return "PDF signed successfully!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error signing PDF: " + e.getMessage();
        }
    }


    @PostMapping("/createKeystore")
    public String createKeyStore(
            @RequestParam String keyStoreName,
            @RequestParam String keyStorePassword,
            @RequestParam String keyAlias,
            @RequestParam String keyPassword,
            @RequestParam String firstNameLastName,
            @RequestParam String organizationalUnit,
            @RequestParam String organization,
            @RequestParam String city,
            @RequestParam String state,
            @RequestParam String country) {
        try {
            pdfService.createKeyStore(keyStoreName, keyStorePassword, keyAlias, keyPassword,
                    firstNameLastName, organizationalUnit, organization,
                    city, state, country);
            return "Keystore created successfully.";
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "Failed to create keystore.";
        }
    }


    @PostMapping("/imageToPdfOCR")
    public String imageToPdfOCR(
            @RequestParam String imagePath,
            @RequestParam Boolean isOCR) throws Exception {
        try {
            if (isOCR){
                pdfService.convertImageToPdfWithOCR(imagePath);
            }else {
                pdfService.convertImageToPdf(imagePath);
            }
            return "Image converted to PDF successfully.";
        } catch (IOException | TesseractException e) {
            e.printStackTrace();
            return "Failed to convert image to PDF.";
        }
    }

    @PostMapping("/merge")
    public String mergePdfs(@RequestParam String[] fileNames, @RequestParam String outputFileName) {
        try {
            pdfService.mergePdfs(Arrays.asList(fileNames), outputFileName);
            return "PDFs successfully merged.";
        } catch (IOException e) {
            return "Error merging PDFs: " + e.getMessage();
        }
    }

    @PostMapping("/split")
    public String splitPdf(@RequestParam String fileName,
                           @RequestParam int startPage,
                           @RequestParam int endPage) {
        try {
            pdfService.splitPdf(fileName, startPage, endPage);
            return "Selected pages successfully split from PDF.";
        } catch (IOException e) {
            return "Error splitting PDF: " + e.getMessage();
        }
    }

}
