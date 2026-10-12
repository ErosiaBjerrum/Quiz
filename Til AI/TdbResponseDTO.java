package bybjerrum.dto;

import java.util.List;

public class TdbResponseDTO {

    private int response_code;
    private List<QuestionTdbDTO> results;


    public TdbResponseDTO() {
    }

    public int getResponse_code() {
        return response_code;
    }

    public void setResponse_code(int response_code) {
        this.response_code = response_code;
    }

    public List<QuestionTdbDTO> getResults() {
        return results;
    }

    public void setResults(List<QuestionTdbDTO> results) {
        this.results = results;
    }
}