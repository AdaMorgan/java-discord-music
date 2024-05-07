package bot.main;

import com.moandjiezana.toml.Toml;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.internal.utils.CacheConsumer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.FileReader;

public class Config {
	private final Toml content;

	public Config(Toml toml) {
		this.content = toml;
	}

	public String getToken() {
		return content.getString("bot.token");
	}

	public String getTextChannelByName() {
		return content.getString("bot.channel");
	}

	public String getSpotifyClientId() {
		return content.getString("spotify.client.id");
	}

	public String getEmailUsername() {
		return content.getString("email.username");
	}

	public String getEmailPassword() {
		return content.getString("email.password");
	}

	public String getSpotifyClientSecret() {
		return content.getString("spotify.client.secret");
	}

	public String getDeezerSourceManager() {
		return content.getString("deezer.key");
	}

	public int getQueueLimit() {
		return content.getLong("bot.limit", 1000L).intValue();
	}

	public static Config readFromFile(String path) {
//		MessageChannel channel;
		//		channel.sendMessage().queue();
		try(FileReader reader = new FileReader(path)) {
			return new Config(new Toml().read(reader));
		} catch(Exception error) {
			throw new RuntimeException(error.getMessage(), error);
		}
	}
}
