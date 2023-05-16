package pl.morgan.discordbot.listener;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.jetbrains.annotations.NotNull;
import pl.morgan.discordbot.main.Application;
import pl.morgan.discordbot.music.TrackScheduler;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class AudioControlListener extends ListenerAdapter {
	private final Application app;

	public AudioControlListener(Application app) {
		this.app = app;
	}

	private Optional<TrackScheduler> getScheduler(Member member, boolean create) {
		return Optional.ofNullable(member.getVoiceState())
				.map(GuildVoiceState::getChannel)
				.flatMap(channel -> app.manager.getController(channel, create, member));
	}

	@Override
	public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
		String[] id = event.getComponentId().split(":");

		if(!id[0].equals("music")) return;

		switch(id[1]) {
			case "start" -> event.replyModal(getStartModal()).queue();
			case "stop" -> requireScheduler(event, TrackScheduler::stopAudio);
			case "resume" -> requireScheduler(event, TrackScheduler::pauseAudio);
			case "next" -> requireScheduler(event, TrackScheduler::nextAudio);
			case "add" -> event.replyModal(getAddModal()).queue();
			case "back" -> requireScheduler(event, TrackScheduler::backAudio);
		}

		if(!event.isAcknowledged()) event.deferEdit().queue();
	}

	private void requireScheduler(IReplyCallback event, Consumer<TrackScheduler> handler) {
		getScheduler(Objects.requireNonNull(event.getMember()), false).ifPresentOrElse(
				controller -> {
					if (event.getMember().getIdLong() != controller.owner) {
						event.reply("You are not the owner of this player").setEphemeral(true).queue();
						return;
					}

					handler.accept(controller);
				}, () -> event.reply("No audio connection").setEphemeral(true).queue()
		);
	}

	@Override
	public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        if (event.getModalId().equals("add-track")) {
			if (event.isAcknowledged()) return;
			getScheduler(Objects.requireNonNull(event.getMember()), true).ifPresentOrElse(
                    scheduler -> scheduler.addAudio(event.getValue("url").getAsString()),
                    () -> event.reply("Cannot create audio connection").setEphemeral(true).queue()
            );
			event.deferEdit().queue();
        }
	}

//	@Override
//	public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
//		if (!event.getMember().getUser().equals(app.jda.getSelfUser()) || event.getChannelLeft() == null)
//			this.app.manager.getController(event.getChannelLeft(), false, event.getMember())
//							.ifPresent(TrackScheduler::stopAudio);
//	}

	@SuppressWarnings("SameParameterValue")
	private TextInput setAction(final String id, final String label, final TextInputStyle style, final int min, final int max, final boolean required, final String place) {
		return TextInput.create(id, label, style)
				.setMinLength(min)
				.setMaxLength(max)
				.setRequired(required)
				.setPlaceholder(place)
				.build();
	}


	public Modal getAddModal() {
		return Modal.create("add-track", "Add a new track")
				.addActionRow(setAction("url", "Query", TextInputStyle.SHORT, 0, 100, true, "URL or search term(s)"))
				.build();
	}

	public Modal getStartModal() {
		return Modal.create("add-track", "Open connection")
				.addActionRow(setAction("url", "Query", TextInputStyle.SHORT, 0, 100, true, "URL or search term(s)"))
				.build();
	}
}