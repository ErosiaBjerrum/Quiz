package bybjerrum.dto;

public class AnswerResponseDTO {

    private Long id;
    private String text;

    // Sikrer, at hver svarmulighed er unik
    public AnswerResponseDTO(Long id, String text) {
        this.id = id;
        this.text = text;
    }

    public Long getId() {
        return id;
    }

    public String getText() {
        return text;
    }
}