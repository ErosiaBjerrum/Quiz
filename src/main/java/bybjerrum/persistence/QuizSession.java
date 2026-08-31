package bybjerrum.persistence;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class QuizSession {
    @Id
    @GeneratedValue
    private int id;

    private int numberOfQuestions;
    private Difficulty difficulty;

    private GameMode gameMode;
    private int score;

    @ManyToMany
    private List<Category> categories;


}
