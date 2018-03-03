/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.core.requests.restaction;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.Region;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.utils.Checks;
import okhttp3.RequestBody;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONString;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.awt.Color;
import java.util.*;
import java.util.function.BooleanSupplier;

/**
 * {@link net.dv8tion.jda.core.requests.RestAction RestAction} extension
 * specifically designed to allow for the creation of {@link net.dv8tion.jda.core.entities.Guild Guilds}.
 * <br>This is available to all account types but may undergo certain restrictions by Discord.
 *
 * @since  3.4.0
 */
public class GuildAction extends RestAction<Void>
{
    protected String name;
    protected Region region;
    protected Icon icon;
    protected Guild.VerificationLevel verificationLevel;
    protected Guild.NotificationLevel notificationLevel;
    protected Guild.ExplicitContentLevel explicitContentLevel;

    protected final List<RoleData> roles;
    protected final List<ChannelData> channels;

    public GuildAction(JDA api, String name)
    {
        super(api, Route.Guilds.CREATE_GUILD.compile());
        this.setName(name);

        this.roles = new LinkedList<>();
        this.channels = new LinkedList<>();
        // public role is the first element
        this.roles.add(new RoleData(0));
    }

    @Override
    public GuildAction setCheck(BooleanSupplier checks)
    {
        return (GuildAction) super.setCheck(checks);
    }

    /**
     * Sets the voice {@link net.dv8tion.jda.core.Region Region} of
     * the resulting {@link net.dv8tion.jda.core.entities.Guild Guild}.
     *
     * @param  region
     *         The {@link net.dv8tion.jda.core.Region Region} to use
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided region is a VIP region as per {@link net.dv8tion.jda.core.Region#isVip() Region.isVip()}
     *
     * @return The current GuildAction for chaining convenience
     */
    @CheckReturnValue
    public GuildAction setRegion(Region region)
    {
        Checks.check(region == null || !region.isVip(), "Cannot create a Guild with a VIP voice region!");
        this.region = region;
        return this;
    }

    /**
     * Sets the {@link net.dv8tion.jda.core.entities.Icon Icon}
     * for the resulting {@link net.dv8tion.jda.core.entities.Guild Guild}
     *
     * @param  icon
     *         The {@link net.dv8tion.jda.core.entities.Icon Icon} to use
     *
     * @return The current GuildAction for chaining convenience
     */
    @CheckReturnValue
    public GuildAction setIcon(Icon icon)
    {
        this.icon = icon;
        return this;
    }

    /**
     * Sets the name for the resulting {@link net.dv8tion.jda.core.entities.Guild Guild}
     *
     * @param  name
     *         The name to use
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided name is {@code null}, blank or not between 2-100 characters long
     *
     * @return The current GuildAction for chaining convenience
     */
    @CheckReturnValue
    public GuildAction setName(String name)
    {
        Checks.notBlank(name, "Name");
        name = name.trim();
        Checks.check(name.length() >= 2 && name.length() <= 100, "Name must have 2-100 characters in length!");
        this.name = name;
        return this;
    }

    /**
     * Sets the {@link net.dv8tion.jda.core.entities.Guild.VerificationLevel VerificationLevel}
     * for the resulting {@link net.dv8tion.jda.core.entities.Guild Guild}
     *
     * @param  level
     *         The {@link net.dv8tion.jda.core.entities.Guild.VerificationLevel VerificationLevel} to use
     *
     * @return The current GuildAction for chaining convenience
     */
    @CheckReturnValue
    public GuildAction setVerificationLevel(Guild.VerificationLevel level)
    {
        this.verificationLevel = level;
        return this;
    }

    /**
     * Sets the {@link net.dv8tion.jda.core.entities.Guild.NotificationLevel NotificationLevel}
     * for the resulting {@link net.dv8tion.jda.core.entities.Guild Guild}
     *
     * @param  level
     *         The {@link net.dv8tion.jda.core.entities.Guild.NotificationLevel NotificationLevel} to use
     *
     * @return The current GuildAction for chaining convenience
     */
    @CheckReturnValue
    public GuildAction setNotificationLevel(Guild.NotificationLevel level)
    {
        this.notificationLevel = level;
        return this;
    }

    /**
     * Sets the {@link net.dv8tion.jda.core.entities.Guild.ExplicitContentLevel ExplicitContentLevel}
     * for the resulting {@link net.dv8tion.jda.core.entities.Guild Guild}
     *
     * @param  level
     *         The {@link net.dv8tion.jda.core.entities.Guild.ExplicitContentLevel ExplicitContentLevel} to use
     *
     * @return The current GuildAction for chaining convenience
     */
    @CheckReturnValue
    public GuildAction setExplicitContentLevel(Guild.ExplicitContentLevel level)
    {
        this.explicitContentLevel = level;
        return this;
    }

    // Channels

    /**
     * Adds a {@link net.dv8tion.jda.core.entities.Channel Channel} to the resulting
     * Guild. This cannot be of type {@link net.dv8tion.jda.core.entities.ChannelType#CATEGORY CATEGORY}!
     *
     * @param  channel
     *         The {@link net.dv8tion.jda.core.requests.restaction.GuildAction.ChannelData ChannelData}
     *         to use for the construction of the Channel
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided channel is {@code null}!
     *
     * @return The current GuildAction for chaining convenience
     */
    @CheckReturnValue
    public GuildAction addChannel(ChannelData channel)
    {
        Checks.notNull(channel, "Channel");
        this.channels.add(channel);
        return this;
    }

    /**
     * Gets the {@link net.dv8tion.jda.core.requests.restaction.GuildAction.ChannelData ChannelData}
     * of the specified index. The index is 0 based on insertion order of {@link #addChannel(GuildAction.ChannelData)}!
     *
     * @param  index
     *         The 0 based index of the channel
     *
     * @throws java.lang.IndexOutOfBoundsException
     *         If the provided index is not in bounds
     *
     * @return The current GuildAction for chaining convenience
     */
    @CheckReturnValue
    public ChannelData getChannel(int index)
    {
        return this.channels.get(index);
    }

    /**
     * Removes the {@link net.dv8tion.jda.core.requests.restaction.GuildAction.ChannelData ChannelData}
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
    @CheckReturnValue
    public ChannelData removeChannel(int index)
    {
        return this.channels.remove(index);
    }

    /**
     * Removes the provided {@link net.dv8tion.jda.core.requests.restaction.GuildAction.ChannelData ChannelData}
     * from this GuildAction if present.
     *
     * @param  data
     *         The ChannelData to remove
     *
     * @return The current GuildAction for chaining convenience
     */
    @CheckReturnValue
    public GuildAction removeChannel(ChannelData data)
    {
        this.channels.remove(data);
        return this;
    }

    /**
     * Creates a new {@link net.dv8tion.jda.core.requests.restaction.GuildAction.ChannelData ChannelData}
     * instance and adds it to this GuildAction.
     *
     * @param  type
     *         The {@link net.dv8tion.jda.core.entities.ChannelType ChannelType} of the resulting Channel
     *         <br>This may be of type {@link net.dv8tion.jda.core.entities.ChannelType#TEXT TEXT} or {@link net.dv8tion.jda.core.entities.ChannelType#VOICE VOICE}!
     * @param  name
     *         The name of the channel. This must be alphanumeric with underscores for type TEXT
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If provided with an invalid ChannelType</li>
     *             <li>If the provided name is {@code null} or blank</li>
     *             <li>If the provided name is not between 2-100 characters long</li>
     *             <li>If the type is TEXT and the provided name is not alphanumeric with underscores</li>
     *         </ul>
     *
     * @return The new ChannelData instance
     */
    @CheckReturnValue
    public ChannelData newChannel(ChannelType type, String name)
    {
        ChannelData data = new ChannelData(type, name);
        addChannel(data);
        return data;
    }

    // Roles

    /**
     * Retrieves the {@link net.dv8tion.jda.core.requests.restaction.GuildAction.RoleData RoleData} for the
     * public role ({@link net.dv8tion.jda.core.entities.Guild#getPublicRole() Guild.getPublicRole()}) for the resulting Guild.
     * <br>The public role is also known in the official client as the {@code @everyone} role.
     *
     * <p><b>You can only change the permissions of the public role!</b>
     *
     * @return RoleData of the public role
     */
    @CheckReturnValue
    public RoleData getPublicRole()
    {
        return this.roles.get(0);
    }

    /**
     * Retrieves the {@link net.dv8tion.jda.core.requests.restaction.GuildAction.RoleData RoleData} for the
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
    @CheckReturnValue
    public RoleData getRole(int index)
    {
        return this.roles.get(index);
    }

    /**
     * Creates and add a new {@link net.dv8tion.jda.core.requests.restaction.GuildAction.RoleData RoleData} object
     * representing a Role for the resulting Guild.
     *
     * <p>This can be used in {@link GuildAction.ChannelData#addPermissionOverride(GuildAction.RoleData, long, long) ChannelData.addPermissionOverride(...)}.
     * <br>You may change any properties of this {@link net.dv8tion.jda.core.requests.restaction.GuildAction.RoleData RoleData} instance!
     *
     * @return RoleData for the new Role
     */
    @CheckReturnValue
    public RoleData newRole()
    {
        final RoleData role = new RoleData(roles.size());
        this.roles.add(role);
        return role;
    }

    @Override
    protected RequestBody finalizeData()
    {
        final JSONObject object = new JSONObject();
        object.put("name", name);
        object.put("roles", new JSONArray(roles));
        if (!channels.isEmpty())
            object.put("channels", new JSONArray(channels));
        if (icon != null)
            object.put("icon", icon.getEncoding());
        if (verificationLevel != null)
            object.put("verification_level", verificationLevel.getKey());
        if (notificationLevel != null)
            object.put("default_message_notifications", notificationLevel.getKey());
        if (explicitContentLevel != null)
            object.put("explicit_content_filter", explicitContentLevel.getKey());
        if (region != null)
            object.put("region", region.getKey());
        return getRequestBody(object);
    }

    @Override
    protected void handleResponse(Response response, Request<Void> request)
    {
        if (response.isOk())
            request.onSuccess(null);
        else
            request.onFailure(response);
    }

    /**
     * Mutable object containing information on a {@link net.dv8tion.jda.core.entities.Role Role}
     * of the resulting {@link net.dv8tion.jda.core.entities.Guild Guild} that is constructed by a GuildAction instance
     *
     * <p>This may be used in {@link net.dv8tion.jda.core.requests.restaction.GuildAction.ChannelData#addPermissionOverride(GuildAction.RoleData, long, long)}  ChannelData.addPermissionOverride(...)}!
     */
    public static class RoleData implements JSONString
    {
        protected final long id;
        protected final boolean isPublicRole;

        protected Long permissions;
        protected String name;
        protected Integer color;
        protected Integer position;
        protected Boolean mentionable, hoisted;

        protected RoleData(long id)
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
         * @throws java.lang.IllegalArgumentException
         *         If the provided permissions are negative or exceed the maximum permissions
         *
         * @return The current RoleData instance for chaining convenience
         */
        public RoleData setPermissionsRaw(Long rawPermissions)
        {
            if (rawPermissions != null)
            {
                Checks.notNegative(rawPermissions, "Raw Permissions");
                Checks.check(rawPermissions <= Permission.ALL_PERMISSIONS, "Provided permissions may not be greater than a full permission set!");
            }
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
        public RoleData addPermissions(Permission... permissions)
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
        public RoleData addPermissions(Collection<Permission> permissions)
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
        public RoleData setName(String name)
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
        public RoleData setColor(Color color)
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
        public RoleData setColor(Integer color)
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
        public RoleData setPosition(Integer position)
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
        public RoleData setMentionable(Boolean mentionable)
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
        public RoleData setHoisted(Boolean hoisted)
        {
            checkPublic("hoisted");
            this.hoisted = hoisted;
            return this;
        }

        @Override
        public String toJSONString()
        {
            final JSONObject o = new JSONObject().put("id", Long.toUnsignedString(id));
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
            return o.toString();
        }

        protected void checkPublic(String comment)
        {
            if (isPublicRole)
                throw new IllegalStateException("Cannot modify " + comment + " for the public role!");
        }
    }

    /**
     * Channel information used for the creation of {@link net.dv8tion.jda.core.entities.Channel Channels} within
     * the construction of a {@link net.dv8tion.jda.core.entities.Guild Guild} via GuildAction.
     *
     * <p>Use with {@link net.dv8tion.jda.core.requests.restaction.GuildAction#addChannel(GuildAction.ChannelData) GuildAction.addChannel(ChannelData)}.
     */
    public static class ChannelData implements JSONString
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
         * a {@link net.dv8tion.jda.core.entities.Channel Channel} to be used in the construction
         * of a {@link net.dv8tion.jda.core.entities.Guild Guild}!
         *
         * @param  type
         *         The {@link net.dv8tion.jda.core.entities.ChannelType ChannelType} of the resulting Channel
         *         <br>This may be of type {@link net.dv8tion.jda.core.entities.ChannelType#TEXT TEXT} or {@link net.dv8tion.jda.core.entities.ChannelType#VOICE VOICE}!
         * @param  name
         *         The name of the channel. This must be alphanumeric with underscores for type TEXT
         *
         * @throws java.lang.IllegalArgumentException
         *         <ul>
         *             <li>If provided with an invalid ChannelType</li>
         *             <li>If the provided name is {@code null} or blank</li>
         *             <li>If the provided name is not between 2-100 characters long</li>
         *             <li>If the type is TEXT and the provided name is not alphanumeric with underscores</li>
         *         </ul>
         */
        public ChannelData(ChannelType type, String name)
        {
            Checks.notBlank(name, "Name");
            Checks.check(type == ChannelType.TEXT || type == ChannelType.VOICE, "Can only create channels of type TEXT or VOICE in GuildAction!");
            Checks.check(name.length() >= 2 && name.length() <= 100, "Channel name has to be between 2-100 characters long!");
            Checks.check(type == ChannelType.VOICE || name.matches("[a-zA-Z0-9-_]+"), "Channels of type TEXT must have a name in alphanumeric with underscores!");

            this.type = type;
            this.name = name;
        }

        /**
         * Sets the topic for this channel.
         * <br>These are only relevant to channels of type {@link net.dv8tion.jda.core.entities.ChannelType#TEXT TEXT}.
         *
         * @param  topic
         *         The topic for the channel
         *
         * @throws java.lang.IllegalArgumentException
         *         If the provided topic is bigger than 1024 characters
         *
         * @return This ChannelData instance for chaining convenience
         */
        public ChannelData setTopic(String topic)
        {
            if (topic != null && topic.length() > 1024)
                throw new IllegalArgumentException("Channel Topic must not be greater than 1024 in length!");
            this.topic = topic;
            return this;
        }

        /**
         * Sets the whether this channel should be marked NSFW.
         * <br>These are only relevant to channels of type {@link net.dv8tion.jda.core.entities.ChannelType#TEXT TEXT}.
         *
         * @param  nsfw
         *         Whether this channel should be marked NSFW
         *
         * @return This ChannelData instance for chaining convenience
         */
        public ChannelData setNSFW(Boolean nsfw)
        {
            this.nsfw = nsfw;
            return this;
        }

        /**
         * Sets the bitrate for this channel.
         * <br>These are only relevant to channels of type {@link net.dv8tion.jda.core.entities.ChannelType#VOICE VOICE}.
         *
         * @param  bitrate
         *         The bitrate for the channel (8000-96000)
         *
         * @throws java.lang.IllegalArgumentException
         *         If the provided bitrate is not between 8000-96000
         *
         * @return This ChannelData instance for chaining convenience
         */
        public ChannelData setBitrate(Integer bitrate)
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
         * <br>These are only relevant to channels of type {@link net.dv8tion.jda.core.entities.ChannelType#VOICE VOICE}.
         *
         * @param  userlimit
         *         The userlimit for the channel (0-99)
         *
         * @throws java.lang.IllegalArgumentException
         *         If the provided userlimit is not between 0-99
         *
         * @return This ChannelData instance for chaining convenience
         */
        public ChannelData setUserlimit(Integer userlimit)
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
        public ChannelData setPosition(Integer position)
        {
            this.position = position;
            return this;
        }

        /**
         * Adds a {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride} to this channel
         * with the provided {@link net.dv8tion.jda.core.requests.restaction.GuildAction.RoleData RoleData}!
         * <br>Use {@link GuildAction#newRole() GuildAction.newRole()} to retrieve an instance of RoleData.
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
         *             <li>If the provided allow value is negative or exceeds maximum permissions</li>
         *             <li>If the provided deny value is negative or exceeds maximum permissions</li>
         *         </ul>
         *
         * @return This ChannelData instance for chaining convenience
         */
        public ChannelData addPermissionOverride(RoleData role, long allow, long deny)
        {
            Checks.notNull(role, "Role");
            Checks.notNegative(allow, "Granted permissions value");
            Checks.notNegative(deny, "Denied permissions value");
            Checks.check(allow <= Permission.ALL_PERMISSIONS, "Specified allow value may not be greater than a full permission set");
            Checks.check(deny <= Permission.ALL_PERMISSIONS,  "Specified deny value may not be greater than a full permission set");
            this.overrides.add(new PermOverrideData(PermOverrideData.ROLE_TYPE, role.id, allow, deny));
            return this;
        }

        /**
         * Adds a {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride} to this channel
         * with the provided {@link net.dv8tion.jda.core.requests.restaction.GuildAction.RoleData RoleData}!
         * <br>Use {@link GuildAction#newRole() GuildAction.newRole()} to retrieve an instance of RoleData.
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
        public ChannelData addPermissionOverride(RoleData role, @Nullable Collection<Permission> allow, @Nullable Collection<Permission> deny)
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

        @Override
        public String toJSONString()
        {
            final JSONObject o = new JSONObject();
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
            return o.toString();
        }
    }
}
