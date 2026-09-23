package bybjerrum.utils;

import bybjerrum.entity.Answer;
import bybjerrum.entity.Category;
import bybjerrum.entity.Difficulty;
import bybjerrum.entity.Question;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.ArrayList;

public class Populator {

    private EntityManagerFactory emf;

    public Populator(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public void populate() {
        try (EntityManager em = emf.createEntityManager()) {

            em.getTransaction().begin();

            // Categories
            Category geography = new Category("Geography");
            Category history = new Category("History");
            Category science = new Category("Science");
            Category movies = new Category("Movies");
            Category technology = new Category("Technology");

            em.persist(geography);
            em.persist(history);
            em.persist(science);
            em.persist(movies);
            em.persist(technology);

            // GEOGRAPHY
            createQuestion(em, "What is the capital of Germany?",
                    Difficulty.VERY_EASY, geography,
                    "Berlin", "Munich", "Hamburg", "Frankfurt");

            createQuestion(em, "What is the capital of France?",
                    Difficulty.VERY_EASY, geography,
                    "Paris", "Lyon", "Marseille", "Nice");

            createQuestion(em, "Which continent is Brazil located in?",
                    Difficulty.VERY_EASY, geography,
                    "South America", "North America", "Africa", "Asia");

            createQuestion(em, "What is the capital of Sweden?",
                    Difficulty.EASY, geography,
                    "Stockholm", "Gothenburg", "Malmö", "Uppsala");

            createQuestion(em, "Which country has Lisbon as its capital?",
                    Difficulty.EASY, geography,
                    "Portugal", "Spain", "Italy", "Greece");

            createQuestion(em, "What is the largest ocean on Earth?",
                    Difficulty.EASY, geography,
                    "Pacific Ocean", "Atlantic Ocean", "Indian Ocean", "Arctic Ocean");

            createQuestion(em, "What is the capital of Canada?",
                    Difficulty.MEDIUM, geography,
                    "Ottawa", "Toronto", "Vancouver", "Montreal");

            createQuestion(em, "Which country contains the city of Kraków?",
                    Difficulty.MEDIUM, geography,
                    "Poland", "Hungary", "Romania", "Slovakia");

            createQuestion(em, "What is the capital of Mongolia?",
                    Difficulty.HARD, geography,
                    "Ulaanbaatar", "Astana", "Tashkent", "Bishkek");

            createQuestion(em, "Which country has Yerevan as its capital?",
                    Difficulty.VERY_HARD, geography,
                    "Armenia", "Georgia", "Azerbaijan", "Moldova");


            // HISTORY
            createQuestion(em, "In which year did World War II end?",
                    Difficulty.VERY_EASY, history,
                    "1945", "1939", "1941", "1950");

            createQuestion(em, "Who was the first president of the United States?",
                    Difficulty.VERY_EASY, history,
                    "George Washington", "Thomas Jefferson", "Abraham Lincoln", "John Adams");

            createQuestion(em, "The pyramids of Giza were built in which ancient civilization?",
                    Difficulty.EASY, history,
                    "Ancient Egypt", "Ancient Rome", "Ancient Greece", "Mesopotamia");

            createQuestion(em, "Which city was buried by Mount Vesuvius in AD 79?",
                    Difficulty.EASY, history,
                    "Pompeii", "Athens", "Sparta", "Carthage");

            createQuestion(em, "Who was known as the Maid of Orléans?",
                    Difficulty.MEDIUM, history,
                    "Joan of Arc", "Marie Antoinette", "Catherine de Medici", "Eleanor of Aquitaine");

            createQuestion(em, "Which empire was ruled by Genghis Khan?",
                    Difficulty.MEDIUM, history,
                    "Mongol Empire", "Ottoman Empire", "Roman Empire", "Persian Empire");

            createQuestion(em, "In which year did the Berlin Wall fall?",
                    Difficulty.MEDIUM, history,
                    "1989", "1987", "1991", "1993");

            createQuestion(em, "Who succeeded Julius Caesar as Rome's first emperor?",
                    Difficulty.HARD, history,
                    "Augustus", "Nero", "Trajan", "Marcus Aurelius");

            createQuestion(em, "Which battle ended Napoleon's rule in 1815?",
                    Difficulty.HARD, history,
                    "Battle of Waterloo", "Battle of Trafalgar", "Battle of Leipzig", "Battle of Austerlitz");

            createQuestion(em, "Which Byzantine emperor ordered the construction of Hagia Sophia?",
                    Difficulty.VERY_HARD, history,
                    "Justinian I", "Constantine XI", "Theodosius I", "Basil II");


            // SCIENCE
            createQuestion(em, "What planet is known as the Red Planet?",
                    Difficulty.VERY_EASY, science,
                    "Mars", "Venus", "Jupiter", "Mercury");

            createQuestion(em, "What gas do humans need to breathe?",
                    Difficulty.VERY_EASY, science,
                    "Oxygen", "Nitrogen", "Helium", "Hydrogen");

            createQuestion(em, "What is H2O commonly called?",
                    Difficulty.VERY_EASY, science,
                    "Water", "Hydrogen", "Oxygen", "Salt");

            createQuestion(em, "How many planets are in the Solar System?",
                    Difficulty.EASY, science,
                    "8", "7", "9", "10");

            createQuestion(em, "What is the chemical symbol for gold?",
                    Difficulty.EASY, science,
                    "Au", "Ag", "Fe", "Cu");

            createQuestion(em, "What force keeps planets in orbit around the Sun?",
                    Difficulty.EASY, science,
                    "Gravity", "Magnetism", "Friction", "Electricity");

            createQuestion(em, "What is the largest organ of the human body?",
                    Difficulty.MEDIUM, science,
                    "Skin", "Liver", "Brain", "Lungs");

            createQuestion(em, "Which element has atomic number 6?",
                    Difficulty.MEDIUM, science,
                    "Carbon", "Oxygen", "Nitrogen", "Helium");

            createQuestion(em, "What particle has a negative electric charge?",
                    Difficulty.HARD, science,
                    "Electron", "Proton", "Neutron", "Photon");

            createQuestion(em, "What is the SI unit of electric resistance?",
                    Difficulty.VERY_HARD, science,
                    "Ohm", "Volt", "Ampere", "Watt");


            // MOVIES
            createQuestion(em, "Which film series features the character Indiana Jones?",
                    Difficulty.VERY_EASY, movies,
                    "Indiana Jones", "Star Wars", "Jurassic Park", "Rocky");

            createQuestion(em, "Which fictional archaeologist is played by Harrison Ford?",
                    Difficulty.VERY_EASY, movies,
                    "Indiana Jones", "James Bond", "Rocky Balboa", "John McClane");

            createQuestion(em, "Which film features a theme park populated by cloned dinosaurs?",
                    Difficulty.VERY_EASY, movies,
                    "Jurassic Park", "Jaws", "Alien", "King Kong");

            createQuestion(em, "Who directed Titanic?",
                    Difficulty.EASY, movies,
                    "James Cameron", "Steven Spielberg", "Ridley Scott", "George Lucas");

            createQuestion(em, "Which film features the character Rocky Balboa?",
                    Difficulty.EASY, movies,
                    "Rocky", "Raging Bull", "Creed", "The Fighter");

            createQuestion(em, "Who directed Pulp Fiction?",
                    Difficulty.MEDIUM, movies,
                    "Quentin Tarantino", "Martin Scorsese", "David Fincher", "Francis Ford Coppola");

            createQuestion(em, "Which actor played Maximus in Gladiator?",
                    Difficulty.MEDIUM, movies,
                    "Russell Crowe", "Brad Pitt", "Mel Gibson", "Tom Hanks");

            createQuestion(em, "Who directed Blade Runner?",
                    Difficulty.HARD, movies,
                    "Ridley Scott", "James Cameron", "Stanley Kubrick", "David Lynch");

            createQuestion(em, "Which 1957 film was directed by Ingmar Bergman and features a chess game with Death?",
                    Difficulty.HARD, movies,
                    "The Seventh Seal", "Persona", "Wild Strawberries", "Fanny and Alexander");

            createQuestion(em, "Who directed the 1927 film Metropolis?",
                    Difficulty.VERY_HARD, movies,
                    "Fritz Lang", "F. W. Murnau", "Charlie Chaplin", "Sergei Eisenstein");


            // TECHNOLOGY
            createQuestion(em, "What does CPU stand for?",
                    Difficulty.VERY_EASY, technology,
                    "Central Processing Unit", "Computer Processing Utility",
                    "Central Program Unit", "Computer Power Unit");

            createQuestion(em, "What does RAM stand for?",
                    Difficulty.VERY_EASY, technology,
                    "Random Access Memory", "Read Access Memory",
                    "Rapid Application Memory", "Remote Access Module");

            createQuestion(em, "Which company develops the Windows operating system?",
                    Difficulty.VERY_EASY, technology,
                    "Microsoft", "Apple", "Google", "IBM");

            createQuestion(em, "What does HTML stand for?",
                    Difficulty.EASY, technology,
                    "HyperText Markup Language", "High Text Machine Language",
                    "Hyper Transfer Markup Language", "Home Tool Markup Language");

            createQuestion(em, "Which language runs natively in most web browsers?",
                    Difficulty.EASY, technology,
                    "JavaScript", "Java", "Python", "C#");

            createQuestion(em, "What does SQL primarily deal with?",
                    Difficulty.MEDIUM, technology,
                    "Databases", "Image processing", "Operating systems", "Computer graphics");

            createQuestion(em, "What HTTP method is normally used to retrieve data?",
                    Difficulty.MEDIUM, technology,
                    "GET", "POST", "DELETE", "PATCH");

            createQuestion(em, "What does API stand for?",
                    Difficulty.MEDIUM, technology,
                    "Application Programming Interface", "Application Processing Internet",
                    "Advanced Programming Integration", "Automated Program Interface");

            createQuestion(em, "Which HTTP status code means 'Not Found'?",
                    Difficulty.HARD, technology,
                    "404", "200", "301", "500");

            createQuestion(em, "Which normal form removes transitive dependencies in relational database design?",
                    Difficulty.VERY_HARD, technology,
                    "Third Normal Form", "First Normal Form",
                    "Second Normal Form", "Boyce-Codd Normal Form");

            em.getTransaction().commit();

            System.out.println("50 questions added successfully!");
        }
    }

    private void createQuestion(
            EntityManager em,
            String questionText,
            Difficulty difficulty,
            Category category,
            String correctAnswer,
            String wrongAnswer1,
            String wrongAnswer2,
            String wrongAnswer3) {

        Question question = new Question(
                questionText,
                difficulty,
                category,
                new ArrayList<>()
        );

        em.persist(question);

        Answer a1 = new Answer(correctAnswer, true, question);
        Answer a2 = new Answer(wrongAnswer1, false, question);
        Answer a3 = new Answer(wrongAnswer2, false, question);
        Answer a4 = new Answer(wrongAnswer3, false, question);

        question.getAnswers().add(a1);
        question.getAnswers().add(a2);
        question.getAnswers().add(a3);
        question.getAnswers().add(a4);

        em.persist(a1);
        em.persist(a2);
        em.persist(a3);
        em.persist(a4);
    }
}