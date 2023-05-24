package org.syndrome.parametrizedword;

// Java program to create open or
// save dialog using JFileChooser

import org.syndrome.parametrizedword.structuralfile.StructuralFile;
import org.syndrome.parametrizedword.structuralfile.StructuralFileFactory;
import org.syndrome.parametrizedword.structuralfile.StructuralFileRow;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Set;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

class Main extends JFrame implements ActionListener {

    private static final String SELECT_A_PARAMETRIZED_WORD_FILE = "Parametrik word dosyası seçer misin";
    private static final String SELECT_AN_EXCEL_FILE_FOR_PARAMETERS = "Bir de parametreleri okuyacağım excel dosyasını rica edeyim";
    private static final String SELECT_OUTPUT_DIRECTORY_FOR_GENERATED_FILES = "Zahmet olacak ama bir de dosyaları hangi klasöre aktaracağımı seçer misin";
    private static final String GENERATE = "Dosyaları oluştur";

    private final static JLabel wordFileLabel = new JLabel();
    private final static JLabel excelFileLabel = new JLabel();
    private final static JLabel excelFileParametersLabel = new JLabel();
    private final static JLabel outputDirectoryParametersLabel = new JLabel();
    private final static JFrame frame = new JFrame("Parametrized Word");
    private final static JButton wordFileSelectorButton = new JButton(SELECT_A_PARAMETRIZED_WORD_FILE);
    private final static JButton excelFileSelectorButton = new JButton(SELECT_AN_EXCEL_FILE_FOR_PARAMETERS);
    private final static JButton outputDirectorySelectorButton = new JButton(SELECT_OUTPUT_DIRECTORY_FOR_GENERATED_FILES);
    private final static JButton generateButton = new JButton(GENERATE);
    private static File word, excel, output;
    private static StructuralFile structuralFile;


    public static void main(String[] args) {
        frame.setSize(500, 400);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        excelFileSelectorButton.setVisible(false);
        excelFileLabel.setVisible(false);
        excelFileParametersLabel.setVisible(false);
        outputDirectorySelectorButton.setVisible(false);
        outputDirectoryParametersLabel.setVisible(false);
        generateButton.setVisible(false);

        Main f1 = new Main();
        resetFileLabel(wordFileLabel);
        resetFileLabel(excelFileLabel);

        wordFileSelectorButton.addActionListener(f1);
        excelFileSelectorButton.addActionListener(f1);
        outputDirectorySelectorButton.addActionListener(f1);
        generateButton.addActionListener(f1);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(wordFileLabel);
        p.add(wordFileSelectorButton);
        p.add(excelFileLabel);
        p.add(excelFileSelectorButton);
        p.add(excelFileParametersLabel);
        p.add(outputDirectoryParametersLabel);
        p.add(outputDirectorySelectorButton);
        p.add(generateButton);
        frame.add(p);

        frame.setVisible(true);
    }

    public void actionPerformed(ActionEvent evt) {
        // if the user presses the save button show the save dialog
        String actionCommand = evt.getActionCommand();

        switch (actionCommand) {
            case SELECT_A_PARAMETRIZED_WORD_FILE:
                handleWordFileChooser();
                break;
            case SELECT_AN_EXCEL_FILE_FOR_PARAMETERS:
                handleExcelFileChooser();
                break;
            case SELECT_OUTPUT_DIRECTORY_FOR_GENERATED_FILES:
                handleOutputDirectoryChooser();
                break;
            case GENERATE:
                handleGenerateButton();
                break;
        }
    }

    private void handleGenerateButton() {
        try {
            for (File file : output.listFiles()) {
                file.delete();
            }

            for (StructuralFileRow record : structuralFile.getRecords()) {
                try (WordReplacer wordReplacer = new WordReplacer(word)) {
                    for (String column : structuralFile.getRowMeta().getColumns()) {
                        wordReplacer.replaceWordsInTables(
                                String.format("${%s}", column),
                                record.getRequiredTextValue(column)
                        );
                    }
                    wordReplacer.saveAndGetModdedFile(new File(output, String.format("%s.docx", record.getRequiredTextValue("file_name"))));
                }
            }
            showErrorMessage("Dosya oluşturmayı tamamladım");
            Desktop.getDesktop().open(output);
            System.exit(0);
        } catch (Throwable t) {
            showErrorMessage(t);
        }
    }

    private void handleOutputDirectoryChooser() {
        JFileChooser j = getDirectoryChooser();
        // invoke the showsSaveDialog function to show the save dialog
        int r = j.showSaveDialog(null);

        // if the user selects a file
        if (r == JFileChooser.APPROVE_OPTION) {
            // set the label to the path of the selected file
            output = j.getSelectedFile();
            String absolutePath = output.getAbsolutePath();
            showErrorMessage(String.format("'%s' butonuna bastığın anda '%s' klasörünün altındaki bütün dosyalar silinecek. Bilgine", GENERATE, absolutePath));
            outputDirectoryParametersLabel.setText(absolutePath);
            generateButton.setVisible(true);
            setLastPath(absolutePath, JFileChooser.DIRECTORIES_ONLY);
        }
    }

    private void handleWordFileChooser() {
        JFileChooser j = getFileChooser("doc", "docx");
        // invoke the showsSaveDialog function to show the save dialog
        int r = j.showSaveDialog(null);

        // if the user selects a file
        if (r == JFileChooser.APPROVE_OPTION) {
            // set the label to the path of the selected file
            word = j.getSelectedFile();
            String absolutePath = word.getAbsolutePath();
            wordFileLabel.setText(absolutePath);
            excelFileSelectorButton.setVisible(true);
            excelFileLabel.setVisible(true);
            setLastPath(absolutePath, JFileChooser.FILES_ONLY);
        }
    }

    private void handleExcelFileChooser() {
        // create an object of JFileChooser class
        JFileChooser j = getFileChooser("xls", "xlsx");
        // invoke the showsSaveDialog function to show the save dialog
        int r = j.showSaveDialog(null);

        // if the user selects a file
        if (r == JFileChooser.APPROVE_OPTION) {
            // set the label to the path of the selected file
            excel = j.getSelectedFile();
            String absolutePath = excel.getAbsolutePath();
            try {
                structuralFile = StructuralFileFactory.createFromFile(excel);
            } catch (IOException e) {
                showErrorMessage(e);
                return;
            }

            Set<String> columns = structuralFile
                    .getRowMeta()
                    .getColumns();
            if (!columns.contains("file_name")) {
                showErrorMessage("Parametrelerin olduğu excel dosyasında file_name isimli bir kolon bulamadım. bunu ilgili satır için output dosya ismi olarak kullanacağım");
                return;
            }

            int uniqueSize = structuralFile.getRecords()
                    .stream()
                    .map(x -> x.getRequiredTextValue("file_name"))
                    .collect(Collectors.toSet())
                    .size();

            if (uniqueSize != structuralFile.getRecords().size()) {
                showErrorMessage("file_name kolonundaki verilerin eşsiz olmasını bekliyordum ama malesef muadil kayıtlar var");
                return;
            }

            String parameters = String.join(
                    ", ",
                    columns
            );

            excelFileLabel.setText(absolutePath);
            excelFileSelectorButton.setVisible(true);
            excelFileParametersLabel.setText(String.format("Found parameters: %s", parameters));
            excelFileParametersLabel.setVisible(true);
            outputDirectoryParametersLabel.setVisible(true);
            outputDirectorySelectorButton.setVisible(true);
            setLastPath(absolutePath, JFileChooser.FILES_ONLY);
        }
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(
                frame,
                message,
                "Beklenmedik hata",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private void showErrorMessage(Throwable t) {
        showErrorMessage(printStackTrace(t));
    }

    private void setLastPath(String absolutePath,
                             int selectionMode) {
        Preferences pref = Preferences.userRoot();
        pref.put("DEFAULT_PATH" + selectionMode, absolutePath);
    }

    private static JFileChooser getDirectoryChooser() {
        return constructChooser(JFileChooser.DIRECTORIES_ONLY);
    }

    private static JFileChooser getFileChooser(String... extension) {
        return constructChooser(JFileChooser.FILES_ONLY, extension);
    }

    private static JFileChooser constructChooser(int selectionMode,
                                                 String... extensions) {
        Preferences pref = Preferences.userRoot();
        String path = pref.get("DEFAULT_PATH" + selectionMode, "");
        // create an object of JFileChooser class
        JFileChooser chooser = new JFileChooser(FileSystemView.getFileSystemView());
        if (JFileChooser.FILES_ONLY == selectionMode) {
            if (extensions.length > 0) {
                FileNameExtensionFilter filter = new FileNameExtensionFilter(
                        String.format("%s Tipindeki dosyalar", String.join(", ", extensions)),
                        extensions
                );
                chooser.setFileFilter(filter);
            }
        }
        chooser.setFileSelectionMode(selectionMode);
        chooser.setCurrentDirectory(new File(path));
        return chooser;
    }

    private static void resetFileLabel(JLabel jLabel) {
        jLabel.setText("Alttaki dosya seçme butonundan daha dosya seçmedin");
    }

    public static String printStackTrace(Throwable t) {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        t.printStackTrace(printWriter);
        return writer.toString();
    }
}
