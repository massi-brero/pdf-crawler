package pdfcrawler.adesso.de.logging;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import static org.apache.log4j.Level.*;

public class LoggingService {

    private static boolean applicationLoggingEnabled = true;

    private LoggingService() {
    }

    private static final Logger logger = Logger.getLogger(LoggingService.class);
    private static final String DIVIDER = "________________________________________________";

    public static void startLogging() {
        logger.log(INFO, DIVIDER);
        logger.log(INFO, "Starting Logger for PDF Extraction Job");
    }

    public static void addExceptionToLog(Exception e) {
        log(ERROR, e.getClass() + ": " + e.getMessage(), false);
    }

    public static void addExceptionToLog(Exception e, boolean withApplicationLogger) {
        log(ERROR, e.getClass() + ": " + e.getMessage(), withApplicationLogger);
    }

    public static void addErrorToLog(String msg) {
        log(ERROR, msg, false);
    }

    public static void addErrorToLog(String msg, boolean withApplicationLogger) {
        log(ERROR, msg, withApplicationLogger);
    }

    public static void log(String msg) {
        log(INFO, msg, false);
    }

    public static void log(String msg, boolean withApplicationLogger) {
        log(INFO, msg, withApplicationLogger);
    }

    public static boolean isApplicationLoggingEnabled() {
        return applicationLoggingEnabled;
    }

    public static void setApplicationLoggingEnabled(boolean applicationLoggingEnabled) {
        if (ApplicationLogger.getOutputFile() != null) {
            LoggingService.applicationLoggingEnabled = applicationLoggingEnabled;
        } else {
            LoggingService.applicationLoggingEnabled = false;
        }
    }

    public static void logApplicationLogs(String msg) {
        if (applicationLoggingEnabled && ApplicationLogger.getOutputFile() != null) {
            ApplicationLogger.log(msg);
        }
    }

    private static void log(Level level, String msg, boolean withApplicationLogger) {
        logger.log(level, msg);

        if (withApplicationLogger &&
                applicationLoggingEnabled &&
                ApplicationLogger.getOutputFile() != null) {
            ApplicationLogger.log(msg);
        }
    }
}
