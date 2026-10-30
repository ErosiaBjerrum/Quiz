package bybjerrum.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class QuizSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int numberOfQuestions;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    @ManyToOne
    private Category category;

    @ManyToMany
    @OrderColumn
    private List<Question> questions;

    private int currentQuestionIndex;
    private boolean finished;

    @Enumerated(EnumType.STRING)
    private QuizPhase phase = QuizPhase.LOBBY;

    @Column(unique = true, length = 4)
    private String roomCode;
    private Integer hostContestantId;

    public QuizSession(int numberOfQuestions, Difficulty difficulty, Category category) {
        this.numberOfQuestions = numberOfQuestions;
        this.difficulty = difficulty;
        this.category = category;
    }
}