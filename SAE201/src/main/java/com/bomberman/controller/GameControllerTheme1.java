package com.bomberman.controller;

import com.bomberman.model.GameBoard;
import com.bomberman.model.Player;
import com.bomberman.model.Bomb;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class GameControllerTheme1 implements Initializable {

    @FXML
    private StackPane gameArea;

    @FXML
    private GridPane gameGrid;

    @FXML
    private Button startButton;

    @FXML
    private Button pauseButton;

    @FXML
    private Button resetButton;

    @FXML
    private Label scoreLabel;

    private boolean gameStarted = false;
    private boolean gamePaused = false;

    // Modèle du jeu
    private GameBoard gameBoard;
    private Player player;
    private List<Bomb> activeBombs;

    // Représentation visuelle
    private Circle playerSprite;
    private Map<Bomb, Circle> bombSprites;
    private List<Rectangle> explosionSprites;

    // État du jeu
    private boolean playerAlive;

    // Taille des cellules en pixels
    private static final int CELL_SIZE = 30;
    private static final int BOMB_EXPLOSION_RANGE = 2;


    private Image wallImage;
    private Image blockImage;
    private Image floorImage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        wallImage = new Image(getClass().getResource("/images/images_theme1/wall.png").toExternalForm());
        blockImage = new Image(getClass().getResource("/images/images_theme1/block.png").toExternalForm());
        floorImage = new Image(getClass().getResource("/images/images_theme1/floor.png").toExternalForm());

        System.out.println("GameController initialisé");
        initializeGameArea();
        setupKeyboardControls();
    }

    private void initializeGameArea() {
        // Créer le plateau de jeu
        gameBoard = new GameBoard();

        // Créer le joueur à une position de départ (coin supérieur gauche libre)
        player = new Player("Player 1", 1, 1);
        playerAlive = true;

        // Initialiser les listes
        activeBombs = new ArrayList<>();
        bombSprites = new HashMap<>();
        explosionSprites = new ArrayList<>();

        // Vider la grille actuelle
        gameGrid.getChildren().clear();

        // Créer la représentation visuelle du plateau
        createVisualBoard();

        // Créer et placer le joueur
        createPlayerSprite();

        System.out.println("Plateau de jeu " + gameBoard.getWidth() + "x" + gameBoard.getHeight() + " créé");
        System.out.println("Joueur créé : " + player);
    }

    private void createVisualBoard() {
        for (int row = 0; row < gameBoard.getHeight(); row++) {
            for (int col = 0; col < gameBoard.getWidth(); col++) {
                Rectangle cell = createCell(row, col);
                gameGrid.add(cell, col, row);
            }
        }
    }

    private Rectangle createCell(int row, int col) {
        Rectangle cell = new Rectangle(CELL_SIZE, CELL_SIZE);

        GameBoard.CellType cellType = gameBoard.getCellType(row, col);
        switch (cellType) {
            case EMPTY:
                cell.setFill(new ImagePattern(floorImage));
                break;
            case INDESTRUCTIBLE_WALL:
                // Appliquer l'image comme fond
                cell.setFill(new ImagePattern(wallImage));
                break;
            case DESTRUCTIBLE_WALL:
                //cell.getStyleClass().add("wall");
                cell.setFill(new ImagePattern(blockImage));
                break;
        }

        return cell;
    }

    private void createPlayerSprite() {
        // Créer le sprite du joueur (cercle rouge)
        playerSprite = new Circle(CELL_SIZE / 3.0);
        playerSprite.getStyleClass().add("player");

        // Placer le joueur sur la grille
        gameGrid.add(playerSprite, player.getCol(), player.getRow());

        GridPane.setHalignment(playerSprite, HPos.CENTER);
        GridPane.setValignment(playerSprite, VPos.CENTER);

    }

    private void setupKeyboardControls() {
        // Rendre la scène focusable pour capturer les événements clavier
        gameArea.setFocusTraversable(true);
        gameArea.setOnKeyPressed(this::handleKeyPress);

        // Demander le focus
        gameArea.requestFocus();
    }

    @FXML
    private void handleKeyPress(KeyEvent event) {
        if (!gameStarted || !playerAlive) return; // Ne rien faire si le jeu n'a pas commencé ou si le joueur est mort

        boolean moved = false;

        switch (event.getCode()) {
            case Z: // Haut
                moved = player.moveUp(gameBoard);
                if (moved) System.out.println("Joueur monte : " + player);
                break;
            case S: // Bas
                moved = player.moveDown(gameBoard);
                if (moved) System.out.println("Joueur descend : " + player);
                break;
            case Q: // Gauche
                moved = player.moveLeft(gameBoard);
                if (moved) System.out.println("Joueur va à gauche : " + player);
                break;
            case D: // Droite
                moved = player.moveRight(gameBoard);
                if (moved) System.out.println("Joueur va à droite : " + player);
                break;
            case SPACE: // Placer une bombe
                placeBomb();
                break;
            default:
                break;
        }

        // Mettre à jour la position visuelle si le joueur a bougé
        if (moved) {
            updatePlayerPosition();
        }

        // Conserver le focus
        gameArea.requestFocus();
    }

    private void updatePlayerPosition() {
        // Supprimer le joueur de sa position actuelle
        gameGrid.getChildren().remove(playerSprite);

        // Le replacer à sa nouvelle position
        gameGrid.add(playerSprite, player.getCol(), player.getRow());
    }

    private void placeBomb() {
        // Vérifier qu'il n'y a pas déjà une bombe à cette position
        for (Bomb bomb : activeBombs) {
            if (bomb.getRow() == player.getRow() && bomb.getCol() == player.getCol()) {
                System.out.println("Il y a déjà une bombe ici !");
                return;
            }
        }

        // Créer une nouvelle bombe
        Bomb bomb = new Bomb(player.getRow(), player.getCol(), BOMB_EXPLOSION_RANGE);
        activeBombs.add(bomb);

        // Créer le sprite visuel de la bombe
        Circle bombSprite = new Circle(CELL_SIZE / 4.0);
        bombSprite.getStyleClass().add("bomb");
        bombSprites.put(bomb, bombSprite);

        // Placer la bombe sur la grille
        gameGrid.add(bombSprite, bomb.getCol(), bomb.getRow());

        GridPane.setHalignment(bombSprite, HPos.CENTER);
        GridPane.setValignment(bombSprite, VPos.CENTER);

        // Démarrer le minuteur
        bomb.startTimer(this::onBombExplosion, gameBoard);

        System.out.println("Bombe placée : " + bomb);
    }

    private void onBombExplosion(Bomb bomb, List<Bomb.Position> explosionCells) {
        System.out.println("💥 EXPLOSION ! " + bomb);

        // Supprimer le sprite de la bombe
        Circle bombSprite = bombSprites.get(bomb);
        if (bombSprite != null) {
            gameGrid.getChildren().remove(bombSprite);
            bombSprites.remove(bomb);
        }

        // Détruire les murs destructibles dans la zone d'explosion
        destroyWallsInExplosion(explosionCells);

        // Créer l'animation d'explosion
        createExplosionAnimation(explosionCells);

        // Vérifier si le joueur est touché
        checkPlayerInExplosion(explosionCells);

        // Retirer la bombe de la liste active
        activeBombs.remove(bomb);
    }

    private void destroyWallsInExplosion(List<Bomb.Position> explosionCells) {
        boolean needsUpdate = false;

        for (Bomb.Position pos : explosionCells) {
            boolean wallDestroyed = gameBoard.destroyWall(pos.row, pos.col);
            if (wallDestroyed) {
                System.out.println("🧱 Mur détruit en (" + pos.row + ", " + pos.col + ")");
                needsUpdate = true;
            }
        }

        // Mettre à jour seulement si des murs ont été détruits
        if (needsUpdate) {
            updateBoardDisplay();
        }
    }

    private void updateBoardDisplay() {
        // Supprimer SEULEMENT les rectangles qui représentent les cellules du plateau
        // en gardant tous les autres éléments (joueur, bombes, explosions)
        gameGrid.getChildren().removeIf(node -> {
            if (node instanceof Rectangle) {
                Rectangle rect = (Rectangle) node;
                // Supprimer si c'est une cellule du plateau (pas une explosion)
                // Les cellules du plateau ont une taille de CELL_SIZE
                return rect.getWidth() == CELL_SIZE && rect.getHeight() == CELL_SIZE &&
                        !explosionSprites.contains(rect);
            }
            return false;
        });

        // Recréer seulement les cellules du plateau
        for (int row = 0; row < gameBoard.getHeight(); row++) {
            for (int col = 0; col < gameBoard.getWidth(); col++) {
                Rectangle cell = createCell(row, col);
                gameGrid.add(cell, col, row);
            }
        }

        // S'assurer que le joueur reste au premier plan
        if (playerSprite != null) {
            playerSprite.toFront();
        }

        // S'assurer que les bombes restent au premier plan
        for (Circle bombSprite : bombSprites.values()) {
            bombSprite.toFront();
        }
    }

    private void createExplosionAnimation(List<Bomb.Position> explosionCells) {
        // Créer les sprites d'explosion
        for (Bomb.Position pos : explosionCells) {
            Rectangle explosionSprite = new Rectangle(CELL_SIZE * 0.8, CELL_SIZE * 0.8);
            explosionSprite.getStyleClass().add("explosion");explosionSprites.add(explosionSprite);

            // Placer l'explosion sur la grille
            gameGrid.add(explosionSprite, pos.col, pos.row);

            GridPane.setHalignment(explosionSprite, HPos.CENTER);
            GridPane.setValignment(explosionSprite, VPos.CENTER);
        }

        // Programmer la suppression de l'explosion après 1 seconde
        Timeline explosionTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            for (Rectangle sprite : explosionSprites) {
                gameGrid.getChildren().remove(sprite);
            }
            explosionSprites.clear();
        }));
        explosionTimer.play();
    }

    private void checkPlayerInExplosion(List<Bomb.Position> explosionCells) {
        Bomb.Position playerPos = new Bomb.Position(player.getRow(), player.getCol());

        for (Bomb.Position explosionPos : explosionCells) {
            if (explosionPos.equals(playerPos)) {
                // Le joueur est touché !
                playerDied();
                return;
            }
        }
    }

    private void playerDied() {
        playerAlive = false;
        System.out.println("💀 LE JOUEUR EST MORT !");

        // Changer la couleur du joueur pour indiquer qu'il est mort
        playerSprite.setFill(Color.GRAY);

        // Arrêter le jeu
        gamePaused = true;
        startButton.setText("GAME OVER");
        startButton.setDisable(true);

        // Arrêter toutes les bombes actives
        for (Bomb bomb : activeBombs) {
            bomb.stopTimer();
        }
    }

    @FXML
    private void startGame() {
        if (!gameStarted) {
            System.out.println("Démarrage du jeu...");
            gameStarted = true;
            startButton.setText("Reprendre");
            pauseButton.setDisable(false);

            // Donner le focus pour les contrôles clavier
            gameArea.requestFocus();
        } else if (gamePaused) {
            System.out.println("Reprise du jeu...");
            gamePaused = false;
            startButton.setText("Reprendre");
            pauseButton.setText("Pause");

            // Redonner le focus
            gameArea.requestFocus();
        }
    }

    @FXML
    private void pauseGame() {
        if (gameStarted && !gamePaused) {
            System.out.println("Pause du jeu...");
            gamePaused = true;
            pauseButton.setText("Reprendre");
            startButton.setText("Reprendre");
        }
    }

    @FXML
    private void resetGame() {
        System.out.println("Reset du jeu...");
        gameStarted = false;
        gamePaused = false;
        startButton.setText("Démarrer");
        startButton.setDisable(false);
        pauseButton.setText("Pause");
        pauseButton.setDisable(true);
        scoreLabel.setText("0");

        // Arrêter toutes les bombes actives
        for (Bomb bomb : activeBombs) {
            bomb.stopTimer();
        }

        // Réinitialiser le plateau de jeu
        initializeGameArea();
    }

    /**
     * Méthode utilitaire pour vérifier si un déplacement est possible
     * (utilisable plus tard pour le joueur)
     */
    public boolean canMoveTo(int row, int col) {
        return gameBoard != null && gameBoard.isValidMove(row, col);
    }
}