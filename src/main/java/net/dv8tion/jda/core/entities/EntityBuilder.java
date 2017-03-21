/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
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

package net.dv8tion.jda.core.entities;

import net.dv8tion.jda.bot.entities.ApplicationInfo;
import net.dv8tion.jda.bot.entities.impl.ApplicationInfoImpl;
import net.dv8tion.jda.client.entities.*;
import net.dv8tion.jda.client.entities.impl.*;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.Region;
import net.dv8tion.jda.core.entities.MessageEmbed.*;
import net.dv8tion.jda.core.entities.impl.*;
import net.dv8tion.jda.core.exceptions.AccountTypeException;
import net.dv8tion.jda.core.handle.GuildMembersChunkHandler;
import net.dv8tion.jda.core.handle.ReadyHandler;
import net.dv8tion.jda.core.requests.GuildLock;
import net.dv8tion.jda.core.requests.WebSocketClient;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.Color;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EntityBuilder
{
    public static final String MISSING_CHANNEL = "MISSING_CHANNEL";
    public static final String MISSING_USER = "MISSING_USER";

    private static final HashMap<JDA, EntityBuilder> builders = new HashMap<>();
    private static final Pattern channelMentionPattern = Pattern.compile("<#(\\d+)>");

    protected final JDAImpl api;
    protected final HashMap<String, JSONObject> cachedGuildJsons = new HashMap<>();
    protected final HashMap<String, Consumer<Guild>> cachedGuildCallbacks = new HashMap<>();

    public static EntityBuilder get(JDA api)
    {
        EntityBuilder builder = builders.get(api);
        if (builder == null)
        {
            builder = new EntityBuilder(api);
            builders.put(api, builder);
        }
        return builder;
    }

    private EntityBuilder(JDA api)
    {
        this.api = (JDAImpl) api;
    }

    public SelfUser createSelfUser(JSONObject self)
    {
        SelfUserImpl selfUser = ((SelfUserImpl) api.getSelfUser());
        if (selfUser == null)
        {
            selfUser = new SelfUserImpl(self.getString("id"), api);
            api.setSelfUser(selfUser);
        }
        if (!api.getUserMap().containsKey(selfUser.getId()))
        {
            api.getUserMap().put(selfUser.getId(), selfUser);
        }
        return (SelfUser) selfUser
                .setVerified(self.getBoolean("verified"))
                .setMfaEnabled(self.getBoolean("mfa_enabled"))
                .setEmail(!self.isNull("email") ? self.getString("email") : null)
                .setName(self.getString("username"))
                .setDiscriminator(self.getString("discriminator"))
                .setAvatarId(self.isNull("avatar") ? null : self.getString("avatar"))
                .setBot(self.has("bot") && self.getBoolean("bot"));
    }

    public void createGuildFirstPass(JSONObject guild, Consumer<Guild> secondPassCallback)
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
            //This is used for when GuildCreateHandler receives a guild that is currently unavailable. During normal READY
            // loading for bots (which unavailable is always true) the secondPassCallback parameter will always
            // be null.
            if (secondPassCallback != null)
                secondPassCallback.accept(guildObj);
            GuildLock.get(api).lock(id);
            return;
        }

        //If we make it to here, the Guild is available. This means 1 of 2 things:
        //Either:
        // 1) This is Guild provided during READY for a Client account
        // 2) This is a Guild received from GuildCreateHandler from a GUILD_CREATE event.
        //      This could be triggered by joining a guild or due to discord finally
        //      providing us with Guild information about a previously unavailable guild.
        //      Whether it was unavailable due to Bot READY unavailability or due to an
        //      outage within discord matters now.
        //
        // Either way, we now have enough information to fill in the general information about the Guild.
        // This does NOT necessarily mean that we have all information to complete the guild.
        // For Client accounts, we will also need to use op 12 (GUILD_SYNC) to get all presences of online users because
        // discord only provides Online users that we have an open PM channel with or are friends with for Client accounts.
        // On larger guilds we will still need to request all users using op 8 (GUILD_MEMBERS_CHUNK).
        //
        // The code below takes the information we -do- have and starts to fill in the Guild. It won't create anything
        // that might rely on Users that we don't have due to needing the GUILD_MEMBERS_CHUNK
        // This includes making VoiceStatus and PermissionOverrides

        guildObj.setAvailable(true)
                .setIconId(guild.isNull("icon") ? null : guild.getString("icon"))
                .setSplashId(guild.isNull("splash") ? null : guild.getString("splash"))
                .setRegion(Region.fromKey(guild.getString("region")))
                .setName(guild.getString("name"))
                .setAfkTimeout(Guild.Timeout.fromKey(guild.getInt("afk_timeout")))
                .setVerificationLevel(Guild.VerificationLevel.fromKey(guild.getInt("verification_level")))
                .setDefaultNotificationLevel(Guild.NotificationLevel.fromKey(guild.getInt("default_message_notifications")))
                .setRequiredMFALevel(Guild.MFALevel.fromKey(guild.getInt("mfa_level")));

        JSONArray roles = guild.getJSONArray("roles");
        for (int i = 0; i < roles.length(); i++)
        {
            Role role = createRole(roles.getJSONObject(i), guildObj.getId());
            guildObj.getRolesMap().put(role.getId(), role);
            if (role.getId().equals(guildObj.getId()))
                guildObj.setPublicRole(role);
        }

        if (!guild.isNull("emojis"))
        {
            JSONArray array = guild.getJSONArray("emojis");
            Map<String, Emote> emoteMap = guildObj.getEmoteMap();
            for (int i = 0; i < array.length(); i++)
            {
                JSONObject object = array.getJSONObject(i);
                JSONArray emoteRoles = object.getJSONArray("roles");
                String emoteId = object.getString("id");

                EmoteImpl emoteObj = new EmoteImpl(emoteId, guildObj);
                Set<Role> roleSet = emoteObj.getRoleSet();

                for (int j = 0; j < emoteRoles.length(); j++)
                    roleSet.add(guildObj.getRoleById(emoteRoles.getString(j)));
                emoteMap.put(emoteId, emoteObj
                        .setName(object.getString("name"))
                        .setManaged(object.getBoolean("managed")));
            }
        }

        if (guild.has("members"))
        {
            JSONArray members = guild.getJSONArray("members");
            createGuildMemberPass(guildObj, members);
        }

        //This could be null for Client accounts. Will be fixed by GUILD_SYNC
        Member owner = guildObj.getMemberById(guild.getString("owner_id"));
        if (owner != null)
            guildObj.setOwner(owner);

        if (guild.has("presences"))
        {
            JSONArray presences = guild.getJSONArray("presences");
            for (int i = 0; i < presences.length(); i++)
            {
                JSONObject presence = presences.getJSONObject(i);
                String userId = presence.getJSONObject("user").getString("id");
                MemberImpl member = (MemberImpl) guildObj.getMembersMap().get(userId);

                if (member == null)
                    WebSocketClient.LOG.debug("Received a ghost presence in GuildFirstPass! Guild: " + guildObj + " UserId: " + userId);
                else
                    createPresence(member, presence);
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
                    TextChannel newChannel = createTextChannel(channel, guildObj.getId(), false);
                    if (newChannel.getId().equals(guildObj.getId()))
                        guildObj.setPublicChannel(newChannel);
                }
                else if (type == ChannelType.VOICE)
                {
                    VoiceChannel newChannel = createVoiceChannel(channel, guildObj.getId(), false);
                    if (!guild.isNull("afk_channel_id")
                            && newChannel.getId().equals(guild.getString("afk_channel_id")))
                        guildObj.setAfkChannel(newChannel);
                }
                else
                    WebSocketClient.LOG.fatal("Received a channel for a guild that isn't a text or voice channel. JSON: " + channel);
            }
        }

        //If the members that we were provided with (and loaded above) were not all of the
        //  the members in this guild, then we need to request more users from Discord using
        //  op 9 (GUILD_MEMBERS_CHUNK). To do so, we will cache the guild's JSON so we can properly
        //  load stuff that relies on Users like Channels, PermissionOverrides and VoiceStatuses
        //  after we have the rest of the users. We will request the GUILD_MEMBERS_CHUNK information
        //  which will be sent from discord over the main Websocket and will be handled by
        //  GuildMemberChunkHandler. After the handler has received all users as determined by the
        //  value set using `setExpectedGuildMembers`, it will do one of the following:
        //    1) If this is a Bot account, immediately call EntityBuilder#createGuildSecondPass, thus finishing
        //        the Guild object creation process.
        //    2) If this is a Client account, it will request op 12 (GUILD_SYNC) to make sure we have all information
        //        about online users as GUILD_MEMBERS_CHUNK does not include presence information, and when loading the
        //        members from GUILD_MEMBERS_CHUNK, we assume they are offline. GUILD_SYNC makes sure that we mark them
        //        properly. After GUILD_SYNC is received by GuildSyncHandler, it will call EntityBuilder#createGuildSecondPass
        //
        //If we actually -did- get all of the users needed, then we don't need to Chunk. Furthermore,
        // we don't need to use GUILD_SYNC because we always get presences with users thus we have all information
        // needed to guild the Guild. We will skip
        if (guild.getJSONArray("members").length() != guild.getInt("member_count"))
        {
            cachedGuildJsons.put(id, guild);
            cachedGuildCallbacks.put(id, secondPassCallback);

            GuildMembersChunkHandler handler = api.getClient().getHandler("GUILD_MEMBERS_CHUNK");
            handler.setExpectedGuildMembers(id, guild.getInt("member_count"));

            //If we are already past READY / RESUME, then chunk at runtime. Otherwise, pass back to the ReadyHandler
            // and let it send a burst chunk request.
            if (api.getClient().isReady())
            {
                if (api.getAccountType() == AccountType.CLIENT)
                {
                    JSONObject obj = new JSONObject()
                            .put("op", 12)
                            .put("guild_id", guildObj.getId());
                    api.getClient().send(obj.toString());
                }
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
                ReadyHandler readyHandler = api.getClient().getHandler("READY");
                readyHandler.acknowledgeGuild(guildObj, true, true, api.getAccountType() == AccountType.CLIENT);
            }

            GuildLock.get(api).lock(id);
            return;
        }

        //As detailed in the comment above, if we've made it this far then we have all member information needed to
        // create the Guild. Thus, we fill in the remaining information, unlock the guild, and provide the guild
        // to the callback
        //This should only occur on small user count guilds.

        JSONArray channels = guild.getJSONArray("channels");
        createGuildChannelPass(guildObj, channels); //Actually creates PermissionOverrides

        JSONArray voiceStates = guild.getJSONArray("voice_states");
        createGuildVoiceStatePass(guildObj, voiceStates);

        GuildLock.get(api).unlock(guildObj.getId());
        if (secondPassCallback != null)
            secondPassCallback.accept(guildObj);
    }

    public void createGuildSecondPass(String guildId, List<JSONArray> memberChunks)
    {
        JSONObject guildJson = cachedGuildJsons.remove(guildId);
        Consumer<Guild> secondPassCallback = cachedGuildCallbacks.remove(guildId);
        GuildImpl guildObj = (GuildImpl) api.getGuildMap().get(guildId);

        if (guildObj == null)
            throw new IllegalStateException("Attempted to perform a second pass on an unknown Guild. Guild not in JDA " +
                    "mapping. GuildId: " + guildId);
        if (guildJson == null)
            throw new IllegalStateException("Attempted to perform a second pass on an unknown Guild. No cached Guild " +
                    "for second pass. GuildId: " + guildId);
        if (secondPassCallback == null)
            throw new IllegalArgumentException("No callback provided for the second pass on the Guild!");

        for (JSONArray chunk : memberChunks)
        {
            createGuildMemberPass(guildObj, chunk);
        }

        Member owner = guildObj.getMemberById(guildJson.getString("owner_id"));
        if (owner != null)
            guildObj.setOwner(owner);

        if (guildObj.getOwner() == null)
            WebSocketClient.LOG.fatal("Never set the Owner of the Guild: " + guildObj.getId() + " because we don't have the owner User object! How?!");

        JSONArray channels = guildJson.getJSONArray("channels");
        createGuildChannelPass(guildObj, channels);

        JSONArray voiceStates = guildJson.getJSONArray("voice_states");
        createGuildVoiceStatePass(guildObj, voiceStates);

        secondPassCallback.accept(guildObj);
        GuildLock.get(api).unlock(guildId);
    }

    public void handleGuildSync(GuildImpl guild, JSONArray members, JSONArray presences)
    {
        for (int i = 0; i < members.length(); i++)
        {
            JSONObject memberJson = members.getJSONObject(i);
            Member member = createMember(guild, memberJson);
        }

        for (int i = 0; i < presences.length(); i++)
        {
            JSONObject presenceJson = presences.getJSONObject(i);
            String userId = presenceJson.getJSONObject("user").getString("id");

            MemberImpl member = (MemberImpl) guild.getMembersMap().get(userId);
            if (member == null)
                WebSocketClient.LOG.fatal("Received a Presence for a non-existent Member when dealing with GuildSync!");
            else
                this.createPresence(member, presenceJson);
        }
    }

    private void createGuildMemberPass(GuildImpl guildObj, JSONArray members)
    {
        for (int i = 0; i < members.length(); i++)
        {
            JSONObject memberJson = members.getJSONObject(i);
            Member member = createMember(guildObj, memberJson);
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
                WebSocketClient.LOG.fatal("Received a channel for a guild that isn't a text or voice channel (ChannelPass). JSON: " + channel);

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
                        //Caused by Discord not properly clearing PermissionOverrides when a Member leaves a Guild.
                        WebSocketClient.LOG.debug(e.getMessage() + ". Ignoring PermissionOverride.");
                    }
                }
            }
            else
            {
                throw new RuntimeException("Got permission_override for unknown channel with id: " + channel.getString("id"));
            }
        }
    }

    public void createGuildVoiceStatePass(GuildImpl guildObj, JSONArray voiceStates)
    {
        for (int i = 0; i < voiceStates.length(); i++)
        {
            JSONObject voiceStateJson = voiceStates.getJSONObject(i);
            Member member = guildObj.getMembersMap().get(voiceStateJson.getString("user_id"));
            if (member == null)
            {
                WebSocketClient.LOG.fatal("Received a VoiceState for a unknown Member! GuildId: "
                        + guildObj.getId() + " MemberId: " + voiceStateJson.getString("user_id"));
                continue;
            }

            VoiceChannelImpl voiceChannel =
                    (VoiceChannelImpl) guildObj.getVoiceChannelMap().get(voiceStateJson.getString("channel_id"));
            voiceChannel.getConnectedMembersMap().put(member.getUser().getId(), member);

            GuildVoiceStateImpl voiceState = (GuildVoiceStateImpl) member.getVoiceState();
            voiceState.setSelfMuted(voiceStateJson.getBoolean("self_mute"))
                    .setSelfDeafened(voiceStateJson.getBoolean("self_deaf"))
                    .setGuildMuted(voiceStateJson.getBoolean("mute"))
                    .setGuildDeafened(voiceStateJson.getBoolean("deaf"))
                    .setSuppressed(voiceStateJson.getBoolean("suppress"))
                    .setSessionId(voiceStateJson.getString("session_id"))
                    .setConnectedChannel(voiceChannel);
        }
    }

    public User createFakeUser(JSONObject user, boolean modifyCache) { return createUser(user, true, modifyCache); }
    public User createUser(JSONObject user)     { return createUser(user, false, true); }
    private User createUser(JSONObject user, boolean fake, boolean modifyCache)
    {
        String id = user.getString("id");
        UserImpl userObj;

        userObj = (UserImpl) api.getUserMap().get(id);
        if (userObj == null)
        {
            userObj = (UserImpl) api.getFakeUserMap().get(id);
            if (userObj != null)
            {
                if (!fake && modifyCache)
                {
                    api.getFakeUserMap().remove(id);
                    userObj.setFake(false);
                    api.getUserMap().put(userObj.getId(), userObj);
                    if (userObj.hasPrivateChannel())
                    {
                        PrivateChannelImpl priv = (PrivateChannelImpl) userObj.getPrivateChannel();
                        priv.setFake(false);
                        api.getFakePrivateChannelMap().remove(priv.getId());
                        api.getPrivateChannelMap().put(priv.getId(), priv);
                    }
                }
            }
            else
            {
                userObj = new UserImpl(id, api).setFake(fake);
                if (modifyCache)
                {
                    if (fake)
                        api.getFakeUserMap().put(id, userObj);
                    else
                        api.getUserMap().put(id, userObj);
                }
            }
        }

        return userObj
                .setName(user.getString("username"))
                .setDiscriminator(user.get("discriminator").toString())
                .setAvatarId(user.isNull("avatar") ? null : user.getString("avatar"))
                .setBot(user.has("bot") && user.getBoolean("bot"));
    }

    public Member createMember(GuildImpl guild, JSONObject memberJson)
    {
        User user = createUser(memberJson.getJSONObject("user"));
        MemberImpl member = (MemberImpl) guild.getMember(user);
        if (member == null)
        {
            member = new MemberImpl(guild, user);
            guild.getMembersMap().put(user.getId(), member);
        }

        ((GuildVoiceStateImpl) member.getVoiceState())
            .setGuildMuted(memberJson.getBoolean("mute"))
            .setGuildDeafened(memberJson.getBoolean("deaf"));

        member.setJoinDate(OffsetDateTime.parse(memberJson.getString("joined_at")))
            .setNickname(memberJson.has("nick") && !memberJson.isNull("nick")
                ? memberJson.getString("nick")
                : null);

        JSONArray rolesJson = memberJson.getJSONArray("roles");
        for (int k = 0; k < rolesJson.length(); k++)
        {
            String roleId = rolesJson.getString(k);
            Role r = guild.getRolesMap().get(roleId);
            if (r == null)
            {
                WebSocketClient.LOG.debug("Received a Member with an unknown Role. MemberId: "
                        + member.getUser().getId() + " GuildId: " + guild.getId() + " roleId: " + roleId);
            }
            else
            {
                member.getRoleSet().add(r);
            }
        }

        return member;
    }

    //Effectively the same as createFriendPresence
    public void createPresence(Object memberOrFriend, JSONObject presenceJson)
    {
        if (memberOrFriend == null)
            throw new NullPointerException("Provided memberOrFriend was null!");

        JSONObject gameJson = presenceJson.isNull("game") ? null: presenceJson.getJSONObject("game");
        OnlineStatus onlineStatus = OnlineStatus.fromKey(presenceJson.getString("status"));
        Game game = null;

        if (gameJson != null && !gameJson.isNull("name"))
        {
            String gameName = gameJson.get("name").toString();
            String url = gameJson.isNull("url")
                    ? null
                    : gameJson.get("url").toString();

            Game.GameType gameType;
            try
            {
                gameType = gameJson.isNull("type")
                        ? Game.GameType.DEFAULT
                        : Game.GameType.fromKey(Integer.parseInt(gameJson.get("type").toString()));
            }
            catch (NumberFormatException e)
            {
                gameType = Game.GameType.DEFAULT;
            }

            game = new GameImpl(gameName, url, gameType);
        }
        if (memberOrFriend instanceof Member)
        {
            MemberImpl member = (MemberImpl) memberOrFriend;
            member.setOnlineStatus(onlineStatus);
            member.setGame(game);
        }
        else if (memberOrFriend instanceof Friend)
        {
            FriendImpl friend = (FriendImpl) memberOrFriend;
            friend.setOnlineStatus(onlineStatus);
            friend.setGame(game);

            OffsetDateTime lastModified = OffsetDateTime.ofInstant(
                    Instant.ofEpochMilli(presenceJson.getLong("last_modified")),
                    TimeZone.getTimeZone("GMT").toZoneId());

            friend.setOnlineStatusModifiedTime(lastModified);
        }
        else
            throw new IllegalArgumentException("An object was provided to EntityBuilder#createPresence that wasn't a Member or Friend. JSON: " + presenceJson);
    }

    public TextChannel createTextChannel(JSONObject json, String guildId)
    {
        return createTextChannel(json, guildId, true);

    }
    public TextChannel createTextChannel(JSONObject json, String guildId, boolean guildIsLoaded)
    {
        String id = json.getString("id");
        TextChannelImpl channel = (TextChannelImpl) api.getTextChannelMap().get(id);
        if (channel == null)
        {
            GuildImpl guild = ((GuildImpl) api.getGuildMap().get(guildId));
            channel = new TextChannelImpl(id, guild);
            guild.getTextChannelsMap().put(id, channel);
            api.getTextChannelMap().put(id, channel);
        }

        if (!json.isNull("permission_overwrites") && guildIsLoaded)
        {
            JSONArray overrides = json.getJSONArray("permission_overwrites");
            for (int i = 0; i < overrides.length(); i++)
            {
                createPermissionOverride(overrides.getJSONObject(i), channel);
            }
        }

        return channel
                .setLastMessageId(json.isNull("last_message_id") ? null : json.getString("last_message_id"))
                .setName(json.getString("name"))
                .setTopic(json.isNull("topic") ? "" : json.getString("topic"))
                .setRawPosition(json.getInt("position"));
    }

    public VoiceChannel createVoiceChannel(JSONObject json, String guildId)
    {
        return createVoiceChannel(json, guildId, true);
    }
    public VoiceChannel createVoiceChannel(JSONObject json, String guildId, boolean guildIsLoaded)
    {
        String id = json.getString("id");
        VoiceChannelImpl channel = ((VoiceChannelImpl) api.getVoiceChannelMap().get(id));
        if (channel == null)
        {
            GuildImpl guild = (GuildImpl) api.getGuildMap().get(guildId);
            channel = new VoiceChannelImpl(id, guild);
            guild.getVoiceChannelMap().put(id, channel);
            api.getVoiceChannelMap().put(id, channel);
        }

        if (!json.isNull("permission_overwrites") && guildIsLoaded)
        {
            JSONArray overrides = json.getJSONArray("permission_overwrites");
            for (int i = 0; i < overrides.length(); i++)
            {
                createPermissionOverride(overrides.getJSONObject(i), channel);
            }
        }

        return channel
                .setName(json.getString("name"))
                .setRawPosition(json.getInt("position"))
                .setUserLimit(json.getInt("user_limit"))
                .setBitrate(json.getInt("bitrate"));
    }

    public PrivateChannel createPrivateChannel(JSONObject privatechat)
    {
        JSONObject recipient = privatechat.has("recipients") ? 
            privatechat.getJSONArray("recipients").getJSONObject(0) :
            privatechat.getJSONObject("recipient");
        UserImpl user = ((UserImpl) api.getUserMap().get(recipient.getString("id")));
        if (user == null)
        {   //The API can give us private channels connected to Users that we can no longer communicate with.
            // As such, make a fake user and fake private channel.
            user = (UserImpl) createFakeUser(recipient, true);
        }

        PrivateChannelImpl priv = new PrivateChannelImpl(privatechat.getString("id"), user)
                .setLastMessageId(privatechat.isNull("last_message_id") ? null : privatechat.getString("last_message_id"));
        user.setPrivateChannel(priv);

        if (user.isFake())
        {
            priv.setFake(true);
            api.getFakePrivateChannelMap().put(priv.getId(), priv);
        }
        else
            api.getPrivateChannelMap().put(priv.getId(), priv);
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
        return role.setName(roleJson.getString("name"))
                .setRawPosition(roleJson.getInt("position"))
                .setRawPermissions(roleJson.getLong("permissions"))
                .setManaged(roleJson.getBoolean("managed"))
                .setHoisted(roleJson.getBoolean("hoist"))
                .setColor(roleJson.getInt("color") != 0 ? new Color(roleJson.getInt("color")) : null)
                .setMentionable(roleJson.has("mentionable") && roleJson.getBoolean("mentionable"));
    }

    public Message createMessage(JSONObject jsonObject) { return createMessage(jsonObject, false); }
    public Message createMessage(JSONObject jsonObject, boolean exceptionOnMissingUser)
    {
        String channelId = jsonObject.getString("channel_id");
        MessageChannel chan = api.getTextChannelById(channelId);
        if (chan == null)
            chan = api.getPrivateChannelById(channelId);
        if (chan == null)
            chan = api.getFakePrivateChannelMap().get(channelId);
        if (chan == null && api.getAccountType() == AccountType.CLIENT)
            chan = api.asClient().getGroupById(channelId);
        if (chan == null)
            throw new IllegalArgumentException(MISSING_CHANNEL);

        return createMessage(jsonObject, chan, exceptionOnMissingUser);
    }
    public Message createMessage(JSONObject jsonObject, MessageChannel chan, boolean exceptionOnMissingUser)
    {
        String id = jsonObject.getString("id");
        String content = !jsonObject.isNull("content") ? jsonObject.getString("content") : "";

        JSONObject author = jsonObject.getJSONObject("author");
        String authorId = author.getString("id");
        boolean fromWebhook = jsonObject.has("webhook_id");

        MessageImpl message = new MessageImpl(id, chan, fromWebhook)
                .setContent(content)
                .setTime(!jsonObject.isNull("timestamp") ? OffsetDateTime.parse(jsonObject.getString("timestamp")) : OffsetDateTime.now())
                .setMentionsEveryone(!jsonObject.isNull("mention_everyone") && jsonObject.getBoolean("mention_everyone"))
                .setTTS(!jsonObject.isNull("tts") && jsonObject.getBoolean("tts"))
                .setPinned(!jsonObject.isNull("pinned") && jsonObject.getBoolean("pinned"));
        if (chan instanceof PrivateChannel)
        {
            if (StringUtils.equals(authorId, api.getSelfUser().getId()))
                message.setAuthor(api.getSelfUser());
            else
                message.setAuthor(((PrivateChannel) chan).getUser());
        }
        else if (chan instanceof Group)
        {
            UserImpl user = (UserImpl) api.getUserMap().get(authorId);
            if (user == null)
                user = (UserImpl) api.getFakeUserMap().get(authorId);
            if (user == null && fromWebhook)
                user = (UserImpl) createFakeUser(author, false);
            if (user == null)
            {
                if (exceptionOnMissingUser)
                    throw new IllegalArgumentException(MISSING_USER);   //Specifically for MESSAGE_CREATE
                else
                    user = (UserImpl) createFakeUser(author, false);  //Any message creation that isn't MESSAGE_CREATE
            }
            message.setAuthor(user);

            //If the message was sent by a cached fake user, lets update it.
            if (user.isFake() && !fromWebhook)
            {
                user.setName(author.getString("username"))
                        .setDiscriminator(author.get("discriminator").toString())
                        .setAvatarId(author.isNull("avatar") ? null : author.getString("avatar"))
                        .setBot(author.has("bot") && author.getBoolean("bot"));
            }
        }
        else
        {
            GuildImpl guild = (GuildImpl) ((TextChannel) chan).getGuild();
            Member member = guild.getMembersMap().get(authorId);
            User user = member != null ? member.getUser() : null;
            if (user != null)
                message.setAuthor(user);
            else if (fromWebhook || !exceptionOnMissingUser)
                message.setAuthor(createFakeUser(author, false));
            else
                throw new IllegalArgumentException(MISSING_USER);
        }

        List<Message.Attachment> attachments = new LinkedList<>();
        if (!jsonObject.isNull("attachments"))
        {
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

        if (jsonObject.has("reactions"))
        {
            JSONArray reactions = jsonObject.getJSONArray("reactions");
            List<MessageReaction> list = new LinkedList<>();
            for (int i = 0; i < reactions.length(); i++)
            {
                JSONObject obj = reactions.getJSONObject(i);
                JSONObject emoji = obj.getJSONObject("emoji");

                String emojiId = emoji.isNull("id") ? null : emoji.getString("id");
                String emojiName = emoji.getString("name");

                boolean self = obj.has("self") && obj.getBoolean("self");
                int count = obj.getInt("count");
                Emote emote = null;
                if (emojiId != null)
                {
                    emote = api.getEmoteById(emojiId);
                    if (emote == null)
                        emote = new EmoteImpl(emojiId, api).setName(emojiName);
                }
                MessageReaction.ReactionEmote reactionEmote;
                if (emote == null)
                    reactionEmote = new MessageReaction.ReactionEmote(emojiName, null, api);
                else
                    reactionEmote = new MessageReaction.ReactionEmote(emote);
                list.add(new MessageReaction(chan, reactionEmote, message.getId(), self, count));
            }
            message.setReactions(list);
        }

        if (message.isFromType(ChannelType.TEXT))
        {
            TextChannel textChannel = message.getTextChannel();
            TreeMap<Integer, User> mentionedUsers = new TreeMap<>();
            if (!jsonObject.isNull("mentions"))
            {
                JSONArray mentions = jsonObject.getJSONArray("mentions");
                for (int i = 0; i < mentions.length(); i++)
                {
                    JSONObject mention = mentions.getJSONObject(i);
                    User u = api.getUserMap().get(mention.getString("id"));
                    if (u != null)
                    {
                        //We do this to properly order the mentions. The array given by discord is out of order sometimes.

                        String mentionId = mention.getString("id");
                        int index = content.indexOf("<@" + mentionId + ">");
                        if (index < 0)
                            index = content.indexOf("<@!" + mentionId + ">");
                        mentionedUsers.put(index, u);
                    }
                }
            }
            message.setMentionedUsers(new LinkedList<User>(mentionedUsers.values()));

            TreeMap<Integer, Role> mentionedRoles = new TreeMap<>();
            if (!jsonObject.isNull("mention_roles"))
            {
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
        return message;
    }

    public MessageEmbed createMessageEmbed(JSONObject messageEmbed)
    {
        if (messageEmbed.isNull("type"))
            throw new JSONException("Encountered embed object with missing/null type field for Json: " + messageEmbed);
        EmbedType type = EmbedType.fromKey(messageEmbed.getString("type"));
       /* if (type == EmbedType.UNKNOWN)
            throw new JSONException("Discord provided us an unknown embed type.  Json: " + messageEmbed);*/
        MessageEmbedImpl embed = new MessageEmbedImpl()
                .setType(type)
                .setUrl(messageEmbed.isNull("url") ? null : messageEmbed.getString("url"))
                .setTitle(messageEmbed.isNull("title") ? null : messageEmbed.getString("title"))
                .setDescription(messageEmbed.isNull("description") ? null : messageEmbed.getString("description"))
                .setColor(messageEmbed.isNull("color") || messageEmbed.getInt("color") == 0 ? null : new Color(messageEmbed.getInt("color")))
                .setTimestamp(messageEmbed.isNull("timestamp") ? null : OffsetDateTime.parse(messageEmbed.getString("timestamp")));

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
            embed.setAuthor(new AuthorInfo(
                    authorJson.isNull("name") ? null : authorJson.getString("name"),
                    authorJson.isNull("url") ? null : authorJson.getString("url"),
                    authorJson.isNull("icon_url") ? null : authorJson.getString("icon_url"),
                    authorJson.isNull("proxy_icon_url") ? null : authorJson.getString("proxy_icon_url")));
        }
        else embed.setAuthor(null);

        if (messageEmbed.has("image"))
        {
            JSONObject imageJson = messageEmbed.getJSONObject("image");
            embed.setImage(new ImageInfo(
                    imageJson.isNull("url") ? null : imageJson.getString("url"),
                    imageJson.isNull("proxy_url") ? null : imageJson.getString("proxy_url"),
                    imageJson.isNull("width") ? -1 : imageJson.getInt("width"),
                    imageJson.isNull("height") ? -1 : imageJson.getInt("height")));
        }
        else embed.setImage(null);
        
        if (messageEmbed.has("footer"))
        {
            JSONObject footerJson = messageEmbed.getJSONObject("footer");
            embed.setFooter(new Footer(
                    footerJson.isNull("text") ? null : footerJson.getString("text"),
                    footerJson.isNull("icon_url") ? null : footerJson.getString("icon_url"),
                    footerJson.isNull("proxy_icon_url") ? null : footerJson.getString("proxy_icon_url")));
        }
        else embed.setFooter(null);
        
        if (messageEmbed.has("fields"))
        {
            JSONArray fieldsJson = messageEmbed.getJSONArray("fields");
            List<Field> fields = new LinkedList<>();
            for(int index=0; index<fieldsJson.length(); index++)
            {
                JSONObject fieldJson = fieldsJson.getJSONObject(index);
                fields.add(new Field(
                        fieldJson.isNull("name") ? null : fieldJson.getString("name"),
                        fieldJson.isNull("value") ? null : fieldJson.getString("value"),
                        !fieldJson.isNull("inline") && fieldJson.getBoolean("inline")));
            }
            embed.setFields(fields);
        }
        else embed.setFields(Collections.emptyList());
        
        if (messageEmbed.has("video"))
        {
            JSONObject videoJson = messageEmbed.getJSONObject("video");
            embed.setVideoInfo(new MessageEmbed.VideoInfo(
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
        long allow = override.getLong("allow");
        long deny = override.getLong("deny");

        switch (override.getString("type"))
        {
            case "member":
                Member member = chan.getGuild().getMemberById(id);
                if (member == null)
                    throw new IllegalArgumentException("Attempted to create a PermissionOverride for a non-existent user. Guild: " + chan.getGuild() + ", Channel: " + chan + ", JSON: " + override);

                permOverride = (PermissionOverrideImpl) chan.getPermissionOverride(member);
                if (permOverride == null)
                {
                    permOverride = new PermissionOverrideImpl(chan, member, null);
                    if (chan instanceof TextChannel)
                        ((TextChannelImpl) chan).getMemberOverrideMap().put(member, permOverride);
                    else
                        ((VoiceChannelImpl) chan).getMemberOverrideMap().put(member, permOverride);
                }
                break;
            case "role":
                Role role = ((GuildImpl) chan.getGuild()).getRolesMap().get(id);
                if (role == null)
                    throw new IllegalArgumentException("Attempted to create a PermissionOverride for a non-existent role! JSON: " + override);

                permOverride = (PermissionOverrideImpl) chan.getPermissionOverride(role);
                if (permOverride == null)
                {
                    permOverride = new PermissionOverrideImpl(chan, null, role);
                    if (chan instanceof TextChannel)
                        ((TextChannelImpl) chan).getRoleOverrideMap().put(role, permOverride);
                    else
                        ((VoiceChannelImpl) chan).getRoleOverrideMap().put(role, permOverride);
                }
                break;
            default:
                throw new IllegalArgumentException("Provided with an unknown PermissionOverride type! JSON: " + override);
        }
        return permOverride.setAllow(allow)
                .setDeny(deny);
    }

    public Webhook createWebhook(JSONObject object)
    {
        String id = object.getString("id");
        String token = !object.isNull("token") ? object.getString("token") : null;
        String guildId = object.getString("guild_id");
        String channelId = object.getString("channel_id");

        TextChannel channel = api.getTextChannelById(channelId);
        if (channel == null)
            throw new NullPointerException(String.format("Tried to create Webhook for an un-cached TextChannel! WebhookId: %s ChannelId: %s GuildId: %s",
                    id, channelId, guildId));

        Object name = !object.isNull("name") ? object.get("name") : JSONObject.NULL;
        Object avatar = !object.isNull("avatar") ? object.get("avatar") : JSONObject.NULL;

        JSONObject fakeUser = new JSONObject()
                    .put("username", name)
                    .put("discriminator", "0000")
                    .put("id", id)
                    .put("avatar", avatar);
        User defaultUser = createFakeUser(fakeUser, false);

        JSONObject ownerJson = object.getJSONObject("user");
        String userId = ownerJson.getString("id");

        User owner = api.getUserById(userId);
        if (owner == null)
        {
            ownerJson.put("id", userId);
            owner = createFakeUser(ownerJson, false);
        }

        return new WebhookImpl(channel, id).setToken(token).setOwner(channel.getGuild().getMember(owner)).setUser(defaultUser);
    }

    public Relationship createRelationship(JSONObject relationshipJson)
    {
        if (api.getAccountType() != AccountType.CLIENT)
            throw new AccountTypeException(AccountType.CLIENT, "Attempted to create a Relationship but the logged in account is not a CLIENT!");

        RelationshipType type = RelationshipType.fromKey(relationshipJson.getInt("type"));
        User user;
        if (type == RelationshipType.FRIEND)
            user = createUser(relationshipJson.getJSONObject("user"));
        else
            user = createFakeUser(relationshipJson.getJSONObject("user"), true);

        Relationship relationship = api.asClient().getRelationshipById(user.getId(), type);
        if (relationship == null)
        {
            switch (type)
            {
                case FRIEND:
                    relationship = new FriendImpl(user);
                    break;
                case BLOCKED:
                    relationship = new BlockedUserImpl(user);
                    break;
                case INCOMING_FRIEND_REQUEST:
                    relationship = new IncomingFriendRequestImpl(user);
                    break;
                case OUTGOING_FRIEND_REQUEST:
                    relationship = new OutgoingFriendRequestImpl(user);
                    break;
                default:
                    return null;
            }
            ((JDAClientImpl) api.asClient()).getRelationshipMap().put(user.getId(), relationship);
        }
        return relationship;
    }

    public Group createGroup(JSONObject groupJson)
    {
        if (api.getAccountType() != AccountType.CLIENT)
            throw new AccountTypeException(AccountType.CLIENT, "Attempted to create a Group but the logged in account is not a CLIENT!");

        String groupId = groupJson.getString("id");
        JSONArray recipients = groupJson.getJSONArray("recipients");
        String ownerId = groupJson.getString("owner_id");
        String name = !groupJson.isNull("name") ? groupJson.getString("name") : null;
        String iconId = !groupJson.isNull("icon") ? groupJson.getString("icon") : null;
        String lastMessage = !groupJson.isNull("last_message_id") ? groupJson.getString("last_message_id") : null;

        GroupImpl group = (GroupImpl) api.asClient().getGroupById(groupId);
        if (group == null)
        {
            group = new GroupImpl(groupId, api);
            ((JDAClientImpl) api.asClient()).getGroupMap().put(groupId, group);
        }

        HashMap<String, User> groupUsers = group.getUserMap();
        groupUsers.put(api.getSelfUser().getId(), api.getSelfUser());
        for (int i = 0; i < recipients.length(); i++)
        {
            JSONObject groupUser = recipients.getJSONObject(i);
            groupUsers.put(groupUser.getString("id"), createFakeUser(groupUser, true));
        }

        User owner = api.getUserMap().get(ownerId);
        if (owner == null)
            owner = api.getFakeUserMap().get(ownerId);
        if (owner == null)
            throw new IllegalArgumentException("Attempted to build a Group, but could not find user by provided owner id." +
                    "This should not be possible because the owner should be IN the group!");

        return group
                .setOwner(owner)
                .setLastMessageId(lastMessage)
                .setName(name)
                .setIconId(iconId);
    }

    public Invite createInvite(JSONObject object)
    {
        final String code = object.getString("code");

        final JSONObject channelObject = object.getJSONObject("channel");
        final String channelTypeName = channelObject.getString("type");
        final User inviter = object.has("inviter") ? this.createFakeUser(object.getJSONObject("inviter"), false) : null;

        final ChannelType channelType = channelTypeName.equals("text")
            ? ChannelType.TEXT
            : channelTypeName.equals("voice")
                ? ChannelType.VOICE
                : ChannelType.UNKNOWN;
        final String channelId = channelObject.getString("id");
        final String channelName = channelObject.getString("name");

        final Invite.Channel channel = new InviteImpl.ChannelImpl(channelId, channelName, channelType);

        final JSONObject guildObject = object.getJSONObject("guild");

        final String guildIconId = guildObject.isNull("icon") ? null : guildObject.getString("icon");
        final String guildId = guildObject.getString("id");
        final String guildName = guildObject.getString("name");
        final String guildSplashId = guildObject.isNull("splash") ? null : guildObject.getString("splash");

        final Invite.Guild guild = new InviteImpl.GuildImpl(guildId, guildIconId, guildName, guildSplashId);

        final int maxAge;
        final int maxUses;
        final boolean temporary;
        final OffsetDateTime timeCreated;
        final int uses;
        final boolean expanded;

        if (object.has("max_uses"))
        {
            expanded = true;
            maxAge = object.getInt("max_age");
            maxUses = object.getInt("max_uses");
            uses = object.getInt("uses");
            temporary = object.getBoolean("temporary");
            timeCreated = OffsetDateTime.parse(object.getString("created_at"));
        }
        else
        {
            expanded = false;
            maxAge = -1;
            maxUses = -1;
            uses = -1;
            temporary = false;
            timeCreated = null;
        }

        return new InviteImpl(api, code, expanded, inviter, maxAge, maxUses, temporary, timeCreated, uses, channel, guild);
    }

    public void clearCache()
    {
        cachedGuildJsons.clear();
        cachedGuildCallbacks.clear();
    }

    public ApplicationInfo createApplicationInfo(JSONObject object)
    {
        final String description = object.getString("description");
        final boolean doesBotRequireCodeGrant = object.getBoolean("bot_require_code_grant");
        final String iconId = object.has("icon") && !object.isNull("icon") ? object.getString("icon") : null;
        final String id = object.getString("id");
        final String name = object.getString("name");
        final boolean isBotPublic = object.getBoolean("bot_public");
        final User owner = createFakeUser(object.getJSONObject("owner"), false);

        return new ApplicationInfoImpl(api, description, doesBotRequireCodeGrant, iconId, id, isBotPublic, name, owner);
    }

    public Application createApplication(JSONObject object)
    {
        return new ApplicationImpl(api, object);
    }

    public AuthorizedApplication createAuthorizedApplication(JSONObject object)
    {
        final String authId = object.getString("id");

        JSONArray scopeArray = object.getJSONArray("scopes");
        List<String> scopes = new ArrayList<>(scopeArray.length());
        for (int i = 0; i < scopeArray.length(); i++)
        {
            scopes.add(scopeArray.getString(i));
        }
        JSONObject application = object.getJSONObject("application");

        final String description = application.getString("description");
        final String iconId = application.has("icon") ? application.getString("icon") : null;
        final String id = application.getString("id");
        final String name = application.getString("name");

        return new AuthorizedApplicationImpl(api, authId, description, iconId, id, name, scopes);
    }
}
