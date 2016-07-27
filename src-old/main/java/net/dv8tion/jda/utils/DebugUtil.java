/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
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
package net.dv8tion.jda.utils;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.*;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.handle.EntityBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class DebugUtil
{
    public static JSONObject fromJDA(JDA jda)
    {
        return fromJDA(jda, true, true, true, true, true);
    }

    public static JSONObject fromJDA(JDA iJDA, boolean includeUsers, boolean includeGuilds, boolean includeGuildUsers,
                                     boolean includeChannels, boolean includePrivateChannels)
    {
        JDAImpl jda = (JDAImpl) iJDA;
        JSONObject obj = new JSONObject();
        obj.put("self_info", fromUser(jda.getSelfInfo()))
                .put("proxy", jda.getGlobalProxy() == null ? JSONObject.NULL : new JSONObject()
                    .put("host", jda.getGlobalProxy().getHostName())
                    .put("port", jda.getGlobalProxy().getPort()))
                .put("response_total", jda.getResponseTotal())
                .put("audio_enabled", jda.isAudioEnabled())
                .put("auto_reconnect", jda.isAutoReconnect());

        JSONArray array = new JSONArray();
        for (Object listener : jda.getRegisteredListeners())
            array.put(listener.getClass().getCanonicalName());
        obj.put("event_listeners", array);


        try
        {
            Field f = EntityBuilder.class.getDeclaredField("cachedJdaGuildJsons");
            f.setAccessible(true);
            HashMap<String, JSONObject> cachedJsons = ((HashMap<JDA, HashMap<String, JSONObject>>) f.get(null)).get(jda);

            array = new JSONArray();
            for (String guildId : cachedJsons.keySet())
                array.put(guildId);
            obj.put("second_pass_json_guild_ids", array);

            f = EntityBuilder.class.getDeclaredField("cachedJdaGuildCallbacks");
            f.setAccessible(true);
            HashMap<String, Consumer<Guild>> cachedCallbacks = ((HashMap<JDA, HashMap<String, Consumer<Guild>>>) f.get(null)).get(jda);

            array = new JSONArray();
            for (String guildId : cachedCallbacks.keySet())
                array.put(guildId);
            obj.put("second_pass_callback_guild_ids", array);
        }
        catch (NoSuchFieldException e)
        {
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }

        if (includeGuilds)
        {
            array = new JSONArray();
            for (Guild guild : jda.getGuilds())
                array.put(fromGuild(guild, includeGuildUsers, includeChannels, true, true));
            obj.put("guilds", array);
        }

        if (includePrivateChannels)
        {
            array = new JSONArray();
            for (PrivateChannel chan : jda.getPrivateChannels())
                array.put(fromPrivateChannel(chan, false));
            obj.put("private_channels", array);

            array = new JSONArray();
            for (Map.Entry<String, String> entry : jda.getOffline_pms().entrySet())
            {
                array.put(new JSONObject()
                        .put("id", entry.getValue())
                        .put("user_id", entry.getKey()));
            }
            obj.put("offline_private_channels", array);
        }

        if (includeUsers)
        {
            array = new JSONArray();
            for (User user : jda.getUsers())
                array.put(fromUser(user));
            obj.put("users", array);
        }

        return obj;
    }

    public static JSONObject fromGuild(Guild guild)
    {
        return fromGuild(guild, true, true, true, true);
    }

    public static JSONObject fromGuild(Guild guild, boolean includeUsers, boolean includeChannels, boolean includeRoles, boolean includeVoiceStatuses)
    {
        JSONObject obj = new JSONObject();
        obj.put("id", guild.getId())
                .put("name", guild.getName())
                .put("owner_id", guild.getOwner().getId())
                .put("region", guild.getRegion())
                .put("icon_url", guild.getIconUrl())
                .put("available", guild.isAvailable())
                .put("verification_level", guild.getVerificationLevel())
                .put("verification_passed", guild.checkVerification())
                .put("afk_timeout", guild.getAfkTimeout())
                .put("afk_channel_id", guild.getAfkChannelId());

        JSONArray array = new JSONArray();
        for (Channel chan : guild.getTextChannels())
            array.put(chan.getId());
        obj.put("text_channel_ids", array);

        array = new JSONArray();
        for (Channel chan : guild.getVoiceChannels())
            array.put(chan.getId());
        obj.put("text_channel_ids", array);

        array = new JSONArray();
        for (Role role : guild.getRoles())
            array.put(role.getId());
        obj.put("role_ids", array);

        if (includeChannels)
        {
            array = new JSONArray();
            for (Channel chan : guild.getTextChannels())
                array.put(fromGuildChannel(chan, false, false, true));
            obj.put("text_channels", array);

            array = new JSONArray();
            for (Channel chan : guild.getVoiceChannels())
                array.put(fromGuildChannel(chan, false, false, true));
            obj.put("voice_channels", array);
        }

        if (includeRoles)
        {
            array = new JSONArray();
            for (Role role : guild.getRoles())
                array.put(fromRole(role, false, true, true));
            obj.put("roles", array);
        }

        return obj;
    }

    public static JSONObject fromGuildChannel(Channel chan)
    {
        return fromGuildChannel(chan, true, true, true);
    }

    public static JSONObject fromPrivateChannel(PrivateChannel chan, boolean includeUser)
    {
        JSONObject obj = new JSONObject();
        obj.put("id", chan.getId())
                .put("user_id", chan.getUser().getId());

        if (includeUser)
            obj.put("user", fromUser(chan.getUser()));

        return obj;
    }

    public static JSONObject fromGuildChannel(Channel chan, boolean includeUsers, boolean includeGuild, boolean includePermOverrides)
    {
        JSONObject obj = new JSONObject();
        obj.put("id", chan.getId())
                .put("name", chan.getName())
                .put("position", chan.getPosition())
                .put("position_raw", chan.getPositionRaw())
                .put("guild_id", chan.getGuild().getId());
        if (chan instanceof TextChannel)
            obj.put("topic", chan.getTopic());

        if (includePermOverrides)
        {
            JSONArray permsArray = new JSONArray();
            for (PermissionOverride permOver : chan.getUserPermissionOverrides())
            {
                permsArray.put(fromPermOverride(permOver, false, false, false, true));
            }
            obj.put("user_permission_overrides", permsArray);

            permsArray = new JSONArray();
            for (PermissionOverride permOver : chan.getRolePermissionOverrides())
            {
                permsArray.put(fromPermOverride(permOver, false, false, false, true));
            }
            obj.put("role_permission_overrides", permsArray);
        }

        if (includeGuild)
            obj.put("guild", fromGuild(chan.getGuild(), false, false, false, false));

        if (includeUsers)
        {
            JSONArray userArray = new JSONArray();
            for (User user : chan.getUsers())
            {
                userArray.put(fromUser(user));
            }
            obj.put("users", userArray);
        }
        return obj;
    }

    public static JSONObject fromUser(User user)
    {
        JSONObject obj = new JSONObject();
        obj.put("id", user.getId())
                .put("username", user.getUsername())
                .put("disc", user.getDiscriminator())
                .put("avatar_url", user.getAvatarUrl())
                .put("online_status", user.getOnlineStatus())
                .put("current_game", user.getCurrentGame())
                .put("is_bot", user.isBot());
        return obj;
    }

    public static JSONObject fromRole(Role role)
    {
        return fromRole(role, true, true, true);
    }

    public static JSONObject fromRole(Role role, boolean includeGuild, boolean includeUsers, boolean includePermissions)
    {
        JSONObject obj = new JSONObject();
        obj.put("id", role.getId())
                .put("name", role.getName())
                .put("position", role.getPosition())
                .put("position_raw", role.getPositionRaw())
                .put("color", role.getColor())
                .put("hoisted", role.isGrouped())
                .put("managed", role.isManaged())
                .put("mentionable", role.isMentionable())
                .put("guild_id", role.getGuild().getId())
                .put("permissions_raw", role.getPermissionsRaw());

        if (includeGuild)
            obj.put("guild", fromGuild(role.getGuild(), false, false, false, false));

        if (includeUsers)
        {
            JSONArray userArray = new JSONArray();
            for (User user : role.getGuild().getUsersWithRole(role))
                userArray.put(user.getId());
            obj.put("users", userArray);
        }

        if (includePermissions)
        {
            JSONArray permArray = new JSONArray();
            for (Permission perm : role.getPermissions())
                permArray.put(perm.toString());
            obj.put("permissions", permArray);
        }
        return obj;
    }

    public static JSONObject fromPermOverride(PermissionOverride permOver)
    {
        return fromPermOverride(permOver, true, true, true, true);
    }

    public static JSONObject fromPermOverride(PermissionOverride permOver, boolean includeGuild, boolean includeChannel,
                                              boolean includeRoleOrUserInfo, boolean includePermissions)
    {
        JSONObject obj = new JSONObject();
        obj.put("type", permOver.isRoleOverride() ? "role" : "user")
                .put("allowed_raw", permOver.getAllowedRaw())
                .put("inherited_raw", permOver.getInheritRaw())
                .put("denied_raw", permOver.getDeniedRaw())
                .put("channel_id", permOver.getChannel().getId())
                .put("guild_id", permOver.getGuild().getId());

        if (permOver.isRoleOverride())
            obj.put("role_id", permOver.getRole().getId());
        else
            obj.put("user_id", permOver.getUser().getId());

        if (includePermissions)
        {
            JSONArray permArray = new JSONArray();
            for (Permission perm : permOver.getAllowed())
                permArray.put(perm.toString());
            obj.put("allowed_perms", permArray);

            permArray = new JSONArray();
            for (Permission perm : permOver.getInherit())
                permArray.put(perm.toString());
            obj.put("inherited_perms", permArray);

            permArray = new JSONArray();
            for (Permission perm : permOver.getDenied())
                permArray.put(perm.toString());
            obj.put("denied_perms", permArray);
        }

        if (includeChannel)
            obj.put("channel", fromGuildChannel(permOver.getChannel()));

        if (includeGuild)
            obj.put("guild", fromGuild(permOver.getGuild()));

        if (includeRoleOrUserInfo)
        {
            if (permOver.isRoleOverride())
                obj.put("role", fromRole(permOver.getRole(), false, false, includePermissions));
            else
                obj.put("user", permOver.getUser());
        }
        return obj;
    }
}
