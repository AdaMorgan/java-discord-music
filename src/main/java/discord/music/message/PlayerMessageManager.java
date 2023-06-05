package discord.music.message;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import discord.music.TrackScheduler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

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

	//TODO: history is not working properly
	private String history() {
		int startIndex = Math.max(scheduler.currentIndex - 10, 0);
		return IntStream.rangeClosed(startIndex, scheduler.currentIndex - 1)
				.mapToObj(i -> String.format("%s. %s", scheduler.queue.size() - i, getAudioTrackName(scheduler.queue.get(i))))
				.collect(Collectors.joining("\n"));
	}

	private ButtonStyle getStyle(boolean state) {
		return state ? ButtonStyle.SECONDARY : ButtonStyle.PRIMARY;
	}

	@NotNull
	private Emoji getEmoji() {
		return scheduler.player.isPaused() ? EmojiType.RESUME.fromUnicode() : EmojiType.PAUSE.fromUnicode();
	}

	private String formatAudioTrackName(@NotNull AudioTrack track) {
		return String.format("%s - %s", track.getInfo().author, track.getInfo().title);
	}

	private String getAudioTrackName(@NotNull AudioTrack track) {
		return subAudioTrackByName(track.getSourceManager().getSourceName().equals("youtube") ? track.getInfo().title : formatAudioTrackName(track));
	}

	private String subAudioTrackByName(@NotNull String str) {
		return str.length() > 45 ? str.substring(0, 45) + "..." : str;
	}

	@NotNull
	private String checkAudioTrackType(@NotNull AudioTrack track) {
		return track.isSeekable() ? "track" : "stream";
	}

	@NotNull
	@Contract(pure = true)
	private String getAuthor() {
		return "";
	}

	private String getRichCustomEmoji(EmojiType type) {
		return scheduler.getChannel().getGuild().getEmojiById(type.getCode()).getImageUrl();
	}

	private String getAuthorUrl(@NotNull AudioTrack track) {
		return switch (track.getSourceManager().getSourceName()) {
			case "youtube" -> getRichCustomEmoji(EmojiType.YOUTUBE);
			case "spotify" -> getRichCustomEmoji(EmojiType.SPOTIFY);
			case "soundcloud" -> getRichCustomEmoji(EmojiType.SOUNDCLOUD);
			case "apple" -> "apple";
			case "twitch" -> "twitch";
			case "yandex" -> "yandex";
			default -> "audio";
		};
	}

	@NotNull
	private EmbedBuilder getEmbedAudio(AudioTrack track) {
		return new EmbedBuilder()
				.setAuthor(track.getSourceManager().getSourceName(), null, getAuthorUrl(track))
				.setTitle(getAudioTrackName(track), track.getInfo().uri)
				.addField("Queue:", queue(), true)
				.addField("History:", history(), true)
				.setColor(ColorType.PRIMARY.toColor())
				.setFooter(String.valueOf(scheduler.queue.size()));
	}

	@NotNull
	private List<ActionRow> getAudioButton(@NotNull AudioTrack track) {
		return List.of(
				ActionRow.of(
						ButtonType.STOP.getButton(),
						ButtonType.BACK.getButton(scheduler.currentIndex == 0),
						ButtonType.RESUME.getButton().withStyle(getStyle(scheduler.player.isPaused())).withEmoji(getEmoji()).withDisabled(!track.isSeekable()),
						ButtonType.NEXT.getButton(scheduler.currentIndex == scheduler.queue.size() - 1),
						ButtonType.ADD.getButton()),
				ActionRow.of(
						ButtonType.LOOP_TRACK.getButton().withStyle(getStyle(!scheduler.isLoopTrack())).withDisabled(!track.isSeekable()),
						ButtonType.LOOP_QUEUE.getButton().withStyle(getStyle(!scheduler.isLoopQueue())).withDisabled(!track.isSeekable()),
						ButtonType.SHUFFLE.getButton().withDisabled(!track.isSeekable())
				));
	}

	private synchronized MessageEditData build() {
		return Optional.ofNullable(this.scheduler.player.getPlayingTrack())
				.map(track -> new MessageEditBuilder()
						.setContent("")
						.setEmbeds(getEmbedAudio(track).build())
						.setComponents(getAudioButton(track))
						.build())
				.orElse(MessageEditData.fromContent("Loading..."));
	}
}
