package discord.music.message;

import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public enum ButtonType {
	START("start", EmojiType.START),
	ACCESS("access", EmojiType.PRIVATE),
	STOP("stop", EmojiType.STOP),
	RESUME("resume", EmojiType.RESUME),
	NEXT("next", EmojiType.NEXT),
	BACK("back", EmojiType.BACK),
	ADD("add", EmojiType.ADD),
	LOOP_TRACK("loopTrack", EmojiType.LOOP),
	LOOP_QUEUE("loopQueue", EmojiType.LOOP),
	SEARCH("search", EmojiType.SEARCH),
	SHUFFLE("shuffle", EmojiType.SHUFFLE);

    private final Button button;

	ButtonType(String id, EmojiType emoji) {
		this.button = Button.primary("music:" + id, emoji.fromUnicode());
	}

	public Button getButton(boolean disabled) {
		return button.withDisabled(disabled);
	}

	public Button getButton() {
		return getButton(false);
	}
}
