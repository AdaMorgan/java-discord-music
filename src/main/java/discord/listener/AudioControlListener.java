package discord.listener;

import discord.main.Application;
import discord.main.Config;
import discord.music.TrackScheduler;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class AudioControlListener extends ListenerAdapter {
	private final Application app;

	public AudioControlListener(Application app) {
		this.app = app;
	}

	private Optional<TrackScheduler> getTrackScheduler(@NotNull Member member, boolean create) {
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
			case "loopTrack" -> requireScheduler(event, TrackScheduler::onLoopTrack);
			case "loopQueue" -> requireScheduler(event, TrackScheduler::onLoopQueue);
			case "shuffle" -> requireScheduler(event, TrackScheduler::shuffle);
		}

		if (!event.isAcknowledged()) event.deferEdit().queue();
	}

	private void requireScheduler(@NotNull IReplyCallback event, Consumer<TrackScheduler> handler) {
		getTrackScheduler(Objects.requireNonNull(event.getMember()), false)
				.filter(controller -> event.getMember().getIdLong() == controller.owner.getIdLong() || controller.isAccess())
				.ifPresentOrElse(handler, () -> event.reply("No audio connection").setEphemeral(true).queue());
	}

	@Override
	public void onChannelDelete(@NotNull ChannelDeleteEvent event) {
		Optional.ofNullable(app.manager.controllers.get(event.getGuild().getIdLong()))
				.filter(controller -> controller.getChannel() == null)
				.ifPresent(TrackScheduler::stop);
	}

	@Override
	public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
		Optional.ofNullable(app.manager.controllers.get(event.getGuild().getIdLong()))
				.filter(controller -> event.getMember() == controller.owner)
				.filter(controller -> event.getChannelLeft() == controller.getChannel())
				.ifPresent(TrackScheduler::stop);
	}

	@Override
	public void onMessageDelete(@NotNull MessageDeleteEvent event) {
		Optional.ofNullable(app.manager.controllers.get(event.getGuild().getIdLong()))
				.filter(controller -> event.getMessageIdLong() == controller.message.id)
				.ifPresent(controller -> controller.message.create());
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
				.filter(controller -> Objects.equals(event.getMember(), controller.owner) || controller.owner == null)
				.ifPresentOrElse(controller -> loadTrack(event, controller),
						() -> event.reply("You are not the owner of this player").setEphemeral(true).queue()
				);
	}

	//TODO:
	private void validUrl(@NotNull ModalInteractionEvent event) {

	}

	//TODO:
	private void loadTrack(ModalInteractionEvent event, @NotNull TrackScheduler scheduler) {
		if (scheduler.queue.size() + 1 <= app.config.getQueueLimit())
			scheduler.add(event.getValue("url").getAsString());
		else
			event.reply("you have exceeded the limit 1000 tracks").setEphemeral(true).queue();
	}

	public Modal getAddModal() {
		return Modal.create("add-track", "Add a new track")
				.addActionRow(InputData.create("url", "Query", "URL or search term(s)"))
				.build();
	}

	public Modal getStartModal() {
		return Modal.create("add-track", "Open connection")
				.addActionRow(InputData.create("url", "Query", "URL or search term(s)"))
				.build();
	}
}