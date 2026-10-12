package bybjerrum.mapper;

import bybjerrum.dto.AnswerResponseDTO;
import bybjerrum.dto.QuestionResponseDTO;
import bybjerrum.entity.Question;

public class QuestionMapper {

    public static QuestionResponseDTO toDTO(Question question) {
        return new QuestionResponseDTO(
                question.getId(),
                question.getText(),
                question.getDifficulty(),
                question.getCategory().getName(),
                question.getAnswers().stream()
                        .map(answer -> new AnswerResponseDTO(
                                answer.getId(),
                                answer.getText()
                        ))
                        .toList()
        );
    }
}