package org.example.controllers;
import org.example.models.DataPogoda;
import org.example.services.ServicePogody;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.util.List;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

public class MainController {
    @FXML private ToggleGroup dataTypeGroup;
    @FXML private ToggleGroup locationTypeGroup;

    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField daysField;

    @FXML private TextField cityField;
    @FXML private TextField latField;
    @FXML private TextField lonField;

    @FXML private CheckBox tempCheck;
    @FXML private CheckBox windCheck;
    @FXML private CheckBox rainCheck;
    @FXML private CheckBox pressureCheck;
    @FXML private CheckBox separateChartsCheck;
    @FXML private CheckBox soilTempCheck;


    @FXML
    private void initialize() {
        dataTypeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            boolean isHistorical = "historical".equals(newVal.getUserData());
            startDatePicker.setDisable(!isHistorical);
            endDatePicker.setDisable(!isHistorical);
            daysField.setDisable(isHistorical);

            // Blok temperature gleby dla danych historycznych
            soilTempCheck.setSelected(false);
            soilTempCheck.setDisable(isHistorical);
        });

        // Obsługa przełącznika lokalizacji
        locationTypeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            boolean isCity = "city".equals(newVal.getUserData());
            cityField.setDisable(!isCity);
            latField.setDisable(isCity);
            lonField.setDisable(isCity);
        });

        if (dataTypeGroup.getSelectedToggle() != null) {
            boolean isHistorical = "historical".equals(dataTypeGroup.getSelectedToggle().getUserData());
            soilTempCheck.setSelected(false);
            soilTempCheck.setDisable(isHistorical);
        }
    }


    @FXML
    private void handleShowData() {
        try {
            boolean isHistorical = "historical".equals(dataTypeGroup.getSelectedToggle().getUserData());
            String location;
            if ("city".equals(locationTypeGroup.getSelectedToggle().getUserData())) {
                location = cityField.getText();
            } else {
                String latText = latField.getText();
                String lonText = lonField.getText();

                if (latText == null || latText.isBlank() || lonText == null || lonText.isBlank()) {
                    showAlert("Błąd lokalizacji", "Musisz podać zarówno szerokość, jak i długość geograficzną.");
                    return;
                }

                try {
                    double lat = Double.parseDouble(latText);
                    double lon = Double.parseDouble(lonText);
                    if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
                        showAlert("Błąd lokalizacji", "Szerokość geograficzna musi być w zakresie -90 do 90, a długość w zakresie -180 do 180.");
                        return;
                    }
                    location = lat + "," + lon;
                } catch (NumberFormatException e) {
                    showAlert("Błąd formatu", "Współrzędne muszą być liczbami (np. 52.23, 21.01).");
                    return;
                }
            }

            if (location == null || location.isBlank()) {
                showAlert("Błąd lokalizacji", "Lokalizacja nie wpisana. Musisz wpisać lokalizację.");
            }

            String startDate = isHistorical && startDatePicker.getValue() != null
                    ? startDatePicker.getValue().toString()
                    : null;
            String endDate = isHistorical && endDatePicker.getValue() != null
                    ? endDatePicker.getValue().toString()
                    : null;

            // Walidacja zakresu dat dla danych historycznych
            if (isHistorical) {
                if (startDate == null || endDate == null) {
                    showAlert("Błąd daty", "Musisz wybrać zarówno datę początkową, jak i końcową.");
                    return;
                }

                if (endDate.compareTo(startDate) < 0) {
                    showAlert("Błąd daty", "Data końcowa nie może być wcześniejsza niż początkowa.");
                    return;
                }

                if (java.time.LocalDate.parse(endDate).isAfter(java.time.LocalDate.now())) {
                    showAlert("Błąd daty", "Data końcowa nie może być w przyszłości dla danych historycznych.");
                    return;
                }
            }


            int days = !isHistorical && daysField.getText() != null && !daysField.getText().isEmpty()
                    ? Integer.parseInt(daysField.getText())
                    : 0;
            if (!isHistorical){
                if (daysField.getText() == null || daysField.getText().isBlank()) {
                    showAlert("Błąd daty", "Liczba dni nie wpisana. Musisz wpisać liczbę dni dla prognozy.");
                }



                if (days < 1 || days > 16) {
                    showAlert("Błąd daty", "Dane prognozozwe dostępne od 1 do 16 dni. Wpisz nową liczbę");
                }
            }


            ServicePogody service = new ServicePogody();
            List<DataPogoda> dane = service.getWeatherData(isHistorical, location, startDate, endDate, days);

            if (!tempCheck.isSelected() &&
                    !windCheck.isSelected() &&
                    !rainCheck.isSelected() &&
                    !pressureCheck.isSelected() &&
                    !soilTempCheck.isSelected()) {
                showAlert("Błąd wyboru danych", "Musisz wybrać co najmniej jeden typ danych pogodowych (np. temperatura, wiatr, deszcz itd.).");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/views/wykres.fxml"));
            Parent root = loader.load();

            WykresController controller = loader.getController();
            controller.ustawDane(
                    dane,
                    tempCheck.isSelected(),
                    windCheck.isSelected(),
                    rainCheck.isSelected(),
                    pressureCheck.isSelected(),
                    soilTempCheck.isSelected(),
                    separateChartsCheck.isSelected()
            );


            Stage stage = new Stage();

            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

            Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());
            stage.setScene(scene);
            stage.setX(screenBounds.getMinX());
            stage.setY(screenBounds.getMinY());
            stage.setWidth(screenBounds.getWidth());
            stage.setHeight(screenBounds.getHeight());

            stage.setTitle("Wykres pogodowy");
            stage.show();


        } catch (NumberFormatException e) {
            showAlert("Błąd formatu", "Liczba dni musi być poprawną liczbą całkowitą.");
        } catch (Exception e) {
            showAlert("Błąd lokalizacji", e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


}