package bot.listener;

import bot.main.Application;
import bot.music.TrackScheduler;
import bot.music.message.ButtonType;
import bot.music.message.ColorType;
import bot.music.message.EmojiType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.http.HttpRequestEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class StartupListener extends ListenerAdapter {
	private final Application app;
	private final Map<Long, TrackScheduler> scheduler;
	public Map<Long, Long> message;

	public StartupListener(Application app) {
		this.app = app;
		this.message = new HashMap<>();
		this.scheduler = this.app.manager.controllers;
	}

	@Override
	public void onGuildJoin(@NotNull GuildJoinEvent event) {
		Stream.of(event.getGuild())
				.peek(this::setupGuild)
				.map(Guild::getCommunityUpdatesChannel)
				.forEach(this::sendMessageEmbed);
	}

	private void sendMessageEmbed(TextChannel channel) {
		if (channel != null) channel.sendMessageEmbeds(embedJoin().build()).queue();
	}

	@Override
	public void onHttpRequest(HttpRequestEvent event) {
		super.onHttpRequest(event);
	}

	@NotNull
	private EmbedBuilder embedJoin() {
		return new EmbedBuilder()
				.setAuthor("")
				.setThumbnail("")
				.setDescription("");
	}

	@Override
	public void onReady(@NotNull ReadyEvent event) {
		event.getJDA().getGuilds().forEach(this::setupGuild);
		event.getJDA().getPresence().setActivity(activity());
	}

	@NotNull
	private Activity activity() {
		return Activity.listening(String.format("music | %s", app.jda.getGuilds().size()));
	}

	private TrackScheduler getTrackScheduler(@NotNull Guild guild) {
		return scheduler.getOrDefault(guild.getIdLong(), null);
	}

	@Override
	public void onChannelDelete(@NotNull ChannelDeleteEvent event) {
		if (event.getChannel().getName().equals(app.config.getTextChannelByName())) setupGuild(event.getGuild());
	}

	public void update(Guild guild) {
		performForChannel(guild, channel -> channel.editMessageById(this.message.get(guild.getIdLong()), message(guild)).queue());
	}

	private void setupGuild(Guild guild) {
		performForChannel(guild, channel -> {
			channel.getIterableHistory().queue(messages -> messages.stream()
					.filter(message -> !message.getEmbeds().isEmpty())
					.forEach(message -> message.delete().queue()));

			setupMessage(guild, channel);
		});
	}

	@Override
	public void onMessageDelete(@NotNull MessageDeleteEvent event) {
		if (this.message.get(event.getGuild().getIdLong()) != null && event.getMessageIdLong() == this.message.get(event.getGuild().getIdLong()))
			setupMessage(event.getGuild(), event.getChannel());
	}

	private void setupMessage(Guild guild, @NotNull MessageChannel channel) {
		channel.sendMessage(MessageCreateData.fromEditData(message(guild))).queue(message -> this.message.put(guild.getIdLong(), message.getIdLong()));
	}

	private void performForChannel(@NotNull Guild guild, Consumer<MessageChannel> handler) {
		guild.getTextChannels().stream()
				.filter(channel -> channel.getName().equals(app.config.getTextChannelByName()))
				.findAny()
				.ifPresentOrElse(handler, () -> guild.createTextChannel(app.config.getTextChannelByName()).queue(handler));
	}

	private Color color(Guild guild) {
		return Optional.ofNullable(getTrackScheduler(guild))
				.map(scheduler -> ColorType.DESTRUCTIVE.toColor())
				.orElse(ColorType.SUCCESS.toColor());
	}

	private String author(Guild guild) {
		return Optional.ofNullable(getTrackScheduler(guild))
				.map(scheduler -> scheduler.owner.getUser().getName())
				.orElse("Free");
	}

	private Emoji access(Guild guild) {
		return Optional.ofNullable(getTrackScheduler(guild))
				.filter(TrackScheduler::isAccess)
				.map(controller -> EmojiType.PUBLIC.fromUnicode())
				.orElse(EmojiType.PRIVATE.fromUnicode());
	}

	@NotNull
	private List<ItemComponent> buttons(Guild guild) {
		return ActionRow.of(
				ButtonType.START.getButton().withDisabled(getTrackScheduler(guild) != null),
				ButtonType.ACCESS.getButton().withEmoji(access(guild)).withDisabled(getTrackScheduler(guild) == null)
		).getComponents();
	}

	@NotNull
	private EmbedBuilder embed(Guild guild) {
		return new EmbedBuilder()
				.setDescription("Menu")
				.setColor(color(guild))
				.setFooter(author(guild));
	}

	@NotNull
	private MessageEditData message(Guild guild) {
		return new MessageEditBuilder()
				.setContent("")
				.setEmbeds(embed(guild).build())
				.setActionRow(buttons(guild))
				.build();
	}
}

