package pl.morgan.discordbot.listener;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.jetbrains.annotations.NotNull;
import pl.morgan.discordbot.main.Application;
import pl.morgan.discordbot.music.TrackScheduler;
import pl.morgan.discordbot.music.message.ButtonType;
import pl.morgan.discordbot.music.message.ColorType;
import pl.morgan.discordbot.music.message.EmojiType;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class StartupListener extends ListenerAdapter {
	private final Application app;
	public Map<Long, Long> message;

	public StartupListener(Application app) {
		this.app = app;
		this.message = new HashMap<>();
	}

	@Override
	public void onGuildJoin(@NotNull GuildJoinEvent event) {
		setupGuild(event.getGuild());
	}

	@Override
	public void onReady(@NotNull ReadyEvent event) {
		event.getJDA().getGuilds().forEach(this::setupGuild);
	}

	@Override
	public void onMessageDelete(MessageDeleteEvent event) {
		if (this.message.get(event.getGuild().getIdLong()) != null && event.getMessageIdLong() == this.message.get(event.getGuild().getIdLong()))
			setupMessage(event.getGuild(), event.getChannel());
	}

	public void update(Guild guild) {
		performForChannel(guild, channel -> channel.editMessageById(this.message.get(guild.getIdLong()), message(guild)).queue());
	}

	private void setupGuild(Guild guild) {
		performForChannel(guild, channel -> {
			channel.getIterableHistory().queue(messages -> messages.stream()
					.filter(message -> message.getEmbeds().size() > 0)
					.forEach(message -> message.delete().queue()));

			setupMessage(guild, channel);
		});
	}

	private void setupMessage(Guild guild, MessageChannel channel) {
		channel.sendMessage(MessageCreateData.fromEditData(message(guild))).queue(message -> this.message.put(guild.getIdLong(), message.getIdLong()));
	}

	private void performForChannel(Guild guild, Consumer<MessageChannel> handler) {
		guild.getTextChannels().stream()
				.filter(channel -> channel.getName().equals(app.config.getChannel()))
				.findAny()
				.ifPresentOrElse(handler, () -> guild.createTextChannel(app.config.getChannel()).queue(handler));
	}

	private TrackScheduler getTrackScheduler(Guild guild) {
		return this.app.manager.controllers.get(guild.getIdLong());
	}

	private Color color(Guild guild) {
		return Optional.ofNullable(getTrackScheduler(guild))
				.map(scheduler -> ColorType.DANGER.toColor())
				.orElse(ColorType.SUCCESS.toColor());
	}

	private String author(Guild guild) {
		return Optional.ofNullable(getTrackScheduler(guild))
				.map(scheduler -> scheduler.owner.getUser().getAsTag())
				.orElse("Free");
	}

	private Emoji access(Guild guild) {
		return getTrackScheduler(guild) != null && getTrackScheduler(guild).isAccess() ? EmojiType.PUBLIC.fromUnicode() : EmojiType.PRIVATE.fromUnicode();
	}

	private List<ItemComponent> buttons(Guild guild) {
		return ActionRow.of(
				ButtonType.START.getButton().withDisabled(getTrackScheduler(guild) != null),
				ButtonType.ACCESS.getButton().withEmoji(access(guild)),
				ButtonType.STREAM.getButton().withDisabled(getTrackScheduler(guild) != null)
		).getComponents();
	}

	private EmbedBuilder embed(Guild guild) {
		return new EmbedBuilder()
				.setDescription("Menu")
				.setColor(color(guild))
				.setFooter(author(guild));
	}

	private MessageEditData message(Guild guild) {
		return new MessageEditBuilder()
				.setContent("")
				.setEmbeds(embed(guild).build())
				.setActionRow(buttons(guild))
				.build();
	}
}

