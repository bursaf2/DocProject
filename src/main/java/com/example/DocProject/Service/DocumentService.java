package com.example.DocProject.Service;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DocumentService {

    public void processWordTemplate(File templateFile, JsonNode jsonData, File outputFile) throws IOException {

        XWPFDocument document = new XWPFDocument(templateFile.toURI().toURL().openStream());

        replacePlaceholders(document, jsonData);

        File filledDocxFile = new File("uploads/filled_template.docx");
        try (FileOutputStream out = new FileOutputStream(filledDocxFile)) {
            document.write(out);
        }

        //convertToPdf(filledDocxFile, outputFile);
    }

    private void replacePlaceholders(XWPFDocument document, JsonNode jsonData) {
        Map<String, String> flatJsonData = flattenJson(jsonData, "");

        document.getParagraphs().forEach(paragraph -> {
            String text = paragraph.getText();
            for (Map.Entry<String, String> entry : flatJsonData.entrySet()) {
                String placeholder = "{{" + entry.getKey() + "}}";
                if (text.contains(placeholder)) {
                    text = text.replace(placeholder, entry.getValue());
                }
            }
            String finalText = text;
            // Check if the paragraph has any runs
            List<XWPFRun> runs = new ArrayList<>(paragraph.getRuns());
            if (!runs.isEmpty()) {
                // Clear all runs
                for (XWPFRun run : runs) {
                    paragraph.removeRun(paragraph.getRuns().indexOf(run));
                }

                // Add the new run with the updated text
                XWPFRun newRun = paragraph.createRun();
                newRun.setText(finalText);
            } else {
                // If no runs exist, simply create a new run with the text
                XWPFRun newRun = paragraph.createRun();
                newRun.setText(finalText);
            }
        });
    }

    private Map<String, String> flattenJson(JsonNode jsonNode, String prefix) {
        Map<String, String> flatMap = new HashMap<>();
        if (jsonNode.isObject()) {
            jsonNode.fields().forEachRemaining(entry -> {
                String newPrefix = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
                flatMap.putAll(flattenJson(entry.getValue(), newPrefix));
            });
        } else if (jsonNode.isValueNode()) {
            flatMap.put(prefix, jsonNode.asText());
        }
        return flatMap;
    }


    private void convertToPdf(File docxFile, File pdfFile) throws IOException {
        // LibreOffice veya üçüncü parti kütüphaneleri kullanarak PDF'e dönüştürme
        String command = String.format("libreoffice --headless --convert-to pdf --outdir %s %s",
                pdfFile.getParent(), docxFile.getAbsolutePath());
        Process process = Runtime.getRuntime().exec(command);

        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
