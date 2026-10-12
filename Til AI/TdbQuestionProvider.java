package bybjerrum.provider;

import bybjerrum.dto.ProviderQuestionDTO;
import bybjerrum.dto.TdbResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class TdbQuestionProvider implements QuestionProvider {

    @Override
    public List<ProviderQuestionDTO> fetchQuestions() {
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

            TdbResponseDTO triviaResponse =
                    objectMapper.readValue(response.body(), TdbResponseDTO.class);

            return triviaResponse.getResults().stream()
                    .map(q -> {
                        List<String> answers = new ArrayList<>(q.getIncorrect_answers());
                        answers.add(q.getCorrect_answer());

                        return new ProviderQuestionDTO(
                                q.getQuestion(),
                                q.getDifficulty(),
                                q.getCategory(),
                                answers,
                                List.of(q.getCorrect_answer())
                        );
                    })
                    .toList();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}