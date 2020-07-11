package net.dv8tion.jda.api.entities;

/**
 * Enum used to differentiate between the different types of Guild Roles.
 */
public enum RoleType
{
    /**
     * A basic, user-created role.
     */
    NORMAL,
    /**
     * A role created from a bot on join.
     */
    BOT,
    /**
     * A role created from a YouTube Member or Twitch Subscriber integration..\
     */
    INTEGRATION,
    /**
     * The Server Booster role.
     */
    BOOSTER,
    /**
     * Unknown RoleType.
     */
    UNKNOWN;
}
