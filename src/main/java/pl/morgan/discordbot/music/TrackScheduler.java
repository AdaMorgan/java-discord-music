package pl.morgan.discordbot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import pl.morgan.discordbot.listener.StartupListener;
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
	public final PlayerMessageManager message;
	public final Member owner;
	private final AtomicInteger integer;
	private final StartupListener startup;
	private final Guild guild;
	public Equalizer equalizer;

	public Map<Integer, AudioTrack> queue;
	public int currentIndex = 0;
	private boolean looped, access = false;

	public TrackScheduler(Manager manager, AudioChannel channel, Member member) {
		this.manager = manager;
		this.channel = channel.getIdLong();
		this.guild = this.getChannel().getGuild();
		this.owner = member;
		this.integer = new AtomicInteger(1);
		this.player = manager.createAudioPlayer(this);
		this.message = new PlayerMessageManager(this);
		this.queue = new HashMap<>();
		this.equalizer = new Equalizer(this);
		this.startup = manager.app.startup;

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
		tracks.forEach(track -> queue.put(integer.getAndIncrement(), track));
		if (this.player.getPlayingTrack() == null) next();
		this.startup.update(this.guild);
		message.update();
	}

	public void add(String url) {
		this.manager.getPlayerManager().loadItem(url, new LoadResultHandler(this));
	}

	private void playTrack(AudioTrack track) {
		player.playTrack(track.makeClone());
	}

	public void next() {
		Optional.ofNullable(queue.get(++currentIndex)).ifPresentOrElse(this::playTrack, this::stop);
	}

	public void back() {
		Optional.ofNullable(queue.get(--currentIndex)).ifPresentOrElse(this::playTrack, this::stop);
	}

	public void equalizer() {

	}

	public void access() {
		if (this.owner != null) this.setAccess(!access);
		startup.update(this.guild);
	}

	private void setAccess(boolean state) {
		access = state;
	}

	public boolean isAccess() {
		return access;
	}

	public void looped() {
		if (player.getPlayingTrack() != null) this.setLooped(!looped);
		message.update();
	}

	private void setLooped(boolean state) {
		looped = state;
	}

	public boolean isLooped() {
		return looped;
	}

	public void shuffle() {
		List<AudioTrack> tracks = new ArrayList<>(queue.values());
		Collections.shuffle(tracks);
		queue = IntStream.range(0, queue.size())
				.boxed()
				.collect(Collectors.toMap(i -> i + 1, tracks::get, (key, value) -> value, LinkedHashMap::new));
	}

	public void pause() {
		if (player.getPlayingTrack() != null) player.setPaused(!player.isPaused());
		message.update();
	}

	public void stop() {
		getAudioManager().closeAudioConnection();
		this.manager.controllers.remove(getChannel().getGuild().getIdLong());
		message.cleanup();

		this.startup.update(this.guild);
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
		if (endReason.mayStartNext) {
			if (looped)
				this.playTrack(track);
			else
				next();
		}
	}

	@Override
	public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
		exception.printStackTrace();
	}
}

