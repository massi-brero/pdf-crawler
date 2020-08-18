package pdfcrawler.adesso.de.logging;

import pdfcrawler.adesso.de.utilities.Config;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ApplicationLogger {

    private static final String logfile = "applog.log";
    private static String outputFile;
    private static PrintWriter writer;
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void log(String msg) {
        LocalDateTime datetime = LocalDateTime.now();
        noFormattingLog(dateTimeFormatter.format(datetime) + " ");
        noFormattingLog(msg + Config.ls);
    }

    public static void noFormattingLog(String msg) {
        if (outputFile == null) {
            LoggingService.log("ApplicationLogger.outpufile ist nicht initializiert.");
            return;
        }

        if (writer == null) {
            try {
                writer = new PrintWriter(createFullLogfilePath());
            } catch (FileNotFoundException e) {
                LoggingService.addExceptionToLog(e);
            }
        }

        writer.append(msg);
        writer.flush();
    }

    public static void close() {
        if (writer != null) {
            writer.close();
            writer = null;
        }
    }

    public static String getOutputFile() {
        return outputFile;
    }

    public static void setOutputFile(String outputFile) {
        ApplicationLogger.outputFile = outputFile;
    }

    private static String createFullLogfilePath() {
        outputFile = (outputFile.lastIndexOf(File.separator) + 1) == outputFile.length() ?
                outputFile :
                outputFile + File.separator;
        return outputFile + logfile;
    }
}
