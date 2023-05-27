package discord.listener;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.jetbrains.annotations.NotNull;
import discord.main.Application;
import discord.music.TrackScheduler;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class AudioControlListener extends ListenerAdapter {
	private final Application app;

	public AudioControlListener(Application app) {
		this.app = app;
	}

	private Optional<TrackScheduler> getTrackScheduler(Member member, boolean create) {
		return Optional.ofNullable(member.getVoiceState())
				.map(GuildVoiceState::getChannel)
				.flatMap(channel -> app.manager.getController(channel, create, member));
	}

	@Override
	public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
		String[] id = event.getComponentId().split(":");

		if (!id[0].equals("music")) return;

		switch (id[1]) {
			case "start" -> event.replyModal(getStartModal()).queue();
			case "access" -> requireScheduler(event, TrackScheduler::access);
			case "stop" -> requireScheduler(event, TrackScheduler::stop);
			case "resume" -> requireScheduler(event, TrackScheduler::pause);
			case "next" -> requireScheduler(event, TrackScheduler::next);
			case "add" -> event.replyModal(getAddModal()).queue();
			case "back" -> requireScheduler(event, TrackScheduler::back);
			case "loop" -> requireScheduler(event, TrackScheduler::looped);
			case "shuffle" -> requireScheduler(event, TrackScheduler::shuffle);
			case "equalizer" -> event.reply("In progress...").setEphemeral(true).queue();
		}

		if(!event.isAcknowledged()) event.deferEdit().queue();
	}

	private void requireScheduler(IReplyCallback event, Consumer<TrackScheduler> handler) {
		getTrackScheduler(Objects.requireNonNull(event.getMember()), false).ifPresentOrElse(
				controller -> {
					if (event.getMember().getIdLong() != controller.owner.getIdLong() && !controller.isAccess()) {
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
			getTrackScheduler(Objects.requireNonNull(event.getMember()), true)
					.ifPresentOrElse(scheduler -> inputTrackModal(event, scheduler),
							() -> event.reply("Cannot create audio connection").setEphemeral(true).queue()
            );
			event.deferEdit().queue();
        }
	}

	private void inputTrackModal(@NotNull ModalInteractionEvent event, TrackScheduler scheduler) {
		Optional.of(scheduler)
				.filter(s -> Objects.equals(event.getMember(), s.owner) || s.owner == null)
				.ifPresentOrElse(s -> s.add(event.getValue("url").getAsString()),
						() -> event.reply("You are not the owner of this player").setEphemeral(true).queue()
				);
	}

	public Modal getAddModal() {
		return Modal.create("add-track", "Add a new track")
				.addActionRow(InputData.create("url", "Query", TextInputStyle.SHORT, 0, 100, true, "URL or search term(s)"))
				.build();
	}

	public Modal getStartModal() {
		return Modal.create("add-track", "Open connection")
				.addActionRow(InputData.create("url", "Query", TextInputStyle.SHORT, 0, 100, true, "URL or search term(s)"))
				.build();
	}
}