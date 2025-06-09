package com.bomberman.controller;

import com.bomberman.model.GameBoard;
import com.bomberman.model.Player;
import com.bomberman.model.Bomb;
import com.bomberman.controller.UserManager;
import com.bomberman.model.User;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class GameControllerTheme1 implements Initializable {

    @FXML
    private StackPane gameArea;

    @FXML
    private GridPane gameGrid;

    // Variables pour le menu de pause (maintenant dans FXML)
    @FXML
    private StackPane pauseMenu;
    @FXML
    private VBox pauseMenuContent;
    @FXML
    private Button resumeButton;
    @FXML
    private Button backToMenuButton;
    private boolean isPauseMenuVisible = false;

    private boolean gameStarted = true; // Démarrage automatique
    private boolean gamePaused = false;
    private boolean gameEnded = false;

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

    // NOUVEAU : Gestion des utilisateurs
    private UserManager userManager;

    // Taille des cellules en pixels
    private static final int CELL_SIZE = 40;
    private static final int DEFAULT_EXPLOSION_RANGE = 1;
    private static final int DEFAULT_MAX_BOMBS = 1;

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

    // Images des cellules
    private Image wallImage;
    private Image blockImage;
    private Image floorImage;
    private Image floorShadowImage;

    // Images des explosions
    private Image explosionCenterImage;
    private Image explosionEndUpImage;
    private Image explosionEndDownImage;
    private Image explosionEndLeftImage;
    private Image explosionEndRightImage;
    private Image explosionMiddleUpImage;
    private Image explosionMiddleDownImage;
    private Image explosionMiddleLeftImage;
    private Image explosionMiddleRightImage;

    //Image de fin de partie
    private Image victoire1;
    private Image victoire2;
    private Image egalite;
    private ImageView resultImageView;

    @FXML
    private Label timerLabel;

    // Variables pour le timer
    private Timeline gameTimer;
    private int timeRemainingSeconds = 120;
    private static final int GAME_DURATION_SECONDS = 120;

    // Images Bomb et bonus
    private Image bombImage;
    private Image bombBonusImage;
    private Image rangeBonusImage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // NOUVEAU : Initialiser le gestionnaire d'utilisateurs
        userManager = UserManager.getInstance();

        // Images perso 1
        persoDown = new Image(getClass().getResource("/images/persoDown.png").toExternalForm());
        persoLeft = new Image(getClass().getResource("/images/persoLeft.png").toExternalForm());
        persoRight = new Image(getClass().getResource("/images/persoRight.png").toExternalForm());
        persoUp = new Image(getClass().getResource("/images/persoUp.png").toExternalForm());
        persoDeath = new Image(getClass().getResource("/images/death.png").toExternalForm());

        // Images perso 2
        perso2Down = new Image(getClass().getResource("/images/perso2Down.png").toExternalForm());
        perso2Left = new Image(getClass().getResource("/images/perso2Left.png").toExternalForm());
        perso2Right = new Image(getClass().getResource("/images/perso2Right.png").toExternalForm());
        perso2Up = new Image(getClass().getResource("/images/perso2Up.png").toExternalForm());
        perso2Death = new Image(getClass().getResource("/images/death2.png").toExternalForm());

        wallImage = new Image(getClass().getResource("/images/wall.png").toExternalForm());
        blockImage = new Image(getClass().getResource("/images/block.png").toExternalForm());
        floorImage = new Image(getClass().getResource("/images/floor.png").toExternalForm());
        floorShadowImage = new Image(getClass().getResource("/images/floor_shadow.png").toExternalForm());

        // Bombe et bonus image
        bombImage = new Image(this.getClass().getResource("/images/bomb.png").toExternalForm());
        bombBonusImage = new Image(this.getClass().getResource("/images/bomb-bonus.png").toExternalForm());
        rangeBonusImage = new Image(this.getClass().getResource("/images/range-bonus.png").toExternalForm());

        // Image explosion
        explosionCenterImage = new Image(getClass().getResource("/images/explosion_milieu.png").toExternalForm());
        explosionEndUpImage = new Image(getClass().getResource("/images/bout_explosion_haut.png").toExternalForm());
        explosionEndDownImage = new Image(getClass().getResource("/images/bout_explosion_bas.png").toExternalForm());
        explosionEndLeftImage = new Image(getClass().getResource("/images/bout_explosion_gauche.png").toExternalForm());
        explosionEndRightImage = new Image(getClass().getResource("/images/bout_explosion_droite.png").toExternalForm());
        explosionMiddleUpImage = new Image(getClass().getResource("/images/explosion_haut.png").toExternalForm());
        explosionMiddleDownImage = new Image(getClass().getResource("/images/explosion_bas.png").toExternalForm());
        explosionMiddleLeftImage = new Image(getClass().getResource("/images/explosion_gauche.png").toExternalForm());
        explosionMiddleRightImage = new Image(getClass().getResource("/images/explosion_droite.png").toExternalForm());

        victoire1 = new Image(getClass().getResource("/images/victoire1.png").toExternalForm());
        victoire2 = new Image(getClass().getResource("/images/victoire2.png").toExternalForm());
        egalite = new Image(getClass().getResource("/images/egalite.png").toExternalForm());

        System.out.println("GameController initialisé");
        initializeGameArea();
        setupKeyboardControls();
        initializeTimer();

        // Démarrer automatiquement le jeu
        gameTimer.play();
    }

    // Méthodes timer
    private void initializeTimer() {
        gameTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateTimer()));
        gameTimer.setCycleCount(Timeline.INDEFINITE);
        updateTimerDisplay();
    }

    private void updateTimer() {
        timeRemainingSeconds--;
        updateTimerDisplay();

        if (timeRemainingSeconds <= 0) {
            gameTimer.stop();
            handleTimeUp();
        }
    }

    private void updateTimerDisplay() {
        int minutes = timeRemainingSeconds / 60;
        int seconds = timeRemainingSeconds % 60;
        String timeText = String.format("%02d:%02d", minutes, seconds);
        timerLabel.setText(timeText);

        if (timeRemainingSeconds <= 30) {
            timerLabel.setStyle("-fx-text-fill: red;");
        } else if (timeRemainingSeconds <= 60) {
            timerLabel.setStyle("-fx-text-fill: orange;");
        } else {
            timerLabel.setStyle("-fx-text-fill: green;");
        }
    }

    private void handleTimeUp() {
        gameEnded = true;
        System.out.println("⏰ TEMPS ÉCOULÉ !");

        for (Bomb bomb : activeBombs) {
            bomb.stopTimer();
        }

        checkGameEnd();
        gamePaused = true;
    }

    private void initializeGameArea() {
        gameBoard = new GameBoard();

        player1 = new Player("Player 1", 1, 1);
        player2 = new Player("Player 2", gameBoard.getHeight() - 2, gameBoard.getWidth() - 2);

        player1Alive = true;
        player2Alive = true;
        player1BombsActive = 0;
        player2BombsActive = 0;

        player1ExplosionRange = DEFAULT_EXPLOSION_RANGE;
        player2ExplosionRange = DEFAULT_EXPLOSION_RANGE;
        player1MaxBombs = DEFAULT_MAX_BOMBS;
        player2MaxBombs = DEFAULT_MAX_BOMBS;

        activeBombs = new ArrayList<>();
        bombSprites = new HashMap<>();
        explosionSprites = new ArrayList<>();

        gameGrid.getChildren().clear();
        createVisualBoard();
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
        GameBoard.CellType cellType = gameBoard.getCellType(row, col);
        switch (cellType) {
            case EMPTY:
                boolean hasWallAbove = false;
                if (row > 0) {
                    GameBoard.CellType cellAbove = gameBoard.getCellType(row - 1, col);
                    hasWallAbove = (cellAbove == GameBoard.CellType.INDESTRUCTIBLE_WALL ||
                            cellAbove == GameBoard.CellType.DESTRUCTIBLE_WALL);
                }

                if (hasWallAbove) {
                    cell.setFill(new ImagePattern(floorShadowImage));
                } else {
                    cell.setFill(new ImagePattern(floorImage));
                }
                break;
            case INDESTRUCTIBLE_WALL:
                cell.setFill(new ImagePattern(wallImage));
                break;
            case DESTRUCTIBLE_WALL:
                cell.setFill(new ImagePattern(blockImage));
                break;
            case BOMB_BONUS:
                cell.setFill(new ImagePattern(bombBonusImage));
                break;
            case RANGE_BONUS:
                cell.setFill(new ImagePattern(rangeBonusImage));
                break;
        }

        return cell;
    }

    private void createPlayersSprites() {
        player1Sprite = new Circle(CELL_SIZE / 2.1);
        player1Sprite.setFill(new ImagePattern(persoDown));

        player2Sprite = new Circle(CELL_SIZE / 2.1);
        player2Sprite.setFill(new ImagePattern(perso2Down));

        gameGrid.add(player1Sprite, player1.getCol(), player1.getRow());
        gameGrid.add(player2Sprite, player2.getCol(), player2.getRow());
    }

    private void setupKeyboardControls() {
        gameArea.setFocusTraversable(true);
        gameArea.setOnKeyPressed(this::handleKeyPress);
        gameArea.requestFocus();
    }

    @FXML
    private void handleKeyPress(KeyEvent event) {
        // Gestion de la pause avec Échap
        if (event.getCode() == KeyCode.ESCAPE) {
            if (isPauseMenuVisible) {
                resumeGame();
            } else {
                showPauseMenu();
            }
            event.consume();
            return;
        }

        // Le reste du code (mouvement des joueurs)
        if (!gameStarted || (!player1Alive && !player2Alive) || isPauseMenuVisible) return;

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

        gameArea.requestFocus();
    }

    private void showPauseMenu() {
        if (gameStarted && !gamePaused) {
            gamePaused = true;
            isPauseMenuVisible = true;
            pauseMenu.setVisible(true);

            if (gameTimer != null) {
                gameTimer.pause();
            }

            for (Bomb bomb : activeBombs) {
                bomb.stopTimer();
            }

            System.out.println("⏸️ Jeu en pause");
        }
    }

    @FXML
    private void resumeGame() {
        if (gamePaused) {
            gamePaused = false;
            isPauseMenuVisible = false;
            pauseMenu.setVisible(false);

            if (gameTimer != null) {
                gameTimer.play();
            }

            for (Bomb bomb : activeBombs) {
                bomb.startTimer(this::onBombExplosion, gameBoard);
            }

            gameArea.requestFocus();
            System.out.println("▶️ Jeu repris");
        }
    }

    @FXML
    private void backToMainMenu() {
        try {
            if (gameTimer != null) {
                gameTimer.stop();
            }
            for (Bomb bomb : activeBombs) {
                bomb.stopTimer();
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/menu.fxml"));
            Parent menuRoot = loader.load();

            Scene menuScene = new Scene(menuRoot, 800, 600);
            menuScene.getStylesheets().add(getClass().getResource("/css/menu.css").toExternalForm());

            Stage stage = (Stage) gameArea.getScene().getWindow();
            stage.setScene(menuScene);
            stage.setTitle("Super Bomberman - Menu");

            System.out.println("🏠 Retour au menu principal");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updatePlayer1Position() {
        gameGrid.getChildren().remove(player1Sprite);
        gameGrid.add(player1Sprite, player1.getCol(), player1.getRow());
        checkBonusCollection(1);
    }

    private void updatePlayer2Position() {
        gameGrid.getChildren().remove(player2Sprite);
        gameGrid.add(player2Sprite, player2.getCol(), player2.getRow());
        checkBonusCollection(2);
    }

    private void checkBonusCollection(int playerNumber) {
        Player currentPlayer = (playerNumber == 1) ? player1 : player2;
        GameBoard.CellType cellType = gameBoard.getCellType(currentPlayer.getRow(), currentPlayer.getCol());

        if (cellType == GameBoard.CellType.BOMB_BONUS) {
            if (playerNumber == 1) {
                player1MaxBombs++;
                System.out.println("💣 Joueur 1 collecte un bonus bombes ! Nouvelles bombes max: " + player1MaxBombs);
            } else {
                player2MaxBombs++;
                System.out.println("💣 Joueur 2 collecte un bonus bombes ! Nouvelles bombes max: " + player2MaxBombs);
            }

            gameBoard.setCellType(currentPlayer.getRow(), currentPlayer.getCol(), GameBoard.CellType.EMPTY);
            updateBoardDisplay();

        } else if (cellType == GameBoard.CellType.RANGE_BONUS) {
            if (playerNumber == 1) {
                player1ExplosionRange++;
                System.out.println("🔥 Joueur 1 collecte un bonus portée ! Nouvelle portée: " + player1ExplosionRange);
            } else {
                player2ExplosionRange++;
                System.out.println("🔥 Joueur 2 collecte un bonus portée ! Nouvelle portée: " + player2ExplosionRange);
            }

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

        int activeBombsCount = 0;
        for (Bomb bomb : activeBombs) {
            if (bomb.getOwner() == playerNumber) {
                activeBombsCount++;
            }
        }

        if (activeBombsCount >= playerMaxBombs) {
            System.out.println("❌ Joueur " + playerNumber + " : Limite de bombes atteinte (" + activeBombsCount + "/" + playerMaxBombs + ") !");
            return;
        }

        for (Bomb bomb : activeBombs) {
            if (bomb.getRow() == currentPlayer.getRow() && bomb.getCol() == currentPlayer.getCol()) {
                System.out.println("❌ Il y a déjà une bombe ici !");
                return;
            }
        }

        Bomb bomb = new Bomb(currentPlayer.getRow(), currentPlayer.getCol(), playerExplosionRange);
        bomb.setOwner(playerNumber);
        activeBombs.add(bomb);

        Circle bombSprite = new Circle(CELL_SIZE / 2.1);
        bombSprite.setFill(new ImagePattern(bombImage));

        bombSprites.put(bomb, bombSprite);

        gameGrid.add(bombSprite, bomb.getCol(), bomb.getRow());

        GridPane.setHalignment(bombSprite, HPos.CENTER);
        GridPane.setValignment(bombSprite, VPos.CENTER);

        bomb.startTimer(this::onBombExplosion, gameBoard);

        System.out.println("💣 Joueur " + playerNumber + " place une bombe : " + bomb + " (" + (activeBombsCount + 1) + "/" + playerMaxBombs + ")");
    }

    private void onBombExplosion(Bomb bomb, List<Bomb.Position> explosionCells) {
        System.out.println("💥 EXPLOSION ! " + bomb);

        Circle bombSprite = bombSprites.get(bomb);
        if (bombSprite != null) {
            gameGrid.getChildren().remove(bombSprite);
            bombSprites.remove(bomb);
        }

        destroyWallsInExplosion(explosionCells);
        createExplosionAnimation(explosionCells);
        checkPlayersInExplosion(explosionCells);
        activeBombs.remove(bomb);
    }

    private void destroyWallsInExplosion(List<Bomb.Position> explosionCells) {
        boolean needsUpdate = false;

        for (Bomb.Position pos : explosionCells) {
            GameBoard.CellType cellType = gameBoard.getCellType(pos.row, pos.col);

            boolean wallDestroyed = gameBoard.destroyWall(pos.row, pos.col);
            if (wallDestroyed) {
                System.out.println("🧱 Mur détruit en (" + pos.row + ", " + pos.col + ")");
                needsUpdate = true;
            }

            if (cellType == GameBoard.CellType.BOMB_BONUS) {
                gameBoard.setCellType(pos.row, pos.col, GameBoard.CellType.EMPTY);
                System.out.println("💣 Bonus bombes détruit en (" + pos.row + ", " + pos.col + ")");
                needsUpdate = true;
            } else if (cellType == GameBoard.CellType.RANGE_BONUS) {
                gameBoard.setCellType(pos.row, pos.col, GameBoard.CellType.EMPTY);
                System.out.println("🔥 Bonus portée détruit en (" + pos.row + ", " + pos.col + ")");
                needsUpdate = true;
            }
        }

        if (needsUpdate) {
            updateBoardDisplay();
        }
    }

    private void updateBoardDisplay() {
        gameGrid.getChildren().removeIf(node -> {
            if (node instanceof Rectangle) {
                Rectangle rect = (Rectangle) node;
                return rect.getWidth() == CELL_SIZE && rect.getHeight() == CELL_SIZE &&
                        !explosionSprites.contains(rect);
            }
            return false;
        });

        for (int row = 0; row < gameBoard.getHeight(); row++) {
            for (int col = 0; col < gameBoard.getWidth(); col++) {
                Rectangle cell = createCell(row, col);
                gameGrid.add(cell, col, row);
            }
        }

        if (player1Sprite != null) {
            player1Sprite.toFront();
        }

        if (player2Sprite != null) {
            player2Sprite.toFront();
        }

        for (Circle bombSprite : bombSprites.values()) {
            bombSprite.toFront();
        }
    }

    private void createExplosionAnimation(List<Bomb.Position> explosionCells) {
        if (explosionCells.isEmpty()) return;

        Bomb.Position center = explosionCells.get(0);

        for (Bomb.Position pos : explosionCells) {
            Rectangle explosionSprite = new Rectangle(CELL_SIZE, CELL_SIZE);
            Image explosionImage = getExplosionImageForPosition(pos, center, explosionCells);
            explosionSprite.setFill(new ImagePattern(explosionImage));
            explosionSprites.add(explosionSprite);

            gameGrid.add(explosionSprite, pos.col, pos.row);
        }

        Timeline explosionTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            for (Rectangle sprite : explosionSprites) {
                gameGrid.getChildren().remove(sprite);
            }
            explosionSprites.clear();
        }));
        explosionTimer.play();
    }

    private Image getExplosionImageForPosition(Bomb.Position pos, Bomb.Position center, List<Bomb.Position> allCells) {
        if (pos.row == center.row && pos.col == center.col) {
            return explosionCenterImage;
        }

        int deltaRow = pos.row - center.row;
        int deltaCol = pos.col - center.col;

        if (deltaCol == 0 && deltaRow < 0) {
            if (isExplosionEnd(pos, center, allCells, 1)) {
                return explosionEndUpImage;
            } else {
                return explosionMiddleUpImage;
            }
        }

        if (deltaCol == 0 && deltaRow > 0) {
            if (isExplosionEnd(pos, center, allCells, 0)) {
                return explosionEndDownImage;
            } else {
                return explosionMiddleDownImage;
            }
        }

        if (deltaRow == 0 && deltaCol < 0) {
            if (isExplosionEnd(pos, center, allCells, 3)) {
                return explosionEndLeftImage;
            } else {
                return explosionMiddleLeftImage;
            }
        }

        if (deltaRow == 0 && deltaCol > 0) {
            if (isExplosionEnd(pos, center, allCells, 2)) {
                return explosionEndRightImage;
            } else {
                return explosionMiddleRightImage;
            }
        }

        return explosionCenterImage;
    }

    private boolean isExplosionEnd(Bomb.Position pos, Bomb.Position center, List<Bomb.Position> allCells, int direction) {
        int nextRow = pos.row;
        int nextCol = pos.col;

        switch (direction) {
            case 0: // Bas
                nextRow = pos.row + 1;
                break;
            case 1: // Haut
                nextRow = pos.row - 1;
                break;
            case 2: // Droite
                nextCol = pos.col + 1;
                break;
            case 3: // Gauche
                nextCol = pos.col - 1;
                break;
        }

        for (Bomb.Position cell : allCells) {
            if (cell.row == nextRow && cell.col == nextCol) {
                return false;
            }
        }

        return true;
    }

    private void checkPlayersInExplosion(List<Bomb.Position> explosionCells) {
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

    // NOUVELLE MÉTHODE : Gestion de la fin de partie avec statistiques
    private void checkGameEnd() {
        if (gameEnded) return;

        String winner = null;
        boolean isDraw = false;

        if (!player1Alive && !player2Alive) {
            System.out.println("🤝 MATCH NUL ! Les deux joueurs sont morts !");
            isDraw = true;
            showResult("🤝 MATCH NUL ! Les deux joueurs sont morts !", egalite);
        } else if (!player1Alive) {
            System.out.println("🏆 JOUEUR 2 GAGNE !");
            winner = "player2";
            showResult("🏆 JOUEUR 2 GAGNE !", victoire2);
        } else if (!player2Alive) {
            System.out.println("🏆 JOUEUR 1 GAGNE !");
            winner = "player1";
            showResult("🏆 JOUEUR 1 GAGNE !", victoire1);
        } else if (timeRemainingSeconds <= 0) {
            System.out.println("⏰ TEMPS ÉCOULÉ ! MATCH NUL !");
            isDraw = true;
            showResult("⏰ TEMPS ÉCOULÉ ! MATCH NUL !", egalite);
        }

        gameEnded = true;
        gameTimer.stop();
        updateUserStats(winner, isDraw);

        for (Bomb bomb : activeBombs) {
            bomb.stopTimer();
        }
    }

    // NOUVELLE MÉTHODE : Mettre à jour les statistiques utilisateur (VERSION CORRIGÉE)
    private void updateUserStats(String winner, boolean isDraw) {
        if (!userManager.isLoggedIn()) {
            System.out.println("⚠️ Aucun utilisateur connecté - pas de mise à jour des stats");
            return; // Sortir de la méthode si personne n'est connecté
        }

        try {
            User currentUser = userManager.getCurrentUser();
            System.out.println("📊 Mise à jour des statistiques pour : " + currentUser.getUsername());

            // Incrémenter les parties jouées
            currentUser.incrementGamesPlayed();

            // Ajouter une victoire si nécessaire
            if (!isDraw) {
                // Pour l'instant, considérons que l'utilisateur connecté est toujours "player1"
                // Dans une future version, on pourrait demander qui est qui
                boolean userWon = "player1".equals(winner);

                if (userWon) {
                    currentUser.incrementGamesWon();
                    System.out.println("🏆 Victoire ajoutée ! Total : " + currentUser.getGamesWon() + "/" + currentUser.getGamesPlayed());
                } else {
                    System.out.println("😢 Défaite enregistrée. Score : " + currentUser.getGamesWon() + "/" + currentUser.getGamesPlayed());
                }
            } else {
                System.out.println("🤝 Match nul enregistré. Score : " + currentUser.getGamesWon() + "/" + currentUser.getGamesPlayed());
            }

            // Forcer la sauvegarde via UserManager
            userManager.updateProfile(null, null, null);

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la mise à jour des statistiques : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // NOUVELLE MÉTHODE : Dialog de fin de partie
    private void showEndGameDialog(String winner, boolean isDraw) {
        Timeline delayedDialog = new Timeline(new KeyFrame(Duration.seconds(3), e -> {
            String title = isDraw ? "Match Nul" : (winner.equals("player1") ? "Joueur 1 Gagne !" : "Joueur 2 Gagne !");

            String content = "";
            if (userManager.isLoggedIn()) {
                User user = userManager.getCurrentUser();
                content = String.format("✅ Statistiques de %s :\nParties jouées : %d\nParties gagnées : %d\nRatio victoires : %.1f%%\n\n",
                        user.getUsername(), user.getGamesPlayed(), user.getGamesWon(), user.getWinRate());
            } else {
                content = "ℹ️ Connectez-vous pour sauvegarder vos statistiques !\n\n";
            }
            content += "Que voulez-vous faire ?";

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Fin de Partie");
            alert.setHeaderText(title);
            alert.setContentText(content);

            // Personnaliser les boutons
            ButtonType replayButton = new ButtonType("Rejouer");
            ButtonType menuButton = new ButtonType("Menu Principal");
            alert.getButtonTypes().setAll(replayButton, menuButton);

            // Appliquer le style
            alert.getDialogPane().getStylesheets().add(
                    getClass().getResource("/css/menu.css").toExternalForm()
            );
            alert.getDialogPane().getStyleClass().add("alert");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent()) {
                if (result.get() == replayButton) {
                    restartGame();
                } else {
                    backToMainMenu();
                }
            } else {
                backToMainMenu(); // Par défaut
            }
        }));
        delayedDialog.play();
    }

    // NOUVELLE MÉTHODE : Redémarrer la partie
    private void restartGame() {
        try {
            // Arrêter tous les timers
            if (gameTimer != null) {
                gameTimer.stop();
            }
            for (Bomb bomb : activeBombs) {
                bomb.stopTimer();
            }

            // Recharger la scène de jeu
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/theme1.fxml"));
            Parent gameRoot = loader.load();

            Scene gameScene = new Scene(gameRoot, 800, 700);
            gameScene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            Stage stage = (Stage) gameArea.getScene().getWindow();
            stage.setScene(gameScene);
            stage.setTitle("Super Bomberman - Nouvelle Partie");

            System.out.println("🔄 Nouvelle partie démarrée !");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors du redémarrage : " + e.getMessage());
            backToMainMenu(); // Fallback vers le menu
        }
    }

    private void showResult(String consoleMessage, Image image) {
        System.out.println(consoleMessage);
        if (resultImageView != null) {
            return;
        }
        resultImageView = new ImageView(image);
        resultImageView.setPreserveRatio(true);
        resultImageView.setFitWidth(500);
        gameArea.getChildren().add(resultImageView);
    }
}