package bot.music.message;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public enum ButtonType {
	START("start", EmojiType.START),
	ACCESS("access", EmojiType.PRIVATE),
	STOP("stop", EmojiType.STOP),
	RESUME("resume", EmojiType.RESUME),
	NEXT("next", EmojiType.NEXT),
	BACK("back", EmojiType.BACK),
	ADD("add", EmojiType.ADD),
	STREAM("stream", EmojiType.STREAM),
	LOOP_TRACK("loopTrack", EmojiType.LOOP),
	LOOP_PLAYLIST("loopQueue", EmojiType.LOOP_PLAYLIST),
	SHUFFLE("shuffle", EmojiType.SHUFFLE),
	LIST("list", EmojiType.LOOP_PLAYLIST);

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
