package org.ispw.fastridetrack.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.ispw.fastridetrack.exception.IPLocationException;
import org.ispw.fastridetrack.model.Coordinate;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class IPLocationService {

    // HttpClient riutilizzabile e thread-safe
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    private IPLocationService() {
        throw new UnsupportedOperationException("Utility class - non deve essere istanziata");
    }

    public static Coordinate getCoordinateFromIP(String ip) throws IPLocationException {
        String url = "http://ip-api.com/json/" + ip;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<java.io.InputStream> response = CLIENT.send(
                    request,
                    HttpResponse.BodyHandlers.ofInputStream()
            );

            try (InputStreamReader reader = new InputStreamReader(response.body())) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

                if ("success".equals(json.get("status").getAsString())) {
                    double lat = json.get("lat").getAsDouble();
                    double lon = json.get("lon").getAsDouble();
                    return new Coordinate(lat, lon);
                } else {
                    throw new IPLocationException("Impossibile determinare la posizione dall'IP.");
                }
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IPLocationException("Errore nella richiesta HTTP per IP Location", e);
        }
    }
}


