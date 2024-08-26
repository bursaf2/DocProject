package com.example.DocProject.Service;

import com.aspose.pdf.Document;
import com.aspose.pdf.SaveFormat;
import org.springframework.stereotype.Service;

@Service
public class ConversionWordPdfServiceImpl implements ConversionWordPdfService {

    private static final String LIBREOFFICE_PATH = "C:\\Program Files\\LibreOffice\\program\\soffice.exe";
    private static final String UPLOADS_DIR = "uploads/";

    @Override
    public void convertWordToPdf(String fileName) throws Exception {
        // Append .docx if the file extension is missing
        Document doc = new Document("uploads/"+fileName);
        doc.save("converted.pdf");
    }

    @Override
    public void convertPdfToWord(String fileName) throws Exception {
        Document document = new Document("uploads/"+fileName);
        document.save("uploads/converted.docx", SaveFormat.DocX);
    }


}
