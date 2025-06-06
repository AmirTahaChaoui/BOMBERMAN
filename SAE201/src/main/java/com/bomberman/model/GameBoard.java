package com.bomberman.model;

public class GameBoard {

    public static final int BOARD_WIDTH = 15;
    public static final int BOARD_HEIGHT = 13;

    // Types de cellules
    public enum CellType {
        EMPTY,              // Passage libre
        INDESTRUCTIBLE_WALL, // Mur indestructible
        DESTRUCTIBLE_WALL   // Mur destructible
    }

    private CellType[][] board;

    public GameBoard() {
        initializeBoard();
    }

    private void initializeBoard() {
        board = new CellType[BOARD_HEIGHT][BOARD_WIDTH];

        // Remplir le plateau
        for (int row = 0; row < BOARD_HEIGHT; row++) {
            for (int col = 0; col < BOARD_WIDTH; col++) {
                // Placer les murs indestructibles en quadrillage classique Bomberman
                // Bordures TOUJOURS murs + quadrillage interne (lignes ET colonnes paires)
                if (row == 0 || row == BOARD_HEIGHT - 1 ||
                        col == 0 || col == BOARD_WIDTH - 1 ||
                        (row % 2 == 0 && col % 2 == 0)) {

                    board[row][col] = CellType.INDESTRUCTIBLE_WALL;
                } else {
                    board[row][col] = CellType.EMPTY;
                }
            }
        }

        // Ajouter des murs destructibles aléatoirement
        addDestructibleWalls();
    }

    private void addDestructibleWalls() {
        // Placer des murs destructibles sur environ 60% des cases vides
        // En évitant les positions de départ des joueurs
        for (int row = 1; row < BOARD_HEIGHT - 1; row++) {
            for (int col = 1; col < BOARD_WIDTH - 1; col++) {
                // Ne placer que sur les cases vides
                if (board[row][col] == CellType.EMPTY) {
                    // Éviter les positions de départ (coin supérieur gauche)
                    if ((row == 1 && col == 1) || (row == 1 && col == 2) || (row == 2 && col == 1)) {
                        continue; // Laisser libre pour le spawn du joueur
                    }

                    // 60% de chance de placer un mur destructible
                    if (Math.random() < 0.6) {
                        board[row][col] = CellType.DESTRUCTIBLE_WALL;
                    }
                }
            }
        }
    }

    /**
     * Vérifie si une position est valide et accessible
     */
    public boolean isValidMove(int row, int col) {
        return isInBounds(row, col) && board[row][col] == CellType.EMPTY;
    }

    /**
     * Vérifie si une position est dans les limites du plateau
     */
    public boolean isInBounds(int row, int col) {
        return row >= 0 && row < BOARD_HEIGHT && col >= 0 && col < BOARD_WIDTH;
    }

    /**
     * Retourne le type de cellule à une position donnée
     */
    public CellType getCellType(int row, int col) {
        if (!isInBounds(row, col)) {
            return CellType.INDESTRUCTIBLE_WALL; // Considérer hors limites comme un mur
        }
        return board[row][col];
    }

    /**
     * Définit le type de cellule à une position donnée
     */
    public void setCellType(int row, int col, CellType type) {
        if (isInBounds(row, col)) {
            board[row][col] = type;
        }
    }

    /**
     * Détruit un mur destructible à la position donnée
     * @return true si un mur a été détruit, false sinon
     */
    public boolean destroyWall(int row, int col) {
        if (isInBounds(row, col) && board[row][col] == CellType.DESTRUCTIBLE_WALL) {
            board[row][col] = CellType.EMPTY;
            return true;
        }
        return false;
    }

    // Getters
    public int getWidth() { return BOARD_WIDTH; }
    public int getHeight() { return BOARD_HEIGHT; }
    public CellType[][] getBoard() { return board; }
}