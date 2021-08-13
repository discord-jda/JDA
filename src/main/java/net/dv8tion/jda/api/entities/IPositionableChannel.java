package net.dv8tion.jda.api.entities;

//TODO-v5: Need Docs
//TODO-v5: Should this actually be extending GuildChannel?
public interface IPositionableChannel extends GuildChannel
{
    /**
     * The position this GuildChannel is displayed at.
     * <br>Higher values mean they are displayed lower in the Client. Position 0 is the top most GuildChannel
     * Channels of a {@link net.dv8tion.jda.api.entities.Guild Guild} do not have to have continuous positions
     *
     * @throws IllegalStateException
     *         If this channel is not in the guild cache
     *
     * @return Zero-based int of position of the GuildChannel.
     */
    int getPosition();

    /**
     * The actual position of the {@link GuildChannel GuildChannel} as stored and given by Discord.
     * Channel positions are actually based on a pairing of the creation time (as stored in the snowflake id)
     * and the position. If 2 or more channels share the same position then they are sorted based on their creation date.
     * The more recent a channel was created, the lower it is in the hierarchy. This is handled by {@link #getPosition()}
     * and it is most likely the method you want. If, for some reason, you want the actual position of the
     * channel then this method will give you that value.
     *
     * @return The true, Discord stored, position of the {@link GuildChannel GuildChannel}.
     */
    int getPositionRaw();
}
