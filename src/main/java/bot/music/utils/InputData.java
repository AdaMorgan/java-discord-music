package bot.music.utils;

import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

public class InputData {
    public static TextInput create(String id, String label, String placeholder) {
        return TextInput.create(id, label, TextInputStyle.SHORT)
                .setMinLength(0)
                .setMaxLength(100)
                .setRequired(true)
                .setPlaceholder(placeholder)
                .build();
    }
}
