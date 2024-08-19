package com.example.DocProject.Service;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;
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
    public void createWordDocument(String content, String fileName) {
        try (XWPFDocument document = new XWPFDocument();
             FileOutputStream out = new FileOutputStream(root.resolve(fileName).toFile())) {
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


    @Override
    public void addSignature(String documentName, String signatureImageName, String info) throws IOException {
        Path documentPath = root.resolve(documentName);
        Path imagePath = root.resolve(signatureImageName);


        try (XWPFDocument document = new XWPFDocument(new FileInputStream(documentPath.toFile()))) {


            XWPFParagraph textParagraph = document.createParagraph();
            XWPFRun textRun = textParagraph.createRun();



            textRun.addBreak();
            textRun.addBreak();
            textRun.setBold(true);
            textRun.setText("SIGNATURE");
            textRun.addBreak();
            textRun.addBreak();


            String[] lines = info.split("_");
            for (int i = 0; i < lines.length; i++) {
                if (i > 0) {
                    textRun.addBreak();
                }
                textRun.setBold(true);
                textRun.setText(lines[i]);
            }

            textRun.setBold(true);
            textRun.setFontSize(12);



            byte[] imageBytes = Files.readAllBytes(imagePath);



            XWPFParagraph paragraph = document.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.addPicture(new ByteArrayInputStream(imageBytes), XWPFDocument.PICTURE_TYPE_PNG, signatureImageName, Units.toEMU(200), Units.toEMU(50));


            try (FileOutputStream out = new FileOutputStream(documentPath.toFile())) {
                document.write(out);
            }
        } catch (InvalidFormatException e) {
            throw new RuntimeException(e);
        }
    }


}
