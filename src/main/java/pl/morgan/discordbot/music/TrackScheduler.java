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

public class TrackScheduler extends AudioEventAdapter {
	public final Manager manager;
	public final AudioPlayer player;

	private final long channel;
	private final PlayerMessageManager message;
	public final Long owner;
	public Equalizer equalizer;

	public List<AudioTrack> queue;
	public int currentIndex = 0;
	private boolean looped = false;

	public TrackScheduler(Manager manager, AudioChannel channel, Member member) {
		this.manager = manager;
		this.channel = channel.getIdLong();
		this.owner = member == null ? null : member.getIdLong();
		this.player = manager.createAudioPlayer(this);
		this.message = new PlayerMessageManager(this);
		this.queue = new LinkedList<>();
		this.equalizer = new Equalizer(this);

		getAudioManager().openAudioConnection(channel);
		channel.getGuild().getAudioManager().setSendingHandler(new SendHandler(player));

		manager.app.startup.updateMessage(channel.getGuild());
	}

	private AudioManager getAudioManager() {
		return getChannel().getGuild().getAudioManager();
	}

	public AudioChannel getChannel() {
		return manager.app.jda.getChannelById(AudioChannel.class, channel);
	}

	public Member getOwner() {
		return owner == null ? null : getChannel().getGuild().getMemberById(owner);
	}

	public void loadTrack(Collection<AudioTrack> tracks) {
		this.queue.addAll(tracks);
		if (this.player.getPlayingTrack() == null) next();
	}

	public void add(String url) {
		this.manager.getPlayerManager().loadItem(url, new LoadResultHandler(this));
	}

	private void playTrack(AudioTrack track) {
		player.playTrack(track.makeClone());
	}

	public void next() {
		currentIndex++;

		if(currentIndex >= queue.size()) {
			if(looped) {
				currentIndex = 0;
			}

			else {
				stop();
				return;
			}
		}

		playTrack(queue.get(currentIndex));
	}

	public void back() {
		currentIndex--;

		if(currentIndex < 0) {
			if(looped) {
				currentIndex = queue.size() - 1;
			}

			else {
				stop();
				return;
			}
		}

		playTrack(queue.get(currentIndex));
	}

	public void equalizer() {

	}

	public void loop() {
		if (player.getPlayingTrack() != null) this.setLooped(!looped);
	}

	private void setLooped(boolean state) {
		looped = state;
		message.update();
	}

	public boolean isLooped() {
		return looped;
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

		manager.app.startup.updateMessage(getChannel().getGuild());
	}

	@Override
	public void onTrackStart(AudioPlayer player, AudioTrack track) {
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

