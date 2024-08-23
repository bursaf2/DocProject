package com.example.DocProject.Service;

import com.itextpdf.signatures.*;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.Word;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.encoding.WinAnsiEncoding;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.StampingProperties;

import java.io.FileInputStream;
import java.io.FileOutputStream;

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
        String pdfFilePath = root.resolve(fileName).toString();

        File pdfFile = new File(pdfFilePath);
        PDDocument document = PDDocument.load(pdfFile);
        PDFRenderer pdfRenderer = new PDFRenderer(document);

        for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
            BufferedImage image = pdfRenderer.renderImageWithDPI(pageIndex, 300);

            String pngFileName = fileName.replace(".pdf", ".png");
            File outputFile = new File(root.resolve(pngFileName).toString());
            ImageIO.write(image, "PNG", outputFile);
            System.out.println("Saved: " + outputFile.getAbsolutePath());
        }

        document.close();
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

    @Override
    public void convertImageToPdf(String imagePath) throws IOException {
        // Dosya isimlerini ve yollarını ayarla
        Path imageFilePath = root.resolve(imagePath);
        String outputPdfPath = imageFilePath.toString().replace(".jpg", ".pdf").replace(".png", ".pdf");

        // create a new PDF document
        try (PDDocument document = new PDDocument()) {
            // add new page
            PDPage page = new PDPage();
            document.addPage(page);

            // load image
            PDImageXObject pdImage = PDImageXObject.createFromFile(imageFilePath.toString(), document);

            // add image to content
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.drawImage(pdImage, 0, 0, page.getMediaBox().getWidth(), page.getMediaBox().getHeight());
            }

            // save PDF
            document.save(outputPdfPath);
        }
    }



    @Override
    public void convertImageToPdfWithOCR(String imageFile) throws Exception {

        System.setProperty("TESSDATA_PREFIX", "C:/Program Files/JetBrains/IntelliJ IDEA 2024.1.4/plugins/tesseract/tessdata");

        // Step 1: Perform OCR on the image to extract text with bounding boxes
        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath(System.getProperty("TESSDATA_PREFIX"));
        tesseract.setLanguage("tur+eng+deu");


        BufferedImage image = ImageIO.read(root.resolve(imageFile).toFile());

        // Use 2 for word-level recognition, which is equivalent to PageIteratorLevel.WORD
        List<Word> words = tesseract.getWords(image, 2);

        // Step 2: Create a new PDF document
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(new PDRectangle(image.getWidth(), image.getHeight()));
        document.addPage(page);
        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        // Step 3: Embed the image into the PDF
        PDImageXObject pdImage = PDImageXObject.createFromFileByContent(root.resolve(imageFile).toFile(), document);
        contentStream.drawImage(pdImage, 0, 0, page.getMediaBox().getWidth(), page.getMediaBox().getHeight());

        // Step 4: Overlay the extracted text in invisible mode at the exact coordinates
        contentStream.setFont(PDType1Font.HELVETICA, 12); // Adjust the font size as needed

        for (Word word : words) {
            // Get the position and size of each word
            float x = (float) word.getBoundingBox().x;
            float y = (float) word.getBoundingBox().y;
            float wordHeight = (float) word.getBoundingBox().height;
            float wordWidth = (float) word.getBoundingBox().width;

            // Convert y-coordinate to PDF coordinate system
            y = page.getMediaBox().getHeight() - y - wordHeight;


            PDExtendedGraphicsState gs = new PDExtendedGraphicsState();
            gs.setNonStrokingAlphaConstant(0.0F);
            contentStream.setGraphicsStateParameters(gs);

            // Overlay the text
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, wordHeight); // Match the font size to the original word height
            contentStream.newLineAtOffset(x, y);
            contentStream.showText(remove(word.getText()));
            contentStream.endText();
        }

        contentStream.close();
        int dotIndex = imageFile.lastIndexOf(".");

        String cleanedFileName = (dotIndex == -1) ? imageFile : imageFile.substring(0, dotIndex);

        String outputPdfFile = "ocr_" + cleanedFileName + ".pdf";

        // Save the document
        document.save(root.resolve(outputPdfFile).toFile());
        document.close();
    }

    //Helper function for OCR. Removes invalid characters
    public static String remove(String test) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < test.length(); i++) {
            if (WinAnsiEncoding.INSTANCE.contains(test.charAt(i))) {
                b.append(test.charAt(i));
            }
        }
        return b.toString();
    }


    public void mergePdfs(List<String> sourceFileNames, String outputFileName) throws IOException {
        PDFMergerUtility merger = new PDFMergerUtility();
        merger.setDestinationFileName(root.resolve(outputFileName).toString());

        for (String sourceFileName : sourceFileNames) {
            merger.addSource(root.resolve(sourceFileName).toFile());
        }

        merger.mergeDocuments(null);
    }

    public void splitPdf(String sourceFileName, int startPage, int endPage) throws IOException {
        File sourceFile = root.resolve(sourceFileName).toFile();
        PDDocument document = PDDocument.load(sourceFile);

        int totalPages = document.getNumberOfPages();
        startPage = Math.max(1, startPage);
        endPage = Math.min(totalPages, endPage);

        // Create new PDDocument and add the pages
        PDDocument newDocument = new PDDocument();
        for (int i = startPage - 1; i < endPage; i++) {
            newDocument.addPage(document.getPage(i));
        }

        // save the new PDF
        String baseName = sourceFileName.replace(".pdf", "");
        String outputPath = root.resolve(baseName + "_pages_" + startPage + "_to_" + endPage + ".pdf").toString();
        newDocument.save(outputPath);
        newDocument.close();

        document.close();
    }

}
