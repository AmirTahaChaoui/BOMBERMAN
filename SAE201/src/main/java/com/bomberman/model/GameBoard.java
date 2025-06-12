package com.bomberman.model;

public class GameBoard {

    public static final int BOARD_WIDTH = 15;
    public static final int BOARD_HEIGHT = 13;

    private int width;
    private int height;

    /**
     * Constructeur pour créer un GameBoard avec des dimensions personnalisées
     * (pour les maps custom)
     */
    /**
     * Constructeur pour créer un GameBoard avec des dimensions personnalisées
     * (pour les maps custom)
     */
    public GameBoard(int width, int height) {
        this.width = width;  // ← CORRECTION : utiliser les paramètres
        this.height = height; // ← CORRECTION : utiliser les paramètres
        this.board = new CellType[height][width];

        // Initialiser avec des cellules vides
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                board[row][col] = CellType.EMPTY;
            }
        }
    }


    // Types de cellules
    public enum CellType {
        EMPTY,              // Passage libre
        INDESTRUCTIBLE_WALL, // Mur indestructible
        DESTRUCTIBLE_WALL,   // Mur destructible
        BOMB_BONUS,         // Bonus nombre de bombes
        RANGE_BONUS         // Bonus portée d'explosion

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

                    // Éviter les positions de départ du joueur 1 (coin supérieur gauche)
                    if ((row <= 2 && col <= 2)) {
                        continue; // Laisser libre pour le spawn du joueur 1
                    }

                    // Éviter les positions de départ du joueur 2 (coin inférieur droit)
                    if ((row >= BOARD_HEIGHT - 3 && col >= BOARD_WIDTH - 3)) {
                        continue; // Laisser libre pour le spawn du joueur 2

                    }

                    // 60% de chance de placer un mur destructible
                    if (Math.random() < 0.7) {
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
        if (!isInBounds(row, col)) {
            return false;
        }

        CellType cellType = board[row][col];
        return cellType == CellType.EMPTY ||
                cellType == CellType.BOMB_BONUS ||
                cellType == CellType.RANGE_BONUS;

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

     * Détruit un mur destructible à la position donnée et peut générer un bonus
     * @return true si un mur a été détruit, false sinon
     */
    public boolean destroyWall(int row, int col) {
        if (isInBounds(row, col) && board[row][col] == CellType.DESTRUCTIBLE_WALL) {

            // Générer un bonus aléatoirement
            double random = Math.random();

            if (random < 0.15) {
                // 15% chance de bonus nombre de bombes
                board[row][col] = CellType.BOMB_BONUS;
            } else if (random < 0.30) {
                // 15% chance de bonus portée (0.15 + 0.15 = 0.30)
                board[row][col] = CellType.RANGE_BONUS;
            } else {
                // 70% chance de case vide
                board[row][col] = CellType.EMPTY;
            }


            return true;
        }
        return false;
    }

    // Getters
    public int getWidth() {
        return width; // Au lieu de BOARD_WIDTH si map custom
    }

    public int getHeight() {
        return height; // Au lieu de BOARD_HEIGHT si map custom
    }
    public CellType[][] getBoard() { return board; }
}