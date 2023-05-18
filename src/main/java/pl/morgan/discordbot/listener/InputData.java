package pl.morgan.discordbot.listener;

import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

public record InputData(String id, String label, TextInputStyle style, int minLength, int maxLength, boolean isRequired, String placeholder) {
    public static InputData create(String id, String label, TextInputStyle style, int minLength, int maxLength, boolean isRequired, String placeholder) {
        return new InputData(id, label, style, minLength, maxLength, isRequired, placeholder);
    }

    public TextInput build() {
        return TextInput.create(id, label, style)
                .setMinLength(minLength)
                .setMaxLength(maxLength)
                .setRequired(isRequired)
                .setPlaceholder(placeholder)
                .build();
    }
}
