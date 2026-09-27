package bybjerrum;

import bybjerrum.dao.AnswerDAO;
import io.javalin.Javalin;
import bybjerrum.config.HibernateConfig;
import bybjerrum.controller.QuestionController;
import bybjerrum.dao.QuestionDAO;
import bybjerrum.routes.QuestionRoutes;
import jakarta.persistence.EntityManagerFactory;

public class Main {

    public static void main(String[] args) throws Exception {

        EntityManagerFactory emf =
                HibernateConfig.getEntityManagerFactory();

        QuestionDAO questionDAO =
                new QuestionDAO(emf);
        AnswerDAO answerDAO =
                new AnswerDAO(emf);

        QuestionController questionController =
                new QuestionController(questionDAO, answerDAO);

        QuestionRoutes questionRoutes =
                new QuestionRoutes(questionController);

        Javalin app = Javalin.create().start(7070);
        questionRoutes.register(app);




    }
}