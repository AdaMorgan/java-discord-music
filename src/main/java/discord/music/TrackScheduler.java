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
	private boolean loopQueue = true;
	private boolean loopTrack, access = false;

	public TrackScheduler(Manager manager, AudioChannel channel, Member member) {
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

	public void loadTrack(Collection<AudioTrack> tracks) {
		tracks.forEach(track -> queue.add(integer.getAndIncrement(), track));
		if (this.player.getPlayingTrack() == null) play();
		this.startup.update(this.guild);
		message.update();
	}

	public void play() {
		loopQueue();
		loopTrack();

		message.update();
	}

	//TODO: loop the queue
	private void loopQueue() {
		if (loopQueue && currentIndex == queue.size() - 1)
			this.reloadQueue();
		else
			this.stop();
	}

	//TODO: loop the reload queue
	private void reloadQueue() {
		this.queue.clear();
	}

	//TODO: loop the track
	private void loopTrack() {

	}

	//TODO: make a limit on the incoming queue
	private int limit() {
		return 1000;
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
		message.update();
	}

	@Override
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		if (endReason.mayStartNext) play();
	}

	@Override
	public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
		exception.printStackTrace();
	}
}

