package com.bomberman.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player("TestPlayer", 5, 5);
    }

    // === Tests de base sans dépendances externes ===

    @Test
    void testConstructor() {
        Player newPlayer = new Player("Bomberman", 3, 7);

        assertEquals("Bomberman", newPlayer.getName());
        assertEquals(3, newPlayer.getRow());
        assertEquals(7, newPlayer.getCol());
    }

    @Test
    void testConstructorWithEmptyName() {
        Player emptyPlayer = new Player("", 0, 0);
        assertEquals("", emptyPlayer.getName());
        assertEquals(0, emptyPlayer.getRow());
        assertEquals(0, emptyPlayer.getCol());
    }

    @Test
    void testConstructorWithNullName() {
        Player nullPlayer = new Player(null, 1, 2);
        assertNull(nullPlayer.getName());
        assertEquals(1, nullPlayer.getRow());
        assertEquals(2, nullPlayer.getCol());
    }

    @Test
    void testGettersAndSetters() {
        assertEquals("TestPlayer", player.getName());
        assertEquals(5, player.getRow());
        assertEquals(5, player.getCol());

        player.setName("NewName");
        player.setRow(10);
        player.setCol(15);

        assertEquals("NewName", player.getName());
        assertEquals(10, player.getRow());
        assertEquals(15, player.getCol());
    }

    @Test
    void testSetNullName() {
        player.setName(null);
        assertNull(player.getName());
    }

    @Test
    void testSetNegativePositions() {
        player.setRow(-5);
        player.setCol(-10);

        assertEquals(-5, player.getRow());
        assertEquals(-10, player.getCol());
    }

    @Test
    void testToString() {
        String expected = "TestPlayer at (5, 5)";
        assertEquals(expected, player.toString());

        Player customPlayer = new Player("Hero", 10, 15);
        String expectedCustom = "Hero at (10, 15)";
        assertEquals(expectedCustom, customPlayer.toString());
    }

    @Test
    void testToStringWithNullName() {
        Player nullPlayer = new Player(null, 3, 4);
        String expected = "null at (3, 4)";
        assertEquals(expected, nullPlayer.toString());
    }

    @Test
    void testToStringWithEmptyName() {
        Player emptyPlayer = new Player("", 7, 8);
        String expected = " at (7, 8)";
        assertEquals(expected, emptyPlayer.toString());
    }

    @Test
    void testPositionsWithLargeNumbers() {
        Player largePlayer = new Player("Large", 999, 1000);
        assertEquals(999, largePlayer.getRow());
        assertEquals(1000, largePlayer.getCol());
        assertEquals("Large at (999, 1000)", largePlayer.toString());
    }

    @Test
    void testNameChanges() {
        player.setName("FirstName");
        assertEquals("FirstName", player.getName());

        player.setName("SecondName");
        assertEquals("SecondName", player.getName());

        player.setName("");
        assertEquals("", player.getName());
    }

    @Test
    void testPositionChanges() {
        // Test changements de position multiples
        player.setRow(1);
        player.setCol(1);
        assertEquals(1, player.getRow());
        assertEquals(1, player.getCol());

        player.setRow(100);
        player.setCol(200);
        assertEquals(100, player.getRow());
        assertEquals(200, player.getCol());

        // Retour à zéro
        player.setRow(0);
        player.setCol(0);
        assertEquals(0, player.getRow());
        assertEquals(0, player.getCol());
    }

    @Test
    void testPlayerEquality() {
        // Test que deux joueurs avec même nom et position sont différents objets
        Player player1 = new Player("Same", 1, 1);
        Player player2 = new Player("Same", 1, 1);

        // Même valeurs mais objets différents
        assertEquals(player1.getName(), player2.getName());
        assertEquals(player1.getRow(), player2.getRow());
        assertEquals(player1.getCol(), player2.getCol());
        assertNotSame(player1, player2); // Différents objets
    }

    @Test
    void testLongPlayerName() {
        String longName = "A".repeat(100);
        Player longPlayer = new Player(longName, 5, 5);
        assertEquals(longName, longPlayer.getName());
        assertEquals(5, longPlayer.getRow());
        assertEquals(5, longPlayer.getCol());
    }

    @Test
    void testSpecialCharactersInName() {
        String specialName = "Player@#$%^&*()";
        Player specialPlayer = new Player(specialName, 2, 3);
        assertEquals(specialName, specialPlayer.getName());
        assertEquals("Player@#$%^&*() at (2, 3)", specialPlayer.toString());
    }

    @Test
    void testConstructorWithZeroPosition() {
        Player zeroPlayer = new Player("Zero", 0, 0);
        assertEquals("Zero", zeroPlayer.getName());
        assertEquals(0, zeroPlayer.getRow());
        assertEquals(0, zeroPlayer.getCol());
        assertEquals("Zero at (0, 0)", zeroPlayer.toString());
    }

    @Test
    void testStateConsistency() {
        // Vérifier que l'état reste cohérent après modifications
        String originalName = player.getName();
        int originalRow = player.getRow();
        int originalCol = player.getCol();

        // Modifications
        player.setName("Modified");
        player.setRow(99);
        player.setCol(88);

        // Vérifications
        assertEquals("Modified", player.getName());
        assertEquals(99, player.getRow());
        assertEquals(88, player.getCol());

        // Les valeurs ont bien changé
        assertNotEquals(originalName, player.getName());
        assertNotEquals(originalRow, player.getRow());
        assertNotEquals(originalCol, player.getCol());
    }

    // Note: Les tests de mouvement nécessitent un GameBoard
    // On pourrait les tester avec un GameBoard réel si on en crée un simple
    // ou attendre d'avoir une version de GameBoard pour les tests d'intégration
}