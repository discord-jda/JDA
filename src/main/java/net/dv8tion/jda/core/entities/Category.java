package net.dv8tion.jda.core.entities;

import java.util.List;

public interface Category extends Channel, Comparable<Category>
{
    //TODO docs
    List<Channel> getChannels();
    List<TextChannel> getTextChannels();
    List<VoiceChannel> getVoiceChannels();
}
