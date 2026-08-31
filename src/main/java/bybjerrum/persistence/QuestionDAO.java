package bybjerrum.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.util.HashSet;
import java.util.Set;

public class QuestionDAO implements IDAO<Question> {

    private final EntityManagerFactory emf;

    public QuestionDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Question create(Question question) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(question);
            em.getTransaction().commit();
            return question;
        }
    }

    @Override
    public Set<Question> get() {
        try (EntityManager em = emf.createEntityManager()) {
            return new HashSet<>(
                    em.createQuery(
                            "SELECT q FROM Question q",
                            Question.class
                    ).getResultList()
            );
        }
    }

    @Override
    public Question getByID(Long id) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.find(Question.class, id);
        }
    }

    @Override
    public Question update(Question question) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Question updated = em.merge(question);
            em.getTransaction().commit();
            return updated;
        }
    }

    @Override
    public Long delete(Question question) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            Question managed = em.find(Question.class, question.getId());

            if (managed != null) {
                em.remove(managed);
                em.getTransaction().commit();
                return (long) question.getId();
            }

            em.getTransaction().commit();
            return null;
        }
    }

    public Set<Question> getVeryHardQuestions() {
        try (EntityManager em = emf.createEntityManager()) {

            TypedQuery<Question> query = em.createQuery(
                    "SELECT q FROM Question q WHERE q.difficulty = :difficulty",
                    Question.class
            );

            query.setParameter("difficulty", Difficulty.VERY_HARD);

            return new HashSet<>(query.getResultList());
        }
    }
}