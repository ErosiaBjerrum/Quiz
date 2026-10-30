package bybjerrum.dao;

import bybjerrum.entity.Category;
import bybjerrum.entity.Difficulty;
import bybjerrum.entity.Question;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class QuestionDAO {

    private final EntityManagerFactory emf;

    public Question create(Question question) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(question);
            em.getTransaction().commit();
            return question;
        }
    }

    public Set<Question> get() {
        try (EntityManager em = emf.createEntityManager()) {
            return new HashSet<>(
                    em.createQuery(
                            "SELECT DISTINCT q FROM Question q LEFT JOIN FETCH q.answers",
                            Question.class
                    ).getResultList()
            );
        }
    }

    public Question getByText(String text) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                            "SELECT q FROM Question q WHERE q.text = :text",
                            Question.class
                    )
                    .setParameter("text", text)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        }
    }

     // Fetches random questions from the database.
     // Difficulty and category are optional filters.
    public List<Question> getRandomQuestions(
            int amount, Difficulty difficulty, Category category) {

        try (EntityManager em = emf.createEntityManager()) {

            String jpql = "SELECT q FROM Question q WHERE 1=1";

            if (difficulty != null) {
                jpql += " AND q.difficulty = :difficulty";
            }

            if (category != null) {
                jpql += " AND q.category = :category";
            }

            jpql += " ORDER BY FUNCTION('RANDOM')";

            TypedQuery<Question> query =
                    em.createQuery(jpql, Question.class);

            if (difficulty != null) {
                query.setParameter("difficulty", difficulty);
            }

            if (category != null) {
                query.setParameter("category", category);
            }

            query.setMaxResults(amount);

            return query.getResultList();
        }
    }

}