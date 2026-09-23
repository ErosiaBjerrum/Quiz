package bybjerrum;

import bybjerrum.config.HibernateConfig;
import bybjerrum.dao.CategoryDAO;
import bybjerrum.dao.QuestionDAO;
import bybjerrum.dto.QuestionDTO;
import bybjerrum.dto.TriviaResponseDTO;
import bybjerrum.entity.Answer;
import bybjerrum.entity.Category;
import bybjerrum.entity.Difficulty;
import bybjerrum.entity.Question;
import bybjerrum.service.TriviaApiService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {

    public static void main(String[] args) throws Exception {

        TriviaApiService service = new TriviaApiService();
        ExecutorService executor = Executors.newFixedThreadPool(3);
        Future<TriviaResponseDTO> future =
                executor.submit(service::fetchQuestions);

        TriviaResponseDTO response = future.get();

        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        CategoryDAO categoryDAO = new CategoryDAO(emf);
        QuestionDAO questionDAO = new QuestionDAO(emf);




        try (EntityManager em = emf.createEntityManager()) {


            em.getTransaction().begin();

            for (QuestionDTO dto : response.getResults()) {

                Question existingQuestion = questionDAO.getByText(em, dto.getQuestion());

                if (existingQuestion != null) {
                    continue;
                }

                Category category =
                        categoryDAO.getByName(em, dto.getCategory());

                if (category == null) {
                    category = new Category(dto.getCategory());
                    em.persist(category);
                }

                Question question = new Question(
                        dto.getQuestion(),
                        Difficulty.valueOf(dto.getDifficulty().toUpperCase()),
                        category,
                        new ArrayList<>()
                );

                Answer correctAnswer =
                        new Answer(dto.getCorrect_answer(), true, question);

                question.getAnswers().add(correctAnswer);

                for (String wrong : dto.getIncorrect_answers()) {
                    question.getAnswers().add(
                            new Answer(wrong, false, question)
                    );
                }

                em.persist(question);

                for (Answer answer : question.getAnswers()) {
                    em.persist(answer);
                }
            }

            em.getTransaction().commit();
    }   finally {
                executor.shutdown();
    }

        emf.close();
    }
}