package net.dv8tion.jda.channel;

import net.dv8tion.jda.guild.Guild;

/**
 * Represents a Discord Text Channel.
 * This should provide all necessary functions for interacting with a channel.
 */
public interface Channel
{
    /**
     * The Id of the Channel. This is typically 18 characters long.
     * @return
     */
    public String getId();

    /**
     * The human readable name of the Channel. If no name has been set, this returns null.
     * @return
     */
    public String getName();

    /**
     * The topic set for the Channel. Can also be thought of as the description of this Channel.
     * If no topic has been set, this returns null.
     * @return
     */
    public String getTopic();

    /**
     * Returns the Guild that this Channel is a part of.
     * @return
     */
    public Guild getGuild();
}
