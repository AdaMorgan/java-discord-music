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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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

	private String listQueue(List<? extends AudioTrack> queue, AudioTrack track) {
		return queue.stream()
				.skip(queue.indexOf(track) + 1)
				.limit(9)
				.map(audioTrack -> String.format("%s. %s", queue.indexOf(audioTrack) + 1, subAudioTrackByName(audioTrack.getInfo().title)))
				.collect(Collectors.joining("\n"));
	}

	private String historyQueue(List<? extends AudioTrack> queue, AudioTrack track) {
		int startIndex = Math.max(0, queue.indexOf(track) - 9);
		int endIndex = Math.min(queue.size(), queue.indexOf(track));
		List<? extends AudioTrack> subList = queue.subList(startIndex, endIndex);
		Collections.reverse(subList);
		return IntStream.range(0, subList.size())
				.mapToObj(index -> String.format("%s. %s", subList.size() - index + startIndex, subAudioTrackByName(subList.get(index).getInfo().title)))
				.collect(Collectors.joining("\n"));}

	public synchronized MessageEditData buildAudioMessage() {
		AudioTrack track = scheduler.getPlayer().getPlayingTrack();

		if (track == null) return MessageEditData.fromContent("Loading...");

		List<Button> buttons = Stream.of(
				ButtonType.STOP.getButton(),
				ButtonType.RESUME.getButton().withStyle(getStyle()).withLabel(getLabel()),
				ButtonType.ADD.getButton(),
				ButtonType.NEXT.getButton(scheduler.queue.size() == 0),
				ButtonType.LOOP.getButton(scheduler.queue.indexOf(track) > 0)
		).toList();

		EmbedBuilder embedBuilder = new EmbedBuilder()
				.setTitle(track.getInfo().title, track.getInfo().uri)
				.addField("Queue:", listQueue(scheduler.queue, track), true)
				.addField("History:", historyQueue(scheduler.queue, track), true)
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
