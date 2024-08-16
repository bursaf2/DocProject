package com.example.DocProject.Service;

public interface ConversionWordPdfService {

    void convertWordToPdf(String inputFileName, String outputFileName) throws Exception;
    void convertPdfToWord(String inputFilePath, String outputFilePath) throws Exception;
}
