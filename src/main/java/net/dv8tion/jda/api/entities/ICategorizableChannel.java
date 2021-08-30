package net.dv8tion.jda.api.entities;

import javax.annotation.Nullable;

//TODO-v5: Need Docs
public interface ICategorizableChannel extends GuildChannel
{
    /**
     * Parent {@link net.dv8tion.jda.api.entities.Category Category} of this
     * GuildChannel. Channels don't need to have a parent Category.
     * <br>Note that a {@link net.dv8tion.jda.api.entities.Category Category} will
     * always return {@code null} for this method as nested categories are not supported.
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.Category Category} for this GuildChannel
     */
    @Nullable
    Category getParentCategory();

    /**
     * Whether or not this GuildChannel's {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverrides} match
     * those of {@link #getParentCategory() its parent category}. If the channel doesn't have a parent category, this will return true.
     *
     * <p>This requires {@link net.dv8tion.jda.api.utils.cache.CacheFlag#MEMBER_OVERRIDES CacheFlag.MEMBER_OVERRIDES} to be enabled.
     * <br>{@link net.dv8tion.jda.api.JDABuilder#createLight(String) createLight(String)} disables this CacheFlag by default.
     *
     * @return True, if this channel is synced with its parent category
     *
     * @since  4.2.1
     */
    boolean isSynced();
}
