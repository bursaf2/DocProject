package com.example.DocProject.Service;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WordServiceImpl implements WordService {

    private final Path root = Paths.get("uploads");

    @Override
    public void createWordDocument(String content, String filename) {
        try (XWPFDocument document = new XWPFDocument();
             FileOutputStream out = new FileOutputStream(root.resolve(filename).toFile())) {
            XWPFParagraph paragraph = document.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.setText(content);
            document.write(out);
        } catch (IOException e) {
            throw new RuntimeException("Error creating Word document", e);
        }
    }

    @Override
    public void convertWordToTxt(String wordFilePath, String txtFilePath) {
        try (XWPFDocument document = new XWPFDocument(new FileInputStream(root.resolve(wordFilePath).toFile()));
             FileWriter writer = new FileWriter(root.resolve(txtFilePath).toFile())) {
            document.getParagraphs().forEach(paragraph -> {
                try {
                    writer.write(paragraph.getText() + System.lineSeparator());
                } catch (IOException e) {
                    throw new RuntimeException("Error writing to text file", e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Error converting Word to text", e);
        }
    }

    @Override
    public void convertTxtToWord(String txtFilePath, String wordFilePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(root.resolve(txtFilePath).toFile()));
             XWPFDocument document = new XWPFDocument();
             FileOutputStream out = new FileOutputStream(root.resolve(wordFilePath).toFile())) {
            String line;
            while ((line = reader.readLine()) != null) {
                XWPFParagraph paragraph = document.createParagraph();
                XWPFRun run = paragraph.createRun();
                run.setText(line);
            }
            document.write(out);
        } catch (IOException e) {
            throw new RuntimeException("Error converting text to Word", e);
        }
    }
    @Override
    public List<String> getAllFiles() {
        try {
            return Files.list(root)
                    .filter(path -> path.toString().endsWith(".docx"))
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Could not list files", e);
        }
    }


}
