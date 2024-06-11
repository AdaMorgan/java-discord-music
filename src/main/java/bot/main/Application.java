package bot.main;

import bot.listener.AudioControlListener;
import bot.listener.StartupListener;
import bot.music.Manager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Application {
	public final Manager manager;
	public final JDA jda;
	public final Config config;
	public final StartupListener startup;
	public final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	public static void main(String[] args) {
		new Application(Config.readFromFile("config.toml"));
	}

	public Application(@NotNull Config config) {
		this.config = config;
		this.manager = new Manager(this);
		this.startup = new StartupListener(this);

		this.jda = JDABuilder.createDefault(config.getToken())
				.setStatus(OnlineStatus.ONLINE)
				.enableIntents(GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.MESSAGE_CONTENT)
				.addEventListeners(startup)
				.addEventListeners(new AudioControlListener(this))
				.build();
	}
}


