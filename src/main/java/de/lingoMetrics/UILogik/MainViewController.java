package de.lingoMetrics.UILogik;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import de.lingoMetrics.Service.ServiceManager;


public class MainViewController {

    //create/initialize ServiceManager
    private ServiceManager serviceManager;
    @FXML
    private void initialize() {
        try {
            serviceManager = ServiceManager.createDefault();
        } catch (IOException e) {
            throw new RuntimeException("ServiceManager konnte nicht initialisiert werden.", e);
        }
    }


    //define Window stage, needed for File-Picker
    @FXML
    private AnchorPane mainPane;

    //save path to selectedFile
    private String selectedFile;

    @FXML
    private javafx.scene.control.TextArea textArea;

    @FXML
    private javafx.scene.control.TextArea textAreaCompare;

    @FXML
    private javafx.scene.control.ComboBox<String> styleComboBox;

    @FXML
    private javafx.scene.control.TextArea outputArea;

    @FXML
    private javafx.scene.control.TextArea outputAreaCompare;

    // Analyze - simple (first tab)
    @FXML
    private void onClickAnalyzeSimple() {
        String text = null;
        if (selectedFile != null) {
            text = readFile(selectedFile);
        } else if (textArea != null) {
            text = textArea.getText();
        }

        if (text == null || text.isBlank()) {
            System.out.println("Kein Text zur Analyse (einfach) gefunden.");
            return;
        }

        ServiceManager.AnalysisRequest request = new ServiceManager.AnalysisRequest(text, null, false, false);
        ServiceManager.AnalysisResult result = serviceManager.analyse(request);
        if (outputArea != null) {
            outputArea.setText(formatResult(result));
        }
    }

    // Analyze and compare (second tab)
    @FXML
    private void onClickAnalyzeCompare() {
        String text = null;
        if (selectedFile != null) {
            text = readFile(selectedFile);
        } else if (textAreaCompare != null) {
            text = textAreaCompare.getText();
        }

        String style = styleComboBox != null ? styleComboBox.getValue() : null;

        if (text == null || text.isBlank()) {
            System.out.println("Kein Text zur Analyse (Vergleich) gefunden.");
            return;
        }

        ServiceManager.AnalysisRequest request = new ServiceManager.AnalysisRequest(text, style, false, true);
        ServiceManager.AnalysisResult result = serviceManager.analyse(request);
        if (outputAreaCompare != null) {
            outputAreaCompare.setText(formatResult(result));
        }
    }

    private String formatResult(ServiceManager.AnalysisResult r) {
        if (r == null) return "Keine Ergebnisse.";
        StringBuilder sb = new StringBuilder();
        sb.append("Stil: ").append(r.getStiltype()).append("\n");
        sb.append("Absätze: ").append(r.getAbsatzAnzahl()).append("\n");
        sb.append("Sätze: ").append(r.getSatzAnzahl()).append("\n");
        sb.append("Wörter: ").append(r.getWortAnzahl()).append("\n");
        sb.append("Mittlere Satzlänge: ").append(r.getMittlereSatzlaenge()).append("\n");
        sb.append("Type-Token-Ratio: ").append(r.getTypeTokenRatio()).append("\n");
        sb.append("Lesbarkeitsindex: ").append(r.getLesbarkeitsindex()).append("\n");
        return sb.toString();
    }

    //choose file and save path
    @FXML
    private void openFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Textdatei auswählen");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Textdateien", "*.txt")
        );
        Stage stage = (Stage) mainPane.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            selectedFile = file.getAbsolutePath();
        }
    }

    // read file by absolute path
    private String readFile(String path) {
        if (path == null) return null;
        try {
            return Files.readString(new File(path).toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
