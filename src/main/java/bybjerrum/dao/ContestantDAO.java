package bybjerrum.dao;

import bybjerrum.entity.Contestant;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ContestantDAO {

    private final EntityManagerFactory emf;

    public Contestant create(Contestant contestant) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(contestant);
            em.getTransaction().commit();
            return contestant;
        }
    }

}