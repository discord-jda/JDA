package net.dv8tion.jda.api.entities;

/**
 * Represents a Discord {@link net.dv8tion.jda.api.entities.Clan Clan}.
 * This should contain all information provided from Discord about a Clan.
 *
 * @see User#getClan()
 */
public interface Clan
{

    /**
     * Returns the id of Clan
     */
    String getGuildId();

    /**
     * Returns the name of Clan
     */
    String getTagName();

    /**
     * Returns the badge hash
     */
    String getBadge();

    /**
     * Returns if clan is enabled
     */
    boolean isEnabled();

    /**
     * Returns if clans are same
     */
    boolean equals(Clan otherClan);
}
