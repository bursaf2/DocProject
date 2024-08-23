package com.example.DocProject.Service;

import org.springframework.stereotype.Service;

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
