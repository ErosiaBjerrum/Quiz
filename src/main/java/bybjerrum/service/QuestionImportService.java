package bybjerrum.service;

import bybjerrum.dao.CategoryDAO;
import bybjerrum.dao.QuestionDAO;
import bybjerrum.dto.ProviderQuestionDTO;
import bybjerrum.entity.Answer;
import bybjerrum.entity.Category;
import bybjerrum.entity.Difficulty;
import bybjerrum.entity.Question;
import bybjerrum.provider.QuestionProvider;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class QuestionImportService {

    private final QuestionProvider questionProvider;
    private final QuestionDAO questionDAO;
    private final CategoryDAO categoryDAO;

    public void importQuestions() {
        List<ProviderQuestionDTO> questions = questionProvider.fetchQuestions();

        for (ProviderQuestionDTO dto : questions) {

            // Skips questions that already exist in the database.
            if (questionDAO.getByText(dto.getQuestion()) != null) {
                continue;
            }

            Category category = categoryDAO.getByName(dto.getCategory());

            if (category == null) {
                category = categoryDAO.create(new Category(dto.getCategory()));
            }

            Difficulty difficulty =
                    Difficulty.valueOf(dto.getDifficulty().toUpperCase());

            Question question = new Question(
                    dto.getQuestion(),
                    difficulty,
                    category,
                    new ArrayList<>()
            );

            for (String answerText : dto.getAnswers()) {
                boolean isCorrect =
                        dto.getCorrectAnswers().contains(answerText);

                Answer answer = new Answer(answerText, isCorrect, question);
                question.getAnswers().add(answer);
            }

            questionDAO.create(question);
        }
    }

}