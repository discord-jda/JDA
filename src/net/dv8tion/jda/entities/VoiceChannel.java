package net.dv8tion.jda.entities;

/**
 * Represents a Discord Voice Channel.
 *
 * Place holder for now until we implement voice support.
 */
public interface VoiceChannel
{
    /**
     * The Id of the Channel. This is typically 18 characters long.
     * @return
     */
    String getId();

    /**
     * The human readable name of the Channel. If no name has been set, this returns null.
     * @return
     */
    String getName();

    /**
     * Returns the Guild that this Channel is a part of.
     * @return
     */
    Guild getGuild();
}
