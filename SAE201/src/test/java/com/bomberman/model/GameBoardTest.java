package com.bomberman.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameBoardTest {

    private GameBoard gameBoard;
    private GameBoard customBoard;

    @BeforeEach
    void setUp() {
        gameBoard = new GameBoard(); // Plateau par défaut
        customBoard = new GameBoard(5, 5); // Plateau custom 5x5
    }

    // === Tests des constructeurs ===

    @Test
    void testDefaultConstructor() {
        GameBoard defaultBoard = new GameBoard();

        assertNotNull(defaultBoard.getBoard());
        assertEquals(GameBoard.BOARD_HEIGHT, defaultBoard.getBoard().length);
        assertEquals(GameBoard.BOARD_WIDTH, defaultBoard.getBoard()[0].length);
    }

    @Test
    void testCustomConstructor() {
        GameBoard custom = new GameBoard(10, 8);

        assertEquals(10, custom.getWidth());
        assertEquals(8, custom.getHeight());
        assertNotNull(custom.getBoard());

        // Vérifier que toutes les cellules sont initialisées à EMPTY
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 10; col++) {
                assertEquals(GameBoard.CellType.EMPTY, custom.getCellType(row, col));
            }
        }
    }

    @Test
    void testConstructorWithZeroDimensions() {
        GameBoard zeroBoard = new GameBoard(0, 0);

        assertEquals(0, zeroBoard.getWidth());
        assertEquals(0, zeroBoard.getHeight());
        assertNotNull(zeroBoard.getBoard());
    }

    @Test
    void testConstructorWithLargeDimensions() {
        GameBoard largeBoard = new GameBoard(100, 50);

        assertEquals(100, largeBoard.getWidth());
        assertEquals(50, largeBoard.getHeight());
        assertEquals(GameBoard.CellType.EMPTY, largeBoard.getCellType(5, 5));
    }

    // === Tests des constantes ===

    @Test
    void testConstants() {
        assertEquals(15, GameBoard.BOARD_WIDTH);
        assertEquals(13, GameBoard.BOARD_HEIGHT);
    }

    // === Tests de l'énumération CellType ===

    @Test
    void testCellTypeEnum() {
        GameBoard.CellType[] types = GameBoard.CellType.values();

        assertEquals(5, types.length);
        assertEquals(GameBoard.CellType.EMPTY, types[0]);
        assertEquals(GameBoard.CellType.INDESTRUCTIBLE_WALL, types[1]);
        assertEquals(GameBoard.CellType.DESTRUCTIBLE_WALL, types[2]);
        assertEquals(GameBoard.CellType.BOMB_BONUS, types[3]);
        assertEquals(GameBoard.CellType.RANGE_BONUS, types[4]);
    }

    // === Tests de isInBounds ===

    @Test
    void testIsInBoundsDefaultBoard() {
        // Positions valides
        assertTrue(gameBoard.isInBounds(0, 0));
        assertTrue(gameBoard.isInBounds(0, GameBoard.BOARD_WIDTH - 1));
        assertTrue(gameBoard.isInBounds(GameBoard.BOARD_HEIGHT - 1, 0));
        assertTrue(gameBoard.isInBounds(GameBoard.BOARD_HEIGHT - 1, GameBoard.BOARD_WIDTH - 1));
        assertTrue(gameBoard.isInBounds(5, 7));

        // Positions invalides
        assertFalse(gameBoard.isInBounds(-1, 0));
        assertFalse(gameBoard.isInBounds(0, -1));
        assertFalse(gameBoard.isInBounds(GameBoard.BOARD_HEIGHT, 0));
        assertFalse(gameBoard.isInBounds(0, GameBoard.BOARD_WIDTH));
        assertFalse(gameBoard.isInBounds(-1, -1));
        assertFalse(gameBoard.isInBounds(100, 100));
    }

    @Test
    void testIsInBoundsCustomBoard() {
        // Positions valides dans les limites statiques
        assertTrue(customBoard.isInBounds(0, 0));
        assertTrue(customBoard.isInBounds(4, 4));
        assertTrue(customBoard.isInBounds(2, 3));

        // Positions invalides
        assertFalse(customBoard.isInBounds(-1, 0));
        assertFalse(customBoard.isInBounds(0, -1));
        assertFalse(customBoard.isInBounds(GameBoard.BOARD_HEIGHT, 0));
        assertFalse(customBoard.isInBounds(0, GameBoard.BOARD_WIDTH));
    }

    // === Tests de getCellType et setCellType ===

    @Test
    void testGetCellTypeInBounds() {
        // Le plateau par défaut a des murs sur les bords
        assertEquals(GameBoard.CellType.INDESTRUCTIBLE_WALL, gameBoard.getCellType(0, 0));
        assertEquals(GameBoard.CellType.INDESTRUCTIBLE_WALL, gameBoard.getCellType(0, GameBoard.BOARD_WIDTH - 1));
        assertEquals(GameBoard.CellType.INDESTRUCTIBLE_WALL, gameBoard.getCellType(GameBoard.BOARD_HEIGHT - 1, 0));
    }

    @Test
    void testGetCellTypeOutOfBounds() {
        // Hors limites doit retourner INDESTRUCTIBLE_WALL
        assertEquals(GameBoard.CellType.INDESTRUCTIBLE_WALL, gameBoard.getCellType(-1, 0));
        assertEquals(GameBoard.CellType.INDESTRUCTIBLE_WALL, gameBoard.getCellType(0, -1));
        assertEquals(GameBoard.CellType.INDESTRUCTIBLE_WALL, gameBoard.getCellType(100, 100));
    }

    @Test
    void testSetCellTypeInBounds() {
        customBoard.setCellType(2, 2, GameBoard.CellType.DESTRUCTIBLE_WALL);
        assertEquals(GameBoard.CellType.DESTRUCTIBLE_WALL, customBoard.getCellType(2, 2));

        customBoard.setCellType(2, 2, GameBoard.CellType.BOMB_BONUS);
        assertEquals(GameBoard.CellType.BOMB_BONUS, customBoard.getCellType(2, 2));

        customBoard.setCellType(2, 2, GameBoard.CellType.RANGE_BONUS);
        assertEquals(GameBoard.CellType.RANGE_BONUS, customBoard.getCellType(2, 2));
    }

    @Test
    void testSetCellTypeWithLimits() {
        // Test avec des coordonnées négatives
        assertDoesNotThrow(() -> {
            customBoard.setCellType(-1, 0, GameBoard.CellType.BOMB_BONUS);
            customBoard.setCellType(0, -1, GameBoard.CellType.RANGE_BONUS);
        });

        // Les positions hors limites négatives ne sont pas modifiées
        assertEquals(GameBoard.CellType.INDESTRUCTIBLE_WALL, customBoard.getCellType(-1, 0));
    }

    // === Tests de isValidMove ===

    @Test
    void testIsValidMoveEmptyCell() {
        customBoard.setCellType(2, 2, GameBoard.CellType.EMPTY);
        assertTrue(customBoard.isValidMove(2, 2));
    }

    @Test
    void testIsValidMoveBonusCells() {
        customBoard.setCellType(2, 2, GameBoard.CellType.BOMB_BONUS);
        assertTrue(customBoard.isValidMove(2, 2));

        customBoard.setCellType(3, 3, GameBoard.CellType.RANGE_BONUS);
        assertTrue(customBoard.isValidMove(3, 3));
    }

    @Test
    void testIsValidMoveWalls() {
        customBoard.setCellType(2, 2, GameBoard.CellType.INDESTRUCTIBLE_WALL);
        assertFalse(customBoard.isValidMove(2, 2));

        customBoard.setCellType(3, 3, GameBoard.CellType.DESTRUCTIBLE_WALL);
        assertFalse(customBoard.isValidMove(3, 3));
    }

    @Test
    void testIsValidMoveBasicLimits() {
        assertFalse(customBoard.isValidMove(-1, 0));
        assertFalse(customBoard.isValidMove(0, -1));
    }

    // === Tests de destroyWall ===

    @Test
    void testDestroyWallSuccess() {
        customBoard.setCellType(2, 2, GameBoard.CellType.DESTRUCTIBLE_WALL);

        boolean destroyed = customBoard.destroyWall(2, 2);
        assertTrue(destroyed);

        GameBoard.CellType newType = customBoard.getCellType(2, 2);
        // Peut être EMPTY, BOMB_BONUS ou RANGE_BONUS
        assertTrue(newType == GameBoard.CellType.EMPTY ||
                newType == GameBoard.CellType.BOMB_BONUS ||
                newType == GameBoard.CellType.RANGE_BONUS);
    }

    @Test
    void testDestroyWallIndestructible() {
        customBoard.setCellType(2, 2, GameBoard.CellType.INDESTRUCTIBLE_WALL);

        boolean destroyed = customBoard.destroyWall(2, 2);
        assertFalse(destroyed);
        assertEquals(GameBoard.CellType.INDESTRUCTIBLE_WALL, customBoard.getCellType(2, 2));
    }

    @Test
    void testDestroyWallEmpty() {
        customBoard.setCellType(2, 2, GameBoard.CellType.EMPTY);

        boolean destroyed = customBoard.destroyWall(2, 2);
        assertFalse(destroyed);
        assertEquals(GameBoard.CellType.EMPTY, customBoard.getCellType(2, 2));
    }

    @Test
    void testDestroyWallNegativeCoordinates() {
        boolean destroyed = customBoard.destroyWall(-1, 0);
        assertFalse(destroyed);
    }

    @Test
    void testDestroyWallBonusGeneration() {
        // Test statistique : détruire plusieurs murs et vérifier la génération de bonus
        int totalWalls = 100;
        int bonusCount = 0;

        for (int i = 0; i < totalWalls; i++) {
            GameBoard testBoard = new GameBoard(10, 10);
            testBoard.setCellType(5, 5, GameBoard.CellType.DESTRUCTIBLE_WALL);
            testBoard.destroyWall(5, 5);

            GameBoard.CellType result = testBoard.getCellType(5, 5);
            if (result == GameBoard.CellType.BOMB_BONUS || result == GameBoard.CellType.RANGE_BONUS) {
                bonusCount++;
            }
        }

        // Environ 30% des murs détruits devraient générer un bonus
        // On accepte une marge d'erreur (15-45%)
        assertTrue(bonusCount >= 15 && bonusCount <= 45,
                "Bonus generation rate: " + bonusCount + "% (expected ~30%)");
    }

    // === Tests de l'initialisation du plateau par défaut ===

    @Test
    void testDefaultBoardBorders() {
        // Vérifier que les bordures sont des murs indestructibles
        for (int col = 0; col < GameBoard.BOARD_WIDTH; col++) {
            assertEquals(GameBoard.CellType.INDESTRUCTIBLE_WALL, gameBoard.getCellType(0, col));
            assertEquals(GameBoard.CellType.INDESTRUCTIBLE_WALL, gameBoard.getCellType(GameBoard.BOARD_HEIGHT - 1, col));
        }

        for (int row = 0; row < GameBoard.BOARD_HEIGHT; row++) {
            assertEquals(GameBoard.CellType.INDESTRUCTIBLE_WALL, gameBoard.getCellType(row, 0));
            assertEquals(GameBoard.CellType.INDESTRUCTIBLE_WALL, gameBoard.getCellType(row, GameBoard.BOARD_WIDTH - 1));
        }
    }

    @Test
    void testDefaultBoardInternalWalls() {
        // Vérifier le quadrillage interne (lignes ET colonnes paires)
        for (int row = 2; row < GameBoard.BOARD_HEIGHT - 1; row += 2) {
            for (int col = 2; col < GameBoard.BOARD_WIDTH - 1; col += 2) {
                assertEquals(GameBoard.CellType.INDESTRUCTIBLE_WALL, gameBoard.getCellType(row, col));
            }
        }
    }

    @Test
    void testDefaultBoardSpawnAreas() {
        // Vérifier que les zones de spawn ont un contenu approprié
        // Zone joueur 1 (coin supérieur gauche)
        for (int row = 1; row <= 2; row++) {
            for (int col = 1; col <= 2; col++) {
                if (gameBoard.getCellType(row, col) == GameBoard.CellType.INDESTRUCTIBLE_WALL) {
                    continue; // Ignorer les murs du quadrillage
                }
                // Les autres cellules peuvent être vides ou murs destructibles
                GameBoard.CellType cellType = gameBoard.getCellType(row, col);
                assertTrue(cellType == GameBoard.CellType.EMPTY ||
                        cellType == GameBoard.CellType.DESTRUCTIBLE_WALL);
            }
        }

        // Zone joueur 2 (coin inférieur droit)
        for (int row = GameBoard.BOARD_HEIGHT - 3; row < GameBoard.BOARD_HEIGHT - 1; row++) {
            for (int col = GameBoard.BOARD_WIDTH - 3; col < GameBoard.BOARD_WIDTH - 1; col++) {
                if (gameBoard.getCellType(row, col) == GameBoard.CellType.INDESTRUCTIBLE_WALL) {
                    continue; // Ignorer les murs du quadrillage
                }
                GameBoard.CellType cellType = gameBoard.getCellType(row, col);
                assertTrue(cellType == GameBoard.CellType.EMPTY ||
                        cellType == GameBoard.CellType.DESTRUCTIBLE_WALL);
            }
        }
    }

    // === Tests des getters ===

    @Test
    void testGetters() {
        assertNotNull(gameBoard.getBoard());

        assertEquals(5, customBoard.getWidth());
        assertEquals(5, customBoard.getHeight());
        assertNotNull(customBoard.getBoard());
    }

    @Test
    void testGetBoardArray() {
        GameBoard.CellType[][] board = customBoard.getBoard();
        assertNotNull(board);
        assertEquals(5, board.length); // height
        assertEquals(5, board[0].length); // width

        // Vérifier que c'est bien la référence du plateau interne
        customBoard.setCellType(1, 1, GameBoard.CellType.BOMB_BONUS);
        assertEquals(GameBoard.CellType.BOMB_BONUS, board[1][1]);
    }

    // === Tests de cas limites ===

    @Test
    void testAllCellTypesSetAndGet() {
        for (GameBoard.CellType type : GameBoard.CellType.values()) {
            customBoard.setCellType(2, 2, type);
            assertEquals(type, customBoard.getCellType(2, 2));
        }
    }

    @Test
    void testBoardIntegrity() {
        // Vérifier que modifier une cellule n'affecte pas les autres
        customBoard.setCellType(1, 1, GameBoard.CellType.BOMB_BONUS);
        customBoard.setCellType(2, 2, GameBoard.CellType.RANGE_BONUS);
        customBoard.setCellType(3, 3, GameBoard.CellType.DESTRUCTIBLE_WALL);

        assertEquals(GameBoard.CellType.BOMB_BONUS, customBoard.getCellType(1, 1));
        assertEquals(GameBoard.CellType.RANGE_BONUS, customBoard.getCellType(2, 2));
        assertEquals(GameBoard.CellType.DESTRUCTIBLE_WALL, customBoard.getCellType(3, 3));
        assertEquals(GameBoard.CellType.EMPTY, customBoard.getCellType(4, 4)); // Non modifiée
    }

    @Test
    void testMultipleDestroyWallSamePosition() {
        customBoard.setCellType(2, 2, GameBoard.CellType.DESTRUCTIBLE_WALL);

        assertTrue(customBoard.destroyWall(2, 2)); // Première destruction
        assertFalse(customBoard.destroyWall(2, 2)); // Deuxième tentative sur case non-destructible
    }

    @Test
    void testRandomnessInBoardGeneration() {
        // Créer plusieurs plateaux et vérifier qu'ils sont différents
        GameBoard board1 = new GameBoard();
        GameBoard board2 = new GameBoard();

        boolean foundDifference = false;

        for (int row = 1; row < GameBoard.BOARD_HEIGHT - 1 && !foundDifference; row++) {
            for (int col = 1; col < GameBoard.BOARD_WIDTH - 1 && !foundDifference; col++) {
                if (board1.getCellType(row, col) != board2.getCellType(row, col)) {
                    foundDifference = true;
                }
            }
        }

        // Il devrait y avoir des différences dues au placement aléatoire des murs
        assertTrue(foundDifference, "Les plateaux générés devraient être différents");
    }
}