package bravo.media;

import java.io.InputStream;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class Sound {
    
    
    public static final int ADD_FILE = 0;
    public static final int EXTRACT_FILE = 1;
    public static final int EXTRACT_ALL_FILES = 2;
    public static final int RENAME_FILE = 3;
    public static final int DELETE_FILE = 4;
    public static final int MOVE_FILE = 5;
    public static final int COPY_FILE = 6;
    public static final int PASTE_FILE = 7;
    public static final int OPEN_FILE = 8;
    public static final int CREATE_EMPTY_FOLDER = 9;
    public static final int UPDATE_HIERARCHY = 10;
    public static final int ALTER_COMMENT = 11;
    public static final int CREATE_BAR_FILE = 12;
    public static final int OPEN_BAR_FILE = 13;
    public static final int CLOSE_BAR_FILE = 14;
    
    
    private static boolean playAction(int action) {
        return true;
    }
    
    
    private static String getActionFile(int action) {
        String file;
        switch (action) {
            case OPEN_BAR_FILE -> file = "audio6";
            case CLOSE_BAR_FILE -> file = "audio3";
            case CREATE_BAR_FILE -> file = "audio6";
            case ADD_FILE -> file = "audio2";
            case EXTRACT_FILE -> file = "audio2";
            case EXTRACT_ALL_FILES -> file = "audio2";
            case COPY_FILE -> file = "audio2";
            case PASTE_FILE -> file = "audio2";
            case RENAME_FILE -> file = "audio2";
            case DELETE_FILE -> file = "audio2";
            case MOVE_FILE -> file = "audio2";
            case OPEN_FILE -> file = "audio3";
            case ALTER_COMMENT -> file = "audio2";
            case CREATE_EMPTY_FOLDER -> file = "audio2";
            case UPDATE_HIERARCHY -> file = "audio6";
            default -> file = "audio2";
        }
        return file;
    }
    
    
    public static void play(int action){
        if (playAction(action)) {
            String file = getActionFile(action);
            try {
                String url = "/bravo/sounds/" + file + ".wav";
                try (InputStream inputStream = Sound.class.getResourceAsStream(url)) {
                    AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputStream);
                    Clip clip = AudioSystem.getClip();
                    clip.open(audioInputStream);
                    clip.start();
                }
            } catch (Exception ex){            
            }
        }
    }
    
    
}
