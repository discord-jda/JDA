/**
 *    Copyright 2015-2016 Austin Keener & Michael Ritter
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
package net.dv8tion.jda.handle;

import net.dv8tion.jda.EmbedType;
import net.dv8tion.jda.OnlineStatus;
import net.dv8tion.jda.Region;
import net.dv8tion.jda.entities.*;
import net.dv8tion.jda.entities.MessageEmbed.Provider;
import net.dv8tion.jda.entities.MessageEmbed.Thumbnail;
import net.dv8tion.jda.entities.MessageEmbed.VideoInfo;
import net.dv8tion.jda.entities.impl.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EntityBuilder
{
    private final JDAImpl api;

    public EntityBuilder(JDAImpl api)
    {
        this.api = api;
    }

    public Guild createGuild(JSONObject guild)
    {
        String id = guild.getString("id");
        GuildImpl guildObj = ((GuildImpl) api.getGuildMap().get(id));
        if (guildObj == null)
        {
            guildObj = new GuildImpl(api, id);
            api.getGuildMap().put(id, guildObj);
        }
        guildObj
            .setIconId(guild.isNull("icon") ? null : guild.getString("icon"))
            .setRegion(Region.fromKey(guild.getString("region")))
            .setName(guild.getString("name"))
            .setOwnerId(guild.getString("owner_id"))
            .setAfkTimeout(guild.getInt("afk_timeout"))
            .setAfkChannelId(guild.isNull("afk_channel_id") ? null : guild.getString("afk_channel_id"));


        JSONArray roles = guild.getJSONArray("roles");
        for (int i = 0; i < roles.length(); i++)
        {
            Role role = createRole(roles.getJSONObject(i), guildObj.getId());
            guildObj.getRolesMap().put(role.getId(), role);
            if (role.getName().equals("@everyone"))
            {
                guildObj.setPublicRole(role);
            }
        }

        if (guild.has("members"))
        {
            JSONArray members = guild.getJSONArray("members");
            Map<String, Role> rolesMap = guildObj.getRolesMap();
            Map<User, List<Role>> userRoles = guildObj.getUserRoles();
            for (int i = 0; i < members.length(); i++)
            {
                JSONObject member = members.getJSONObject(i);
                User user = createUser(member.getJSONObject("user"));
                userRoles.put(user, new ArrayList<>());
                JSONArray roleArr = member.getJSONArray("roles");
                for (int j = 0; j < roleArr.length(); j++)
                {
                    String roleId = roleArr.getString(j);
                    userRoles.get(user).add(rolesMap.get(roleId));
                }
                VoiceStatusImpl voiceStatus = new VoiceStatusImpl(user, guildObj);
                voiceStatus.setServerDeaf(member.getBoolean("deaf"));
                voiceStatus.setServerMute(member.getBoolean("mute"));
                guildObj.getVoiceStatusMap().put(user, voiceStatus);
            }
        }

        if (guild.has("channels"))
        {
            JSONArray channels = guild.getJSONArray("channels");
            for (int i = 0; i < channels.length(); i++)
            {
                JSONObject channel = channels.getJSONObject(i);
                String type = channel.getString("type");
                if (type.equalsIgnoreCase("text"))
                {
                    createTextChannel(channel, guildObj.getId());
                }
                else if (type.equalsIgnoreCase("voice"))
                {
                    createVoiceChannel(channel, guildObj.getId());
                }
            }
        }

        if (guild.has("presences"))
        {
            JSONArray presences = guild.getJSONArray("presences");
            for (int i = 0; i < presences.length(); i++)
            {
                JSONObject presence = presences.getJSONObject(i);
                UserImpl user = ((UserImpl) api.getUserMap().get(presence.getJSONObject("user").getString("id")));
                if (user == null)
                {
                    //corresponding user to presence not found... ignoring
                    continue;
                }
                user
                        .setCurrentGame(presence.isNull("game") || presence.getJSONObject("game").isNull("name") ? null : presence.getJSONObject("game").get("name").toString())
                        .setOnlineStatus(OnlineStatus.fromKey(presence.getString("status")));
            }
        }

        if (guild.has("voice_states"))
        {
            JSONArray voiceStates = guild.getJSONArray("voice_states");
            for (int i = 0; i < voiceStates.length(); i++)
            {
                JSONObject voiceState = voiceStates.getJSONObject(i);
                User user = api.getUserById(voiceState.getString("user_id"));
                if (user == null)
                    throw new IllegalArgumentException("When attempting to create a Guild, we were provided with a voice state pertaining to an unknown User. JSON: " + guild);

                createVoiceStatus(voiceState, guildObj, user);
            }
        }

        return guildObj;
    }

    public TextChannel createTextChannel(JSONObject json, String guildId)
    {
        String id = json.getString("id");
        TextChannelImpl channel = (TextChannelImpl) api.getChannelMap().get(id);
        if (channel == null)
        {
            GuildImpl guild = ((GuildImpl) api.getGuildMap().get(guildId));
            channel = new TextChannelImpl(id, guild);
            guild.getTextChannelsMap().put(id, channel);
            api.getChannelMap().put(id, channel);
        }

        JSONArray permissionOverwrites = json.getJSONArray("permission_overwrites");
        for (int i = 0; i < permissionOverwrites.length(); i++)
        {
            createPermissionOverride(permissionOverwrites.getJSONObject(i), channel);
        }

        return channel
                .setName(json.getString("name"))
                .setTopic(json.isNull("topic") ? "" : json.getString("topic"))
                .setPosition(json.getInt("position"));
    }

    public VoiceChannel createVoiceChannel(JSONObject json, String guildId)
    {
        String id = json.getString("id");
        VoiceChannelImpl channel = ((VoiceChannelImpl) api.getVoiceChannelMap().get(id));
        if (channel == null)
        {
            GuildImpl guild = (GuildImpl) api.getGuildMap().get(guildId);
            channel = new VoiceChannelImpl(id, guild);
            guild.getVoiceChannelsMap().put(id, channel);
            api.getVoiceChannelMap().put(id, channel);
        }

        JSONArray permissionOverwrites = json.getJSONArray("permission_overwrites");
        for (int i = 0; i < permissionOverwrites.length(); i++)
        {
            createPermissionOverride(permissionOverwrites.getJSONObject(i), channel);
        }

        return channel
                .setName(json.getString("name"))
                .setPosition(json.getInt("position"));
    }

    public PrivateChannel createPrivateChannel(JSONObject privatechat)
    {
        UserImpl user = ((UserImpl) api.getUserMap().get(privatechat.getJSONObject("recipient").getString("id")));
        if (user == null)
        {   //The API can give us private channels connected to Users that we can no longer communicate with.
            api.getOffline_pms().put(privatechat.getJSONObject("recipient").getString("id"), privatechat.getString("id"));
            return null;
        }

        PrivateChannelImpl priv = new PrivateChannelImpl(privatechat.getString("id"), user, api);
        user.setPrivateChannel(priv);
        return priv;
    }

    public Role createRole(JSONObject roleJson, String guildId)
    {
        String id = roleJson.getString("id");
        GuildImpl guild = ((GuildImpl) api.getGuildMap().get(guildId));
        RoleImpl role = ((RoleImpl) guild.getRolesMap().get(id));
        if (role == null)
        {
            role = new RoleImpl(id, guild);
            guild.getRolesMap().put(id, role);
        }
        role.setName(roleJson.getString("name"))
            .setPosition(roleJson.getInt("position"))
            .setPermissions(roleJson.getInt("permissions"))
            .setManaged(roleJson.getBoolean("managed"))
            .setGrouped(roleJson.getBoolean("hoist"));
        try
        {
            role.setColor(roleJson.getInt("color"));
        }
        catch (JSONException ex)
        {
            role.setColor(0);
        }
        return role;
    }

    protected User createUser(JSONObject user)
    {
        String id = user.getString("id");
        UserImpl userObj = ((UserImpl) api.getUserMap().get(id));
        if (userObj == null)
        {
            userObj = new UserImpl(id, api);
            api.getUserMap().put(id, userObj);
        }
        return userObj
            .setUserName(user.getString("username"))
            .setDiscriminator(user.get("discriminator").toString())
            .setAvatarId(user.isNull("avatar") ? null : user.getString("avatar"));
    }

    protected SelfInfo createSelfInfo(JSONObject self)
    {
        SelfInfoImpl selfInfo = ((SelfInfoImpl) api.getSelfInfo());
        if (selfInfo == null)
        {
            selfInfo = new SelfInfoImpl(self.getString("id"), self.getString("email"), api);
            api.setSelfInfo(selfInfo);
        }
        if (!api.getUserMap().containsKey(selfInfo.getId()))
        {
            api.getUserMap().put(selfInfo.getId(), selfInfo);
        }
        return (SelfInfo) selfInfo
                .setVerified(self.getBoolean("verified"))
                .setUserName(self.getString("username"))
                .setDiscriminator(self.getString("discriminator"))
                .setAvatarId(self.isNull("avatar") ? null : self.getString("avatar"));
    }

    public Message createMessage(JSONObject jsonObject)
    {
        String id = jsonObject.getString("id");
        MessageImpl message = new MessageImpl(id, api)
                .setAuthor(api.getUserMap().get(jsonObject.getJSONObject("author").getString("id")))
                .setContent(jsonObject.getString("content"))
                .setTime(OffsetDateTime.parse(jsonObject.getString("timestamp")))
                .setMentionsEveryone(jsonObject.getBoolean("mention_everyone"))
                .setTTS(jsonObject.getBoolean("tts"));

        List<Message.Attachment> attachments = new LinkedList<>();
        JSONArray jsonAttachments = jsonObject.getJSONArray("attachments");
        for (int i = 0; i < jsonAttachments.length(); i++)
        {
            JSONObject jsonAttachment = jsonAttachments.getJSONObject(i);
            attachments.add(new Message.Attachment(
                    jsonAttachment.getString("id"),
                    jsonAttachment.getString("url"),
                    jsonAttachment.getString("proxy_url"),
                    jsonAttachment.getString("filename"),
                    jsonAttachment.getInt("size"),
                    jsonAttachment.has("height") ? jsonAttachment.getInt("height") : 0,
                    jsonAttachment.has("width") ? jsonAttachment.getInt("width") : 0
            ));
        }
        message.setAttachments(attachments);

        if (!jsonObject.isNull("edited_timestamp"))
            message.setEditedTime(OffsetDateTime.parse(jsonObject.getString("edited_timestamp")));

        String channelId = jsonObject.getString("channel_id");
        TextChannel textChannel = api.getChannelMap().get(channelId);
        if (textChannel != null)
        {
            message.setChannelId(textChannel.getId());
            message.setIsPrivate(false);
            List<User> mentioned = new LinkedList<>();
            JSONArray mentions = jsonObject.getJSONArray("mentions");
            for (int i = 0; i < mentions.length(); i++)
            {
                JSONObject mention = mentions.getJSONObject(i);
                mentioned.add(api.getUserMap().get(mention.getString("id")));
            }
            message.setMentionedUsers(mentioned);
        }
        else
        {
            message.setIsPrivate(true);
            PrivateChannel privateChannel = api.getPmChannelMap().get(channelId);
            if (privateChannel != null)
            {
                message.setChannelId(privateChannel.getId());
            }
            else
            {
                throw new IllegalArgumentException("Could not find Private Channel of id " + channelId);
            }
        }

        return message;
    }

    protected MessageEmbed createMessageEmbed(JSONObject messageEmbed)
    {
        MessageEmbedImpl embed = new MessageEmbedImpl()
            .setUrl(messageEmbed.getString("url"))
            .setTitle(messageEmbed.isNull("title") ? null : messageEmbed.getString("title"))
            .setDescription(messageEmbed.isNull("description") ? null : messageEmbed.getString("description"));

        EmbedType type = EmbedType.fromKey(messageEmbed.getString("type"));
//        if (type.equals(EmbedType.UNKNOWN))
//            throw new IllegalArgumentException("Discord provided us an unknown embed type.  Json: " + messageEmbed);
        embed.setType(type);

        if (messageEmbed.has("thumbnail"))
        {
            JSONObject thumbnailJson = messageEmbed.getJSONObject("thumbnail");
            embed.setThumbnail(new Thumbnail(
                    thumbnailJson.getString("url"),
                    thumbnailJson.getString("proxy_url"),
                    thumbnailJson.getInt("width"),
                    thumbnailJson.getInt("height")));
        }
        else embed.setThumbnail(null);

        if (messageEmbed.has("provider"))
        {
            JSONObject providerJson = messageEmbed.getJSONObject("provider");
            embed.setSiteProvider(new Provider(
                    providerJson.isNull("name") ? null : providerJson.getString("name"),
                    providerJson.isNull("url") ? null : providerJson.getString("url")));
        }
        else embed.setSiteProvider(null);

        if (messageEmbed.has("author"))
        {
            JSONObject authorJson = messageEmbed.getJSONObject("author");
            embed.setAuthor(new Provider(
                    authorJson.isNull("name") ? null : authorJson.getString("name"),
                    authorJson.isNull("url") ? null : authorJson.getString("url")));
        }
        else embed.setAuthor(null);

        if (messageEmbed.has("video"))
        {
            JSONObject videoJson = messageEmbed.getJSONObject("video");
            embed.setVideoInfo(new VideoInfo(
                    videoJson.getString("url"),
                    videoJson.getInt("width"),
                    videoJson.getInt("height")));
        }
        return embed;
    }

    public PermissionOverride createPermissionOverride(JSONObject override, Channel chan)
    {
        PermissionOverrideImpl permOverride = null;
        String id = override.getString("id");
        int allow = override.getInt("allow");
        int deny = override.getInt("deny");

        switch (override.getString("type"))
        {
            case "member":
                User user = api.getUserById(id);
                if (user == null)
                    throw new IllegalArgumentException("Attempted to create a PermissionOverride for a non-existent user. JSON: " + override);

                permOverride = (PermissionOverrideImpl) chan.getOverrideForUser(user);
                if (permOverride == null)
                {
                    permOverride = new PermissionOverrideImpl(chan, user, null);
                    if (chan instanceof TextChannel)
                        ((TextChannelImpl) chan).getUserPermissionOverridesMap().put(user, permOverride);
                    else
                        ((VoiceChannelImpl) chan).getUserPermissionOverridesMap().put(user, permOverride);
                }
                break;
            case "role":
                Role role = ((GuildImpl) chan.getGuild()).getRolesMap().get(id);
                if (role == null)
                    throw new IllegalArgumentException("Attempted to create a PermissionOverride for a non-existent role! JSON: " + override);

                permOverride = (PermissionOverrideImpl) chan.getOverrideForRole(role);
                if (permOverride == null)
                {
                    permOverride = new PermissionOverrideImpl(chan, null, role);
                    if (chan instanceof TextChannel)
                        ((TextChannelImpl) chan).getRolePermissionOverridesMap().put(role, permOverride);
                    else
                        ((VoiceChannelImpl) chan).getRolePermissionOverridesMap().put(role, permOverride);
                }
                break;
            default:
                throw new IllegalArgumentException("Provided with an unknown PermissionOverride type! JSON: " + override);
        }
        return permOverride.setAllow(allow)
                .setDeny(deny);
    }

    public VoiceStatus createVoiceStatus(JSONObject status, Guild guildObj, User user)
    {
        GuildImpl guild = (GuildImpl) guildObj;
        VoiceStatusImpl voiceStatus = (VoiceStatusImpl) guild.getVoiceStatusMap().get(user);
        if (voiceStatus == null)
        {
            voiceStatus = new VoiceStatusImpl(user, guild);
            guild.getVoiceStatusMap().put(user, voiceStatus);
        }

        if (!status.isNull("channel_id"))
        {
            VoiceChannel channel = guild.getVoiceChannelsMap().get(status.getString("channel_id"));
            if (channel == null)
                throw new IllegalArgumentException("Attempted to create a VoiceStatus using a non-existant channel! JSON: " + status);

            voiceStatus.setChannel(channel);
        }
        else
            voiceStatus.setChannel(null);

        if (!status.isNull("session_id"))
            voiceStatus.setSessionId(status.getString("session_id"));
        else
            voiceStatus.setSessionId(null);

        return voiceStatus
                .setMute(status.getBoolean("self_mute"))
                .setDeaf(status.getBoolean("self_deaf"))
                .setServerMute(status.getBoolean("mute"))
                .setServerDeaf(status.getBoolean("deaf"))
                .setSuppressed(status.getBoolean("suppress"));
    }
}
