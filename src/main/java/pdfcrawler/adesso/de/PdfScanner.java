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
    private static final String FILE_SUFFIX = ".pdf";

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
                if (line.startsWith(DATE_KEY)) {
                    date.set(extractLineData(line, DATE_KEY));
                }
            });

            name.set(extractNameFromFileName(file));

            // TODO: If only the name or the date is available, should we not display the error message
            //  and then continue writing the available data to the CSV?
            if (name.toString().isBlank() || date.toString().isBlank()) {
                var message = "Missing Data in file %s. Name: %s - Date: %s";
                LoggingService.addErrorToLog(
                    format(message, file.getFileName(), name.toString(), date.toString())
                );
            } else {
                pdfData.put(name.toString(), date.toString());
            }

        } else {
            LoggingService.addErrorToLog(file.getFileName().toString().concat(" - File is encrypted"));
        }
    }

    private String extractNameFromFileName(Path file) {
        String fileName = file.getFileName().toString().replace(FILE_SUFFIX, "");
        String[] nameComponent = fileName.split("_");
        String name = "";
        try {
            // TODO: How to deal with names like "Hans von der Wiese"?
            name =  nameComponent[nameComponent.length-2]
                    .concat(" ")
                    .concat(nameComponent[nameComponent.length-1]);
        } catch (Exception e) {
           LoggingService.addErrorToLog("File name does not support (.*)Firstname_Lastname.pdf syntax.");
        }

        return name;
    }

    private String extractLineData(String line, String str) {
        return line.replaceAll(format("%s|:|", str.trim()), "");
    }
}
