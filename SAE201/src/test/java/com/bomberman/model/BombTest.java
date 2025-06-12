package com.bomberman.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BombTest {

    private Bomb bomb;

    @BeforeEach
    void setUp() {
        bomb = new Bomb(5, 5, 2);
    }

    // === Tests de base sans dépendances externes ===

    @Test
    void testConstructor() {
        Bomb newBomb = new Bomb(3, 7, 4);

        assertEquals(3, newBomb.getRow());
        assertEquals(7, newBomb.getCol());
        assertEquals(4, newBomb.getExplosionRange());
        assertFalse(newBomb.hasExploded());
        assertEquals(0, newBomb.getOwner());
    }

    @Test
    void testGettersAndSetters() {
        assertEquals(5, bomb.getRow());
        assertEquals(5, bomb.getCol());
        assertEquals(2, bomb.getExplosionRange());

        bomb.setOwner(1);
        assertEquals(1, bomb.getOwner());

        bomb.setOwner(2);
        assertEquals(2, bomb.getOwner());
    }

    @Test
    void testInitialState() {
        assertFalse(bomb.hasExploded());
        assertEquals(0, bomb.getOwner());
    }

    @Test
    void testPositionEquality() {
        Bomb.Position pos1 = new Bomb.Position(3, 4);
        Bomb.Position pos2 = new Bomb.Position(3, 4);
        Bomb.Position pos3 = new Bomb.Position(3, 5);

        assertEquals(pos1, pos2);
        assertNotEquals(pos1, pos3);
        assertEquals(pos1, pos1);
        assertNotEquals(pos1, null);
        assertNotEquals(pos1, "not a position");
    }

    @Test
    void testToString() {
        String expected = "Bomb at (5, 5) range:2";
        assertEquals(expected, bomb.toString());

        Bomb customBomb = new Bomb(10, 15, 3);
        String expectedCustom = "Bomb at (10, 15) range:3";
        assertEquals(expectedCustom, customBomb.toString());
    }

    @Test
    void testConstructorWithZeroRange() {
        Bomb zeroBomb = new Bomb(0, 0, 0);
        assertEquals(0, zeroBomb.getExplosionRange());
        assertEquals(0, zeroBomb.getRow());
        assertEquals(0, zeroBomb.getCol());
    }

    @Test
    void testOwnerManagement() {
        // Test valeurs limites
        bomb.setOwner(-1);
        assertEquals(-1, bomb.getOwner());

        bomb.setOwner(999);
        assertEquals(999, bomb.getOwner());

        // Reset
        bomb.setOwner(0);
        assertEquals(0, bomb.getOwner());
    }
}