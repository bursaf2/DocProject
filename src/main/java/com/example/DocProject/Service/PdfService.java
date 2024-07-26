package com.example.DocProject.Service;

import java.io.IOException;
import java.util.List;

public interface PdfService {
     void createPdf(String filePath);
     void modifyPdf(String filePath,String outputFilePath);
     void extractTextFromPdf(String filePath,String textFilePath);
     List<String> getAllFiles();
     byte[]getFileByName(String filename);
     void convertPdfToImages(String pdfFilePath) throws IOException;
}
