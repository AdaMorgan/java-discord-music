package pl.morgan.discordbot.music.message;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.ActionRow;
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
import java.util.stream.Stream;

public class PlayerMessageManager {
	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	private final TrackScheduler scheduler;
	private long message;

	public PlayerMessageManager(TrackScheduler scheduler) {
		this.scheduler = scheduler;

		if (scheduler.getChannel() instanceof MessageChannel channel)
			executor.execute(() -> message = channel.sendMessage(MessageCreateData.fromEditData(buildAudioMessage())).complete().getIdLong());
	}

	public void cleanup() {
		executor.shutdownNow();

		if (scheduler.getChannel() instanceof MessageChannel channel)
			channel.deleteMessageById(message).queue();
	}

	public void update() {
		if (scheduler.getChannel() instanceof MessageChannel channel)
			executor.execute(() -> channel.editMessageById(message, buildAudioMessage()).queue());
	}

	private String subAudioTrackByName(String str) {
		return str.length() > 45 ? str.substring(0, 45) + "..." : str;
	}

	private String queue() {
		return scheduler.queue.entrySet().stream()
				.filter(entry -> entry.getKey() >= scheduler.currentIndex + 1 && entry.getKey() < scheduler.currentIndex + 11)
				.map(entry -> String.format("**%d**. %s", entry.getKey(), subAudioTrackByName(entry.getValue().getInfo().title)))
				.collect(Collectors.joining("\n"));
	}

	private String history() {
		return scheduler.queue.entrySet().stream()
				.filter(entry -> entry.getKey() >= scheduler.currentIndex - 10 && entry.getKey() < scheduler.currentIndex)
				.map(entry -> String.format("**%d**. %s", entry.getKey(), subAudioTrackByName(entry.getValue().getInfo().title)))
				.sorted(Collections.reverseOrder()).collect(Collectors.joining("\n"));
	}

	private synchronized MessageEditData buildAudioMessage() {
		AudioTrack track = scheduler.player.getPlayingTrack();

		if (track == null) return MessageEditData.fromContent("Loading...");

		List<Button> buttons = Stream.of(
				ButtonType.STOP.getButton(),
				ButtonType.BACK.getButton(!beforeAudio()),
				ButtonType.RESUME.getButton().withStyle(getStyle(scheduler.player.isPaused())).withEmoji(getEmoji()),
				ButtonType.NEXT.getButton(!afterAudio()),
				ButtonType.ADD.getButton()
		).toList();

		List<Button> buttons1 = Stream.of(
				ButtonType.LOOP.getButton().withStyle(getStyle(!scheduler.isLooped())),
				ButtonType.SHUFFLE.getButton(),
				ButtonType.EQUALIZER.getButton()
		).toList();

		EmbedBuilder embedBuilder = new EmbedBuilder()
				.setTitle(track.getInfo().title, track.getInfo().uri)
				.addField("Queue:", queue(), true)
				.addField("History:", history(), true)
				.setThumbnail(track.getInfo().artworkUrl)
				.setColor(scheduler.manager.app.config.getColor())
				.setFooter(String.valueOf(scheduler.queue.size()));

		return new MessageEditBuilder()
				.setEmbeds(embedBuilder.build())
				.setComponents(ActionRow.of(buttons), ActionRow.of(buttons1))
				.build();
	}

	private boolean beforeAudio() {
		return scheduler.queue.keySet().stream().anyMatch(trackId -> trackId < scheduler.currentIndex);
	}

	private boolean afterAudio() {
		return scheduler.queue.keySet().stream().anyMatch(trackId -> trackId > scheduler.currentIndex);
	}

	private ButtonStyle getStyle(boolean state) {
		return state ? ButtonStyle.SECONDARY : ButtonStyle.PRIMARY;
	}

	private Emoji getEmoji() {
		return scheduler.player.isPaused() ? EmojiType.RESUME.fromUnicode() : EmojiType.PAUSE.fromUnicode();
	}
}
