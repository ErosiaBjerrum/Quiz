
package bybjerrum.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void passwordIsHashed() {
        User user = new User("Anders", "Secret123");

        assertNotEquals("Secret123", user.getPasswordHash());
        assertTrue(user.getPasswordHash().startsWith("$2"));
    }

    @Test
    void correctPasswordIsAccepted() {
        User user = new User("Anders", "Secret123");

        assertTrue(user.verifyPassword("Secret123"));
    }

    @Test
    void wrongPasswordIsRejected() {
        User user = new User("Anders", "Secret123");

        assertFalse(user.verifyPassword("WrongPassword"));
    }
}
