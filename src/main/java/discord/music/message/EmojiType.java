package discord.music.message;

import net.dv8tion.jda.api.entities.emoji.Emoji;

public enum EmojiType {
    YOUTUBE("youtube", 1098879196580806767L),
    SPOTIFY("spotify", 1098879199663628308L),
    SOUNDCLOUD("soundcloud", 1098879198044639313L),

    START("youtube", 1098879196580806767L), //
    PRIVATE("youtube", 1098879196580806767L),
    PUBLIC("youtube", 1098879196580806767L),
    STOP("youtube", 1098879196580806767L), //
    RESUME("youtube", 1098879196580806767L),
    PAUSE("youtube", 1098879196580806767L),
    NEXT("youtube", 1098879196580806767L),
    BACK("youtube", 1098879196580806767L),
    ADD("youtube", 1098879196580806767L),
    LOOP("youtube", 1098879196580806767L),
    STREAM("youtube", 1098879196580806767L),
    SHUFFLE("youtube", 1098879196580806767L);

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
