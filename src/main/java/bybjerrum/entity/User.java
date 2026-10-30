
package bybjerrum.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.mindrot.jbcrypt.BCrypt;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "app_users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    // Hashes the password before saving it,
    // so the original password is never stored in the database.
    public User(String username, String password) {
        this.username = username;
        this.passwordHash = BCrypt.hashpw(
                password,
                BCrypt.gensalt()
        );
    }

    public boolean verifyPassword(String password) {
        return BCrypt.checkpw(password, passwordHash);
    }

}