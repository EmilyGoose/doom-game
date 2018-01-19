/**
 * Player.java
 * Player c
 * Misha Larionov
 */

import java.awt.Rectangle;

public class Player {

    private int health;

    //Package private so I can just do player.posX and player.posY
    int posX;
    int posY;
    double viewAngle;
    Rectangle targetReticle;

    //Speeds and such
    private final double lookSpeed = Math.PI / 60; //Look sensitivity
    boolean preciseAim = false;
    private final int walkSpeed = (int)(Game.CUBE_SIZE / 10);

    //Movement variables and such
    private int angleChange = 0;
    private int movement = 0;
    private int strafe = 0;

    Player() {
        this.health = 100;
    }

    Player(int x, int y, double view) {
        this.posX = x;
        this.posY = y;
        this.viewAngle = view; //THIS IS IN RADIANS
        this.health = 100;
    }

    //Functions are package-private intentionally

    void addAngleChange(int change) {
        if (Math.abs(this.angleChange + change) <= 1) {
            this.angleChange += change;
        }
    }

    void addMovement(int change) {
        if (Math.abs(this.movement + change) <= 1) {
            this.movement += change;
        }
    }

    //Not implemented
    void addStrafe(int change) {
        if (Math.abs(this.strafe + change) <= 1) {
            this.strafe += change;
        }
    }

    double getLookSpeed() {
        if (preciseAim) {
            return lookSpeed / 6;
        } else {
            return lookSpeed;
        }
    }

    int getWalkSpeed() {
        return walkSpeed;
    }

    int getMovement() {
        return movement;
    }

    int getAngleChange() {
        return angleChange;
    }

    int getHealth() {
        return this.health;
    }

    void takeDamage(int dmg) {
        this.health -= dmg;
        if (this.health < 0) this.health = 0;
    }

    void heal(int h) {
        this.health += h;
        if (this.health > 100) this.health = 100;
    }
}
