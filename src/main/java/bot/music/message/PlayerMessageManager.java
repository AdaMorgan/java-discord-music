package bot.music.message;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import bot.music.TrackScheduler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PlayerMessageManager implements AutoCloseable {
	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
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
			executor.execute(() -> channel.editMessageById(this.id, build()).complete());
	}

	@Override
	public void close() {
		executor.shutdown();
	}

	private String queue(int count, boolean reverse) {
		int startInclusive = scheduler.currentIndex + (reverse ? count * -1 : 1);
		int endInclusive = scheduler.currentIndex + (reverse ? -1 : count);

		if (startInclusive < 0) {
			startInclusive = 0;
		} else if (endInclusive >= scheduler.queue.size()) {
			endInclusive = scheduler.queue.size() - 1;
		}

		return IntStream.rangeClosed(startInclusive, endInclusive)
				.mapToObj(index -> String.format("%s\\. %s", index + 1, sub(getAudioTrackName(scheduler.queue.get(index)))))
				.sorted(reverse ? Comparator.reverseOrder() : Comparator.naturalOrder())
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
		return sub(track.getSourceManager().getSourceName().equals("youtube") ? track.getInfo().title : formatAudioTrackName(track));
	}

	private String sub(@NotNull String str) {
		return str.length() > 45 ? str.substring(0, 45) + "..." : str;
	}

	@NotNull
	private String checkAudioTrackType(@NotNull AudioTrack track) {
		return track.isSeekable() ? "playlist" : "stream";
	}

	@NotNull
	private String getRichCustomEmoji(@NotNull EmojiType type) {
		return scheduler.getChannel().getGuild().getEmojiById(type.getCode()).getImageUrl();
	}

	private String owner(AudioTrack track) {
		return String.format("%s | %s", scheduler.owner.getUser().getName(), checkAudioTrackType(track));
	}

	private String getImageURI(@NotNull AudioTrack track) {
		return switch (track.getSourceManager().getSourceName()) {
			case "youtube" -> getRichCustomEmoji(EmojiType.YOUTUBE);
			case "spotify" -> getRichCustomEmoji(EmojiType.SPOTIFY);
			case "soundcloud" -> getRichCustomEmoji(EmojiType.SOUNDCLOUD);
			case "deezer" -> getRichCustomEmoji(EmojiType.DEEZER);
			case "apple" -> getRichCustomEmoji(EmojiType.APPLE);
			case "twitch" -> getRichCustomEmoji(EmojiType.TWITCH);
			case "yandex" -> "yandex";
			default -> getRichCustomEmoji(EmojiType.MUSIC);
		};
	}

	private EmbedBuilder getEmbedQueue(AudioTrack track) {
		return Optional.ofNullable(scheduler)
				.filter(controller -> controller.queue.size() > 0 || !track.isSeekable())
				.map(controller -> setEmbedQueue(track))
				.orElse(createEmbedQueue(track));
	}

	private EmbedBuilder setEmbedQueue(AudioTrack track) {
		if (scheduler.isList())
			return createEmbedQueue(track)
					.addField("Queue:", queue(10, false), true)
					.addField("History:", queue(10, true), true);
		else
			return createEmbedQueue(track)
					.addField("Next:", queue(1, false), true)
					.addField("Prev:", queue(1, true), true);
	}

	@NotNull
	private EmbedBuilder createEmbedQueue(AudioTrack track) {
		return new EmbedBuilder()
				.setAuthor(owner(track), null, scheduler.owner.getUser().getAvatarUrl())
				.setTitle(track.getInfo().title, track.getInfo().uri)
				.setThumbnail(getImageURI(track))
				.setColor(ColorType.CTA.toColor())
				.setFooter("ID: " + this.id);
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
						ButtonType.LOOP_PLAYLIST.getButton().withStyle(getStyle(!scheduler.isLoopQueue())).withDisabled(!track.isSeekable()),
						ButtonType.SHUFFLE.getButton().withDisabled(!track.isSeekable()),
						ButtonType.LIST.getButton().withStyle(getStyle(!scheduler.isList()))
				));
	}

	private synchronized MessageEditData build() {
		return Optional.ofNullable(this.scheduler.player.getPlayingTrack())
				.map(track -> new MessageEditBuilder()
						.setContent("")
						.setEmbeds(getEmbedQueue(track).build())
						.setComponents(getAudioButton(track))
						.build())
				.orElse(MessageEditData.fromContent("Loading..."));
	}
}