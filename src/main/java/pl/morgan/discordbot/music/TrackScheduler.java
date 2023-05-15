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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class TrackScheduler extends AudioEventAdapter {
	public final Manager manager;
	private final AudioPlayer player;

	private final long channel;
	private final PlayerMessageManager message;
	private final long owner;
	private final AtomicInteger integer;

	public HashMap<Comparable<?>, AudioTrack> queue;
	public int currentIndex = 0;

	public TrackScheduler(Manager manager, AudioChannel channel, Member member) {
		this.manager = manager;
		this.channel = channel.getIdLong();
		this.owner = member.getIdLong();
		this.integer = new AtomicInteger(1);

		this.player = manager.createAudioPlayer(this);

		this.message = new PlayerMessageManager(this);

		this.queue = new HashMap<>();

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

	public Comparable<?> getKey(AudioTrack track) {
		return queue.entrySet().stream()
				.filter(entry -> entry.getValue().equals(track))
				.map(Map.Entry::getKey)
				.findFirst()
				.orElse(null);
	}

	public long getOwner() {
		return this.owner;
	}

	public void loadTrack(Collection<AudioTrack> tracks) {
		tracks.forEach(track -> this.queue.put(integer.getAndIncrement(), track));
		if (this.player.getPlayingTrack() == null) nextAudio();
		message.update();
	}

	public void addAudio(String url) {
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
	}

	@Override
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		if (endReason.mayStartNext) nextAudio();
	}

	@Override
	public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
		exception.printStackTrace();
	}
}

