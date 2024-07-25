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
    public ResponseEntity<String> createWord(@RequestParam String content, @RequestParam String filename) {
        try {
            wordService.createWordDocument(content, filename);
            return ResponseEntity.ok("Word document created successfully at " + filename);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to create Word document: " + e.getMessage());
        }
    }

    @PostMapping("/wordToTxt")
    public ResponseEntity<String> convertWordToTxt(@RequestParam String wordFilePath, @RequestParam String txtFilePath) {
        try {
            wordService.convertWordToTxt(wordFilePath, txtFilePath);
            return ResponseEntity.ok("Word document converted to text file at " + txtFilePath);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to convert Word document: " + e.getMessage());
        }
    }

    @PostMapping("/txtToWord")
    public ResponseEntity<String> convertTxtToWord(@RequestParam String txtFilePath, @RequestParam String wordFilePath) {
        try {
            wordService.convertTxtToWord(txtFilePath, wordFilePath);
            return ResponseEntity.ok("Text file converted to Word document at " + wordFilePath);
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
