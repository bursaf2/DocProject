package com.example.DocProject.Service;
import com.example.DocProject.model.KeyValuePair;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DocumentService {

    ConversionWordPdfService conversionwordpdfservice = new ConversionWordPdfServiceImpl();

    public void processWordTemplate(File templateFile, JsonNode jsonData) throws IOException {
        // Load the template file into an XWPFDocument
        XWPFDocument document = new XWPFDocument(templateFile.toURI().toURL().openStream());

        // Replace placeholders with the data provided in JSON
        replacePlaceholders(document, jsonData);
        //replacePlaceholdersInHeadersAndFooters(document, jsonData);


        // Determine the output file names
        String fullFileName = templateFile.getName();
        int dotIndex = fullFileName.lastIndexOf(".");
        String filledFileName = (dotIndex == -1) ? fullFileName : fullFileName.substring(0, dotIndex);
        String filledWordFileName = filledFileName + "_filled.docx";
        File filledDocxFile = new File("uploads/" + filledWordFileName);

        // Write the filled document to a file
        try (FileOutputStream out = new FileOutputStream(filledDocxFile)) {
            document.write(out);
        }
        /*
        // Convert the filled Word document to PDF
        try {
            conversionwordpdfservice.convertWordToPdf(filledFileName + "_filled");
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert Word to PDF: " + e.getMessage(), e);
        }
         */
    }
    private void replacePlaceholdersInHeadersAndFooters(XWPFDocument document, JsonNode jsonData) {
        List<KeyValuePair> flatJsonData = flattenJson(jsonData, "");

        // Accessing headers and footers via XWPFDocument
        XWPFHeaderFooterPolicy headerFooterPolicy = document.getHeaderFooterPolicy();

        if (headerFooterPolicy != null) {
            // Replace placeholders in headers
            replaceTextInHeaderOrFooter(headerFooterPolicy.getHeader(XWPFHeaderFooterPolicy.DEFAULT), flatJsonData);


            // Replace placeholders in footers
            replaceTextInHeaderOrFooter(headerFooterPolicy.getFooter(XWPFHeaderFooterPolicy.DEFAULT), flatJsonData);
        }
    }

    private void replaceTextInHeaderOrFooter(XWPFHeaderFooter headerFooter, List<KeyValuePair> flatJsonData) {
        if (headerFooter != null) {
            // Replace placeholders in paragraphs
            for (XWPFParagraph paragraph : headerFooter.getParagraphs()) {
                replaceJsonDataParagraph(flatJsonData, paragraph);
            }
            // Replace placeholders in tables
            for (XWPFTable table : headerFooter.getTables()) {
                replaceJsonDataTable(flatJsonData, table);
            }
        }
    }

    private void replacePlaceholders(XWPFDocument document, JsonNode jsonData) {
        List<KeyValuePair> flatJsonData = flattenJson(jsonData, "");

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

            // Remove backticks from the text for easier matching
            text = text.replace("`", "");

            // Replace expressions in the text
            text = replaceExpressions(text, flatJsonData);

            // Replace placeholders in the text
            for (KeyValuePair pair : flatJsonData) {
                String placeholder = "{{" + pair.getKey().replace("`", "") + "}}";
                if (text.contains(placeholder)) {
                    text = text.replace(placeholder, pair.getValue());
                }
            }

            // Preserve existing runs and replace text
            preserveRuns(paragraph, text);
        }
    }
    private void preserveRuns(XWPFParagraph paragraph, String finalText) {
        List<XWPFRun> runs = paragraph.getRuns();
        int runIndex = 0;
        int textIndex = 0;

        while (runIndex < runs.size() && textIndex < finalText.length()) {
            XWPFRun run = runs.get(runIndex);
            String runText = run.getText(0);
            if (runText != null) {
                int runLength = runText.length();
                if (textIndex + runLength <= finalText.length()) {
                    run.setText(finalText.substring(textIndex, textIndex + runLength), 0);
                } else {
                    run.setText(finalText.substring(textIndex), 0);
                }
                textIndex += runLength;
            }
            runIndex++;
        }

        while (runIndex < runs.size()) {
            paragraph.removeRun(runIndex);
        }

        if (textIndex < finalText.length()) {
            XWPFRun run = paragraph.createRun();
            run.setText(finalText.substring(textIndex), 0);
        }
    }

    public void rowAdder(List<KeyValuePair> flatJsonData, XWPFTable table) {
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
                                numberOfRows = pair.getArraySize() - 1; // because we have one row
                                break outerLoop; // it finds array key and break all loop
                            }
                        }
                    }
                }
            }
        }
        if (numberOfRows > 0) {  // this part add row according to number of size
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
        List<KeyValuePair> flatJsonDataCopy = new ArrayList<>();
        for (KeyValuePair kvp : flatJsonData) {
            flatJsonDataCopy.add(kvp.clone());
        }
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

                        for (KeyValuePair pair : flatJsonDataCopy) {
                            String placeholder = "{{" + pair.getKey() + "}}";
                            if (text.contains(placeholder)) {
                                text = text.replace(placeholder, pair.getValue());
                                if (pair.getType().equals("ARRAY")) {    // this part check all array item and provide print all items
                                    pair.setKey(null);
                                }
                            }
                        }

                        preserveRuns(paragraph, text);
                    }
                }
            }
        }
    }

    private List<KeyValuePair> flattenJson(JsonNode jsonNode, String prefix) {
        List<KeyValuePair> flatList = new ArrayList<>();

        flattenJsonHelper(jsonNode, prefix, flatList);

        // Tüm liste elemanlarını sırayla yazdırabilir veya işleyebilirsin
        for (KeyValuePair kvp : flatList) {
            System.out.println("Key: " + kvp.getKey() + ", Value: " + kvp.getValue() + ", Type: " + kvp.getType() + ", Size: " + kvp.getArraySize());
        }

        return flatList;
    }

    private void flattenJsonHelper(JsonNode jsonNode, String prefix, List<KeyValuePair> flatList) {
        if (jsonNode.isObject()) {
            jsonNode.fields().forEachRemaining(entry -> {
                String newPrefix = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
                flattenJsonHelper(entry.getValue(), newPrefix, flatList);
            });
        } else if (jsonNode.isArray()) {
            int arraySize = jsonNode.size();
            for (JsonNode element : jsonNode) {
                String arrayPrefix = prefix;
                flattenJsonHelper(element, arrayPrefix, flatList);
            }

            flatList.add(new KeyValuePair(prefix, null, "ARRAY", arraySize, null));
        } else if (jsonNode.isValueNode()) {
            flatList.add(new KeyValuePair(prefix, jsonNode.asText(), "VALUE", -1, null));
        }
    }


    private String evaluateExpression(String expression, List<KeyValuePair> flatJsonData) {
        Pattern sumPattern = Pattern.compile("\\$sum\\((.*?)\\)");
        Matcher sumMatcher = sumPattern.matcher(expression);
        if (sumMatcher.find()) {
            String path = sumMatcher.group(1);
            double sum = 0;
            for (KeyValuePair pair : flatJsonData) {
                // Check if pair.getKey() is not null and starts with the path
                if (pair.getKey() != null && pair.getKey().startsWith(path)) {
                    try {
                        sum += Double.parseDouble(pair.getValue());
                    } catch (NumberFormatException e) {
                        e.printStackTrace(); // Handle parsing issues
                    }
                }
            }
            expression = expression.replace("$sum(" + path + ")", String.valueOf(sum));
        }

        // Evaluate the rest of the expression
        return evaluateSimpleExpression(expression, flatJsonData);
    }

    private String replaceExpressions(String text, List<KeyValuePair> flatJsonData) {
        Pattern exprPattern = Pattern.compile("\\{\\{expr\\((.*?)\\)\\}\\}");
        Matcher matcher = exprPattern.matcher(text);
        while (matcher.find()) {
            String expression = matcher.group(1);
            String result = evaluateExpression(expression, flatJsonData);
            text = text.replace("{{expr(" + expression + ")}}", result);
        }
        return text;
    }

    private String evaluateSimpleExpression(String expression, List<KeyValuePair> flatJsonData) {
        // Replace variables in the expression with their values from flatJsonData
        for (KeyValuePair pair : flatJsonData) {
            String variable = pair.getKey();
            if (variable != null && expression.contains(variable)) {
                expression = expression.replace(variable, pair.getValue());
            }
        }
        // Evaluate the expression
        // Here, we can use a simple script engine or another evaluation method
        // For simplicity, let's use a script engine
        try {
            return String.valueOf(eval(expression));
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR";
        }
    }

    private Object eval(String expression) throws Exception {
        // A simple evaluation method for mathematical expressions
        // You can enhance this to handle more complex expressions or use a library
        return new javax.script.ScriptEngineManager().getEngineByName("JavaScript").eval(expression);
    }
}

