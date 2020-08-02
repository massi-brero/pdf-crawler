package pdfcrawler.adesso.de;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import static org.apache.log4j.Level.ERROR;

class LoggingService {

    private LoggingService() {
    }

    private static final Logger logger = Logger.getLogger(LoggingService.class);
    private static final String DIVIDER = "________________________________________________";

    public static void startLogging() {
        logger.log(Level.OFF, DIVIDER);
        logger.log(Level.OFF, "Starting Logger for PDF Extraction Job");
    }

    public static void addExceptionToLog(Exception e) {
        logger.log(ERROR, e.getClass() + ": " + e.getMessage());
    }

    public static void addErrorToLog(String msg) {
        logger.log(ERROR, msg);
    }
}
