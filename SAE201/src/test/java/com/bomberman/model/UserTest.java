package com.bomberman.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("testuser", "hashedpassword123", "Jean", "Dupont");
    }

    // === Tests de base sans dépendances externes ===

    @Test
    void testConstructorWithParameters() {
        User newUser = new User("player1", "password123", "Alice", "Martin");

        assertEquals("player1", newUser.getUsername());
        assertEquals("password123", newUser.getPasswordHash());
        assertEquals("Alice", newUser.getFirstName());
        assertEquals("Martin", newUser.getLastName());
        assertEquals(0, newUser.getGamesPlayed());
        assertEquals(0, newUser.getGamesWon());
        assertNotNull(newUser.getRegistrationDate());
        assertNotNull(newUser.getLastLoginDate());
    }

    @Test
    void testEmptyConstructor() {
        User emptyUser = new User();

        assertNull(emptyUser.getUsername());
        assertNull(emptyUser.getPasswordHash());
        assertNull(emptyUser.getFirstName());
        assertNull(emptyUser.getLastName());
        assertEquals(0, emptyUser.getGamesPlayed());
        assertEquals(0, emptyUser.getGamesWon());
        assertNull(emptyUser.getRegistrationDate());
        assertNull(emptyUser.getLastLoginDate());
    }

    @Test
    void testGettersAndSetters() {
        assertEquals("testuser", user.getUsername());
        assertEquals("hashedpassword123", user.getPasswordHash());
        assertEquals("Jean", user.getFirstName());
        assertEquals("Dupont", user.getLastName());

        // Test des setters modifiables
        user.setFirstName("Pierre");
        user.setLastName("Durand");
        user.setPasswordHash("newhashedpassword");

        assertEquals("Pierre", user.getFirstName());
        assertEquals("Durand", user.getLastName());
        assertEquals("newhashedpassword", user.getPasswordHash());
    }

    @Test
    void testInitialStatistics() {
        assertEquals(0, user.getGamesPlayed());
        assertEquals(0, user.getGamesWon());
        assertEquals(0.0, user.getWinRate(), 0.01);
    }

    @Test
    void testIncrementGamesPlayed() {
        assertEquals(0, user.getGamesPlayed());

        user.incrementGamesPlayed();
        assertEquals(1, user.getGamesPlayed());

        user.incrementGamesPlayed();
        user.incrementGamesPlayed();
        assertEquals(3, user.getGamesPlayed());
    }

    @Test
    void testIncrementGamesWon() {
        assertEquals(0, user.getGamesWon());

        user.incrementGamesWon();
        assertEquals(1, user.getGamesWon());

        user.incrementGamesWon();
        user.incrementGamesWon();
        assertEquals(3, user.getGamesWon());
    }

    @Test
    void testWinRateCalculation() {
        // Pas de parties jouées
        assertEquals(0.0, user.getWinRate(), 0.01);

        // 1 partie jouée, 0 gagnée
        user.incrementGamesPlayed();
        assertEquals(0.0, user.getWinRate(), 0.01);

        // 1 partie jouée, 1 gagnée (100%)
        user.incrementGamesWon();
        assertEquals(100.0, user.getWinRate(), 0.01);

        // 2 parties jouées, 1 gagnée (50%)
        user.incrementGamesPlayed();
        assertEquals(50.0, user.getWinRate(), 0.01);

        // 4 parties jouées, 3 gagnées (75%)
        user.incrementGamesPlayed();
        user.incrementGamesPlayed();
        user.incrementGamesWon();
        user.incrementGamesWon();
        assertEquals(75.0, user.getWinRate(), 0.01);
    }

    @Test
    void testCheckPassword() {
        assertTrue(user.checkPassword("hashedpassword123"));
        assertFalse(user.checkPassword("wrongpassword"));
        assertFalse(user.checkPassword(""));
        assertFalse(user.checkPassword(null));
    }

    @Test
    void testCheckPasswordWithNullUserPassword() {
        User nullPasswordUser = new User();
        nullPasswordUser.setPasswordHash(null);

        assertFalse(nullPasswordUser.checkPassword("anypassword"));
        assertTrue(nullPasswordUser.checkPassword(null));
    }

    @Test
    void testUpdateLastLogin() {
        String originalLastLogin = user.getLastLoginDate();

        // Petite pause pour s'assurer que les dates sont différentes
        try {
            Thread.sleep(1001); // Plus d'une seconde pour garantir une différence
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        user.updateLastLogin();
        String newLastLogin = user.getLastLoginDate();

        assertNotEquals(originalLastLogin, newLastLogin);
    }

    @Test
    void testDateFormats() {
        // Vérifier que les dates ont le bon format (yyyy-MM-dd HH:mm:ss)
        String registrationDate = user.getRegistrationDate();
        String lastLoginDate = user.getLastLoginDate();

        assertNotNull(registrationDate);
        assertNotNull(lastLoginDate);

        // Format basique : 19 caractères "2024-01-15 14:30:00"
        assertEquals(19, registrationDate.length());
        assertEquals(19, lastLoginDate.length());

        // Vérifier la présence des tirets et espaces aux bonnes positions
        assertEquals('-', registrationDate.charAt(4));
        assertEquals('-', registrationDate.charAt(7));
        assertEquals(' ', registrationDate.charAt(10));
        assertEquals(':', registrationDate.charAt(13));
        assertEquals(':', registrationDate.charAt(16));
    }

    @Test
    void testToString() {
        String expected = "User{username='testuser', name='Jean Dupont', games=0, wins=0, winRate=0,0%}";
        String actual = user.toString();

        // Test partiel car le format peut varier selon la locale pour les décimales
        assertTrue(actual.contains("username='testuser'"));
        assertTrue(actual.contains("name='Jean Dupont'"));
        assertTrue(actual.contains("games=0"));
        assertTrue(actual.contains("wins=0"));
        assertTrue(actual.contains("winRate="));
    }

    @Test
    void testToStringWithStatistics() {
        user.incrementGamesPlayed();
        user.incrementGamesPlayed();
        user.incrementGamesWon();

        String result = user.toString();
        assertTrue(result.contains("games=2"));
        assertTrue(result.contains("wins=1"));
        assertTrue(result.contains("winRate="));
    }

    @Test
    void testSettersForDeserialization() {
        User deserializedUser = new User();

        deserializedUser.setUsername("deserialized");
        deserializedUser.setGamesPlayed(10);
        deserializedUser.setGamesWon(7);
        deserializedUser.setRegistrationDate("2024-01-01 10:00:00");
        deserializedUser.setLastLoginDate("2024-01-02 15:30:00");

        assertEquals("deserialized", deserializedUser.getUsername());
        assertEquals(10, deserializedUser.getGamesPlayed());
        assertEquals(7, deserializedUser.getGamesWon());
        assertEquals("2024-01-01 10:00:00", deserializedUser.getRegistrationDate());
        assertEquals("2024-01-02 15:30:00", deserializedUser.getLastLoginDate());
        assertEquals(70.0, deserializedUser.getWinRate(), 0.01);
    }

    @Test
    void testConstructorWithNullValues() {
        User nullUser = new User(null, null, null, null);

        assertNull(nullUser.getUsername());
        assertNull(nullUser.getPasswordHash());
        assertNull(nullUser.getFirstName());
        assertNull(nullUser.getLastName());
        assertEquals(0, nullUser.getGamesPlayed());
        assertEquals(0, nullUser.getGamesWon());
        assertNotNull(nullUser.getRegistrationDate()); // Date générée même avec null
    }

    @Test
    void testConstructorWithEmptyStrings() {
        User emptyUser = new User("", "", "", "");

        assertEquals("", emptyUser.getUsername());
        assertEquals("", emptyUser.getPasswordHash());
        assertEquals("", emptyUser.getFirstName());
        assertEquals("", emptyUser.getLastName());
    }

    @Test
    void testLargeNumberOfGames() {
        // Test avec un grand nombre de parties
        for (int i = 0; i < 1000; i++) {
            user.incrementGamesPlayed();
            if (i % 3 == 0) { // Gagne 1 partie sur 3
                user.incrementGamesWon();
            }
        }

        assertEquals(1000, user.getGamesPlayed());
        assertEquals(334, user.getGamesWon()); // 1000/3 arrondi vers le bas
        assertEquals(33.4, user.getWinRate(), 0.1);
    }

    @Test
    void testSpecialCharactersInNames() {
        User specialUser = new User("user@123", "pass#456", "Jean-Pierre", "O'Connor");

        assertEquals("user@123", specialUser.getUsername());
        assertEquals("pass#456", specialUser.getPasswordHash());
        assertEquals("Jean-Pierre", specialUser.getFirstName());
        assertEquals("O'Connor", specialUser.getLastName());
    }

    @Test
    void testPasswordSecurity() {
        // Vérifier qu'on peut changer le mot de passe
        String originalPassword = user.getPasswordHash();
        user.setPasswordHash("newSecurePassword123");

        assertNotEquals(originalPassword, user.getPasswordHash());
        assertFalse(user.checkPassword(originalPassword));
        assertTrue(user.checkPassword("newSecurePassword123"));
    }

    @Test
    void testRegistrationAndLoginDatesEquality() {
        // À la création, les dates d'inscription et de dernière connexion sont identiques
        assertEquals(user.getRegistrationDate(), user.getLastLoginDate());
    }
}