package com.example.DocProject.Service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PdfServiceImpl implements PdfService {

    private final Path root = Paths.get("uploads");

    @Override
    public void createPdf(String pdfName) {
        Path filePath = root.resolve(pdfName);
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.newLineAtOffset(100, 700);
            contentStream.showText("Hello, PDFBox!");
            contentStream.endText();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            document.save(filePath.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                document.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void modifyPdf(String filePath, String outputFilePath) {
        try (PDDocument document = PDDocument.load(root.resolve(filePath).toFile())) {
            PDPageContentStream contentStream = new PDPageContentStream(document, document.getPage(0), PDPageContentStream.AppendMode.APPEND, true, true);
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.newLineAtOffset(100, 650);
            contentStream.showText("Modified with PDFBox!");
            contentStream.endText();
            contentStream.close();
            document.save(root.resolve(outputFilePath).toFile());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error while modifying PDF", e);
        }
    }

    @Override
    public void extractTextFromPdf(String pdfFilePath, String txtFilePath) {
        try (PDDocument document = PDDocument.load(root.resolve(pdfFilePath).toFile());
             FileWriter writer = new FileWriter(root.resolve(txtFilePath).toFile())) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String text = pdfStripper.getText(document);
            writer.write(text);
        } catch (IOException e) {
            throw new RuntimeException("Error while extracting text from PDF", e);
        }
    }
    @Override
    public List<String> getAllFiles() {
        try {
            return Files.list(root)
                    .filter(path -> path.toString().endsWith(".pdf"))
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Could not list files", e);
        }
    }

    @Override
    public byte[] getFileByName(String filename) {
        if (!filename.endsWith(".pdf")) {
            throw new IllegalArgumentException("Invalid file type. Only PDF files are allowed.");
        }
        try {
            Path file = root.resolve(filename);
            return Files.readAllBytes(file);
        } catch (IOException e) {
            throw new RuntimeException("Could not read file", e);
        }
    }
    @Override
    public void convertPdfToImages(String fileName) throws IOException {
        String pdfFilePath = "uploads/" + fileName + ".pdf";
        File pdfFile = new File(pdfFilePath);
        PDDocument document = PDDocument.load(pdfFile);
        PDFRenderer pdfRenderer = new PDFRenderer(document);

        for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
            BufferedImage image = pdfRenderer.renderImageWithDPI(pageIndex, 300);
            File outputFile = new File(fileName + ".png");
            ImageIO.write(image, "PNG", outputFile);
            System.out.println("Saved: " + outputFile.getAbsolutePath());
        }

        document.close();
    }




    @Override
    public void addSignature(String pdfFilename, String imageFilename) throws IOException {
        Path pdfPath = root.resolve(pdfFilename);
        Path imagePath = root.resolve(imageFilename);
        Path outputPath = root.resolve("signed_" + pdfFilename);

        try (PDDocument document = PDDocument.load(pdfPath.toFile())) {
            PDPage page = document.getPage(document.getNumberOfPages() - 1);
            PDImageXObject pdImage = PDImageXObject.createFromFile(String.valueOf(imagePath.toFile()), document);

            // Sayfadaki mevcut metni ve boş alanları analiz et
            PDFTextStripper textStripper = new PDFTextStripper();
            String pageText = textStripper.getText(document);

            // Sayfanın boyutlarını al
            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();

            // İmza fotoğrafının boyutlarını ayarla
            float imageWidth = 150;
            float imageHeight = 50;

            // İmzayı uygun bir boşluğa yerleştirme mantığı
            // Bu örnekte, sayfanın alt kısmında boş bir alan arıyoruz
            float x = (pageWidth - imageWidth) / 2; // Ortalanmış konum
            float y = 50; // Sayfanın alt kısmında
            boolean hasTextBelow = pageText.contains("some text pattern"); // İmzanın yerleştirilmesini engelleyen metinlerin kontrolü

            if (hasTextBelow) {
                // Eğer metin varsa, imzayı yerleştirmek için uygun boş alanı bul
                // Bu örnekte, boş alana imzayı yerleştirme mantığını basit tutuyoruz
                y = pageHeight - imageHeight - 50; // Sayfanın alt kısmında boş yer bırakma
            }

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true)) {
                contentStream.drawImage(pdImage, x, y, imageWidth, imageHeight);
            }

            document.save(outputPath.toFile());
        }
    }

}
