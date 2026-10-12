package bybjerrum.provider;

import bybjerrum.dto.ProviderQuestionDTO;
import bybjerrum.dto.TdbResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.text.StringEscapeUtils;

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

            if (triviaResponse.getResponse_code() != 0) {
                throw new IllegalStateException(
                        "OpenTDB returned error code: "
                                + triviaResponse.getResponse_code()
                );
            }


            return triviaResponse.getResults().stream()
                    .map(q -> {
                        String correctAnswer =
                                StringEscapeUtils.unescapeHtml4(q.getCorrect_answer());

                        List<String> answers = new ArrayList<>();

                        for (String answer : q.getIncorrect_answers()) {
                            answers.add(StringEscapeUtils.unescapeHtml4(answer));
                        }

                        answers.add(correctAnswer);

                        return new ProviderQuestionDTO(
                                StringEscapeUtils.unescapeHtml4(q.getQuestion()),
                                q.getDifficulty(),
                                StringEscapeUtils.unescapeHtml4(q.getCategory()),
                                answers,
                                List.of(correctAnswer)
                        );
                    })
                    .toList();

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Could not fetch questions from OpenTDB", e
            );
        }
    }
}