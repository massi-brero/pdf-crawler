package pdfcrawler.adesso.de;

import pdfcrawler.adesso.de.logging.LoggingService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigService {

    private String outputFilePath;
    private String inputPath;
    private String slashPic;

    private static final String CONFIG_PATH = "config.properties";

    public ConfigService() {
        init();
    }

    private void init() {
        Properties prop = new Properties();

        try(InputStream inputStream = getClass().getClassLoader().getResourceAsStream(CONFIG_PATH);) {
            assert inputStream != null;
            prop.load(inputStream);

            setInputPath(prop.getProperty("inputPath"));
            String outputFileDirectory = prop.getProperty("outputFileDirectory");
            setOutputFilePath(
                    !outputFileDirectory.substring(
                            outputFileDirectory.length() - 1).equals("-") ?
                            outputFileDirectory.concat(File.separator) :
                            outputFileDirectory);
            setSlashPic(prop.getProperty("slashPic"));
        } catch (IOException e) {
            LoggingService.addExceptionToLog(e);
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
