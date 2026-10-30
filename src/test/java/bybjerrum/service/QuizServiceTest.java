package bybjerrum.service;

import bybjerrum.config.HibernateConfig;
import bybjerrum.dao.*;
import bybjerrum.dto.QuizAnswerResponseDTO;
import bybjerrum.entity.*;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class QuizServiceTest {

    private QuizService quizService;
    private ContestantQuizSessionDAO participationDAO;
    private QuizSessionDAO quizSessionDAO;

    @BeforeEach
    void setUp() {

        EntityManagerFactory emf =
                HibernateConfig.getEntityManagerFactoryForTest();

        CategoryDAO categoryDAO = new CategoryDAO(emf);
        QuestionDAO questionDAO = new QuestionDAO(emf);

        ContestantDAO contestantDAO = new ContestantDAO(emf);
        quizSessionDAO = new QuizSessionDAO(emf);
        participationDAO = new ContestantQuizSessionDAO(emf);

        quizService = new QuizService(
                contestantDAO,
                quizSessionDAO,
                participationDAO,
                questionDAO
        );

        Category category =
                categoryDAO.create(new Category("Geografi"));

        for (int i = 1; i <= 5; i++) {

            Question question = new Question(
                    "Testspørgsmål " + i,
                    Difficulty.EASY,
                    category,
                    new ArrayList<>()
            );

            question.getAnswers().add(
                    new Answer("Korrekt svar", true, question));

            question.getAnswers().add(
                    new Answer("Forkert svar 1", false, question));

            question.getAnswers().add(
                    new Answer("Forkert svar 2", false, question));

            question.getAnswers().add(
                    new Answer("Forkert svar 3", false, question));

            questionDAO.create(question);
        }
    }

    @AfterEach
    void tearDown() {

        HibernateConfig.shutdownTestEmf();
    }

    @Test
    void testStartQuiz() {

        QuizSession session = quizService.startQuiz(
                "Testspiller", 5, Difficulty.EASY, null);

        assertEquals(5, session.getQuestions().size());

        ContestantQuizSession participation =
                participationDAO.getBySessionId(session.getId());

        assertNotNull(participation);

        assertEquals("Testspiller",
                participation.getContestant().getUsername());

        assertEquals(0, participation.getScore());

        assertEquals(0, session.getCurrentQuestionIndex());
    }

    @Test
    void testSubmitCorrectAnswer() {

        QuizSession session = quizService.startQuiz(
                "Testspiller", 5, Difficulty.EASY, null);

        Question question = quizService.getCurrentQuestion(session.getId());

        Long correctAnswerId = question.getAnswers().stream()
                .filter(Answer::isCorrect)
                .findFirst()
                .orElseThrow()
                .getId();

        QuizAnswerResponseDTO response = quizService.submitAnswer(
                session.getId(), correctAnswerId);

        assertTrue(response.isCorrect());

        assertEquals(2, response.getPointsEarned());
        assertEquals(2, response.getTotalScore());

        ContestantQuizSession participation =
                participationDAO.getBySessionId(session.getId());

        assertEquals(2, participation.getScore());

        QuizSession updatedSession = quizSessionDAO.getById(session.getId());

        assertEquals(1, updatedSession.getCurrentQuestionIndex());
    }

    @Test
    void testQuizFinishesAfterLastQuestion() {

        QuizSession session = quizService.startQuiz(
                "Testspiller", 5, Difficulty.EASY, null);

        for (int i = 0; i < 5; i++) {

            Question question =
                    quizService.getCurrentQuestion(session.getId());

            Long correctAnswerId = question.getAnswers().stream()
                    .filter(Answer::isCorrect)
                    .findFirst()
                    .orElseThrow()
                    .getId();

            quizService.submitAnswer(session.getId(), correctAnswerId);
        }

        QuizSession updatedSession =
                quizSessionDAO.getById(session.getId());

        assertTrue(updatedSession.isFinished());
        assertEquals(5, updatedSession.getCurrentQuestionIndex());

        ContestantQuizSession participation =
                participationDAO.getBySessionId(session.getId());

        assertEquals(10, participation.getScore());
    }

    @Test
    void testCreateAndJoinMultiplayerRoom() {

        QuizSession session = quizService.createMultiplayerRoom(
                "Host", 5, Difficulty.EASY, null);

        assertNotNull(session.getRoomCode());
        assertEquals(4, session.getRoomCode().length());
        assertEquals(QuizPhase.LOBBY, session.getPhase());

        ContestantQuizSession player =
                quizService.joinMultiplayerRoom(
                        session.getRoomCode(), "Player2");

        assertNotNull(player.getParticipantToken());
        assertEquals(2,
                quizService.getRoomParticipants(session.getId()).size());
    }

    @Test
    void testMultiplayerScoring() {

        QuizSession session = quizService.createMultiplayerRoom(
                "Host", 5, Difficulty.EASY, null);

        ContestantQuizSession host =
                quizService.getHostParticipation(session.getId());

        ContestantQuizSession player =
                quizService.joinMultiplayerRoom(session.getRoomCode(), "Player2");

        Question question = quizService.startMultiplayerQuiz(
                session.getId(), host.getParticipantToken());

        Long correctId = question.getAnswers().stream()
                .filter(Answer::isCorrect)
                .findFirst().orElseThrow().getId();

        Long wrongId = question.getAnswers().stream()
                .filter(a -> !a.isCorrect())
                .findFirst().orElseThrow().getId();

        quizService.submitMultiplayerAnswer(
                session.getId(), host.getParticipantToken(), correctId);

        quizService.submitMultiplayerAnswer(
                session.getId(), player.getParticipantToken(), wrongId);

        quizService.finishMultiplayerQuestion(session.getId());

        var scores = quizService.getMultiplayerScores(session.getId());

        assertEquals(2, scores.get("Host"));
        assertEquals(0, scores.get("Player2"));
    }


    @Test
    void testConcurrentMultiplayerAnswers() throws Exception {

        QuizSession session = quizService.createMultiplayerRoom(
                "Host", 5, Difficulty.EASY, null);

        ContestantQuizSession host =
                quizService.getHostParticipation(session.getId());

        ContestantQuizSession player =
                quizService.joinMultiplayerRoom(session.getRoomCode(), "Player2");

        Question question = quizService.startMultiplayerQuiz(
                session.getId(), host.getParticipantToken());

        Long correctId = question.getAnswers().stream()
                .filter(Answer::isCorrect)
                .findFirst().orElseThrow().getId();

        CompletableFuture<Void> hostAnswer = CompletableFuture.runAsync(() ->
                quizService.submitMultiplayerAnswer(
                        session.getId(), host.getParticipantToken(), correctId));

        CompletableFuture<Void> playerAnswer = CompletableFuture.runAsync(() ->
                quizService.submitMultiplayerAnswer(
                        session.getId(), player.getParticipantToken(), correctId));

        CompletableFuture.allOf(hostAnswer, playerAnswer)
                .get(5, TimeUnit.SECONDS);

        assertTrue(quizService.haveAllParticipantsAnswered(session.getId()));
    }


}