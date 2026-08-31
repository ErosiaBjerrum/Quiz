package bybjerrum.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor

@Entity
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    String text;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    @ManyToOne
    private Category category;

    @OneToMany(mappedBy = "question")
    private List<Answer> answers;

    public Question(String text, Difficulty difficulty, Category category, List<Answer> answers) {
        this.text = text;
        this.difficulty = difficulty;
        this.category = category;
        this.answers = answers;
    }
}
