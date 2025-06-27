package org.ispw.fastridetrack.util;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class IPFetcher {
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    private IPFetcher() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String getPublicIP() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.ipify.org"))
                .GET()
                .build();

        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}



