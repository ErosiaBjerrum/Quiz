
package bybjerrum.controller;

import bybjerrum.config.ApplicationConfig;
import bybjerrum.config.HibernateConfig;
import bybjerrum.dao.CategoryDAO;
import bybjerrum.dao.QuestionDAO;
import bybjerrum.entity.*;
import bybjerrum.provider.QuestionProvider;
import bybjerrum.security.TokenService;
import io.javalin.Javalin;
import io.restassured.http.ContentType;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class QuizControllerTest {

    private Javalin app;

    @BeforeEach
    void setUp() {

        EntityManagerFactory emf =
                HibernateConfig.getEntityManagerFactoryForTest();

        CategoryDAO categoryDAO = new CategoryDAO(emf);
        QuestionDAO questionDAO = new QuestionDAO(emf);


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
                    new Answer("Korrekt", true, question));
            question.getAnswers().add(
                    new Answer("Forkert 1", false, question));
            question.getAnswers().add(
                    new Answer("Forkert 2", false, question));
            question.getAnswers().add(
                    new Answer("Forkert 3", false, question));

            questionDAO.create(question);
        }

        QuestionProvider testProvider = List::of;

        app = new ApplicationConfig()
                .createApp(emf, testProvider, new TokenService("test-secret"))
                .start(0);
    }

    @AfterEach
    void tearDown() {

        try {
            if (app != null) {
                app.stop();
            }
        } finally {
            HibernateConfig.shutdownTestEmf();
        }
    }

    @Test
    void testStartQuizEndpoint() {

        Map<String, Object> request = Map.of(
                "username", "Testspiller",
                "numberOfQuestions", 5,
                "difficulty", "EASY"
        );

        given()
                .port(app.port())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/quizzes")
                .then()
                .statusCode(201)
                .body("sessionId", greaterThan(0));
    }

    @Test
    void testGetCurrentQuestionEndpoint() {

        int sessionId = startQuiz();

        given()
                .port(app.port())
                .when()
                .get("/quizzes/" + sessionId + "/question")
                .then()

                .statusCode(200)

                .body("text", notNullValue())
                .body("difficulty", equalTo("EASY"))

                .body("answers.size()", equalTo(4))

                .body("answers[0].correct", nullValue())
                .body("answers[0].isCorrect", nullValue());
    }

    @Test
    void testAnswerFeedbackEndpoint() {

        int sessionId = startQuiz();

        var question = given()
                .port(app.port())
                .when()
                .get("/quizzes/" + sessionId + "/question")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath();


        Long answerId = question.getList(
                "answers.id", Long.class
        ).stream().findFirst().orElseThrow();

        var response = given()
                .port(app.port())
                .contentType(ContentType.JSON)
                .body(Map.of("answerId", answerId))
                .when()
                .post("/quizzes/" + sessionId + "/answer")
                .then()
                .statusCode(200)
                .body("correctAnswer", equalTo("Korrekt"))
                .extract()
                .jsonPath();

        boolean correct = response.getBoolean("correct");
        int pointsEarned = response.getInt("pointsEarned");
        int totalScore = response.getInt("totalScore");

        assertEquals(correct ? 2 : 0, pointsEarned);
        assertEquals(pointsEarned, totalScore);
    }

    @Test
    void testCompleteQuizThroughRestApi() {

        int sessionId = startQuiz();

        for (int i = 0; i < 5; i++) {

            Long answerId = given()
                    .port(app.port())
                    .when()
                    .get("/quizzes/" + sessionId + "/question")
                    .then()
                    .statusCode(200)
                    .extract()
                    .jsonPath()
                    .getLong("answers[0].id");

            given()
                    .port(app.port())
                    .contentType(ContentType.JSON)
                    .body(Map.of("answerId", answerId))
                    .when()
                    .post("/quizzes/" + sessionId + "/answer")
                    .then()
                    .statusCode(200);
        }

        given()
                .port(app.port())
                .when()
                .get("/quizzes/" + sessionId + "/status")
                .then()
                .statusCode(200)
                .body("answeredQuestions", equalTo(5))
                .body("totalQuestions", equalTo(5))
                .body("finished", equalTo(true));
    }

    @Test
    void testInvalidQuizSettings() {

        given()
                .port(app.port())
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "username", "",
                        "numberOfQuestions", 5,
                        "difficulty", "EASY"
                ))
                .when()
                .post("/quizzes")
                .then()
                .statusCode(400)
                .body("msg", equalTo("Username is required"));

        given()
                .port(app.port())
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "username", "Testspiller",
                        "numberOfQuestions", -5,
                        "difficulty", "EASY"
                ))
                .when()
                .post("/quizzes")
                .then()
                .statusCode(400)
                .body("msg", equalTo(
                        "Number of questions must be 5, 10, 15 or 20"
                ));
    }

    @Test
    void testInvalidAnswerId() {

        int sessionId = startQuiz();

        given()
                .port(app.port())
                .contentType(ContentType.JSON)
                .body(Map.of("answerId", -1))
                .when()
                .post("/quizzes/" + sessionId + "/answer")
                .then()
                .statusCode(400)
                .body("msg", equalTo("Invalid answer ID"));

        given()
                .port(app.port())
                .when()
                .get("/quizzes/" + sessionId + "/status")
                .then()
                .statusCode(200)
                .body("answeredQuestions", equalTo(0))
                .body("score", equalTo(0));
    }

    private int startQuiz() {
        return given()
                .port(app.port())
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "username", "Testspiller",
                        "numberOfQuestions", 5,
                        "difficulty", "EASY"
                ))
                .when()
                .post("/quizzes")
                .then()
                .statusCode(201)
                .extract()
                .path("sessionId");
    }

}