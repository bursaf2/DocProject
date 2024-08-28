package com.example.DocProject.Service;


public interface ConversionWordPdfService {

    void convertWordToPdf(String fileName) throws Exception;
    void convertPdfToWord(String fileName) throws Exception;
}
