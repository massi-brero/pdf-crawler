package pdfcrawler.adesso.de.csv;

import java.util.HashSet;
import java.util.Set;

public class CSVErrorStatus {
    public static Set<String> selectedDocuments = new HashSet<>();
    public static Set<String> readDocuments = new HashSet<>();
    public static Set<String> notReadDocuments = new HashSet<>();
    public static Set<String> documentsWithErrors = new HashSet<>();
    public static Set<String> documentsWithoutErrors = new HashSet<>();

    public static void addReadSuccess(String documentName) {
        readDocuments.add(documentName);
        documentsWithoutErrors.add(documentName);
    }

    public static void addReadError(String documentName) {
        readDocuments.add(documentName);
        documentsWithErrors.add(documentName);
    }

    public static void resetCounters() {
        selectedDocuments = new HashSet<>();
        readDocuments = new HashSet<>();
        notReadDocuments = new HashSet<>();
        documentsWithErrors = new HashSet<>();
        documentsWithoutErrors = new HashSet<>();
    }
}
