package bybjerrum.dto;

import bybjerrum.entity.Difficulty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StartQuizRequestDTO {

    private String username;
    private int numberOfQuestions;
    private Difficulty difficulty;
    private Long categoryId;
}