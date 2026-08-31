package bybjerrum;

import bybjerrum.persistence.*;
import bybjerrum.utils.Populator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Main {

    public static void main(String[] args) {

        EntityManagerFactory emf =
                HibernateConfig.getEntityManagerFactory();

//        try (EntityManager em = emf.createEntityManager()) {
//
//            em.getTransaction().begin();
//
//            Populator populator = new Populator(emf);
//            populator.populate();
//        }

        emf.close();

        QuestionDAO questionDAO =
                new QuestionDAO(HibernateConfig.getEntityManagerFactory());

        System.out.println(questionDAO.getVeryHardQuestions().size());


    }
}