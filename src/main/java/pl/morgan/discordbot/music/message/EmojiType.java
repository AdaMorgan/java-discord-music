package pl.morgan.discordbot.music.message;

import net.dv8tion.jda.api.entities.emoji.Emoji;

public enum EmojiType {
    YOUTUBE("youtube", 1098879196580806767L),
    SPOTIFY("spotify", 1098879199663628308L),
    SOUNDCLOUD("soundcloud", 1098879198044639313L),

    START("start", 0L),
    PRIVATE("private", 0L),
    PUBLIC("public", 0L),
    STOP("stop", 0L),
    RESUME("resume", 0L),
    PAUSE("pause", 0L),
    NEXT("next", 0L),
    BACK("back", 0L),
    ADD("add", 0L),
    LOOP("loop", 0L),
    SHUFFLE("shuffle", 0L),
    EQUALIZER("equalizer", 0L);

    private final String name;
    private final long code;

    EmojiType(String name, long code) {
        this.name = name;
        this.code = code;
    }

    public Emoji fromUnicode() {
        return Emoji.fromCustom(this.name, this.code, false);
    }
}
