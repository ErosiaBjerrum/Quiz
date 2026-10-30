package bybjerrum.dto;

import bybjerrum.entity.Difficulty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class QuestionResponseDTO {

    private Long id;
    private String text;
    private Difficulty difficulty;
    private String category;
    private List<AnswerResponseDTO> answers;
}