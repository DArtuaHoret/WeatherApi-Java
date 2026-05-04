package org.example.models;

import java.time.LocalDate;
import java.util.List;
import java.time.LocalDateTime;

public class DataPogoda {
    private String location;
    private LocalDateTime date;
    private double temperature;
    private double windSpeed;
    private double precipitation;
    private double pressure;
    private double soilTemperature;


    public DataPogoda() {}

    public DataPogoda(String location, LocalDateTime date, double temperature,
                      double windSpeed, double precipitation, double pressure,
                      double soilTemperature) {
        this.location = location;
        this.date = date;
        this.temperature = temperature;
        this.windSpeed = windSpeed;
        this.precipitation = precipitation;
        this.pressure = pressure;
        this.soilTemperature = soilTemperature;
    }



    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDateTime getDate() { return date; }



    public double getTemperature() { return temperature; }


    public double getWindSpeed() { return windSpeed; }


    public double getPrecipitation() { return precipitation; }


    public double getPressure() { return pressure; }


    public double getSoilTemperature() { return soilTemperature; }

}