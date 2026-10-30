package bybjerrum.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ContestantQuizSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    private Contestant contestant;

    @ManyToOne
    private QuizSession quizSession;

    private int score;

    // Each participant gets a unique token to identify them in multiplayer
    @Column(unique = true)
    private String participantToken;
    private boolean answeredCurrentQuestion;
    private Long selectedAnswerId;

    public ContestantQuizSession(Contestant contestant, QuizSession quizSession) {
        this.contestant = contestant;
        this.quizSession = quizSession;
        this.participantToken = java.util.UUID.randomUUID().toString();
    }
}