package discord.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import discord.listener.StartupListener;
import discord.music.handler.LoadResultHandler;
import discord.music.handler.SendHandler;
import discord.music.message.PlayerMessageManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TrackScheduler extends AudioEventAdapter {
	public final Manager manager;
	public final AudioPlayer player;

	private final AudioChannel channel;
	public final PlayerMessageManager message;
	public final Member owner;
	private final AtomicInteger integer;
	private final StartupListener startup;
	private final Guild guild;

	public List<AudioTrack> queue;
	public int currentIndex = 0;
	private boolean loopQueue, loopTrack, access = false;

	public TrackScheduler(@NotNull Manager manager, AudioChannel channel, Member member) {
		this.manager = manager;
		this.channel = channel;
		this.owner = member;
		this.guild = this.getChannel().getGuild();
		this.integer = new AtomicInteger(0);
		this.player = manager.createAudioPlayer(this);
		this.message = new PlayerMessageManager(this);
		this.queue = new ArrayList<>();
		this.startup = manager.app.startup;

		getAudioManager().openAudioConnection(channel);
		channel.getGuild().getAudioManager().setSendingHandler(new SendHandler(player));
	}

	public AudioManager getAudioManager() {
		return getChannel().getGuild().getAudioManager();
	}

	public AudioChannel getChannel() {
		return manager.app.jda.getChannelById(AudioChannel.class, channel.getIdLong());
	}

	public void loadTrack(@NotNull Collection<AudioTrack> tracks) {
		tracks.forEach(track -> queue.add(integer.getAndIncrement(), track));
		if (this.player.getPlayingTrack() == null) this.playTrack(queue.get(currentIndex));
		this.startup.update(this.guild);
		message.create();
	}

	public void play() {
		loopQueue();
		loopTrack();
	}

	//TODO: loop the queue
	private void loopQueue() {
		Optional.of(this)
				.filter(value -> loopTrack && currentIndex == queue.size() - 1)
				.ifPresentOrElse(controller -> this.playTrack(queue.get(this.currentIndex = 0)), this::stop);
	}

	private void loopTrack() {
		Optional.of(this)
				.filter(state -> getAudioManager().isConnected())
				.filter(state -> loopTrack)
				.ifPresentOrElse(state -> this.playTrack(queue.get(currentIndex)), this::next);
	}

	//TODO: make a limit on the incoming queue
	private int limit() {
		return 1000;
	}

	public void add(String url) {
		this.manager.getPlayerManager().loadItem(url, new LoadResultHandler(this));
	}

	private void playTrack(@NotNull AudioTrack track) {
		player.playTrack(track.makeClone());
	}

	public void next() {
		Optional.ofNullable(queue.get(++currentIndex)).ifPresentOrElse(this::playTrack, this::stop);
	}

	public void back() {
		Optional.ofNullable(queue.get(--currentIndex)).ifPresentOrElse(this::playTrack, this::stop);
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

	public void onLoopQueue() {
		if (player.getPlayingTrack() != null) this.setLoopQueue(!loopQueue);
		message.update();
	}

	public void setLoopQueue(boolean state) {
		this.loopQueue = state;
	}

	public boolean isLoopQueue() {
		return loopQueue;
	}

	public void onLoopTrack() {
		if (player.getPlayingTrack() != null) this.setLoopTrack(!loopTrack);
		message.update();
	}

	private void setLoopTrack(boolean state) {
		this.loopTrack = state;
	}

	public boolean isLoopTrack() {
		return loopTrack;
	}

	public void shuffle() {
		Collections.shuffle(queue);
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

	@Override
	public void onTrackStart(AudioPlayer player, AudioTrack track) {
		//TODO: ERROR: when using the player again
		message.update();
	}

	@Override
	public void onTrackEnd(AudioPlayer player, AudioTrack track, @NotNull AudioTrackEndReason endReason) {
		if (endReason.mayStartNext) play();
	}

	@Override
	public void onTrackException(AudioPlayer player, AudioTrack track, @NotNull FriendlyException exception) {
		exception.printStackTrace();
	}
}

