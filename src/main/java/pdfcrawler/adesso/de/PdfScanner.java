package pdfcrawler.adesso.de;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.String.format;

public class PdfScanner {

    private final HashMap<String, String> pdfData;
    private static final String NAME_KEY = "Name";
    private static final String DATE_KEY = "Eingang";
    private PDFTextStripper tStripper = null;

    /**
     * initializes the scanner
     */
    public PdfScanner() {
        try {
            PDFTextStripperByArea stripper = new PDFTextStripperByArea();
            stripper.setSortByPosition(true);
            tStripper = new PDFTextStripper();
        } catch (IOException e) {
            LoggingService.addExceptionToLog(e);
        }

        pdfData = new HashMap<>();
    }


    /**
     * @param path Path to PDF files
     * @return extracted data
     */
    public HashMap<String, String> scanFile(String path) {
        try {
            Files.walkFileTree(Paths.get(path), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (!Files.isDirectory(file)) {
                        try (PDDocument document = PDDocument.load(new File(file.toAbsolutePath().toString()))) {
                            extractFromFile(document, file);
                        } catch (IOException e) {
                            LoggingService.addExceptionToLog(e);
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            LoggingService.addExceptionToLog(e);
        }

        return pdfData;
    }

    private void extractFromFile(PDDocument document, Path file) throws IOException {
        if (!document.isEncrypted()) {

            String pdfFileInText = tStripper.getText(document);

            List<String> lines = Arrays.asList(pdfFileInText.split("\\r?\\n"));
            AtomicReference<String> name = new AtomicReference<>("");
            AtomicReference<String> date = new AtomicReference<>("");
            lines.forEach(line -> {
                if (line.startsWith(NAME_KEY)) {
                    name.set(extractLineData(line, NAME_KEY));
                }
                if (line.startsWith(DATE_KEY)) {
                    date.set(extractLineData(line, DATE_KEY));
                }
            });

            if (name.toString().isBlank() || date.toString().isBlank()) {
                var message = "Missing Data in file %s. Name: %s - Date: %s";
                LoggingService.addErrorToLog(
                    format(message, file.getFileName(), name.toString(), date.toString())
                );
            } else {
                pdfData.put(name.toString(), date.toString());
            }

        }
    }

    private String extractLineData(String line, String str) {
        return line.replaceAll(format("%s|:|", str.trim()), "");
    }
}
