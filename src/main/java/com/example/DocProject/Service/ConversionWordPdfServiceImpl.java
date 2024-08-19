package com.example.DocProject.Service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

@Service
public class ConversionWordPdfServiceImpl implements ConversionWordPdfService {

    private static final String LIBREOFFICE_PATH = "C:\\Program Files\\LibreOffice\\program\\soffice.exe";
    private static final String UPLOADS_DIR = "uploads/";

    @Override
    public void convertWordToPdf(String fileName) throws Exception {
        // Append .docx if the file extension is missing
        if (!fileName.endsWith(".docx")) {
            fileName += ".docx";
        }

        String inputFilePath = UPLOADS_DIR + fileName;
        String outputDir = UPLOADS_DIR;

        // Debug: Log the command being executed
        System.out.println("Executing LibreOffice command:");
        System.out.println(LIBREOFFICE_PATH + " --headless --convert-to pdf " + inputFilePath + " --outdir " + outputDir);

        ProcessBuilder processBuilder = new ProcessBuilder(
                LIBREOFFICE_PATH,
                "--headless",
                "--convert-to",
                "pdf",
                inputFilePath,
                "--outdir",
                outputDir
        );

        Process process = processBuilder.start();

        // Capture the output and error streams for debugging
        try (BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
             BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

            String s;
            System.out.println("Standard output:");
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
            }

            System.out.println("Error output (if any):");
            while ((s = stdError.readLine()) != null) {
                System.err.println(s);
            }

        }

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("LibreOffice conversion (Word to PDF) operation failed with exit code " + exitCode);
        }

        System.out.println("Conversion completed successfully. PDF saved in: " + outputDir);
    }

    @Override
    public void convertPdfToWord(String fileName) throws Exception {

    }


}
