package com.example.DocProject.Service;

import com.example.DocProject.model.KeyValuePair;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DocumentService {

    ConversionWordPdfService conversionwordpdfservice = new ConversionWordPdfServiceImpl();

    public void processWordTemplate(File templateFile,boolean pdf, JsonNode jsonData) throws IOException, InvalidFormatException {
        // Load the template file into an XWPFDocument
        XWPFDocument document = new XWPFDocument(templateFile.toURI().toURL().openStream());

        // Replace placeholders with the data provided in JSON
        replacePlaceholders(document, jsonData);

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

        // Convert the filled Word document to PDF
        if(pdf) {
            try {
                conversionwordpdfservice.convertWordToPdf(filledFileName + "_filled");
            } catch (Exception e) {
                throw new RuntimeException("Failed to convert Word to PDF: " + e.getMessage(), e);
            }
        }
    }

    private void replacePlaceholders(XWPFDocument document, JsonNode jsonData) throws IOException, InvalidFormatException {
        List<KeyValuePair> flatJsonData = flattenJson(jsonData, "");


        // 1. Adım: Kapak sayfasındaki resmi çıkar ve kaydet
        List<XWPFPictureData> pictures = document.getAllPictures();
        if (!pictures.isEmpty()) {
            XWPFPictureData coverImage = pictures.get(0); // İlk resmi alıyoruz
            try (FileOutputStream fos = new FileOutputStream("uploads/cover_image.png")) {
                fos.write(coverImage.getData());
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // 2. Adım: Kapak sayfasındaki metni işleyin (bu örnekte sadece resmi kaldırıyoruz)
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            for (XWPFRun run : paragraph.getRuns()) {
                run.removeBreak(); // Gereksiz satır sonlarını kaldır
                if (run.getEmbeddedPictures().size() > 0) {
                    run.getEmbeddedPictures().clear(); // Resmi kaldır
                }
            }
        }


        for (XWPFFooter footer : document.getFooterList()) {

            // Paragrafları Almak ve Resimleri İşlemek
            for (XWPFParagraph paragraph : footer.getParagraphs()) {
                replaceJsonDataParagraph(flatJsonData, paragraph);
            }

            // Tabloları Almak ve Resimleri İşlemek
            for (XWPFTable table : footer.getTables()) {
                replaceJsonDataTable(flatJsonData, table);
            }
        }

        for (XWPFHeader header : document.getHeaderList()) {

            // Paragrafları Almak ve Resimleri İşlemek
            for (XWPFParagraph paragraph : header.getParagraphs()) {
                replaceJsonDataParagraph(flatJsonData, paragraph);
            }

            // Tabloları Almak ve Resimleri İşlemek
            for (XWPFTable table : header.getTables()) {
                replaceJsonDataTable(flatJsonData, table);
            }
        }


        // Iterate over all document elements
        for (IBodyElement element : document.getBodyElements()) {
            if (element instanceof XWPFParagraph paragraph) {
                replaceJsonDataParagraph(flatJsonData, paragraph); // if element equals to paragraph
            } else if (element instanceof XWPFTable table) {
                replaceJsonDataTable(flatJsonData, table);  // if element equals to table
            }
        }





        // 3. Adım: Aynı resmi arka plan olarak ekleyin
        XWPFParagraph newParagraph = document.createParagraph();
        XWPFRun newRun = newParagraph.createRun();
        FileInputStream fis = new FileInputStream("uploads/cover_image.png");
        newRun.addPicture(fis, XWPFDocument.PICTURE_TYPE_PNG, "uploads/cover_image.png", Units.toEMU(595), Units.toEMU(842));
        fis.close();
    }


    private void replaceJsonDataParagraph(List<KeyValuePair> flatJsonData, XWPFParagraph paragraph) {


        for (KeyValuePair pair : flatJsonData) {

            String placeholder = "{{" + pair.getKey() + "}}";
            if (paragraph.getText().contains(placeholder)) {
                String replacedText = paragraph.getText().replace(placeholder, pair.getValue());
                replacedText = replacedText.replace("`", "");
                // Replace expressions in the text
                replacedText = replaceExpressions(replacedText, flatJsonData);
                preserveRuns(paragraph, replacedText);
            }
        }
    }

    private void preserveRuns(XWPFParagraph paragraph, String finalText) {
        // split paragraph to rows
        String[] lines = finalText.split("\n");

        // clean recent runs
        int numberOfRuns = paragraph.getRuns().size();
        for (int i = numberOfRuns - 1; i >= 0; i--) {
            paragraph.removeRun(i);
        }

        // add rows to runs
        for (int i = 0; i < lines.length; i++) {
            XWPFRun run = paragraph.createRun();
            run.setText(lines[i]);
            // If not last row,add break
            if (i < lines.length - 1) {
                run.addBreak();
            }
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
                        text = text.replace("`", "");


                        for (KeyValuePair pair : flatJsonDataCopy) {
                            String placeholder = "{{" + pair.getKey() + "}}";
                            if (text.contains(placeholder)) {
                                text = text.replace(placeholder, pair.getValue());
                                preserveRuns(paragraph, text);

                                if (pair.getType().equals("ARRAY")) {    // this part check all array item and provide print all items
                                    pair.setKey(null);
                                }
                            }
                        }

                        //preserveRuns(paragraph, text);
                    }
                }
            }
        }
    }

    private List<KeyValuePair> flattenJson(JsonNode jsonNode, String prefix) {
        List<KeyValuePair> flatList = new ArrayList<>();
        String type = "";
        int size = -1;
        flattenJsonHelper(jsonNode, prefix, flatList, type, size);
        flatList.forEach(System.out::println); // Print out each key-value pair
        return flatList;
    }

    private void flattenJsonHelper(JsonNode jsonNode, String prefix, List<KeyValuePair> flatList, String type, int size) {
        if (jsonNode.isObject()) {
            jsonNode.fields().forEachRemaining(entry -> {
                String newPrefix = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
                flattenJsonHelper(entry.getValue(), newPrefix, flatList, type, size);
            });
        } else if (jsonNode.isArray()) {
            for (int i = 0; i < jsonNode.size(); i++) {
                String newPrefix = prefix;
                flattenJsonHelper(jsonNode.get(i), newPrefix, flatList, "ARRAY", jsonNode.size());
            }
        } else if (jsonNode.isValueNode()) {
            flatList.add(new KeyValuePair(prefix, jsonNode.asText(), type, size));
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
        Pattern exprPattern = Pattern.compile("\\{\\{expr\\((.*?)\\)}}");
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

