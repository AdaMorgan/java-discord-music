package bot.music.handler;

import bot.music.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;

public class LoadResultHandler implements AudioLoadResultHandler {
	public TrackScheduler scheduler;

	public LoadResultHandler(TrackScheduler queue) {
		this.scheduler = queue;
	}

	@Override
	public void trackLoaded(AudioTrack track) {
		this.scheduler.loadTrack(Collections.singleton(track));
	}

	@Override
	public void playlistLoaded(@NotNull AudioPlaylist playlist) {
		this.scheduler.loadTrack(playlist.isSearchResult() ? Collections.singleton(playlist.getTracks().get(0)) : new ArrayList<>(playlist.getTracks()));
	}

	@Override
	public void noMatches() {
		this.scheduler.manager.app.logger.atError().log("No matches");
	}

	@Override
	public void loadFailed(@NotNull FriendlyException exception) {
		exception.printStackTrace();
	}
}
