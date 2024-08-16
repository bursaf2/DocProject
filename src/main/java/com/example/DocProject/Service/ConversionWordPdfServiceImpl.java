package com.example.DocProject.Service;

import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.JodConverter;
import org.jodconverter.local.office.LocalOfficeManager;
import org.springframework.stereotype.Service;

import java.io.*;

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
    public void convertPdfToWord(String inputFilePath, String outputFilePath) throws Exception {

        OfficeManager officeManager = LocalOfficeManager.builder()
                .officeHome(new File(LIBREOFFICE_PATH).getParentFile())
                .build();

        try {
            officeManager.start();

            JodConverter.convert(new File(inputFilePath))
                    .to(new File(outputFilePath))
                    .execute();

            System.out.println("Dönüştürme işlemi başarılı.");

        } finally {
            if (officeManager != null) {
                officeManager.stop();
            }
        }
    }

}