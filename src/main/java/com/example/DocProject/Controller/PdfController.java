package com.example.DocProject.Controller;

import com.example.DocProject.Service.PdfService;
import com.example.DocProject.Service.PdfServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
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


    @PostMapping("/add-signature")
    public ResponseEntity<String> addSignature(@RequestParam("pdfFilename") String pdfFilename,
                                               @RequestParam("imageFilename") String imageFilename) {
        try {
            // PDF'ye imza ekle
            pdfService.addSignature(pdfFilename, imageFilename);

            return new ResponseEntity<>("Signature added successfully. Output file: signed_" + pdfFilename, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error adding signature: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/sign")
    public String signPdf(@RequestParam String sourceFileName, @RequestParam String signedFileName, @RequestParam String keystoreName, String password ) {
        try {
            pdfService.signPdf(sourceFileName, signedFileName, keystoreName, password);
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



}
