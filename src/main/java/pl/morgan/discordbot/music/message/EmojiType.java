package pl.morgan.discordbot.music.message;

import net.dv8tion.jda.api.entities.emoji.Emoji;

public enum EmojiType {
    YOUTUBE("<:youtube:1098879196580806767>"),
    SPOTIFY("<:spotify:1098879199663628308>"),
    SOUNDCLOUD("<:soundcloud:1098879198044639313>"),

    START("U+23EF"),
    STOP("U+23F9"),
    RESUME("U+25B6"),
    PAUSE("U+23F8"),
    NEXT("U+23E9"),
    BACK("U+23EA"),
    ADD("U+1F195"),
    LOOP("U+1F501"),
    SHUFFLE("U+1F500"),
    EQUALIZER("U+1F39A");

    private final String code;

    EmojiType(String code) {
        this.code = code;
    }

    public Emoji fromUnicode() {
        return Emoji.fromUnicode(code);
    }
}
