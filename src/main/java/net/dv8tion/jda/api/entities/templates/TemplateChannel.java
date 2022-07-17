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

package net.dv8tion.jda.api.entities.templates;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.channel.ChannelType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

/**
 * POJO for the channels information provided by a template.
 *
 * @see TemplateGuild#getChannels()
 */
public class TemplateChannel implements ISnowflake
{
    private final long id;
    private final ChannelType channelType;
    private final String name;
    private final String topic;
    private final int rawPosition;
    private final long parentId;
    private final boolean isNews;
    private final List<TemplateChannel.PermissionOverride> permissionOverrides;

    // text only properties
    private final boolean nsfw;
    private final int slowmode;

    // voice only properties
    private final int bitrate;
    private final int userLimit;

    public TemplateChannel(final long id, final ChannelType channelType, final String name, final String topic, final int rawPosition, final long parentId,
                           final boolean news, final List<TemplateChannel.PermissionOverride> permissionOverrides, final boolean nsfw, final int slowmode,
                           final int bitrate, final int userLimit)
    {
        this.id = id;
        this.channelType = channelType;
        this.name = name;
        this.topic = topic;
        this.rawPosition = rawPosition;
        this.parentId = parentId;
        this.isNews = news;
        this.permissionOverrides = Collections.unmodifiableList(permissionOverrides);

        this.nsfw = nsfw;
        this.slowmode = slowmode;

        this.bitrate = bitrate;
        this.userLimit = userLimit;
    }

    /**
     * The ids of channels are their position as stored by Discord so this will not look like a typical snowflake.
     *
     * @return The id of the channel as stored by Discord
     */
    @Override
    public long getIdLong()
    {
        return this.id;
    }

    /**
     * As the ids of channels are their position, the date of creation cannot be calculated.
     *
     * @throws java.lang.UnsupportedOperationException
     *         The date of creation cannot be calculated.
     */
    @Override
    public OffsetDateTime getTimeCreated()
    {
        throw new UnsupportedOperationException("The date of creation cannot be calculated");
    }

    /**
     * The {@link ChannelType ChannelType} for this TemplateChannel
     *
     * @return The channel type
     */
    @Nonnull
    public ChannelType getType()
    {
        return this.channelType;
    }

    /**
     * The human readable name of the  GuildChannel.
     * <br>If no name has been set, this returns null.
     *
     * @return The name of this GuildChannel
     */
    @Nonnull
    public String getName()
    {
        return this.name;
    }

    /**
     * The topic set for this TemplateChannel.
     * <br>If no topic has been set or the {@link ChannelType ChannelType}
     * <b>is not {@link ChannelType#TEXT TEXT}</b>, this returns {@code null}.
     *
     * @return Possibly-null String containing the topic of this TemplateChannel.
     */
    @Nullable
    public String getTopic()
    {
        return this.topic;
    }

    /**
     * The actual position of the {@link net.dv8tion.jda.api.entities.templates.TemplateChannel TemplateChannel} as stored and given by Discord.
     * Channel positions are actually based on a pairing of the creation time (as stored in the snowflake id)
     * and the position. If 2 or more channels share the same position then they are sorted based on their creation date.
     * The more recent a channel was created, the lower it is in the hierarchy.
     *
     * @return The true, Discord stored, position of the {@link net.dv8tion.jda.api.entities.templates.TemplateChannel TemplateChannel}.
     */
    public int getPositionRaw()
    {
        return this.rawPosition;
    }

    /**
     * Parent Category id of this TemplateChannel. Channels don't need to have a parent Category.
     * <br>Note that a Category channel will always return {@code -1} for this method
     * as nested categories are not supported.
     *
     * @return The id of the parent Category or {@code -1} if the channel doesn't have a parent Category
     */
    public long getParentId()
    {
        return this.parentId;
    }

    /**
     * Whether or not this channel is considered as "NSFW" (Not-Safe-For-Work).
     * <br>If the {@link ChannelType ChannelType}
     * <b>is not {@link ChannelType#TEXT TEXT}</b>, this returns {@code false}.
     *
     * @return Whether this TextChannel is considered NSFW or {@code false} if the channel is not a text channel
     */
    public boolean isNSFW()
    {
        return this.nsfw;
    }

    /**
     * The slowmode set for this TemplateChannel.
     * <br>If slowmode is set this returns an {@code int} between 1 and {@link net.dv8tion.jda.api.entities.channel.concrete.TextChannel#MAX_SLOWMODE TextChannel.MAX_SLOWMODE}.
     * <br>If not set this returns {@code 0}.
     *
     * <p>Note bots are unaffected by this.
     * <br>Having {@link net.dv8tion.jda.api.Permission#MESSAGE_MANAGE MESSAGE_MANAGE} or
     * {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} permission also
     * grants immunity to slowmode.
     *
     * @return The slowmode for this TextChannel, between 1 and {@link net.dv8tion.jda.api.entities.channel.concrete.TextChannel#MAX_SLOWMODE TextChannel.MAX_SLOWMODE}, {@code 0} if no slowmode is set.
     */
    public int getSlowmode()
    {
        return this.slowmode;
    }

    /**
     * The audio bitrate of the voice audio that is transmitted in this channel. While higher bitrates can be sent to
     * this channel, it will be scaled down by the client.
     *
     * <br>Default and recommended value is 64000
     *
     * @return The audio bitrate of this voice channel
     */
    public int getBitrate()
    {
        return this.bitrate;
    }
    /**
     * The maximum amount of {@link net.dv8tion.jda.api.entities.Member Members} that can be in this
     * voice channel at once.
     *
     * <br>0 - No limit
     *
     * @return The maximum amount of members allowed in this channel at once.
     */
    public int getUserLimit()
    {
        return this.userLimit;
    }

    /**
     * Whether or not this channel is considered an Announcement-/News-Channel.
     * <br>These channels can be used to crosspost messages to other guilds by using a follower type webhook.
     *
     * @return True, if this is considered a news channel
     */
    public boolean isNews()
    {
        return isNews;
    }

    /**
     * Gets all of the {@link net.dv8tion.jda.api.entities.templates.TemplateChannel.PermissionOverride PermissionOverrides} that are part
     * of this {@link net.dv8tion.jda.api.entities.templates.TemplateChannel TemplateChannel}.
     * <br><b>This will only contain {@link net.dv8tion.jda.api.entities.templates.TemplateRole Role} overrides.</b>
     *
     * @return Immutable list of all {@link net.dv8tion.jda.api.entities.templates.TemplateChannel.PermissionOverride PermissionOverrides}
     *         for this {@link net.dv8tion.jda.api.entities.templates.TemplateChannel TemplateChannel}.
     */
    @Nonnull
    public List<TemplateChannel.PermissionOverride> getPermissionOverrides()
    {
        return this.permissionOverrides;
    }

    /**
     * Represents the specific {@link net.dv8tion.jda.api.entities.templates.TemplateRole Role}
     * permission overrides that can be set for channels.
     *
     * @see TemplateChannel#getPermissionOverrides()
     */
    public static class PermissionOverride implements ISnowflake
    {
        private final long id;
        private final long allow;
        private final long deny;

        public PermissionOverride(final long id, final long allow, final long deny)
        {
            this.id = id;
            this.allow = allow;
            this.deny = deny;
        }

        /**
         * This is the raw binary representation (as a base 10 long) of the permissions <b>allowed</b> by this override.
         * <br>The long relates to the offsets used by each {@link net.dv8tion.jda.api.Permission Permission}.
         *
         * @return Never-negative long containing the binary representation of the allowed permissions of this override.
         */
        public long getAllowedRaw()
        {
            return allow;
        }

        /**
         * This is the raw binary representation (as a base 10 long) of the permissions <b>not affected</b> by this override.
         * <br>The long relates to the offsets used by each {@link net.dv8tion.jda.api.Permission Permission}.
         *
         * @return Never-negative long containing the binary representation of the unaffected permissions of this override.
         */
        public long getInheritRaw()
        {
            return ~(allow | deny);
        }

        /**
         * This is the raw binary representation (as a base 10 long) of the permissions <b>denied</b> by this override.
         * <br>The long relates to the offsets used by each {@link net.dv8tion.jda.api.Permission Permission}.
         *
         * @return Never-negative long containing the binary representation of the denied permissions of this override.
         */
        public long getDeniedRaw()
        {
            return deny;
        }

        /**
         * EnumSet of all {@link net.dv8tion.jda.api.Permission Permissions} that are specifically allowed by this override.
         * <br><u>Changes to the returned set do not affect this entity directly.</u>
         *
         * @return Possibly-empty set of allowed {@link net.dv8tion.jda.api.Permission Permissions}.
         */
        @Nonnull
        public EnumSet<Permission> getAllowed()
        {
            return Permission.getPermissions(allow);
        }

        /**
         * EnumSet of all {@link net.dv8tion.jda.api.Permission Permission} that are unaffected by this override.
         * <br><u>Changes to the returned set do not affect this entity directly.</u>
         *
         * @return Possibly-empty set of unaffected {@link net.dv8tion.jda.api.Permission Permissions}.
         */
        @Nonnull
        public EnumSet<Permission> getInherit()
        {
            return Permission.getPermissions(getInheritRaw());
        }

        /**
         * EnumSet of all {@link net.dv8tion.jda.api.Permission Permissions} that are denied by this override.
         * <br><u>Changes to the returned set do not affect this entity directly.</u>
         *
         * @return Possibly-empty set of denied {@link net.dv8tion.jda.api.Permission Permissions}.
         */
        @Nonnull
        public EnumSet<Permission> getDenied()
        {
            return Permission.getPermissions(deny);
        }

        /**
         * The ids of roles are their position as stored by Discord so this will not look like a typical snowflake.
         *
         * @return The id for the role this override is for
         */
        @Override
        public long getIdLong()
        {
            return id;
        }

        /**
         * As the ids of roles are their position, the date of creation cannot be calculated.
         *
         * @throws java.lang.UnsupportedOperationException
         *         The date of creation cannot be calculated.
         */
        @Override
        public OffsetDateTime getTimeCreated()
        {
            throw new UnsupportedOperationException("The date of creation cannot be calculated");
        }
    }
}
