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
	private boolean list;

	public List<AudioTrack> queue;
	public int currentIndex = 0;
	private boolean loopQueue, loopTrack, access = false;
	private boolean connection;

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
		this.connection = true;

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
		tracks.forEach(this::limit);
		if (this.player.getPlayingTrack() == null) this.playTrack(queue.get(currentIndex));
		this.startup.update(this.guild);
		this.message.update();
	}

	private void limit(AudioTrack track) {
		if (queue.size() < manager.app.config.getQueueLimit()) queue.add(integer.getAndIncrement(), track);
	}

	public void play() {
		loopQueue();
		if (connection) loopTrack();
	}

	private void loopQueue() {
		if (currentIndex == queue.size() - 1) {
			if (loopQueue)
				this.playTrack(queue.get(this.currentIndex = 0));
			else
				stop();
		}
	}

	private void loopTrack() {
		if (loopTrack)
			this.playTrack(queue.get(currentIndex));
		else
			next();
	}

	public String checkAsTag() {
		return owner.getUser().getAsTag().split("#")[1].equals("0000") ? owner.getUser().getAsTag().split("#")[0] : owner.getUser().getAsTag();
	}

	public boolean isSeekable() {
		return this.player.getPlayingTrack().isSeekable();
	}

	public void add(String url) {
		this.manager.getPlayerManager().loadItem(url, new LoadResultHandler(this));
		message.update();
		System.out.println(this.queue.size());
	}

	private void playTrack(@NotNull AudioTrack track) {
		player.playTrack(track.makeClone());
	}

	public boolean isConnection() {
		return connection;
	}

	public void next() {
		Optional.ofNullable(queue.get(++currentIndex)).ifPresentOrElse(this::playTrack, this::stop);
	}

	public void back() {
		Optional.ofNullable(queue.get(--currentIndex)).ifPresentOrElse(this::playTrack, this::stop);
	}

	public void list() {
		if (player.getPlayingTrack() != null) this.setList(!list);
		message.update();
	}

	public void setList(boolean state) {
		list = state;
	}

	public boolean isList() {
		return list;
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
		message.update();
	}

	public void pause() {
		if (player.getPlayingTrack() != null) player.setPaused(!player.isPaused());
		message.update();
	}

	public void stop() {
		getAudioManager().closeAudioConnection();
		this.manager.controllers.remove(getChannel().getGuild().getIdLong());
		message.cleanup();
		connection = false;

		this.startup.update(this.guild);
	}

	@Override
	public void onTrackStart(AudioPlayer player, AudioTrack track) {
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

