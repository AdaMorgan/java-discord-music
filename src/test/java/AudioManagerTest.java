import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import org.junit.Before;
import org.junit.Test;

public class AudioManagerTest {

    private SoundCloudAudioSourceManager manager;

    @Before
    public void register() {
        manager = new DefaultAudioPlayerManager();
    }

    @Test
    public void playTrack() {
        manager.shutdown();
    }

}
