package discord.music.message;

import net.dv8tion.jda.api.entities.emoji.Emoji;

public enum EmojiType {
    INFO("info", 1L),
    WARNING("warning", 1L),
    ERROR("error", 1L),

    YOUTUBE("youtube", 1120750136386134096L),
    SPOTIFY("spotify", 1120750131646570496L),
    SOUNDCLOUD("soundcloud", 1120750130145009714L),
    DEEZER("deezer", 1120750123861946409L),
    TWITCH("twitch", 1120750133416579162L),
    RADIO("radio", 1120750127905255457L),
    MUSIC("music", 1120750126521143466L),
    APPLE("apple", 1120750122247139490L),

    START("start", 1120758269783326802L),
    PRIVATE("private", 1120758263194066964L),
    PUBLIC("public", 1120758266163634357L),
    STOP("stop", 1120752337984696321L),
    RESUME("resume", 1120752333471633519L),
    PAUSE("pause", 1120752327503126638L),
    NEXT("next", 1120752324990738464L),
    BACK("back", 1120752321635287111L),
    ADD("add", 1120958577046212699L),
    LOOP("loop", 1120758260983660566L),
    LOOP_PLAYLIST("loop_playlist", 1120958578812010496L),
    SHUFFLE("shuffle", 1120758268520841336L),
    STREAM("stream", 1120758539317686362L);

    private final String name;
    private final long code;

    EmojiType(String name, long code) {
        this.name = name;
        this.code = code;
    }

    public long getCode() {
        return this.code;
    }

    public Emoji fromUnicode() {
        return Emoji.fromCustom(this.name, this.code, false);
    }
}
