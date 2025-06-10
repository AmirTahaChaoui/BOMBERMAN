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

    // Constructeur complet
    public CustomMap(String name, String description, int width, int height, int[][] matrix, String author, String created) {
        this.name = name;
        this.description = description;
        this.width = width;
        this.height = height;
        this.matrix = matrix;
        this.author = author;
        this.created = created;
    }

    // Constructeur pour nouvelle map
    public CustomMap(String name, String description, int width, int height, int[][] matrix, String author) {
        this.name = name;
        this.description = description;
        this.width = width;
        this.height = height;
        this.matrix = matrix;
        this.author = author;
        this.created = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    // Constructeur de copie
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

        System.out.println("⚠️ Accès hors limites dans getCellValue: (" + row + "," + col + ") " +
                "max=(" + realHeight + "," + realWidth + ")");
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
     * Affiche la map en console (debug)
     */
    public void printMatrix() {
        System.out.println("=== MAP: " + name + " ===");
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println("========================");
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
        // CORRECTION : Utiliser les vraies dimensions de la matrice
        int realHeight = matrix.length;
        int realWidth = matrix.length > 0 ? matrix[0].length : 0;

        System.out.println("🔍 CustomMap.toGameBoard():");
        System.out.println("   - Dimensions déclarées: " + width + "x" + height);
        System.out.println("   - Dimensions réelles: " + realWidth + "x" + realHeight);

        GameBoard gameBoard = new GameBoard(realWidth, realHeight);

        // Copier la matrice vers le GameBoard en utilisant les VRAIES dimensions
        for (int row = 0; row < realHeight; row++) {
            for (int col = 0; col < realWidth; col++) {
                // Accès direct à la matrice au lieu d'utiliser getCellType()
                int cellValue = matrix[row][col];
                GameBoard.CellType cellType = convertIntToCellType(cellValue);
                gameBoard.setCellType(row, col, cellType);
            }
        }

        return gameBoard;
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
                System.out.println("⚠️ Valeur inconnue dans la matrice : " + value + " -> EMPTY");
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