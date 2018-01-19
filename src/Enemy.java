import org.w3c.dom.css.Rect;

import java.awt.*;

/**
 * Enemy.java
 * Main enemy for the doom game
 * Misha Larionov
 */
public class Enemy {

    private int health;

    //Package private so I can just do player.posX and player.posY
    int posX;
    int posY;

    //Sizes
    int height = (int)Math.floor(Game.CUBE_SIZE * (3.0/4));
    int width = Math.floorDiv(height, 3);

    long lastAttack = 0;

    //Hitbox
    Rectangle hitbox;

    //Speeds and such
    private int walkSpeed = (int)(Game.CUBE_SIZE / 40);

    Enemy() {
        this.health = 100;
    }

    Enemy(int x, int y) {
        this.posX = x;
        this.posY = y;
        this.health = 100;
    }

    //Returns whether or not the monster is dead
    //package-private functions are intentional
    boolean takeDamage(int d) {
        this.health -= d;
        return this.health <= 0;
    }

    public int getHealth() {
        return this.health;
    }

    public int getWalkSpeed() {
        return walkSpeed;
    }
}
