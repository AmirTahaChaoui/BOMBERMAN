package com.bomberman.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CustomMap {
    private String name;
    private String description;
    private int width;
    private int height;
    private int[][] matrix;
    private String author;
    private String created;

    /**
     * Crée une carte personnalisée avec toutes les informations, y compris la date de création.
     *
     * @param name        Nom de la carte
     * @param description Description de la carte
     * @param width       Largeur (nombre de colonnes)
     * @param height      Hauteur (nombre de lignes)
     * @param matrix      Matrice représentant les cellules de la carte
     * @param author      Auteur de la carte
     * @param created     Date de création de la carte (format ISO 8601)
     */
    public CustomMap(String name, String description, int width, int height, int[][] matrix, String author, String created) {
        this.name = name;
        this.description = description;
        this.width = width;
        this.height = height;
        this.matrix = matrix;
        this.author = author;
        this.created = created;
    }

    /**
     * Crée une nouvelle carte personnalisée avec la date de création automatiquement générée.
     *
     * @param name        Nom de la carte
     * @param description Description de la carte
     * @param width       Largeur de la carte
     * @param height      Hauteur de la carte
     * @param matrix      Matrice représentant la carte
     * @param author      Auteur de la carte
     */
    public CustomMap(String name, String description, int width, int height, int[][] matrix, String author) {
        this.name = name;
        this.description = description;
        this.width = width;
        this.height = height;
        this.matrix = matrix;
        this.author = author;
        this.created = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /**
     * Construit une nouvelle carte à partir d'une autre (copie profonde de la matrice).
     *
     * @param other Carte à copier
     */
    public CustomMap(CustomMap other) {
        this.name = other.name;
        this.description = other.description;
        this.width = other.width;
        this.height = other.height;
        this.author = other.author;
        this.created = other.created;

        // Copie profonde de la matrice
        this.matrix = new int[other.height][other.width];
        for (int i = 0; i < other.height; i++) {
            System.arraycopy(other.matrix[i], 0, this.matrix[i], 0, other.width);
        }
    }

    // === GETTERS ===

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int[][] getMatrix() {
        return matrix;
    }

    public String getAuthor() {
        return author;
    }

    public String getCreated() {
        return created;
    }

    // === SETTERS ===

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setMatrix(int[][] matrix) {
        this.matrix = matrix;
    }

    // === MÉTHODES UTILITAIRES ===

    /**
     * Obtient la valeur d'une cellule
     */
    public int getCellValue(int row, int col) {
        // Utiliser les vraies dimensions de la matrice
        int realHeight = matrix.length;
        int realWidth = matrix.length > 0 ? matrix[0].length : 0;

        if (row >= 0 && row < realHeight && col >= 0 && col < realWidth) {
            return matrix[row][col];
        }
        return -1; // Valeur invalide
    }

    /**
     * Définit la valeur d'une cellule
     */
    public void setCellValue(int row, int col, int value) {
        if (row >= 0 && row < height && col >= 0 && col < width) {
            matrix[row][col] = value;
        }
    }

    /**
     * Vérifie si la map est valide (au moins 2 zones de spawn)
     */
    public boolean isValid() {
        int spawnZones = 0;

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (matrix[i][j] == 0) { // Zone de spawn
                    spawnZones++;
                }
            }
        }

        return spawnZones >= 2; // Au moins 2 zones pour les 2 joueurs
    }

    /**
     * Retourne une copie de la matrice
     */
    public int[][] getMatrixCopy() {
        int[][] copy = new int[height][width];
        for (int i = 0; i < height; i++) {
            System.arraycopy(matrix[i], 0, copy[i], 0, width);
        }
        return copy;
    }

    /**
     * Convertit les valeurs de matrice en CellType de GameBoard
     */
    public GameBoard.CellType getCellType(int row, int col) {
        int value = getCellValue(row, col);
        switch (value) {
            case 0: return GameBoard.CellType.EMPTY;           // Zone de spawn
            case 1: return GameBoard.CellType.INDESTRUCTIBLE_WALL;
            case 2: return GameBoard.CellType.DESTRUCTIBLE_WALL;
            case 3: return GameBoard.CellType.BOMB_BONUS;
            case 4: return GameBoard.CellType.RANGE_BONUS;
            default: return GameBoard.CellType.EMPTY;
        }
    }

    @Override
    public String toString() {
        return "CustomMap{" +
                "name='" + name + '\'' +
                ", size=" + width + "x" + height +
                ", author='" + author + '\'' +
                ", created='" + created + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        CustomMap other = (CustomMap) obj;
        return name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * Convertit cette CustomMap en GameBoard
     */
    public GameBoard toGameBoard() {
        // Cette méthode doit convertir la matrice de la CustomMap
        // en un GameBoard avec les bonnes dimensions
        GameBoard board = new GameBoard(this.width, this.height);

        for (int row = 0; row < this.height; row++) {
            for (int col = 0; col < this.width; col++) {
                // Copier les cellules de la matrice vers le GameBoard
                GameBoard.CellType cellType = convertCellType(matrix[row][col]);
                board.setCellType(row, col, cellType);
            }
        }

        return board;
    }

    /**
     * Convertit une valeur entière de cellule en type de cellule {@link GameBoard.CellType}.
     *
     * @param cellValue Valeur entière représentant un type de cellule
     * @return Type de cellule correspondant à la valeur entière
     */
    private GameBoard.CellType convertCellType(int cellValue) {
        switch (cellValue) {
            case 0:  // 0 = Case vide
                return GameBoard.CellType.EMPTY;

            case 1:  // 1 = Mur indestructible
                return GameBoard.CellType.INDESTRUCTIBLE_WALL;

            case 2:  // 2 = Mur destructible
                return GameBoard.CellType.DESTRUCTIBLE_WALL;

            case 3:  // 3 = Bonus bombes (si tu en as)
                return GameBoard.CellType.BOMB_BONUS;

            case 4:  // 4 = Bonus portée (si tu en as)
                return GameBoard.CellType.RANGE_BONUS;

            default: // Valeur inconnue = case vide
                return GameBoard.CellType.EMPTY;
        }
    }

    /**
     * Convertit une valeur entière en CellType
     */
    private GameBoard.CellType convertIntToCellType(int value) {
        switch (value) {
            case 0: return GameBoard.CellType.EMPTY;
            case 1: return GameBoard.CellType.INDESTRUCTIBLE_WALL;
            case 2: return GameBoard.CellType.DESTRUCTIBLE_WALL;
            case 3: return GameBoard.CellType.BOMB_BONUS;
            case 4: return GameBoard.CellType.RANGE_BONUS;
            default:
                return GameBoard.CellType.EMPTY;
        }
    }

    /**
     * Crée une CustomMap à partir d'un GameBoard
     */
    public static CustomMap fromGameBoard(GameBoard gameBoard, String name, String description, String author) {
        int width = gameBoard.getWidth();
        int height = gameBoard.getHeight();
        int[][] matrix = new int[height][width];

        // Convertir le GameBoard en matrice
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                GameBoard.CellType cellType = gameBoard.getCellType(row, col);
                matrix[row][col] = cellTypeToInt(cellType);
            }
        }

        return new CustomMap(name, description, width, height, matrix, author);
    }

    /**
     * Convertit un CellType en valeur entière
     */
    private static int cellTypeToInt(GameBoard.CellType cellType) {
        switch (cellType) {
            case EMPTY: return 0;
            case INDESTRUCTIBLE_WALL: return 1;
            case DESTRUCTIBLE_WALL: return 2;
            case BOMB_BONUS: return 3;
            case RANGE_BONUS: return 4;
            default: return 0;
        }
    }
}