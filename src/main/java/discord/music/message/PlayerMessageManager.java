package discord.music.message;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import discord.music.TrackScheduler;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

public class PlayerMessageManager implements AutoCloseable {
	private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
	private final TrackScheduler scheduler;
	private long id;

	public PlayerMessageManager(TrackScheduler scheduler) {
		this.scheduler = scheduler;

		create();
	}

	public void create() {
		if (scheduler.getChannel() instanceof MessageChannel channel)
			executor.execute(() -> this.id = channel.sendMessage(MessageCreateData.fromEditData(buildAudioMessage())).complete().getIdLong());
	}

	public void cleanup() {
		if (scheduler.getChannel() instanceof MessageChannel channel)
			channel.deleteMessageById(id).queue();
	}

	@Override
	public void close() {
		executor.shutdown();
	}

	public void update() {
		if (scheduler.getChannel() instanceof MessageChannel channel)
			executor.execute(() -> channel.editMessageById(this.id, buildAudioMessage()).queue());
	}

	private AudioTrack getAudioTrack() {
		return this.scheduler.player.getPlayingTrack();
	}

	private String queue() {
		return scheduler.queue.entrySet().stream()
				.filter(entry -> entry.getKey() >= scheduler.currentIndex + 1 && entry.getKey() < scheduler.currentIndex + 11)
				.map(entry -> String.format("**%s**. %s", entry.getKey(), getAudioTrackName(entry.getValue())))
				.collect(Collectors.joining("\n"));
	}

	private String history() {
		return scheduler.queue.entrySet().stream()
				.filter(entry -> entry.getKey() >= scheduler.currentIndex - 10 && entry.getKey() < scheduler.currentIndex)
				.map(entry -> String.format("**%s**. %s", entry.getKey(), getAudioTrackName(entry.getValue())))
				.sorted(Comparator.reverseOrder()).collect(Collectors.joining("\n"));
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

	private String formatAudioTrackName(AudioTrack track) {
		return String.format("%s - %s", track.getInfo().author, track.getInfo().title);
	}

	private String getAudioTrackName(AudioTrack track) {
		return subAudioTrackByName(track.getSourceManager().getSourceName().equals("youtube") ? track.getInfo().title : formatAudioTrackName(track));
	}

	private String subAudioTrackByName(String str) {
		return str.length() > 45 ? str.substring(0, 45) + "..." : str;
	}

	private String getAudioTrackArtwork(AudioTrack track) {
		return switch (track.getSourceManager().getSourceName()) {
			case "youtube" -> track.getInfo().artworkUrl;
			case "spotify" -> "spotify";
			case "soundcloud" -> "soundcloud";
			default -> throw new IllegalStateException("Unexpected value: " + track.getSourceManager().getSourceName());
		};
	}

	private EmbedBuilder embed(AudioTrack track) {
		return new EmbedBuilder()
				.setTitle(getAudioTrackName(track), track.getInfo().uri)
				.addField("Queue:", queue(), true)
				.addField("History:", history(), true)
				.setThumbnail(getAudioTrackArtwork(track))
				.setColor(ColorType.PRIMARY.toColor())
				.setFooter(String.valueOf(scheduler.queue.size()));
	}

	private List<ActionRow> buttons() {
		return List.of(
				ActionRow.of(
						ButtonType.STOP.getButton(),
						ButtonType.BACK.getButton(!beforeAudio()),
						ButtonType.RESUME.getButton().withStyle(getStyle(scheduler.player.isPaused())).withEmoji(getEmoji()),
						ButtonType.NEXT.getButton(!afterAudio()),
						ButtonType.ADD.getButton()),
				ActionRow.of(
						ButtonType.LOOP.getButton().withStyle(getStyle(!scheduler.isLooped())),
						ButtonType.SHUFFLE.getButton(),
						ButtonType.EQUALIZER.getButton()
				));
	}

	private synchronized MessageEditData buildAudioMessage() {
		return Optional.ofNullable(getAudioTrack())
				.map(track -> new MessageEditBuilder()
						.setContent("")
						.setEmbeds(embed(track).build())
						.setComponents(buttons())
						.build())
				.orElse(MessageEditData.fromContent("Loading..."));
	}
}
