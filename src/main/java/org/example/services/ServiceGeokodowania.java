package org.example.services;

import org.example.models.Lokacja;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class ServiceGeokodowania {
    private static final String API = "https://nominatim.openstreetmap.org/search";
    private final HttpClient httpClient;

    public ServiceGeokodowania() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public CompletableFuture<Lokacja> znajdzWspolrzedne(String nazwaMiasta) {
        if (nazwaMiasta == null || nazwaMiasta.trim().isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        String encodedCity = nazwaMiasta.replace(" ", "%20");
        String url = String.format("%s?city=%s&format=json&limit=1", API, encodedCity);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "WeatherApp/1.0")
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        return parseApiResponse(response.body(), nazwaMiasta);
                    }
                    return null;
                })
                .exceptionally(ex -> {
                    System.err.println("Geocoding API error: " + ex.getMessage());
                    return null;
                });
    }

    private Lokacja parseApiResponse(String json, String originalName) {
        try {
            JsonObject firstResult = JsonParser.parseString(json)
                    .getAsJsonArray()
                    .get(0)
                    .getAsJsonObject();

            double lat = firstResult.get("lat").getAsDouble();
            double lon = firstResult.get("lon").getAsDouble();

            Lokacja location = new Lokacja(originalName, lat, lon);
            return location;
        } catch (Exception e) {
            System.err.println("Blad parsowania " + e.getMessage());
            return null;
        }
    }


}