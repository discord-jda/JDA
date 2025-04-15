package net.dv8tion.jda.api.entities;

import java.util.Set;

/**
 * SKUs (stock-keeping units) in Discord represent premium offerings that can be made available to application users or guilds.
 */
public interface SKU extends SkuSnowflake
{
    /**
     * Type of the SKU
     * @return SKU type
     */
    SKUType getType();

    /**
     * Customer-facing name of your premium offering
     * @return SKU name
     */
    String getName();

    /**
     * System-generated URL slug based on the SKU's name
     * @return SKU slug
     */
    String getSlug();

    /**
     * Flags can be used to differentiate user and server subscriptions
     * @return set of flags.
     */
    Set<SKUFlag> getFlags();

    /**
     * Checks whether the SKU is available to be purchased for a guild.
     * @return true if it's for guilds
     */
    default boolean isGuildSKU()
    {
        return getFlags().contains(SKUFlag.GUILD_SUBSCRIPTION);
    }

    /**
     * Checks whether the SKU is available to be purchased for a user.
     * @return true if it's for users
     */
    default boolean isUserSKU()
    {
        return getFlags().contains(SKUFlag.USER_SUBSCRIPTION);
    }

    /**
     * Checks whether this SKU is available.
     * @return true if available
     */
    default boolean isAvailable()
    {
        return getFlags().contains(SKUFlag.AVAILABLE);
    }
}
