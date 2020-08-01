package pdfcrawler.adesso.de;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigService {

    String result = "";
    InputStream inputStream;
    private String outputFilePath;
    private String inputPath;
    private String slashPic;

    private final static String CONFIG_PATH = "config.properties";

    public ConfigService() {
        init();
    }

    private void init() {
        Properties prop = new Properties();
        String propFileName = CONFIG_PATH;

        inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

        try {
            prop.load(inputStream);

            setInputPath(prop.getProperty("inputPath"));
            setOutputFilePath(prop.getProperty("outputFilePath"));
            setSlashPic(prop.getProperty("slashPic"));
        } catch (IOException e) {
            LoggingService.addExceptionToLog(e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                LoggingService.addExceptionToLog(e);
            }
        }
    }

    public String getOutputFilePath() {
        return outputFilePath;
    }

    public void setOutputFilePath(String outputFilePath) {
        this.outputFilePath = outputFilePath;
    }

    public String getInputPath() {
        return inputPath;
    }

    public void setInputPath(String inputPath) {
        this.inputPath = inputPath;
    }

    public String getSlashPic() {
        return slashPic;
    }

    public void setSlashPic(String slashPic) {
        this.slashPic = slashPic;
    }
}
