
package bybjerrum.dao;

import bybjerrum.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserDAO {

    private final EntityManagerFactory emf;

    public User createUser(String username, String password) {

        try (EntityManager em = emf.createEntityManager()) {

            if (findByUsername(username) != null) {
                throw new IllegalArgumentException(
                        "Username already exists"
                );
            }

            User user = new User(username, password);

            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();

            return user;
        }
    }

    public User findByUsername(String username) {

        try (EntityManager em = emf.createEntityManager()) {

            return em.createQuery(
                            "SELECT u FROM User u WHERE u.username = :username",
                            User.class
                    )
                    .setParameter("username", username)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        }
    }

}