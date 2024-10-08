package com.example.DocProject.Controller;

import com.example.DocProject.Service.WordService;
import com.example.DocProject.Service.WordServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/word")
public class WordController {

    WordService wordService = new WordServiceImpl();

    @PostMapping("/create")
    public ResponseEntity<String> createWord(@RequestParam String content, @RequestParam String fileName) {
        try {
            wordService.createWordDocument(content, fileName );
            return ResponseEntity.ok("Word document created successfully at " + fileName);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to create Word document: " + e.getMessage());
        }
    }

    @PostMapping("/wordToTxt")
    public ResponseEntity<String> convertWordToTxt(@RequestParam String wordFileName, @RequestParam String txtFileName) {
        try {
            wordService.convertWordToTxt(wordFileName , txtFileName);
            return ResponseEntity.ok("Word document " + wordFileName+ " converted to text file at " + txtFileName);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to convert Word document: " + e.getMessage());
        }
    }

    @PostMapping("/txtToWord")
    public ResponseEntity<String> convertTxtToWord(@RequestParam String txtFileName, @RequestParam String wordFileName) {
        try {
            wordService.convertTxtToWord(txtFileName , wordFileName );
            return ResponseEntity.ok("Text file " + txtFileName +" converted to Word document at " + wordFileName);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to convert text file: " + e.getMessage());
        }
    }
    @GetMapping("/files")
    public ResponseEntity<List<String>> getAllFiles() {
        List<String> fileNames = wordService.getAllFiles();
        return new ResponseEntity<>(fileNames, HttpStatus.OK);
    }

    @PostMapping("/add-signature")
    public ResponseEntity<String> addSignature(
            @RequestParam("documentName") String documentName,
            @RequestParam("signatureImageName") String signatureImageName,
            @RequestParam("info") String info) {
        try {
            wordService.addSignature(documentName, signatureImageName, info);
            return ResponseEntity.ok("Signature added successfully");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error adding signature: " + e.getMessage());
        }
    }


}
