package pdfcrawler.adesso.de;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


public class Controller {
    private static final ConfigService configService = new ConfigService();
    private static final Scanner sn = new Scanner(System.in);

    /**
     *
     * @param args not needed here
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        var pdfData = new HashMap<String, String>();
        final var pdfScanner = new PdfScanner();
        showSplash();

        var option = showMenu();

        while (!option.equals("3")) {
            LoggingService.startLogging();

            switch (option) {
                case "1":
                    pdfData = pdfScanner.scanFile(configService.getInputPath());
                    new CsvWriter().createCsv(pdfData);
                    break;
                case "2":
                    var customPath = getPathFromUserInput();
                    pdfData = pdfScanner.scanFile(customPath);
                    new CsvWriter().createCsv(pdfData);
                    break;
                default:
                    //inform user in case of invalid choice.
                    System.out.println("Ungueltige Eingabe.");
            }
            pdfData.clear();
            option = showMenu();
        }

        System.out.println("Bis bald.");
        System.exit(0);

    }

    private static String showMenu() {

        System.out.println("\n\n******* Menue *******");
        System.out.println("[1] Standardkonfiguration");
        System.out.println("[2] Neuen Pfad eingeben");
        System.out.println("[3] Beenden");

        System.out.print("Option: ");

        return sn.next();

    }

    private static String getPathFromUserInput() {
        System.out.print("Ordner zu den PDF-Dateien: ");
        return sn.next();
    }

    private static void showSplash() {
        System.out.println(configService.getSlashPic());
    }
}
