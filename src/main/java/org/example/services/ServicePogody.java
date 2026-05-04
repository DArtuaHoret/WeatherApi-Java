package org.example.services;

import org.example.models.DataPogoda;
import org.example.models.Lokacja;
import org.example.utilities.LocalDateTimeAdapter;
import redis.clients.jedis.Jedis;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import com.google.gson.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;

public class ServicePogody {
    private final Gson gson;
    private final ServiceCache cache;
    private final ServiceGeokodowania geokoder;
    private final HttpClient httpClient;

    public ServicePogody() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
        this.cache = new ServiceCache(gson); // Now ServiceCache manages its own connections
        this.geokoder = new ServiceGeokodowania();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public List<DataPogoda> getWeatherData(boolean isHistorical, String location,
                                           String startDate, String endDate, int days) {
        Lokacja lokacja = null;
        String[] coordinates = null;

        if (!location.matches("-?\\d+([.,]\\d+)?([,;]\\s*-?\\d+([.,]\\d+)?)?")) {
            try {
                lokacja = geokoder.znajdzWspolrzedne(location).get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Błąd podczas geokodowania: " + e.getMessage());
            }
            if (lokacja == null) {
                throw new IllegalArgumentException("Nie znaleziono lokalizacji: " + location);
            }
        } else {
            String separator = location.contains(",") ? "," : ";";
            coordinates = location.split(separator);
            if (coordinates.length != 2) {
                throw new RuntimeException("Nieprawidłowy format współrzędnych");
            }
        }

        // Dla prognozy, używamy bieżącej daty i godziny
        if (!isHistorical && days > 0) {
            LocalDateTime now = LocalDateTime.now();
            startDate = now.format(DateTimeFormatter.ISO_LOCAL_DATE);
            endDate = now.plusDays(days).format(DateTimeFormatter.ISO_LOCAL_DATE);
        }

        String kluczCache = generujKluczCache(isHistorical, location, startDate, endDate, days);

        //proba z cahce
        List<DataPogoda> cachedData = cache.pobierzDanePogodowe(kluczCache);
        if (cachedData != null) {
            return cachedData;
        }

        Lokacja finalLokacja = lokacja != null ? lokacja :
                new Lokacja("", parseCoordinate(coordinates[0]), parseCoordinate(coordinates[1]));

        //budowa uri
        String url = budujUrlApi(isHistorical, finalLokacja, startDate, endDate, days);

        try {
            System.out.println("API URL: " + url);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                List<DataPogoda> dane = przetworzOdpowiedzApi(response.body(), !isHistorical);
                dane.forEach(d -> d.setLocation(finalLokacja.getNazwaMiasta()));


                if (!isHistorical) {
                    LocalDateTime now = LocalDateTime.now();
                    dane = dane.stream()
                            .filter(d -> !d.getDate().isBefore(now))
                            .toList();
                }

                cache.zapiszDanePogodowe(kluczCache, dane, isHistorical);
                return dane;
            } else {
                throw new RuntimeException("Błąd API: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Błąd podczas pobierania danych", e);
        }
    }

    private double parseCoordinate(String coord) {
        return Double.parseDouble(coord.trim().replace(',', '.'));
    }

    private String generujKluczCache(boolean isHistorical, String location,
                                     String startDate, String endDate, int days) {
        return String.format("%s:%s:%s:%s:%d",
                isHistorical ? "historyczne" : "prognoza",
                location,
                startDate,
                endDate,
                days);
    }

    private String budujUrlApi(boolean isHistorical, Lokacja lokacja,
                               String startDate, String endDate, int days) {
        String baseUrl = isHistorical
                ? "https://archive-api.open-meteo.com/v1/archive"
                : "https://api.open-meteo.com/v1/forecast";


        String wspolne = "&hourly=temperature_2m,precipitation,windspeed_10m,pressure_msl";
        if (!isHistorical) {
            wspolne += ",soil_temperature_0cm";
        }
        wspolne += "&timezone=auto";


        if (!isHistorical && days > 0) {
            return String.format(java.util.Locale.US,
                    "%s?latitude=%.6f&longitude=%.6f&forecast_days=%d%s",
                    baseUrl,
                    lokacja.getSzerokoscGeo(),
                    lokacja.getDlugoscGeo(),
                    days,
                    wspolne);
        } else {
            return String.format(java.util.Locale.US,
                    "%s?latitude=%.6f&longitude=%.6f&start_date=%s&end_date=%s%s",
                    baseUrl,
                    lokacja.getSzerokoscGeo(),
                    lokacja.getDlugoscGeo(),
                    startDate,
                    endDate,
                    wspolne);
        }
    }

    private List<DataPogoda> przetworzOdpowiedzApi(String jsonOdpowiedz, boolean isForecast) {
        List<DataPogoda> wynik = new ArrayList<>();
        JsonObject root = JsonParser.parseString(jsonOdpowiedz).getAsJsonObject();

        if (!root.has("hourly")) {
            throw new RuntimeException("API nie zwróciło danych godzinnych");
        }

        JsonObject hourly = root.getAsJsonObject("hourly");

        JsonArray timeArray = hourly.getAsJsonArray("time");
        JsonArray tempArray = hourly.getAsJsonArray("temperature_2m");
        JsonArray precipitationArray = hourly.getAsJsonArray("precipitation");
        JsonArray windSpeedArray = hourly.getAsJsonArray("windspeed_10m");
        JsonArray pressureArray = hourly.getAsJsonArray("pressure_msl");
        JsonArray soilTempArray = isForecast && hourly.has("soil_temperature_0cm")
                ? hourly.getAsJsonArray("soil_temperature_0cm")
                : null;

        for (int i = 0; i < timeArray.size(); i++) {
            String timeStr = timeArray.get(i).getAsString();
            LocalDateTime time = LocalDateTime.parse(timeStr.replace(" ", "T"));

            double temp = tempArray.get(i).isJsonNull() ? 0 : tempArray.get(i).getAsDouble();
            double precipitation = precipitationArray.get(i).isJsonNull() ? 0 : precipitationArray.get(i).getAsDouble();
            double windSpeed = windSpeedArray.get(i).isJsonNull() ? 0 : windSpeedArray.get(i).getAsDouble();
            double pressure = pressureArray.get(i).isJsonNull() ? 0 : pressureArray.get(i).getAsDouble();
            double soilTemp = (soilTempArray != null && !soilTempArray.get(i).isJsonNull())
                    ? soilTempArray.get(i).getAsDouble()
                    : 0;

            DataPogoda data = new DataPogoda(
                    "",
                    time,
                    temp,
                    windSpeed,
                    precipitation,
                    pressure,
                    soilTemp
            );
            wynik.add(data);
        }

        return wynik;
    }

}