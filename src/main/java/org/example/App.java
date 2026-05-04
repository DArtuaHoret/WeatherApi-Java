package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/views/main.fxml"));

            // Pobierz dostępny obszar ekranu (bez paska zadań)
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

            // Stwórz scenę o rozmiarze pełnego widocznego ekranu
            Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());

            // Ustaw pozycję i rozmiar okna
            primaryStage.setX(screenBounds.getMinX());
            primaryStage.setY(screenBounds.getMinY());
            primaryStage.setWidth(screenBounds.getWidth());
            primaryStage.setHeight(screenBounds.getHeight());

            primaryStage.setScene(scene);
            primaryStage.setTitle("Aplikacja Pogodowa");
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace(); // To pokaże dokładny błąd w konsoli
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
