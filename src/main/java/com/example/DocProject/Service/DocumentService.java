package com.example.DocProject.Service;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.poi.xwpf.usermodel.*;
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

        // Iterate over all document elements
        for (IBodyElement element : document.getBodyElements()) {
            if (element instanceof XWPFParagraph) {
                XWPFParagraph paragraph = (XWPFParagraph) element;
                replaceJsonDataToWord(flatJsonData, paragraph);
            } else if (element instanceof XWPFTable) {
                XWPFTable table = (XWPFTable) element;
                System.out.println("Table:");
                for (XWPFTableRow row : table.getRows()) {
                    System.out.println("  Row:");
                    for (XWPFTableCell cell : row.getTableCells()) {
                        System.out.println("    Cell:");
                        for (XWPFParagraph paragraph : cell.getParagraphs()) {
                            String paragraphText = paragraph.getText();
                            System.out.println("      Paragraph: " + paragraphText);
                            replaceJsonDataToWord(flatJsonData, paragraph);
                        }
                    }
                }
            }
        }
    }

    private void replaceJsonDataToWord(Map<String, String> flatJsonData, XWPFParagraph paragraph) {
        List<XWPFRun> runs = paragraph.getRuns();
        if (runs != null && !runs.isEmpty()) {
            StringBuilder paragraphText = new StringBuilder();
            for (XWPFRun run : runs) {
                paragraphText.append(run.getText(0));
            }

            String text = paragraphText.toString();
            for (Map.Entry<String, String> entry : flatJsonData.entrySet()) {
                String placeholder = "{{" + entry.getKey() + "}}";
                if (text.contains(placeholder)) {
                    text = text.replace(placeholder, entry.getValue());
                }
            }
            replacePlaceholderRuns(paragraph, text);
        }
    }

    private void replacePlaceholderRuns(XWPFParagraph paragraph, String finalText) {
        List<XWPFRun> runs = new ArrayList<>(paragraph.getRuns());
        for (XWPFRun run : runs) {
            paragraph.removeRun(paragraph.getRuns().indexOf(run));
        }

        XWPFRun newRun = paragraph.createRun();
        newRun.setText(finalText, 0);
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
        for (Map.Entry<String, String> entry : flatMap.entrySet()) {
            System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
        }
        return flatMap;
    }

    private void convertToPdf(File docxFile, File pdfFile) throws IOException {
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
