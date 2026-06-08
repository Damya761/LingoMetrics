package de.lingoMetrics.UILogik;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Locale;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import de.lingoMetrics.Service.ServiceManager;
import de.lingoMetrics.Service.ExportService;
import javafx.scene.control.Alert;



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
    private String selectedFileSimple;
    private String selectedFileCompare;

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

    @FXML
    private javafx.scene.control.Label fileLabelSimple;

    @FXML
    private javafx.scene.control.Label fileLabelCompare;

    @FXML
    private StackPane dropzoneSimple;

    @FXML
    private StackPane dropzoneCompare;

    // Analyze - simple (first tab)
    @FXML
    private void onClickAnalyzeSimple() {
        String text = null;
        if (selectedFileSimple != null) {
            text = readFile(selectedFileSimple);
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

    /* Drag & Drop handlers for Tab1 */
    @FXML
    private void handleDragOverTab1(DragEvent event) {
        Dragboard db = event.getDragboard();
        if (db.hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    @FXML
    private void handleDragEnteredTab1(DragEvent event) {
        if (dropzoneSimple != null) dropzoneSimple.getStyleClass().add("drag-over");
        event.consume();
    }

    @FXML
    private void handleDragExitedTab1(DragEvent event) {
        if (dropzoneSimple != null) dropzoneSimple.getStyleClass().remove("drag-over");
        event.consume();
    }

    @FXML
    private void handleDragDroppedTab1(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            File file = db.getFiles().get(0);
            selectedFileSimple = file.getAbsolutePath();
            if (fileLabelSimple != null) {
                fileLabelSimple.setText(file.getName());
                if (!fileLabelSimple.getStyleClass().contains("file-selected")) fileLabelSimple.getStyleClass().add("file-selected");
            }
            if (dropzoneSimple != null && !dropzoneSimple.getStyleClass().contains("selected")) dropzoneSimple.getStyleClass().add("selected");
            success = true;
        }
        event.setDropCompleted(success);
        event.consume();
    }

    /* Drag & Drop handlers for Tab2 */
    @FXML
    private void handleDragOverTab2(DragEvent event) {
        Dragboard db = event.getDragboard();
        if (db.hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    @FXML
    private void handleDragEnteredTab2(DragEvent event) {
        if (dropzoneCompare != null) dropzoneCompare.getStyleClass().add("drag-over");
        event.consume();
    }

    @FXML
    private void handleDragExitedTab2(DragEvent event) {
        if (dropzoneCompare != null) dropzoneCompare.getStyleClass().remove("drag-over");
        event.consume();
    }

    @FXML
    private void handleDragDroppedTab2(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            File file = db.getFiles().get(0);
            selectedFileCompare = file.getAbsolutePath();
            if (fileLabelCompare != null) {
                fileLabelCompare.setText(file.getName());
                if (!fileLabelCompare.getStyleClass().contains("file-selected")) fileLabelCompare.getStyleClass().add("file-selected");
            }
            if (dropzoneCompare != null && !dropzoneCompare.getStyleClass().contains("selected")) dropzoneCompare.getStyleClass().add("selected");
            success = true;
        }
        event.setDropCompleted(success);
        event.consume();
    }

    // Analyze and compare (second tab)
    @FXML
    private void onClickAnalyzeCompare() {
        String text = null;
        if (selectedFileCompare != null) {
            text = readFile(selectedFileCompare);
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

        sb.append("Basisdaten\n");
        appendLine(sb, "Absätze", r.getAbsatzAnzahl());
        appendLine(sb, "Sätze", r.getSatzAnzahl());
        appendLine(sb, "Wörter", r.getWortAnzahl());

        sb.append("\nMetriken\n");
        appendLine(sb, "Wortlängenverteilung", formatDouble(r.getWortlaengenverteilung()));
        appendLine(sb, "Mittlere Satzlänge", formatDouble(r.getMittlereSatzlaenge()));
        appendLine(sb, "Satzlängenunterschied", formatDouble(r.getSatzlaengenunterschied()));
        appendLine(sb, "Funktionswörteranteil", formatDouble(r.getFunktionswoerterAnteil()));
        appendLine(sb, "Füllwortanteil", formatDouble(r.getFuellwoerterAnteil()));
        appendLine(sb, "Type-Token-Ratio", formatDouble(r.getTypeTokenRatio()));
        appendLine(sb, "Lesbarkeitsindex", formatDouble(r.getLesbarkeitsindex()));
        appendLine(sb, "Mittleres Sentiment", formatDouble(r.getMittleresSentiment()));
        appendLine(sb, "Hapax Legomena", r.getHapaxLegomena());
        appendLine(sb, "Adjektiv-Verb-Quotient", formatDouble(r.getAdjektivVerbQuotient()));
        //appendLine(sb, "Mittlere Konkretheit", formatDouble(r.getMittlereKonkretheit()));

        sb.append("\nInterpunktion\n");
        if (r.getInterpunktion().isEmpty()) {
            sb.append("- keine Satzzeichen erkannt\n");
        } else {
            r.getInterpunktion().forEach((zeichen, anzahl) ->
                    sb.append("- ").append(zeichen).append(": ").append(anzahl).append("\n")
            );
        }

        if (r.hasAuswertung()) {
            sb.append("\nAuswertung\n");
            appendLine(sb, "Score", r.getScore());
            appendLine(sb, "Bewertung", r.getGesamtBewertung());
            if (!r.getHinweise().isEmpty()) {
                sb.append("Hinweise:\n");
                for (String hinweis : r.getHinweise()) {
                    sb.append("- ").append(hinweis).append("\n");
                }
            } else {
                sb.append("Hinweise: keine\n");
            }
        }
        return sb.toString();
    }

    private void appendLine(StringBuilder sb, String label, Object value) {
        sb.append(label).append(": ").append(value).append("\n");
    }

    private String formatDouble(double value) {
        return String.format(Locale.GERMANY, "%.4f", value);
    }

    //choose file and save path
    @FXML
    private void openFileChooserTab1() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Textdatei auswählen");
        File documents = new File(
                System.getProperty("user.home"),
                "Documents"
        );
        fileChooser.setInitialDirectory(documents);
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Textdateien", "*.txt")
        );
        Stage stage = (Stage) mainPane.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            selectedFileSimple = file.getAbsolutePath();
            if (fileLabelSimple != null) {
                fileLabelSimple.setText(file.getName());
                if (!fileLabelSimple.getStyleClass().contains("file-selected")) fileLabelSimple.getStyleClass().add("file-selected");
            }
            if (dropzoneSimple != null && !dropzoneSimple.getStyleClass().contains("selected")) dropzoneSimple.getStyleClass().add("selected");
        }
    }


    //choose file and save path
    @FXML
    private void openFileChooserTab2() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Textdatei auswählen");
        File documents = new File(
                System.getProperty("user.home"),
                "Documents"
        );
        fileChooser.setInitialDirectory(documents);
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Textdateien", "*.txt")
        );
        Stage stage = (Stage) mainPane.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            selectedFileCompare = file.getAbsolutePath();
            if (fileLabelCompare != null) {
                fileLabelCompare.setText(file.getName());
                if (!fileLabelCompare.getStyleClass().contains("file-selected")) fileLabelCompare.getStyleClass().add("file-selected");
            }
            if (dropzoneCompare != null && !dropzoneCompare.getStyleClass().contains("selected")) dropzoneCompare.getStyleClass().add("selected");
        }
    }

    @FXML
    private void removeSelectedFileSimple() {
        selectedFileSimple = null;
        if (fileLabelSimple != null) {
            fileLabelSimple.setText("Keine Datei gewählt");
            fileLabelSimple.getStyleClass().remove("file-selected");
        }
        if (dropzoneSimple != null) dropzoneSimple.getStyleClass().remove("selected");
    }

    @FXML
    private void removeSelectedFileCompare() {
        selectedFileCompare = null;
        if (fileLabelCompare != null) {
            fileLabelCompare.setText("Keine Datei gewählt");
            fileLabelCompare.getStyleClass().remove("file-selected");
        }
        if (dropzoneCompare != null) dropzoneCompare.getStyleClass().remove("selected");
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

    @FXML
    private void onClickExportSimple() {
        String text = null;
        if (selectedFileSimple != null) {
            text = readFile(selectedFileSimple);
        } else if (textArea != null) {
            text = textArea.getText();
        }

        if (text == null || text.isBlank()) {
            showErrorAlert("Export Fehler", "Kein Text zum Exportieren gefunden.");
            return;
        }

        try {
            ServiceManager.AnalysisRequest request = new ServiceManager.AnalysisRequest(text, null, true, false);
            ServiceManager.AnalysisResult result = serviceManager.analyse(request);

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Rich-Text-Datei speichern");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Rich Text Format", "*.rtf")
            );
            fileChooser.setInitialFileName("lingometrics_analyse.rtf");
            File documents = new File(System.getProperty("user.home"), "Documents");
            if (documents.exists()) {
                fileChooser.setInitialDirectory(documents);
            }

            Stage stage = (Stage) mainPane.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                ExportService.exportToRtf(result.getDocument(), result, text, file);
                showInfoAlert("Export erfolgreich", "Die Analyse wurde erfolgreich als RTF-Datei exportiert.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Export Fehler", "Fehler beim Exportieren: " + e.getMessage());
        }
    }

    @FXML
    private void onClickExportCompare() {
        String text = null;
        if (selectedFileCompare != null) {
            text = readFile(selectedFileCompare);
        } else if (textAreaCompare != null) {
            text = textAreaCompare.getText();
        }

        String style = styleComboBox != null ? styleComboBox.getValue() : null;

        if (text == null || text.isBlank()) {
            showErrorAlert("Export Fehler", "Kein Text zum Exportieren gefunden.");
            return;
        }

        try {
            ServiceManager.AnalysisRequest request = new ServiceManager.AnalysisRequest(text, style, true, true);
            ServiceManager.AnalysisResult result = serviceManager.analyse(request);

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Rich-Text-Datei speichern");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Rich Text Format", "*.rtf")
            );
            fileChooser.setInitialFileName("lingometrics_vergleich.rtf");
            File documents = new File(System.getProperty("user.home"), "Documents");
            if (documents.exists()) {
                fileChooser.setInitialDirectory(documents);
            }

            Stage stage = (Stage) mainPane.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                ExportService.exportToRtf(result.getDocument(), result, text, file);
                showInfoAlert("Export erfolgreich", "Der Vergleich wurde erfolgreich als RTF-Datei exportiert.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Export Fehler", "Fehler beim Exportieren: " + e.getMessage());
        }
    }

    private void showInfoAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

