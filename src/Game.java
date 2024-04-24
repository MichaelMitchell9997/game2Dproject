
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


import game2D.*;

import javax.swing.*;

// Game demonstrates how we can override the GameCore class
// to create our own 'game'. We usually need to implement at
// least 'draw' and 'update' (not including any local event handling)
// to begin the process. You should also add code to the 'init'
// method that will initialise event handlers etc.

// Student ID: 2831597


/**
 * The type Game.
 */
public class Game extends GameCore {


    /**
     * The constant SCREEN_WIDTH.
     */
// Game Constants
    public static final int SCREEN_WIDTH = 1920;
    /**
     * The constant SCREEN_HEIGHT.
     */
    public static final int SCREEN_HEIGHT = 1080;
    /**
     * The constant MAX_SCREEN_WIDTH.
     */
    public static final int MAX_SCREEN_WIDTH = SCREEN_WIDTH * 5;
    /**
     * The constant GRAVITY.
     */
    public static final float GRAVITY = 0.001f;

    // Game State Flags
    private boolean rightKeyPressed = false;
    private boolean leftKeyPressed = false;
    private boolean lastKeyRight = false;
    private boolean lastKeyLeft = false;
    private boolean jump = false;
    private boolean debug = false;
    private boolean onGround = true;
    private boolean tileAbovePlayer = false;
    private boolean playerMoving = false;
    private boolean playerMovingRight = false;

    // Player Attributes
    private int playerHealth = 3;
    private int bossHealth = 4;
    private float newVolume = 1;

    // Game Resources
    private Animation playerIdle;
    private Animation playerRun;
    private Animation playerDeath;
    private Animation enemyRun;
    private Animation enemyDeath;
    private Animation bossWalk;
    private Animation bossDead;
    private Animation playerArrow;
    private Animation coinAnim;

    private Animation princess;

    private int xo;

    private Sprite player = null;
    private Sprite arrow;
    private Sprite arrowCollision;
    private Sprite boss;

    private Sprite princessSprite;

    private ArrayList<Sprite> background = new ArrayList<>();
    private ArrayList<Sprite> enemies = new ArrayList<>();
    private ArrayList<Sprite> coinSprite = new ArrayList<>();
    private ArrayList<Tile> collidedTiles = new ArrayList<>();

    // Tile Maps
    private TileMap tmap = new TileMap(); // Note: loaded in init()
    private TileMap tmapBackground = new TileMap();
    private TileMap tmapDoor = new TileMap();

    // Miscellaneous
    private int totalScore;
    private int level = 1;
    /**
     * The Background music.
     */
    public SoundLoop backgroundMusic;

    private SoundLoop walkingSound;
    private boolean isWalkingSoundPlaying = false;

    private boolean gameOverPlayed = false;

    private Image healthIcon, bossHealthIcon, WinScreen;


    /**
     * The obligatory main method that creates
     * an instance of our class and starts it running
     *
     * @param args The list of parameters this program might use (ignored)
     */
    public static void main(String[] args) {

        Game gct = new Game();

        gct.init();
        // Start in windowed mode with the given screen height and width
        gct.run(false, SCREEN_WIDTH, SCREEN_HEIGHT);
    }


    /**
     * Initialise the class, e.g. set up variables, load images,
     * create animations, register event handlers.
     * <p>
     * This shows you the general principles but you should create specific
     * methods for setting up your game that can be called again when you wish to
     * restart the game (for example you may only want to load animations once
     * but you could reset the positions of sprites each time you restart the game).
     */
    public void init() {
        initGameWindow(1920, 1080);
        initTileMap("backgroundMap.txt", "map.txt", "doorMap.txt");
        initSprites();
        initBackgroundAnimation("images/background/grassRoad.png", "images/background/grasses.png", "images/background/trees.png", "images/background/jungle_bg.png");
        initBackgroundMusic("sounds/bgmusic.wav");
        initialiseGame();
    }


    private void initGameWindow(int width, int height) {
        setSize(width, height);
        setVisible(true);
    }

    private void initTileMap(String backgroundMapPath, String mapLevelOne, String doorMapPath) {
        tmapBackground.loadMap("maps", backgroundMapPath);
        tmapDoor.loadMap("maps", doorMapPath);
        tmap.loadMap("maps", mapLevelOne);
    }

    private void initSprites() {
        // Player animations
        playerIdle = loadAnimationFromSheet("images/Idle.png", 4, 250);
        playerRun = loadAnimationFromSheet("images/Run.png", 7, 150);
        playerArrow = loadAnimationFromSheet("images/arrow.png", 3, 250);
        playerDeath = loadAnimationFromSheet("images/Dead.png", 6, 450);
        player = new Sprite(playerIdle);

        princess = loadAnimationFromSheet("images/princess.png", 4, 250);
        princessSprite = new Sprite(princess);

        coinAnim = loadAnimationFromSheet("images/coin.png", 9, 250);
        for (int i = 0; i < 4; i++) {
            coinSprite.add(new Sprite(coinAnim));
        }

        // Enemy animations
        enemyRun = loadAnimationFromSheet("images/slimeRun.png", 13, 150);
        enemyDeath = loadAnimationFromSheet("images/slimeDead.png", 3, 300);
        for (int i = 0; i < 4; i++) {
            enemies.add(new Sprite(enemyRun));
        }

        //Player Arrow attack
        arrow = new Sprite(playerArrow);
        arrowCollision = new Sprite(playerArrow);
        arrow.setPosition(-100f, -100f);
        arrowCollision.setPosition(-100f, -100f);

        //boss
        bossWalk = loadAnimationFromSheet("images/bossRun.png", 7, 200);
        bossDead = loadAnimationFromSheet("images/bossDead.png", 3, 350);
        boss = new Sprite(bossWalk);

        healthIcon = new ImageIcon("images/playerHealth.png").getImage();

        bossHealthIcon = new ImageIcon("images/bossHealth.png").getImage();

        WinScreen = new ImageIcon("images/background/winBackground.png").getImage();
    }

    private Animation loadAnimationFromSheet(String path, int frames, int duration) {
        Animation animation = new Animation();
        animation.loadAnimationFromSheet(path, frames, 1, duration);
        return animation;
    }

    private void initBackgroundAnimation(String bg1Path, String bg2Path, String bg3Path, String bg4Path) {
        Animation bg1 = loadAnimationFromSheet(bg1Path, 1, 100);
        Animation bg2 = loadAnimationFromSheet(bg2Path, 1, 100);
        Animation bg3 = loadAnimationFromSheet(bg3Path, 1, 100);
        Animation bg4 = loadAnimationFromSheet(bg4Path, 1, 100);

        background.add(new Sprite(bg4));
        background.add(new Sprite(bg3));
        background.add(new Sprite(bg2));
        background.add(new Sprite(bg1));
    }

    private void initBackgroundMusic(String musicPath) {
        backgroundMusic = new SoundLoop(musicPath);
        backgroundMusic.start();
    }

    /**
     * You will probably want to put code to restart a game in
     * a separate method so that you can call it when restarting
     * the game when the player loses.
     */
    public void initialiseGame() {
        player.setPosition(200, 960);
        player.setVelocity(0, 0);
        player.show();

        if (level == 1) {
            levelOneSpawn();
        }
        if (level == 2) {
            levelTwoSpawn();
        }
    }

    /**
     * Level one spawn.
     */
    public void levelOneSpawn() {
        boss.setPosition(8000, 640);
        boss.setVelocity(0.1f, 0f);
        boss.show();

        princessSprite.hide();

        //enemy placement
        for (int i = 0; i < enemies.size(); i++) {
            Sprite s = enemies.get(i);
            if (i == 0) {
                s.setPosition(1300, 960);
            } else if (i == 1) {
                s.setPosition(600, 832);
            } else if (i == 2) {
                s.setPosition(1810, 896);
            } else if (i == 3) {
                s.setPosition(2500, 960);
            }
            s.setVelocity(0, 0);
            s.show();
            s.setVelocityX(0.05f);
        }

        //coin placement
        for (int i = 0; i < coinSprite.size(); i++) {
            Sprite s = coinSprite.get(i);
            if (i == 0) {
                s.setPosition(6000, 640);
            } else if (i == 1) {
                s.setPosition(600, 832);
            } else if (i == 2) {
                s.setPosition(900, 448);
            } else if (i == 3) {
                s.setPosition(2100, 768);
            }
            s.setVelocity(0, 0);
            s.show();
            s.setVelocityX(0);
        }

    }

    /**
     * Level two spawn.
     */
    public void levelTwoSpawn() {

        princessSprite.setPosition(3900, 960);
        princessSprite.setVelocity(0, 0);
        princessSprite.show();

        for (int i = 0; i < coinSprite.size(); i++) {
            Sprite s = coinSprite.get(i);
            if (i == 0) {
                s.setPosition(500, 960);
            } else if (i == 1) {
                s.setPosition(600, 960);
            } else if (i == 2) {
                s.setPosition(400, 960);
            } else if (i == 3) {
                s.setPosition(300, 960);
            }
            s.setVelocity(0, 0);
            s.show();
            s.setVelocityX(0);
        }

        for (int i = 0; i < enemies.size(); i++) {
            Sprite s = enemies.get(i);
            if (i == 0) {
                s.setPosition(1300, 960);
            } else if (i == 1) {
                s.setPosition(600, 960);
            } else if (i == 2) {
                s.setPosition(1810, 960);
            } else if (i == 3) {
                s.setPosition(2500, 960);
            }
            s.setVelocity(0, 0);
            s.show();
            s.setVelocityX(0.05f);
        }

        player.setPosition(200, 800); // fixes the player glitching through the floor and going offscreen
        player.show();
    }

    /**
     * Draw the current state of the game. Note the sample use of
     * debugging output that is drawn directly to the game screen.
     */
    public void draw(Graphics2D g) {
        if (player.getX() < (float) SCREEN_WIDTH / 2) {
            //xo = -(int) player.getX() + ((screenWidth - Math.round(player.getX())));
            //was trying to do some math where the players position was minus the screen width until half way where it would then lock on
            //but turns out I just needed to have xo=0
            xo = 0;
        } else if (player.getX() > MAX_SCREEN_WIDTH - (float) SCREEN_WIDTH / 2) {
            xo = -(MAX_SCREEN_WIDTH - SCREEN_WIDTH);
        } else {
            xo = -(int) player.getX() + SCREEN_WIDTH / 2;
        }
        int yo = 0;

        g.setColor(Color.darkGray);
        g.fillRect(0, 0, getWidth(), getHeight());
        // Apply offsets to sprites then draw them
        for (Sprite s : background) {
            for (int i = 1; i < 7; i++) {
                s.setOffsets(xo + (SCREEN_WIDTH * i), yo);
                s.draw(g);
            }
            for (int i = 0; i < 2; i++) {
                s.setOffsets(xo - (SCREEN_WIDTH * i), yo);
                s.draw(g);
            }
        }

        // Apply offsets to tile map and draw  it
        tmapBackground.draw(g, xo, yo);
        tmapDoor.draw(g, xo, yo);
        tmap.draw(g, xo, yo);

        AffineTransform transform = new AffineTransform();
        transform.translate(Math.round(player.getX()), Math.round(player.getY()));

        if (leftKeyPressed || lastKeyLeft) {
            player.setScale(-1, 1);
            transform.translate(-player.getWidth(), 0);
        } else if (rightKeyPressed || lastKeyRight) {
            player.setScale(1, 1);
        }

        player.setOffsets(xo, yo);
        boss.setOffsets(xo, yo);
        princessSprite.setOffsets(xo, yo);

        for (int i = 0; i < playerHealth; i++) {
            g.drawImage(healthIcon, 1800 - (i * 50), 50, null); // Adjust position and spacing
        }
        if (player.getX() > 7000 && !boss.scored()) {
            for (int i = 0; i < bossHealth; i++) {
                g.drawImage(bossHealthIcon, (int)((boss.getX() - (i * 100))+xo)+350, 600, null); // Adjust position to be above the boss
            }
        }


        if (arrow.getVelocityX() < 0) {
            arrow.setScale(-1, 1);
        } else {
            arrow.setScale(1, 1);
        }
        arrow.drawTransformed(g);

        for (Sprite s : coinSprite) {
            s.setOffsets(xo, yo);
            s.setScale(2, 2);
            s.drawTransformed(g);
            s.setAnimationSpeed(1);
        }


        player.drawTransformed(g);

        princessSprite.setScale(-1, 1);
        princessSprite.drawTransformed(g);
        for (Sprite s : enemies) {
            s.setOffsets(xo, yo);
            if (s.getVelocityX() < 0) {
                s.setScale(-1, 1);
            } else if (s.getVelocityX() > 0) {
                s.setScale(1, 1);
            }
            s.drawTransformed(g);
        }
        boss.drawTransformed(g);

        // Show score and status information
        String msg = String.format("Score: " + totalScore);
        g.setColor(Color.RED);
        g.drawString(msg,

                getWidth() - 1910, 50);

        String musicVolume = String.format("Music Volume: %.0f %%", newVolume * 100);
        Font largeFont = new Font("Serif", Font.BOLD, 24);
        g.setColor(Color.RED);
        g.setFont(largeFont);
        g.drawString(musicVolume,

                getWidth() - 1910, 90);

        if (debug) {
            tmap.drawBorder(g, xo, yo, Color.black);

            g.setColor(Color.RED);
            boss.drawBoundingBox(g);
            player.drawBoundingBox(g);
            arrow.drawBoundingBox(g);
            arrowCollision.drawBoundingBox(g);
            for (Sprite s : enemies) {
                s.drawBoundingBox(g);
            }

            g.drawString(String.format("Player: %.0f,%.0f", player.getX(), player.getY()),
                    getWidth() - 1910, 130);

            drawCollidedTiles(g, tmap, xo, yo);
        }
        if (gameOverPlayed) {
            g.drawImage(WinScreen, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, null);
        }
    }

    /**
     * Draw collided tiles.
     *
     * @param g       the g
     * @param map     the map
     * @param xOffset the x offset
     * @param yOffset the y offset
     */
    public void drawCollidedTiles(Graphics2D g, TileMap map, int xOffset, int yOffset) {
        if (!collidedTiles.isEmpty()) {
            int tileWidth = map.getTileWidth();
            int tileHeight = map.getTileHeight();

            g.setColor(Color.blue);
            for (Tile t : collidedTiles) {
                g.drawRect(t.getXC() + xOffset, t.getYC() + yOffset, tileWidth, tileHeight);
            }
        }
    }

    /**
     * Update any sprites and check for collisions
     *
     * @param elapsed The elapsed time between this call and the previous call of elapsed
     */
    public void update(long elapsed) {

        // Make adjustments to the speed of the sprite due to gravity
        player.setVelocityY(player.getVelocityY() + (GRAVITY * elapsed));
        player.setAnimationSpeed(1.0f);
        if (player.getX() < 0) {
            player.setX(200);
        }


        for (Sprite s : enemies) {
            if (playerHealth > 0 && s.getHealth() > 1) { //if player isn't dead and enemy isn't dead sets the enemy animation to run
                s.setAnimation(enemyRun);
            }
            if (playerHealth > 0 && s.getVelocityX() == 0) { //checks if player is alive and if sprite isn't moving (sprite not moving means its dead)
                s.setAnimation(enemyDeath); //sets enemy animation to the death

                if (!s.scored) { // Check if the score has already been given for this enemy death
                    totalScore += 10;
                    s.scored = true;
                    Sound slimeDeathSound = new Sound("sounds/slimeDeath.wav");
                    slimeDeathSound.start();
                }
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        s.damage(1); // damages enemy after animation so its then removed

                    }
                }, 850);
            }
        }
        if (playerHealth > 0 && boss.getHealth() < -3) {
            boss.setAnimation(bossDead);
            boss.setVelocityX(0);
            boss.setAnimationSpeed(0.5f);
            if (!boss.scored) { // Check if the score has already been given for this enemy death
                totalScore += 300;
                boss.scored = true;
                PlayFilter bossDeathSound = new PlayFilter("sounds/slimeDeath.wav");
                bossDeathSound.start();
            }
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    boss.hide();
                    boss.kill();
                }
            }, 2000);

        }


        if (rightKeyPressed && leftKeyPressed && playerHealth > 0) {
            player.setAnimation(playerIdle);
            player.setVelocityX(0.0f);
        } else if (rightKeyPressed && playerHealth > 0) {
            player.setAnimation(playerRun);
            player.setVelocityX(+0.3f);
            lastKeyRight = true;
            lastKeyLeft = false;

        } else if (leftKeyPressed && playerHealth > 0) {
            player.setAnimation(playerRun);
            player.setVelocityX(-0.3f);
            lastKeyLeft = true;
            lastKeyRight = false;
        } else {
            player.setAnimation(playerIdle);
            player.setVelocityX(0.0f);
        }
        if ((leftKeyPressed || rightKeyPressed) && !isWalkingSoundPlaying) {
            if (walkingSound == null || !walkingSound.isAlive()) {
                walkingSound = new SoundLoop("sounds/walkGrass.wav");
                walkingSound.start();
            }
            isWalkingSoundPlaying = true;
        } else if (!(leftKeyPressed || rightKeyPressed) && isWalkingSoundPlaying) {
            if (walkingSound != null) {
                walkingSound.stopSound();
                isWalkingSoundPlaying = false;
            }
        }

        if (jump) {
            if (onGround) {
                if (!tileAbovePlayer) {
                    player.setVelocityY(-0.4f); // Initial jump velocity
                    jump = false; // Reset the jump flag
                    onGround = false;
                    player.setVelocityY(player.getVelocityY());
                }

            } else {
                player.setVelocityY(player.getVelocityY());
            }

        }


        backgroundScrollSpeed(elapsed);
        checkBoundingCollision(boss);

        for (Sprite s : enemies) {
            checkBoundingCollision(s);
            if (boundingBoxCollision(arrowCollision, s)) {
                s.setVelocityX(0);
                arrow.hideSprite();
                arrowCollision.hideSprite();
            }
            s.update(elapsed);
        }
        for (Sprite s : coinSprite) {
            checkBoundingCollision(s);
            if (boundingBoxCollision(s, player)) {
                s.kill();
                totalScore += 100;
                Sound sound = new Sound("sounds/coinPickUp.wav");
                sound.start();
            }
            s.update(elapsed);
        }
        coinSprite.removeIf(s -> !s.isActive());
        // This removes the sprite if it has been marked as inactive ( killed )
        enemies.removeIf(s -> !s.isActive());

        if (boundingBoxCollision(player, princessSprite)) {
            endGameWin();
        }
        if (boundingBoxCollision(arrowCollision, boss)) {
            boss.damage(1);
            bossHealth -= 1;
            arrow.hideSprite();
            arrowCollision.hideSprite();
        }
        if (playerHealth < 1) {
            playerDeathCycle();
        }

        if (boss.getVelocityX() == 0) {
            tmapDoor.setTileChar('o', 146, 15);
        }

        player.update(elapsed);
        boss.update(elapsed);
        arrow.update(elapsed);
        arrowCollision.update(elapsed);
        princessSprite.update(elapsed);
        //endLevel();

        // Then check for any collisions that may have occurred
        handleScreenEdge(player, tmap, elapsed);
        enemyTileCollision(tmap);
        bossTileCollision(boss, tmap);
        arrowTileCollisionCheck(arrowCollision, tmap);
        checkTileCollision(player, tmap, tmapBackground, tmapDoor);


    }

    /**
     * Handle screen edge.
     *
     * @param s       the s
     * @param tmap    the tmap
     * @param elapsed the elapsed
     */
    public void handleScreenEdge(Sprite s, TileMap tmap, long elapsed) {
        // This method just checks if the sprite has gone off the bottom screen.
        // Ideally you should use tile collision instead of this approach

        float difference = s.getY() + s.getHeight() - tmap.getPixelHeight();
        if (difference > 0) {
            // Put the player back on the map according to how far over they were
            s.setY(tmap.getPixelHeight() - s.getHeight() - (int) (difference));
        }
    }

    /**
     * Player death cycle.
     */
    public void playerDeathCycle() {
        // Play the death animation for the player sprite
        player.setAnimation(playerDeath);

        // Stop all enemies moving and hide them
        for (Sprite s : enemies) {
            s.setVelocityX(0.0f);
            s.hide();
        }
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Reset the game after the pause
                playerHealth = 3;
                for (int i = enemies.size(); i < 4; i++) {
                    enemies.add(new Sprite(enemyRun));
                } // adds a new enemy in until there are 4 enemies again

                initialiseGame();
            }
        }, 2600); // Total duration of the death animation
    }

    /**
     * Check bounding collision.
     *
     * @param s the s
     */
    public void checkBoundingCollision(Sprite s) {
        if (boundingBoxCollision(player, s)) {
            Sound playerHurtSound = new Sound("sounds/playerHurt.wav");
            if (s.getVelocityX() < 0) {
                s.setVelocityX(s.getVelocityX() * -1);
                player.setX(player.getX() - 63);
                playerHealth--;
                playerHurtSound.start();

            } else if (s.getVelocityX() > 0) {
                s.setVelocityX(s.getVelocityX() * -1);
                player.setX(player.getX() + 63);
                playerHealth--;
                playerHurtSound.start();
            }
        }
    }

    /**
     * Background scroll speed.
     *
     * @param elapsed the elapsed
     */
    public void backgroundScrollSpeed(Long elapsed) {

        for (int i = 0; i < background.size(); i++) {
            Sprite s = background.get(i);
            float velocity;

            if (player.getVelocityX() < 0 && playerMoving) {
                velocity = 0.005f * (i + 1);
            } else if (player.getVelocityX() > 0 && playerMovingRight) {
                velocity = -0.005f * (i + 1);
            } else if (player.getVelocityX() == 0.0f) {
                velocity = 0.0f;
            } else {
                velocity = 0.0f;
            }

            s.setVelocityX(velocity);
            s.update(elapsed);
        }

    }

    private void adjustBackgroundMusicVolume(float adjustment) {
        if (backgroundMusic != null && backgroundMusic.volumeControl != null) {
            float currentVolume = getCurrentVolumeFromDecibels(backgroundMusic.volumeControl.getValue());
            newVolume = Math.max(0f, Math.min(1f, currentVolume + adjustment));
            backgroundMusic.setVolume(newVolume);
        }
    }

    private float getCurrentVolumeFromDecibels(float dB) {
        return (float) Math.pow(10.0, dB / 20.0);
    }

    /**
     * Use the sample code in the lecture notes to properly detect
     * a bounding box collision between sprites s1 and s2.
     *
     * @param s1 the s 1
     * @param s2 the s 2
     * @return true if a collision may have occurred, false if it has not.
     */
    public boolean boundingBoxCollision(Sprite s1, Sprite s2) {
        return ((s1.getX() + s1.getImage().getWidth(null) > s2.getX()) &&
                (s1.getX() < (s2.getX() + s2.getImage().getWidth(null))) &&
                ((s1.getY() + s1.getImage().getHeight(null) > s2.getY()) &&
                        (s1.getY() < s2.getY() + s2.getImage().getHeight(null))));

    }

    /**
     * Check and handles collisions with a tile map for the
     * given sprite 's'. Initial functionality is limited...
     *
     * @param s        The Sprite to check collisions for
     * @param tmap     The tile map to check
     * @param tmapbg   the tmapbg
     * @param tmapDoor the tmap door
     */
    public void checkTileCollision(Sprite s, TileMap tmap, TileMap tmapbg, TileMap tmapDoor) {
        // Empty out our current set of collided tiles
        collidedTiles.clear();

        float spriteX = s.getX();
        float spriteY = s.getY();

        float tileWidth = tmap.getTileWidth();
        float tileHeight = tmap.getTileHeight();

        //was having issues with code from the top left and top right collisions breaking each other so i had to split them up into different methods
        checkAboveCollision(s, tmap, spriteX, spriteY, tileWidth, tileHeight);
        checkBottomLeftCollision(s, tmap, spriteX, spriteY, tileWidth, tileHeight);
        checkPlayerCollision(s, tmapbg, tmap, spriteX, spriteY, tileWidth, tileHeight);
        checkPlayerCollision(s, tmapDoor, tmap, spriteX, spriteY, tileWidth, tileHeight);
        checkMiddleLeftCollision(s, tmap, spriteX, spriteY, tileWidth, tileHeight);
        checkMiddleRightCollision(s, tmap, spriteX, spriteY, tileWidth, tileHeight);


    }

    /**
     * Arrow tile collision check.
     *
     * @param s    the s
     * @param tmap the tmap
     */
    public void arrowTileCollisionCheck(Sprite s, TileMap tmap) {
        collidedTiles.clear();

        float spriteX = s.getX();
        float spriteY = s.getY();

        float tileWidth = tmap.getTileWidth();
        float tileHeight = tmap.getTileHeight();

        ArrowLeftCollision(tmap, spriteX, spriteY, tileWidth, tileHeight);
        ArrowRightCollision(s, tmap, spriteX, spriteY, tileWidth, tileHeight);
    }

    private void ArrowRightCollision(Sprite sprite, TileMap tileMap, float spriteX, float spriteY, float tileWidth, float tileHeight) {
        int xtile = (int) ((spriteX + sprite.getWidth()) / tileWidth);
        int ytile = (int) (spriteY / tileHeight);

        Tile aR = tileMap.getTile(xtile, ytile);

        if (aR != null && aR.getCharacter() != '.') {
            arrow.hideSprite();
            arrowCollision.hideSprite();
        }
        collidedTiles.add(aR);
    }

    private void ArrowLeftCollision(TileMap tileMap, float spriteX, float spriteY, float tileWidth, float tileHeight) {
        int xtile = (int) (spriteX / tileWidth);
        int ytile = (int) (spriteY / tileHeight);

        Tile aL = tileMap.getTile(xtile, ytile);

        if (aL != null && aL.getCharacter() != '.') {
            arrow.hideSprite();
            arrowCollision.hideSprite();
        }
        collidedTiles.add(aL);
    }

    /**
     * Boss tile collision.
     *
     * @param s    the s
     * @param tmap the tmap
     */
    public void bossTileCollision(Sprite s, TileMap tmap) {
        float spriteX = s.getX();
        float spriteY = s.getY();

        float tileWidth = tmap.getTileWidth();
        float tileHeight = tmap.getTileHeight();
        enemyMiddleLeftCollision(s, tmap, spriteX, spriteY, tileWidth, tileHeight);
        enemyMiddleRightCollision(s, tmap, spriteX, spriteY, tileWidth, tileHeight);
    }

    /**
     * Enemy tile collision.
     *
     * @param tmap the tmap
     */
    public void enemyTileCollision(TileMap tmap) {
        // Empty out our current set of collided tiles
        collidedTiles.clear();
        for (Sprite x : enemies) {
            float spriteX = x.getX();
            float spriteY = x.getY();

            float tileWidth = tmap.getTileWidth();
            float tileHeight = tmap.getTileHeight();
            enemyMiddleLeftCollision(x, tmap, spriteX, spriteY, tileWidth, tileHeight);
            enemyMiddleRightCollision(x, tmap, spriteX, spriteY, tileWidth, tileHeight);
        }

    }

    private void enemyMiddleLeftCollision(Sprite sprite, TileMap tileMap, float spriteX, float spriteY, float tileWidth, float tileHeight) {
        int xtile = (int) (spriteX / tileWidth);
        int ytile = (int) (spriteY / tileHeight);

        Tile tl = tileMap.getTile(xtile, ytile);

        if (tl != null && tl.getCharacter() != '.') {
            sprite.setVelocityX(sprite.getVelocityX() * -1);
        }
        collidedTiles.add(tl);
    }

    private void enemyMiddleRightCollision(Sprite sprite, TileMap tileMap, float spriteX, float spriteY, float tileWidth, float tileHeight) {
        int xtile = (int) ((spriteX + sprite.getWidth()) / tileWidth);
        int ytile = (int) (spriteY / tileHeight);

        Tile tl = tileMap.getTile(xtile, ytile);

        if (tl != null && tl.getCharacter() != '.') {
            sprite.setVelocityX(sprite.getVelocityX() * -1);
        }
        collidedTiles.add(tl);
    }


    private void checkMiddleLeftCollision(Sprite sprite, TileMap tileMap, float spriteX, float spriteY, float tileWidth, float tileHeight) {
        boolean tileCollision = false;
        int xtile = (int) (spriteX / tileWidth);
        int ytile = (int) (spriteY / tileHeight);

        int xtileBelow = (int) ((spriteX + sprite.getWidth()) / tileWidth);
        int ytileBelow = (int) ((spriteY + sprite.getHeight()) / tileHeight);

        Tile tileBelow = tileMap.getTile(xtileBelow, ytileBelow);
        Tile tl = tileMap.getTile(xtile, ytile);

        if (tl != null && tl.getCharacter() != '.') {
            if (tileBelow != null && tileBelow.getCharacter() == '.') {
                sprite.setX(xtile * tileWidth + tileWidth);
                sprite.setY(spriteY + GRAVITY);
            } else {
                sprite.setX(xtile * tileWidth + tileWidth);
                sprite.stop();

            }
            tileCollision = true;
            collidedTiles.add(tl);
        }
        if (tileCollision) {
            playerMoving = false;
        } else
            playerMoving = true;

    }

    private void checkMiddleRightCollision(Sprite sprite, TileMap tileMap, float spriteX, float spriteY, float tileWidth, float tileHeight) {

        boolean tileCollisionTR = false;
        int xtile = (int) ((spriteX + sprite.getWidth()) / tileWidth);
        int ytile = (int) (spriteY / tileHeight);

        int xtileBelowRight = (int) (spriteX / tileWidth);
        int ytileBelowRight = (int) ((spriteY + sprite.getHeight()) / tileHeight);

        Tile tileBelowRight = tileMap.getTile(xtileBelowRight, ytileBelowRight);
        Tile tr = tileMap.getTile(xtile, ytile);

        if (tr != null && tr.getCharacter() != '.') {
            if (tileBelowRight != null && tileBelowRight.getCharacter() == '.') {
                sprite.setX(xtile * tileWidth - tileWidth);
                sprite.setY(spriteY + GRAVITY);
                player.setVelocityX(0.0f);
            } else {
                sprite.setX(xtile * tileWidth - tileWidth);
                sprite.stop();
            }
            tileCollisionTR = true;
            collidedTiles.add(tr);
        }
        playerMovingRight = !tileCollisionTR;
    }


    private void checkBottomLeftCollision(Sprite sprite, TileMap tileMap, float spriteX, float spriteY, float tileWidth, float tileHeight) {
        int xtile = (int) ((spriteX + (tileWidth / 2)) / tileWidth);
        int ytile = (int) ((spriteY + sprite.getHeight()) / tileHeight);
        Tile bl = tileMap.getTile(xtile, ytile);
        Tile tileAbove = tileMap.getTile(xtile, ytile - 1);

        if (bl != null && bl.getCharacter() != '.' && (tileAbove == null || tileAbove.getCharacter() == '.')) {

            sprite.setY(ytile * tileHeight - sprite.getHeight());
            sprite.stop();
            onGround = true;
        } else {
            sprite.setX(spriteX + GRAVITY);
        }
        collidedTiles.add(bl);
    }

    private void checkPlayerCollision(Sprite sprite, TileMap tileMap, TileMap tmap, float spriteX, float spriteY, float tileWidth, float tileHeight) {
        int xtile = (int) ((spriteX + (tileWidth / 2)) / tileWidth);
        int ytile = (int) (spriteY / tileHeight);
        Tile pc = tileMap.getTile(xtile, ytile);

        if (pc != null && pc.getCharacter() == '$' && level == 1) {
            PlayFilter sound = new PlayFilter("sounds/activeWayPoint.wav");
            sound.start();
            tileMap.setTileChar('£', xtile, ytile);

            //Location of the door halfway into the level
            tmap.setTileChar('.', 58, 15);
            tmap.setTileChar('.', 58, 14);
            tmap.setTileChar('.', 58, 13);

        }
        if (pc != null && pc.getCharacter() == 'o' && level == 1) {
            level = 2;
            endLevel();

        }
        collidedTiles.add(pc);
    }

    private void checkAboveCollision(Sprite sprite, TileMap tileMap, float spriteX, float spriteY, float tileWidth, float tileHeight) {
        int xtile = (int) ((spriteX + (tileWidth / 2)) / tileWidth);
        int ytile = (int) ((spriteY - sprite.getHeight()) / tileHeight);
        Tile ab = tileMap.getTile(xtile, ytile);

        tileAbovePlayer = ab != null && ab.getCharacter() != '.';
        collidedTiles.add(ab);
    }

/*    Below is an attempt to make my vertical collision work properly and not just be a flag that doesnt allow the player to jump.
    this code has issues too though as if the tile is only 1 tile in height the player will phase through it.*/

/*    private void checkAboveCollision(Sprite sprite, TileMap tileMap, float spriteX, float spriteY, float tileWidth, float tileHeight) {
        int xtile = (int) ((spriteX + (tileWidth / 2)) / tileWidth);
        int ytile = (int) ((spriteY - sprite.getHeight()) / tileHeight);
        Tile ab = tileMap.getTile(xtile, ytile);

        if (ab != null && ab.getCharacter() != '.' && sprite.getVelocityY() < 0) {
            sprite.setY((ytile + 1) * tileHeight);
            sprite.setVelocityY(0);
            TileAbovePlayer = true;
        } else {
            TileAbovePlayer = false;
        }
        collidedTiles.add(ab);
    }*/

    /**
     * Override of the keyPressed event defined in GameCore to catch our
     * own events
     *
     * @param e The event that has been generated
     */
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if(e.getKeyCode() == KeyEvent.VK_ESCAPE){
            System.exit(0);
        }
        if (!gameOverPlayed) {
            switch (keyCode) {
                case KeyEvent.VK_ESCAPE:
                    System.exit(0);
                    break;

                case KeyEvent.VK_RIGHT:
                case KeyEvent.VK_D:
                    rightKeyPressed = true;
                    break;

                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_A:
                    leftKeyPressed = true;
                    break;

                case KeyEvent.VK_UP:
                case KeyEvent.VK_W:
                    jump = true;
                    break;

                case KeyEvent.VK_EQUALS:
                    debug = !debug;
                    break;

                case KeyEvent.VK_R: // Resets the game in case the user gets stuck
                    initialiseGame();
                    break;

                case KeyEvent.VK_SPACE:
                    playerAttack();
                    Sound sound = new Sound("sounds/ArrowFly.wav");
                    sound.start();
                    break;

                case KeyEvent.VK_X:
                    if (level == 1)
                        player.setPosition(7800, 960);

                    break;

                case KeyEvent.VK_2: // Increase volume
                    adjustBackgroundMusicVolume(0.1f);
                    break;

                case KeyEvent.VK_1: // lower volume
                    adjustBackgroundMusicVolume(-0.1f);
                    break;

                default:
                    break;
            }
        }

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (debug) {
                    if (player.getX() < ((float) SCREEN_WIDTH / 2)) {
                        player.setX(e.getX());
                        player.setY(e.getY());
                    } else {
                        player.setX(e.getX() - xo);
                        player.setY(e.getY());
                    }
                }
            }
        });
    }

    public void keyReleased(KeyEvent e) {

        int keyCode = e.getKeyCode();

        switch (keyCode) {
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                rightKeyPressed = false;
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                leftKeyPressed = false;
                break;
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                jump = false;
                break;
            default:
                break;
        }

        e.consume();
    }

    private void playerAttack() {
        int arrowX;
        int arrowY = (int) player.getY();
        if (lastKeyLeft) {
            arrow.setVelocityX(-0.7f);
            arrowCollision.setVelocityX(-0.7f);
        } else {
            arrow.setVelocityX(+0.7f);
            arrowCollision.setVelocityX(+0.7f);
        }
        if (player.getX() > ((float) SCREEN_WIDTH / 2)) {
            arrowX = SCREEN_WIDTH / 2;
        } else {
            arrowX = (int) player.getX();
        }

        arrowCollision.setPosition(player.getX(), player.getY());
        arrow.setPosition(arrowX, arrowY);

    }

    private void endLevel() {
        level = 2;
        cleanUpGame();
        initTileMap("mapLevelTwo.txt", "mapLevelTwo.txt", "mapLevelTwo.txt");
        initBackgroundMusic("sounds/bgMusicLevel2.wav");
        initBackgroundAnimation("images/background/floor.png", "images/background/columns.png", "images/background/windows.png", "images/background/mountains.png");
        initSprites();
        initialiseGame();
    }

    private void endGameWin() {
        if (!gameOverPlayed) {
            cleanUpGame();
            Sound winSound = new Sound("sounds/gameWin.wav");
            winSound.start();
            gameOverPlayed = true;
        }


    }

    private void cleanUpGame() {
        background.clear();
        enemies.clear();
        coinSprite.clear();
        backgroundMusic.stopSound();
    }
}
