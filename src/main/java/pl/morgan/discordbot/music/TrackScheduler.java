package pl.morgan.discordbot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import pl.morgan.discordbot.music.handler.LoadResultHandler;
import pl.morgan.discordbot.music.handler.SendHandler;
import pl.morgan.discordbot.music.message.PlayerMessageManager;

import java.net.URI;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class TrackScheduler extends AudioEventAdapter {
	private static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(0);

	public final Manager manager;
	private final AudioPlayer player;

	private final long channel;
	private final PlayerMessageManager message;

	public final List<AudioTrack> queue;

	private final long owner;
	public ScheduledFuture<?> future;
	public int currentIndex = -1;

	public TrackScheduler(Manager manager, AudioChannel channel, Member member) {
		this.manager = manager;
		this.channel = channel.getIdLong();
		this.owner = member.getIdLong();

		this.player = manager.createAudioPlayer(this);

		this.message = new PlayerMessageManager(this);

		this.queue = new ArrayList<>();

		getAudioManager().openAudioConnection(channel);
		channel.getGuild().getAudioManager().setSendingHandler(new SendHandler(player));
	}

	private AudioManager getAudioManager() {
		return getChannel().getGuild().getAudioManager();
	}

	public AudioChannel getChannel() {
		return manager.app.jda.getChannelById(AudioChannel.class, channel);
	}

	public AudioPlayer getPlayer() {
		return this.player;
	}

	public long getOwner() {
		return this.owner;
	}

	public void loadTrack(Collection<AudioTrack> tracks) {
		this.queue.addAll(tracks);
		if (this.player.getPlayingTrack() == null) nextAudio();

		message.update();
	}

	public void addAudio(String url) {
		try {
			new URI(url);
		} catch(Exception e) {
			url = "ytsearch: " + url; //If the provided string is not a valid url search on YouTube
		}

		this.manager.getPlayerManager().loadItem(url, new LoadResultHandler(this));
	}

	public void nextAudio() {
		Optional.ofNullable(this.queue.get(currentIndex += 1)).ifPresentOrElse(player::playTrack, this::stopAudio);
	}

	public void backAudio() {
		Optional.ofNullable(this.queue.get(currentIndex -= 1)).ifPresentOrElse(player::playTrack, this::stopAudio);
	}

	public void resumeAudio() {
		if (getPlayer().getPlayingTrack() != null) getPlayer().setPaused(!isPaused());

		message.update();
	}

	public boolean isPaused() {
		return player.isPaused();
	}

	public void loopAudio(AudioTrack track) {
		//player.startTrack(track.makeClone(), true);
	}

	public void stopAudio() {
		getAudioManager().closeAudioConnection();
		message.cleanup();

		this.manager.controllers.remove(getChannel().getGuild().getIdLong());
	}

	@Override
	public void onTrackStart(AudioPlayer player, AudioTrack track) {
		message.update();

		if (future != null) { future.cancel(false); future = null; }
	}

	@Override
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		if (endReason.mayStartNext) nextAudio();

		if (player.getPlayingTrack() == null && this.queue.isEmpty())
			future = EXECUTOR.schedule(this::stopAudio, 5, TimeUnit.MINUTES);
	}

	@Override
	public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
		exception.printStackTrace();
	}
}

