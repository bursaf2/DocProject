package com.example.DocProject.Service;

public interface PdfService {
    public void createPdf(String filePath);
    public void modifyPdf(String filePath,String outputFilePath);
    public void extractTextFromPdf(String filePath,String textFilePath);
}
