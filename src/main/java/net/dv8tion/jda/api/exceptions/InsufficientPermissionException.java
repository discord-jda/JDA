/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.api.exceptions;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates that the user is missing a {@link Permission} for some action.
 *
 * @see   net.dv8tion.jda.api.entities.IPermissionHolder#hasPermission(Permission...) IPermissionHolder.hasPermission(Permission...)
 * @see   net.dv8tion.jda.api.entities.IPermissionHolder#hasPermission(net.dv8tion.jda.api.entities.GuildChannel, Permission...) IPermissionHolder.hasPermission(GuildChannel, Permission...)
 */
public class InsufficientPermissionException extends PermissionException
{
    private final long guildId;
    private final long channelId;
    private final ChannelType channelType;

    public InsufficientPermissionException(@Nonnull Guild guild, @Nonnull Permission permission)
    {
        this(guild, null, permission);
    }

    public InsufficientPermissionException(@Nonnull Guild guild, @Nonnull Permission permission, @Nonnull String reason)
    {
        this(guild, null, permission, reason);
    }

    public InsufficientPermissionException(@Nonnull GuildChannel channel, @Nonnull Permission permission)
    {
        this(channel.getGuild(), channel, permission);
    }

    public InsufficientPermissionException(@Nonnull GuildChannel channel, @Nonnull Permission permission, @Nonnull String reason)
    {
        this(channel.getGuild(), channel, permission, reason);
    }

    private InsufficientPermissionException(@Nonnull Guild guild, @Nullable GuildChannel channel, @Nonnull Permission permission)
    {
        super(permission, "Cannot perform action due to a lack of Permission. Missing permission: " + permission.toString());
        this.guildId = guild.getIdLong();
        this.channelId = channel == null ? 0 : channel.getIdLong();
        this.channelType = channel == null ? ChannelType.UNKNOWN : channel.getType();
    }

    private InsufficientPermissionException(@Nonnull Guild guild, @Nullable GuildChannel channel, @Nonnull Permission permission, @Nonnull String reason)
    {
        super(permission, reason);
        this.guildId = guild.getIdLong();
        this.channelId = channel == null ? 0 : channel.getIdLong();
        this.channelType = channel == null ? ChannelType.UNKNOWN : channel.getType();
    }

    /**
     * The id for the responsible {@link net.dv8tion.jda.api.entities.Guild} instance.
     *
     * @return The ID as a long
     *
     * @since  4.0.0
     *
     * @see    net.dv8tion.jda.api.JDA#getGuildById(long)
     */
    public long getGuildId()
    {
        return guildId;
    }

    /**
     * The id for the responsible {@link GuildChannel} instance.
     *
     * @return The ID as a long or 0
     *
     * @since  4.0.0
     *
     * @see    #getChannel(net.dv8tion.jda.api.JDA)
     */
    public long getChannelId()
    {
        return channelId;
    }

    /**
     * The {@link net.dv8tion.jda.api.entities.ChannelType} for the {@link #getChannelId() channel id}.
     *
     * @return The channel type or {@link net.dv8tion.jda.api.entities.ChannelType#UNKNOWN}.
     *
     * @since  4.0.0
     */
    @Nonnull
    public ChannelType getChannelType()
    {
        return channelType;
    }

    /**
     * The {@link net.dv8tion.jda.api.entities.Guild} instance for the {@link #getGuildId() guild id}.
     *
     * @param  api
     *         The shard to perform the lookup in
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided JDA instance is null
     *
     * @since  4.0.0
     *
     * @return The Guild instance or null
     */
    @Nullable
    public Guild getGuild(@Nonnull JDA api)
    {
        Checks.notNull(api, "JDA");
        return api.getGuildById(guildId);
    }

    /**
     * The {@link GuildChannel} instance for the {@link #getChannelId() channel id}.
     *
     * @param  api
     *         The shard to perform the lookup in
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided JDA instance is null
     *
     * @since  4.0.0
     *
     * @return The GuildChannel instance or null
     */
    @Nullable
    public GuildChannel getChannel(@Nonnull JDA api)
    {
        Checks.notNull(api, "JDA");
        return api.getGuildChannelById(channelType, channelId);
    }
}
