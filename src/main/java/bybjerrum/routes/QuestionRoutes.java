package bybjerrum.routes;

import bybjerrum.controller.QuestionController;
import io.javalin.Javalin;

public class QuestionRoutes {

    private final QuestionController questionController;

    public QuestionRoutes(QuestionController questionController) {
        this.questionController = questionController;
    }

    public void register(Javalin app) {
        app.get("/questions", questionController::getAllQuestions);
        app.post("/questions/answer", questionController::checkAnswer);
    }

}