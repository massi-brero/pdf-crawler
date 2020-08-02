package pdfcrawler.adesso.de;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class CsvWriter {
    final String[] headers = {"Name", "title"};
    private final String SUFFIX = ".csv";

    public void createCsv(Map<String, String> data) throws IOException {
        var configService = new ConfigService();
        Format formatter = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss");
        String dateTime = formatter.format(new Date());
        var filePath = configService.getOutputFilePath().concat(dateTime).concat(SUFFIX);
        FileWriter out = new FileWriter(filePath);

        try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT
                .withHeader(headers))) {
            data.forEach((name, date) -> {
                try {
                    printer.printRecord(name, date);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } finally {
            out.close();
        }
    }
}
