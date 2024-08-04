package com.example.DocProject.Service;

import com.example.DocProject.model.KeyValuePair;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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
        // add this method and some fix some problems
        //convertToPdf(filledDocxFile, outputFile);
    }

    private void replacePlaceholders(XWPFDocument document, JsonNode jsonData) {
        List<KeyValuePair> flatJsonData =  flattenJson(jsonData, "");

        // Iterate over all document elements
        for (IBodyElement element : document.getBodyElements()) {
            if (element instanceof XWPFParagraph paragraph) {
                replaceJsonDataParagraph(flatJsonData, paragraph); // if element equals to paragraph
            } else if (element instanceof XWPFTable table) {
                replaceJsonDataTable(flatJsonData, table);  // if element equals to table
            }
        }
    }

    private void replaceJsonDataParagraph(List<KeyValuePair> flatJsonData, XWPFParagraph paragraph) {
        List<XWPFRun> runs = paragraph.getRuns();
        if (runs != null && !runs.isEmpty()) {
            StringBuilder paragraphText = new StringBuilder();
            for (XWPFRun run : runs) {
                paragraphText.append(run.getText(0));
            }
            String text = paragraphText.toString();
            for (KeyValuePair pair : flatJsonData) {
                String placeholder = "{{" + pair.getKey() + "}}";
                if (text.contains(placeholder)) {
                    text = text.replace(placeholder, pair.getValue());
                }
            }
            replacePlaceholderRuns(paragraph, text);
        }
    }
    public void rowAdder(List<KeyValuePair> flatJsonData,XWPFTable table) {
        int numberOfRows = 0;
        outerLoop:
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell1 : row.getTableCells()) {
                for (XWPFParagraph paragraph : cell1.getParagraphs()) {
                    String text = paragraph.getText();
                    for (KeyValuePair pair : flatJsonData) {
                        String placeholder = "{{" + pair.getKey() + "}}";
                        if (text.contains(placeholder)) {
                            if (pair.getType().equals("ARRAY")) {
                                numberOfRows = pair.getArraySize() -1 ; // because we have one row
                                System.out.println("Row adder array size " + numberOfRows);
                                break outerLoop; // it finds array key and break all loop
                            }
                        }
                    }
                }
            }
        }
        if(numberOfRows > 0) {  // this part add row according to number of size
            XWPFTableRow templateRow = table.getRow(1);
            for (int i = 0; i < numberOfRows; i++) {
                XWPFTableRow newRow = table.createRow();
                for (int j = 0; j < templateRow.getTableCells().size(); j++) {
                    XWPFTableCell cell = newRow.getCell(j);
                    if (cell == null) {
                        cell = newRow.createCell();
                    }
                    cell.setText(templateRow.getCell(j).getText());
                }
            }
        }
    }
    public void replaceJsonDataTable(List<KeyValuePair> flatJsonData, XWPFTable table) {
        rowAdder(flatJsonData, table); // fix row number
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                for (XWPFParagraph paragraph : cell.getParagraphs()) {
                    List<XWPFRun> runs = paragraph.getRuns();
                    if (runs != null && !runs.isEmpty()) {
                        StringBuilder paragraphText = new StringBuilder();
                        for (XWPFRun run : runs) {
                            paragraphText.append(run.getText(0));
                        }
                        String text = paragraphText.toString();

                        for (KeyValuePair pair : flatJsonData) {
                            String placeholder = "{{" + pair.getKey() + "}}";
                            if (text.contains(placeholder)) {
                                text = text.replace(placeholder, pair.getValue());
                                if(pair.getType().equals("ARRAY")) {    // this part check all array item and provide print all items
                                    pair.setKey(null);
                                }
                            }
                        }

                        replacePlaceholderRuns(paragraph, text);
                    }
                }
            }
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


    private List<KeyValuePair> flattenJson(JsonNode jsonNode, String prefix) {
        List<KeyValuePair> flatList = new ArrayList<>();
        String type ="";
        int size = -1;
        flattenJsonHelper(jsonNode, prefix, flatList,type, size);
        flatList.forEach(System.out::println); // Print out each key-value pair
        return flatList;
    }

    private void flattenJsonHelper(JsonNode jsonNode, String prefix, List<KeyValuePair> flatList,String type,int size) {
        if (jsonNode.isObject()) {
            jsonNode.fields().forEachRemaining(entry -> {
                String newPrefix = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
                flattenJsonHelper(entry.getValue(), newPrefix, flatList, type, size);
            });
        } else if (jsonNode.isArray()) {
            for (int i = 0; i < jsonNode.size(); i++) {
                String newPrefix = prefix;
                flattenJsonHelper(jsonNode.get(i), newPrefix, flatList,"ARRAY",jsonNode.size());
            }
        } else if (jsonNode.isValueNode()) {
            flatList.add(new KeyValuePair(prefix, jsonNode.asText(),type,size));
        }
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
