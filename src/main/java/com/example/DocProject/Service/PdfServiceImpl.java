package com.example.DocProject.Service;


import com.itextpdf.signatures.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;





import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.StampingProperties;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;



@Service
public class PdfServiceImpl implements PdfService {

    private final Path root = Paths.get("uploads");

    @Override
    public void createPdf(String pdfName, String text, String fontName, int fontSize, String imageName) {
        Path filePath = root.resolve(pdfName);
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            contentStream.beginText();

            // Set the font based on user input
            PDType1Font font = PDType1Font.HELVETICA;
            switch (fontName.toUpperCase()) {
                case "HELVETICA_BOLD":
                    font = PDType1Font.HELVETICA_BOLD;
                    break;
                case "TIMES_ROMAN":
                    font = PDType1Font.TIMES_ROMAN;
                    break;
                case "COURIER":
                    font = PDType1Font.COURIER;
                    break;
                // Add more fonts as needed
            }

            contentStream.setFont(font, fontSize);
            contentStream.newLineAtOffset(100, 100);
            contentStream.showText(text);
            contentStream.endText();

            // Add the image if provided
            if (imageName != null && !imageName.isEmpty()) {
                Path imagePath = root.resolve(imageName);

                PDImageXObject image = PDImageXObject.createFromFile(imagePath.toString(), document);
                float imageWidth = image.getWidth() * 0.2f; // 50% of original size
                float imageHeight = image.getHeight() * 0.2f; // 50% of original size

                contentStream.drawImage(image, 100, 150, imageWidth, imageHeight); // Adjust position and size as needed

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
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


            PDFTextStripper textStripper = new PDFTextStripper();
            String pageText = textStripper.getText(document);


            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();


            float imageWidth = 150;
            float imageHeight = 50;


            float x = (pageWidth - imageWidth) / 2;
            float y = 50;
            boolean hasTextBelow = pageText.contains("some text pattern");
            if (hasTextBelow) {
                y = pageHeight - imageHeight - 50;
            }

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true)) {
                contentStream.drawImage(pdImage, x, y, imageWidth, imageHeight);
            }

            document.save(outputPath.toFile());
        }
    }

    @Override
    public void signPdf(String sourceFileName, String signedFileName, String keystoreName, String password) throws Exception {
        BouncyCastleProvider provider = new BouncyCastleProvider();
        Security.addProvider(provider);


        Path keystorePath = root.resolve(keystoreName);



        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(new FileInputStream(keystorePath.toFile()), password.toCharArray());
        String alias = (String) ks.aliases().nextElement();
        PrivateKey pk = (PrivateKey) ks.getKey(alias, password.toCharArray());
        Certificate[] chain = ks.getCertificateChain(alias);

        Path srcPath = root.resolve(sourceFileName);
        Path destPath = root.resolve(signedFileName);

        try (PdfReader reader = new PdfReader(srcPath.toString());
             FileOutputStream fos = new FileOutputStream(destPath.toFile())) {

            PdfSigner signer = new PdfSigner(reader, fos, new StampingProperties());
            IExternalSignature pks = new PrivateKeySignature(pk, DigestAlgorithms.SHA256, provider.getName());
            IExternalDigest digest = new BouncyCastleDigest();
            ICrlClient crlClient = null;

            PdfSignatureAppearance appearance = signer.getSignatureAppearance();
            appearance.setReason("Digital signature example");
            appearance.setLocation("Location");
            appearance.setContact("Contact");
            appearance.setSignatureCreator("Creator");
            signer.setFieldName("Signature");
            signer.signDetached(digest, pks, chain, null, null, (ITSAClient) crlClient, 0, PdfSigner.CryptoStandard.CMS);
        }
    }

    @Override
    public void createKeyStore(String keyStoreName, String keyStorePassword, String keyAlias, String keyPassword,
                               String firstNameLastName, String organizationalUnit, String organization,
                               String city, String state, String country) throws IOException, InterruptedException {


        Path keyStorePath = root.resolve(keyStoreName);


        List<String> commands = new ArrayList<>();
        commands.add("keytool");
        commands.add("-genkeypair");
        commands.add("-alias");
        commands.add(keyAlias);
        commands.add("-keyalg");
        commands.add("RSA");
        commands.add("-keysize");
        commands.add("2048");
        commands.add("-validity");
        commands.add("365");
        commands.add("-keystore");
        commands.add(keyStorePath.toString());
        commands.add("-storepass");
        commands.add(keyStorePassword);
        commands.add("-keypass");
        commands.add(keyPassword);

        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        Process process = processBuilder.start();

        // Provide the answers to the keytool questions
        try (OutputStream os = process.getOutputStream()) {
            os.write((firstNameLastName + "\n").getBytes());
            os.write((organizationalUnit + "\n").getBytes());
            os.write((organization + "\n").getBytes());
            os.write((city + "\n").getBytes());
            os.write((state + "\n").getBytes());
            os.write((country + "\n").getBytes());
            os.write(("y\n").getBytes()); // Answer 'yes' to the confirmation
            os.flush();
        }

        int exitCode = process.waitFor();
        if (exitCode == 0) {
            System.out.println("Keystore created successfully.");
        } else {
            System.err.println("Failed to create keystore.");
        }
    }

}