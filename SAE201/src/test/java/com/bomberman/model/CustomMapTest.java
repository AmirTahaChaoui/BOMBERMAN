package com.bomberman.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CustomMapTest {

    private CustomMap customMap;
    private int[][] testMatrix;

    @BeforeEach
    void setUp() {
        // Matrice de test 3x3
        testMatrix = new int[][] {
                {0, 1, 0},
                {2, 0, 2},
                {0, 1, 0}
        };

        customMap = new CustomMap(
                "TestMap",
                "Une carte de test",
                3,
                3,
                testMatrix,
                "TestAuthor"
        );
    }

    // === Tests des constructeurs ===

    @Test
    void testConstructorWithAutoDate() {
        CustomMap map = new CustomMap("Map1", "Description1", 2, 2, new int[2][2], "Author1");

        assertEquals("Map1", map.getName());
        assertEquals("Description1", map.getDescription());
        assertEquals(2, map.getWidth());
        assertEquals(2, map.getHeight());
        assertNotNull(map.getMatrix());
        assertEquals("Author1", map.getAuthor());
        assertNotNull(map.getCreated());
    }

    @Test
    void testConstructorWithManualDate() {
        String testDate = "2024-01-15T10:30:00";
        CustomMap map = new CustomMap("Map2", "Description2", 4, 4, new int[4][4], "Author2", testDate);

        assertEquals("Map2", map.getName());
        assertEquals("Description2", map.getDescription());
        assertEquals(4, map.getWidth());
        assertEquals(4, map.getHeight());
        assertEquals("Author2", map.getAuthor());
        assertEquals(testDate, map.getCreated());
    }

    @Test
    void testCopyConstructor() {
        CustomMap original = new CustomMap("Original", "Test", 2, 2, new int[][]{{1, 2}, {3, 4}}, "Author", "2024-01-01T00:00:00");
        CustomMap copy = new CustomMap(original);

        assertEquals(original.getName(), copy.getName());
        assertEquals(original.getDescription(), copy.getDescription());
        assertEquals(original.getWidth(), copy.getWidth());
        assertEquals(original.getHeight(), copy.getHeight());
        assertEquals(original.getAuthor(), copy.getAuthor());
        assertEquals(original.getCreated(), copy.getCreated());

        // Vérifier que c'est une copie profonde
        assertNotSame(original.getMatrix(), copy.getMatrix());
        assertArrayEquals(original.getMatrix()[0], copy.getMatrix()[0]);

        // Modifier l'original ne doit pas affecter la copie
        original.getMatrix()[0][0] = 999;
        assertNotEquals(original.getMatrix()[0][0], copy.getMatrix()[0][0]);
    }

    // === Tests des getters et setters ===

    @Test
    void testGettersAndSetters() {
        assertEquals("TestMap", customMap.getName());
        assertEquals("Une carte de test", customMap.getDescription());
        assertEquals(3, customMap.getWidth());
        assertEquals(3, customMap.getHeight());
        assertEquals("TestAuthor", customMap.getAuthor());
        assertNotNull(customMap.getCreated());

        customMap.setName("NewName");
        customMap.setDescription("NewDescription");
        customMap.setAuthor("NewAuthor");

        assertEquals("NewName", customMap.getName());
        assertEquals("NewDescription", customMap.getDescription());
        assertEquals("NewAuthor", customMap.getAuthor());
    }

    @Test
    void testSetMatrix() {
        int[][] newMatrix = {{1, 0}, {0, 1}};
        customMap.setMatrix(newMatrix);

        assertSame(newMatrix, customMap.getMatrix());
    }

    // === Tests des méthodes utilitaires ===

    @Test
    void testGetCellValue() {
        assertEquals(0, customMap.getCellValue(0, 0));
        assertEquals(1, customMap.getCellValue(0, 1));
        assertEquals(2, customMap.getCellValue(1, 0));
        assertEquals(0, customMap.getCellValue(1, 1));

        // Tests hors limites
        assertEquals(-1, customMap.getCellValue(-1, 0));
        assertEquals(-1, customMap.getCellValue(0, -1));
        assertEquals(-1, customMap.getCellValue(10, 0));
        assertEquals(-1, customMap.getCellValue(0, 10));
    }

    @Test
    void testSetCellValue() {
        customMap.setCellValue(0, 0, 5);
        assertEquals(5, customMap.getCellValue(0, 0));

        customMap.setCellValue(2, 2, 9);
        assertEquals(9, customMap.getCellValue(2, 2));

        // Tests hors limites (ne doit pas lever d'exception)
        assertDoesNotThrow(() -> {
            customMap.setCellValue(-1, 0, 1);
            customMap.setCellValue(0, -1, 1);
            customMap.setCellValue(10, 0, 1);
            customMap.setCellValue(0, 10, 1);
        });
    }

    @Test
    void testIsValid() {
        // Notre matrice de test a 3 zones de spawn (valeur 0)
        assertTrue(customMap.isValid());

        // Matrice sans zone de spawn
        int[][] noSpawnMatrix = {{1, 1}, {1, 1}};
        CustomMap noSpawnMap = new CustomMap("NoSpawn", "Test", 2, 2, noSpawnMatrix, "Author");
        assertFalse(noSpawnMap.isValid());

        // Matrice avec une seule zone de spawn
        int[][] oneSpawnMatrix = {{0, 1}, {1, 1}};
        CustomMap oneSpawnMap = new CustomMap("OneSpawn", "Test", 2, 2, oneSpawnMatrix, "Author");
        assertFalse(oneSpawnMap.isValid());

        // Matrice avec exactement 2 zones de spawn
        int[][] twoSpawnMatrix = {{0, 1}, {1, 0}};
        CustomMap twoSpawnMap = new CustomMap("TwoSpawn", "Test", 2, 2, twoSpawnMatrix, "Author");
        assertTrue(twoSpawnMap.isValid());
    }

    @Test
    void testGetMatrixCopy() {
        int[][] copy = customMap.getMatrixCopy();

        // Vérifier que c'est une copie
        assertNotSame(testMatrix, copy);
        assertArrayEquals(testMatrix[0], copy[0]);
        assertArrayEquals(testMatrix[1], copy[1]);
        assertArrayEquals(testMatrix[2], copy[2]);

        // Modifier la copie ne doit pas affecter l'original
        copy[0][0] = 999;
        assertNotEquals(customMap.getCellValue(0, 0), copy[0][0]);
    }

    @Test
    void testGetCellType() {
        assertEquals(GameBoard.CellType.EMPTY, customMap.getCellType(0, 0)); // 0
        assertEquals(GameBoard.CellType.INDESTRUCTIBLE_WALL, customMap.getCellType(0, 1)); // 1
        assertEquals(GameBoard.CellType.DESTRUCTIBLE_WALL, customMap.getCellType(1, 0)); // 2

        // Tester avec des bonus
        customMap.setCellValue(0, 0, 3);
        assertEquals(GameBoard.CellType.BOMB_BONUS, customMap.getCellType(0, 0));

        customMap.setCellValue(0, 0, 4);
        assertEquals(GameBoard.CellType.RANGE_BONUS, customMap.getCellType(0, 0));

        // Valeur invalide
        customMap.setCellValue(0, 0, 999);
        assertEquals(GameBoard.CellType.EMPTY, customMap.getCellType(0, 0));
    }

    // === Tests des méthodes toString, equals, hashCode ===

    @Test
    void testToString() {
        String result = customMap.toString();
        assertTrue(result.contains("TestMap"));
        assertTrue(result.contains("3x3"));
        assertTrue(result.contains("TestAuthor"));
        assertTrue(result.contains("created"));
    }

    @Test
    void testEquals() {
        CustomMap map1 = new CustomMap("SameName", "Desc1", 2, 2, new int[2][2], "Author1");
        CustomMap map2 = new CustomMap("SameName", "Desc2", 3, 3, new int[3][3], "Author2");
        CustomMap map3 = new CustomMap("DifferentName", "Desc1", 2, 2, new int[2][2], "Author1");

        assertEquals(map1, map2); // Même nom
        assertNotEquals(map1, map3); // Nom différent
        assertEquals(map1, map1); // Même référence
        assertNotEquals(map1, null);
        assertNotEquals(map1, "not a map");
    }

    @Test
    void testHashCode() {
        CustomMap map1 = new CustomMap("SameName", "Desc1", 2, 2, new int[2][2], "Author1");
        CustomMap map2 = new CustomMap("SameName", "Desc2", 3, 3, new int[3][3], "Author2");

        assertEquals(map1.hashCode(), map2.hashCode());
    }

    // === Tests de cas limites ===

    @Test
    void testConstructorWithNullValues() {
        CustomMap nullMap = new CustomMap(null, null, 1, 1, new int[1][1], null);

        assertNull(nullMap.getName());
        assertNull(nullMap.getDescription());
        assertNull(nullMap.getAuthor());
        assertEquals(1, nullMap.getWidth());
        assertEquals(1, nullMap.getHeight());
        assertNotNull(nullMap.getCreated());
    }

    @Test
    void testConstructorWithEmptyValues() {
        CustomMap emptyMap = new CustomMap("", "", 2, 2, new int[2][2], "");

        assertEquals("", emptyMap.getName());
        assertEquals("", emptyMap.getDescription());
        assertEquals("", emptyMap.getAuthor());
    }

    @Test
    void testConstructorWithZeroDimensions() {
        CustomMap zeroMap = new CustomMap("Zero", "Test", 0, 0, new int[0][0], "Author");

        assertEquals(0, zeroMap.getWidth());
        assertEquals(0, zeroMap.getHeight());
        assertNotNull(zeroMap.getMatrix());
    }

    @Test
    void testLargeMatrix() {
        int size = 100;
        int[][] largeMatrix = new int[size][size];

        // Remplir avec des valeurs alternées
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                largeMatrix[i][j] = (i + j) % 2;
            }
        }

        CustomMap largeMap = new CustomMap("Large", "Test", size, size, largeMatrix, "Author");

        assertEquals(size, largeMap.getWidth());
        assertEquals(size, largeMap.getHeight());
        assertEquals(0, largeMap.getCellValue(0, 0));
        assertEquals(1, largeMap.getCellValue(0, 1));
        assertTrue(largeMap.isValid()); // Beaucoup de zones de spawn
    }

    @Test
    void testMatrixWithNegativeValues() {
        int[][] negativeMatrix = {{-1, -2}, {-3, 0}};
        CustomMap negativeMap = new CustomMap("Negative", "Test", 2, 2, negativeMatrix, "Author");

        assertEquals(-1, negativeMap.getCellValue(0, 0));
        assertEquals(GameBoard.CellType.EMPTY, negativeMap.getCellType(0, 0)); // Valeur inconnue = EMPTY
        assertFalse(negativeMap.isValid()); // Une seule zone de spawn (0) - pas assez !

        // Test avec au moins 2 zones de spawn
        int[][] validNegativeMatrix = {{-1, 0}, {0, -2}};
        CustomMap validNegativeMap = new CustomMap("ValidNegative", "Test", 2, 2, validNegativeMatrix, "Author");
        assertTrue(validNegativeMap.isValid()); // Deux zones de spawn (0)
    }

    @Test
    void testDateFormat() {
        String created = customMap.getCreated();
        assertNotNull(created);
        assertTrue(created.length() > 10); // Format ISO contient au moins une date

        // Test avec date manuelle
        String manualDate = "2024-12-25T15:30:45";
        CustomMap dateMap = new CustomMap("Test", "Test", 1, 1, new int[1][1], "Author", manualDate);
        assertEquals(manualDate, dateMap.getCreated());
    }

    @Test
    void testSpecialCharactersInStrings() {
        String specialName = "Map@#$%^&*()";
        String specialDesc = "Description avec accents éàü";
        String specialAuthor = "Auteur-Spécial_123";

        CustomMap specialMap = new CustomMap(specialName, specialDesc, 2, 2, new int[2][2], specialAuthor);

        assertEquals(specialName, specialMap.getName());
        assertEquals(specialDesc, specialMap.getDescription());
        assertEquals(specialAuthor, specialMap.getAuthor());
    }

    @Test
    void testMatrixModificationAfterCopy() {
        int[][] originalMatrix = {{1, 2}, {3, 4}};
        CustomMap map = new CustomMap("Test", "Test", 2, 2, originalMatrix, "Author");

        // Modifier la matrice originale
        originalMatrix[0][0] = 999;

        // Vérifier que la map a été affectée (référence partagée)
        assertEquals(999, map.getCellValue(0, 0));

        // Mais getCellValue devrait gérer les dimensions correctement
        assertNotEquals(-1, map.getCellValue(0, 0));
    }

    @Test
    void testCopyConstructorWithNullMatrix() {
        CustomMap original = new CustomMap("Test", "Test", 2, 2, new int[2][2], "Author");
        original.setMatrix(null);

        // Le constructeur de copie devrait gérer le cas null
        assertThrows(NullPointerException.class, () -> {
            new CustomMap(original);
        });
    }
}