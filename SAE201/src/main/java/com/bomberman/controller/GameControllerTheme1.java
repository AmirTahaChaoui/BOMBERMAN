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

    private Player player1;
    private Player player2;
    private List<Bomb> activeBombs;

    // Représentation visuelle
    private Circle player1Sprite;
    private Circle player2Sprite;
    private Map<Bomb, Circle> bombSprites;
    private List<Rectangle> explosionSprites;

    // État des joueurs
    private boolean player1Alive;
    private boolean player2Alive;
    private int player1BombsActive;
    private int player2BombsActive;

    // Statistiques individuelles des joueurs
    private int player1ExplosionRange;
    private int player2ExplosionRange;
    private int player1MaxBombs;
    private int player2MaxBombs;

    // Taille des cellules en pixels
    private static final int CELL_SIZE = 50;
    private static final int DEFAULT_EXPLOSION_RANGE = 1;
    private static final int DEFAULT_MAX_BOMBS = 1; // Au début, on peut poser qu'une seule bombe

    // Images perso 1
    private Image persoUp;
    private Image persoDown;
    private Image persoLeft;
    private Image persoRight;
    private Image persoDeath;

    // Images perso 2
    private Image perso2Up;
    private Image perso2Down;
    private Image perso2Left;
    private Image perso2Right;
    private Image perso2Death;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Init des images perso 1 :
        persoDown = new Image(getClass().getResource("/images/persoDown.png").toExternalForm());
        persoLeft = new Image(getClass().getResource("/images/persoLeft.png").toExternalForm());
        persoRight = new Image(getClass().getResource("/images/persoRight.png").toExternalForm());
        persoUp = new Image(getClass().getResource("/images/persoUp.png").toExternalForm());
        persoDeath = new Image(getClass().getResource("/images/death.png").toExternalForm());

        // Init des images perso 2 :
        perso2Down = new Image(getClass().getResource("/images/perso2Down.png").toExternalForm());
        perso2Left = new Image(getClass().getResource("/images/perso2Left.png").toExternalForm());
        perso2Right = new Image(getClass().getResource("/images/perso2Right.png").toExternalForm());
        perso2Up = new Image(getClass().getResource("/images/perso2Up.png").toExternalForm());
        perso2Death = new Image(getClass().getResource("/images/death2.png").toExternalForm());

        System.out.println("GameController initialisé");
        initializeGameArea();
        setupKeyboardControls();
    }

    private void initializeGameArea() {
        // Créer le plateau de jeu
        gameBoard = new GameBoard();

        // Créer les joueurs aux coins opposés
        player1 = new Player("Player 1", 1, 1);
        player2 = new Player("Player 2", gameBoard.getHeight() - 2, gameBoard.getWidth() - 2);

        // État des joueurs
        player1Alive = true;
        player2Alive = true;
        player1BombsActive = 0;
        player2BombsActive = 0;

        // Statistiques initiales des joueurs
        player1ExplosionRange = DEFAULT_EXPLOSION_RANGE;
        player2ExplosionRange = DEFAULT_EXPLOSION_RANGE;
        player1MaxBombs = DEFAULT_MAX_BOMBS;
        player2MaxBombs = DEFAULT_MAX_BOMBS;

        // Initialiser les listes
        activeBombs = new ArrayList<>();
        bombSprites = new HashMap<>();
        explosionSprites = new ArrayList<>();

        // Vider la grille actuelle
        gameGrid.getChildren().clear();

        // Créer la représentation visuelle du plateau
        createVisualBoard();

        // Créer et placer les joueurs
        createPlayersSprites();

        System.out.println("Plateau de jeu " + gameBoard.getWidth() + "x" + gameBoard.getHeight() + " créé");
        System.out.println("Joueur 1 créé : " + player1 + " (Max bombes: " + player1MaxBombs + ")");
        System.out.println("Joueur 2 créé : " + player2 + " (Max bombes: " + player2MaxBombs + ")");
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

        // Définir le style selon le type de cellule
        GameBoard.CellType cellType = gameBoard.getCellType(row, col);
        switch (cellType) {
            case EMPTY:
                cell.getStyleClass().add("floor");
                break;
            case INDESTRUCTIBLE_WALL:
                cell.getStyleClass().add("wall");
                break;
            case DESTRUCTIBLE_WALL:
                cell.getStyleClass().add("destructible-wall");
                break;
            case BOMB_BONUS:
                cell.getStyleClass().add("bomb-bonus");
                break;
            case RANGE_BONUS:
                cell.getStyleClass().add("range-bonus");
                break;
        }

        return cell;
    }

    private void createPlayersSprites() {
        // Créer le sprite du joueur 1 (cercle rouge)
        player1Sprite = new Circle(CELL_SIZE / 2.1);
        //player1Sprite.getStyleClass().add("player1");
        player1Sprite.setFill(new ImagePattern(persoDown));

        // Créer le sprite du joueur 2 (cercle bleu)
        player2Sprite = new Circle(CELL_SIZE / 2.1);
        //player2Sprite.getStyleClass().add("player2");
        player2Sprite.setFill(new ImagePattern(perso2Down));

        // Placer les joueurs sur la grille
        gameGrid.add(player1Sprite, player1.getCol(), player1.getRow());
        gameGrid.add(player2Sprite, player2.getCol(), player2.getRow());
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

        if (!gameStarted || (!player1Alive && !player2Alive)) return;

        // Contrôles du joueur 1 (ZQSD + ESPACE)
        if (player1Alive) {
            switch (event.getCode()) {
                case Z: // Haut
                    if (player1.moveUp(gameBoard)) {
                        System.out.println("Joueur 1 monte : " + player1);
                        player1Sprite.setFill(new ImagePattern(persoUp));
                        updatePlayer1Position();
                    }
                    break;
                case S: // Bas
                    if (player1.moveDown(gameBoard)) {
                        System.out.println("Joueur 1 descend : " + player1);
                        player1Sprite.setFill(new ImagePattern(persoDown));
                        updatePlayer1Position();
                    }
                    break;
                case Q: // Gauche
                    if (player1.moveLeft(gameBoard)) {
                        System.out.println("Joueur 1 va à gauche : " + player1);
                        player1Sprite.setFill(new ImagePattern(persoLeft));
                        updatePlayer1Position();
                    }
                    break;
                case D: // Droite
                    if (player1.moveRight(gameBoard)) {
                        System.out.println("Joueur 1 va à droite : " + player1);
                        player1Sprite.setFill(new ImagePattern(persoRight));
                        updatePlayer1Position();
                    }
                    break;
                case SPACE: // Placer une bombe
                    placeBomb(1);
                    break;
            }
        }

        // Contrôles du joueur 2 (OKML + SHIFT)
        if (player2Alive) {
            switch (event.getCode()) {
                case O: // Haut
                    if (player2.moveUp(gameBoard)) {
                        System.out.println("Joueur 2 monte : " + player2);
                        player2Sprite.setFill(new ImagePattern(perso2Up));
                        updatePlayer2Position();
                    }
                    break;
                case L: // Bas
                    if (player2.moveDown(gameBoard)) {
                        System.out.println("Joueur 2 descend : " + player2);
                        player2Sprite.setFill(new ImagePattern(perso2Down));
                        updatePlayer2Position();
                    }
                    break;
                case K: // Gauche
                    if (player2.moveLeft(gameBoard)) {
                        System.out.println("Joueur 2 va à gauche : " + player2);
                        player2Sprite.setFill(new ImagePattern(perso2Left));
                        updatePlayer2Position();
                    }
                    break;
                case M: // Droite
                    if (player2.moveRight(gameBoard)) {
                        System.out.println("Joueur 2 va à droite : " + player2);
                        player2Sprite.setFill(new ImagePattern(perso2Right));
                        updatePlayer2Position();
                    }
                    break;
                case SHIFT: // Placer une bombe
                    placeBomb(2);
                    break;
            }
        }

        // Conserver le focus
        gameArea.requestFocus();
    }

    private void updatePlayer1Position() {
        gameGrid.getChildren().remove(player1Sprite);
        gameGrid.add(player1Sprite, player1.getCol(), player1.getRow());

        // Vérifier et collecter les bonus
        checkBonusCollection(1);
    }

    private void updatePlayer2Position() {
        gameGrid.getChildren().remove(player2Sprite);
        gameGrid.add(player2Sprite, player2.getCol(), player2.getRow());

        // Vérifier et collecter les bonus
        checkBonusCollection(2);
    }

    private void checkBonusCollection(int playerNumber) {
        Player currentPlayer = (playerNumber == 1) ? player1 : player2;
        GameBoard.CellType cellType = gameBoard.getCellType(currentPlayer.getRow(), currentPlayer.getCol());

        if (cellType == GameBoard.CellType.BOMB_BONUS) {
            // Collecter bonus nombre de bombes
            if (playerNumber == 1) {
                player1MaxBombs++;
                System.out.println("💣 Joueur 1 collecte un bonus bombes ! Nouvelles bombes max: " + player1MaxBombs);
            } else {
                player2MaxBombs++;
                System.out.println("💣 Joueur 2 collecte un bonus bombes ! Nouvelles bombes max: " + player2MaxBombs);
            }

            // Supprimer le bonus de la carte
            gameBoard.setCellType(currentPlayer.getRow(), currentPlayer.getCol(), GameBoard.CellType.EMPTY);
            updateBoardDisplay();

        } else if (cellType == GameBoard.CellType.RANGE_BONUS) {
            // Collecter bonus portée
            if (playerNumber == 1) {
                player1ExplosionRange++;
                System.out.println("🔥 Joueur 1 collecte un bonus portée ! Nouvelle portée: " + player1ExplosionRange);
            } else {
                player2ExplosionRange++;
                System.out.println("🔥 Joueur 2 collecte un bonus portée ! Nouvelle portée: " + player2ExplosionRange);
            }

            // Supprimer le bonus de la carte
            gameBoard.setCellType(currentPlayer.getRow(), currentPlayer.getCol(), GameBoard.CellType.EMPTY);
            updateBoardDisplay();
        }
    }

    private void placeBomb(int playerNumber) {
        Player currentPlayer;
        int playerExplosionRange;
        int playerMaxBombs;

        if (playerNumber == 1) {
            currentPlayer = player1;
            playerExplosionRange = player1ExplosionRange;
            playerMaxBombs = player1MaxBombs;
        } else {
            currentPlayer = player2;
            playerExplosionRange = player2ExplosionRange;
            playerMaxBombs = player2MaxBombs;
        }

        // Compter le nombre de bombes actives pour ce joueur
        int activeBombsCount = 0;
        for (Bomb bomb : activeBombs) {
            if (bomb.getOwner() == playerNumber) {
                activeBombsCount++;
            }
        }

        // Vérifier si le joueur a atteint sa limite de bombes
        if (activeBombsCount >= playerMaxBombs) {
            System.out.println("❌ Joueur " + playerNumber + " : Limite de bombes atteinte (" + activeBombsCount + "/" + playerMaxBombs + ") !");
            return;
        }

        // Vérifier qu'il n'y a pas déjà une bombe à cette position
        for (Bomb bomb : activeBombs) {
            if (bomb.getRow() == currentPlayer.getRow() && bomb.getCol() == currentPlayer.getCol()) {
                System.out.println("❌ Il y a déjà une bombe ici !");
                return;
            }
        }

        // Créer une nouvelle bombe
        Bomb bomb = new Bomb(currentPlayer.getRow(), currentPlayer.getCol(), playerExplosionRange);
        bomb.setOwner(playerNumber);
        activeBombs.add(bomb);

        // Créer le sprite visuel de la bombe
        Circle bombSprite = new Circle(CELL_SIZE / 4.0);
        if (playerNumber == 1) {
            bombSprite.getStyleClass().add("bomb1");
        } else {
            bombSprite.getStyleClass().add("bomb2");
        }

        bombSprites.put(bomb, bombSprite);

        // Placer la bombe sur la grille
        gameGrid.add(bombSprite, bomb.getCol(), bomb.getRow());

        GridPane.setHalignment(bombSprite, HPos.CENTER);
        GridPane.setValignment(bombSprite, VPos.CENTER);

        // Démarrer le minuteur
        bomb.startTimer(this::onBombExplosion, gameBoard);

        System.out.println("💣 Joueur " + playerNumber + " place une bombe : " + bomb + " (" + (activeBombsCount + 1) + "/" + playerMaxBombs + ")");
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

        // Vérifier si les joueurs sont touchés
        checkPlayersInExplosion(explosionCells);

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

        if (needsUpdate) {
            updateBoardDisplay();
        }
    }

    private void updateBoardDisplay() {
        // Supprimer seulement les rectangles qui représentent les cellules du plateau
        gameGrid.getChildren().removeIf(node -> {
            if (node instanceof Rectangle) {
                Rectangle rect = (Rectangle) node;
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

        // S'assurer que les joueurs restent au premier plan
        if (player1Sprite != null) {
            player1Sprite.toFront();
        }

        if (player2Sprite != null) {
            player2Sprite.toFront();
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
            explosionSprite.getStyleClass().add("explosion");
            explosionSprites.add(explosionSprite);

            // Placer l'explosion sur la grille
            gameGrid.add(explosionSprite, pos.col, pos.row);
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

    private void checkPlayersInExplosion(List<Bomb.Position> explosionCells) {
        // Vérifier le joueur 1
        if (player1Alive) {
            Bomb.Position player1Pos = new Bomb.Position(player1.getRow(), player1.getCol());
            for (Bomb.Position explosionPos : explosionCells) {
                if (explosionPos.equals(player1Pos)) {
                    playerDied(1);
                    player1Sprite.setFill(new ImagePattern(persoDeath));
                    break;
                }
            }
        }

        // Vérifier le joueur 2
        if (player2Alive) {
            Bomb.Position player2Pos = new Bomb.Position(player2.getRow(), player2.getCol());
            for (Bomb.Position explosionPos : explosionCells) {
                if (explosionPos.equals(player2Pos)) {
                    playerDied(2);
                    player2Sprite.setFill(new ImagePattern(perso2Death));
                    break;
                }
            }
        }
    }

    private void playerDied(int playerNumber) {
        if (playerNumber == 1) {
            player1Alive = false;
            System.out.println("💀 LE JOUEUR 1 EST MORT !");
            player1Sprite.setFill(Color.GRAY);
        } else {
            player2Alive = false;
            System.out.println("💀 LE JOUEUR 2 EST MORT !");
            player2Sprite.setFill(Color.GRAY);
        }

        checkGameEnd();
    }

    private void checkGameEnd() {
        if (!player1Alive && !player2Alive) {
            System.out.println("🤝 MATCH NUL ! Les deux joueurs sont morts !");
            startButton.setText("MATCH NUL");
        } else if (!player1Alive) {
            System.out.println("🏆 JOUEUR 2 GAGNE !");
            startButton.setText("JOUEUR 2 GAGNE");
        } else if (!player2Alive) {
            System.out.println("🏆 JOUEUR 1 GAGNE !");
            startButton.setText("JOUEUR 1 GAGNE");
        }

        if (!player1Alive || !player2Alive) {
            gamePaused = true;
            startButton.setDisable(true);

            for (Bomb bomb : activeBombs) {
                bomb.stopTimer();
            }
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

        player1BombsActive = 0;
        player2BombsActive = 0;

        initializeGameArea();
    }

    public boolean canMoveTo(int row, int col) {
        return gameBoard != null && gameBoard.isValidMove(row, col);
    }
}