package bybjerrum.controller;

import bybjerrum.dao.AnswerDAO;
import bybjerrum.dao.QuestionDAO;
import bybjerrum.dto.AnswerRequestDTO;
import bybjerrum.dto.AnswerResponseDTO;
import bybjerrum.dto.QuestionResponseDTO;
import bybjerrum.entity.Answer;
import bybjerrum.service.QuestionImportService;
import io.javalin.http.Context;


public class QuestionController {

    private final QuestionDAO questionDAO;
    private final AnswerDAO answerDAO;
    private final QuestionImportService questionImportService;

    public QuestionController(
            QuestionDAO questionDAO,
            AnswerDAO answerDAO,
            QuestionImportService questionImportService
    ) {
        this.questionDAO = questionDAO;
        this.answerDAO = answerDAO;
        this.questionImportService = questionImportService;
    }

    // Metoden lader os vælge, hvilke felter vi vil sende med
    public void getAllQuestions(Context ctx) {

        var questions = questionDAO.get();

        var response = questions.stream()
                .map(q -> new QuestionResponseDTO(
                        q.getId(),
                        q.getText(),
                        q.getDifficulty(),
                        q.getCategory().getName(),
                        q.getAnswers().stream()
                                .map(answer -> new AnswerResponseDTO(
                                        answer.getId(),
                                        answer.getText()
                                ))
                                .toList()
                ))
                .toList();

        ctx.json(response);
    }

    public void checkAnswer(Context ctx) {

        AnswerRequestDTO request =
                ctx.bodyAsClass(AnswerRequestDTO.class);

        Answer answer =
                answerDAO.getByID(request.getAnswerId());

        if (answer == null) {
            ctx.status(404).json(
                    java.util.Map.of(
                            "status", 404,
                            "msg", "Answer not found"
                    )
            );
            return;
        }

        ctx.json(
                java.util.Map.of("correct", answer.getIsCorrect())
        );
    }

    public void importQuestions(Context ctx) {

        questionImportService.importQuestions();

        ctx.status(201).json(
                java.util.Map.of(
                        "message", "Questions imported successfully"
                )
        );
    }

}