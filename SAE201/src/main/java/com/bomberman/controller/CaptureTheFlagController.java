package com.bomberman.controller;

import com.bomberman.model.GameBoard;
import com.bomberman.model.Player;
import com.bomberman.model.Bomb;
import com.bomberman.model.Flag;
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

public class CaptureTheFlagController implements Initializable {

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

    //Image CAPTURE THE FLAG
    private Image redFlagImage, blueFlagImage;
    private Image redFlagDroppedImage, blueFlagDroppedImage;
    private Image redBaseImage, blueBaseImage;
    private Flag redFlag, blueFlag;
    private ImageView redFlagSprite, blueFlagSprite;
    private boolean player1HasFlag, player2HasFlag;
    private int player1Score = 0, player2Score = 0;
    private static final int WINNING_SCORE = 3;
    private static final int FLAG_RETURN_TIME = 10;

    // Images Bomb et bonus
    private Image bombImage;
    private Image bombBonusImage;
    private Image rangeBonusImage;

    @FXML
    private Label timerLabel;

    // Variables pour le timer
    private Timeline gameTimer;
    private int timeRemainingSeconds = 120;
    private static final int GAME_DURATION_SECONDS = 120;


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

        //Flag
        blueFlagImage = new Image(getClass().getResource("/images/CTF/blueFlag.png").toExternalForm());
        redFlagImage = new Image(getClass().getResource("/images/CTF/redFlag.png").toExternalForm());

        System.out.println("CTF Controller initialisé");
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

        endGame();
        gamePaused = true;
    }


    private void initializeGameArea() {
        gameBoard = new GameBoard();

        player1 = new Player("Player 1", 1, 1);
        player2 = new Player("Player 2", gameBoard.getHeight() - 2, gameBoard.getWidth() - 2);

        redFlag = new Flag(gameBoard.getHeight() - 2, 1, Flag.Team.RED);
        blueFlag = new Flag(1, gameBoard.getWidth() - 2, Flag.Team.BLUE);

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
        createFlagSprites();

        System.out.println("CTF Game initialized - Score to win: " + WINNING_SCORE);
        System.out.println("🚩 Red flag a: (" + redFlag.getRow() + ", " + redFlag.getCol() + ")");
        System.out.println("🚩 Blue flag a+: (" + blueFlag.getRow() + ", " + blueFlag.getCol() + ")");
    }

    private void createFlagSprites(){
        redFlagSprite = new ImageView(redFlagImage);
        redFlagSprite.setFitWidth(CELL_SIZE * 0.8);
        redFlagSprite.setFitHeight(CELL_SIZE * 0.8);
        redFlagSprite.setPreserveRatio(true);

        blueFlagSprite = new ImageView(blueFlagImage);
        blueFlagSprite.setFitWidth(CELL_SIZE * 0.8);
        blueFlagSprite.setFitHeight(CELL_SIZE * 0.8);
        blueFlagSprite.setPreserveRatio(true);

        gameGrid.add(redFlagSprite, redFlag.getCol(), redFlag.getRow());
        gameGrid.add(blueFlagSprite, blueFlag.getCol(), blueFlag.getRow());

        GridPane.setHalignment(redFlagSprite, HPos.CENTER);
        GridPane.setValignment(redFlagSprite, VPos.CENTER);
        GridPane.setHalignment(blueFlagSprite, HPos.CENTER);
        GridPane.setValignment(blueFlagSprite, VPos.CENTER);
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
        if (!gameStarted || (!player1Alive || !player2Alive) || isPauseMenuVisible) return;

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
        checkFlagInteraction(1); // New CTF method
    }

    private void updatePlayer2Position() {
        gameGrid.getChildren().remove(player2Sprite);
        gameGrid.add(player2Sprite, player2.getCol(), player2.getRow());
        checkBonusCollection(2);
        checkFlagInteraction(2); // New CTF method
    }

    private void checkFlagInteraction(int playerNumber) {
        Player currentPlayer = (playerNumber == 1) ? player1 : player2;

        // Check if player is trying to capture enemy flag
        if (playerNumber == 1 && !player1HasFlag) {
            // Player 1 trying to get blue flag
            if (currentPlayer.getRow() == blueFlag.getRow() &&
                    currentPlayer.getCol() == blueFlag.getCol() &&
                    !blueFlag.isDropped() && !blueFlag.isCaptured()) {

                captureFlag(1, blueFlag);
            }
        } else if (playerNumber == 2 && !player2HasFlag) {
            // Player 2 trying to get red flag
            if (currentPlayer.getRow() == redFlag.getRow() &&
                    currentPlayer.getCol() == redFlag.getCol() &&
                    !redFlag.isDropped() && !redFlag.isCaptured()) {

                captureFlag(2, redFlag);
            }
        }

        // Check if player is returning to base with flag
        if (playerNumber == 1 && player1HasFlag) {
            // Player 1 returning to red base
            if (currentPlayer.getRow() == redFlag.getRow() &&
                    currentPlayer.getCol() == redFlag.getCol()) {

                scoreCapture(1);
            }
        } else if (playerNumber == 2 && player2HasFlag) {
            // Player 2 returning to blue base
            if (currentPlayer.getRow() == blueFlag.getRow() &&
                    currentPlayer.getCol() == blueFlag.getCol()) {

                scoreCapture(2);
            }
        }

        // Check if player is picking up dropped flag
        checkDroppedFlagPickup(playerNumber);
    }

    private void captureFlag(int playerNumber, Flag flag) {
        System.out.println("🚩 Player " + playerNumber + " captured the " +
                (flag.getTeam() == Flag.Team.RED ? "RED" : "BLUE") + " flag!");

        flag.setCaptured(true);
        if (playerNumber == 1) {
            player1HasFlag = true;
        } else {
            player2HasFlag = true;
        }

        // Hide the flag sprite from its original position
        if (flag == redFlag) {
            gameGrid.getChildren().remove(redFlagSprite);
        } else {
            gameGrid.getChildren().remove(blueFlagSprite);
        }

        // Visual indicator that player has flag (could change player color/add effect)
        updatePlayerSpriteWithFlag(playerNumber, true);
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

    private void restoreFlagSprites() {
        System.out.println("🔧 Remise en cours des Drapeau...");
        System.out.println("Drapeau Rouge - capturer " + redFlag.isCaptured() + ", Lacher: " + redFlag.isDropped() + ", position: (" + redFlag.getRow() + ", " + redFlag.getCol() + ")");
        System.out.println("Drapeau Bleu - capturer " + blueFlag.isCaptured() + ", Lacher: " + blueFlag.isDropped() + ", position: (" + blueFlag.getRow() + ", " + blueFlag.getCol() + ")");

        // Count children before restoration
        int childrenBefore = gameGrid.getChildren().size();

        // Restore red flag if not captured and not dropped
        if (!redFlag.isCaptured() && !redFlag.isDropped()) {
            boolean alreadyPresent = gameGrid.getChildren().contains(redFlagSprite);
            System.out.println("Drapeau Rouge Deja sur le GameBoard: " + alreadyPresent);

            if (!alreadyPresent) {
                gameGrid.add(redFlagSprite, redFlag.getCol(), redFlag.getRow());
                GridPane.setHalignment(redFlagSprite, HPos.CENTER);
                GridPane.setValignment(redFlagSprite, VPos.CENTER);
                redFlagSprite.toFront();
                System.out.println("🚩 Drapeau Rouge remis a (" + redFlag.getRow() + ", " + redFlag.getCol() + ")");
            }
        }

        // Restore blue flag if not captured and not dropped
        if (!blueFlag.isCaptured() && !blueFlag.isDropped()) {
            boolean alreadyPresent = gameGrid.getChildren().contains(blueFlagSprite);
            System.out.println("Drapeau Bleu Deja sur le GameBoard: " + alreadyPresent);

            if (!alreadyPresent) {
                gameGrid.add(blueFlagSprite, blueFlag.getCol(), blueFlag.getRow());
                GridPane.setHalignment(blueFlagSprite, HPos.CENTER);
                GridPane.setValignment(blueFlagSprite, VPos.CENTER);
                blueFlagSprite.toFront();
                System.out.println("🚩 Drapeau bleu remis a (" + blueFlag.getRow() + ", " + blueFlag.getCol() + ")");
            }
        }

        int childrenAfter = gameGrid.getChildren().size();
        System.out.println("Grid children: " + childrenBefore + " -> " + childrenAfter);
    }

    private void updateBoardDisplay() {
        gameGrid.getChildren().removeIf(node -> {
            if (node instanceof Rectangle) {
                Rectangle rect = (Rectangle) node;
                return rect.getWidth() == CELL_SIZE &&
                        rect.getHeight() == CELL_SIZE &&
                        !explosionSprites.contains(rect) &&
                        (rect.getId() == null || !rect.getId().equals("explosion-sprite"));
            }
            return false;
        });

        for (int row = 0; row < gameBoard.getHeight(); row++) {
            for (int col = 0; col < gameBoard.getWidth(); col++) {
                Rectangle cell = createCell(row, col);
                gameGrid.add(cell, col, row);
            }
        }

        // Restore players
        if (player1Sprite != null) {
            player1Sprite.toFront();
        }

        if (player2Sprite != null) {
            player2Sprite.toFront();
        }

        // Restore bombs
        for (Circle bombSprite : bombSprites.values()) {
            bombSprite.toFront();
        }

        // 🔧 FIX: Restore flag sprites
        restoreFlagSprites();
    }

    private void createExplosionAnimation(List<Bomb.Position> explosionCells) {
        if (explosionCells.isEmpty()) return;

        Bomb.Position center = explosionCells.get(0);
        List<Rectangle> currentExplosionSprites = new ArrayList<>();

        // Sauvegarder les positions des drapeaux AVANT d'ajouter les explosions
        boolean redFlagVisible = !redFlag.isCaptured() && !redFlag.isDropped();
        boolean blueFlagVisible = !blueFlag.isCaptured() && !blueFlag.isDropped();

        for (Bomb.Position pos : explosionCells) {
            Rectangle explosionSprite = new Rectangle(CELL_SIZE, CELL_SIZE);
            Image explosionImage = getExplosionImageForPosition(pos, center, explosionCells);
            explosionSprite.setFill(new ImagePattern(explosionImage));
            explosionSprite.setId("explosion-sprite");

            // Rendre l'explosion semi-transparente si elle est sur un drapeau
            boolean isOnFlag = false;
            if (redFlagVisible && pos.row == redFlag.getRow() && pos.col == redFlag.getCol()) {
                explosionSprite.setOpacity(0.7); // Semi-transparent
                isOnFlag = true;
            }
            if (blueFlagVisible && pos.row == blueFlag.getRow() && pos.col == blueFlag.getCol()) {
                explosionSprite.setOpacity(0.7); // Semi-transparent
                isOnFlag = true;
            }

            explosionSprites.add(explosionSprite);
            currentExplosionSprites.add(explosionSprite);

            gameGrid.add(explosionSprite, pos.col, pos.row);

            if (isOnFlag) {
                System.out.println("🔥 Explosion semi-transparente sur drapeau à (" + pos.row + ", " + pos.col + ")");
            }
        }

        // Remettre les drapeaux au premier plan IMMÉDIATEMENT
        if (redFlagVisible) {
            redFlagSprite.toFront();
        }
        if (blueFlagVisible) {
            blueFlagSprite.toFront();
        }

        Timeline explosionTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            for (Rectangle sprite : currentExplosionSprites) {
                gameGrid.getChildren().remove(sprite);
                explosionSprites.remove(sprite);
            }

            // S'assurer que les drapeaux sont toujours visibles
            restoreFlagSprites();

            System.out.println("🧹 Explosion cleaned up, flags preserved");
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
        if ((playerNumber == 1 && player1HasFlag) || (playerNumber == 2 && player2HasFlag)) {
            dropFlag(playerNumber);
        }

        // In CTF, players should respawn after a delay
        Timeline respawnTimer = new Timeline(new KeyFrame(Duration.seconds(5), e -> {
            respawnPlayer(playerNumber);
        }));
        respawnTimer.play();
    }

    private void resetFlag(Flag flag) {
        flag.reset();

        // Restore flag sprite to original position
        if (flag == redFlag) {
            gameGrid.add(redFlagSprite, redFlag.getCol(), redFlag.getRow());
        } else {
            gameGrid.add(blueFlagSprite, blueFlag.getCol(), blueFlag.getRow());
        }

        GridPane.setHalignment(flag == redFlag ? redFlagSprite : blueFlagSprite, HPos.CENTER);
        GridPane.setValignment(flag == redFlag ? redFlagSprite : blueFlagSprite, VPos.CENTER);
    }

    private void dropFlag(int playerNumber) {
        Player currentPlayer = (playerNumber == 1) ? player1 : player2;

        if (playerNumber == 1 && player1HasFlag) {
            // Drop blue flag
            blueFlag.drop(currentPlayer.getRow(), currentPlayer.getCol());
            player1HasFlag = false;

            // Show dropped flag sprite
            ImageView droppedSprite = new ImageView(blueFlagDroppedImage);
            droppedSprite.setFitWidth(CELL_SIZE * 0.6);
            droppedSprite.setFitHeight(CELL_SIZE * 0.6);
            droppedSprite.setPreserveRatio(true);
            gameGrid.add(droppedSprite, blueFlag.getCol(), blueFlag.getRow());

            // Start return timer
            startFlagReturnTimer(blueFlag);

        } else if (playerNumber == 2 && player2HasFlag) {
            // Drop red flag
            redFlag.drop(currentPlayer.getRow(), currentPlayer.getCol());
            player2HasFlag = false;

            // Show dropped flag sprite
            ImageView droppedSprite = new ImageView(redFlagDroppedImage);
            droppedSprite.setFitWidth(CELL_SIZE * 0.6);
            droppedSprite.setFitHeight(CELL_SIZE * 0.6);
            droppedSprite.setPreserveRatio(true);
            gameGrid.add(droppedSprite, redFlag.getCol(), redFlag.getRow());

            // Start return timer
            startFlagReturnTimer(redFlag);
        }

        updatePlayerSpriteWithFlag(playerNumber, false);
        System.out.println("💧 Player " + playerNumber + " dropped the flag!");
    }

    private void startFlagReturnTimer(Flag flag) {
        Timeline returnTimer = new Timeline(new KeyFrame(Duration.seconds(FLAG_RETURN_TIME), e -> {
            if (flag.isDropped()) {
                System.out.println("⏰ Flag returned to base automatically!");
                resetFlag(flag);
            }
        }));
        returnTimer.play();
    }

    private void checkDroppedFlagPickup(int playerNumber) {
        Player currentPlayer = (playerNumber == 1) ? player1 : player2;

        // Check if friendly player is picking up their own dropped flag
        if (playerNumber == 1 && redFlag.isDropped() &&
                currentPlayer.getRow() == redFlag.getRow() &&
                currentPlayer.getCol() == redFlag.getCol()) {

            // Player 1 returning their own flag
            resetFlag(redFlag);
            System.out.println("🔄 Player 1 returned their flag!");

        } else if (playerNumber == 2 && blueFlag.isDropped() &&
                currentPlayer.getRow() == blueFlag.getRow() &&
                currentPlayer.getCol() == blueFlag.getCol()) {

            // Player 2 returning their own flag
            resetFlag(blueFlag);
            System.out.println("🔄 Player 2 returned their flag!");

        } else if (playerNumber == 1 && blueFlag.isDropped() && !player1HasFlag &&
                currentPlayer.getRow() == blueFlag.getRow() &&
                currentPlayer.getCol() == blueFlag.getCol()) {

            // Player 1 picking up dropped enemy flag
            blueFlag.setCaptured(true);
            blueFlag.setDropped(false);
            player1HasFlag = true;
            updatePlayerSpriteWithFlag(1, true);
            System.out.println("🎯 Player 1 picked up the dropped blue flag!");

        } else if (playerNumber == 2 && redFlag.isDropped() && !player2HasFlag &&
                currentPlayer.getRow() == redFlag.getRow() &&
                currentPlayer.getCol() == redFlag.getCol()) {

            // Player 2 picking up dropped enemy flag
            redFlag.setCaptured(true);
            redFlag.setDropped(false);
            player2HasFlag = true;
            updatePlayerSpriteWithFlag(2, true);
            System.out.println("🎯 Player 2 picked up the dropped red flag!");
        }
    }

    private void updatePlayerSpriteWithFlag(int playerNumber, boolean hasFlag) {
        // You could modify the player sprite to show they have a flag
        // For example, add a glow effect or change the border color
        if (hasFlag) {
            if (playerNumber == 1) {
                player1Sprite.setStroke(Color.YELLOW);
                player1Sprite.setStrokeWidth(3);
            } else {
                player2Sprite.setStroke(Color.YELLOW);
                player2Sprite.setStrokeWidth(3);
            }
        } else {
            if (playerNumber == 1) {
                player1Sprite.setStroke(null);
            } else {
                player2Sprite.setStroke(null);
            }
        }
    }

    private void respawnPlayer(int playerNumber) {
        if (playerNumber == 1) {
            player1.setRow(1);
            player1.setCol(1);
            player1Alive = true;
            player1Sprite.setFill(new ImagePattern(persoDown));
            updatePlayer1Position();
            System.out.println("♻️ Player 1 respawned!");
        } else {
            player2.setRow(gameBoard.getHeight() - 2);
            player2.setCol(gameBoard.getWidth() - 2);
            player2Alive = true;
            player2Sprite.setFill(new ImagePattern(perso2Down));
            updatePlayer2Position();
            System.out.println("♻️ Player 2 respawned!");
        }
    }

    private void scoreCapture(int playerNumber) {
        System.out.println("🏆 Player " + playerNumber + " scored!");

        if (playerNumber == 1) {
            player1Score++;
            player1HasFlag = false;
            // Reset blue flag to its base
            resetFlag(blueFlag);
        } else {
            player2Score++;
            player2HasFlag = false;
            // Reset red flag to its base
            resetFlag(redFlag);
        }

        updatePlayerSpriteWithFlag(playerNumber, false);
        System.out.println("📊 Score: Player 1: " + player1Score + " - Player 2: " + player2Score);

        // Check for win condition
        if (player1Score >= WINNING_SCORE || player2Score >= WINNING_SCORE) {
            endGame();
        }
    }


    private void endGame() {
        gameEnded = true;
        gameTimer.stop();

        String winner = null;
        boolean isDraw = false;

        if (player1Score >= WINNING_SCORE && player2Score >= WINNING_SCORE) {
            // Shouldn't happen, but just in case
            isDraw = true;
            showResult("🤝 TIE GAME!", egalite);
        } else if (player1Score >= WINNING_SCORE) {
            winner = "player1";
            showResult("🏆 PLAYER 1 WINS!", victoire1);
        } else if (player2Score >= WINNING_SCORE) {
            winner = "player2";
            showResult("🏆 PLAYER 2 WINS!", victoire2);
        } else if (timeRemainingSeconds <= 0) {
            // Time up - highest score wins
            if (player1Score > player2Score) {
                winner = "player1";
                showResult("⏰ TIME UP! PLAYER 1 WINS!", victoire1);
            } else if (player2Score > player1Score) {
                winner = "player2";
                showResult("⏰ TIME UP! PLAYER 2 WINS!", victoire2);
            } else {
                isDraw = true;
                showResult("⏰ TIME UP! TIE GAME!", egalite);
            }
        }

        gameEnded = true;
        gameTimer.stop();
        updateUserStats(winner, isDraw);

        for (Bomb bomb : activeBombs) {
            bomb.stopTimer();
        }

        System.out.println("🏁 Final Score: Player 1: " + player1Score + " - Player 2: " + player2Score);
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
