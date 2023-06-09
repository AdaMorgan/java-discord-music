package discord.music.handler;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import discord.music.TrackScheduler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

import java.util.ArrayList;
import java.util.Collections;

public class LoadResultHandler implements AudioLoadResultHandler {
	public TrackScheduler queue;

	public LoadResultHandler(TrackScheduler queue) {
		this.queue = queue;
	}

	@Override
	public void trackLoaded(AudioTrack track) {
		this.queue.loadTrack(Collections.singleton(track));
	}

	@Override
	public void playlistLoaded(AudioPlaylist playlist) {
		this.queue.loadTrack(playlist.isSearchResult() ? Collections.singleton(playlist.getTracks().get(0)) : new ArrayList<>(playlist.getTracks()));
	}

	@Override
	public void noMatches() {

	}

	@Override
	public void loadFailed(FriendlyException exception) {
		exception.printStackTrace();
		this.queue.play();
	}
}
