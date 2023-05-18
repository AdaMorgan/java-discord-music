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

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TrackScheduler extends AudioEventAdapter {
	public final Manager manager;
	public final AudioPlayer player;

	private final long channel;
	private final PlayerMessageManager message;
	public final Member owner;
	private final AtomicInteger integer;
	public Equalizer equalizer;

	public HashMap<Integer, AudioTrack> queue;
	public int currentIndex = 0;
	private boolean isLooped = false;

	public TrackScheduler(Manager manager, AudioChannel channel, Member member) {
		this.manager = manager;
		this.channel = channel.getIdLong();
		this.owner = member;
		this.integer = new AtomicInteger(1);
		this.player = manager.createAudioPlayer(this);
		this.message = new PlayerMessageManager(this);
		this.queue = new HashMap<>();
		this.equalizer = new Equalizer(this);

		getAudioManager().openAudioConnection(channel);
		channel.getGuild().getAudioManager().setSendingHandler(new SendHandler(player));
	}

	private AudioManager getAudioManager() {
		return getChannel().getGuild().getAudioManager();
	}

	public AudioChannel getChannel() {
		return manager.app.jda.getChannelById(AudioChannel.class, channel);
	}

	public void loadTrack(Collection<AudioTrack> tracks) {
		tracks.forEach(track -> this.queue.put(integer.getAndIncrement(), track));
		if (this.player.getPlayingTrack() == null) next();
	}

	public void add(String url) {
		this.manager.getPlayerManager().loadItem(url, new LoadResultHandler(this));
	}

	private void playTrack(AudioTrack track) {
		player.playTrack(track.makeClone());
	}

	public void next() {
		Optional.ofNullable(this.queue.get(++currentIndex)).ifPresentOrElse(this::playTrack, this::stop);
	}

	public void back() {
		Optional.ofNullable(this.queue.get(--currentIndex)).ifPresentOrElse(this::playTrack, this::stop);
	}

	public void loop() {
		if (player.getPlayingTrack() != null) this.setLooped(!isLooped);
		message.update();
	}

	public void equalizer() {

	}

	private void setLooped(boolean state) {
		isLooped = state;
	}

	public boolean isLooped() {
		return isLooped;
	}

	public void shuffle() {
		List<AudioTrack> tracks = new ArrayList<>(queue.values());
		Collections.shuffle(tracks);
		List<Integer> keys = new ArrayList<>(queue.keySet());
		queue = IntStream.range(0, queue.size())
				.boxed()
				.collect(Collectors.toMap(keys::get, tracks::get, (key, value) -> value, LinkedHashMap::new));
	}

	public void pause() {
		if (player.getPlayingTrack() != null) player.setPaused(!player.isPaused());
		message.update();
	}

	public void stop() {
		getAudioManager().closeAudioConnection();
		this.manager.controllers.remove(getChannel().getGuild().getIdLong());
		message.update();
		message.cleanup();
	}

	private void remove() {
		this.queue.entrySet().removeIf(entry -> entry.getKey() == currentIndex - 11);
	}

	@Override
	public void onTrackStart(AudioPlayer player, AudioTrack track) {
		remove();
		message.update();
	}

	@Override
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		if (endReason.mayStartNext) next();
	}

	@Override
	public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
		exception.printStackTrace();
	}
}

