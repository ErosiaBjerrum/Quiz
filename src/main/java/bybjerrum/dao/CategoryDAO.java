package bybjerrum.dao;

import bybjerrum.entity.Category;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CategoryDAO {

    private final EntityManagerFactory emf;

    public Category create(Category category) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(category);
            em.getTransaction().commit();
            return category;
        }
    }

    public Category getByID(Long id) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.find(Category.class, id);
        }
    }

    public Category getByName(String name) {
        try (EntityManager em = emf.createEntityManager()) {
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

}