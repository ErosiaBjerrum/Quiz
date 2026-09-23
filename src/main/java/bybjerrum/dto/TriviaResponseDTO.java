package bybjerrum.dto;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class TriviaResponseDTO {

    private int response_code;
    private List<QuestionDTO> results;



    public TriviaResponseDTO() {
    }

    public int getResponse_code() {
        return response_code;
    }

    public void setResponse_code(int response_code) {
        this.response_code = response_code;
    }

    public List<QuestionDTO> getResults() {
        return results;
    }

    public void setResults(List<QuestionDTO> results) {
        this.results = results;
    }
}