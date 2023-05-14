package pl.morgan.discordbot.music.message;

import net.dv8tion.jda.api.entities.emoji.Emoji;

public enum EmojiType {
    YOUTUBE("<:youtube:1098879196580806767>"),
    SPOTIFY("<:spotify:1098879199663628308>"),
    SOUNDCLOUD("<:soundcloud:1098879198044639313>"),

    START("<:spotify:1098879199663628308>"),
    STOP("<:spotify:1098879199663628308>"),
    RESUME("<:spotify:1098879199663628308>"),
    NEXT("<:spotify:1098879199663628308>"),
    BACK("<:spotify:1098879199663628308>"),
    ADD("<:spotify:1098879199663628308>"),
    LOOP("<:spotify:1098879199663628308>");

    private final String code;

    EmojiType(String code) {
        this.code = code;
    }

    public Emoji fromFormatted() {
        return Emoji.fromFormatted(code);
    }
}
