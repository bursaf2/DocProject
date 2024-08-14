package com.example.DocProject.Controller;

import com.example.DocProject.Service.ConversionWordPdfService;
import com.example.DocProject.Service.ConversionWordPdfServiceImpl;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/convert")
public class ConversionWordPdfController {

    ConversionWordPdfService conversionwordpdfservice = new ConversionWordPdfServiceImpl();

    @PostMapping("/word-to-pdf")
    public String convertWordToPdf(@RequestParam("fileName") String fileName) {
        try {
            String inputFilePath = "uploads/" + fileName + ".docx";
            String outputFilePath = "uploads/" + fileName + ".pdf";
            conversionwordpdfservice .convertWordToPdf(inputFilePath, outputFilePath);
            return "File converted to pdf : " + outputFilePath;
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed conversion: " + e.getMessage();
        }
    }


    @PostMapping("/pdf-to-word")
    public String convertPdfToWord(@RequestParam("fileName") String fileName) {
        try {
            String inputFilePath = "uploads/" + fileName + ".pdf";
            String outputFilePath = "uploads/" + fileName + ".docx";
            conversionwordpdfservice.convertPdfToWord(inputFilePath, outputFilePath);
            return "File converted to Word: " + outputFilePath;
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed conversion: " + e.getMessage();
        }
    }
}
