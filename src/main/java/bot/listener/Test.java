package bot.listener;

import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.utils.SessionController;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.utils.JDALogger;

public class Test {
    private final Guild guild;
    private final TextChannel textChannel;
    private final VoiceChannel voiceChannel;
    private final User user;

    public Test(Guild guild) {
        this(guild, null, null, null);
    }

    public Test(Guild guild, User user, TextChannel textChannel, VoiceChannel voiceChannel) {
        this.guild = guild;
        this.user = user;
        this.textChannel = textChannel;
        this.voiceChannel = voiceChannel;
    }

    public void run() {

    }
}
