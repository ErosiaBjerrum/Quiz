package bybjerrum.routes;

import bybjerrum.controller.QuizController;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class QuizRoutes {

    public static EndpointGroup getRoutes(QuizController quizController) {

        return () -> {
            path("quizzes", () -> {

                post(quizController::startQuiz);
                get("{sessionId}/question", quizController::getCurrentQuestion);
                post("{sessionId}/answer", quizController::submitAnswer);
                get("{sessionId}/status", quizController::getQuizStatus);
                post("rooms", quizController::createMultiplayerRoom);
                post("rooms/join", quizController::joinMultiplayerRoom);
                get("rooms/{sessionId}/participants", quizController::getRoomParticipants);
            });
        };
    }
}