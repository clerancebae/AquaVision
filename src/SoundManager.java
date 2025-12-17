import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.File;

public class SoundManager {

    private static Clip music;
    private static FloatControl volume;

    public static void init() {
        try {
            AudioInputStream audio =
                    AudioSystem.getAudioInputStream(
                            new File("Child_Game_Bg_Music.wav"));

            music = AudioSystem.getClip();
            music.open(audio);

            volume = (FloatControl)
                    music.getControl(FloatControl.Type.MASTER_GAIN);

            music.loop(Clip.LOOP_CONTINUOUSLY);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void setVolume(float value) {
        if (volume != null) {
            volume.setValue(value);
        }
    }
}