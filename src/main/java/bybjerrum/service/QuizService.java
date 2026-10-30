package bybjerrum.service;

import bybjerrum.dto.QuizAnswerResponseDTO;
import bybjerrum.entity.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import bybjerrum.dao.ContestantDAO;
import bybjerrum.dao.ContestantQuizSessionDAO;
import bybjerrum.dao.QuestionDAO;
import bybjerrum.dao.QuizSessionDAO;
import bybjerrum.dto.QuizStatusDTO;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.NotFoundResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class QuizService {

    private final ContestantDAO contestantDAO;
    private final QuizSessionDAO quizSessionDAO;
    private final ContestantQuizSessionDAO contestantQuizSessionDAO;
    private final QuestionDAO questionDAO;

    public QuizSession startQuiz(
            String username,
            int numberOfQuestions,
            Difficulty difficulty,
            Category category) {

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }

        if (numberOfQuestions != 5 &&
                numberOfQuestions != 10 &&
                numberOfQuestions != 15 &&
                numberOfQuestions != 20) {

            throw new IllegalArgumentException(
                    "Number of questions must be 5, 10, 15 or 20");
        }

        List<Question> questions = questionDAO.getRandomQuestions(
                numberOfQuestions, difficulty, category);

        if (questions.size() < numberOfQuestions) {
            throw new IllegalArgumentException(
                    "Not enough questions available");
        }

        Contestant contestant = new Contestant(username);
        contestantDAO.create(contestant);

        QuizSession session = new QuizSession(
                numberOfQuestions, difficulty, category);

        session.setQuestions(questions);
        quizSessionDAO.create(session);

        ContestantQuizSession participation =
                new ContestantQuizSession(contestant, session);

        contestantQuizSessionDAO.create(participation);

        return session;
    }

    public Question getCurrentQuestion(int sessionId) {

        QuizSession session = getQuizSession(sessionId);

        if (session.isFinished()) {
            throw new IllegalArgumentException("Quiz already finished");
        }

        int index = session.getCurrentQuestionIndex();

        return session.getQuestions().get(index);
    }

    private boolean isCorrectAnswer(Question question, Long answerId) {

        for (Answer answer : question.getAnswers()) {

            if (answer.getId().equals(answerId)) {

                return answer.isCorrect();
            }
        }

        throw new IllegalArgumentException("Invalid answer ID");
    }

    private int calculatePoints(Difficulty difficulty) {

        return switch (difficulty) {

            case EASY -> 2;
            case MEDIUM -> 3;
            case HARD  -> 4;
        };
    }

    public QuizAnswerResponseDTO submitAnswer(int sessionId, Long answerId) {

        QuizSession session = getQuizSession(sessionId);

        if (session.isFinished()) {
            throw new IllegalArgumentException("Quiz already finished");
        }

        ContestantQuizSession participation =
                contestantQuizSessionDAO.getBySessionId(sessionId);

        if (participation == null) {
            throw new IllegalArgumentException("Contestant not found");
        }

        int index = session.getCurrentQuestionIndex();
        Question question = session.getQuestions().get(index);

        boolean correct = isCorrectAnswer(question, answerId);

        String correctAnswer = findCorrectAnswer(question);

        int pointsEarned = 0;

        if (correct) {
            pointsEarned = calculatePoints(question.getDifficulty());

            participation.setScore(
                    participation.getScore() + pointsEarned
            );

            contestantQuizSessionDAO.update(participation);
        }

        session.setCurrentQuestionIndex(index + 1);

        if (session.getCurrentQuestionIndex() >= session.getQuestions().size()) {
            session.setFinished(true);
        }

        quizSessionDAO.update(session);

        return new QuizAnswerResponseDTO(
                correct,
                correctAnswer,
                pointsEarned,
                participation.getScore()
        );
    }

    public QuizStatusDTO getQuizStatus(int sessionId) {

        QuizSession session = getQuizSession(sessionId);

        ContestantQuizSession participation =
                contestantQuizSessionDAO.getBySessionId(sessionId);

        if (participation == null) {
            throw new IllegalArgumentException("Contestant not found");
        }

        int maxScore = 0;

        for (Question question : session.getQuestions()) {
            maxScore += calculatePoints(question.getDifficulty());
        }

        return new QuizStatusDTO(
                session.getId(),
                participation.getContestant().getUsername(),
                participation.getScore(),
                session.getCurrentQuestionIndex(),
                session.getNumberOfQuestions(),
                session.isFinished(),
                maxScore
        );
    }

    public QuizSession createMultiplayerRoom(
            String username,
            int numberOfQuestions,
            Difficulty difficulty,
            Category category) {

        QuizSession session = startQuiz(
                username, numberOfQuestions, difficulty, category);

        ContestantQuizSession hostParticipation =
                contestantQuizSessionDAO.getBySessionId(session.getId());

        session.setHostContestantId(
                hostParticipation.getContestant().getId()
        );

        session.setRoomCode(generateRoomCode());

        quizSessionDAO.update(session);

        return session;
    }

    private String generateRoomCode() {
        Random random = new Random();

        String code = String.format("%04d", random.nextInt(10000));

        while (quizSessionDAO.roomCodeExists(code)) {
            code = String.format("%04d", random.nextInt(10000));
        }

        return code;
    }

    public ContestantQuizSession joinMultiplayerRoom(String roomCode, String username) {

        if (username == null || username.isBlank()) {
            throw new BadRequestResponse("Username is required");
        }

        QuizSession session = quizSessionDAO.getByRoomCode(roomCode);

        if (session == null) {
            throw new NotFoundResponse("Room not found");
        }

        if (session.getPhase() != QuizPhase.LOBBY) {
            throw new BadRequestResponse("Quiz has already started");
        }

        List<ContestantQuizSession> participants =
                contestantQuizSessionDAO.getAllBySessionId(session.getId());
        boolean usernameTaken = participants.stream()
                .anyMatch(p -> p.getContestant().getUsername()
                        .equalsIgnoreCase(username.trim()));

        if (usernameTaken) {
            throw new BadRequestResponse("Username already taken in this room");
        }

        if (participants.size() >= 4) {
            throw new BadRequestResponse("Room is full");
        }

        Contestant contestant = new Contestant(username);
        contestantDAO.create(contestant);

        ContestantQuizSession participation =
                new ContestantQuizSession(contestant, session);

        contestantQuizSessionDAO.create(participation);

        return participation;
    }

    public List<String> getRoomParticipants(int sessionId) {

        getMultiplayerRoom(sessionId);

        return contestantQuizSessionDAO.getAllBySessionId(sessionId)
                .stream()
                .map(participation -> participation.getContestant().getUsername())
                .toList();
    }

    public ContestantQuizSession getHostParticipation(int sessionId) {
        return contestantQuizSessionDAO.getBySessionId(sessionId);
    }

    public Question startMultiplayerQuiz(int sessionId, String participantToken) {

        QuizSession session = getMultiplayerRoom(sessionId);

        ContestantQuizSession participation =
                getParticipant(sessionId, participantToken);

        if (participation.getContestant().getId() != session.getHostContestantId()) {
            throw new ForbiddenResponse("Only the host can start the quiz");
        }

        if (session.getPhase() != QuizPhase.LOBBY) {
            throw new BadRequestResponse("Quiz has already started");
        }

        session.setPhase(QuizPhase.QUESTION);
        quizSessionDAO.update(session);

        return session.getQuestions()
                .get(session.getCurrentQuestionIndex());
    }

    public ContestantQuizSession getParticipant(
            int sessionId, String participantToken) {

        ContestantQuizSession participation =
                contestantQuizSessionDAO.getByToken(participantToken);

        if (participation == null ||
                participation.getQuizSession().getId() != sessionId) {
            throw new ForbiddenResponse("Invalid participant token");
        }

        return participation;
    }

    public void submitMultiplayerAnswer(
            int sessionId, String participantToken, Long answerId) {

        ContestantQuizSession participation =
                getParticipant(sessionId, participantToken);

        QuizSession session = quizSessionDAO.getById(sessionId);

        if (session.getPhase() != QuizPhase.QUESTION) {
            throw new BadRequestResponse("Question is not active");
        }

        if (participation.isAnsweredCurrentQuestion()) {
            throw new BadRequestResponse("Already answered");
        }

        Question question = session.getQuestions()
                .get(session.getCurrentQuestionIndex());

        boolean validAnswer = question.getAnswers().stream()
                .anyMatch(answer -> answer.getId().equals(answerId));

        if (!validAnswer) {
            throw new BadRequestResponse("Invalid answer ID");
        }

        participation.setSelectedAnswerId(answerId);
        participation.setAnsweredCurrentQuestion(true);

        contestantQuizSessionDAO.update(participation);
    }

    public boolean haveAllParticipantsAnswered(int sessionId) {
        List<ContestantQuizSession> participants =
                contestantQuizSessionDAO.getAllBySessionId(sessionId);

        if (participants.isEmpty()) {
            return false;
        }

        for (ContestantQuizSession participant : participants) {
            if (!participant.isAnsweredCurrentQuestion()) {
                return false;
            }
        }

        return true;
    }

    // Calculates scores when a question ends instead of
    // when a player submits their answer.
    public void finishMultiplayerQuestion(int sessionId) {

        QuizSession session = quizSessionDAO.getById(sessionId);

        if (session == null || session.getPhase() != QuizPhase.QUESTION) {
            throw new BadRequestResponse("Question is not active");
        }

        Question question = session.getQuestions()
                .get(session.getCurrentQuestionIndex());

        List<ContestantQuizSession> participants =
                contestantQuizSessionDAO.getAllBySessionId(sessionId);

        for (ContestantQuizSession participant : participants) {

            Long answerId = participant.getSelectedAnswerId();

            if (answerId != null && isCorrectAnswer(question, answerId)) {
                int points = calculatePoints(question.getDifficulty());

                participant.setScore(participant.getScore() + points);
                contestantQuizSessionDAO.update(participant);
            }
        }

        session.setPhase(QuizPhase.RESULT);
        quizSessionDAO.update(session);
    }

    public Map<String, Integer> getMultiplayerScores(int sessionId) {
        Map<String, Integer> scores = new LinkedHashMap<>();

        for (ContestantQuizSession participant :
                contestantQuizSessionDAO.getAllBySessionId(sessionId)) {

            scores.put(
                    participant.getContestant().getUsername(),
                    participant.getScore()
            );
        }

        return scores;
    }

    public String getCorrectAnswer(int sessionId) {
        Question question = getCurrentQuestion(sessionId);
        return findCorrectAnswer(question);
    }

    public Question nextMultiplayerQuestion(int sessionId, String participantToken) {

        QuizSession session = getMultiplayerRoom(sessionId);

        ContestantQuizSession participant =
                getParticipant(sessionId, participantToken);

        if (participant.getContestant().getId() != session.getHostContestantId()) {
            throw new ForbiddenResponse("Only the host can continue");
        }

        if (session.getPhase() != QuizPhase.RESULT) {
            throw new BadRequestResponse("Quiz is not showing results");
        }

        session.setCurrentQuestionIndex(session.getCurrentQuestionIndex() + 1);

        if (session.getCurrentQuestionIndex() >= session.getQuestions().size()) {
            session.setPhase(QuizPhase.FINISHED);
            session.setFinished(true);
            quizSessionDAO.update(session);
            return null;
        }

        session.setPhase(QuizPhase.QUESTION);
        quizSessionDAO.update(session);

        // Resets each player's answer status so they can answer the next question.
        for (ContestantQuizSession p :
                contestantQuizSessionDAO.getAllBySessionId(sessionId)) {
            p.setAnsweredCurrentQuestion(false);
            p.setSelectedAnswerId(null);
            contestantQuizSessionDAO.update(p);
        }

        return session.getQuestions()
                .get(session.getCurrentQuestionIndex());
    }

    public QuizSession getQuizSession(int sessionId) {
        QuizSession session = quizSessionDAO.getById(sessionId);

        if (session == null) {
            throw new NotFoundResponse("Quiz not found");
        }

        return session;
    }

    private QuizSession getMultiplayerRoom(int sessionId) {
        QuizSession session = quizSessionDAO.getById(sessionId);

        if (session == null || session.getRoomCode() == null) {
            throw new NotFoundResponse("Multiplayer room not found");
        }

        return session;
    }

    private String findCorrectAnswer(Question question) {
        for (Answer answer : question.getAnswers()) {
            if (answer.isCorrect()) {
                return answer.getText();
            }
        }

        throw new IllegalArgumentException("No correct answer found");
    }

}