package bybjerrum.mapper;

import bybjerrum.dto.AnswerResponseDTO;
import bybjerrum.dto.CurrentQuestionDTO;
import bybjerrum.dto.QuestionResponseDTO;
import bybjerrum.entity.Question;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    public static CurrentQuestionDTO toCurrentQuestionDTO(Question question) {

        List<AnswerResponseDTO> answers = question.getAnswers()
                .stream()
                .map(answer -> new AnswerResponseDTO(
                        answer.getId(),
                        answer.getText()
                ))
                .toList();

        answers = new ArrayList<>(answers);

        // Shuffles the answers
        // so the correct answer is not always in the same position.
        Collections.shuffle(answers);

        return new CurrentQuestionDTO(
                question.getId(),
                question.getText(),
                question.getDifficulty(),
                answers
        );
    }

}