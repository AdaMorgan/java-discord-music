package pl.morgan.discordbot.listener;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import pl.morgan.discordbot.main.Application;

public class ShutdownListener extends ListenerAdapter {

    private final Application app;

    public ShutdownListener(Application app) {
        this.app = app;
    }
}
