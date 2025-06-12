package com.bomberman.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FlagTest {

    private Flag redFlag;
    private Flag blueFlag;

    @BeforeEach
    void setUp() {
        redFlag = new Flag(5, 10, Flag.Team.RED);
        blueFlag = new Flag(2, 3, Flag.Team.BLUE);
    }

    // === Tests de base sans dépendances externes ===

    @Test
    void testConstructor() {
        Flag newFlag = new Flag(7, 8, Flag.Team.RED);

        assertEquals(7, newFlag.getRow());
        assertEquals(8, newFlag.getCol());
        assertEquals(7, newFlag.getOriginalRow());
        assertEquals(8, newFlag.getOriginalCol());
        assertEquals(Flag.Team.RED, newFlag.getTeam());
        assertFalse(newFlag.isCaptured());
        assertFalse(newFlag.isDropped());
    }

    @Test
    void testConstructorBlueTeam() {
        Flag newBlueFlag = new Flag(1, 2, Flag.Team.BLUE);

        assertEquals(1, newBlueFlag.getRow());
        assertEquals(2, newBlueFlag.getCol());
        assertEquals(Flag.Team.BLUE, newBlueFlag.getTeam());
        assertFalse(newBlueFlag.isCaptured());
        assertFalse(newBlueFlag.isDropped());
    }

    @Test
    void testConstructorWithZeroCoordinates() {
        Flag zeroFlag = new Flag(0, 0, Flag.Team.RED);

        assertEquals(0, zeroFlag.getRow());
        assertEquals(0, zeroFlag.getCol());
        assertEquals(0, zeroFlag.getOriginalRow());
        assertEquals(0, zeroFlag.getOriginalCol());
    }

    @Test
    void testConstructorWithNegativeCoordinates() {
        Flag negativeFlag = new Flag(-1, -2, Flag.Team.BLUE);

        assertEquals(-1, negativeFlag.getRow());
        assertEquals(-2, negativeFlag.getCol());
        assertEquals(-1, negativeFlag.getOriginalRow());
        assertEquals(-2, negativeFlag.getOriginalCol());
    }

    // === Tests des getters et setters ===

    @Test
    void testGettersInitialState() {
        assertEquals(5, redFlag.getRow());
        assertEquals(10, redFlag.getCol());
        assertEquals(5, redFlag.getOriginalRow());
        assertEquals(10, redFlag.getOriginalCol());
        assertEquals(Flag.Team.RED, redFlag.getTeam());
        assertFalse(redFlag.isCaptured());
        assertFalse(redFlag.isDropped());
    }

    @Test
    void testSetCaptured() {
        assertFalse(redFlag.isCaptured());

        redFlag.setCaptured(true);
        assertTrue(redFlag.isCaptured());

        redFlag.setCaptured(false);
        assertFalse(redFlag.isCaptured());
    }

    @Test
    void testSetDropped() {
        assertFalse(redFlag.isDropped());

        redFlag.setDropped(true);
        assertTrue(redFlag.isDropped());

        redFlag.setDropped(false);
        assertFalse(redFlag.isDropped());
    }

    @Test
    void testSetRowAndCol() {
        redFlag.setRow(15);
        redFlag.setCol(20);

        assertEquals(15, redFlag.getRow());
        assertEquals(20, redFlag.getCol());

        // La position originale ne change pas
        assertEquals(5, redFlag.getOriginalRow());
        assertEquals(10, redFlag.getOriginalCol());
    }

    // === Tests de la logique métier ===

    @Test
    void testDrop() {
        redFlag.drop(12, 15);

        assertEquals(12, redFlag.getRow());
        assertEquals(15, redFlag.getCol());
        assertFalse(redFlag.isCaptured());
        assertTrue(redFlag.isDropped());

        // La position originale ne change pas
        assertEquals(5, redFlag.getOriginalRow());
        assertEquals(10, redFlag.getOriginalCol());
    }

    @Test
    void testDropWithZeroCoordinates() {
        redFlag.drop(0, 0);

        assertEquals(0, redFlag.getRow());
        assertEquals(0, redFlag.getCol());
        assertFalse(redFlag.isCaptured());
        assertTrue(redFlag.isDropped());
    }

    @Test
    void testDropWithNegativeCoordinates() {
        redFlag.drop(-5, -3);

        assertEquals(-5, redFlag.getRow());
        assertEquals(-3, redFlag.getCol());
        assertFalse(redFlag.isCaptured());
        assertTrue(redFlag.isDropped());
    }

    @Test
    void testDropWhenAlreadyCaptured() {
        redFlag.setCaptured(true);
        assertTrue(redFlag.isCaptured());

        redFlag.drop(8, 9);

        assertEquals(8, redFlag.getRow());
        assertEquals(9, redFlag.getCol());
        assertFalse(redFlag.isCaptured()); // Plus capturé après drop
        assertTrue(redFlag.isDropped());
    }

    @Test
    void testReset() {
        // Modifier l'état du drapeau
        redFlag.drop(20, 25);
        redFlag.setCaptured(true);

        // Vérifier l'état modifié
        assertEquals(20, redFlag.getRow());
        assertEquals(25, redFlag.getCol());
        assertTrue(redFlag.isCaptured());
        assertTrue(redFlag.isDropped());

        // Reset
        redFlag.reset();

        // Vérifier le retour à l'état initial
        assertEquals(5, redFlag.getRow());
        assertEquals(10, redFlag.getCol());
        assertFalse(redFlag.isCaptured());
        assertFalse(redFlag.isDropped());

        // Position originale inchangée
        assertEquals(5, redFlag.getOriginalRow());
        assertEquals(10, redFlag.getOriginalCol());
    }

    @Test
    void testResetWhenNotMoved() {
        // Reset sans avoir bougé le drapeau
        redFlag.reset();

        assertEquals(5, redFlag.getRow());
        assertEquals(10, redFlag.getCol());
        assertFalse(redFlag.isCaptured());
        assertFalse(redFlag.isDropped());
    }

    // === Tests de l'énumération Team ===

    @Test
    void testTeamEnum() {
        Flag.Team[] teams = Flag.Team.values();

        assertEquals(2, teams.length);
        assertEquals(Flag.Team.RED, teams[0]);
        assertEquals(Flag.Team.BLUE, teams[1]);
    }

    @Test
    void testTeamComparison() {
        assertEquals(Flag.Team.RED, redFlag.getTeam());
        assertEquals(Flag.Team.BLUE, blueFlag.getTeam());

        assertNotEquals(redFlag.getTeam(), blueFlag.getTeam());
    }

    // === Tests de scénarios complexes ===

    @Test
    void testCaptureAndDropScenario() {
        // Scénario : capture -> déplacement -> drop -> reset

        // 1. Capturer le drapeau
        redFlag.setCaptured(true);
        assertTrue(redFlag.isCaptured());
        assertFalse(redFlag.isDropped());

        // 2. Le déplacer
        redFlag.setRow(8);
        redFlag.setCol(12);
        assertEquals(8, redFlag.getRow());
        assertEquals(12, redFlag.getCol());

        // 3. Le déposer ailleurs
        redFlag.drop(15, 18);
        assertEquals(15, redFlag.getRow());
        assertEquals(18, redFlag.getCol());
        assertFalse(redFlag.isCaptured());
        assertTrue(redFlag.isDropped());

        // 4. Reset complet
        redFlag.reset();
        assertEquals(5, redFlag.getRow());
        assertEquals(10, redFlag.getCol());
        assertFalse(redFlag.isCaptured());
        assertFalse(redFlag.isDropped());
    }

    @Test
    void testMultipleDrops() {
        // Déposer plusieurs fois le drapeau
        redFlag.drop(1, 1);
        assertEquals(1, redFlag.getRow());
        assertEquals(1, redFlag.getCol());
        assertTrue(redFlag.isDropped());

        redFlag.drop(2, 2);
        assertEquals(2, redFlag.getRow());
        assertEquals(2, redFlag.getCol());
        assertTrue(redFlag.isDropped());

        redFlag.drop(3, 3);
        assertEquals(3, redFlag.getRow());
        assertEquals(3, redFlag.getCol());
        assertTrue(redFlag.isDropped());
    }

    @Test
    void testStateIndependence() {
        // Vérifier que captured et dropped sont indépendants (via setters)
        redFlag.setCaptured(true);
        redFlag.setDropped(true);

        assertTrue(redFlag.isCaptured());
        assertTrue(redFlag.isDropped());

        redFlag.setCaptured(false);
        assertFalse(redFlag.isCaptured());
        assertTrue(redFlag.isDropped()); // Reste dropped

        redFlag.setCaptured(true);
        redFlag.setDropped(false);
        assertTrue(redFlag.isCaptured());
        assertFalse(redFlag.isDropped());
    }

    // === Tests de cas limites ===

    @Test
    void testLargeCoordinates() {
        Flag largeFlag = new Flag(1000, 2000, Flag.Team.RED);

        assertEquals(1000, largeFlag.getRow());
        assertEquals(2000, largeFlag.getCol());

        largeFlag.drop(5000, 10000);
        assertEquals(5000, largeFlag.getRow());
        assertEquals(10000, largeFlag.getCol());

        largeFlag.reset();
        assertEquals(1000, largeFlag.getRow());
        assertEquals(2000, largeFlag.getCol());
    }

    @Test
    void testSamePositionOperations() {
        // Drop à la même position que l'original
        redFlag.drop(5, 10);

        assertEquals(5, redFlag.getRow());
        assertEquals(10, redFlag.getCol());
        assertTrue(redFlag.isDropped());
        assertFalse(redFlag.isCaptured());

        // Reset devrait quand même remettre les flags à false
        redFlag.reset();
        assertFalse(redFlag.isDropped());
        assertFalse(redFlag.isCaptured());
    }

    @Test
    void testOriginalPositionImmutability() {
        int originalRow = redFlag.getOriginalRow();
        int originalCol = redFlag.getOriginalCol();

        // Diverses opérations
        redFlag.setRow(999);
        redFlag.setCol(888);
        redFlag.drop(777, 666);
        redFlag.setCaptured(true);
        redFlag.setDropped(true);

        // La position originale ne doit jamais changer
        assertEquals(originalRow, redFlag.getOriginalRow());
        assertEquals(originalCol, redFlag.getOriginalCol());

        redFlag.reset();
        assertEquals(originalRow, redFlag.getOriginalRow());
        assertEquals(originalCol, redFlag.getOriginalCol());
    }

    @Test
    void testBothTeamsIndependence() {
        // Vérifier que les deux drapeaux sont indépendants
        redFlag.setCaptured(true);
        redFlag.drop(99, 88);

        // Le drapeau bleu ne doit pas être affecté
        assertEquals(2, blueFlag.getRow());
        assertEquals(3, blueFlag.getCol());
        assertFalse(blueFlag.isCaptured());
        assertFalse(blueFlag.isDropped());
        assertEquals(Flag.Team.BLUE, blueFlag.getTeam());

        blueFlag.setCaptured(true);

        // Le drapeau rouge doit garder son état modifié
        // Après drop(), captured devient false
        assertFalse(redFlag.isCaptured()); // drop() met captured à false
        assertTrue(redFlag.isDropped());
        assertEquals(99, redFlag.getRow());
        assertEquals(88, redFlag.getCol());
    }
}