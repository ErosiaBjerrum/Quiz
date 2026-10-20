package bybjerrum.config;

import bybjerrum.controller.QuestionController;
import bybjerrum.dao.AnswerDAO;
import bybjerrum.dao.CategoryDAO;
import bybjerrum.dao.QuestionDAO;
import bybjerrum.provider.QuestionProvider;
import bybjerrum.provider.TdbQuestionProvider;
import bybjerrum.routes.QuestionRoutes;
import bybjerrum.service.QuestionImportService;
import io.javalin.Javalin;
import jakarta.persistence.EntityManagerFactory;

import java.util.Map;

public class ApplicationConfig {

    public void runApplication() {
        EntityManagerFactory emf =
                HibernateConfig.getEntityManagerFactory();

        Javalin app = createApp(emf, new TdbQuestionProvider());
        app.start(7070);
    }

    public Javalin createApp(
            EntityManagerFactory emf,
            QuestionProvider questionProvider
    ) {
        QuestionDAO questionDAO = new QuestionDAO(emf);
        AnswerDAO answerDAO = new AnswerDAO(emf);
        CategoryDAO categoryDAO = new CategoryDAO(emf);

        QuestionImportService questionImportService =
                new QuestionImportService(
                        questionProvider,
                        questionDAO,
                        categoryDAO
                );

        QuestionController questionController =
                new QuestionController(
                        questionDAO,
                        answerDAO,
                        questionImportService
                );

        QuestionRoutes questionRoutes =
                new QuestionRoutes(questionController);

        Javalin app = Javalin.create();

        app.exception(Exception.class, (e, ctx) -> {

            ctx.status(500).json(
                    Map.of(
                            "status", 500,
                            "msg", "An unexpected server error occurred"
                    )
            );
        });

        questionRoutes.register(app);
        return app;
    }
}