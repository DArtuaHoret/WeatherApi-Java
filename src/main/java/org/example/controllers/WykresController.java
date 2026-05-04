package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.embed.swing.SwingFXUtils;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.example.models.DataPogoda;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class WykresController {
    @FXML private VBox chartContainer;
    @FXML private TextField exportFilenameField;

    private List<DataPogoda> danePogodowe;
    private boolean showTemp;
    private boolean showWind;
    private boolean showRain;
    private boolean showPressure;
    private boolean showSoilTemp;

    public void ustawDane(List<DataPogoda> danePogodowe,
                          boolean showTemp, boolean showWind,
                          boolean showRain, boolean showPressure,
                          boolean showSoilTemp,
                          boolean separateCharts) {
        this.danePogodowe = danePogodowe;
        this.showTemp = showTemp;
        this.showWind = showWind;
        this.showRain = showRain;
        this.showPressure = showPressure;
        this.showSoilTemp = showSoilTemp;

        if (separateCharts) {
            generujOddzielneWykresy();
        } else {
            generujWspolnyWykres();
        }
    }


    private void generujWspolnyWykres() {
        chartContainer.getChildren().clear();

        if (danePogodowe == null || danePogodowe.isEmpty()) return;

        TimeSeriesCollection dataset = new TimeSeriesCollection();

        if (showTemp) {
            TimeSeries series = new TimeSeries("Temperatura (°C)");
            for (DataPogoda dane : danePogodowe) {
                series.add(new Hour(dane.getDate().getHour(),
                                new Day(dane.getDate().getDayOfMonth(),
                                        dane.getDate().getMonthValue(),
                                        dane.getDate().getYear())),
                        dane.getTemperature());
            }
            dataset.addSeries(series);
        }

        if (showWind) {
            TimeSeries series = new TimeSeries("Wiatr (m/s)");
            for (DataPogoda dane : danePogodowe) {
                series.add(new Hour(dane.getDate().getHour(),
                                new Day(dane.getDate().getDayOfMonth(),
                                        dane.getDate().getMonthValue(),
                                        dane.getDate().getYear())),
                        dane.getWindSpeed());
            }
            dataset.addSeries(series);
        }

        if (showRain) {
            TimeSeries series = new TimeSeries("Opady (mm)");
            for (DataPogoda dane : danePogodowe) {
                series.add(new Hour(dane.getDate().getHour(),
                                new Day(dane.getDate().getDayOfMonth(),
                                        dane.getDate().getMonthValue(),
                                        dane.getDate().getYear())),
                        dane.getPrecipitation());
            }
            dataset.addSeries(series);
        }

        if (showPressure) {
            TimeSeries series = new TimeSeries("Ciśnienie (hPa)");
            for (DataPogoda dane : danePogodowe) {
                series.add(new Hour(dane.getDate().getHour(),
                                new Day(dane.getDate().getDayOfMonth(),
                                        dane.getDate().getMonthValue(),
                                        dane.getDate().getYear())),
                        dane.getPressure());
            }
            dataset.addSeries(series);
        }
        if (showSoilTemp) {
            TimeSeries series = new TimeSeries("Temperatura gleby (°C)");
            for (DataPogoda dane : danePogodowe) {
                if (dane.getSoilTemperature() != 0) {
                    series.add(new Hour(dane.getDate().getHour(),
                                    new Day(dane.getDate().getDayOfMonth(),
                                            dane.getDate().getMonthValue(),
                                            dane.getDate().getYear())),
                            dane.getSoilTemperature());
                }
            }
            dataset.addSeries(series);
        }


        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Dane pogodowe",
                "Data",
                "Wartość",
                dataset,
                true,
                true,
                false
        );

        ImageView imageView = new ImageView(
                SwingFXUtils.toFXImage(chart.createBufferedImage(800, 600), null)
        );

        chartContainer.getChildren().add(imageView);
    }

    private void generujOddzielneWykresy() {
        chartContainer.getChildren().clear();

        if (showTemp) {
            TimeSeries series = new TimeSeries("Temperatura (°C)");
            for (DataPogoda dane : danePogodowe) {
                series.add(new Hour(dane.getDate().getHour(),
                                new Day(dane.getDate().getDayOfMonth(),
                                        dane.getDate().getMonthValue(),
                                        dane.getDate().getYear())),
                        dane.getTemperature());
            }
            TimeSeriesCollection dataset = new TimeSeriesCollection(series);
            dodajWykresDoKontenera("Temperatura", "Data", "°C", dataset);
        }

        if (showWind) {
            TimeSeries series = new TimeSeries("Wiatr (m/s)");
            for (DataPogoda dane : danePogodowe) {
                series.add(new Hour(dane.getDate().getHour(),
                                new Day(dane.getDate().getDayOfMonth(),
                                        dane.getDate().getMonthValue(),
                                        dane.getDate().getYear())),
                        dane.getWindSpeed());
            }
            TimeSeriesCollection dataset = new TimeSeriesCollection(series);
            dodajWykresDoKontenera("Wiatr", "Data", "m/s", dataset);
        }

        if (showRain) {
            TimeSeries series = new TimeSeries("Opady (mm)");
            for (DataPogoda dane : danePogodowe) {
                series.add(new Hour(dane.getDate().getHour(),
                                new Day(dane.getDate().getDayOfMonth(),
                                        dane.getDate().getMonthValue(),
                                        dane.getDate().getYear())),
                        dane.getPrecipitation());
            }
            TimeSeriesCollection dataset = new TimeSeriesCollection(series);
            dodajWykresDoKontenera("Opady", "Data", "mm", dataset);
        }

        if (showPressure) {
            TimeSeries series = new TimeSeries("Ciśnienie (hPa)");
            for (DataPogoda dane : danePogodowe) {
                series.add(new Hour(dane.getDate().getHour(),
                                new Day(dane.getDate().getDayOfMonth(),
                                        dane.getDate().getMonthValue(),
                                        dane.getDate().getYear())),
                        dane.getPressure());
            }
            TimeSeriesCollection dataset = new TimeSeriesCollection(series);
            dodajWykresDoKontenera("Ciśnienie", "Data", "hPa", dataset);
        }
        if (showSoilTemp) {
            TimeSeries series = new TimeSeries("Temperatura gleby (°C)");
            for (DataPogoda dane : danePogodowe) {
                if (dane.getSoilTemperature() != 0) {
                    series.add(new Hour(dane.getDate().getHour(),
                                    new Day(dane.getDate().getDayOfMonth(),
                                            dane.getDate().getMonthValue(),
                                            dane.getDate().getYear())),
                            dane.getSoilTemperature());
                }
            }
            TimeSeriesCollection dataset = new TimeSeriesCollection(series);
            dodajWykresDoKontenera("Temperatura gleby", "Data", "°C", dataset);
        }

    }

    private void dodajWykresDoKontenera(String tytul, String oX, String oY, TimeSeriesCollection dataset) {
        JFreeChart chart = ChartFactory.createTimeSeriesChart(tytul, oX, oY, dataset, true, true, false);
        ImageView imageView = new ImageView(SwingFXUtils.toFXImage(chart.createBufferedImage(800, 600), null));
        chartContainer.getChildren().add(imageView);
    }



    @FXML
    private void Export() {
        String nazwaPliku = exportFilenameField.getText();
        if (nazwaPliku == null || nazwaPliku.isEmpty()) {
            nazwaPliku = "dane_pogodowe.txt";
        }

        if (!nazwaPliku.endsWith(".txt")) {
            nazwaPliku += ".txt";
        }

        try (FileWriter writer = new FileWriter(nazwaPliku)) {
            writer.write("Data;Temperatura;Prędkość wiatru;Opady;Ciśnienie;Temperatura gleby\n");

            for (DataPogoda dane : danePogodowe) {
                writer.write(String.format("%s;%.2f;%.2f;%.2f;%.2f;%.2f\n",
                        dane.getDate().toString(),
                        dane.getTemperature(),
                        dane.getWindSpeed(),
                        dane.getPrecipitation(),
                        dane.getPressure(),
                        dane.getSoilTemperature()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
