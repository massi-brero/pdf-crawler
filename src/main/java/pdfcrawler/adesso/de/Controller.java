package pdfcrawler.adesso.de;

import java.io.IOException;
import java.util.Scanner;


public class Controller {
    private final static ConfigService configService = new ConfigService();
    private final static Scanner sn = new Scanner(System.in);

    public static void main(String[] args) throws IOException {

        showSplash();
        var option = showMenu();

        LoggingService.startLogging();
        final var pdfScanner = new PdfScanner();

        switch (option) {
            case "1":
                var pdfData = pdfScanner.scanFile(configService.getInputPath());
                new CsvWriter().createCsv(pdfData);
                break;
            case "2":
                System.out.println("[2] Neuen Pfad eingeben");
                break;
            case "3":
                //exit from the program
                System.out.println("Exiting...");
                System.exit(0);
            default:
                //inform user in case of invalid choice.
                System.out.println("Ungueltige Eingabe. Read the options carefully...");
        }
    }

    private static String showMenu() {
        var option = "";

        while (option.isEmpty()) {
            System.out.println("\n\n***** Menue *****");
            System.out.println("[1] Standardkonfiguration");
            System.out.println("[2] Neuen Pfad eingeben");

            System.out.print("Option: ");
            option = sn.next();
        }

        return option;
    }

    private void getPathAsUserInput() {

    }

    private static void showSplash() {
        System.out.println(configService.getSlashPic());
    }
}
