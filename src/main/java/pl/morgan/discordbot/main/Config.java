package pl.morgan.discordbot.main;

import com.moandjiezana.toml.Toml;

import java.awt.*;
import java.io.*;

public class Config {
	private final Toml content;

	public Config(Toml toml) {
		this.content = toml;
	}

	public String getToken() {
		return content.getString("discord.token");
	}

	public Color getColor() {
		return Color.decode(content.getString("discord.color"));
	}

	public String getChannel() {
		return content.getString("discord.channel");
	}

	public String getEmail() {
		return content.getString("email.email");
	}

	public String getEmailPassword() {
		return content.getString("email.password");
	}

	public String getSpotifyClientId() {
		return content.getString("spotify.client.id");
	}

	public String getSpotifyClientSecret() {
		return content.getString("spotify.client.secret");
	}

	public String getDeezerSourceManager() {
		return content.getString("deezer.key");
	}

	public static Config readFromFile(String path) {
		try(FileReader reader = new FileReader(path)) {
			return new Config(new Toml().read(reader));
		} catch(Exception exception) {
			throw new RuntimeException("Failed to read config", exception);
		}
	}
}
