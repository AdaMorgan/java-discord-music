package pl.morgan.discordbot.music.message;

import net.dv8tion.jda.api.interactions.components.buttons.Button;

public enum ButtonType {
	START("start", EmojiType.START),
	STOP("stop", EmojiType.STOP),

	RESUME("resume", EmojiType.RESUME),
	NEXT("next", EmojiType.NEXT),
	BACK("back", EmojiType.BACK),
	ADD("add", EmojiType.ADD),
	LOOP("loop", EmojiType.LOOP);

	private final Button button;

	ButtonType(String id, EmojiType emoji) {
		this.button = Button.primary("music:" + id, id);
	}

	public Button getButton(boolean disabled) {
		return button.withDisabled(disabled);
	}

	public Button getButton() {
		return getButton(false);
	}
}
