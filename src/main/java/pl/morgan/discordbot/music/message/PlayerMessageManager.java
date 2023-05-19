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

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Stream;

public class PlayerMessageManager {
	private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(0);
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
		if (scheduler.getChannel() instanceof MessageChannel channel) {
			executor.execute(() -> channel.editMessageById(message, buildAudioMessage()).complete());
		}
	}

	private String subAudioTrackByName(String str) {
		return str.length() > 45 ? str.substring(0, 42) + "..." : str;
	}

	private String queue() {
		StringBuilder builder = new StringBuilder();

		for(int i = 0; i < scheduler.queue.size(); i++) {
			var track = scheduler.queue.get(i);

			builder.append(String.format("**%d**. %s", i, subAudioTrackByName(track.getInfo().title))).append("\n");
		}

		return builder.toString();
	}

	private ButtonStyle getStyle(boolean state) {
		return state ? ButtonStyle.SECONDARY : ButtonStyle.PRIMARY;
	}

	private Emoji getEmoji() {
		return scheduler.player.isPaused() ? EmojiType.RESUME.get() : EmojiType.PAUSE.get();
	}

	private synchronized MessageEditData buildAudioMessage() {
		AudioTrack track = scheduler.player.getPlayingTrack();

		if (track == null) return MessageEditData.fromContent("Loading...");

		List<Button> buttons = Stream.of(
				ButtonType.STOP.getButton(),
				ButtonType.BACK.getButton(!scheduler.isLooped() && scheduler.currentIndex <= 0),
				ButtonType.RESUME.getButton().withStyle(getStyle(scheduler.player.isPaused())).withEmoji(getEmoji()),
				ButtonType.NEXT.getButton(!scheduler.isLooped() && scheduler.currentIndex >= scheduler.queue.size() - 1),
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
				.setThumbnail(track.getInfo().artworkUrl)
				.setColor(ColorType.PRIMARY.toColor())
				.setFooter(String.valueOf(scheduler.queue.size()));

		return new MessageEditBuilder()
				.setEmbeds(embedBuilder.build())
				.setComponents(ActionRow.of(buttons), ActionRow.of(buttons1))
				.build();
	}
}
