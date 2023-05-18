package pl.morgan.discordbot.listener;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import pl.morgan.discordbot.main.Application;
import pl.morgan.discordbot.music.message.ButtonType;
import pl.morgan.discordbot.music.message.ColorType;

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

	private void setupGuild(Guild guild) {
		performForChannel(guild, channel -> {
//			channel.getIterableHistory().queue(messages -> messages.stream()
//					.filter(message -> message.getEmbeds().size() > 0)
//					.forEach(message -> message.delete().queue()));

			channel.sendMessageEmbeds(getEmbedMenu().build())
					.setActionRow(ButtonType.START.getButton(false), ButtonType.ACCESS.getButton(true))
					.queue(message -> this.message.put(guild.getIdLong(), message.getIdLong()));
		});
	}

	private void performForChannel(Guild guild, Consumer<TextChannel> handler) {
		guild.getTextChannels().stream()
				.filter(channel -> channel.getName().equals(app.config.getChannel()))
				.findAny()
				.ifPresentOrElse(handler, () -> guild.createTextChannel(app.config.getChannel()).queue(handler));
	}

	private EmbedBuilder getEmbedMenu() {
		return new EmbedBuilder()
				.setColor(ColorType.PRIMARY.toColor())
				.setDescription("MENU");
	}
}

