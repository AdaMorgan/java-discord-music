package pl.morgan.discordbot.music.message;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import pl.morgan.discordbot.music.TrackScheduler;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PlayerMessageManager {
	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

	private final TrackScheduler scheduler;

	private long message;

	public PlayerMessageManager(TrackScheduler scheduler) {
		this.scheduler = scheduler;

		if(scheduler.getChannel() instanceof MessageChannel channel)
			executor.execute(() -> message = channel.sendMessage(MessageCreateData.fromEditData(buildAudioMessage())).complete().getIdLong());
	}

	public void cleanup() {
		executor.shutdownNow();

		if(scheduler.getChannel() instanceof MessageChannel channel)
			channel.deleteMessageById(message).queue();
	}

	public void update() {
		if (scheduler.getChannel() instanceof MessageChannel channel)
			executor.execute(() -> channel.editMessageById(message, buildAudioMessage()).complete());
	}

	private String subAudioTrackByName(String str) {
		return str.length() > 45 ? str.substring(0, 45) + "..." : str;
	}

	private String listQueue(List<? extends AudioTrack> queue, AudioTrack currentTrack) {
		int currentTrackIndex = queue.indexOf(currentTrack);
		int startIndex = Math.max(0, currentTrackIndex - 5);
		int endIndex = Math.min(queue.size() -1, currentTrackIndex);
		List<? extends AudioTrack> audioTracks = queue.subList(startIndex, endIndex + 1);

		return audioTracks.stream()
				.map(track -> (queue.indexOf(track) + 1) + "." + track.getInfo().title)
				.collect(Collectors.joining("/n"));
	}

	public synchronized MessageEditData buildAudioMessage() {
		AudioTrack track = scheduler.getPlayer().getPlayingTrack();

		if (track == null) {
			return MessageEditData.fromContent("Loading...");
		}

		List<Button> buttons = Stream.of(
				ButtonType.STOP.getButton(),
				ButtonType.RESUME.getButton().withStyle(getStyle()).withLabel(getLabel()),
				ButtonType.ADD.getButton(),
				ButtonType.NEXT.getButton(scheduler.queue.size() == 0),
				ButtonType.LOOP.getButton(scheduler.queue.indexOf(track) > 0)
		).toList();

		EmbedBuilder embedBuilder = new EmbedBuilder()
				.setTitle(track.getInfo().title, track.getInfo().uri)
				.appendDescription(listQueue(scheduler.queue, track))
				.setThumbnail(track.getInfo().artworkUrl)
				.setColor(scheduler.manager.app.config.getColor())
				.setFooter(String.valueOf(scheduler.queue.size()));

		return new MessageEditBuilder()
				.setContent("")
				.setEmbeds(embedBuilder.build())
				.setActionRow(buttons.toArray(new Button[0]))
				.build();
	}

	private ButtonStyle getStyle() {
		return scheduler.isPaused() ? ButtonStyle.SECONDARY : ButtonStyle.PRIMARY;
	}

	private String getLabel() {
		return scheduler.isPaused() ? "PAUSE" : "PLAY";
	}
}
