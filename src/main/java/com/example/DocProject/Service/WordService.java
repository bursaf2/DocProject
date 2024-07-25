package com.example.DocProject.Service;

import java.util.List;

public interface WordService {
    void createWordDocument(String content, String filename);
    void convertWordToTxt(String wordFilePath, String txtFilePath);
    void convertTxtToWord(String txtFilePath, String wordFilePath);
    List<String> getAllFiles();

}
