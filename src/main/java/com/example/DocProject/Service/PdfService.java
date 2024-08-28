package com.example.DocProject.Service;


import java.io.IOException;
import java.util.List;

public interface PdfService {
     void createPdf(String pdfName, String text, String fontName, int fontSize, String imagePath);
     void extractTextFromPdf(String filePath,String textFilePath);
     List<String> getAllFiles();
     byte[]getFileByName(String filename);
     void convertPdfToImages(String pdfFilePath) throws IOException;
     void createKeyStore(String keyStoreName, String keyStorePassword, String keyAlias, String keyPassword,
                         String firstNameLastName, String organizationalUnit, String organization,
                         String city, String state, String country) throws IOException, InterruptedException;
     void signPdf(String sourceFileName, String keystoreName, String password) throws Exception;
     void convertImageToPdf(String imagePath) throws IOException;
     void convertImageToPdfWithOCR(String imageFile) throws Exception;
     void mergePdfs(List<String> sourceFileNames, String outputFileName) throws IOException;
     void splitPdf(String sourceFileName, int startPage, int endPage) throws IOException;
}
