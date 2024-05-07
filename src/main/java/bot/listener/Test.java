package bot.listener;

import net.dv8tion.jda.api.Region;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

public class Test {
    private final Guild guild;
    private final TextChannel textChannel;
    private final VoiceChannel voiceChannel;
    private final User user;

    public Test(Guild guild, User user, TextChannel textChannel, VoiceChannel voiceChannel) {
        this.guild = guild;
        this.user = user;
        this.textChannel = textChannel;
        this.voiceChannel = voiceChannel;
    }

    public void run() {
        this.guild.getChannels().forEach(guildChannel -> {
            guildChannel.getType();
        });
    }
}
