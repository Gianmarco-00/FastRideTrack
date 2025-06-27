package org.ispw.fastridetrack.adapter;

import com.google.gson.*;
import org.ispw.fastridetrack.bean.CoordinateBean;
import org.ispw.fastridetrack.bean.MapRequestBean;
import org.ispw.fastridetrack.exception.MapServiceException;
import org.ispw.fastridetrack.model.Map;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GoogleMapsAdapter implements MapService {
    private static final String API_KEY = System.getenv("GOOGLE_MAPS_API_KEY");
    private static final String STATUS_FIELD = "status";
    private final HttpClient client = HttpClient.newHttpClient();

    @Override
    public Map calculateRoute(MapRequestBean requestBean) throws MapServiceException {
        if (requestBean == null) {
            throw new MapServiceException("MapRequestBean non può essere nullo");
        }

        String from = requestBean.getOriginAsString();
        String to = requestBean.getDestination();
        String html = generateRouteHtml(from, to);
        double estimatedTimeMinutes;
        double distanceKm;

        try {
            String encodedOrigin = URLEncoder.encode(from, StandardCharsets.UTF_8);
            String encodedDestination = URLEncoder.encode(to, StandardCharsets.UTF_8);
            String url = "https://maps.googleapis.com/maps/api/distancematrix/json"
                    + "?origins=" + encodedOrigin
                    + "&destinations=" + encodedDestination
                    + "&mode=driving"
                    + "&language=it"
                    + "&key=" + API_KEY;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                String status = json.get(STATUS_FIELD).getAsString();

                if ("OK".equals(status)) {
                    JsonArray rows = json.getAsJsonArray("rows");
                    JsonObject element = rows.get(0).getAsJsonObject()
                            .getAsJsonArray("elements")
                            .get(0).getAsJsonObject();
                    String elementStatus = element.get(STATUS_FIELD).getAsString();

                    if ("OK".equals(elementStatus)) {
                        int durationSeconds = element.getAsJsonObject("duration").get("value").getAsInt();
                        int distanceMeters = element.getAsJsonObject("distance").get("value").getAsInt();
                        estimatedTimeMinutes = durationSeconds / 60.0;
                        distanceKm = distanceMeters / 1000.0;
                    } else {
                        throw new MapServiceException("Errore nella risposta Distance Matrix: " + elementStatus);
                    }
                } else {
                    throw new MapServiceException("Errore API Distance Matrix: " + status);
                }
            } else {
                throw new MapServiceException("Errore HTTP Distance Matrix: " + response.statusCode());
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new MapServiceException("Operazione interrotta durante il calcolo della distanza o tempo stimato", ie);
        } catch (Exception e) {
            throw new MapServiceException("Errore nel calcolo della distanza o tempo stimato", e);
        }


        return new Map(html, from, to, distanceKm, estimatedTimeMinutes);
    }

    private String generateRouteHtml(String origin, String destination) throws MapServiceException {
        if (API_KEY == null || API_KEY.isBlank()) {
            throw new MapServiceException("Google Maps API key non configurata");
        }

        String encodedOrigin = URLEncoder.encode(origin, StandardCharsets.UTF_8);
        String encodedDestination = URLEncoder.encode(destination, StandardCharsets.UTF_8);

        String src = "https://www.google.com/maps/embed/v1/directions"
                + "?key=" + API_KEY
                + "&origin=" + encodedOrigin
                + "&destination=" + encodedDestination
                + "&mode=driving"
                + "&language=it"
                + "&region=IT";

        return "<iframe width=\"100%\" height=\"100%\" frameborder=\"0\" style=\"border:0\" "
                + "src=\"" + src + "\" allowfullscreen></iframe>";
    }

    @Override
    public String getAddressFromCoordinates(double latitude, double longitude) throws MapServiceException {
        if (API_KEY == null || API_KEY.isBlank()) {
            throw new MapServiceException("Indirizzo non disponibile: API key mancante");
        }

        try {
            String latlngParam = latitude + "," + longitude;
            String url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" +
                    URLEncoder.encode(latlngParam, StandardCharsets.UTF_8) +
                    "&key=" + API_KEY;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                String status = json.get(STATUS_FIELD).getAsString();

                if ("OK".equals(status)) {
                    JsonArray results = json.getAsJsonArray("results");
                    if (results.size() > 0) {
                        JsonObject firstResult = results.get(0).getAsJsonObject();
                        return firstResult.get("formatted_address").getAsString();
                    } else {
                        throw new MapServiceException("Nessun indirizzo trovato per le coordinate");
                    }
                } else {
                    throw new MapServiceException("Errore API Geocoding: " + status);
                }
            } else {
                throw new MapServiceException("Errore HTTP Geocoding: " + response.statusCode());
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new MapServiceException("Operazione interrotta durante il reverse geocoding", ie);
        } catch (Exception e) {
            throw new MapServiceException("Errore durante il reverse geocoding", e);
        }

    }

    @Override
    public CoordinateBean geocodeAddress(String address) throws MapServiceException {
        if (API_KEY == null || API_KEY.isBlank()) {
            throw new MapServiceException("Google Maps API key non configurata");
        }

        try {
            String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
            String url = "https://maps.googleapis.com/maps/api/geocode/json?address="
                    + encodedAddress + "&key=" + API_KEY;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                String status = json.get(STATUS_FIELD).getAsString();

                if ("OK".equals(status)) {
                    JsonArray results = json.getAsJsonArray("results");
                    if (results.size() > 0) {
                        JsonObject location = results.get(0)
                                .getAsJsonObject()
                                .getAsJsonObject("geometry")
                                .getAsJsonObject("location");

                        double lat = location.get("lat").getAsDouble();
                        double lng = location.get("lng").getAsDouble();

                        return new CoordinateBean(lat, lng);
                    } else {
                        throw new MapServiceException("Nessun risultato trovato per l'indirizzo");
                    }
                } else {
                    throw new MapServiceException("Errore API Geocoding: " + status);
                }
            } else {
                throw new MapServiceException("Errore HTTP Geocoding: " + response.statusCode());
            }

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new MapServiceException("Operazione interrotta durante il geocoding", ie);
        } catch (Exception e) {
            throw new MapServiceException("Errore durante il geocoding", e);
        }
    }

}









