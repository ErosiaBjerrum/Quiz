package bybjerrum.dao;

import bybjerrum.entity.Question;
import bybjerrum.entity.QuizSession;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class QuizSessionDAO {

    private final EntityManagerFactory emf;

    public QuizSession create(QuizSession quizSession) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(quizSession);
            em.getTransaction().commit();
            return quizSession;
        }
    }

    public QuizSession getById(int id) {

        try (EntityManager em = emf.createEntityManager()) {

            QuizSession session = em.createQuery(
                            "SELECT DISTINCT q FROM QuizSession q " +
                                    "LEFT JOIN FETCH q.questions " +
                                    "WHERE q.id = :id",
                            QuizSession.class
                    )
                    .setParameter("id", id)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

            // Loads the answers while the EntityManager is still open,
            // so they can also be accessed after it closes.
            if (session != null) {
                for (Question question : session.getQuestions()) {
                    question.getAnswers().size();
                }
            }

            return session;
        }
    }

    public QuizSession update(QuizSession quizSession) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            QuizSession updated = em.merge(quizSession);
            em.getTransaction().commit();
            return updated;
        }
    }

    public boolean roomCodeExists(String code) {
        try (EntityManager em = emf.createEntityManager()) {
            Long count = em.createQuery(
                            "SELECT COUNT(q) FROM QuizSession q WHERE q.roomCode = :code",
                            Long.class
                    )
                    .setParameter("code", code)
                    .getSingleResult();

            return count > 0;
        }
    }

    public QuizSession getByRoomCode(String roomCode) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                            "SELECT q FROM QuizSession q WHERE q.roomCode = :roomCode",
                            QuizSession.class
                    )
                    .setParameter("roomCode", roomCode)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        }
    }

}