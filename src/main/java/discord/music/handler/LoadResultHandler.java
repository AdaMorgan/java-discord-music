package discord.music.handler;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import discord.music.TrackScheduler;

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
	public void playlistLoaded(AudioPlaylist playlist) {
		this.scheduler.loadTrack(playlist.isSearchResult() ? Collections.singleton(playlist.getTracks().get(0)) : new ArrayList<>(playlist.getTracks()));
	}

	@Override
	public void noMatches() {

	}

	@Override
	public void loadFailed(FriendlyException exception) {
		exception.printStackTrace();
	}
}
