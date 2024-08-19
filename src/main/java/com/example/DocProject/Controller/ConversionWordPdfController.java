package com.example.DocProject.Controller;

import com.example.DocProject.Service.ConversionWordPdfService;
import com.example.DocProject.Service.ConversionWordPdfServiceImpl;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/convert")
public class ConversionWordPdfController {

    ConversionWordPdfService conversionWordPdfService = new ConversionWordPdfServiceImpl();

    @PostMapping("/word-to-pdf")
    public String convertWordToPdf(@RequestParam("fileName") String fileName) {
        try {
            conversionWordPdfService.convertWordToPdf(fileName);
            return "File converted to PDF: " + fileName ;
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed conversion: " + e.getMessage();
        }
    }

    @PostMapping("/pdf-to-word")
    public String convertPdfToWord(@RequestParam("fileName") String fileName) {
        try {
            conversionWordPdfService.convertPdfToWord(fileName);
            return "File converted to Word: " + fileName ;
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed conversion: " + e.getMessage();
        }
    }
}
