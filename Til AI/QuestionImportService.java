package bybjerrum.service;

import bybjerrum.dao.AnswerDAO;
import bybjerrum.dao.CategoryDAO;
import bybjerrum.dao.QuestionDAO;
import bybjerrum.dto.ProviderQuestionDTO;
import bybjerrum.entity.Answer;
import bybjerrum.entity.Category;
import bybjerrum.entity.Difficulty;
import bybjerrum.entity.Question;
import bybjerrum.provider.QuestionProvider;

import java.util.ArrayList;
import java.util.List;

public class QuestionImportService {
    private final QuestionDAO questionDAO;
    private final AnswerDAO answerDAO;
    private final CategoryDAO categoryDAO;

    private final QuestionProvider questionProvider;

    public QuestionImportService(
            QuestionProvider questionProvider,
            QuestionDAO questionDAO,
            AnswerDAO answerDAO,
            CategoryDAO categoryDAO
    ) {
        this.questionProvider = questionProvider;
        this.questionDAO = questionDAO;
        this.answerDAO = answerDAO;
        this.categoryDAO = categoryDAO;
    }

    public List<ProviderQuestionDTO> fetchQuestions() {
        return questionProvider.fetchQuestions();
    }

    // Denne metode
    public void importQuestions() {
        // Her laves spørgsmålene
        List<ProviderQuestionDTO> questions = questionProvider.fetchQuestions();

        for (ProviderQuestionDTO dto : questions) {
            Category category = categoryDAO.getByName(dto.getCategory());
            if (category == null) {
                category = categoryDAO.create(new Category(dto.getCategory()));
            }

            // konverterer tekststring til enum, fx "easy" -> "EASY"
            Difficulty difficulty =
                    Difficulty.valueOf(dto.getDifficulty().toUpperCase());

            Question question = new Question(
                    dto.getQuestion(),
                    difficulty,
                    category,
                    new ArrayList<>()
            );
            question = questionDAO.create(question);

            // Nu laves svarene
            for (String answerText : dto.getAnswers()) {

                boolean isCorrect =
                        dto.getCorrectAnswers().contains(answerText);

                Answer answer =
                        new Answer(answerText, isCorrect, question);
                question.getAnswers().add(answer);

                answerDAO.create(answer);
            }

        }
    }

}