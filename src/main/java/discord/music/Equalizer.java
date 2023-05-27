package discord.music;

import com.sedmelluq.discord.lavaplayer.filter.equalizer.EqualizerFactory;

public class Equalizer {
    private final EqualizerFactory factory;

    public float band1, band2, band3, band4, band5 = 0.0F;
    
    public Equalizer(TrackScheduler scheduler) {
        this.factory = new EqualizerFactory();

        scheduler.player.setFilterFactory(factory);
    }

    public void setGain() {
        this.factory.setGain(0, band1 - 5F);
        this.factory.setGain(1, band2 - 1F);
        this.factory.setGain(2, band3 + 5F);
        this.factory.setGain(3, band4 - 7F);
        this.factory.setGain(4, band5 - 12F);
    }
}
