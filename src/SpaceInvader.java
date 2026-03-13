/*
In essence, my project is a basic space game.
You move left and right using the arrow keys and shoot with the Space bar.
There are reward or penalty drops with certain probabilities.
You can pause the game with the P key and reset it with the R key.
While creating a function using ready image assets, I used JavaFX.
*/


// Here, I am making the necessary imports.

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.*;
import java.util.stream.Collectors;

public class SpaceInvader extends Application {
    // Then, I handle the assignment of values to my final variables.
//  I define my boolean values and, along with some list definitions
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int PLAYER_WIDTH = 40;
    private static final int PLAYER_HEIGHT = 40;
    private static final int ENEMY_WIDTH = 40;
    private static final int ENEMY_HEIGHT = 40;
    private static final double ENEMY_SPEED = 2;
    private static final int BULLET_WIDTH = 5;
    private static final int BULLET_HEIGHT = 10;
    private static final long ENEMY_SPAWN_INTERVAL = 2000; // 2 seconds
    private static final long POWER_UP_DURATION = 5000; // 5 seconds
    private static final long EXPLOSION_DURATION = 1000; // 1 second

    private boolean gameRunning = false;
    private boolean gamePaused = false;
    private boolean enhancedFire = false;
    private boolean gameOver = false;
    private boolean inMainMenu = true;

    private double playerX;
    private double playerY;
    private int score = 0;
    private long lastEnemySpawnTime = 0;
    private long enhancedFireStartTime = 0;

    private final List<Bullet> bullets = new ArrayList<>();
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Message> messages = new ArrayList<>();
    private final List<Explosion> explosions = new ArrayList<>();
    private final List<Reward> rewards = new ArrayList<>();

    // I define the images I will get from the assets.
    private Image playerShip;
    private Image enemyShip;
    private Image reward;
    private Image punishmentImage;
    private Image titleScreen;
    private GraphicsContext gc;
    private AnimationTimer gameLoop;

    // After that, I write the game start function using the sequential functions here.
    @Override
    public void start(Stage primaryStage) {
        initializeGame(primaryStage);
    }

    private void initializeGame(Stage primaryStage) {
        primaryStage.setTitle("Space Invader Game");
        loadResources();
        setupCanvas(primaryStage);
    }

    // Here, I am getting the images I will use from the file location.
    private void loadResources() {
        try {
            playerShip = new Image(getClass().getResource("/assets/player.png").toExternalForm());
            enemyShip = new Image(getClass().getResource("/assets/enemy.png").toExternalForm());
            reward = new Image(getClass().getResource("/assets/reward.png").toExternalForm());
            punishmentImage = new Image(getClass().getResource("/assets/punishment.png").toExternalForm());
            titleScreen = new Image(getClass().getResource("/assets/main.png").toExternalForm());
            reward = new Image(getClass().getResource("/assets/reward.png").toExternalForm());
        } catch (Exception e) {
            System.err.println("Error loading resources: " + e.getMessage());
            System.exit(1);
        }
    }

    //  I am creating my important Canvas method, which will handle my game screen, key controls, and so on.
    private void setupCanvas(Stage primaryStage) {
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        gc = canvas.getGraphicsContext2D();
        Group root = new Group(canvas);
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        scene.setOnKeyPressed(e -> handleKeyPress(e.getCode()));
        primaryStage.setScene(scene);
        primaryStage.show();

        showTitleScreen();
    }

    // With the handleKeyPress method, I adjust the basic mechanics. I assign key bindings based on different situations.
    private void handleKeyPress(KeyCode code) {
        if (gameOver) {
            if (code == KeyCode.ESCAPE) {
                returnToTitle();
            } else if (code == KeyCode.R) {
                restartGame();
            }
            return;
        }

        if (!gameRunning && code == KeyCode.R) {
            restartGame();
            return;
        }

        if (!gameRunning) {
            if (code == KeyCode.ENTER) {
                inMainMenu = false;
                gameRunning = true;
                gameOver = false;
                resetGameState();
                initializeGameLoop();
                gameLoop.start();
            }
            if (code == KeyCode.ESCAPE) {
                System.exit(0);
            }
            return;
        }


        switch (code) {
            case LEFT:
                movePlayer(-10);
                break;
            case RIGHT:
                movePlayer(10);
                break;
            case SPACE:
                shoot();
                break;
            case P:
                gamePaused = !gamePaused;
                break;
            case R:
                restartGame();
                break;
            case ESCAPE:
                returnToTitle();
                break;
        }
    }

    // The returnToTitle function will take us back to the main menu.
    private void returnToTitle() {
        gameRunning = false;
        gameOver = false;
        inMainMenu = true;
        if (gameLoop != null) {
            gameLoop.stop();
        }
        showTitleScreen();
    }

    // The restartGame function resets the game, I could say.
    private void restartGame() {
        resetGameState();
        gameRunning = true;
        gameOver = false;
        initializeGameLoop();
        gameLoop.start();
    }


    // The resetGameState function resets the necessary in-game values, I could say.
    // It resets most of my things to avoid any issues.
    private void resetGameState() {
        gameRunning = true;
        gamePaused = false;
        enhancedFire = false;
        score = 0;
        playerX = WIDTH / 2.0 - PLAYER_WIDTH / 2.0;
        playerY = HEIGHT - PLAYER_HEIGHT - 10;
        bullets.clear();
        enemies.clear();
        messages.clear();
        explosions.clear();
        rewards.clear();
    }

    // The initializeGameLoop provides the main game loop.
    private void initializeGameLoop() {
        if (gameLoop != null) {
            gameLoop.stop();
        }

        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!gamePaused) {
                    updateGameState();
                    renderGame();
                }
            }
        };
    }

    // The showTitleScreen function sets the screen we initially see and provides messages to the player.
    private void showTitleScreen() {
        gc.clearRect(0, 0, WIDTH, HEIGHT);
        gc.drawImage(titleScreen, 0, 0, WIDTH, HEIGHT);

        gc.setFill(Color.BLACK);
        gc.setFont(new Font(30));
        gc.fillText("PRESS ENTER TO PLAY", (WIDTH / 2.0) - 125, HEIGHT / 2.0);
        gc.fillText("PRESS ESC TO EXIT", (WIDTH / 2.0) - 100, (HEIGHT / 2.0) + 40);
    }

    // The gameOver function displays a game over screen and includes messages on that screen.
    // It shows instructions on what to do next, such as how to play again or return to the main menu.
    private void gameOver() {
        gameRunning = false;
        gameOver = true;
        if (gameLoop != null) {
            gameLoop.stop();
        }

        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        gc.setFill(Color.RED);
        gc.setFont(new Font("Arial Bold", 64));
        String gameOverText = "GAME OVER";
        double gameOverWidth = gc.getFont().getSize() * gameOverText.length() * 0.5;
        gc.fillText(gameOverText, ((WIDTH - gameOverWidth) / 2.0) - 40, HEIGHT / 3.0);

        gc.setFill(Color.WHITE);
        gc.setFont(new Font("Arial", 40));
        String scoreText = "Score: " + score;
        double scoreWidth = gc.getFont().getSize() * scoreText.length() * 0.5;
        gc.fillText(scoreText, (WIDTH - scoreWidth) / 2.0, HEIGHT / 2.0);

        gc.setFont(new Font("Arial", 24));
        String restartText = "PRESS R TO RESTART";
        String menuText = "PRESS ESC FOR MAIN MENU";
        double restartWidth = gc.getFont().getSize() * restartText.length() * 0.5;
        double menuWidth = gc.getFont().getSize() * menuText.length() * 0.5;

        gc.fillText(restartText, ((WIDTH - restartWidth) / 2.0) - 20, HEIGHT * 0.65);
        gc.fillText(menuText, ((WIDTH - menuWidth) / 2.0) - 24, HEIGHT * 0.7);

        gc.getCanvas().requestFocus();
    }

    // I also have a basic movePlayer function, which I use for movement. I call this function within another function.
    private void movePlayer(double delta) {
        double newX = playerX + delta;
        if (newX >= 0 && newX <= WIDTH - PLAYER_WIDTH) {
            playerX = newX;
        }
    }

    // The updateGameState is one of my most important functions.
    // It controls the flow of the game by calling different functions.
    private void updateGameState() {
        updateBullets();
        spawnEnemies();
        updateEnemies();
        updateRewards();
        updatePowerUps();
        cleanupExpiredElements();
    }

    //  I have a Bullet class that holds the necessary information for managing bullets.
    private static class Bullet {
        private double x, y;
        private final double dx, dy;

        Bullet(double x, double y, double dx, double dy) {
            this.x = x;
            this.y = y;
            this.dx = dx;
            this.dy = dy;
        }

        void update() {
            x += dx;
            y += dy;
        }

        boolean isOffScreen() {
            return y < 0;
        }
    }

    // The createBullet and updateBullets functions create bullets and control their movement.
    private Bullet createBullet(double x, double y, double dx, double dy) {
        return new Bullet(x, y, dx, dy);
    }

    private void updateBullets() {
        bullets.removeIf(bullet -> {
            bullet.update();
            return bullet.isOffScreen();
        });
    }

    // The shoot function controls the bullet movement according to two different firing mechanisms.
    private void shoot() {
        if (enhancedFire) {
            double centerX = playerX + PLAYER_WIDTH / 2.0;
            bullets.add(createBullet(centerX - BULLET_WIDTH / 2.0, playerY, 0, -5));  // Straight
            bullets.add(createBullet(centerX, playerY, 3, -5));                       // Right diagonal
            bullets.add(createBullet(centerX - BULLET_WIDTH, playerY, -3, -5));       // Left diagonal
        } else {
            bullets.add(createBullet(playerX + PLAYER_WIDTH / 2.0 - BULLET_WIDTH / 2.0, playerY, 0, -5));
        }
    }

    // I also have a function for creating enemies, and the following updateEnemies function handles the movement of the enemies.
    private void spawnEnemies() {
        if (System.currentTimeMillis() - lastEnemySpawnTime > ENEMY_SPAWN_INTERVAL) {
            double enemyX = new Random().nextDouble() * (WIDTH - ENEMY_WIDTH);
            enemies.add(new Enemy(enemyX, 0));
            lastEnemySpawnTime = System.currentTimeMillis();
        }
    }

    private void updateEnemies() {
        for (Enemy enemy : new ArrayList<>(enemies)) {
            enemy.update();

            if (enemy.isCollidingWithPlayer(playerX, playerY)) {
                gameOver();
                enemies.clear();
                bullets.clear();
                return;
            }

            if (checkBulletCollisions(enemy)) {
                enemies.remove(enemy);
                handleEnemyDestroyed(enemy);
            } else if (enemy.isOffScreen()) {
                enemies.remove(enemy);
            }
        }
    }

    // My checkBulletCollisions function checks bullet collisions and helps with scoring.
    private boolean checkBulletCollisions(Enemy enemy) {
        return bullets.removeIf(bullet -> {
            if (enemy.isCollidingWithBullet(bullet)) {
                score += 100;
                return true;
            }
            return false;
        });
    }

    // With the handleEnemyDestroyed function, I manage the reward and penalty system.
    // There is a 60% chance for a drop, but this drop has a 1/3 chance of being a penalty and a 2/3 chance of being a reward.
    private void handleEnemyDestroyed(Enemy enemy) {
        explosions.add(new Explosion(enemy.x, enemy.y));

        int chance = new Random().nextInt(100);
        if (chance < 60) {
            int rewardType = new Random().nextInt(3);
            rewards.add(new Reward(enemy.x, enemy.y, rewardType));
        }
    }

    // I detail this part in the updateRewards function.
    private void updateRewards() {
        Iterator<Reward> rewardIterator = rewards.iterator();
        while (rewardIterator.hasNext()) {
            Reward reward = rewardIterator.next();
            reward.update();

            if (reward.isCollidingWithPlayer(playerX, playerY)) {
                if (reward.type == 0) {
                    score += 50;
                    addMessage("+50 Points!", playerX, playerY, Color.GREEN);
                } else if (reward.type == 1) {
                    score -= 50;
                    addMessage("-50 Points!", playerX, playerY, Color.RED);
                } else if (reward.type == 2) {
                    enhancedFire = true;
                    enhancedFireStartTime = System.currentTimeMillis();
                    addMessage("Enhanced Fire Active!", playerX, playerY, Color.GREEN);
                }
                rewardIterator.remove();
            } else if (reward.isOffScreen()) {
                rewardIterator.remove();
            }
        }
    }

    // I print messages with addMessage, which appear in the top-right corner of the screen.
    // If there is an active reward message (for 5 seconds), the next message is displayed below the previous one.
    private void addMessage(String text, double x, double y, Color color) {
        double messageY = 80;
        double messageX = 20;

        if (messages.stream().anyMatch(msg -> msg.text.equals("Enhanced Fire Active!"))) {
            List<Message> enhancedMessages = messages.stream()
                    .filter(msg -> msg.text.equals("Enhanced Fire Active!"))
                    .collect(Collectors.toList());
            if (!enhancedMessages.isEmpty()) {
                Message lastEnhancedMessage = enhancedMessages.get(enhancedMessages.size() - 1);
                messageY = lastEnhancedMessage.y + 40;
            }
        }

        messages.add(new Message(text, messageX, messageY, color, text.equals("Enhanced Fire Active!") ? 5000 : 2000));
    }

    // The functions here are used within the UpdateGameStats function.
    // As I mentioned, these are crucial for the game flow and manage the process of enhanced bullet functionality.
    private void updatePowerUps() {
        long currentTime = System.currentTimeMillis();
        if (enhancedFire && currentTime - enhancedFireStartTime > POWER_UP_DURATION) {
            enhancedFire = false;
        }
    }

    // cleanupExpiredElements is a function that manages a one-second process after an explosion.
    private void cleanupExpiredElements() {
        long currentTime = System.currentTimeMillis();
        messages.removeIf(message -> currentTime - message.startTime > message.duration);
        explosions.removeIf(explosion -> currentTime - explosion.timestamp > EXPLOSION_DURATION);
    }

    // Then, I have my render functions listed below.
    private void renderGame() {
        if (!gameRunning) {
            return;
        }

        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        renderGameElements();
        renderUI();
    }

    // With the renderGameElements function, I perform general rendering tasks, which I can simply explain as such.
    private void renderGameElements() {
        gc.drawImage(playerShip, playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT);

        for (Bullet bullet : bullets) {
            gc.setFill(Color.WHITE);
            gc.fillRect(bullet.x, bullet.y, BULLET_WIDTH, BULLET_HEIGHT);
        }

        for (Enemy enemy : enemies) {
            gc.drawImage(enemyShip, enemy.x, enemy.y, ENEMY_WIDTH, ENEMY_HEIGHT);
        }

        for (Explosion explosion : explosions) {
            gc.drawImage(punishmentImage, explosion.x, explosion.y, ENEMY_WIDTH, ENEMY_HEIGHT);
        }

        for (Reward reward : rewards) {
            gc.drawImage(this.reward, reward.x, reward.y, ENEMY_WIDTH, ENEMY_HEIGHT);
        }
    }

    // I print messages again with the renderUI function.
    private void renderUI() {
        gc.setFill(Color.WHITE);
        gc.setFont(new Font(24));
        gc.fillText("Score: " + score, 20, 40);

        for (Message message : messages) {
            gc.setFill(message.color);
            gc.fillText(message.text, message.x, message.y);
        }
    }


    // The Enemy class contains the necessary code for the enemies.
    private static class Enemy {
        private final double x;
        private double y;

        Enemy(double x, double y) {
            this.x = x;
            this.y = y;
        }

        void update() {
            y += ENEMY_SPEED;
        }

        boolean isOffScreen() {
            return y > HEIGHT;
        }

        boolean isCollidingWithPlayer(double playerX, double playerY) {
            return (x < playerX + PLAYER_WIDTH &&
                    x + ENEMY_WIDTH > playerX &&
                    y < playerY + PLAYER_HEIGHT &&
                    y + ENEMY_HEIGHT > playerY);
        }

        boolean isCollidingWithBullet(Bullet bullet) {
            return bullet.x < x + ENEMY_WIDTH &&
                    bullet.x + BULLET_WIDTH > x &&
                    bullet.y < y + ENEMY_HEIGHT &&
                    bullet.y + BULLET_HEIGHT > y;
        }
    }

    // The Message class contains the necessary code for messages.
    private static class Message {
        private final String text;
        private final double x, y;
        private final Color color;
        private final long startTime;
        private final long duration;

        Message(String text, double x, double y, Color color, long duration) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.color = color;
            this.startTime = System.currentTimeMillis();
            this.duration = duration;
        }
    }


    // I also have a very basic Explosion Class here.
    private static class Explosion {
        private final double x, y;
        private final long timestamp;

        Explosion(double x, double y) {
            this.x = x;
            this.y = y;
            this.timestamp = System.currentTimeMillis();
        }
    }


    // Lastly, I have the Reward class, which contains some basic code for managing the drop speed of the reward and similar properties.
    private static class Reward {
        private double x, y;
        private final int type;
        private static final double FALL_SPEED = 3.0;

        Reward(double x, double y, int type) {
            this.x = x;
            this.y = y;
            this.type = type;
        }

        void update() {
            y += FALL_SPEED;
        }

        boolean isOffScreen() {
            return y > HEIGHT;
        }

        boolean isCollidingWithPlayer(double playerX, double playerY) {
            return (x < playerX + PLAYER_WIDTH &&
                    x + ENEMY_WIDTH > playerX &&
                    y < playerY + PLAYER_HEIGHT &&
                    y + ENEMY_HEIGHT > playerY);
        }
    }

    // Finally, I have the Main method here.
    public static void main(String[] args) {
        launch(args);
    }
}