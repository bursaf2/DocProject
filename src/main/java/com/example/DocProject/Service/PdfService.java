package com.example.DocProject.Service;

import java.util.List;

public interface PdfService {
    public void createPdf(String filePath);
    public void modifyPdf(String filePath,String outputFilePath);
    public void extractTextFromPdf(String filePath,String textFilePath);
    public List<String> getAllFiles();
    public byte[]getFileByName(String filename);
}
