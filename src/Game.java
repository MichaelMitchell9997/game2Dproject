
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
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

// Student ID: ???????


@SuppressWarnings("serial")


public class Game extends GameCore {


    // Useful game constants
    static int screenWidth = 1920;
    static int screenHeight = 1080;

    static int maxScreenWidth = (screenWidth * 5);

    public float gravity = 0.001f;

    // Game state flags
    boolean rightKeyPressed, leftKeyPressed, lastKeyRight, lastKeyLeft, jump = false;
    boolean debug = true;

    boolean onGround = true;
    boolean TileAbovePlayer = false;
    boolean playerMoving, playerMovingRight = false;


    // Game resources
    Animation playerIdle, playerRun, playerDeath, enemyKnightRun, enemyKnightIdle, bg1, bg2, bg3, bg4;
    private Image bgImage;

    Sprite player = null;

    private int playerHealth = 3;


    ArrayList<Sprite> background = new ArrayList<>();
    ArrayList<Sprite> enemies = new ArrayList<>();
    int[] enemyHealth = {1, 1, 1, 1};

    ArrayList<Tile> collidedTiles = new ArrayList<Tile>();

    TileMap tmap = new TileMap();    // Our tile map, note that we load it in init()
    TileMap tmapBackground = new TileMap();

    long total;                    // The score will be the total time elapsed since a crash

    //ImageIcon overlayImage = new ImageIcon("images/GameOver.png");

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
        gct.run(false, screenWidth, screenHeight);
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
        Sprite s;    // Temporary reference to a sprite

        // Load the tile map and print it out so we can check it is valid
        tmapBackground.loadMap("maps", "backgroundMap.txt");
        tmap.loadMap("maps", "map.txt");

        bgImage = loadImage("images/jungle_bg.png");

        //setSize(tmap.getPixelWidth()/4, tmap.getPixelHeight());
        setSize(1920, 1080);
        setVisible(true);

        //sprites for player character
        playerIdle = new Animation();
        playerIdle.loadAnimationFromSheet("images/Idle.png", 4, 1, 250);

        playerRun = new Animation();
        playerRun.loadAnimationFromSheet("images/Run.png", 7, 1, 150);

        playerDeath = new Animation();
        playerDeath.loadAnimationFromSheet("images/Dead.png", 6, 1, 450);

        //sprite for enemies
        enemyKnightRun = new Animation();
        enemyKnightRun.loadAnimationFromSheet("images/enemyRun.png", 7, 1, 450);

        enemyKnightIdle = new Animation();
        enemyKnightIdle.loadAnimationFromSheet("images/enemyIdle.png", 4, 1, 250);

        // Initialise the player with an animation
        player = new Sprite(playerIdle);

        enemies.add(new Sprite(enemyKnightRun));
        enemies.add(new Sprite(enemyKnightRun));
        enemies.add(new Sprite(enemyKnightRun));
        enemies.add(new Sprite(enemyKnightRun));

        bg1 = new Animation();
        bg1.loadAnimationFromSheet("images/background/grassRoad.png", 1, 1, 100);
        bg2 = new Animation();
        bg2.loadAnimationFromSheet("images/background/grasses.png", 1, 1, 100);
        bg3 = new Animation();
        bg3.loadAnimationFromSheet("images/background/trees.png", 1, 1, 100);
        bg4 = new Animation();
        bg4.loadAnimationFromSheet("images/background/jungle_bg.png", 1, 1, 100);


        background.add(new Sprite(bg4));
        background.add(new Sprite(bg3));
        background.add(new Sprite(bg2));
        background.add(new Sprite(bg1));

        Sound sound = new Sound("sounds/bgmusic.wav");
        new Thread(() -> {
            while (true) {
                sound.run();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }).start();

        initialiseGame();

        System.out.println(tmap);
    }

    /**
     * You will probably want to put code to restart a game in
     * a separate method so that you can call it when restarting
     * the game when the player loses.
     */
    public void initialiseGame() {
        total = 0;
        player.setPosition(200, 960);
        player.setVelocity(0, 0);
        player.show();
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
            s.setVelocityX(0.1f);
        }

    }

    /**
     * Draw the current state of the game. Note the sample use of
     * debugging output that is drawn directly to the game screen.
     */
    public void draw(Graphics2D g) {
        // Be careful about the order in which you draw objects - you
        // should draw the background first, then work your way 'forward'

        // First work out how much we need to shift the view in order to
        // see where the player is. To do this, we adjust the offset so that
        // it is relative to the player's position along with a shift
        int xo = 0;

        if (player.getX() < screenWidth / 2) {
            //xo = -(int) player.getX() + ((screenWidth - Math.round(player.getX())));
            //was trying to do some math where the players postion was minus the screen width until half way where it would then lock on
            //but turns out i just needed to have xo=0
            xo = 0;
        } else if (player.getX() > maxScreenWidth - screenWidth / 2) {
            xo = -(maxScreenWidth - screenWidth);
        } else {
            xo = -(int) player.getX() + screenWidth / 2;
        }
        //int xo = -(int)player.getX() + screenWidth/2;
        int yo = -100;

        g.setColor(Color.white);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.drawImage(bgImage, xo, yo, null);


        // Apply offsets to sprites then draw them
        for (Sprite s : background) {
            for (int i = 1; i < 7; i++) {
                s.setOffsets(xo + (screenWidth * i), yo);
                s.draw(g);
            }
            for (int i = 0; i < 2; i++) {
                s.setOffsets(xo - (screenWidth * i), yo);
                s.draw(g);
            }
        }

        // Apply offsets to tile map and draw  it
        tmapBackground.draw(g, xo, yo);
        tmap.draw(g, xo, yo);

        AffineTransform transform = new AffineTransform();
        transform.translate(Math.round(player.getX()), Math.round(player.getY()));

        if (leftKeyPressed || lastKeyLeft) {
            player.setScale(-1, 1);
            transform.translate(-player.getWidth(), 0);
        } else if (rightKeyPressed || lastKeyRight) {
            player.setScale(1, 1);
        }
        //   g.drawImage(player.getImage(), transform, null);

        player.setOffsets(xo, yo);
        //  player.draw(g);

        player.drawTransformed(g);
        for (Sprite s : enemies) {
            s.setOffsets(xo, yo);
            if (s.getVelocityX() < 0) {
                s.setScale(-1, 1);
            } else {
                s.setScale(1, 1);
            }
            s.drawTransformed(g);
        }

        // Show score and status information
        String msg = String.format("Score: %d", total / 100);
        g.setColor(Color.darkGray);
        g.drawString(msg,

                getWidth() - 100, 50);

        if (debug) {
            tmap.drawBorder(g, xo, yo, Color.black);

            g.setColor(Color.red);
            player.drawBoundingBox(g);
            for (Sprite s : enemies) {
                s.drawBoundingBox(g);
            }

            g.drawString(String.format("Player: %.0f,%.0f", player.getX(), player.getY()),
                    getWidth() - 100, 70);

            drawCollidedTiles(g, tmap, xo, yo);
        }
    }

    public void drawCollidedTiles(Graphics2D g, TileMap map, int xOffset, int yOffset) {
        if (collidedTiles.size() > 0) {
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
        player.setVelocityY(player.getVelocityY() + (gravity * elapsed));

        player.setAnimationSpeed(1.0f);
        for (Sprite s : enemies) {
            s.setAnimationSpeed(0.5f);
            if (playerHealth > 0) {
                s.setAnimation(enemyKnightRun);
            }
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

        if (jump) {
            if (onGround) {
                if (!TileAbovePlayer) {
                    player.setVelocityY(-0.4f); // Initial jump velocity
                    jump = false; // Reset the jump flag
                    onGround = false;
                    player.setVelocityY(player.getVelocityY());
                }

            } else {
                //player.setVelocityY(0.0f);
                player.setVelocityY(player.getVelocityY());
            }

        }


        backgroundScrollSpeed(elapsed);
        // Now update the sprites animation and position
        //player.update(elapsed);
        int i = 0;
        for (Sprite s : enemies) {
            s.update(elapsed);
            if (boundingBoxCollision(player, s)) {
                if (s.getVelocityX() < 0) {
                    s.setVelocityX(s.getVelocityX() * -1);
                    player.setX(player.getX() - 63);
                    playerHealth--;
                    enemyHealth[i]--;
                    System.out.println("Enemy Health: " + enemyHealth[i]);
                } else {
                    s.setVelocityX(s.getVelocityX() * -1);
                    player.setX(player.getX() + 63);
                    playerHealth--;
                    enemyHealth[i]--;
                    System.out.println("Enemy Health: " + enemyHealth[i]);
                }
            }
            i++;
            s.update(elapsed);
        }
        if (playerHealth <= 0) {
            playerDeathCycle();
            //playerHealth = 3;
        }
        player.update(elapsed);
        // Then check for any collisions that may have occurred
        handleScreenEdge(player, tmap, elapsed);
        enemyTileCollision(enemies, tmap);
        checkTileCollision(player, tmap, tmapBackground);

    }

    public void playerDeathCycle() {
        // Play the death animation for the player sprite
        player.setAnimation(playerDeath);

        // Stop all enemies moving
        for (Sprite s : enemies) {
            s.setVelocityX(0.0f);
            s.setAnimation(enemyKnightIdle);
        }

        // Pause for 2600 milliseconds so full death animation plays
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Reset the game after the pause
                playerHealth = 3;
                for (int i = 0; i < enemyHealth.length; i++) {
                    enemyHealth[i] = 1;
                }
                initialiseGame();
            }
        }, 2600); // Total duration of the death animation + 500ms pause
    }


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

    /**
     * Checks and handles collisions with the edge of the screen. You should generally
     * use tile map collisions to prevent the player leaving the game area. This method
     * is only included as a temporary measure until you have properly developed your
     * tile maps.
     *
     * @param s       The Sprite to check collisions for
     * @param tmap    The tile map to check
     * @param elapsed How much time has gone by since the last call
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
     * Override of the keyPressed event defined in GameCore to catch our
     * own events
     *
     * @param e The event that has been generated
     */
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (keyCode == KeyEvent.VK_ESCAPE) System.exit(0);

        if (keyCode == KeyEvent.VK_RIGHT) {
            rightKeyPressed = true;
        }
        if (keyCode == KeyEvent.VK_LEFT) {
            leftKeyPressed = true;
        }
        if (keyCode == KeyEvent.VK_SPACE) {
            jump = true;
        }
        if (keyCode == KeyEvent.VK_EQUALS) {
            debug = !debug;
        }
        if (keyCode == KeyEvent.VK_R) { //resets the game in case user gets stuck
            initialiseGame();
        }
        if (keyCode == KeyEvent.VK_D) {

        }
        if (keyCode == KeyEvent.VK_A) {//[TODO] add a way to attack
            //player.setAnimation(playerAttack);

        }
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (player.getX() < (screenWidth / 2)) {
                    player.setX(e.getX());
                    player.setY(e.getY());
                } else if (e.getX() > (screenWidth / 2)) {
                    player.setX(player.getX() + (e.getX() / 2));
                    player.setY(e.getY());
                }else {
                   // player.setX(player.getX() - (e.getX() + (screenWidth / 2)));
                    player.setY(e.getY());
                    System.out.println(e.getX());
                }
            }


        });
    }

    /**
     * Use the sample code in the lecture notes to properly detect
     * a bounding box collision between sprites s1 and s2.
     *
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
     * @param s    The Sprite to check collisions for
     * @param tmap The tile map to check
     */

    public void checkTileCollision(Sprite s, TileMap tmap, TileMap tmapbg) {
        // Empty out our current set of collided tiles
        collidedTiles.clear();

        float spriteX = s.getX();
        float spriteY = s.getY();

        float tileWidth = tmap.getTileWidth();
        float tileHeight = tmap.getTileHeight();

        //was having issues with code from the top left and top right collisions breaking each other so i had to split them up into different methods
        checkAboveCollision(s, tmap, spriteX, spriteY, tileWidth, tileHeight);
        checkBottomLeftCollision(s, tmap, spriteX, spriteY, tileWidth, tileHeight);
        checkPlayerCollison(s, tmapbg, tmap, spriteX, spriteY, tileWidth, tileHeight);
        checkMiddleLeftCollision(s, tmap, spriteX, spriteY, tileWidth, tileHeight);
        checkMiddleRightCollsion(s, tmap, spriteX, spriteY, tileWidth, tileHeight);


    }

    public void enemyTileCollision(ArrayList<Sprite> s, TileMap tmap) {
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
                sprite.setY(spriteY + gravity);
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

    private void checkMiddleRightCollsion(Sprite sprite, TileMap tileMap, float spriteX, float spriteY, float tileWidth, float tileHeight) {

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
                sprite.setY(spriteY + gravity);
                player.setVelocityX(0.0f);
            } else {
                sprite.setX(xtile * tileWidth - tileWidth);
                sprite.stop();
            }
            tileCollisionTR = true;
            collidedTiles.add(tr);
        }
        if (tileCollisionTR) {
            playerMovingRight = false;
        } else
            playerMovingRight = true;
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
            sprite.setX(spriteX + gravity);
        }
        collidedTiles.add(bl);
    }

    private void checkPlayerCollison(Sprite sprite, TileMap tileMap, TileMap tmap, float spriteX, float spriteY, float tileWidth, float tileHeight) {
        int xtile = (int) ((spriteX + (tileWidth / 2)) / tileWidth);
        int ytile = (int) (spriteY / tileHeight);
        Tile pc = tileMap.getTile(xtile, ytile);

        if (pc != null && pc.getCharacter() == '$') {
            //new Thread(() -> playSoundEffect("sounds/activeWayPoint.wav")).start();
            Sound sound = new Sound("sounds/activeWayPoint.wav");
            sound.start();
            tileMap.setTileChar('£', xtile, ytile);

            //Location of the end of level door
            tmap.setTileChar('.', 58, 15);
            tmap.setTileChar('.', 58, 14);
            tmap.setTileChar('.', 58, 13);

        }
        collidedTiles.add(pc);
    }

    private void checkAboveCollision(Sprite sprite, TileMap tileMap, float spriteX, float spriteY, float tileWidth, float tileHeight) {
        int xtile = (int) ((spriteX + (tileWidth / 2)) / tileWidth);
        int ytile = (int) ((spriteY - sprite.getHeight()) / tileHeight);
        Tile ab = tileMap.getTile(xtile, ytile);

        if (ab != null && ab.getCharacter() != '.') {
            TileAbovePlayer = true;
        } else {
            TileAbovePlayer = false;
        }
        collidedTiles.add(ab);
    }

    public void keyReleased(KeyEvent e) {

	/*	switch (key)
		{
			case KeyEvent.VK_ESCAPE : stop(); break;
			case KeyEvent.VK_UP     : flap = false; break;
			case KeyEvent.VK_RIGHT  : moveRight = false; break;
			default :  break;
		}*/
        int keyCode = e.getKeyCode();

        if (keyCode == KeyEvent.VK_RIGHT) {
            rightKeyPressed = false;
        }
        if (keyCode == KeyEvent.VK_LEFT) {
            leftKeyPressed = false;
        }
        if (keyCode == KeyEvent.VK_SPACE) {
            jump = false;
        }

        e.consume();
    }
}
