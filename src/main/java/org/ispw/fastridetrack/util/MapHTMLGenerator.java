package org.ispw.fastridetrack.util;

import org.ispw.fastridetrack.model.Coordinate;

import java.io.IOException;
import java.util.Locale;

public class MapHTMLGenerator {

    // Costruttore privato per impedire l'istanziazione
    private MapHTMLGenerator() {
        throw new UnsupportedOperationException("Utility class - non deve essere istanziata");
    }

    public static String generateMapHtmlString(Coordinate coordinate) throws IOException {
        String apiKey = System.getenv("GOOGLE_MAPS_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IOException("Google Maps API key non trovata nelle variabili d'ambiente.");
        }

        return String.format(Locale.US, """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta name="viewport" content="initial-scale=1.0">
                    <meta charset="utf-8">
                    <title>Posizione corrente</title>
                    <style>
                        html, body, #map {
                            height: 100%%;
                            margin: 0;
                            padding: 0;
                            width: 100%%;
                        }
                    </style>
                    <script src="https://maps.googleapis.com/maps/api/js?key=%s"></script>
                    <script>
                        function initMap() {
                            var pos = { lat: %f, lng: %f };
                            var map = new google.maps.Map(document.getElementById('map'), {
                                zoom: 14,
                                center: pos,
                                mapTypeId: google.maps.MapTypeId.ROADMAP,
                            });
                            var marker = new google.maps.Marker({
                                position: pos,
                                map: map,
                                title: 'Posizione attuale'
                            });
                        }
                    </script>
                </head>
                <body onload="initMap()">
                    <div id="map"></div>
                </body>
                </html>
                """, apiKey, coordinate.getLatitude(), coordinate.getLongitude());
    }
}




