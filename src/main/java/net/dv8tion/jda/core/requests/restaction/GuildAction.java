/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter & Florian Spieß
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
import java.awt.Color;
import java.util.*;

/**
 * {@link net.dv8tion.jda.core.requests.RestAction RestAction} extension
 * specifically designed to allow for the creation of {@link net.dv8tion.jda.core.entities.Guild Guilds}.
 * <br>This is available to all account types but may undergo certain restrictions by Discord.
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

    @CheckReturnValue
    public GuildAction setRegion(Region region)
    {
        Checks.check(region == null || !region.isVip(), "Cannot create a Guild with a VIP voice region!");
        this.region = region;
        return this;
    }

    @CheckReturnValue
    public GuildAction setIcon(Icon icon)
    {
        this.icon = icon;
        return this;
    }

    @CheckReturnValue
    public GuildAction setName(String name)
    {
        Checks.notBlank(name, "Name");
        name = name.trim();
        Checks.check(name.length() >= 2 && name.length() <= 100, "Name must have 2-100 characters in length!");
        this.name = name;
        return this;
    }

    @CheckReturnValue
    public GuildAction setVerificationLevel(Guild.VerificationLevel level)
    {
        this.verificationLevel = level;
        return this;
    }

    @CheckReturnValue
    public GuildAction setNotificationLevel(Guild.NotificationLevel level)
    {
        this.notificationLevel = level;
        return this;
    }

    @CheckReturnValue
    public GuildAction setExplicitContentLevel(Guild.ExplicitContentLevel level)
    {
        this.explicitContentLevel = level;
        return this;
    }

    // Channels

    @CheckReturnValue
    public GuildAction addChannel(ChannelData channel)
    {
        Checks.notNull(channel, "Channel");
        this.channels.add(channel);
        return this;
    }

    @CheckReturnValue
    public ChannelData getChannel(int index)
    {
        return this.channels.get(index);
    }

    // Roles

    @CheckReturnValue
    public RoleData getPublicRole()
    {
        return this.roles.get(0);
    }

    @CheckReturnValue
    public RoleData getRole(int index)
    {
        return this.roles.get(index);
    }

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

        public RoleData addPermissions(Collection<Permission> permissions)
        {
            Checks.noneNull(permissions, "Permissions");
            if (this.permissions == null)
                this.permissions = 0L;
            this.permissions |= Permission.getRaw(permissions);
            return this;
        }

        public RoleData setName(String name)
        {
            checkPublic("name");
            this.name = name;
            return this;
        }

        public RoleData setColor(Color color)
        {
            checkPublic("color");
            this.color = color == null ? null : color.getRGB();
            return this;
        }

        public RoleData setPosition(Integer position)
        {
            checkPublic("position");
            this.position = position;
            return this;
        }

        public RoleData setMentionable(Boolean mentionable)
        {
            checkPublic("mentionable");
            this.mentionable = mentionable;
            return this;
        }

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

        public ChannelData(ChannelType type, String name)
        {
            Checks.notBlank(name, "Name");
            Checks.check(type == ChannelType.TEXT || type == ChannelType.VOICE, "Can only create channels of type TEXT or VOICE in GuildAction!");
            Checks.check(name.length() >= 2 && name.length() <= 100, "Channel name has to be between 2-100 characters long!");

            this.type = type;
            this.name = name;
        }

        public ChannelData setTopic(String topic)
        {
            if (topic != null && topic.length() > 1024)
                throw new IllegalArgumentException("Channel Topic must not be greater than 1024 in length!");
            this.topic = topic;
            return this;
        }

        public ChannelData setNSFW(Boolean nsfw)
        {
            this.nsfw = nsfw;
            return this;
        }

        public ChannelData setBitrate(Integer bitrate)
        {
            if (bitrate != null)
            {
                Checks.check(bitrate >= 8000, "Bitrate must be greater than 8000.");
                Checks.check(bitrate <= 128000, "Bitrate must be less than 128000.");
            }
            this.bitrate = bitrate;
            return this;
        }

        public ChannelData setUserlimit(Integer userlimit)
        {
            if (userlimit != null && (userlimit < 0 || userlimit > 99))
                throw new IllegalArgumentException("Userlimit must be between 0-99!");
            this.userlimit = userlimit;
            return this;
        }

        public ChannelData setPosition(Integer position)
        {
            this.position = position;
            return this;
        }

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

        public ChannelData addPermissionOverride(RoleData role, Collection<Permission> allow, Collection<Permission> deny)
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
