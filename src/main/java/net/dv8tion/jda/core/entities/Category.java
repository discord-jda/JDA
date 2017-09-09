package net.dv8tion.jda.core.entities;

import java.util.List;

public interface Category extends Channel, Comparable<Category>
{
    /**
     * All {@link net.dv8tion.jda.core.entities.Channel Channels} listed
     * for this Category
     * <br>This may contain both {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannels}
     * and {@link net.dv8tion.jda.core.entities.TextChannel TextChannels}!
     *
     * @return Immutable list of all child channels
     */
    List<Channel> getChannels();

    /**
     * All {@link net.dv8tion.jda.core.entities.TextChannel TextChannels}
     * listed for this Category
     *
     * @return Immutable list of all child TextChannels
     */
    List<TextChannel> getTextChannels();

    /**
     * All {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannels}
     * listed for this Category
     *
     * @return Immutable list of all child VoiceChannels
     */
    List<VoiceChannel> getVoiceChannels();
}
