package com.example.DocProject.Controller;

import com.example.DocProject.Service.WordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/word")
public class WordController {

    @Autowired
    private WordService wordService;

    @PostMapping("/create")
    public ResponseEntity<String> createWord(@RequestParam String content, @RequestParam String fileName) {
        try {
            wordService.createWordDocument(content, fileName +".docx");
            return ResponseEntity.ok("Word document created successfully at " + fileName);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to create Word document: " + e.getMessage());
        }
    }

    @PostMapping("/wordToTxt")
    public ResponseEntity<String> convertWordToTxt(@RequestParam String wordFileName, @RequestParam String txtFileName) {
        try {
            wordService.convertWordToTxt(wordFileName + ".docx", txtFileName + ".txt");
            return ResponseEntity.ok("Word document " + wordFileName+ " converted to text file at " + txtFileName);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to convert Word document: " + e.getMessage());
        }
    }

    @PostMapping("/txtToWord")
    public ResponseEntity<String> convertTxtToWord(@RequestParam String txtFileName, @RequestParam String wordFileName) {
        try {
            wordService.convertTxtToWord(txtFileName + ".txt", wordFileName + ".docx");
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


}
