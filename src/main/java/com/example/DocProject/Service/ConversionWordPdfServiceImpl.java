package com.example.DocProject.Service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

@Service
public class ConversionWordPdfServiceImpl implements ConversionWordPdfService {

    private static final String LIBREOFFICE_PATH = "C:\\Program Files\\LibreOffice\\program\\soffice.exe"; // LibreOffice path

    @Override
    public void convertWordToPdf(String inputFilePath, String outputFilePath) throws Exception {

        ProcessBuilder processBuilder = new ProcessBuilder(
                LIBREOFFICE_PATH,
                "--headless",
                "--convert-to",
                "pdf",
                inputFilePath,
                "--outdir",
                new File(outputFilePath).getParent()
        );

        Process process = processBuilder.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("LibreOffice conversion( word to pdf ) operation failed.");
        }
    }
    @Override
    public void convertPdfToWord(String inputFilePath, String outputFilePath) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder(
                LIBREOFFICE_PATH,
                "--headless",
                "--convert-to",
                "docx",
                inputFilePath,
                "--outdir",
                new File(outputFilePath).getParent()
        );

        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("LibreOffice conversion operation failed.");
        }
    }


}
