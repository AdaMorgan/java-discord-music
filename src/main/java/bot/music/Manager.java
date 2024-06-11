package bot.music;

import bot.main.Application;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import org.jetbrains.annotations.NotNull;

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
		this.playerManager = new ManagerProvider(app).registerDefaultAudioPlayerManager();
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

	public Optional<TrackScheduler> getController(@NotNull AudioChannel channel, boolean create, Member member) {
		return Optional.ofNullable(controllers.computeIfAbsent(channel.getGuild().getIdLong(), id -> createScheduler(channel, create, member)));
	}

	private TrackScheduler createScheduler(AudioChannel channel, boolean create, Member member) {
		return create ? new TrackScheduler(this, channel, member) : null;
	}
}