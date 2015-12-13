package net.dv8tion.jda.guild;

/**
 * Represents a Discord Guild. This should contain all information provided from Discord about a Guild.
 */
public interface Guild
{
    /**
     * The Id of the Guild. This is typically 18 characters long.
     * @return
     */
    public abstract String getId();

    /**
     * The human readable name of the Guild. If no name has been set, this returns null.
     * @return
     */
    public abstract String getName();

    /**
     * The Discord Id for this server's Icon image. If no icon has been set, this returns null.
     * @return
     */
    public abstract String getIconId();

    /**
     * The URL for this server's Icon image. If no icon has been set, this returns null.
     * @return
     */
    public abstract String getIconUrl();

    /**
     * The Id of the AFK Voice Channel.
     * @return
     */
    public abstract String afkChannelId();

    /**
     * The user Id of the owner of this Guild. Currently, there is no way to transfer ownership of a discord
     *   Guild, and a such this user is also the original creator.
     * @return
     */
    public abstract String getOwnerId();

    /**
     * The amount of time (in seconds) that must pass with no activity to be considered AFK by this server.
     * Default is 300 seconds (5 minutes)
     * @return
     */
    public abstract int getAfkTimeout();

    /**
     * The Region that this server exists in.
     * @return
     */
    public abstract Region getRegion();
}
