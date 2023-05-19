package pl.morgan.discordbot.music;

import com.sedmelluq.discord.lavaplayer.filter.equalizer.EqualizerFactory;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import pl.morgan.discordbot.listener.TextInputUtils;

public class Equalizer {
    private final EqualizerFactory factory;

    
    public float band1, band2, band3, band4, band5 = 15.7F;
    
    public Equalizer(TrackScheduler scheduler) {
        this.factory = new EqualizerFactory();

        scheduler.player.setFilterFactory(factory);
    }

    public void setGain() {
        this.factory.setGain(0, band1 - 5F);
        this.factory.setGain(1, band2 - 1F );
        this.factory.setGain(2, band3 + 5F);
        this.factory.setGain(3, band4 - 7F);
        this.factory.setGain(4, band5 - 12F);
    }

    private float getValue(ModalInteractionEvent event, String id) {
        return Float.parseFloat(event.getValue(id).toString());
    }

    public Modal modal() {
        return Modal.create("equalizer-modal", "change audio recording audio track")
                .addActionRow(TextInputUtils.build("band1", "band1", TextInputStyle.SHORT, 0, 100, true, String.valueOf(band1)))
                .addActionRow(TextInputUtils.build("band2", "band2", TextInputStyle.SHORT, 0, 100, true, String.valueOf(band2)))
                .build();
    }
}
