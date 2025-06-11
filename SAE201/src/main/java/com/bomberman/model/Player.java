package com.bomberman.model;

import java.util.List;

public class Player {

    private int row;
    private int col;
    private String name;

    public Player(String name, int startRow, int startCol) {
        this.name = name;
        this.row = startRow;
        this.col = startCol;
    }

    /**
     * Tente de déplacer le joueur vers une nouvelle position
     * @param gameBoard Le plateau de jeu pour vérifier les collisions
     * @param newRow Nouvelle ligne
     * @param newCol Nouvelle colonne
     * @return true si le déplacement a réussi, false sinon
     */
    public boolean moveTo(GameBoard gameBoard, int newRow, int newCol) {
        if (gameBoard.isValidMove(newRow, newCol)) {
            this.row = newRow;
            this.col = newCol;
            return true;
        }
        return false;
    }

    /**
     * Déplace le joueur vers le haut
     */
    public boolean moveUp(GameBoard gameBoard) {
        return moveTo(gameBoard, row - 1, col);
    }

    /**
     * Déplace le joueur vers le bas
     */
    public boolean moveDown(GameBoard gameBoard) {
        return moveTo(gameBoard, row + 1, col);
    }

    /**
     * Déplace le joueur vers la gauche
     */
    public boolean moveLeft(GameBoard gameBoard) {
        return moveTo(gameBoard, row, col - 1);
    }

    /**
     * Déplace le joueur vers la droite
     */
    public boolean moveRight(GameBoard gameBoard) {
        return moveTo(gameBoard, row, col + 1);
    }

    private boolean hasBombAt(List<Bomb> activeBombs, int row, int col) {
        for (Bomb bomb : activeBombs) {
            if (bomb.getRow() == row && bomb.getCol() == col) {
                return true;
            }
        }
        return false;
    }


    // Getters et Setters
    public int getRow() { return row; }
    public int getCol() { return col; }
    public String getName() { return name; }

    public void setRow(int row) { this.row = row; }
    public void setCol(int col) { this.col = col; }
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() {
        return name + " at (" + row + ", " + col + ")";
    }

}