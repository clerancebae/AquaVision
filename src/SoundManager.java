import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.net.URL;

public class SoundManager {

    private static Clip music;
    private static FloatControl volume;

    public static void init() {
        try {
            URL soundUrl = SoundManager.class.getResource("/Child_Game_Bg_Music.wav");
            if (soundUrl == null) {
                System.err.println("Sound file not found in classpath");
                return;
            }
            AudioInputStream audio = AudioSystem.getAudioInputStream(soundUrl);

            music = AudioSystem.getClip();
            music.open(audio);
            audio.close();

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