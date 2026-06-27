package de.lingoMetrics;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.io.IOException;

public class Main extends Application {


    private static ApplicationContext context;

    public static ApplicationContext getContext() {
        return context;
    }

    @Override
    public void start(Stage stage) throws IOException {
        try {
            context = new ApplicationContext();

            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/UIViews/main-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 800, 600);

            stage.setTitle("LingoMetrics");
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            System.err.println("Fehler beim Laden der FXML-Datei: " + e.getMessage());
            e.printStackTrace();
            // Fallback: einfache UI
            Label label = new Label("Fehler beim Laden der UI");
            Scene scene = new Scene(label, 600, 400);
            stage.setTitle("LingoMetrics - Fehler");
            stage.setScene(scene);
            stage.show();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}


