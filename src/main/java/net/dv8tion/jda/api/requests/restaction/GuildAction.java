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

package net.dv8tion.jda.api.requests.restaction;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.internal.requests.restaction.GuildActionImpl;
import net.dv8tion.jda.internal.requests.restaction.PermOverrideData;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

/**
 * {@link net.dv8tion.jda.api.requests.RestAction RestAction} extension
 * specifically designed to allow for the creation of {@link net.dv8tion.jda.api.entities.Guild Guilds}.
 * <br>This is available to all account types but may undergo certain restrictions by Discord.
 *
 * @since  3.4.0
 *
 * @see    net.dv8tion.jda.api.JDA#createGuild(String)
 */
public interface GuildAction extends RestAction<Void>
{
    @Nonnull
    @Override
    GuildAction setCheck(@Nullable BooleanSupplier checks);

    @Nonnull
    @Override
    GuildAction timeout(long timeout, @Nonnull TimeUnit unit);

    @Nonnull
    @Override
    GuildAction deadline(long timestamp);

    /**
     * Sets the {@link net.dv8tion.jda.api.entities.Icon Icon}
     * for the resulting {@link net.dv8tion.jda.api.entities.Guild Guild}
     *
     * @param  icon
     *         The {@link net.dv8tion.jda.api.entities.Icon Icon} to use
     *
     * @return The current GuildAction for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    GuildAction setIcon(@Nullable Icon icon);

    /**
     * Sets the name for the resulting {@link net.dv8tion.jda.api.entities.Guild Guild}
     *
     * @param  name
     *         The name to use
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided name is {@code null}, blank or not between 2-100 characters long
     *
     * @return The current GuildAction for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    GuildAction setName(@Nonnull String name);

    /**
     * Sets the {@link net.dv8tion.jda.api.entities.Guild.VerificationLevel VerificationLevel}
     * for the resulting {@link net.dv8tion.jda.api.entities.Guild Guild}
     *
     * @param  level
     *         The {@link net.dv8tion.jda.api.entities.Guild.VerificationLevel VerificationLevel} to use
     *
     * @return The current GuildAction for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    GuildAction setVerificationLevel(@Nullable Guild.VerificationLevel level);

    /**
     * Sets the {@link net.dv8tion.jda.api.entities.Guild.NotificationLevel NotificationLevel}
     * for the resulting {@link net.dv8tion.jda.api.entities.Guild Guild}
     *
     * @param  level
     *         The {@link net.dv8tion.jda.api.entities.Guild.NotificationLevel NotificationLevel} to use
     *
     * @return The current GuildAction for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    GuildAction setNotificationLevel(@Nullable Guild.NotificationLevel level);

    /**
     * Sets the {@link net.dv8tion.jda.api.entities.Guild.ExplicitContentLevel ExplicitContentLevel}
     * for the resulting {@link net.dv8tion.jda.api.entities.Guild Guild}
     *
     * @param  level
     *         The {@link net.dv8tion.jda.api.entities.Guild.ExplicitContentLevel ExplicitContentLevel} to use
     *
     * @return The current GuildAction for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    GuildAction setExplicitContentLevel(@Nullable Guild.ExplicitContentLevel level);

    /**
     * Adds a {@link GuildChannel GuildChannel} to the resulting
     * Guild. This cannot be of type {@link net.dv8tion.jda.api.entities.ChannelType#CATEGORY CATEGORY}!
     *
     * @param  channel
     *         The {@link ChannelData ChannelData}
     *         to use for the construction of the GuildChannel
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided channel is {@code null}!
     *
     * @return The current GuildAction for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    GuildAction addChannel(@Nonnull ChannelData channel);

    /**
     * Gets the {@link ChannelData ChannelData}
     * of the specified index. The index is 0 based on insertion order of {@link #addChannel(ChannelData)}!
     *
     * @param  index
     *         The 0 based index of the channel
     *
     * @throws java.lang.IndexOutOfBoundsException
     *         If the provided index is not in bounds
     *
     * @return The current GuildAction for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    ChannelData getChannel(int index);

    /**
     * Removes the {@link ChannelData ChannelData}
     * at the specified index and returns the removed object.
     *
     * @param  index
     *         The index of the channel
     *
     * @throws java.lang.IndexOutOfBoundsException
     *         If the index is out of bounds
     *
     * @return The removed object
     */
    @Nonnull
    @CheckReturnValue
    ChannelData removeChannel(int index);

    /**
     * Removes the provided {@link ChannelData ChannelData}
     * from this GuildAction if present.
     *
     * @param  data
     *         The ChannelData to remove
     *
     * @return The current GuildAction for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    GuildAction removeChannel(@Nonnull ChannelData data);

    /**
     * Creates a new {@link ChannelData ChannelData}
     * instance and adds it to this GuildAction.
     *
     * @param  type
     *         The {@link net.dv8tion.jda.api.entities.ChannelType ChannelType} of the resulting GuildChannel
     *         <br>This may be of type {@link net.dv8tion.jda.api.entities.ChannelType#TEXT TEXT} or {@link net.dv8tion.jda.api.entities.ChannelType#VOICE VOICE}!
     * @param  name
     *         The name of the channel.
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If provided with an invalid ChannelType</li>
     *             <li>If the provided name is {@code null} or blank</li>
     *             <li>If the provided name is not between 2-100 characters long</li>
     *         </ul>
     *
     * @return The new ChannelData instance
     */
    @Nonnull
    @CheckReturnValue
    ChannelData newChannel(@Nonnull ChannelType type, @Nonnull String name);

    /**
     * Retrieves the {@link RoleData RoleData} for the
     * public role ({@link net.dv8tion.jda.api.entities.Guild#getPublicRole() Guild.getPublicRole()}) for the resulting Guild.
     * <br>The public role is also known in the official client as the {@code @everyone} role.
     *
     * <p><b>You can only change the permissions of the public role!</b>
     *
     * @return RoleData of the public role
     */
    @Nonnull
    @CheckReturnValue
    RoleData getPublicRole();

    /**
     * Retrieves the {@link RoleData RoleData} for the
     * provided index.
     * <br>The public role is at the index 0 and all others are ordered by insertion order!
     *
     * @param  index
     *         The index of the role
     *
     * @throws java.lang.IndexOutOfBoundsException
     *         If the provided index is out of bounds
     *
     * @return RoleData of the provided index
     */
    @Nonnull
    @CheckReturnValue
    RoleData getRole(int index);

    /**
     * Creates and add a new {@link RoleData RoleData} object
     * representing a Role for the resulting Guild.
     *
     * <p>This can be used in {@link GuildAction.ChannelData#addPermissionOverride(GuildAction.RoleData, long, long) ChannelData.addPermissionOverride(...)}.
     * <br>You may change any properties of this {@link RoleData RoleData} instance!
     *
     * @return RoleData for the new Role
     */
    @Nonnull
    @CheckReturnValue
    RoleData newRole();

    /**
     * Mutable object containing information on a {@link net.dv8tion.jda.api.entities.Role Role}
     * of the resulting {@link net.dv8tion.jda.api.entities.Guild Guild} that is constructed by a GuildAction instance
     *
     * <p>This may be used in {@link GuildAction.ChannelData#addPermissionOverride(GuildAction.RoleData, long, long)}  ChannelData.addPermissionOverride(...)}!
     */
    class RoleData implements SerializableData
    {
        protected final long id;
        protected final boolean isPublicRole;

        protected Long permissions;
        protected String name;
        protected Integer color;
        protected Integer position;
        protected Boolean mentionable, hoisted;

        public RoleData(long id)
        {
            this.id = id;
            this.isPublicRole = id == 0;
        }

        /**
         * Sets the raw permission value for this Role
         *
         * @param  rawPermissions
         *         Raw permission value
         *
         * @return The current RoleData instance for chaining convenience
         */
        @Nonnull
        public RoleData setPermissionsRaw(@Nullable Long rawPermissions)
        {
            this.permissions = rawPermissions;
            return this;
        }

        /**
         * Adds the provided permissions to the Role
         *
         * @param  permissions
         *         The permissions to add
         *
         * @throws java.lang.IllegalArgumentException
         *         If any of the provided permissions is {@code null}
         *
         * @return The current RoleData instance for chaining convenience
         */
        @Nonnull
        public RoleData addPermissions(@Nonnull Permission... permissions)
        {
            Checks.notNull(permissions, "Permissions");
            for (Permission perm : permissions)
                Checks.notNull(perm, "Permissions");
            if (this.permissions == null)
                this.permissions = 0L;
            this.permissions |= Permission.getRaw(permissions);
            return this;
        }

        /**
         * Adds the provided permissions to the Role
         *
         * @param  permissions
         *         The permissions to add
         *
         * @throws java.lang.IllegalArgumentException
         *         If any of the provided permissions is {@code null}
         *
         * @return The current RoleData instance for chaining convenience
         */
        @Nonnull
        public RoleData addPermissions(@Nonnull Collection<Permission> permissions)
        {
            Checks.noneNull(permissions, "Permissions");
            if (this.permissions == null)
                this.permissions = 0L;
            this.permissions |= Permission.getRaw(permissions);
            return this;
        }

        /**
         * Sets the name for this Role
         *
         * @param  name
         *         The name
         *
         * @throws java.lang.IllegalStateException
         *         If this is the public role
         *
         * @return The current RoleData instance for chaining convenience
         */
        @Nonnull
        public RoleData setName(@Nullable String name)
        {
            checkPublic("name");
            this.name = name;
            return this;
        }

        /**
         * Sets the color for this Role
         *
         * @param  color
         *         The color for this Role
         *
         * @throws java.lang.IllegalStateException
         *         If this is the public role
         *
         * @return The current RoleData instance for chaining convenience
         */
        @Nonnull
        public RoleData setColor(@Nullable Color color)
        {
            checkPublic("color");
            this.color = color == null ? null : color.getRGB();
            return this;
        }

        /**
         * Sets the color for this Role
         *
         * @param  color
         *         The color for this Role, or {@code null} to unset
         *
         * @throws java.lang.IllegalStateException
         *         If this is the public role
         *
         * @return The current RoleData instance for chaining convenience
         */
        @Nonnull
        public RoleData setColor(@Nullable Integer color)
        {
            checkPublic("color");
            this.color = color;
            return this;
        }

        /**
         * Sets the position for this Role
         *
         * @param  position
         *         The position
         *
         * @throws java.lang.IllegalStateException
         *         If this is the public role
         *
         * @return The current RoleData instance for chaining convenience
         */
        @Nonnull
        public RoleData setPosition(@Nullable Integer position)
        {
            checkPublic("position");
            this.position = position;
            return this;
        }

        /**
         * Sets whether the Role is mentionable
         *
         * @param  mentionable
         *         Whether the role is mentionable
         *
         * @throws java.lang.IllegalStateException
         *         If this is the public role
         *
         * @return The current RoleData instance for chaining convenience
         */
        @Nonnull
        public RoleData setMentionable(@Nullable Boolean mentionable)
        {
            checkPublic("mentionable");
            this.mentionable = mentionable;
            return this;
        }

        /**
         * Sets whether the Role is hoisted
         *
         * @param  hoisted
         *         Whether the role is hoisted
         *
         * @throws java.lang.IllegalStateException
         *         If this is the public role
         *
         * @return The current RoleData instance for chaining convenience
         */
        @Nonnull
        public RoleData setHoisted(@Nullable Boolean hoisted)
        {
            checkPublic("hoisted");
            this.hoisted = hoisted;
            return this;
        }

        @Nonnull
        @Override
        public DataObject toData()
        {
            final DataObject o = DataObject.empty().put("id", Long.toUnsignedString(id));
            if (permissions != null)
                o.put("permissions", permissions);
            if (position != null)
                o.put("position", position);
            if (name != null)
                o.put("name", name);
            if (color != null)
                o.put("color", color & 0xFFFFFF);
            if (mentionable != null)
                o.put("mentionable", mentionable);
            if (hoisted != null)
                o.put("hoist", hoisted);
            return o;
        }

        protected void checkPublic(String comment)
        {
            if (isPublicRole)
                throw new IllegalStateException("Cannot modify " + comment + " for the public role!");
        }
    }

    /**
     * GuildChannel information used for the creation of {@link GuildChannel Channels} within
     * the construction of a {@link net.dv8tion.jda.api.entities.Guild Guild} via GuildAction.
     *
     * <p>Use with {@link #addChannel(ChannelData) GuildAction.addChannel(ChannelData)}.
     */
    class ChannelData implements SerializableData
    {
        protected final ChannelType type;
        protected final String name;

        protected final Set<PermOverrideData> overrides = new HashSet<>();

        protected Integer position;

        // Text only
        protected String topic;
        protected Boolean nsfw;
        // Voice only
        protected Integer bitrate, userlimit;

        /**
         * Constructs a data object containing information on
         * a {@link GuildChannel GuildChannel} to be used in the construction
         * of a {@link net.dv8tion.jda.api.entities.Guild Guild}!
         *
         * @param  type
         *         The {@link net.dv8tion.jda.api.entities.ChannelType ChannelType} of the resulting GuildChannel
         *         <br>This may be of type {@link net.dv8tion.jda.api.entities.ChannelType#TEXT TEXT} or {@link net.dv8tion.jda.api.entities.ChannelType#VOICE VOICE}!
         * @param  name
         *         The name of the channel.
         *
         * @throws java.lang.IllegalArgumentException
         *         <ul>
         *             <li>If provided with an invalid ChannelType</li>
         *             <li>If the provided name is {@code null} or blank</li>
         *             <li>If the provided name is not between 2-100 characters long</li>
         *         </ul>
         */
        public ChannelData(ChannelType type, String name)
        {
            Checks.notBlank(name, "Name");
            Checks.check(type == ChannelType.TEXT || type == ChannelType.VOICE || type == ChannelType.STAGE,
                "Can only create channels of type TEXT, STAGE, or VOICE in GuildAction!");
            Checks.check(name.length() >= 2 && name.length() <= 100,
                "Channel name has to be between 2-100 characters long!");

            this.type = type;
            this.name = name;
        }

        /**
         * Sets the topic for this channel.
         * <br>These are only relevant to channels of type {@link net.dv8tion.jda.api.entities.ChannelType#TEXT TEXT}.
         *
         * @param  topic
         *         The topic for the channel
         *
         * @throws java.lang.IllegalArgumentException
         *         If the provided topic is bigger than 1024 characters
         *
         * @return This ChannelData instance for chaining convenience
         */
        @Nonnull
        public ChannelData setTopic(@Nullable String topic)
        {
            if (topic != null && topic.length() > 1024)
                throw new IllegalArgumentException("Channel Topic must not be greater than 1024 in length!");
            this.topic = topic;
            return this;
        }

        /**
         * Sets the whether this channel should be marked NSFW.
         * <br>These are only relevant to channels of type {@link net.dv8tion.jda.api.entities.ChannelType#TEXT TEXT}.
         *
         * @param  nsfw
         *         Whether this channel should be marked NSFW
         *
         * @return This ChannelData instance for chaining convenience
         */
        @Nonnull
        public ChannelData setNSFW(@Nullable Boolean nsfw)
        {
            this.nsfw = nsfw;
            return this;
        }

        /**
         * Sets the bitrate for this channel.
         * <br>These are only relevant to channels of type {@link net.dv8tion.jda.api.entities.ChannelType#VOICE VOICE}.
         *
         * @param  bitrate
         *         The bitrate for the channel (8000-96000)
         *
         * @throws java.lang.IllegalArgumentException
         *         If the provided bitrate is not between 8000-96000
         *
         * @return This ChannelData instance for chaining convenience
         */
        @Nonnull
        public ChannelData setBitrate(@Nullable Integer bitrate)
        {
            if (bitrate != null)
            {
                Checks.check(bitrate >= 8000, "Bitrate must be greater than 8000.");
                Checks.check(bitrate <= 96000, "Bitrate must be less than 96000.");
            }
            this.bitrate = bitrate;
            return this;
        }

        /**
         * Sets the userlimit for this channel.
         * <br>These are only relevant to channels of type {@link net.dv8tion.jda.api.entities.ChannelType#VOICE VOICE}.
         *
         * @param  userlimit
         *         The userlimit for the channel (0-99)
         *
         * @throws java.lang.IllegalArgumentException
         *         If the provided userlimit is not between 0-99
         *
         * @return This ChannelData instance for chaining convenience
         */
        @Nonnull
        public ChannelData setUserlimit(@Nullable Integer userlimit)
        {
            if (userlimit != null && (userlimit < 0 || userlimit > 99))
                throw new IllegalArgumentException("Userlimit must be between 0-99!");
            this.userlimit = userlimit;
            return this;
        }

        /**
         * Sets the position for this channel.
         *
         * @param  position
         *         The position for the channel
         *
         * @return This ChannelData instance for chaining convenience
         */
        @Nonnull
        public ChannelData setPosition(@Nullable Integer position)
        {
            this.position = position;
            return this;
        }

        /**
         * Adds a {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverride} to this channel
         * with the provided {@link RoleData RoleData}!
         * <br>Use {@link #newRole() GuildAction.newRole()} to retrieve an instance of RoleData.
         *
         * @param  role
         *         The target role
         * @param  allow
         *         The permissions to grant in the override
         * @param  deny
         *         The permissions to deny in the override
         *
         * @throws java.lang.IllegalArgumentException
         *         If the provided role is {@code null}
         *
         * @return This ChannelData instance for chaining convenience
         */
        @Nonnull
        public ChannelData addPermissionOverride(@Nonnull GuildActionImpl.RoleData role, long allow, long deny)
        {
            Checks.notNull(role, "Role");
            this.overrides.add(new PermOverrideData(PermOverrideData.ROLE_TYPE, role.id, allow, deny));
            return this;
        }

        /**
         * Adds a {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverride} to this channel
         * with the provided {@link GuildAction.RoleData RoleData}!
         * <br>Use {@link #newRole() GuildAction.newRole()} to retrieve an instance of RoleData.
         *
         * @param  role
         *         The target role
         * @param  allow
         *         The permissions to grant in the override
         * @param  deny
         *         The permissions to deny in the override
         *
         * @throws java.lang.IllegalArgumentException
         *         <ul>
         *             <li>If the provided role is {@code null}</li>
         *             <li>If any permission is {@code null}</li>
         *         </ul>
         *
         * @return This ChannelData instance for chaining convenience
         */
        @Nonnull
        public ChannelData addPermissionOverride(@Nonnull GuildActionImpl.RoleData role, @Nullable Collection<Permission> allow, @Nullable Collection<Permission> deny)
        {
            long allowRaw = 0;
            long denyRaw = 0;
            if (allow != null)
            {
                Checks.noneNull(allow, "Granted Permissions");
                allowRaw = Permission.getRaw(allow);
            }
            if (deny != null)
            {
                Checks.noneNull(deny, "Denied Permissions");
                denyRaw = Permission.getRaw(deny);
            }
            return addPermissionOverride(role, allowRaw, denyRaw);
        }

        @Nonnull
        @Override
        public DataObject toData()
        {
            final DataObject o = DataObject.empty();
            o.put("name", name);
            o.put("type", type.getId());
            if (topic != null)
                o.put("topic", topic);
            if (nsfw != null)
                o.put("nsfw", nsfw);
            if (bitrate != null)
                o.put("bitrate", bitrate);
            if (userlimit != null)
                o.put("user_limit", userlimit);
            if (position != null)
                o.put("position", position);
            if (!overrides.isEmpty())
                o.put("permission_overwrites", overrides);
            return o;
        }
    }
}
