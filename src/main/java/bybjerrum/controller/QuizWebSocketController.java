package bybjerrum.controller;

import bybjerrum.dto.CurrentQuestionDTO;
import bybjerrum.entity.Question;
import bybjerrum.entity.QuizPhase;
import bybjerrum.entity.QuizSession;
import bybjerrum.mapper.QuestionMapper;
import bybjerrum.service.QuizService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.websocket.WsConfig;
import io.javalin.websocket.WsContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class QuizWebSocketController {

    // Keeps track of WebSocket connections for each quiz room.
    // ConcurrentHashMap allows multiple players to connect at the same time.
    private final Map<String, Set<WsContext>> rooms =
            new ConcurrentHashMap<>();
    private final QuizService quizService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ScheduledExecutorService timer =
            Executors.newSingleThreadScheduledExecutor();

public void register(WsConfig ws) {

        ws.onConnect(ctx -> {
            ctx.session.setIdleTimeout(java.time.Duration.ofMinutes(30));
            String sessionId = ctx.pathParam("sessionId");

            rooms.computeIfAbsent(
                    sessionId,
                    id -> ConcurrentHashMap.newKeySet()
            ).add(ctx);

            ctx.send("Connected to quiz " + sessionId);
        });

        ws.onMessage(ctx -> {
            String sessionId = ctx.pathParam("sessionId");
            String message = ctx.message();

            if (message.startsWith("START:")) {

                String token = message.substring(6);

                try {
                    Question question = quizService.startMultiplayerQuiz(
                            Integer.parseInt(sessionId),
                            token
                    );

                    sendQuestion(sessionId, question, 0);
                } catch (Exception e) {
                    ctx.send("ERROR: " + e.getMessage());
                }

            } else if (message.startsWith("NEXT:")) {

                String token = message.substring(5);

                try {
                    Question question = quizService.nextMultiplayerQuestion(
                            Integer.parseInt(sessionId), token
                    );

                    if (question == null) {
                        broadcast(sessionId, "QUIZ_FINISHED");
                    } else {
                        sendQuestion(
                                sessionId,
                                question,
                                quizService.getQuizSession(Integer.parseInt(sessionId))
                                        .getCurrentQuestionIndex()
                        );
                    }

                } catch (Exception e) {
                    ctx.send("ERROR: " + e.getMessage());
                }

            } else if (message.startsWith("ANSWER:")) {

                String[] parts = message.split(":");

                try {
                    quizService.submitMultiplayerAnswer(
                            Integer.parseInt(sessionId),
                            parts[1],
                            Long.parseLong(parts[2])
                    );

                    ctx.send("ANSWER_RECEIVED");

                    if (quizService.haveAllParticipantsAnswered(Integer.parseInt(sessionId))) {
                        finishAndBroadcastQuestion(sessionId);
                    }

                } catch (Exception e) {
                    ctx.send("ERROR: " + e.getMessage());
                }

            }
        });

        ws.onClose(ctx -> {
            String sessionId = ctx.pathParam("sessionId");

            Set<WsContext> connections = rooms.get(sessionId);

            if (connections != null) {
                connections.remove(ctx);
            }
        });
    }

    public void notifyParticipantsUpdated(int sessionId) {
        broadcast(String.valueOf(sessionId), "PARTICIPANTS_UPDATED");
    }

    private void broadcast(String sessionId, String message) {
        Set<WsContext> connections = rooms.get(sessionId);

        if (connections != null) {
            for (WsContext connection : connections) {
                connection.send(message);
            }
        }
    }

    // Ends a question after 30 seconds, but only if the quiz
    // is still on the same question.
    private void startQuestionTimer(String sessionId, int questionIndex) {
        timer.schedule(() -> {
            try {
                int id = Integer.parseInt(sessionId);

                QuizSession session = quizService.getQuizSession(id);

                if (session.getPhase() == QuizPhase.QUESTION
                        && session.getCurrentQuestionIndex() == questionIndex) {
                    finishAndBroadcastQuestion(sessionId);
                }

            } catch (Exception e) {
                System.out.println("Timer: " + e.getMessage());
            }
        }, 30, TimeUnit.SECONDS);
    }

    private void finishAndBroadcastQuestion(String sessionId)
            throws JsonProcessingException {

        int id = Integer.parseInt(sessionId);

        quizService.finishMultiplayerQuestion(id);

        broadcast(sessionId,
                "CORRECT_ANSWER:" + quizService.getCorrectAnswer(id));

        broadcast(sessionId, objectMapper.writeValueAsString(
                quizService.getMultiplayerScores(id)));
    }

    private void sendQuestion(String sessionId, Question question, int questionIndex)
            throws JsonProcessingException {

        CurrentQuestionDTO dto =
                QuestionMapper.toCurrentQuestionDTO(question);

        broadcast(sessionId, objectMapper.writeValueAsString(dto));
        startQuestionTimer(sessionId, questionIndex);
    }

}