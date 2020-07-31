import java.io.IOException;
import java.util.Scanner;


public class Controller {

  public static void main(String[] args) throws IOException {

    final var pdfScanner = new PdfScanner();

    var pdfData = pdfScanner.scanFile("");
    System.out.println(pdfData);
    new CsvWriter().createCsv(pdfData);
    showSplash();

    var option = showMenu();

      //Check the user input
      switch (option) {
        case "1":
          System.out.println("[1] Standardkonfiguration");
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
      final Scanner sn = new Scanner(System.in);
      System.out.println("\n\n***** Menue *****");
      System.out.println("[1] Standardkonfiguration");
      System.out.println("[2] Neuen Pfad eingeben");

      System.out.print("\n\nOption:");
      option = sn.next();
    }

    return option;
  }

  private void getPathAsUserInput() {

  }

  private static void showSplash() {
    System.out.println(" _____  _____  ______    _____                    _");
    System.out.println("|  __ \\|  __ \\|  ____|  / ____|                  | |");
    System.out.println("| |__) | |  | | |__    | |     _ __ __ ___      _| | ___ _ __");
    System.out.println("|  ___/| |  | |  __|   | |    | '__/ _` \\ \\ /\\ / / |/ _ \\ '__|");
    System.out.println("| |    | |__| | |      | |____| | | (_| |\\ V  V /| |  __/ |");
    System.out.println("|_|    |_____/|_|       \\_____|_|  \\__,_| \\_/\\_/ |_|\\___|_|");
    System.out.println("(c) Public Sued Boss");
  }
}
