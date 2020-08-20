package pdfcrawler.adesso.de.gui;

import pdfcrawler.adesso.de.csv.CSVErrorStatus;
import pdfcrawler.adesso.de.csv.CsvWriter;
import pdfcrawler.adesso.de.PdfScanner;
import pdfcrawler.adesso.de.logging.ApplicationLogger;
import pdfcrawler.adesso.de.logging.LoggingService;
import pdfcrawler.adesso.de.utilities.Config;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FrameFactory {
    private static String SETTINGS_PROPERTIES = "settings.properties";
    private static String DEFAULT_INPUTPATH = "default.inputPath";
    private static String DEFAULT_OUTPUTPATH = "default.outputPath";
    private static String INPUTPATH_CHECKBOX = "inputpath.checkbox";
    private static String OUTPUTPATH_CHECKBOX = "outputpath.checkbox";
    private static String BASE_DIR;

    static {
        BASE_DIR = System.getProperty("user.dir") + File.separator + ".pdf-crawl/";
    }

    private static JFrame frame;
    private static Container pane;

    private static final PdfScanner pdfScanner = new PdfScanner();

    private static JLabel fileInputPathLabel = new JLabel("Eingabe Verzeichnis(e) / Detei(en) auswählen:");
    private static JLabel fileOutputPathLabel = new JLabel("Ausgabe Verzeichnis auswählen:");

    private static JFileChooser inputPathFileChooser = new JFileChooser();
    private static JFileChooser outputPathFileChooser = new JFileChooser();

    private final static int TEXTFIELD_WIDTH = 30;
    private static JTextArea inputPathTextArea = new JTextArea(5, TEXTFIELD_WIDTH);
    private static JTextField outputPathTextField = new JTextField(TEXTFIELD_WIDTH);

    private static JTextArea logsArea = new JTextArea(10, 50);
    private static JScrollPane logsAreaScroll = new JScrollPane(logsArea);

    private static JScrollPane inputPathTextAreaScrollPane = new JScrollPane(inputPathTextArea);

    private static JButton browseInputButton = new JButton("Auswählen");
    private static JButton browseOutputButton = new JButton("Auswählen");
    private static JButton convertButton = new JButton("Ausführen");

    private static JCheckBox inputPathCheckbox = new JCheckBox("Speichere Eingabepfad");
    private static JCheckBox outputPathCheckbox = new JCheckBox("Speichere Ausgabepfad");

    public static JFrame initializeFrame() {
        if (frame == null) {
            frame = new JFrame("PDF Crawler");
        }

        pane = frame.getContentPane();
        GridBagLayout mgr = new GridBagLayout();
        pane.setLayout(mgr);

        GridBagConstraints c = new GridBagConstraints();

        inputPathFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        inputPathFileChooser.setMultiSelectionEnabled(true);
        inputPathFileChooser.setPreferredSize(new Dimension(1000, 600));
        outputPathFileChooser.setPreferredSize(new Dimension(1000, 600));
        outputPathFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        outputPathFileChooser.setMultiSelectionEnabled(false);
        setDefaults();

        logsAreaScroll.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 0));
        logsArea.setAutoscrolls(true);
        logsArea.setEditable(false);
        logsAreaScroll.setPreferredSize(new Dimension(600, 250));
        ( (DefaultCaret) logsArea.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        ButtonsActionListener buttonsActionListener = new ButtonsActionListener();

        browseInputButton.addActionListener(buttonsActionListener);
        browseOutputButton.addActionListener(buttonsActionListener);
        convertButton.addActionListener(buttonsActionListener);
        inputPathCheckbox.addActionListener(buttonsActionListener);
        outputPathCheckbox.addActionListener(buttonsActionListener);

        inputPathTextAreaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // Edit Element display properties
        inputPathTextAreaScrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));

        Panel defaultCheckboxPanel = new Panel();
        defaultCheckboxPanel.add(inputPathCheckbox);
        defaultCheckboxPanel.add(outputPathCheckbox);

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

        // Add defaults checkbox panel.
        c.fill = GridBagConstraints.VERTICAL;
        c.gridx = 0;
        c.gridy = 3;
        pane.add(defaultCheckboxPanel, c);

        // Add convert button
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 3;
        c.gridwidth = 3;
        pane.add(convertButton, c);

        // Add logs text area
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 3;
        pane.add(logsAreaScroll, c);

        frame.pack();
        frame.setResizable(false);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(dim.width/2-frame.getSize().width/2, dim.height/2-frame.getSize().height/2);
        URL iconURL = FrameFactory.class.getResource("/favicon.png");
        // iconURL is null when not found
        ImageIcon icon = new ImageIcon(iconURL);
        frame.setIconImage(icon.getImage());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        return frame;
    }

    private static void setDefaults() {
        Properties prop = getDefaultSettings();

        if (prop != null) {
            String inputpath = prop.getProperty(DEFAULT_INPUTPATH);
            if (inputpath != null) {
                inputPathTextArea.setText(inputpath.replace(",", Config.ls));
            }
            String outputpath = prop.getProperty(DEFAULT_OUTPUTPATH);
            if (outputpath != null) {
                outputPathTextField.setText(outputpath);
            }

            String inputPathCheckboxProp = prop.getProperty(INPUTPATH_CHECKBOX);
            String outputPathCheckboxProp = prop.getProperty(OUTPUTPATH_CHECKBOX);

            if (inputPathCheckboxProp != null) {
                inputPathCheckbox.setSelected(inputPathCheckboxProp.equals("true"));
            }
            if (outputPathCheckboxProp != null) {
                outputPathCheckbox.setSelected(outputPathCheckboxProp.equals("true"));
            }
        }
    }

    /**
     * Save the default input and output paths to the settings file.
     * @param key
     * @param value
     */
    private static void editSettingsToFile(String key, String value) {
        if (key == null) {
            return;
        }

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

                if (value != null) {
                    prop.setProperty(key, value);
                } else {
                    prop.remove(key);
                }

                prop.store(output, null);
            } finally {
                if (output != null) {
                    output.close();
                }
            }

        } catch (IOException e) {
            LoggingService.log("Es ist ein Fehler beim Anlegen der Setting." + e.getMessage());
        }
    }

    private static Properties getDefaultSettings() {
        String settingsFilePath = BASE_DIR + SETTINGS_PROPERTIES;

        try(InputStream inputStream = new FileInputStream(settingsFilePath)) {
            Properties properties = new Properties();

            if (inputStream != null) {
                properties.load(inputStream);
                return properties;
            } else {
                LoggingService.log("Es ist ein Fehler beim Settings-Einlesen passiert. InnputStream ist null.");
                return null;
            }
        } catch (IOException e) {
            LoggingService.log("Es ist ein Fehler beim Settings-Einlesen passiert: " + e.getMessage());
            return null;
        }
    }

    static class ButtonsActionListener implements ActionListener {
        @Override
        // Browse input file
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
                                Arrays.stream(files).forEach(filepath -> inputPathTextArea.append(filepath.getAbsolutePath() + Config.ls));
                            }
                        } else {
                            inputPathTextArea.append(path.getAbsolutePath() + Config.ls);
                        }
                    });

                    if (inputPathCheckbox.isSelected()) {
                        editSettingsToFile(DEFAULT_INPUTPATH, inputPathTextArea.getText());
                    }
                }
                // Browse output file
            } else if (actionEvent.getSource() == browseOutputButton) {
                int returnVal = outputPathFileChooser.showOpenDialog(pane);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    outputPathTextField.setText(outputPathFileChooser.getSelectedFile().getAbsolutePath());
                    ApplicationLogger.setOutputFile(outputPathTextField.getText());

                    if (outputPathCheckbox.isSelected()) {
                        editSettingsToFile(DEFAULT_OUTPUTPATH, outputPathTextField.getText());
                    }
                } else {
                    LoggingService.log("Fehler beim Auswählen des Ausgabepfad.");
                }
                // Press convert button
            } else if (actionEvent.getSource() == convertButton) {
                if (inputPathTextArea.getText().isBlank()) {
                    LoggingService.log("Konvertierung kann nicht stattfinden. Eingabedateipfad ist nicht definiert.");
                    JOptionPane.showMessageDialog(frame, "Bitte Eingabefeld definieren.");
                    return;
                } else if (outputPathTextField.getText().isBlank()) {
                    LoggingService.log("Konvertierung kann nicht stattfinden. Ausgabepfad ist nicht definiert");
                    JOptionPane.showMessageDialog(frame, "Bitte Ausgabefeld definieren.");
                    return;
                }

                ApplicationLogger.setOutputFile(outputPathTextField.getText());
                String[] inputPathLines = inputPathTextArea.getText().split(Config.ls);

                Map<String, String> pdfData = new HashMap<>();

                // Log all selected paths.
                LoggingService.logApplicationLogs("Diese Pfade wurden zum Bearbeiten ausgewählt: ");
                Arrays.stream(inputPathLines).forEach(inputPath -> {
                    ApplicationLogger.noFormattingLog(String.format("\t%s\n", inputPath));
                });

                ExecutorService executorService = Executors.newFixedThreadPool(2);
                Runnable processPDFsTask = () -> {
                    CSVErrorStatus.resetCounters();
                    convertButton.setEnabled(false);
                    for (String inputPath : inputPathLines) {
                        pdfData.putAll(pdfScanner.scanFile(inputPath));
                    }
                };
                Future<?> processPDFsTaskFuture = executorService.submit(processPDFsTask);

                Runnable writeToCSVTask = () -> {
                    try {
                        processPDFsTaskFuture.get();
                    } catch (InterruptedException | ExecutionException e) {
                        LoggingService.addExceptionToLog(e, true);
                    }

                    try {
                        File outputFile = new File(outputPathTextField.getText());
                        outputFile.mkdir();
                        new CsvWriter().createCsv(pdfData, outputFile);
                        editSettingsToFile(DEFAULT_INPUTPATH, inputPathTextArea.getText());
                        editSettingsToFile(DEFAULT_OUTPUTPATH, outputPathTextField.getText());
                        editSettingsToFile(INPUTPATH_CHECKBOX, inputPathCheckbox.isSelected() ? "true" : "false");
                        editSettingsToFile(OUTPUTPATH_CHECKBOX, outputPathCheckbox.isSelected() ? "true" : "false");
                    } catch (IOException e) {
                        // TODO: Display a GUI error message.
                        LoggingService.log("Fehler beim Erstellen von CSV:");
                        LoggingService.log(e.getMessage());
                    }
                    finally {
                        convertButton.setEnabled(true);
                        logStatistics();
                        LoggingService.log("VERARBEITUNG WURDE BEENDET");
                        ApplicationLogger.noFormattingLog("\n###VERARBEITUNG WURDE BEENDET###\n");
                        JOptionPane.showMessageDialog(frame,
                                String.format("Verarbeitung wurde %s beendet.",
                                        CSVErrorStatus.documentsWithErrors.isEmpty() ? "fehlerfrei" : "mit Fehlern"),
                                "Result",
                                CSVErrorStatus.documentsWithErrors.isEmpty() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
                    }
                };
                executorService.execute(writeToCSVTask);
            } else if (actionEvent.getSource() == inputPathCheckbox) {
                if (inputPathCheckbox.isSelected()) {
                    editSettingsToFile(DEFAULT_INPUTPATH, inputPathTextArea.getText());
                    editSettingsToFile(INPUTPATH_CHECKBOX, "true");
                } else {
                    editSettingsToFile(DEFAULT_INPUTPATH, null);
                    editSettingsToFile(INPUTPATH_CHECKBOX, "false");
                }
            } else if (actionEvent.getSource() == outputPathCheckbox) {
                if (outputPathCheckbox.isSelected()) {
                    editSettingsToFile(DEFAULT_OUTPUTPATH, outputPathTextField.getText());
                    editSettingsToFile(OUTPUTPATH_CHECKBOX, "true");
                } else {
                    editSettingsToFile(DEFAULT_OUTPUTPATH, null);
                    editSettingsToFile(OUTPUTPATH_CHECKBOX, "false");
                }
            }
        }

        private void logStatistics() {
            logProcesedFiles(String.format("Gufunden (%d):", CSVErrorStatus.selectedDocuments.size()), CSVErrorStatus.selectedDocuments);
            logProcesedFiles(String.format("Ignoriert (%d):", CSVErrorStatus.notReadDocuments.size()), CSVErrorStatus.notReadDocuments);
            logProcesedFiles(String.format("Eingelesen (%d):", CSVErrorStatus.readDocuments.size()), CSVErrorStatus.readDocuments);
            logProcesedFiles(String.format("Fehlerfrei (%d):", CSVErrorStatus.documentsWithoutErrors.size()), CSVErrorStatus.documentsWithoutErrors);
            logProcesedFiles(String.format("Fehlerhaft (%d):", CSVErrorStatus.documentsWithErrors.size()
            ), CSVErrorStatus.documentsWithErrors);
            logCounters();
        }

        private void logProcesedFiles(String message, Set<String> documents) {
            ApplicationLogger.log(message);
            documents.forEach(documentPath -> {
                ApplicationLogger.noFormattingLog(String.format("\t%s\n", documentPath));
            });
        }

        private void logCounters() {

            ApplicationLogger.log(String.format("Statistiken:"));
            ApplicationLogger.noFormattingLog(String.format("\tEingegeben:\t\t%s\n" +
                            "\t===\n" +
                            "\tNich eingelesen:\t%d\n" +
                            "\t===\n" +
                            "\tEingelesen:\t\t%d\n" +
                            "\t===\n" +
                            "\tOhne Fehler:\t\t%d\n" +
                            "\t===\n" +
                            "\tMit Fehlern:\t\t%d\n",
                    CSVErrorStatus.selectedDocuments.size(),
                    CSVErrorStatus.notReadDocuments.size(),
                    CSVErrorStatus.readDocuments.size(),
                    CSVErrorStatus.documentsWithoutErrors.size(),
                    CSVErrorStatus.documentsWithErrors.size())
            );
        }
    }

    public static JTextArea getLogsArea() {
        return logsArea;
    }
}
