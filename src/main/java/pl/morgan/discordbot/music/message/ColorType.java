package pl.morgan.discordbot.music.message;

import java.awt.*;

public enum ColorType {
    PRIMARY("#5865F2"),
    SUCCESS("#57F287"),
    DANGER("#ED4245");

    private final String code;

    ColorType(String code) {
        this.code = code;
    }

    public Color toColor() {
        return Color.decode(code);
    }
}
