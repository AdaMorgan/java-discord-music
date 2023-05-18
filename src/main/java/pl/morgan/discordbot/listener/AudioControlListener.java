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
import pl.morgan.discordbot.music.Equalizer;
import pl.morgan.discordbot.music.TrackScheduler;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class AudioControlListener extends ListenerAdapter {
	private final Application app;

	private Equalizer equalizer;
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
			case "stop" -> requireScheduler(event, TrackScheduler::stop);
			case "resume" -> requireScheduler(event, TrackScheduler::pause);
			case "next" -> requireScheduler(event, TrackScheduler::next);
			case "add" -> event.replyModal(getAddModal()).queue();
			case "back" -> requireScheduler(event, TrackScheduler::back);
			case "loop" -> requireScheduler(event, TrackScheduler::loop);
			case "shuffle" -> requireScheduler(event, TrackScheduler::shuffle);
			case "equalizer" -> requireScheduler(event, TrackScheduler::equalizer);
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
			getScheduler(Objects.requireNonNull(event.getMember()), true)
					.ifPresentOrElse(scheduler -> scheduler.add(event.getValue("url").getAsString()),
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

	public Modal getAddModal() {
		return Modal.create("add-track", "Add a new track")
				.addActionRow(InputData.create("url", "Query", TextInputStyle.SHORT, 0, 100, true, "URL or search term(s)").build())
				.build();
	}

	public Modal getEqualizerModal() {
		return Modal.create("equalizer-modal", "change audio recording audio track")
				.addActionRow(InputData.create("band1", "band1", TextInputStyle.SHORT, 0, 100, true, "bland1").build())
				.addActionRow(InputData.create("band2", "band2", TextInputStyle.SHORT, 0, 100, true, "bland2").build())
				.build();
	}

	public Modal getStartModal() {
		return Modal.create("add-track", "Open connection")
				.addActionRow(InputData.create("url", "Query", TextInputStyle.SHORT, 0, 100, true, "URL or search term(s)").build())
				.build();
	}
}