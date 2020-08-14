package pdfcrawler.adesso.de.gui;

import pdfcrawler.adesso.de.CsvWriter;
import pdfcrawler.adesso.de.LoggingService;
import pdfcrawler.adesso.de.PdfScanner;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class FrameFactory {
    private static String SETTINGS_PROPERTIES = "settings.properties";
    private static String DEFAULT_INPUTPATH = "default.inputPath";
    private static String DEFAULT_OUTPUTPATH = "default.outputPath";
    private static String BASE_DIR;

    static {
        try {
            BASE_DIR = FrameFactory.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        } catch (URISyntaxException e) {
            LoggingService.log("There is an error extracting the base directory path: " + e.getMessage());
        }
    }

    private static JFrame frame;
    private static Container pane;

    private static String ls = System.getProperty("line.separator");

    private static final PdfScanner pdfScanner = new PdfScanner();

    private static JLabel fileInputPathLabel = new JLabel("Select input directory or file:");
    private static JLabel fileOutputPathLabel = new JLabel("Select output directory:");

    private static JFileChooser inputPathFileChooser = new JFileChooser();
    private static JFileChooser outputPathFileChooser = new JFileChooser();

    private final static int TEXTFIELD_WIDTH = 30;
    private static JTextArea inputPathTextArea = new JTextArea(5, TEXTFIELD_WIDTH);
    private static JTextField outputPathTextField = new JTextField(TEXTFIELD_WIDTH);

    private static JScrollPane inputPathTextAreaScrollPane = new JScrollPane(inputPathTextArea);

    private static JButton browseInputButton = new JButton("Browse");
    private static JButton browseOutputButton = new JButton("Browse");
    private static JButton convertButton = new JButton("Convert");

    public static JFrame initializeFrame() {
        if (frame == null) {
            frame = new JFrame("PDF Crawler");
        }

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        pane = frame.getContentPane();
        pane.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        // TODO: Set default path.
        inputPathFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        inputPathFileChooser.setMultiSelectionEnabled(true);
        Properties prop = getDefaultPaths();
        inputPathTextArea.setText(prop.getProperty(DEFAULT_INPUTPATH).replace(",", ls));
//        inputPathFileChooser.setDragEnabled(true);
        outputPathFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        outputPathFileChooser.setMultiSelectionEnabled(false);
        outputPathTextField.setText(prop.getProperty(DEFAULT_OUTPUTPATH));

        ButtonActionListener buttonActionListener = new ButtonActionListener();

        browseInputButton.addActionListener(buttonActionListener);
        browseOutputButton.addActionListener(buttonActionListener);
        convertButton.addActionListener(buttonActionListener);

        inputPathTextAreaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // Edit Element display properties
        inputPathTextAreaScrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));

        // Add input path label
        c.insets = new Insets(10, 10, 10, 10);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        pane.add(fileInputPathLabel, c);

        // Add input path text area scroll pane
        c.gridx = 1;
        c.gridy = 0;
        pane.add(inputPathTextAreaScrollPane, c);

        // Add browse input button
        c.gridx = 2;
        c.gridy = 0;
        pane.add(browseInputButton, c);

        // Add output path label
        c.gridx = 0;
        c.gridy = 1;
        pane.add(fileOutputPathLabel, c);

        // Add output path text field
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 1;
        c.gridy = 1;
        pane.add(outputPathTextField, c);

        // Add browse output button
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 2;
        c.gridy = 1;
        pane.add(browseOutputButton, c);

        // Add convert button
        c.gridx = 1;
        c.gridy = 3;
        c.gridwidth = 3;
        pane.add(convertButton, c);

        frame.pack();

        return frame;
    }

    /**
     * Save the default input and output paths to the settings file.
     * @param defaultInputPath
     * @param defaultOutputPath
     */
    private static void saveSettingsToFile(String defaultInputPath, String defaultOutputPath) {
        try {
            String settingsFilePath = BASE_DIR + SETTINGS_PROPERTIES;
            File settingsFile = new File(settingsFilePath);

            if (!settingsFile.exists()) {
                settingsFile.createNewFile();
            }

            OutputStream output = null;

            try (InputStream inputStream = new FileInputStream(settingsFile)) {
                Properties prop = new Properties();
                prop.load(inputStream);

                output = new FileOutputStream(settingsFile);
                if (defaultInputPath != null) {
                    prop.setProperty(DEFAULT_INPUTPATH, defaultInputPath);
                }
                if (defaultOutputPath != null) {
                    prop.setProperty(DEFAULT_OUTPUTPATH, defaultOutputPath);
                }

                prop.store(output, null);
            } finally {
                if (output != null) {
                    output.close();
                }
            }

        } catch (IOException e) {
            LoggingService.log("There is an error saving setting to file: " + e.getMessage());
        }
    }

    private static Properties getDefaultPaths() {
        String settingsFilePath = BASE_DIR + SETTINGS_PROPERTIES;

        try(InputStream inputStream = new FileInputStream(settingsFilePath)) {
            Properties properties = new Properties();

            if (inputStream != null) {
                properties.load(inputStream);
                return properties;
            } else {
                LoggingService.log("There is an error reading setting to file. InputStream is null.");
                return new Properties();
            }
        } catch (IOException e) {
            LoggingService.log("There is an error reading setting to file: " + e.getMessage());
            return new Properties();
        }
    }
    static class ButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if (actionEvent.getSource() == browseInputButton) {
                int returnVal = inputPathFileChooser.showOpenDialog(pane);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File[] inputPaths = inputPathFileChooser.getSelectedFiles();

                    inputPathTextArea.setText("");
                    // Fill textarea with file paths.
                    Arrays.stream(inputPaths).forEach(path -> {
                        if (Files.isDirectory(Paths.get(path.getAbsolutePath()))) {
                            File[] files = path.listFiles();
                            if (files != null && files.length > 0) {
                                Arrays.stream(files).forEach(filepath -> inputPathTextArea.append(filepath.getAbsolutePath() + ls));
                            }
                        } else {
                            inputPathTextArea.append(path.getAbsolutePath() + ls);
                        }
                    });

                    String contentString = inputPathTextArea.getText().replace(ls, ",");
                    if (contentString.substring(contentString.length() - 1).equals(",")) {
                        contentString = contentString.substring(0, contentString.length() -1);
                    }
                    saveSettingsToFile(contentString, null);
                } else {
                    LoggingService.log("Open command cancelled by user.");
                }
            } else if (actionEvent.getSource() == browseOutputButton) {
                int returnVal = outputPathFileChooser.showOpenDialog(pane);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File outputPath = outputPathFileChooser.getSelectedFile();
                    outputPathTextField.setText(outputPath.getAbsolutePath());
                    saveSettingsToFile(null, outputPathTextField.getText());
                    LoggingService.log("Output directory: " + outputPath.getName());
                } else {
                    LoggingService.log("Error occurred during choosing output directory.");
                }
            } else if (actionEvent.getSource() == convertButton) {
                if (inputPathTextArea.getText().isBlank()) {
                    LoggingService.log("Start conversion without specifying an input path.");
                    return;
                } else if (outputPathTextField.getText().isBlank()) {
                    LoggingService.log("Start conversion without specifying an output path.");
                    return;
                }

                String[] inputPathLines = inputPathTextArea.getText().split(ls);

                Map<String, String> pdfData = new HashMap<>();

                for (String inputPath : inputPathLines) {
                    pdfData.putAll(pdfScanner.scanFile(inputPath));
                }

                try {
                    new CsvWriter().createCsv(pdfData, new File(outputPathTextField.getText()));
                } catch (IOException e) {
                    // TODO: Display a GUI error messgae.
                    LoggingService.log("Error creating CSV:");
                    LoggingService.log(e.getMessage());
                }
            }
        }
    }
}
