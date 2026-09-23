package bybjerrum.dao;

import bybjerrum.entity.Category;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.HashSet;
import java.util.Set;

public class CategoryDAO implements IDAO<Category> {

    private final EntityManagerFactory emf;

    public CategoryDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Category create(Category category) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(category);
            em.getTransaction().commit();
            return category;
        }
    }

    @Override
    public Set<Category> get() {
        try (EntityManager em = emf.createEntityManager()) {
            return new HashSet<>(
                    em.createQuery(
                            "SELECT c FROM Category c",
                            Category.class
                    ).getResultList()
            );
        }
    }

    @Override
    public Category getByID(Long id) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.find(Category.class, id);
        }
    }

    @Override
    public Category update(Category category) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Category updated = em.merge(category);
            em.getTransaction().commit();
            return updated;
        }
    }

    @Override
    public Long delete(Category category) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            Category managed = em.find(Category.class, category.getId());

            if (managed != null) {
                em.remove(managed);
                em.getTransaction().commit();
                return (long) category.getId();
            }

            em.getTransaction().commit();
            return null;
        }
    }

    public Category getByName(EntityManager em, String name) {
        return em.createQuery(
                        "SELECT c FROM Category c WHERE c.name = :name",
                        Category.class
                )
                .setParameter("name", name)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }
}