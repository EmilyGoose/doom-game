/**
 * HealthKit.java
 * Health kit for doom game
 * Misha Larionov
 */

public class HealthKit {
    //Package private so I can just do player.posX and player.posY
    int posX;
    int posY;

    //Sizes
    int height = Math.floorDiv(Game.CUBE_SIZE, 10);
    int width = height;

    HealthKit(int x, int y) {
        this.posX = x;
        this.posY = y;
    }

}

