package bybjerrum.controller;

import bybjerrum.dao.QuestionDAO;
import bybjerrum.mapper.QuestionMapper;
import bybjerrum.service.QuestionImportService;
import io.javalin.http.Context;
import java.util.Map;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class QuestionController {

    private final QuestionDAO questionDAO;
    private final QuestionImportService questionImportService;

    public void getAllQuestions(Context ctx) {
        var questions = questionDAO.get();

        var response = questions.stream()
                .map(QuestionMapper::toDTO)
                .toList();

        ctx.json(response);
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