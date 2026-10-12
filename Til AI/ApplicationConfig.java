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

public class ApplicationConfig {

    public void runApplication() {
        EntityManagerFactory emf =
                HibernateConfig.getEntityManagerFactory();

        QuestionDAO questionDAO =
                new QuestionDAO(emf);

        AnswerDAO answerDAO =
                new AnswerDAO(emf);

        CategoryDAO categoryDAO =
                new CategoryDAO(emf);

        QuestionProvider questionProvider =
                new TdbQuestionProvider();

        QuestionImportService questionImportService =
                new QuestionImportService(
                        questionProvider,
                        questionDAO,
                        answerDAO,
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

        Javalin app = Javalin.create().start(7070);
        questionRoutes.register(app);
    }

}
