package bybjerrum.routes;

import bybjerrum.controller.QuestionController;
import io.javalin.Javalin;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class QuestionRoutes {

    private final QuestionController questionController;

    public void register(Javalin app) {
        app.get("/questions", questionController::getAllQuestions);
        app.post("/questions/import", questionController::importQuestions);
    }

}