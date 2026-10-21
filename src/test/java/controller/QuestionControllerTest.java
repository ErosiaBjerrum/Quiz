package controller;

import bybjerrum.config.ApplicationConfig;
import bybjerrum.config.HibernateConfig;
import bybjerrum.dao.CategoryDAO;
import bybjerrum.dao.QuestionDAO;
import bybjerrum.entity.Answer;
import bybjerrum.entity.Category;
import bybjerrum.entity.Difficulty;
import bybjerrum.entity.Question;
import bybjerrum.provider.QuestionProvider;
import io.javalin.Javalin;
import io.restassured.http.ContentType;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;
import bybjerrum.dto.ProviderQuestionDTO;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class QuestionControllerTest {

    private Javalin app;
    private Long correctAnswerId;
    private Long incorrectAnswerId;

    @BeforeEach
    void setUp() {
        EntityManagerFactory emf =
                HibernateConfig.getEntityManagerFactoryForTest();

        CategoryDAO categoryDAO = new CategoryDAO(emf);
        QuestionDAO questionDAO = new QuestionDAO(emf);

        Category category =
                categoryDAO.create(new Category("Geografi"));

        Question question = new Question(
                "Hvad er Danmarks hovedstad?",
                Difficulty.EASY,
                category,
                new ArrayList<>()
        );

        question.getAnswers().add(
                new Answer("København", true, question));
        question.getAnswers().add(
                new Answer("Aarhus", false, question));
        question.getAnswers().add(
                new Answer("Odense", false, question));
        question.getAnswers().add(
                new Answer("Aalborg", false, question));

        questionDAO.create(question);

        for (Answer answer : question.getAnswers()) {
            if (answer.getIsCorrect()) {
                correctAnswerId = answer.getId();
            } else {
                incorrectAnswerId = answer.getId();
            }
        }

        // Testen skal ikke kontakte OpenTDB
        QuestionProvider testProvider = () -> List.of(
                new ProviderQuestionDTO(
                        "Hvad er Norges hovedstad?",
                        "easy",
                        "Geografi",
                        List.of("Oslo", "Bergen", "Trondheim", "Stavanger"),
                        List.of("Oslo")
                )
        );

        app = new ApplicationConfig()
                .createApp(emf, testProvider)
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
    void getAllQuestionsReturnsQuestionWithAnswers() {
        given()
                .baseUri("http://localhost")
                .port(app.port())
                .when()
                .get("/questions")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", equalTo(1))
                .body("[0].text",
                        equalTo("Hvad er Danmarks hovedstad?"))
                .body("[0].difficulty", equalTo("EASY"))
                .body("[0].category", equalTo("Geografi"))
                .body("[0].answers.text", containsInAnyOrder(
                        "København", "Aarhus", "Odense", "Aalborg"))
                .body("[0].answers",
                        everyItem(not(hasKey("isCorrect"))));
    }

    @Test
    void checkAnswerReturns400WhenAnswerIdIsMissing() {
        given()
                .baseUri("http://localhost")
                .port(app.port())
                .contentType(ContentType.JSON)
                .body("{}")
                .when()
                .post("/questions/answer")
                .then()
                .statusCode(400)
                .body("msg", equalTo("answerId is required"));
    }

    @Test
    void checkAnswerReturns404WhenAnswerDoesNotExist() {
        given()
                .baseUri("http://localhost")
                .port(app.port())
                .contentType(ContentType.JSON)
                .body("{\"answerId\": -1}")
                .when()
                .post("/questions/answer")
                .then()
                .statusCode(404)
                .body("msg", equalTo("Answer not found"));
    }

    @Test
    void checkAnswerReturnsTrueForCorrectAnswer() {
        given()
                .baseUri("http://localhost")
                .port(app.port())
                .contentType(ContentType.JSON)
                .body("{\"answerId\": " + correctAnswerId + "}")
                .when()
                .post("/questions/answer")
                .then()
                .statusCode(200)
                .body("correct", equalTo(true));
    }

    @Test
    void checkAnswerReturnsFalseForIncorrectAnswer() {
        given()
                .baseUri("http://localhost")
                .port(app.port())
                .contentType(ContentType.JSON)
                .body("{\"answerId\": " + incorrectAnswerId + "}")
                .when()
                .post("/questions/answer")
                .then()
                .statusCode(200)
                .body("correct", equalTo(false));
    }

    @Test
    void importQuestionsSavesQuestionWithAnswers() {
        given()
                .baseUri("http://localhost")
                .port(app.port())
                .when()
                .post("/questions/import")
                .then()
                .statusCode(201);

        given()
                .baseUri("http://localhost")
                .port(app.port())
                .when()
                .get("/questions")
                .then()
                .statusCode(200)
                .body("size()", equalTo(2))
                .body("find { it.text == 'Hvad er Norges hovedstad?' }.category",
                        equalTo("Geografi"))
                .body("find { it.text == 'Hvad er Norges hovedstad?' }.difficulty",
                        equalTo("EASY"))
                .body("find { it.text == 'Hvad er Norges hovedstad?' }.answers.text",
                        containsInAnyOrder(
                                "Oslo", "Bergen", "Trondheim", "Stavanger"));
    }

    @Test
    void importQuestionsDoesNotCreateDuplicates() {
        for (int i = 0; i < 2; i++) {
            given()
                    .baseUri("http://localhost")
                    .port(app.port())
                    .when()
                    .post("/questions/import")
                    .then()
                    .statusCode(201);
        }

        given()
                .baseUri("http://localhost")
                .port(app.port())
                .when()
                .get("/questions")
                .then()
                .statusCode(200)
                .body("size()", equalTo(2))
                .body("findAll { it.text == 'Hvad er Norges hovedstad?' }.size()",
                        equalTo(1));
    }

    @Test
    void importedCorrectAnswerReturnsTrue() {
        given()
                .baseUri("http://localhost")
                .port(app.port())
                .when()
                .post("/questions/import")
                .then()
                .statusCode(201);

        long osloAnswerId =
                given()
                        .baseUri("http://localhost")
                        .port(app.port())
                        .when()
                        .get("/questions")
                        .then()
                        .statusCode(200)
                        .extract()
                        .jsonPath()
                        .getLong(
                                "find { it.text == 'Hvad er Norges hovedstad?' }" +
                                        ".answers.find { it.text == 'Oslo' }.id"
                        );

        given()
                .baseUri("http://localhost")
                .port(app.port())
                .contentType(ContentType.JSON)
                .body("{\"answerId\": " + osloAnswerId + "}")
                .when()
                .post("/questions/answer")
                .then()
                .statusCode(200)
                .body("correct", equalTo(true));
    }

    @Test
    void importedCorrectAnswerReturnsFalse() {
        given()
                .baseUri("http://localhost")
                .port(app.port())
                .when()
                .post("/questions/import")
                .then()
                .statusCode(201);

        long bergenAnswerId =
                given()
                        .baseUri("http://localhost")
                        .port(app.port())
                        .when()
                        .get("/questions")
                        .then()
                        .statusCode(200)
                        .extract()
                        .jsonPath()
                        .getLong(
                                "find { it.text == 'Hvad er Norges hovedstad?' }" +
                                        ".answers.find { it.text == 'Bergen' }.id"
                        );

        given()
                .baseUri("http://localhost")
                .port(app.port())
                .contentType(ContentType.JSON)
                .body("{\"answerId\": " + bergenAnswerId + "}")
                .when()
                .post("/questions/answer")
                .then()
                .statusCode(200)
                .body("correct", equalTo(false));
    }

    @Test
    void importQuestionsReturns500WhenProviderFails() {
        app.stop();

        QuestionProvider failingProvider = () -> {
            throw new IllegalStateException("Simuleret fejl fra spørgsmålskilden");
        };

        app = new ApplicationConfig()
                .createApp(
                        HibernateConfig.getEntityManagerFactoryForTest(),
                        failingProvider
                )
                .start(0);

        given()
                .baseUri("http://localhost")
                .port(app.port())
                .when()
                .post("/questions/import")
                .then()
                .statusCode(500)
                .body("msg", equalTo("An unexpected server error occurred"));
    }

}