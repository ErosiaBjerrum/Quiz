package bybjerrum.service;

import bybjerrum.dto.TriviaResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TriviaApiService {

    public TriviaResponseDTO fetchQuestions() {
        try {
            String url = "https://opentdb.com/api.php?amount=10&type=multiple";

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper objectMapper = new ObjectMapper();

            return objectMapper.readValue(
                    response.body(),
                    TriviaResponseDTO.class
            );

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}