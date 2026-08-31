package bybjerrum.persistence;

import jakarta.persistence.*;

@Entity
public class ContestantQuizSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    private Category favoriteCategory;
}

