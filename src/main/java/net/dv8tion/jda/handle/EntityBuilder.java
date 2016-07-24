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
package net.dv8tion.jda.handle;

import net.dv8tion.jda.EmbedType;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.OnlineStatus;
import net.dv8tion.jda.Region;
import net.dv8tion.jda.entities.*;
import net.dv8tion.jda.entities.MessageEmbed.Provider;
import net.dv8tion.jda.entities.MessageEmbed.Thumbnail;
import net.dv8tion.jda.entities.MessageEmbed.VideoInfo;
import net.dv8tion.jda.entities.impl.*;
import net.dv8tion.jda.requests.GuildLock;
import net.dv8tion.jda.requests.WebSocketClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EntityBuilder
{
    private static final HashMap<JDA, HashMap<String, JSONObject>> cachedJdaGuildJsons = new HashMap<>();
    private static final HashMap<JDA, HashMap<String, Consumer<Guild>>> cachedJdaGuildCallbacks = new HashMap<>();
    private static final Pattern channelMentionPattern = Pattern.compile("<#(\\d+)>");
    private final JDAImpl api;

    public EntityBuilder(JDAImpl api)
    {
        this.api = api;
        if(!cachedJdaGuildCallbacks.containsKey(api))
            cachedJdaGuildCallbacks.put(api, new HashMap<>());
        if(!cachedJdaGuildJsons.containsKey(api))
            cachedJdaGuildJsons.put(api, new HashMap<>());
    }

    public void clearCache()
    {
        cachedJdaGuildCallbacks.get(api).clear();
        cachedJdaGuildJsons.get(api).clear();
    }

    public Guild createGuildFirstPass(JSONObject guild, Consumer<Guild> secondPassCallback)
    {
        String id = guild.getString("id");
        GuildImpl guildObj = ((GuildImpl) api.getGuildMap().get(id));
        if (guildObj == null)
        {
            guildObj = new GuildImpl(api, id);
            api.getGuildMap().put(id, guildObj);
        }
        if (guild.has("unavailable") && guild.getBoolean("unavailable"))
        {
            guildObj.setAvailable(false);
            if (secondPassCallback != null)
            {
                secondPassCallback.accept(guildObj);
            }
            GuildLock.get(api).lock(id);
            return guildObj;
        }
        guildObj
            .setAvailable(true)
            .setIconId(guild.isNull("icon") ? null : guild.getString("icon"))
            .setRegion(Region.fromKey(guild.getString("region")))
            .setName(guild.getString("name"))
            .setAfkTimeout(guild.getInt("afk_timeout"))
            .setAfkChannelId(guild.isNull("afk_channel_id") ? null : guild.getString("afk_channel_id"))
            .setVerificationLevel(Guild.VerificationLevel.fromKey(guild.getInt("verification_level")));


        JSONArray roles = guild.getJSONArray("roles");
        for (int i = 0; i < roles.length(); i++)
        {
            Role role = createRole(roles.getJSONObject(i), guildObj.getId());
            guildObj.getRolesMap().put(role.getId(), role);
            if (role.getId().equals(guildObj.getId()))
                guildObj.setPublicRole(role);
        }

        if (guild.has("members"))
        {
            JSONArray members = guild.getJSONArray("members");
            createGuildMemberPass(guildObj, members);
        }

        User owner = api.getUserById(guild.getString("owner_id"));
        if (owner != null)
            guildObj.setOwner(owner);

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
                Game presenceGame = null;
                if( !presence.isNull("game") && !presence.getJSONObject("game").isNull("name") )
                {
                    presenceGame = new GameImpl(presence.getJSONObject("game").get("name").toString(),
                            presence.getJSONObject("game").isNull("url") ? null : presence.getJSONObject("game").get("url").toString(),
                            presence.getJSONObject("game").isNull("type") ? Game.GameType.DEFAULT : Game.GameType.fromKey((int) presence.getJSONObject("game").get("type")) );
                }
                user
                    .setCurrentGame(presenceGame)
                    .setOnlineStatus(OnlineStatus.fromKey(presence.getString("status")));
            }
        }

        if (guild.has("channels"))
        {
            JSONArray channels = guild.getJSONArray("channels");

            for (int i = 0; i < channels.length(); i++)
            {
                JSONObject channel = channels.getJSONObject(i);
                ChannelType type = ChannelType.fromId(channel.getInt("type"));
                if (type == ChannelType.TEXT)
                {
                    TextChannel newChannel = createTextChannel(channel, guildObj.getId());
                    if (newChannel.getId().equals(guildObj.getId()))
                        guildObj.setPublicChannel(newChannel);
                }
                else if (type == ChannelType.VOICE)
                {
                    createVoiceChannel(channel, guildObj.getId());
                }
                else
                    JDAImpl.LOG.fatal("Received a channel for a guild that isn't a text or voice channel. JSON: " + channel);
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
                {
                    //voice-status of offline user -> ignore
                    continue;
                }

                try
                {
                    VoiceStatus voiceStatus = createVoiceStatus(voiceState, guildObj, user);
                    ((VoiceChannelImpl) voiceStatus.getChannel()).getUsersModifiable().add(user);
                }
                catch (IllegalArgumentException ignored)
                {
                    //Ignore this: weird behaviour of Discord itself gives us presences to vc that were deleted
                }
            }
        }


        //We allow Guild creation without second pass for when JDA itself creates a NEW Guild. We won't need
        // to worry about there being a lack of offline Users because there wont be -any users or, at the very
        // most, the only User will be the JDA user that just created the new Guild.
        //This fall through is used by JDAImpl.createGuild(String, Region).
        if (secondPassCallback != null && guild.has("large") && guild.getBoolean("large"))
        {
            HashMap<String, JSONObject> cachedGuildJsons = cachedJdaGuildJsons.get(api);
            HashMap<String, Consumer<Guild>> cachedGuildCallbacks = cachedJdaGuildCallbacks.get(api);
            cachedGuildJsons.put(id, guild);
            cachedGuildCallbacks.put(id, secondPassCallback);
            GuildMembersChunkHandler.setExpectedGuildMembers(api, id, guild.getInt("member_count"));
            if (api.getClient().isReady())
            {
                JSONObject obj = new JSONObject()
                        .put("op", 8)
                        .put("d", new JSONObject()
                                .put("guild_id", id)
                                .put("query","")
                                .put("limit", 0)
                        );
                api.getClient().send(obj.toString());
            }
            else
            {
                new ReadyHandler(api, 0).onGuildNeedsMembers(guildObj);
            }
            GuildLock.get(api).lock(id);
            return null;//Nothing should be using the return of this method besides JDAImpl.createGuild(String, Region)
        }

        JSONArray channels = guild.getJSONArray("channels");
        createGuildChannelPass(guildObj, channels);

        if (secondPassCallback != null)
        {
            secondPassCallback.accept(guildObj);
            GuildLock.get(api).unlock(guildObj.getId());
            return null;//Nothing should be using the return of this method besides JDAImpl.createGuild(String, Region)
        }

        GuildLock.get(api).unlock(guildObj.getId());
        return guildObj;
    }

    public void createGuildSecondPass(String guildId, List<JSONArray> memberChunks)
    {
        HashMap<String, JSONObject> cachedGuildJsons = cachedJdaGuildJsons.get(api);
        HashMap<String, Consumer<Guild>> cachedGuildCallbacks = cachedJdaGuildCallbacks.get(api);

        JSONObject guildJson = cachedGuildJsons.remove(guildId);
        Consumer<Guild> secondPassCallback = cachedGuildCallbacks.remove(guildId);
        GuildImpl guildObj = (GuildImpl) api.getGuildMap().get(guildId);

        if (guildObj == null)
            throw new IllegalStateException("Attempted to preform a second pass on an unknown Guild. Guild not in JDA " +
                    "mapping. GuildId: " + guildId);
        if (guildJson == null)
            throw new IllegalStateException("Attempted to preform a second pass on an unknown Guild. No cached Guild " +
                    "for second pass. GuildId: " + guildId);
        if (secondPassCallback == null)
            throw new IllegalArgumentException("No callback provided for the second pass on the Guild!");

        for (JSONArray chunk : memberChunks)
        {
            createGuildMemberPass(guildObj, chunk);
        }

        User owner = api.getUserById(guildJson.getString("owner_id"));
        if (owner != null)
            guildObj.setOwner(owner);

        if (guildObj.getOwner() == null)
            JDAImpl.LOG.fatal("Never set the Owner of the Guild: " + guildObj.getId() + " because we don't have the owner User object! How?!");

        JSONArray channels = guildJson.getJSONArray("channels");
        createGuildChannelPass(guildObj, channels);

        secondPassCallback.accept(guildObj);
        GuildLock.get(api).unlock(guildId);
    }

    private void createGuildMemberPass(GuildImpl guildObj, JSONArray members)
    {
        Map<String, Role> rolesMap = guildObj.getRolesMap();
        Map<User, List<Role>> userRoles = guildObj.getUserRoles();
        Map<User, VoiceStatus> voiceStatusMap = guildObj.getVoiceStatusMap();
        Map<User, OffsetDateTime> joinedAtMap = guildObj.getJoinedAtMap();
        Map<User, String> nickMap = guildObj.getNickMap();
        for (int i = 0; i < members.length(); i++)
        {
            JSONObject member = members.getJSONObject(i);
            User user = createUser(member.getJSONObject("user"));
            userRoles.put(user, new ArrayList<>());
            JSONArray roleArr = member.getJSONArray("roles");
            for (int j = 0; j < roleArr.length(); j++)
            {
                String roleId = roleArr.getString(j);
                Role role = rolesMap.get(roleId);
                if (role != null)
                {
                    userRoles.get(user).add(role);
                }
                else
                {
                    WebSocketClient.LOG.warn("While building the guild users, encountered a user that is assigned a " +
                            "non-existent role. This is a Discord error, not a JDA error. Ignoring the role. " +
                            "GuildId: " + guildObj.getId() + " UserId: " + user.getId() + " RoleId: " + roleId);
                }
            }
            VoiceStatusImpl voiceStatus = new VoiceStatusImpl(user, guildObj);
            voiceStatus.setServerDeaf(member.getBoolean("deaf"));
            voiceStatus.setServerMute(member.getBoolean("mute"));
            voiceStatusMap.put(user, voiceStatus);
            joinedAtMap.put(user, OffsetDateTime.parse(member.getString("joined_at")));
            if(member.has("nick") && !member.isNull("nick"))
                nickMap.put(user, member.getString("nick"));
        }
    }

    private void createGuildChannelPass(GuildImpl guildObj, JSONArray channels)
    {
        for (int i = 0; i < channels.length(); i++)
        {
            JSONObject channel = channels.getJSONObject(i);
            ChannelType type = ChannelType.fromId(channel.getInt("type"));
            Channel channelObj = null;
            if (type == ChannelType.TEXT)
            {
                channelObj = api.getTextChannelById(channel.getString("id"));
            }
            else if (type == ChannelType.VOICE)
            {
                channelObj = api.getVoiceChannelById(channel.getString("id"));
            }
            else
                JDAImpl.LOG.fatal("Received a channel for a guild that isn't a text or voice channel (ChannelPass). JSON: " + channel);

            if (channelObj != null)
            {
                JSONArray permissionOverwrites = channel.getJSONArray("permission_overwrites");
                for (int j = 0; j < permissionOverwrites.length(); j++)
                {
                    try
                    {
                        createPermissionOverride(permissionOverwrites.getJSONObject(j), channelObj);
                    }
                    catch (IllegalArgumentException e)
                    {
                        WebSocketClient.LOG.warn(e.getMessage() + ". Ignoring PermissionOverride.");
                    }
                }
            }
            else
            {
                throw new RuntimeException("Got permission_override for unknown channel with id: " + channel.getString("id"));
            }
        }
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

        return channel
                .setName(json.getString("name"))
                .setPosition(json.getInt("position"))
                .setUserLimit(json.getInt("user_limit"))
                .setBitrate(json.getInt("bitrate"));
    }

    public PrivateChannel createPrivateChannel(JSONObject privatechat)
    {
        JSONObject recipient = privatechat.getJSONArray("recipients").getJSONObject(0);
        UserImpl user = ((UserImpl) api.getUserMap().get(recipient.getString("id")));
        if (user == null)
        {   //The API can give us private channels connected to Users that we can no longer communicate with.
            api.getOffline_pms().put(recipient.getString("id"), privatechat.getString("id"));
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
                .setGrouped(roleJson.getBoolean("hoist"))
                .setMentionable(roleJson.has("mentionable") && roleJson.getBoolean("mentionable"));
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
                .setAvatarId(user.isNull("avatar") ? null : user.getString("avatar"))
                .setIsBot(user.has("bot") && user.getBoolean("bot"));
    }

    protected SelfInfo createSelfInfo(JSONObject self)
    {
        SelfInfoImpl selfInfo = ((SelfInfoImpl) api.getSelfInfo());
        if (selfInfo == null)
        {
            selfInfo = new SelfInfoImpl(self.getString("id"), api);
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
                .setAvatarId(self.isNull("avatar") ? null : self.getString("avatar"))
                .setIsBot(self.has("bot") && self.getBoolean("bot"));
    }

    public Message createMessage(JSONObject jsonObject)
    {
        String id = jsonObject.getString("id");
        String content = jsonObject.getString("content");
        MessageImpl message = new MessageImpl(id, api)
                .setAuthor(api.getUserMap().get(jsonObject.getJSONObject("author").getString("id")))
                .setContent(content)
                .setTime(OffsetDateTime.parse(jsonObject.getString("timestamp")))
                .setMentionsEveryone(jsonObject.getBoolean("mention_everyone"))
                .setTTS(jsonObject.getBoolean("tts"))
                .setPinned(jsonObject.getBoolean("pinned"));

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
                    jsonAttachment.has("width") ? jsonAttachment.getInt("width") : 0,
                    api
            ));
        }
        message.setAttachments(attachments);

        List<MessageEmbed> embeds = new LinkedList<>();
        JSONArray jsonEmbeds = jsonObject.getJSONArray("embeds");
        for (int i = 0; i < jsonEmbeds.length(); i++)
        {
            embeds.add(createMessageEmbed(jsonEmbeds.getJSONObject(i)));
        }
        message.setEmbeds(embeds);

        if (!jsonObject.isNull("edited_timestamp"))
            message.setEditedTime(OffsetDateTime.parse(jsonObject.getString("edited_timestamp")));

        String channelId = jsonObject.getString("channel_id");
        TextChannel textChannel = api.getChannelMap().get(channelId);
        if (textChannel != null)
        {
            message.setChannelId(textChannel.getId());
            message.setIsPrivate(false);
            TreeMap<Integer, User> mentionedUsers = new TreeMap<>();
            JSONArray mentions = jsonObject.getJSONArray("mentions");
            for (int i = 0; i < mentions.length(); i++)
            {
                JSONObject mention = mentions.getJSONObject(i);
                User u = api.getUserMap().get(mention.getString("id"));
                if (u != null)
                {
                    //We do this to properly order the mentions. The array given by discord is out of order sometimes.
                    int index = content.indexOf("<@" + mention.getString("id") + ">");
                    mentionedUsers.put(index, u);
                }
            }
            message.setMentionedUsers(new LinkedList<User>(mentionedUsers.values()));

            TreeMap<Integer, Role> mentionedRoles = new TreeMap<>();
            JSONArray roleMentions = jsonObject.getJSONArray("mention_roles");
            for (int i = 0; i < roleMentions.length(); i++)
            {
                String roleId = roleMentions.getString(i);
                Role r = textChannel.getGuild().getRoleById(roleId);
                if (r != null)
                {
                    int index = content.indexOf("<@&" + roleId + ">");
                    mentionedRoles.put(index, r);
                }
            }
            message.setMentionedRoles(new LinkedList<Role>(mentionedRoles.values()));

            List<TextChannel> mentionedChannels = new LinkedList<>();
            Map<String, TextChannel> chanMap = ((GuildImpl) textChannel.getGuild()).getTextChannelsMap();
            Matcher matcher = channelMentionPattern.matcher(content);
            while (matcher.find())
            {
                TextChannel channel = chanMap.get(matcher.group(1));
                if(channel != null && !mentionedChannels.contains(channel))
                {
                    mentionedChannels.add(channel);
                }
            }
            message.setMentionedChannels(mentionedChannels);
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
                throw new IllegalArgumentException("Could not find Private/Text Channel of id " + channelId);
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
                    videoJson.isNull("width") ? -1 : videoJson.getInt("width"),
                    videoJson.isNull("height") ? -1 : videoJson.getInt("height")));
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
                    throw new IllegalArgumentException("Attempted to create a PermissionOverride for a non-existent user. Guild: " + chan.getGuild() + ", Channel: " + chan + ", JSON: " + override);

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
