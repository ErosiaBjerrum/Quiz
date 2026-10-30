package bybjerrum.controller;

import bybjerrum.dto.*;
import bybjerrum.entity.ContestantQuizSession;
import bybjerrum.service.QuizService;
import io.javalin.http.Context;
import bybjerrum.dao.CategoryDAO;
import bybjerrum.entity.Category;
import bybjerrum.entity.QuizSession;
import bybjerrum.entity.Question;
import bybjerrum.mapper.QuestionMapper;
import io.javalin.http.BadRequestResponse;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;
    @Setter
    private QuizWebSocketController webSocketController;
    private final CategoryDAO categoryDAO;

    public void startQuiz(Context ctx) {

        StartQuizRequestDTO request =
                ctx.bodyAsClass(StartQuizRequestDTO.class);

        Category category = getCategory(request);

        QuizSession session = quizService.startQuiz(
                request.getUsername(),
                request.getNumberOfQuestions(),
                request.getDifficulty(),
                category
        );

        ctx.status(201).json(Map.of(
                "sessionId", session.getId()
        ));
    }

    public void createMultiplayerRoom(Context ctx) {

        StartQuizRequestDTO request =
                ctx.bodyAsClass(StartQuizRequestDTO.class);

        Category category = getCategory(request);

        QuizSession session = quizService.createMultiplayerRoom(
                request.getUsername(),
                request.getNumberOfQuestions(),
                request.getDifficulty(),
                category
        );

        ContestantQuizSession host =
                quizService.getHostParticipation(session.getId());

        ctx.status(201).json(Map.of(
                "sessionId", session.getId(),
                "roomCode", session.getRoomCode(),
                "participantToken", host.getParticipantToken()
        ));
    }

    public void joinMultiplayerRoom(Context ctx) {

        Map<String, String> request = ctx.bodyAsClass(Map.class);

        String roomCode = request.get("roomCode");
        String username = request.get("username");

        ContestantQuizSession participation =
                quizService.joinMultiplayerRoom(roomCode, username);
        webSocketController.notifyParticipantsUpdated(participation.getQuizSession().getId());

        ctx.status(200).json(Map.of(
                "sessionId", participation.getQuizSession().getId(),
                "roomCode", participation.getQuizSession().getRoomCode(),
                "participantToken", participation.getParticipantToken(),
                "totalQuestions", participation.getQuizSession().getNumberOfQuestions()
        ));
    }

    public void getCurrentQuestion(Context ctx) {

        int sessionId = Integer.parseInt(ctx.pathParam("sessionId"));

        Question question = quizService.getCurrentQuestion(sessionId);

        CurrentQuestionDTO response =
                QuestionMapper.toCurrentQuestionDTO(question);

        ctx.json(response);
    }

    public void submitAnswer(Context ctx) {

        int sessionId = Integer.parseInt(ctx.pathParam("sessionId"));

        AnswerRequestDTO request =
                ctx.bodyAsClass(AnswerRequestDTO.class);

        if (request.getAnswerId() == null) {
            throw new BadRequestResponse("answerId is required");
        }

        QuizAnswerResponseDTO response = quizService.submitAnswer(
                sessionId,
                request.getAnswerId()
        );

        ctx.json(response);
    }

    public void getQuizStatus(Context ctx) {

        int sessionId = Integer.parseInt(ctx.pathParam("sessionId"));

        QuizStatusDTO status = quizService.getQuizStatus(sessionId);

        ctx.json(status);
    }

    public void getRoomParticipants(Context ctx) {

        int sessionId = Integer.parseInt(ctx.pathParam("sessionId"));

        List<String> participants =
                quizService.getRoomParticipants(sessionId);

        ctx.json(participants);
    }

    private Category getCategory(StartQuizRequestDTO request) {
        if (request.getCategoryId() == null) {
            return null;
        }

        Category category = categoryDAO.getByID(request.getCategoryId());

        if (category == null) {
            throw new BadRequestResponse("Invalid category ID");
        }

        return category;
    }

}