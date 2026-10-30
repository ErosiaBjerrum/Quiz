package bybjerrum.config;

import bybjerrum.controller.QuestionController;
import bybjerrum.controller.QuizWebSocketController;
import bybjerrum.dao.CategoryDAO;
import bybjerrum.dao.QuestionDAO;
import bybjerrum.provider.QuestionProvider;
import bybjerrum.provider.TdbQuestionProvider;
import bybjerrum.routes.QuestionRoutes;
import bybjerrum.service.QuestionImportService;
import bybjerrum.controller.QuizController;
import bybjerrum.dao.ContestantDAO;
import bybjerrum.dao.ContestantQuizSessionDAO;
import bybjerrum.dao.QuizSessionDAO;
import bybjerrum.routes.QuizRoutes;
import bybjerrum.service.QuizService;
import bybjerrum.controller.SecurityController;
import bybjerrum.dao.UserDAO;
import bybjerrum.security.TokenService;
import bybjerrum.routes.SecurityRoutes;
import com.auth0.jwt.exceptions.JWTVerificationException;
import io.javalin.Javalin;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.http.staticfiles.Location;
import jakarta.persistence.EntityManagerFactory;

import java.util.Map;

public class ApplicationConfig {

    public void runApplication() {

        EntityManagerFactory emf =
                HibernateConfig.getEntityManagerFactory();

        Javalin app = createApp(
                emf,
                new TdbQuestionProvider(),
                new TokenService()
        );

        app.start(7070);
    }

    public Javalin createApp(
            EntityManagerFactory emf,
            QuestionProvider questionProvider,
            TokenService tokenService
    ) {
        QuestionDAO questionDAO = new QuestionDAO(emf);
        UserDAO userDAO = new UserDAO(emf);
        SecurityController securityController =
                new SecurityController(userDAO, tokenService);
        CategoryDAO categoryDAO = new CategoryDAO(emf);
        ContestantDAO contestantDAO = new ContestantDAO(emf);
        QuizSessionDAO quizSessionDAO = new QuizSessionDAO(emf);
        ContestantQuizSessionDAO participationDAO =
                new ContestantQuizSessionDAO(emf);

        QuizService quizService = new QuizService(
                contestantDAO,
                quizSessionDAO,
                participationDAO,
                questionDAO
        );

        QuizController quizController =
                new QuizController(quizService, categoryDAO);

        QuestionImportService questionImportService =
                new QuestionImportService(
                        questionProvider,
                        questionDAO,
                        categoryDAO
                );

        QuestionController questionController =
                new QuestionController(
                        questionDAO,
                        questionImportService
                );

        QuestionRoutes questionRoutes =
                new QuestionRoutes(questionController);

        Javalin app = Javalin.create(config -> {

            config.router.apiBuilder(
                    QuizRoutes.getRoutes(quizController)
            );

            config.router.apiBuilder(
                    SecurityRoutes.getRoutes(securityController)
            );

            config.staticFiles.add("/public", Location.CLASSPATH);
        });

        app.exception(IllegalArgumentException.class, (e, ctx) -> {

            ctx.status(400).json(
                    Map.of(
                            "status", 400,
                            "msg", e.getMessage()
                    )
            );
        });

        app.exception(Exception.class, (e, ctx) -> {

            e.printStackTrace();
            ctx.status(500).json(
                    Map.of(
                            "status", 500,
                            "msg", "An unexpected server error occurred"
                    )
            );
        });

        app.before("/questions/import", ctx -> {
            String authHeader = ctx.header("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new UnauthorizedResponse("Missing or invalid token");
            }

            String token = authHeader.substring(7);

            try {
                tokenService.validateToken(token);
            } catch (JWTVerificationException | NumberFormatException e) {
                throw new UnauthorizedResponse("Invalid or expired token");
            }
        });

        questionRoutes.register(app);

        QuizWebSocketController webSocketController =
                new QuizWebSocketController(quizService);

        quizController.setWebSocketController(webSocketController);

        app.ws("/ws/quiz/{sessionId}", webSocketController::register);

        return app;
    }
}