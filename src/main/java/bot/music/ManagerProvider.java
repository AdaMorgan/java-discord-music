package bot.music;

import bot.main.Application;
import bot.main.Config;
import com.github.topisenpai.lavasrc.deezer.DeezerAudioSourceManager;
import com.github.topisenpai.lavasrc.spotify.SpotifySourceManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public class ManagerProvider {
    private final Config config;
    private final DefaultAudioPlayerManager player;

    public ManagerProvider(@NotNull Application app) {
        this.config = app.config;
        this.player = new DefaultAudioPlayerManager();
    }

    public DefaultAudioPlayerManager registerDefaultAudioPlayerManager() {
        return Stream.of(new YoutubeAudioSourceManager(true, this.config.getEmailUsername(), this.config.getEmailPassword()))
                .peek(youtube -> youtube.setPlaylistPageCount(1000))
                .peek(player::registerSourceManager)
                .peek(unused -> registerSpotifySourceManage())
                .peek(unused -> registerSoundCloudSourceManage())
                .peek(unused -> registerDeezerSourceManage())
                .peek(unused -> registerTwitchSourceManage())
                .peek(unused -> AudioSourceManagers.registerRemoteSources(this.player))
                .findFirst()
                .map(unused -> this.player).get();
    }

    public void registerSpotifySourceManage() {
        player.registerSourceManager(new SpotifySourceManager(null,
                this.config.getSpotifyClientId(),
                this.config.getSpotifyClientSecret(),
                "US",
                player
        ));
    }

    public void registerSoundCloudSourceManage() {
        player.registerSourceManager(SoundCloudAudioSourceManager.createDefault());
    }

    public void registerDeezerSourceManage() {
        player.registerSourceManager(new DeezerAudioSourceManager(this.config.getDeezerSourceManager()));
    }

    public void registerTwitchSourceManage() {
        player.registerSourceManager(new TwitchStreamAudioSourceManager());
    }
}
