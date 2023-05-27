package discord.listener;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import discord.main.Application;

public class ShutdownListener extends ListenerAdapter {

    private final Application app;

    public ShutdownListener(Application app) {
        this.app = app;
    }

}
