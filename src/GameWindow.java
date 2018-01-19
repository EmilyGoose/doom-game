/**
 * GameWindow.java
 * <p>
 * Misha Larionov
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.Hashtable;

public class GameWindow extends JFrame {

    private static final boolean DEBUG = false;
    private static final int DEBUG_OFFSET = 200;

    private static final Image wallTexture = Game.resources.getImage("wallTexture");

    GameWindow() {
        super("DoomGame");
        //This block of code makes it fullscreen
        GraphicsEnvironment gEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice screenDevice = gEnv.getDefaultScreenDevice();
        this.setUndecorated(true);
        this.setResizable(false);
        this.setFocusable(false);
        screenDevice.setFullScreenWindow(this);

        //320 by 200 for that real old-timey feel
        DisplayMode dm = new DisplayMode(Game.PLANE_WIDTH, Game.PLANE_HEIGHT, 32, 60);
        screenDevice.setDisplayMode(dm);
        setSize(new Dimension(dm.getWidth(), dm.getHeight()));
        validate();

        GamePanel gamePanel = new GamePanel();

        this.setFocusable(true);
        this.requestFocusInWindow();
        this.requestFocus();
        this.addKeyListener(gamePanel);
        this.addMouseListener(gamePanel);
        this.setFocusTraversalKeysEnabled(false); //Allows us to capture TAB key presses

        getContentPane().add(gamePanel);
        pack();

        this.setVisible(true);
    }

    //Inner class GamePanel
    static class GamePanel extends JPanel  implements KeyListener, MouseListener {

        private static boolean inBounds(int gridCoordX, int gridCoordY, byte[][] board) {
            return (gridCoordX >= 0 && gridCoordY >= 0 && gridCoordX < board[0].length && gridCoordY < board[0].length);
        }

        GamePanel() {
            this.setPreferredSize(new Dimension(Game.PLANE_WIDTH, Game.PLANE_HEIGHT));
        }

        @SuppressWarnings("Duplicates")
        private double[] castRay(double currentAngle, double slope, Graphics g) {

            double[] returnDoubles = new double[3];
            double dist = -1;
            boolean facingRight = (currentAngle <= Math.PI / 2) || (currentAngle >= (3 * Math.PI) / 2);

            if (facingRight) {
                for (int x = Game.player.posX; x < Game.board[0].length * Game.CUBE_SIZE; x++) {
                    //y = m(x-p)+q where (p,q) are the player coords

                    double y = slope * ((double) x - (double) Game.player.posX) + (double) Game.player.posY;

                    //Figure out which block we've intersected with
                    int gridPosX = Math.floorDiv(x, Game.CUBE_SIZE);
                    int gridPosY = Math.floorDiv((int) y, Game.CUBE_SIZE);

                    if (inBounds(gridPosX, gridPosY, Game.board) && (char) Game.board[gridPosY][gridPosX] == 'X') {
                        //Todo mark out of bounds as wall
                        //Use distance formula to find distance to the wall
                        dist = Math.sqrt(Math.pow(y - Game.player.posY, 2) + Math.pow(x - Game.player.posX, 2));

                        returnDoubles[1] = x;
                        returnDoubles[2] = y;

                        //Debug top-down drawing
                        if (DEBUG) {
                            g.setColor(Color.red);
                            g.drawRect(x, (int) y + DEBUG_OFFSET, 1, 1);
                        }

                        break; //I hate using break but I kind of have to here
                    }
                    //Debug top-down drawing
                    if (DEBUG) {
                        g.setColor(Color.gray);
                        g.drawRect(x, (int) y + DEBUG_OFFSET, 1, 1);
                    }
                    //*/
                }
            } else {
                for (int x = Game.player.posX; x > 0; x--) {
                    //y = m(x-p)+q where (p,q) are the player coords

                    double y = slope * ((double) x - (double) Game.player.posX) + (double) Game.player.posY;

                    //Figure out which block we've intersected with
                    int gridPosX = Math.floorDiv(x, Game.CUBE_SIZE);
                    int gridPosY = Math.floorDiv((int) y, Game.CUBE_SIZE);

                    if (inBounds(gridPosX, gridPosY, Game.board) && (char) Game.board[gridPosY][gridPosX] == 'X') {
                        //Todo mark out of bounds as wall
                        //Use distance formula to find distance to the wall
                        dist = Math.sqrt(Math.pow(y - Game.player.posY, 2) + Math.pow(x - Game.player.posX, 2));

                        returnDoubles[1] = x;
                        returnDoubles[2] = y;

                        //Debug top-down drawing
                        if (DEBUG) {
                            g.setColor(Color.red);
                            g.drawRect(x, (int) y + DEBUG_OFFSET, 1, 1);
                        }

                        break; //I hate using break but I kind of have to here
                    }
                    //Debug top-down drawing
                    if (DEBUG) {
                        g.setColor(Color.gray);
                        g.drawRect(x, (int) y + DEBUG_OFFSET, 1, 1);
                    }
                    //*/
                }
            }

            if (dist == -1) dist = Double.MAX_VALUE;
            returnDoubles[0] = dist;

            return returnDoubles;
        }

        @SuppressWarnings("Duplicates")
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            //Make sure we're actually in the game
            if (Game.getGameState() == 1) {
                final double playerViewAngle = Game.player.viewAngle;
                double currentAngle = playerViewAngle - Game.FOV / 2;

                //This method uses basic linear functions to figure out where the nearest wall is (Thanks Mr Shim)

                //I'm lazy
                double pi = Math.PI;

                //Loop through each angle increment and check how far the ray goes before hitting a wall
                //Debug top-down drawing
                if (DEBUG) {
                    for (int i = 0; i < Game.board[1].length; i++) {
                        for (int j = 0; j < Game.board.length; j++) {
                            g.setColor(Color.green);
                            g.drawRect(j * Game.CUBE_SIZE, i * Game.CUBE_SIZE + DEBUG_OFFSET, Game.CUBE_SIZE, Game.CUBE_SIZE);
                        }
                    }
                }

                //Iterate for each vertical column of pixels to display
                for (int i = 0; i <= Game.PLANE_WIDTH; i += 1) {

                    //Bring currentAngle into the domain of 0-2pi
                    currentAngle = currentAngle % (pi * 2);

                    //Find the slope of the line
                    double slope = Math.tan(currentAngle);

                    double dist = -1;

                    //Debug top-down drawing
                    if (DEBUG) {
                        g.setColor(Color.red);
                        g.drawRect(Game.player.posX - 5, Game.player.posY - 5 + DEBUG_OFFSET, 10, 10);
                    }

                    boolean facingRight = (currentAngle < pi / 2) || (currentAngle > (3 * pi) / 2);

                    double[] returnValues = castRay(currentAngle, slope, g);

                    dist = returnValues[0];

                    //This seemingly convoluted way of returning 3 values is so we can map textures
                    double x = Math.abs(returnValues[1]);
                    double y = Math.abs(returnValues[2]);

                    //Get rid of distortion caused by combining polar and cartesian coords
                    dist *= Math.cos(Math.abs(currentAngle - playerViewAngle));

                    //Draw the line on the screen for the vertical slice
                    int lineHeight = (int) Math.floor((Game.CUBE_SIZE / dist) * Game.PLANE_DISTANCE);

                    double imagePixel;
                    //Check if we're on the x or y axis
                    if (x % Game.CUBE_SIZE == 0) {
                        double slice = y % Game.CUBE_SIZE;
                        imagePixel = slice / Game.CUBE_SIZE * wallTexture.getWidth(this);
                    } else {
                        double slice = x % Game.CUBE_SIZE;
                        imagePixel = slice / Game.CUBE_SIZE * wallTexture.getWidth(this);
                    }

                    int sliceWidth = Math.floorDiv(wallTexture.getWidth(this), Game.CUBE_SIZE);

                    //Draw a section of the image
                    //g.drawImage(wallTexture, i, (Game.PLANE_HEIGHT/2 - lineHeight/2), 1, lineHeight, this);
//
                    g.drawImage(
                            wallTexture,
                            i,
                            (Game.PLANE_HEIGHT / 2 - lineHeight / 2),
                            i + 1,
                            (Game.PLANE_HEIGHT / 2 - lineHeight / 2) + lineHeight,
                            (int) imagePixel,
                            0,
                            (int) imagePixel + 1,
                            wallTexture.getHeight(this),
                            this

                    );

                    //*/
                    //Increment the current angle so we can move to checking the next one
                    currentAngle += Game.FOV / Game.PLANE_WIDTH;
                }

                //Iterate through all enemies in the game
                for (Enemy enemy : Game.enemies) {
                    //If an enemy is within the player's FOV, figure out the distance and angle from normal
                    //Checking within FOV:
                    // 1. Find the slope of the line and the x/y diff between enemy and player
                    // 2. Use trig to figure out angle

                    //Find the differences
                    int xDiff = enemy.posX - Game.player.posX;
                    int yDiff = enemy.posY - Game.player.posY;

                    //Casting to double to avoid lossy division
                    double slope = (double) yDiff / xDiff;

                    //Find the relative angle
                    double relativeAngle = Math.atan(slope);

                    //Bind the relativeAngle to pi, this fixes looking left
//                if (relativeAngle < 0) {
//                    relativeAngle += pi;
//                }

                    //Compare both angles to the origin and then each other to avoid issues with crossing it
                    double enemyReferenceAngle = relativeAngle;

                    //Fix angles to work with all 4 quadrants
                    if (xDiff < 0) {
                        enemyReferenceAngle = relativeAngle + pi;
                    }
                    if (yDiff < 0 && xDiff > 0) {
                        enemyReferenceAngle = pi * 2 + relativeAngle;
                    }

                    double angleDifference = enemyReferenceAngle - playerViewAngle;

                    if (DEBUG) {
                        g.setColor(Color.blue);
                        g.drawRect(enemy.posX - 5, enemy.posY - 5 + DEBUG_OFFSET, 10, 10);
                    }

                    int xCenterOffset = (int) Math.floor((angleDifference / Game.FOV) * Game.PLANE_WIDTH);

                    //Figure out how far the enemy is
                    double enemyDist = Math.sqrt(Math.pow(xDiff, 2) + Math.pow(yDiff, 2));
                    //Check if there's a wall closer than the enemy
                    double wallDist = castRay(enemyReferenceAngle, slope, g)[0];

                    if (enemyDist < wallDist) {
                        //Draw the enemy after figuring out dimensions
                        int enemyXPos = (Game.PLANE_WIDTH / 2 + xCenterOffset) - (enemy.width / 2);
                        int enemyHeight = (int) Math.floor((enemy.height / enemyDist) * Game.PLANE_DISTANCE);
                        int enemyWidth = (int) Math.floor((enemy.width / enemyDist) * Game.PLANE_DISTANCE);
                        //Draw the enemy touching the floor
                        int wallHeight = (int) Math.floor((Game.CUBE_SIZE / enemyDist) * Game.PLANE_DISTANCE);
                        int enemyYPos = Game.PLANE_HEIGHT / 2 + wallHeight / 2 - enemyHeight;
                        g.setColor(Color.red);
                        enemy.hitbox = new Rectangle(enemyXPos, enemyYPos, enemyWidth, enemyHeight);
                        g.drawImage(Game.resources.getImage("enemy"), enemyXPos, enemyYPos, enemyWidth, enemyHeight, this);
                    }
                }

                //Iterate through all health kits in the game
                for (HealthKit healthKit : Game.healthKits) {
                    //If a health kit is within the player's FOV, figure out the distance and angle from normal
                    //Checking within FOV:
                    // 1. Find the slope of the line and the x/y diff between enemy and player
                    // 2. Use trig to figure out angle

                    //Find the differences
                    int xDiff = healthKit.posX - Game.player.posX;
                    int yDiff = healthKit.posY - Game.player.posY;

                    //Casting to double to avoid lossy division
                    double slope = (double) yDiff / xDiff;

                    //Find the relative angle
                    double relativeAngle = Math.atan(slope);

                    //Bind the relativeAngle to pi, this fixes looking left
//                if (relativeAngle < 0) {
//                    relativeAngle += pi;
//                }

                    //Compare both angles to the origin and then each other to avoid issues with crossing it
                    double kitReferenceAngle = relativeAngle;

                    //Fix angles to work with all 4 quadrants
                    if (xDiff < 0) {
                        kitReferenceAngle = relativeAngle + pi;
                    }
                    if (yDiff < 0 && xDiff > 0) {
                        kitReferenceAngle = pi * 2 + relativeAngle;
                    }

                    double angleDifference = kitReferenceAngle - playerViewAngle;

                    if (DEBUG) {
                        g.setColor(Color.blue);
                        g.drawRect(healthKit.posX - 5, healthKit.posY - 5 + DEBUG_OFFSET, 10, 10);
                    }

                    int xCenterOffset = (int) Math.floor((angleDifference / Game.FOV) * Game.PLANE_WIDTH);

                    //Figure out how far the enemy is
                    double kitDist = Math.sqrt(Math.pow(xDiff, 2) + Math.pow(yDiff, 2));
                    //Check if there's a wall closer than the health pickup
                    double wallDist = castRay(kitReferenceAngle, slope, g)[0];

                    if (kitDist < wallDist) {
                        //Draw the enemy after figuring out dimensions
                        int kitXPos = (Game.PLANE_WIDTH / 2 + xCenterOffset) - (healthKit.width / 2);
                        int kitHeight = (int) Math.floor((healthKit.height / kitDist) * Game.PLANE_DISTANCE);
                        int kitWidth = (int) Math.floor((healthKit.width / kitDist) * Game.PLANE_DISTANCE);
                        //Draw the enemy touching the floor
                        int wallHeight = (int) Math.floor((Game.CUBE_SIZE / kitDist) * Game.PLANE_DISTANCE);
                        int kitYPos = Game.PLANE_HEIGHT / 2 + wallHeight / 2 - kitHeight;
                        g.setColor(Color.red);
                        g.fillRect(kitXPos, kitYPos, kitWidth, kitHeight);
                    }
                }

                //Draw the aiming reticle
                g.setColor(Color.green);
                g.drawRect(Game.PLANE_WIDTH / 2 - 5, Game.PLANE_HEIGHT / 2 - 5, 10, 10);
                Game.player.targetReticle = new Rectangle(Game.PLANE_WIDTH / 2 - 5, Game.PLANE_HEIGHT / 2 - 5, 10, 10);

                //Draw the health display
                Image healthImg = Game.resources.getImage("health");
                double scale = Game.PLANE_HEIGHT / 5.0 / healthImg.getHeight(this);
                g.drawImage(
                        healthImg,
                        0,
                        Game.PLANE_HEIGHT - (int) (healthImg.getHeight(this) * scale),
                        (int) (healthImg.getWidth(this) * scale),
                        (int) (healthImg.getHeight(this) * scale),
                        this
                );

                int fontSize = (int) Math.floor(scale * healthImg.getHeight(this) * 0.6);
                g.setColor(new Color(127, 0, 0));
                g.setFont(new Font("Mael", 0, fontSize));
                g.drawString(
                        Integer.toString(Game.player.getHealth()),
                        (int) Math.floor(healthImg.getWidth(this) * scale * 0.1),
                        Game.PLANE_HEIGHT - (int) ((healthImg.getHeight(this) * scale) - fontSize / 2) / 2
                );
            } else if (Game.getGameState() == 2) { //When gameState is 2, the game is over
                g.setColor(new Color(127, 0, 0));
                int fontSize = Math.floorDiv(Game.PLANE_HEIGHT, 10);
                g.setFont(new Font("Cracked Johnnie", 0, fontSize));
                g.drawString(
                        "GAME OVER",
                        Game.PLANE_WIDTH / 2 - 300,
                        Game.PLANE_HEIGHT /2 - fontSize / 2
                );
            }
        }

        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyPressed(KeyEvent e) {
            //Change the view angle of the player when A is held down
            if (e.getKeyChar() == 'A' || e.getKeyChar() == 'a') {
                Game.player.addAngleChange(-1);
            } else if (e.getKeyChar() == 'D' || e.getKeyChar() == 'd') {
                Game.player.addAngleChange(1);
            } else if (e.getKeyChar() == 'W' || e.getKeyChar() == 'w') {
                Game.player.addMovement(1);
            } else if (e.getKeyChar() == 'S' || e.getKeyChar() == 's') {
                Game.player.addMovement(-1);
            } else if (e.getKeyChar() == 'Q' || e.getKeyChar() == 'q') {
                Game.player.addStrafe(-1);
            } else if (e.getKeyChar() == 'E' || e.getKeyChar() == 'e') {
                Game.player.addStrafe(1);
            } else if (e.getKeyCode() == 16){ //shift key
                Game.player.preciseAim = true;
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            //Get rid of view angle changes when a key is released
            if (e.getKeyChar() == 'A' || e.getKeyChar() == 'a') {
                Game.player.addAngleChange(1);
            } else if (e.getKeyChar() == 'D' || e.getKeyChar() == 'd') {
                Game.player.addAngleChange(-1);
            } else if (e.getKeyChar() == 'W' || e.getKeyChar() == 'w') {
                Game.player.addMovement(-1);
            } else if (e.getKeyChar() == 'S' || e.getKeyChar() == 's') {
                Game.player.addMovement(1);
            } else if (e.getKeyChar() == 'Q' || e.getKeyChar() == 'q') {
                Game.player.addStrafe(1);
            } else if (e.getKeyChar() == 'E' || e.getKeyChar() == 'e') {
                Game.player.addStrafe(-1);
            } else if (e.getKeyCode() == 16){ //shift key
                Game.player.preciseAim = false;
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            Game.mouseClick = true;
        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }

}
