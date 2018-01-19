/**
 * Game.java
 * Doom-like game built in Java. ICS4UE final
 * Misha Larionov
 */

//Import statements
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Game {

    //Assign some world constants
    //These are all package-private intentionally
    static final int CUBE_SIZE = 256;
    //FOV must be even
    static final double FOV = 2 * Math.PI /6; //60 degrees

    //Height and width of the projection plane
    static final int PLANE_WIDTH = 1024;
    static final int PLANE_HEIGHT = 768;
    static final int PLANE_DISTANCE = 1028;

    //Game things (Board, player, etc...)
    static Player player;
    static byte[][] board;
    static ArrayList<Enemy> enemies;
    static ArrayList<HealthKit> healthKits;
    static ResourceLoader resources;
    private static final double ENEMY_CHANCE = 1.0/500;
    private static final double HEALTH_CHANCE = 1.0/1000;

    //Framerates and such
    private static final int MAX_FRAMES = 60;
    private static long lastFrame = 0;
    private static int gameState = 0;

    //Handle the mouse
    static boolean mouseClick;

    private static void playSound(File audioFile) {
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            DataLine.Info info = new DataLine.Info(Clip.class, audioStream.getFormat());
            Clip clip = (Clip) AudioSystem.getLine(info);
            clip.open(audioStream);
            clip.start();
        } catch (Exception e) {
            System.out.println("Sound failed to play");
        }
    }

    private static double getDistance(int x1, int y1, int x2, int y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    public static void main(String[] args) {

        //Read the world from a text file
        //Format: Line 1 is # of rows, line 2 is # of columns
        Scanner input = null;
        try {
            input = new Scanner(new File("res/board.txt"));
        } catch (FileNotFoundException e) {
            System.out.println("Board not found, cannot continue!");
            e.printStackTrace();
            System.exit(-1);
        }

        //Find board heights and widths
        int board_height = Integer.parseInt(input.nextLine());
        int board_width = Integer.parseInt(input.nextLine());

        //Initialize the board
        board = new byte[board_height][board_width];

        //Put the rows into the array
        for (int i = 0; i < board_height; i++) {
            byte[] line = input.nextLine().getBytes();
            board[i] = line;
        }

        //Load the resources
        resources = new ResourceLoader();

        //Player position. (0,0) is at top left
        player = new Player(CUBE_SIZE * 2 + 5, CUBE_SIZE * 2 + 32, 0);
        //todo create spawn point in map

        //Initialize the enemies
        enemies = new ArrayList<>();
        enemies.add(new Enemy(Game.CUBE_SIZE * 5 + 128, Game.CUBE_SIZE * 5 + 128));

        //Initialize the health kits
        healthKits = new ArrayList<>();

        GameWindow window = new GameWindow();

        //For damage and enemy spawns
        long lastDamaged = 0;
        long lastEnemySpawn = System.currentTimeMillis();

        //Set the game state to indicate
        Game.gameState = 1;
        //Main game loop
        do {
            //Move the player's view angle
            Game.player.viewAngle += Game.player.getAngleChange() * Game.player.getLookSpeed();
            Game.player.viewAngle = Game.player.viewAngle % (Math.PI * 2);

            //Bind the player's view angle to a range of 0 to 2pi
            if (Game.player.viewAngle < 0) {
                Game.player.viewAngle += Math.PI * 2;
            } else if (Game.player.viewAngle == Math.PI / 2 || Game.player.viewAngle == 3 * Math.PI / 2) {
                Game.player.viewAngle += Math.PI / 64;
            }

            //Check if there are enemies near the player and hurt them (Only once per second)
            for (Enemy e : enemies) {
                if (getDistance(player.posX, player.posY, e.posX, e.posY) <= Game.CUBE_SIZE / 4) {
                    if (System.currentTimeMillis() - e.lastAttack >= 1000) {
                        e.lastAttack = System.currentTimeMillis();
                        player.takeDamage((int) (Math.random() * 20));
                        Game.playSound(resources.getSound("damage"));
                    }
                }
            }


            //Check if there's a health kit the player can pick up
            ArrayList<HealthKit> usedKits = new ArrayList<>();
            for (HealthKit h : healthKits) {
                if (getDistance(player.posX, player.posY, h.posX, h.posY) <= Game.CUBE_SIZE / 4) {
                    usedKits.add(h);
                    player.heal(20);
                }
            }
            healthKits.removeAll(usedKits);

            //Check if the player clicked the mouse to fire the gun
            if (mouseClick) {
                mouseClick = false;

                //Play SFX
                Game.playSound(resources.getSound("gunshot"));

                //Get the rectangle
                Rectangle reticle = Game.player.targetReticle;

                for (Enemy e : enemies) {
                    try {
                        if (e.hitbox.intersects(reticle)) {
                            boolean damage = e.takeDamage(110);
                        }
                    } catch (NullPointerException err) {
                        System.out.println("Couldn't find enemy hitbox");
                    }
                }
            }

            //Move enemies towards the player
            for (Enemy e : enemies) {
                int xDist = Game.player.posX - e.posX;
                int yDist = Game.player.posY - e.posY;

                double angle;
                try {
                    angle = Math.atan(yDist / xDist);
                } catch (ArithmeticException err) {
                    angle = 0;
                }
                int xMove = (int)(e.getWalkSpeed() * Math.cos(angle));
                if (player.posX< e.posX) xMove *= -1;
                int yMove = (int)(e.getWalkSpeed() * Math.sin(angle));
                if (player.posY < e.posY) yMove *= -1;

                int newGridPosX = Math.floorDiv(e.posX + xMove, Game.CUBE_SIZE);
                int newGridPosY = Math.floorDiv(e.posY + yMove, Game.CUBE_SIZE);

                if ((char)Game.board[newGridPosY][newGridPosX] != 'X') { //todo pixel-perfect collision
                    e.posX += xMove;
                    e.posY += yMove;
                }
            }

            //Move the player 5 units on the diagonal formed by the view angle
            int movement = Game.player.getMovement() * Game.player.getWalkSpeed();
            int xChange = (int)Math.floor(movement * Math.cos(Game.player.viewAngle));
            int yChange = (int)Math.floor(movement * Math.sin(Game.player.viewAngle));

            //Stop the player from running through a wall
            int newGridPosX = Math.floorDiv(Game.player.posX + xChange, Game.CUBE_SIZE);
            int newGridPosY = Math.floorDiv(Game.player.posY + yChange, Game.CUBE_SIZE);
            if ((char)Game.board[newGridPosY][newGridPosX] != 'X') { //todo pixel-perfect collision
                Game.player.posX += xChange;
                Game.player.posY += yChange;
            }

            //Use a second ArrayList to avoid concurrent modification problems
            ArrayList<Enemy> flaggedForDeath = new ArrayList<>();
            //Clear out dead enemies
            for (Enemy e : enemies) {
                if (e.getHealth() <= 0) {
                    flaggedForDeath.add(e);
                }
            }
            enemies.removeAll(flaggedForDeath);

            //Spawn new enemies
            for (int y = 0; y < board.length; y++) {
                for (int x = 0; x < board[0].length; x++) {
                    if ((char)board[y][x] == 'E') {
                        //Spawn an enemy at least once per 5 seconds
                        if (Math.random() <= Game.ENEMY_CHANCE || System.currentTimeMillis() - lastEnemySpawn >= 5000) {
                            lastEnemySpawn = System.currentTimeMillis();
                            Enemy e = new Enemy(x * Game.CUBE_SIZE + Game.CUBE_SIZE / 2, y * Game.CUBE_SIZE + Game.CUBE_SIZE / 2);
                            Game.enemies.add(e);
                        }
                    } else if ((char)board[y][x] == 'H') {
                        if (Math.random() <= Game.HEALTH_CHANCE) {
                            HealthKit h = new HealthKit(x * Game.CUBE_SIZE + Game.CUBE_SIZE / 2, y * Game.CUBE_SIZE + Game.CUBE_SIZE / 2);
                            Game.healthKits.add(h);
                        }
                    }
                }
            }

            //Make the call to repaint the window
            window.repaint();

            //This happens after everything's done, making sure're not running at more than 60fps
            lastFrame = System.currentTimeMillis();
            if (System.currentTimeMillis() - lastFrame < 1000 / MAX_FRAMES) {
                try {
                    Thread.sleep(1000 / MAX_FRAMES - (System.currentTimeMillis() - lastFrame));
                } catch (Exception e) {
                    System.out.println("Thread could not sleep. Game may become unstable.");
                    System.exit(1);
                }
            }
        } while (Game.player.getHealth() > 0);

        //Update the game state
        Game.gameState = 2;

        window.repaint();

        Game.playSound(Game.resources.getSound("death"));

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    public static int getGameState() {
        return gameState;
    }
}