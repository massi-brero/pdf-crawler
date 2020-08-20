package pdfcrawler.adesso.de;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import pdfcrawler.adesso.de.csv.CSVErrorStatus;
import pdfcrawler.adesso.de.exception.ExtractDataException;
import pdfcrawler.adesso.de.logging.ApplicationLogger;
import pdfcrawler.adesso.de.logging.LoggingService;
import pdfcrawler.adesso.de.utilities.Config;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.String.format;

public class PdfScanner {

    private final HashMap<String, String> pdfData;
    private static final String NAME_KEY = "Name";
    private static final String DATE_KEY = "Eingang:";
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
            LoggingService.addExceptionToLog(e, true);
        }

        pdfData = new HashMap<>();
    }


    /**
     * @param path Path to PDF files
     * @return extracted data
     */
    public HashMap<String, String> scanFile(String path) {
        LoggingService.log(String.format("Datei [%s] einlesen:", path), true);
        try {
            Files.walkFileTree(Paths.get(path), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    String fileAbsolutePath = file.toAbsolutePath().toString();
                    CSVErrorStatus.selectedDocuments.add(fileAbsolutePath);

                    try (PDDocument document = PDDocument.load(new File(fileAbsolutePath))) {
                        HashMap<String, String> readData = extractFromFile(document, file);
                        pdfData.putAll(readData);

                        CSVErrorStatus.addReadSuccess(fileAbsolutePath);

                        String message = format("\tName[%s]:Datum[%s]\n",
                                readData.keySet().stream().findFirst().orElse("N/A"),
                                readData.values().stream().findFirst().orElse("N/A")
                        );
                        ApplicationLogger.noFormattingLog(message);

                    } catch (IOException e) {
                        CSVErrorStatus.notReadDocuments.add(fileAbsolutePath);
                        ApplicationLogger.noFormattingLog(
                                format("\tFEHLER:\tEs ist ein Fehler beim Einlesen passiert:\n\t %s\n",
                                        e.getMessage())
                        );
                        LoggingService.addExceptionToLog(e);
                    } catch (ExtractDataException e) {
                        CSVErrorStatus.addReadError(fileAbsolutePath);
                        LoggingService.addExceptionToLog(e);
                        ApplicationLogger.noFormattingLog(String.format("\t%s\n", e.getMessage()));
                    }

                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            LoggingService.addExceptionToLog(e);
        }

        return pdfData;
    }

    private HashMap<String, String> extractFromFile(PDDocument document, Path file) throws IOException, ExtractDataException {
        HashMap<String, String> readData;

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
                throw new ExtractDataException("Der Name konnnte nicht extrahiert werden.");
            } else if (date.toString().isBlank()) {
                throw new ExtractDataException("Das Datum konnnte nicht extrahiert werden.");
            } else {
                readData = new HashMap<>();
                readData.put(name.toString(), date.toString());
            }

            return readData;

        } else {
            throw new ExtractDataException(String.format("FEHLER:\tDatei[%s] - File is verschlüsselt", file.toAbsolutePath().toString()));
        }
    }

    private String extractNameFromFileName(Path file) {
        String fileName = file.getFileName().toString().replace(FILE_SUFFIX, "");
        String[] nameComponent = fileName.split("_");
        String name = "";

        // TODO: How to deal with names like "Hans von der Wiese"?
        int nameStartingIndex = nameComponent.length-2;
        if (nameStartingIndex >= 0) {
            name = nameComponent[nameComponent.length - 2]
                    .concat(" ")
                    .concat(nameComponent[nameComponent.length - 1]);
        }
        return name;
    }

    private String extractLineData(String line, String str) {
        String result = line.replaceAll(format("%s", str.trim()), "").trim();
        return result.replaceAll(" [0-9]?[0-9]:[0-9]?[0-9] Uhr \\(Druck:.*", "");
    }
}
