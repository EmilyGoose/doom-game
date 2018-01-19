/**
 * ResourceLoader.java
 * Loads resources for the doom game
 * Misha Larionov
 */

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.util.Hashtable;
import java.util.Scanner;

public class ResourceLoader {

    private boolean ready = false;

    //Declare the hash tables
    private Hashtable<String, Image> images = new Hashtable<>();
    private Hashtable<String, File> sounds = new Hashtable<>();

    ResourceLoader() {
        try {
            Scanner input = new Scanner(new File("res/ImageLoadList.txt"));
            while (input.hasNext()) {
                String[] line = input.nextLine().split("//");
                try {
                    //Try to load the image
                    ImageIO.read(new File("res/" + line[1]));
                    images.put(line[0], ImageIO.read(new File("res/" + line[1])));
                    System.out.println(line[0] + " (" + line[1] + ") loaded");
                } catch (Exception e) {
                    //File doesn't exist
                    System.out.println(line[1] + " failed to load");
                }
            }
            input.close();
            input = new Scanner(new File("res/SoundLoadList.txt"));
            while (input.hasNext()) {
                String[] line = input.nextLine().split("//");
                try {
                    File audioFile = new File("res/" + line[1]);
                    sounds.put(line[0], audioFile);
                    System.out.println(line[0] + " (" + line[1] + ") loaded");
                } catch (Exception e) {
                    //File doesn't exist
                    System.out.println(line[1] + " failed to load");
                }
            }
        } catch (Exception E){
            System.out.println("Image load list could not be found or is not properly formatted! Cannot proceed.");
            System.exit(1);
        }
    }

    Image getImage(String name) {
        return images.get(name);
    }

    File getSound(String name) {
        return sounds.get(name);
    }

    boolean isReady() {
        return ready;
    }
}
