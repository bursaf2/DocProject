package com.example.DocProject.Service;


import java.io.IOException;
import java.util.List;

public interface PdfService {
     void createPdf(String pdfName, String text, String fontName, int fontSize, String imagePath);
     void extractTextFromPdf(String filePath,String textFilePath);
     List<String> getAllFiles();
     byte[]getFileByName(String filename);
     void convertPdfToImages(String pdfFilePath) throws IOException;
     void addSignature(String pdfFilename, String imageFilename) throws IOException;
     //void signPdf(String sourceFileName, String signedFileName) throws Exception;
     void createKeyStore(String keyStoreName, String keyStorePassword, String keyAlias, String keyPassword,
                         String firstNameLastName, String organizationalUnit, String organization,
                         String city, String state, String country) throws IOException, InterruptedException;
     void signPdf(String sourceFileName, String signedFileName, String keystoreName, String password) throws Exception;
}