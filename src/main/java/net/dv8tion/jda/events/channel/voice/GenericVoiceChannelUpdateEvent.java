package net.dv8tion.jda.events.channel.voice;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.VoiceChannel;

public class GenericVoiceChannelUpdateEvent extends GenericVoiceChannelEvent
{

    public GenericVoiceChannelUpdateEvent(JDA api, int responseNumber, VoiceChannel channel)
    {
        super(api, responseNumber, channel);
    }
}
