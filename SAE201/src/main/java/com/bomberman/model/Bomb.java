package com.bomberman.model;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class Bomb {

    private int row;
    private int col;
    private int explosionRange;
    private Timeline timer;
    private boolean hasExploded;
    private int owner; // ID du joueur propriétaire (1 ou 2)


    // Interface pour notifier l'explosion
    public interface ExplosionCallback {
        void onExplosion(Bomb bomb, List<Position> explosionCells);
    }

    // Classe pour représenter une position
    public static class Position {
        public final int row;
        public final int col;

        public Position(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Position position = (Position) obj;
            return row == position.row && col == position.col;
        }
    }

    public Bomb(int row, int col, int explosionRange) {
        this.row = row;
        this.col = col;
        this.explosionRange = explosionRange;
        this.hasExploded = false;
        this.owner = 0; // Pas de propriétaire par défaut

    }

    /**
     * Démarre le minuteur de la bombe
     */
    public void startTimer(ExplosionCallback callback, GameBoard gameBoard) {
        timer = new Timeline(new KeyFrame(Duration.seconds(2), e -> explode(callback, gameBoard)));
        timer.play();
    }

    /**
     * Fait exploser la bombe
     */
    private void explode(ExplosionCallback callback, GameBoard gameBoard) {
        if (hasExploded) return;

        hasExploded = true;
        List<Position> explosionCells = calculateExplosionArea(gameBoard);

        if (callback != null) {
            callback.onExplosion(this, explosionCells);
        }
    }

    /**
     * Calcule les cellules affectées par l'explosion
     */
    private List<Position> calculateExplosionArea(GameBoard gameBoard) {
        List<Position> explosionCells = new ArrayList<>();

        // Ajouter la position de la bombe
        explosionCells.add(new Position(row, col));

        // Explosion vers le haut
        for (int i = 1; i <= explosionRange; i++) {
            int newRow = row - i;
            if (!gameBoard.isInBounds(newRow, col)) {
                break; // Arrêter si on sort du plateau
            }

            GameBoard.CellType cellType = gameBoard.getCellType(newRow, col);

            // Si c'est un mur indestructible, on s'arrête SANS l'ajouter
            if (cellType == GameBoard.CellType.INDESTRUCTIBLE_WALL) {
                break;
            }

            // Ajouter la cellule à l'explosion
            explosionCells.add(new Position(newRow, col));

            // Si c'est un mur destructible, on s'arrête APRÈS l'avoir ajouté
            if (cellType == GameBoard.CellType.DESTRUCTIBLE_WALL) {
                break;
            }
        }

        // Explosion vers le bas
        for (int i = 1; i <= explosionRange; i++) {
            int newRow = row + i;
            if (!gameBoard.isInBounds(newRow, col)) {
                break;
            }

            GameBoard.CellType cellType = gameBoard.getCellType(newRow, col);

            if (cellType == GameBoard.CellType.INDESTRUCTIBLE_WALL) {
                break;
            }

            explosionCells.add(new Position(newRow, col));

            if (cellType == GameBoard.CellType.DESTRUCTIBLE_WALL) {
                break;
            }
        }

        // Explosion vers la gauche
        for (int i = 1; i <= explosionRange; i++) {
            int newCol = col - i;
            if (!gameBoard.isInBounds(row, newCol)) {
                break;
            }

            GameBoard.CellType cellType = gameBoard.getCellType(row, newCol);

            if (cellType == GameBoard.CellType.INDESTRUCTIBLE_WALL) {
                break;
            }

            explosionCells.add(new Position(row, newCol));

            if (cellType == GameBoard.CellType.DESTRUCTIBLE_WALL) {
                break;
            }
        }

        // Explosion vers la droite
        for (int i = 1; i <= explosionRange; i++) {
            int newCol = col + i;
            if (!gameBoard.isInBounds(row, newCol)) {
                break;
            }

            GameBoard.CellType cellType = gameBoard.getCellType(row, newCol);

            if (cellType == GameBoard.CellType.INDESTRUCTIBLE_WALL) {
                break;
            }

            explosionCells.add(new Position(row, newCol));

            if (cellType == GameBoard.CellType.DESTRUCTIBLE_WALL) {
                break;
            }
        }

        return explosionCells;
    }

    /**
     * Arrête le minuteur de la bombe
     */
    public void stopTimer() {
        if (timer != null) {
            timer.stop();
        }
    }


    // Getters et Setters

    public int getRow() { return row; }
    public int getCol() { return col; }
    public int getExplosionRange() { return explosionRange; }
    public boolean hasExploded() { return hasExploded; }

    public int getOwner() { return owner; }
    public void setOwner(int owner) { this.owner = owner; }


    @Override
    public String toString() {
        return "Bomb at (" + row + ", " + col + ") range:" + explosionRange;
    }
}