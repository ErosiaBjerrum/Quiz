package bybjerrum.controller;

import bybjerrum.dao.AnswerDAO;
import bybjerrum.dao.QuestionDAO;
import bybjerrum.dto.AnswerRequestDTO;
import bybjerrum.entity.Answer;
import bybjerrum.mapper.QuestionMapper;
import bybjerrum.service.QuestionImportService;
import io.javalin.http.Context;
import java.util.Map;


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
                .map(QuestionMapper::toDTO)
                .toList();

        ctx.json(response);
    }

    public void checkAnswer(Context ctx) {
        AnswerRequestDTO request =
                ctx.bodyAsClass(AnswerRequestDTO.class);

        if (request.getAnswerId() == null) {
            ctx.status(400).json(
                    Map.of(
                            "status", 400,
                            "msg", "answerId is required"
                    )
            );
            return;
        }

        Answer answer =
                answerDAO.getByID(request.getAnswerId());

        if (answer == null) {
            ctx.status(404).json(
                    Map.of(
                            "status", 404,
                            "msg", "Answer not found"
                    )
            );
            return;
        }

        ctx.json(
                Map.of("correct", answer.getIsCorrect())
        );
    }

    public void importQuestions(Context ctx) {

        questionImportService.importQuestions();

        ctx.status(201).json(
                Map.of(
                        "message", "Questions imported successfully"
                )
        );
    }

}