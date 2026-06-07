package de.lingoMetrics.UILogik;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class MainViewController {

    @FXML
    private AnchorPane mainPane;
    public String textToAnalyze;
    /*public Object textToCompare;*/

    @FXML
    private void onClickSelectFileToAnalyze() {
        textToAnalyze = openFileChooser();
    }

    /*@FXML
    private void onClickSelectFileToCompare() {
        textToCompare = openFileChooser();
    }*/

    private String openFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Textdatei auswählen");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Textdateien", "*.txt")
        );
        Stage stage = (Stage) mainPane.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            return readFile(file);
        }
        return null;
    }

    private String readFile(File file) {
        try{
            return Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
