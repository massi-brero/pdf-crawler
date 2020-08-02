package pdfcrawler.adesso.de;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigService {

    InputStream inputStream;
    private String outputFilePath;
    private String inputPath;
    private String slashPic;

    private static final String CONFIG_PATH = "config.properties";

    public ConfigService() {
        init();
    }

    private void init() {
        Properties prop = new Properties();

        inputStream = getClass().getClassLoader().getResourceAsStream(CONFIG_PATH);

        try {
            prop.load(inputStream);

            setInputPath(prop.getProperty("inputPath"));
            setOutputFilePath(prop.getProperty("outputFileName"));
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
