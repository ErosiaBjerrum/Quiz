package bybjerrum.dao;

import bybjerrum.entity.ContestantQuizSession;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ContestantQuizSessionDAO {

    private final EntityManagerFactory emf;

    public ContestantQuizSession create(ContestantQuizSession participation) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(participation);
            em.getTransaction().commit();
            return participation;
        }
    }

    public ContestantQuizSession update(ContestantQuizSession participation) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            ContestantQuizSession updated = em.merge(participation);
            em.getTransaction().commit();
            return updated;
        }
    }

    public ContestantQuizSession getBySessionId(int sessionId) {

        try (EntityManager em = emf.createEntityManager()) {

            return em.createQuery(
                            "SELECT c FROM ContestantQuizSession c " +
                                    "WHERE c.quizSession.id = :sessionId",
                            ContestantQuizSession.class
                    )

                    .setParameter("sessionId", sessionId)

                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        }
    }

    public List<ContestantQuizSession> getAllBySessionId(int sessionId) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                            "SELECT c FROM ContestantQuizSession c " +
                                    "WHERE c.quizSession.id = :sessionId",
                            ContestantQuizSession.class
                    )
                    .setParameter("sessionId", sessionId)
                    .getResultList();
        }
    }

    public ContestantQuizSession getByToken(String token) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                            "SELECT c FROM ContestantQuizSession c " +
                                    "WHERE c.participantToken = :token",
                            ContestantQuizSession.class
                    )
                    .setParameter("token", token)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        }
    }

}