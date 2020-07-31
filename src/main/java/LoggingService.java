import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class LoggingService {

    private static final Logger logger = Logger.getLogger(LoggingService.class);
    private static final String DIVIDER = "________________________________________________";

    public static void startLogging() {
        logger.log(Level.OFF, DIVIDER);
        logger.log(Level.OFF, "Starting Logger for PDF Extraction Job");
    }

    public static void addExceptionToLog(Exception e) {
        logger.log(Level.ERROR, e.getMessage());
    }

    public static void addErrorToLog(String msg) {
        logger.log(Level.ERROR, msg);
    }
}
