package net.dv8tion.jda.events.audio;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.VoiceChannel;

public class AudioConnectEvent extends GenericAudioEvent {
    protected final VoiceChannel connectedChannel;

    public AudioConnectEvent(JDA api, VoiceChannel connectedChannel) {
        super(api, -1);
        this.connectedChannel = connectedChannel;
    }

    public VoiceChannel getConnectedChannel() {
        return connectedChannel;
    }
}
