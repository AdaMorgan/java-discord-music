package pl.morgan.discordbot.main;

import net.dv8tion.jda.api.JDA;
 import net.dv8tion.jda.api.JDABuilder;
 import net.dv8tion.jda.api.OnlineStatus;
 import net.dv8tion.jda.api.entities.Activity;
 import net.dv8tion.jda.api.requests.GatewayIntent;
 import pl.morgan.discordbot.listener.AudioControlListener;
 import pl.morgan.discordbot.listener.StartupListener;
import pl.morgan.discordbot.music.Manager;


public class Application {
	public final Manager manager = new Manager(this);
	public final JDA jda;
	public final Config config;

	public static void main(String[] args) {
		new Application(Config.readFromFile("config.toml"));
	}

	public Application(Config config) {
		this.config = config;

		jda = JDABuilder.createDefault(config.getToken())
				.setStatus(OnlineStatus.ONLINE)
				.enableIntents(GatewayIntent.GUILD_VOICE_STATES)
				.setActivity(Activity.listening("music"))

				.addEventListeners(new StartupListener(this))
				.addEventListeners(new AudioControlListener(this))
				.build();
	}
}
