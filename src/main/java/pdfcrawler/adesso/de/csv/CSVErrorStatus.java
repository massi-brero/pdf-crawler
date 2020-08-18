package pdfcrawler.adesso.de.csv;

import java.util.HashSet;
import java.util.Set;

public class CSVErrorStatus {
    public static Set<String> selectedDocuments = new HashSet<>();
    public static Set<String> readDocuments = new HashSet<>();
    public static Set<String> notReadDocuments = new HashSet<>();
    public static Set<String> documentWithErrors = new HashSet<>();
    public static Set<String> documentWithoutErrors = new HashSet<>();
    public static int successCount = 0;
    public static int errorCount = 0;

    public static void addReadSuccess(String documentName) {
        readDocuments.add(documentName);
        documentWithoutErrors.add(documentName);
        successCount++;
    }

    public static void addReadError(String documentName) {
        readDocuments.add(documentName);
        documentWithErrors.add(documentName);
        errorCount++;
    }
}
