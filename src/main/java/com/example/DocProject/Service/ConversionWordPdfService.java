package com.example.DocProject.Service;

import java.io.IOException;

public interface ConversionWordPdfService {

    void convertWordToPdf(String fileName) throws Exception;
    void convertPdfToWord(String fileName) throws Exception;
}
