package discord.main;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import discord.listener.AudioControlListener;
import discord.listener.StartupListener;
import discord.music.Manager;


public class Application {
	public final Manager manager = new Manager(this);
	public final JDA jda;
	public final Config config;
	public final StartupListener startup;

	public static void main(String[] args) {
		new Application(Config.readFromFile("config.toml"));
	}

	public Application(Config config) {
		this.config = config;
		this.startup = new StartupListener(this);

		jda = JDABuilder.createDefault(config.getToken())
				.setStatus(OnlineStatus.ONLINE)
				.enableIntents(GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.MESSAGE_CONTENT)
				.setActivity(Activity.listening("music"))

				.addEventListeners(startup)
				.addEventListeners(new AudioControlListener(this))
				.build();
	}
}