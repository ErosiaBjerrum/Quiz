
package bybjerrum.controller;

import bybjerrum.config.ApplicationConfig;
import bybjerrum.config.HibernateConfig;
import bybjerrum.dao.CategoryDAO;
import bybjerrum.dao.QuestionDAO;
import bybjerrum.dto.ProviderQuestionDTO;
import bybjerrum.entity.Answer;
import bybjerrum.entity.Category;
import bybjerrum.entity.Difficulty;
import bybjerrum.entity.Question;
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

class QuestionControllerTest {

    private Javalin app;

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

        QuestionProvider testProvider = () -> List.of(
                new ProviderQuestionDTO(
                        "Hvad er Norges hovedstad?",
                        "easy",
                        "Geografi",
                        List.of(
                                "Oslo",
                                "Bergen",
                                "Trondheim",
                                "Stavanger"
                        ),
                        List.of("Oslo")
                )
        );

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
                        "København",
                        "Aarhus",
                        "Odense",
                        "Aalborg"))
                .body("[0].answers",
                        everyItem(not(hasKey("correct"))))
                .body("[0].answers",
                        everyItem(not(hasKey("isCorrect"))));
    }

    @Test
    void importQuestionsSavesQuestionWithAnswers() {

        String token = new TokenService("test-secret").generateToken(1L);

        given()
                .baseUri("http://localhost")
                .port(app.port())
                .header("Authorization", "Bearer " + token)
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
                                "Oslo",
                                "Bergen",
                                "Trondheim",
                                "Stavanger"));
    }

    @Test
    void importQuestionsDoesNotCreateDuplicates() {

        String token = new TokenService("test-secret").generateToken(1L);

        for (int i = 0; i < 2; i++) {
            given()
                    .baseUri("http://localhost")
                    .port(app.port())
                    .header("Authorization", "Bearer " + token)
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
    void importQuestionsWithoutTokenReturns401() {
        given()
                .port(app.port())
                .when()
                .post("/questions/import")
                .then()
                .statusCode(401);
    }



}