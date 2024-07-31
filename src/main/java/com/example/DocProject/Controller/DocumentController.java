package com.example.DocProject.Controller;

import com.example.DocProject.Service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @PostMapping("/generatePdf")
    public String generatePdf(
            @RequestParam("templateName") String templateName,
            @RequestParam("outputName") String outputName,
            @RequestBody JsonNode jsonData) throws IOException {

        // Şablon dosyasının yolu
        File templateFile = new File("uploads/" + templateName);

        // PDF dosyasının yolu
        File outputFile = new File("uploads/" + outputName + ".pdf");

        // Word şablonunu JSON verileriyle doldur ve PDF'e dönüştür
        documentService.processWordTemplate(templateFile, jsonData, outputFile);

        return "PDF oluşturuldu: " + outputFile.getAbsolutePath();

    }
}
