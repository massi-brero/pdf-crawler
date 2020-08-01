package pdfcrawler.adesso.de;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class CsvWriter {
 final  String[] HEADERS = { "Name", "title"};

  public void createCsv(Map<String, String> data) throws IOException {
    FileWriter out = new FileWriter("../bewerbungen.csv");
    try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT
      .withHeader(HEADERS))) {
      data.forEach((name, date) -> {
        try {
          printer.printRecord(name, date);
        } catch (IOException e) {
          e.printStackTrace();
        }
      });
    }
  }
}
