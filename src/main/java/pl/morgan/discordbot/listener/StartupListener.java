package pl.morgan.discordbot.listener;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.jetbrains.annotations.NotNull;
import pl.morgan.discordbot.main.Application;
import pl.morgan.discordbot.music.message.ButtonType;
import pl.morgan.discordbot.music.message.ColorType;
import pl.morgan.discordbot.music.message.EmojiType;

import java.util.HashMap;
import java.util.Map;
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

	private synchronized void setupGuild(Guild guild) {
		performForChannel(guild, channel -> {
			channel.getIterableHistory().queue(messages -> messages.stream()
					.filter(message -> message.getEmbeds().size() > 0)
					.forEach(message -> message.delete().queue()));

			channel.sendMessage(MessageCreateData.fromEditData(getEmbedMenu(guild))).queue(message -> this.message.put(guild.getIdLong(), message.getIdLong()));
		});
	}

	private void performForChannel(Guild guild, Consumer<TextChannel> handler) {
		guild.getTextChannels().stream()
				.filter(channel -> channel.getName().equals(app.config.getChannel()))
				.findAny()
				.ifPresentOrElse(handler, () -> guild.createTextChannel(app.config.getChannel()).queue(handler));
	}

	public void updateMessage(Guild guild) {
		performForChannel(guild, channel ->
			channel.editMessageById(
					message.get(guild.getIdLong()),
					getEmbedMenu(guild)
			).queue()
		);
	}

	private MessageEditData getEmbedMenu(Guild guild) {
		var scheduler = app.manager.controllers.get(guild.getIdLong());

		var embed = new EmbedBuilder()
				.setColor(ColorType.PRIMARY.toColor())
				.setDescription("MENU");

		if(scheduler != null) {
			var owner = app.jda.getUserById(scheduler.owner);

			if(owner != null) {
				embed.setAuthor(owner.getName(), null, owner.getEffectiveAvatarUrl());
			}
		}

		var accessButton = ButtonType.ACCESS.getButton(scheduler == null || scheduler.getOwner() != null);

		return new MessageEditBuilder()
				.setEmbeds(embed.build())
				.setActionRow(
						ButtonType.START.getButton(scheduler != null),
						scheduler == null ? accessButton : accessButton.withEmoji(scheduler.getOwner() == null ? EmojiType.PUBLIC.get() : EmojiType.PRIVATE.get())
				)
				.build();
	}
}

