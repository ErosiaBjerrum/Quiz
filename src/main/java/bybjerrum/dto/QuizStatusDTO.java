package bybjerrum.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QuizStatusDTO {

    private int sessionId;
    private String username;
    private int score;
    private int answeredQuestions;
    private int totalQuestions;
    private boolean finished;
    private int maxScore;
}