package com.example.DocProject.Controller;
import com.example.DocProject.Service.PdfService;
import com.example.DocProject.Service.PdfServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    PdfService pdfService = new PdfServiceImpl();

    @PostMapping("/create")
    public ResponseEntity<String> createPdf(@RequestParam String filePath) {
        pdfService.createPdf(filePath);
        return ResponseEntity.ok("PDF created successfully at " + filePath);
    }

    @PostMapping("/modify")
    public ResponseEntity<String> modifyPdf(@RequestParam String filePath, @RequestParam String outputFilePath) {
        try {
            pdfService.modifyPdf(filePath, outputFilePath);
            return ResponseEntity.ok("PDF modified successfully and saved to " + outputFilePath);
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("Failed to modify PDF: " + e.getMessage());
        }
    }

    @PostMapping("/extractText")
    public ResponseEntity<String> extractTextFromPdf(@RequestParam String pdfFilePath, @RequestParam String txtFilePath) {
        try {
            pdfService.extractTextFromPdf(pdfFilePath, txtFilePath);
            return ResponseEntity.ok("Text extracted successfully to " + txtFilePath);
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("Failed to extract text from PDF: " + e.getMessage());
        }
    }
}
