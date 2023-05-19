package pl.morgan.discordbot.listener;

import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

public class TextInputUtils {
    public static TextInput build(String id, String label, TextInputStyle style, int minLength, int maxLength, boolean isRequired, String placeholder) {
        return TextInput.create(id, label, style)
                .setMinLength(minLength)
                .setMaxLength(maxLength)
                .setRequired(isRequired)
                .setPlaceholder(placeholder)
                .build();
    }
}
