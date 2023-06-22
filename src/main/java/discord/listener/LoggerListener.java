package discord.listener;

import discord.music.message.ColorType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.session.GenericSessionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LoggerListener extends ListenerAdapter {

    @Override
    public void onShutdown(@NotNull ShutdownEvent event) {
        this.sendMessage(event);
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        this.sendMessage(event);
    }

    private void sendMessage(GenericSessionEvent event) {
        event.getJDA().retrieveApplicationInfo().queue(
                info -> info.getOwner().openPrivateChannel().queue(channel ->
                        channel.sendMessageEmbeds(getEmbed(event).build()).queue())
        );
    }

    private EmbedBuilder getEmbed(GenericSessionEvent event) {
        return new EmbedBuilder()
                .setTitle(getTitle(event))
                .setDescription(getDescription(event))
                .setColor(getColor(event))
                .setFooter(getDate());
    }

    private String getDate() {
        return new SimpleDateFormat("dd/MM/yyyy hh:mm a z").format(new Date());
    }

    private String getTitle(GenericSessionEvent event) {
        return event instanceof ReadyEvent ? "READY" : "SHUTDOWN";
    }

    private String getDescription(GenericSessionEvent event) {
        return event instanceof ReadyEvent ? "ready" : "shutdown";
    }

    private Color getColor(GenericSessionEvent event) {
        return event instanceof ReadyEvent ? ColorType.SUCCESS.toColor() : ColorType.DESTRUCTIVE.toColor();
    }
}
