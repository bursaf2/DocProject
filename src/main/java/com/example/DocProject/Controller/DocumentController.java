package com.example.DocProject.Controller;

import com.example.DocProject.Service.DocumentService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.File;
import java.io.IOException;

@AllArgsConstructor
@RestController
@RequestMapping("/documents")
public class DocumentController {

    private DocumentService documentService;


    @PostMapping("/generatePdf")
    public String generatePdf(
            @RequestParam("templateName") String templateName,
            @RequestBody JsonNode jsonData) throws IOException {

        File templateFile = new File("uploads/" + templateName ); // it holds to word file path

        documentService.processWordTemplate(templateFile, jsonData);

        return "Word filled with name : " + templateName ;

    }
}
