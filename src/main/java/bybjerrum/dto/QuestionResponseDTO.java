package bybjerrum.dto;

import bybjerrum.entity.Difficulty;

import java.util.List;

public class QuestionResponseDTO {

    private Long id;
    private String text;
    private Difficulty difficulty;
    private String category;
    private List<AnswerResponseDTO> answers;

    public QuestionResponseDTO(
            Long id,
            String text,
            Difficulty difficulty,
            String category,
            List<AnswerResponseDTO> answers
    ) {
        this.id = id;
        this.text = text;
        this.difficulty = difficulty;
        this.category = category;
        this.answers = answers;
    }

    public Long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public String getCategory() {
        return category;
    }

    public List<AnswerResponseDTO> getAnswers() {
        return answers;
    }
}