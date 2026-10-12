package bybjerrum.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProviderQuestionDTO {

    private String question;
    private String difficulty;
    private String category;
    private List<String> answers;
    private List<String> correctAnswers;
}