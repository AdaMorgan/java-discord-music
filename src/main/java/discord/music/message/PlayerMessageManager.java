package discord.music.message;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import discord.music.TrackScheduler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PlayerMessageManager implements AutoCloseable {
	private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
	private final TrackScheduler scheduler;
	public long id;

	public PlayerMessageManager(TrackScheduler scheduler) {
		this.scheduler = scheduler;

		create();
	}

	public void create() {
		if (scheduler.getChannel() instanceof MessageChannel channel)
			executor.execute(() -> this.id = channel.sendMessage(MessageCreateData.fromEditData(build())).complete().getIdLong());
	}

	public void cleanup() {
		if (scheduler.getChannel() instanceof MessageChannel channel)
			channel.deleteMessageById(id).queue();
	}

	public void update() {
		if (scheduler.getChannel() instanceof MessageChannel channel)
			executor.execute(() -> channel.editMessageById(this.id, build()).queue());
	}

	@Override
	public void close() {
		executor.shutdown();
	}

	private String queue() {
		return scheduler.queue.stream()
				.filter(track -> scheduler.queue.indexOf(track) > scheduler.currentIndex && scheduler.queue.indexOf(track) <= scheduler.currentIndex + 10)
				.map(track -> String.format("%s. %s", scheduler.queue.indexOf(track) + 1, getAudioTrackName(track)))
				.collect(Collectors.joining("\n"));
	}

	//TODO:
	private String history() {
		int startIndex = Math.max(scheduler.currentIndex - 10, 0);
		return IntStream.rangeClosed(startIndex, scheduler.currentIndex - 1)
				.mapToObj(i -> String.format("%s. %s", scheduler.queue.size() - i, getAudioTrackName(scheduler.queue.get(i))))
				.collect(Collectors.joining("\n"));
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

	private String checkAudioTrackType(AudioTrack track) {
		return track.isSeekable() ? "track" : "stream";
	}

	private String getAudioTrackArtwork(AudioTrack track) {
		return switch (track.getSourceManager().getSourceName()) {
			case "youtube" -> track.getInfo().artworkUrl;
			case "spotify" -> "spotify";
			case "soundcloud" -> "soundcloud";
			case "apple" -> "apple";
			case "twitch" -> "twitch";
			case "yandex" -> "yandex";
			default -> "audio";
		};
	}

	private EmbedBuilder getEmbedAudio(AudioTrack track) {
		return new EmbedBuilder()
				.setTitle(getAudioTrackName(track), track.getInfo().uri)
				.addField("Queue:", queue(), true)
				.addField("History:", history(), true)
				.setThumbnail(getAudioTrackArtwork(track))
				.setColor(ColorType.PRIMARY.toColor())
				.setFooter(String.valueOf(scheduler.queue.size()));
	}

	private List<ActionRow> getAudioButton(AudioTrack track) {
		return List.of(
				ActionRow.of(
						ButtonType.STOP.getButton(),
						ButtonType.BACK.getButton(scheduler.currentIndex == 0),
						ButtonType.RESUME.getButton().withStyle(getStyle(scheduler.player.isPaused())).withEmoji(getEmoji()).withDisabled(!track.isSeekable()),
						ButtonType.NEXT.getButton(scheduler.currentIndex == scheduler.queue.size() - 1),
						ButtonType.ADD.getButton()),
				ActionRow.of(
						ButtonType.LOOP.getButton().withStyle(getStyle(!scheduler.isLooped())).withDisabled(!track.isSeekable()),
						ButtonType.SHUFFLE.getButton().withDisabled(!track.isSeekable())
				));
	}

	private synchronized MessageEditData build() {
		return Optional.ofNullable(this.scheduler.player.getPlayingTrack())
				.map(track -> new MessageEditBuilder()
						.setContent(checkAudioTrackType(track))
						.setEmbeds(getEmbedAudio(track).build())
						.setComponents(getAudioButton(track))
						.build())
				.orElse(MessageEditData.fromContent("Loading..."));
	}
}
