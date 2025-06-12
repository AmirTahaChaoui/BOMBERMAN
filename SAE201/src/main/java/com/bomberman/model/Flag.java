package com.bomberman.model;

public class Flag {
    public enum Team {
        RED, BLUE
    }

    private int originalRow;
    private int originalCol;
    private int currentRow;
    private int currentCol;
    private Team team;
    private boolean captured;
    private boolean dropped;


    /**
     * Crée un drapeau appartenant à une équipe, placé à une position initiale.
     *
     * @param row Ligne d'origine
     * @param col Colonne d'origine
     * @param team Équipe du drapeau (rouge ou bleue)
     */
    public Flag(int row, int col, Team team) {
        this.originalRow = row;
        this.originalCol = col;
        this.currentRow = row;
        this.currentCol = col;
        this.team = team;
        this.captured = false;
        this.dropped = false;
    }

    /**
     * Dépose le drapeau à une nouvelle position sur le plateau.
     * Le drapeau n'est plus capturé mais devient "déposé".
     *
     * @param row Nouvelle ligne où le drapeau est déposé
     * @param col Nouvelle colonne où le drapeau est déposé
     */
    public void drop(int row, int col) {
        this.currentRow = row;
        this.currentCol = col;
        this.captured = false;
        this.dropped = true;
    }

    /**
     * Réinitialise le drapeau à sa position d'origine.
     * Le drapeau n'est plus capturé ni déposé.
     */
    public void reset() {
        this.currentRow = originalRow;
        this.currentCol = originalCol;
        this.captured = false;
        this.dropped = false;
    }

    // Getters and setters
    public int getRow() { return currentRow; }
    public int getCol() { return currentCol; }
    public int getOriginalRow() { return originalRow; }
    public int getOriginalCol() { return originalCol; }
    public Team getTeam() { return team; }
    public boolean isCaptured() { return captured; }
    public boolean isDropped() { return dropped; }

    public void setCaptured(boolean captured) { this.captured = captured; }
    public void setDropped(boolean dropped) { this.dropped = dropped; }
    public void setRow(int row) { this.currentRow = row; }
    public void setCol(int col) { this.currentCol = col; }
}