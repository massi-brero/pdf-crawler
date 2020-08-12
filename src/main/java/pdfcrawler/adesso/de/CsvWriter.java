package pdfcrawler.adesso.de;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class CsvWriter {
    final String[] headers = {"Name", "Datum"};
    private final String SUFFIX = ".csv";

    public void createCsv(Map<String, String> data) throws IOException {
        var configService = new ConfigService();
        Format formatter = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss");
        String dateTime = formatter.format(new Date());
        String outputDirectory = configService.getOutputFilePath();
        createOutputDirectory(outputDirectory);

        var filePath = outputDirectory.concat(dateTime).concat(SUFFIX);

        try (FileWriter out = new FileWriter(filePath);
             CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withHeader(headers))
        ) {
            data.forEach((name, date) -> {
                try {
                    printer.printRecord(name, date);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } finally {
            LoggingService.log("Number of file scanned: " + data.size());
        }
    }

    /**
     * Create the output directory to store the CSV files, if it not exists.
     *
     * @param outputFileDirectory   The full path to the directory.
     * @throws FileNotFoundException
     */
    private void createOutputDirectory(String outputFileDirectory) throws FileNotFoundException {
        if (!Files.exists(Paths.get(outputFileDirectory))) {
            if (!new File(outputFileDirectory).mkdirs()) {
                throw new InternalError("Output directory " + outputFileDirectory + " could not be created.");
            }
        }
    }
}
