package bybjerrum.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ProviderQuestionDTO {

    private String question;
    private String difficulty;
    private String category;
    private List<String> answers;
    private List<String> correctAnswers;
}