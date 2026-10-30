package bybjerrum.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QuizAnswerResponseDTO {

    private boolean correct;
    private String correctAnswer;
    private int pointsEarned;
    private int totalScore;
}