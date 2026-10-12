package bybjerrum.dao;

import bybjerrum.entity.Answer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.HashSet;
import java.util.Set;

public class AnswerDAO implements IDAO<Answer> {

    private final EntityManagerFactory emf;

    public AnswerDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Answer create(Answer answer) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(answer);
            em.getTransaction().commit();
            return answer;
        }
    }

    @Override
    public Set<Answer> get() {
        try (EntityManager em = emf.createEntityManager()) {
            return new HashSet<>(
                    em.createQuery(
                            "SELECT a FROM Answer a",
                            Answer.class
                    ).getResultList()
            );
        }
    }

    @Override
    public Answer getByID(Long id) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.find(Answer.class, id);
        }
    }

    @Override
    public Answer update(Answer answer) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Answer updated = em.merge(answer);
            em.getTransaction().commit();
            return updated;
        }
    }

    @Override
    public Long delete(Answer answer) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            Answer managed = em.find(Answer.class, answer.getId());

            if (managed != null) {
                em.remove(managed);
                em.getTransaction().commit();
                return (long) answer.getId();
            }

            em.getTransaction().commit();
            return null;
        }
    }
}