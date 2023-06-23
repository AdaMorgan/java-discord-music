package discord.listener;

import discord.music.message.ColorType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.session.GenericSessionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StateListener extends ListenerAdapter {

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        this.sendMessage(event);
    }

    @Override
    public void onShutdown(@NotNull ShutdownEvent event) {
        this.sendMessage(event);
    }

    private void sendMessage(@NotNull GenericSessionEvent event) {
        event.getJDA().retrieveApplicationInfo().queue(
                info -> info.getOwner().openPrivateChannel().queue(channel ->
                        channel.sendMessageEmbeds(getEmbed(event).build()).queue())
        );
    }

    @NotNull
    private EmbedBuilder getEmbed(GenericSessionEvent event) {
        return new EmbedBuilder()
                .setTitle(getTitle(event))
                .setDescription(getDescription(event))
                .setColor(getColor(event))
                .setFooter(getDate());
    }

    @NotNull
    private String getDate() {
        return new SimpleDateFormat("dd/MM/yyyy hh:mm a z").format(new Date());
    }

    @NotNull
    @Contract(pure = true)
    private String getTitle(GenericSessionEvent event) {
        return event instanceof ReadyEvent ? "READY" : "SHUTDOWN";
    }

    @NotNull
    @Contract(pure = true)
    private String getDescription(GenericSessionEvent event) {
        return event instanceof ReadyEvent ? "ready" : "shutdown";
    }

    @NotNull
    private Color getColor(GenericSessionEvent event) {
        return event instanceof ReadyEvent ? ColorType.SUCCESS.toColor() : ColorType.DESTRUCTIVE.toColor();
    }
}
