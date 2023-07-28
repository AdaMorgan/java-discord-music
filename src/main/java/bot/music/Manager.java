package bot.music;

import com.github.topisenpai.lavasrc.deezer.DeezerAudioSourceManager;
import com.github.topisenpai.lavasrc.spotify.SpotifySourceManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import bot.main.Application;
import bot.main.Config;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class Manager {
	public final Application app;

	private final AudioPlayerManager playerManager;
	public final Map<Long, TrackScheduler> controllers = new ConcurrentHashMap<>();

	public Manager(Application app) {
		this.app = app;

		playerManager = new DefaultAudioPlayerManager();

		YoutubeAudioSourceManager youtubeManager = new YoutubeAudioSourceManager(true, null, null);
		youtubeManager.setPlaylistPageCount(1000);
		playerManager.registerSourceManager(youtubeManager);

		playerManager.registerSourceManager(SoundCloudAudioSourceManager.createDefault());

		playerManager.registerSourceManager(new SpotifySourceManager(null,
						getConfig().getSpotifyClientId(),
						getConfig().getSpotifyClientSecret(),
						"US",
						playerManager
				));

		playerManager.registerSourceManager(new DeezerAudioSourceManager(
						getConfig().getDeezerSourceManager()
				));

		playerManager.registerSourceManager(new TwitchStreamAudioSourceManager());

		AudioSourceManagers.registerRemoteSources(playerManager);
	}

	public static Config getConfig() {
		return Config.readFromFile("config.toml");
	}

	public AudioPlayer createAudioPlayer(TrackScheduler scheduler) {
		return Stream.of(playerManager.createPlayer())
				.peek(player -> player.setVolume(20))
				.peek(player -> player.addListener(scheduler))
				.findFirst()
				.orElse(null);
	}

	public AudioPlayerManager getPlayerManager() {
		return playerManager;
	}

	public Optional<TrackScheduler> getController(AudioChannel channel, boolean create, Member member) {
		return Optional.ofNullable(controllers.computeIfAbsent(channel.getGuild().getIdLong(), id -> createScheduler(channel, create, member)));
	}

	private TrackScheduler createScheduler(AudioChannel channel, boolean create, Member member) {
		return create ? new TrackScheduler(this, channel, member) : null;
	}
}