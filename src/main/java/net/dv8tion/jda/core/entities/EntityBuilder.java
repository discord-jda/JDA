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

package net.dv8tion.jda.core.entities;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import net.dv8tion.jda.bot.entities.ApplicationInfo;
import net.dv8tion.jda.bot.entities.impl.ApplicationInfoImpl;
import net.dv8tion.jda.client.entities.*;
import net.dv8tion.jda.client.entities.impl.*;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.WebSocketCode;
import net.dv8tion.jda.core.audit.ActionType;
import net.dv8tion.jda.core.audit.AuditLogChange;
import net.dv8tion.jda.core.audit.AuditLogEntry;
import net.dv8tion.jda.core.entities.Guild.VerificationLevel;
import net.dv8tion.jda.core.entities.MessageEmbed.*;
import net.dv8tion.jda.core.entities.impl.*;
import net.dv8tion.jda.core.exceptions.AccountTypeException;
import net.dv8tion.jda.core.handle.GuildMembersChunkHandler;
import net.dv8tion.jda.core.handle.ReadyHandler;
import net.dv8tion.jda.core.utils.Helpers;
import net.dv8tion.jda.core.utils.JDALogger;
import net.dv8tion.jda.core.utils.MiscUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class EntityBuilder
{
    public static final Logger LOG = JDALogger.getLog(EntityBuilder.class);
    public static final String MISSING_CHANNEL = "MISSING_CHANNEL";
    public static final String MISSING_USER = "MISSING_USER";
    public static final String UNKNOWN_MESSAGE_TYPE = "UNKNOWN_MESSAGE_TYPE";
    private static final Set<String> richGameFields;
    static
    {
        Set<String> tmp = new HashSet<>();
        tmp.add("application_id");
        tmp.add("assets");
        tmp.add("details");
        tmp.add("flags");
        tmp.add("party");
        tmp.add("session_id");
        tmp.add("state");
        tmp.add("sync_id");
        richGameFields = Collections.unmodifiableSet(tmp);
    }

    protected final JDAImpl api;
    protected final TLongObjectMap<JSONObject> cachedGuildJsons = MiscUtil.newLongMap();
    protected final TLongObjectMap<Consumer<Guild>> cachedGuildCallbacks = MiscUtil.newLongMap();

    public EntityBuilder(JDA api)
    {
        this.api = (JDAImpl) api;
    }

    public SelfUser createSelfUser(JSONObject self)
    {
        SelfUserImpl selfUser = ((SelfUserImpl) api.getSelfUser());
        if (selfUser == null)
        {
            final long id = self.getLong("id");
            selfUser = new SelfUserImpl(id, api);
            api.setSelfUser(selfUser);
        }

        if (!api.getUserMap().containsKey(selfUser.getIdLong()))
            api.getUserMap().put(selfUser.getIdLong(), selfUser);

        selfUser.setVerified(self.getBoolean("verified"))
                .setMfaEnabled(self.getBoolean("mfa_enabled"))
                .setName(self.getString("username"))
                .setDiscriminator(self.getString("discriminator"))
                .setAvatarId(self.optString("avatar", null))
                .setBot(Helpers.optBoolean(self, "bot"));

        if (this.api.getAccountType() == AccountType.CLIENT)
        {
            selfUser
                .setEmail(self.optString("email", null))
                .setMobile(Helpers.optBoolean(self, "mobile"))
                .setNitro(Helpers.optBoolean(self, "premium"))
                .setPhoneNumber(self.optString("phone", null));
        }

        return selfUser;
    }

    public Game createGame(String name, String url, Game.GameType type)
    {
        return new Game(name, url, type);
    }

    public void createGuildFirstPass(JSONObject guild, Consumer<Guild> secondPassCallback)
    {
        final long id = guild.getLong("id");
        GuildImpl guildObj = ((GuildImpl) api.getGuildMap().get(id));
        if (guildObj == null)
        {
            guildObj = new GuildImpl(api, id);
            api.getGuildMap().put(id, guildObj);
        }
        if (Helpers.optBoolean(guild, "unavailable"))
        {
            guildObj.setAvailable(false);
            //This is used for when GuildCreateHandler receives a guild that is currently unavailable. During normal READY
            // loading for bots (which unavailable is always true) the secondPassCallback parameter will always
            // be null.
            if (secondPassCallback != null)
                secondPassCallback.accept(guildObj);
            api.getGuildLock().lock(id);
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
                .setIconId(guild.optString("icon", null))
                .setSplashId(guild.optString("splash", null))
                .setRegion(guild.getString("region"))
                .setName(guild.getString("name"))
                .setAfkTimeout(Guild.Timeout.fromKey(guild.getInt("afk_timeout")))
                .setVerificationLevel(Guild.VerificationLevel.fromKey(guild.getInt("verification_level")))
                .setDefaultNotificationLevel(Guild.NotificationLevel.fromKey(guild.getInt("default_message_notifications")))
                .setRequiredMFALevel(Guild.MFALevel.fromKey(guild.getInt("mfa_level")))
                .setExplicitContentLevel(Guild.ExplicitContentLevel.fromKey(guild.getInt("explicit_content_filter")));


        if(guild.isNull("features"))
            guildObj.setFeatures(Collections.emptySet());
        else
        {
            guildObj.setFeatures(
                StreamSupport.stream(guild.getJSONArray("features").spliterator(), false)
                    .map(String::valueOf)
                    .collect(Collectors.toSet())
            );
        }

        JSONArray roles = guild.getJSONArray("roles");
        for (int i = 0; i < roles.length(); i++)
        {
            Role role = createRole(roles.getJSONObject(i), guildObj.getIdLong());
            guildObj.getRolesMap().put(role.getIdLong(), role);
            if (role.getIdLong() == guildObj.getIdLong())
                guildObj.setPublicRole(role);
        }

        if (!guild.isNull("emojis"))
        {
            JSONArray array = guild.getJSONArray("emojis");
            TLongObjectMap<Emote> emoteMap = guildObj.getEmoteMap();
            for (int i = 0; i < array.length(); i++)
            {
                JSONObject object = array.getJSONObject(i);
                if (object.isNull("id"))
                {
                    LOG.error("Received GUILD_CREATE with an emoji with a null ID. JSON: {}", object);
                    continue;
                }
                JSONArray emoteRoles = object.isNull("roles") ? new JSONArray() : object.getJSONArray("roles");
                final long emoteId = object.getLong("id");

                EmoteImpl emoteObj = new EmoteImpl(emoteId, guildObj);
                Set<Role> roleSet = emoteObj.getRoleSet();

                for (int j = 0; j < emoteRoles.length(); j++)
                    roleSet.add(guildObj.getRoleById(emoteRoles.getString(j)));
                emoteMap.put(emoteId, emoteObj
                            .setName(object.optString("name"))
                            .setAnimated(object.optBoolean("animated"))
                            .setManaged(Helpers.optBoolean(object, "managed")));
            }
        }

        if (guild.has("members"))
        {
            JSONArray members = guild.getJSONArray("members");
            createGuildMemberPass(guildObj, members);
        }

        //This could be null for Client accounts. Will be fixed by GUILD_SYNC
        Member owner = guildObj.getMemberById(guild.getLong("owner_id"));
        if (owner != null)
            guildObj.setOwner(owner);

        if (guild.has("presences"))
        {
            JSONArray presences = guild.getJSONArray("presences");
            for (int i = 0; i < presences.length(); i++)
            {
                JSONObject presence = presences.getJSONObject(i);
                final long userId = presence.getJSONObject("user").getLong("id");
                MemberImpl member = (MemberImpl) guildObj.getMembersMap().get(userId);

                if (member == null)
                    LOG.debug("Received a ghost presence in GuildFirstPass! UserId: {} Guild: {}", userId, guildObj);
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
                switch (type)
                {
                    case TEXT:
                        createTextChannel(channel, guildObj.getIdLong(), false);
                        break;
                    case VOICE:
                        createVoiceChannel(channel, guildObj.getIdLong(), false);
                        break;
                    case CATEGORY:
                        createCategory(channel, guildObj.getIdLong(), false);
                        break;
                    default:
                        LOG.error("Received a channel for a guild that isn't a text, voice or category channel. JSON: {}", channel);
                }
            }
        }

        if (!guild.isNull("system_channel_id"))
            guildObj.setSystemChannel(guildObj.getTextChannelsMap().get(guild.getLong("system_channel_id")));

        if (!guild.isNull("afk_channel_id"))
            guildObj.setAfkChannel(guildObj.getVoiceChannelsMap().get(guild.getLong("afk_channel_id")));

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
                            .put("op", WebSocketCode.GUILD_SYNC)
                            .put("guild_id", guildObj.getId());
                    api.getClient().chunkOrSyncRequest(obj);
                }
                JSONObject obj = new JSONObject()
                        .put("op", WebSocketCode.MEMBER_CHUNK_REQUEST)
                        .put("d", new JSONObject()
                            .put("guild_id", id)
                            .put("query","")
                            .put("limit", 0)
                        );
                api.getClient().chunkOrSyncRequest(obj);
            }
            else
            {
                ReadyHandler readyHandler = api.getClient().getHandler("READY");
                readyHandler.acknowledgeGuild(guildObj, true, true, api.getAccountType() == AccountType.CLIENT);
            }

            api.getGuildLock().lock(id);
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

        api.getGuildLock().unlock(guildObj.getIdLong());
        if (secondPassCallback != null)
            secondPassCallback.accept(guildObj);
    }

    public void createGuildSecondPass(long guildId, List<JSONArray> memberChunks)
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
            createGuildMemberPass(guildObj, chunk);

        Member owner = guildObj.getMemberById(guildJson.getLong("owner_id"));
        if (owner != null)
            guildObj.setOwner(owner);

        if (guildObj.getOwner() == null)
            LOG.error("Never set the Owner of the Guild: {} because we don't have the owner User object! How?!", guildObj.getId());

        JSONArray channels = guildJson.getJSONArray("channels");
        createGuildChannelPass(guildObj, channels);

        JSONArray voiceStates = guildJson.getJSONArray("voice_states");
        createGuildVoiceStatePass(guildObj, voiceStates);

        secondPassCallback.accept(guildObj);
        api.getGuildLock().unlock(guildId);
    }

    public void handleGuildSync(GuildImpl guild, JSONArray members, JSONArray presences)
    {
        for (int i = 0; i < members.length(); i++)
        {
            JSONObject memberJson = members.getJSONObject(i);
            createMember(guild, memberJson);
        }

        for (int i = 0; i < presences.length(); i++)
        {
            JSONObject presenceJson = presences.getJSONObject(i);
            final long userId = presenceJson.getJSONObject("user").getLong("id");

            MemberImpl member = (MemberImpl) guild.getMembersMap().get(userId);
            if (member == null)
                LOG.error("Received a Presence for a non-existent Member when dealing with GuildSync!");
            else
                this.createPresence(member, presenceJson);
        }
    }

    private void createGuildMemberPass(GuildImpl guildObj, JSONArray members)
    {
        for (int i = 0; i < members.length(); i++)
        {
            JSONObject memberJson = members.getJSONObject(i);
            createMember(guildObj, memberJson);
        }
    }

    private void createGuildChannelPass(GuildImpl guildObj, JSONArray channels)
    {
        for (int i = 0; i < channels.length(); i++)
        {
            JSONObject channel = channels.getJSONObject(i);
            ChannelType type = ChannelType.fromId(channel.getInt("type"));
            Channel channelObj = null;
            switch (type)
            {
                case TEXT:
                    channelObj = api.getTextChannelById(channel.getLong("id"));
                    break;
                case VOICE:
                    channelObj = api.getVoiceChannelById(channel.getLong("id"));
                    break;
                case CATEGORY:
                    channelObj = api.getCategoryMap().get(channel.getLong("id"));
                    break;
                default:
                    LOG.error("Received a channel for a guild that isn't a text, voice or category channel (ChannelPass). JSON: {}", channel);
            }

            if (channelObj != null)
            {
                JSONArray permissionOverwrites = channel.getJSONArray("permission_overwrites");
                createOverridesPass((AbstractChannelImpl<?>) channelObj, permissionOverwrites);
            }
            else
            {
                LOG.error("Got permission_override for unknown channel with id: {}", channel.getString("id"));
            }
        }
    }

    public void createGuildVoiceStatePass(GuildImpl guildObj, JSONArray voiceStates)
    {
        for (int i = 0; i < voiceStates.length(); i++)
        {
            JSONObject voiceStateJson = voiceStates.getJSONObject(i);
            final long userId = voiceStateJson.getLong("user_id");
            Member member = guildObj.getMembersMap().get(userId);
            if (member == null)
            {
                LOG.error("Received a VoiceState for a unknown Member! GuildId: "
                        + guildObj.getId() + " MemberId: " + voiceStateJson.getString("user_id"));
                continue;
            }

            final long channelId = voiceStateJson.getLong("channel_id");
            VoiceChannelImpl voiceChannel =
                    (VoiceChannelImpl) guildObj.getVoiceChannelsMap().get(channelId);
            if (voiceChannel != null)
                voiceChannel.getConnectedMembersMap().put(member.getUser().getIdLong(), member);
            else
                LOG.error("Received a GuildVoiceState with a channel ID for a non-existent channel! ChannelId: {} GuildId: {} UserId: {}",
                    channelId, guildObj.getId(), userId);

            // VoiceState is considered volatile so we don't expect anything to actually exist
            GuildVoiceStateImpl voiceState = (GuildVoiceStateImpl) member.getVoiceState();
            voiceState.setSelfMuted(Helpers.optBoolean(voiceStateJson, "self_mute"))
                      .setSelfDeafened(Helpers.optBoolean(voiceStateJson, "self_deaf"))
                      .setGuildMuted(Helpers.optBoolean(voiceStateJson, "mute"))
                      .setGuildDeafened(Helpers.optBoolean(voiceStateJson, "deaf"))
                      .setSuppressed(Helpers.optBoolean(voiceStateJson, "suppress"))
                      .setSessionId(voiceStateJson.optString("session_id"))
                      .setConnectedChannel(voiceChannel);
        }
    }

    public UserImpl createFakeUser(JSONObject user, boolean modifyCache) { return createUser(user, true, modifyCache); }
    public UserImpl createUser(JSONObject user)     { return createUser(user, false, true); }
    private UserImpl createUser(JSONObject user, boolean fake, boolean modifyCache)
    {
        final long id = user.getLong("id");
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
                    api.getUserMap().put(userObj.getIdLong(), userObj);
                    if (userObj.hasPrivateChannel())
                    {
                        PrivateChannelImpl priv = (PrivateChannelImpl) userObj.getPrivateChannel();
                        priv.setFake(false);
                        api.getFakePrivateChannelMap().remove(priv.getIdLong());
                        api.getPrivateChannelMap().put(priv.getIdLong(), priv);
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
                .setAvatarId(user.optString("avatar", null))
                .setBot(Helpers.optBoolean(user, "bot"));
    }

    public Member createMember(GuildImpl guild, JSONObject memberJson)
    {
        User user = createUser(memberJson.getJSONObject("user"));
        MemberImpl member = (MemberImpl) guild.getMember(user);
        if (member == null)
        {
            member = new MemberImpl(guild, user);
            guild.getMembersMap().put(user.getIdLong(), member);
        }

        ((GuildVoiceStateImpl) member.getVoiceState())
            .setGuildMuted(memberJson.getBoolean("mute"))
            .setGuildDeafened(memberJson.getBoolean("deaf"));

        member.setJoinDate(OffsetDateTime.parse(memberJson.getString("joined_at")))
              .setNickname(memberJson.optString("nick", null));

        JSONArray rolesJson = memberJson.getJSONArray("roles");
        for (int k = 0; k < rolesJson.length(); k++)
        {
            final long roleId = rolesJson.getLong(k);
            Role r = guild.getRolesMap().get(roleId);
            if (r == null)
            {
                LOG.debug("Received a Member with an unknown Role. MemberId: {} GuildId: {} roleId: {}",
                    member.getUser().getId(), guild.getId(), roleId);
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

        JSONObject gameJson = presenceJson.isNull("game") ? null : presenceJson.getJSONObject("game");
        OnlineStatus onlineStatus = OnlineStatus.fromKey(presenceJson.getString("status"));
        Game game = null;
        boolean parsedGame = false;

        if (gameJson != null && !gameJson.isNull("name"))
        {
            try
            {
                game = createGame(gameJson);
                parsedGame = true;
            }
            catch (Exception ex)
            {
                String userId;
                if (memberOrFriend instanceof Member)
                    userId = ((Member) memberOrFriend).getUser().getId();
                else if (memberOrFriend instanceof Friend)
                    userId = ((Friend) memberOrFriend).getUser().getId();
                else
                    userId = "unknown";
                if (LOG.isDebugEnabled())
                    LOG.warn("Encountered exception trying to parse a presence! UserId: {} JSON: {}", userId, gameJson, ex);
                else
                    LOG.warn("Encountered exception trying to parse a presence! UserId: {} Message: {} Enable debug for details", userId, ex.getMessage());
            }
        }
        if (memberOrFriend instanceof Member)
        {
            MemberImpl member = (MemberImpl) memberOrFriend;
            member.setOnlineStatus(onlineStatus);
            if (parsedGame)
                member.setGame(game);
        }
        else if (memberOrFriend instanceof Friend)
        {
            FriendImpl friend = (FriendImpl) memberOrFriend;
            friend.setOnlineStatus(onlineStatus);
            if (parsedGame)
                friend.setGame(game);

            OffsetDateTime lastModified = OffsetDateTime.ofInstant(
                    Instant.ofEpochMilli(presenceJson.getLong("last_modified")),
                    TimeZone.getTimeZone("GMT").toZoneId());

            friend.setOnlineStatusModifiedTime(lastModified);
        }
        else
            throw new IllegalArgumentException("An object was provided to EntityBuilder#createPresence that wasn't a Member or Friend. JSON: " + presenceJson);
    }

    public static Game createGame(JSONObject gameJson)
    {
        String name = String.valueOf(gameJson.get("name"));
        String url = gameJson.isNull("url") ? null : String.valueOf(gameJson.get("url"));
        Game.GameType type;
        try
        {
            type = gameJson.isNull("type")
                ? Game.GameType.DEFAULT
                : Game.GameType.fromKey(Integer.parseInt(gameJson.get("type").toString()));
        }
        catch (NumberFormatException e)
        {
            type = Game.GameType.DEFAULT;
        }

        RichPresence.Timestamps timestamps = null;
        if (!gameJson.isNull("timestamps"))
        {
            JSONObject obj = gameJson.getJSONObject("timestamps");
            long start, end;
            start = obj.isNull("start") ? 0 : obj.getLong("start");
            end = obj.isNull("end") ? 0 : obj.getLong("end");
            timestamps = new RichPresence.Timestamps(start, end);
        }

        if (!CollectionUtils.containsAny(gameJson.keySet(), richGameFields))
            return new Game(name, url, type, timestamps);

        // data for spotify
        long id = Helpers.optLong(gameJson, "application_id", 0);
        String sessionId = gameJson.optString("session_id", null);
        String syncId = gameJson.optString("sync_id", null);
        int flags = Helpers.optInt(gameJson, "flags", 0);
        String details = gameJson.isNull("details") ? null : String.valueOf(gameJson.get("details"));
        String state = gameJson.isNull("state") ? null : String.valueOf(gameJson.get("state"));

        RichPresence.Party party = null;
        if (!gameJson.isNull("party"))
        {
            JSONObject obj = gameJson.getJSONObject("party");
            String partyId = obj.isNull("id") ? null : obj.getString("id");
            JSONArray sizeArr = obj.isNull("size") ? null : obj.getJSONArray("size");
            long size = 0, max = 0;
            if (sizeArr != null && sizeArr.length() > 0)
            {
                size = sizeArr.getLong(0);
                max = sizeArr.isNull(1) ? 0 : sizeArr.getLong(1);
            }
            party = new RichPresence.Party(partyId, size, max);
        }

        String smallImageKey = null, smallImageText = null;
        String largeImageKey = null, largeImageText = null;
        if (!gameJson.isNull("assets"))
        {
            JSONObject assets = gameJson.getJSONObject("assets");
            if (!assets.isNull("small_image"))
            {
                smallImageKey = String.valueOf(assets.get("small_image"));
                smallImageText = assets.isNull("small_text") ? null : String.valueOf(assets.get("small_text"));
            }
            if (!assets.isNull("large_image"))
            {
                largeImageKey = String.valueOf(assets.get("large_image"));
                largeImageText = assets.isNull("large_text") ? null : String.valueOf(assets.get("large_text"));
            }
        }

        return new RichPresence(type, name, url,
            id, party, details, state, timestamps, syncId, sessionId, flags,
            largeImageKey, largeImageText, smallImageKey, smallImageText);
    }

    public Category createCategory(JSONObject json, long guildId)
    {
        return createCategory(json, guildId, true);
    }

    public Category createCategory(JSONObject json, long guildId, boolean guildIsLoaded)
    {
        final long id = json.getLong("id");
        CategoryImpl channel = (CategoryImpl) api.getCategoryMap().get(id);
        if (channel == null)
        {
            GuildImpl guild = ((GuildImpl) api.getGuildMap().get(guildId));
            channel = new CategoryImpl(id, guild);
            guild.getCategoriesMap().put(id, channel);
            api.getCategoryMap().put(id, channel);
        }

        if (!json.isNull("permission_overwrites") && guildIsLoaded)
        {
            JSONArray overrides = json.getJSONArray("permission_overwrites");
            createOverridesPass(channel, overrides);
        }

        return channel
                .setName(json.getString("name"))
                .setPosition(json.getInt("position"));
    }

    public TextChannel createTextChannel(JSONObject json, long guildId)
    {
        return createTextChannel(json, guildId, true);

    }

    public TextChannel createTextChannel(JSONObject json, long guildId, boolean guildIsLoaded)
    {
        final long id = json.getLong("id");
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
            createOverridesPass(channel, overrides);
        }

        return channel
                .setParent(Helpers.optLong(json, "parent_id", 0))
                .setLastMessageId(Helpers.optLong(json, "last_message_id", 0))
                .setName(json.getString("name"))
                .setTopic(json.optString("topic"))
                .setPosition(json.getInt("position"))
                .setNSFW(Helpers.optBoolean(json, "nsfw"));
    }

    public VoiceChannel createVoiceChannel(JSONObject json, long guildId)
    {
        return createVoiceChannel(json, guildId, true);
    }

    public VoiceChannel createVoiceChannel(JSONObject json, long guildId, boolean guildIsLoaded)
    {
        final long id = json.getLong("id");
        VoiceChannelImpl channel = ((VoiceChannelImpl) api.getVoiceChannelMap().get(id));
        if (channel == null)
        {
            GuildImpl guild = (GuildImpl) api.getGuildMap().get(guildId);
            channel = new VoiceChannelImpl(id, guild);
            guild.getVoiceChannelsMap().put(id, channel);
            api.getVoiceChannelMap().put(id, channel);
        }

        if (!json.isNull("permission_overwrites") && guildIsLoaded)
        {
            JSONArray overrides = json.getJSONArray("permission_overwrites");
            createOverridesPass(channel, overrides);
        }

        return channel
                .setParent(Helpers.optLong(json, "parent_id", 0))
                .setName(json.getString("name"))
                .setPosition(json.getInt("position"))
                .setUserLimit(json.getInt("user_limit"))
                .setBitrate(json.getInt("bitrate"));
    }

    public PrivateChannel createPrivateChannel(JSONObject privatechat)
    {
        JSONObject recipient = privatechat.has("recipients") ?
            privatechat.getJSONArray("recipients").getJSONObject(0) :
            privatechat.getJSONObject("recipient");
        final long userId = recipient.getLong("id");
        UserImpl user = ((UserImpl) api.getUserMap().get(userId));
        if (user == null)
        {   //The API can give us private channels connected to Users that we can no longer communicate with.
            // As such, make a fake user and fake private channel.
            user = (UserImpl) createFakeUser(recipient, true);
        }

        final long channelId = privatechat.getLong("id");
        PrivateChannelImpl priv = new PrivateChannelImpl(channelId, user)
                .setLastMessageId(Helpers.optLong(privatechat, "last_message_id", 0));
        user.setPrivateChannel(priv);

        if (user.isFake())
        {
            priv.setFake(true);
            api.getFakePrivateChannelMap().put(channelId, priv);
        }
        else
            api.getPrivateChannelMap().put(channelId, priv);
        return priv;
    }

    public void createOverridesPass(AbstractChannelImpl<?> channel, JSONArray overrides)
    {
        for (int i = 0; i < overrides.length(); i++)
        {
            try
            {
                createPermissionOverride(overrides.getJSONObject(i), channel);
            }
            catch (NoSuchElementException e)
            {
                //Caused by Discord not properly clearing PermissionOverrides when a Member leaves a Guild.
                LOG.debug("{}. Ignoring PermissionOverride.", e.getMessage());
            }
            catch (IllegalArgumentException e)
            {
                //Missing handling for a type
                LOG.warn("{}. Ignoring PermissionOverride.", e.getMessage());
            }
        }
    }

    public Role createRole(JSONObject roleJson, long guildId)
    {
        final long id = roleJson.getLong("id");
        GuildImpl guild = ((GuildImpl) api.getGuildMap().get(guildId));
        RoleImpl role = ((RoleImpl) guild.getRolesMap().get(id));
        if (role == null)
        {
            role = new RoleImpl(id, guild);
            guild.getRolesMap().put(id, role);
        }
        final int color = roleJson.getInt("color");
        return role.setName(roleJson.getString("name"))
                .setRawPosition(roleJson.getInt("position"))
                .setRawPermissions(roleJson.getLong("permissions"))
                .setManaged(roleJson.getBoolean("managed"))
                .setHoisted(roleJson.getBoolean("hoist"))
                .setColor(color == 0 ? Role.DEFAULT_COLOR_RAW : color)
                .setMentionable(roleJson.has("mentionable") && roleJson.getBoolean("mentionable"));
    }

    public Message createMessage(JSONObject jsonObject) { return createMessage(jsonObject, false); }
    public Message createMessage(JSONObject jsonObject, boolean exceptionOnMissingUser)
    {
        final long channelId = jsonObject.getLong("channel_id");

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
        final long id = jsonObject.getLong("id");
        String content = jsonObject.optString("content");

        JSONObject author = jsonObject.getJSONObject("author");
        final long authorId = author.getLong("id");
        final boolean fromWebhook = jsonObject.has("webhook_id");
        final boolean pinned = Helpers.optBoolean(jsonObject, "pinned");
        final boolean tts = Helpers.optBoolean(jsonObject, "tts");
        final boolean mentionsEveryone = Helpers.optBoolean(jsonObject, "mention_everyone");
        final OffsetDateTime editTime = jsonObject.isNull("edited_timestamp") ? null : OffsetDateTime.parse(jsonObject.getString("edited_timestamp"));
        final String nonce = jsonObject.isNull("nonce") ? null : jsonObject.get("nonce").toString();

        final List<Message.Attachment> attachments = map(jsonObject, "attachments", this::createMessageAttachment);
        final List<MessageEmbed>       embeds      = map(jsonObject, "embeds",      this::createMessageEmbed);
        final List<MessageReaction>    reactions   = map(jsonObject, "reactions",   (obj) -> createMessageReaction(chan, id, obj));

        User user;
        switch (chan.getType())
        {
            case PRIVATE:
                if (authorId == api.getSelfUser().getIdLong())
                    user = api.getSelfUser();
                else
                    user = ((PrivateChannel) chan).getUser();
                break;
            case GROUP:
                user = api.getUserById(authorId);
                if (user == null)
                    user = api.getFakeUserMap().get(authorId);
                if (user == null && fromWebhook)
                    user = createFakeUser(author, false);
                if (user == null)
                {
                    if (exceptionOnMissingUser)
                        throw new IllegalArgumentException(MISSING_USER); // Specifically for MESSAGE_CREATE
                    else
                        user = createFakeUser(author, false); // Any other message creation
                }

                if (user.isFake() && !fromWebhook)
                {
                    UserImpl impl = (UserImpl) user;
                    impl.setName(author.getString("username"))
                        .setDiscriminator(author.get("discriminator").toString())
                        .setAvatarId(author.optString("avatar", null))
                        .setBot(Helpers.optBoolean(author, "bot"));
                }
                break;
            case TEXT:
                Guild guild = ((TextChannel) chan).getGuild();
                Member member = guild.getMemberById(authorId);
                user = member != null ? member.getUser() : null;
                if (user == null)
                {
                    if (fromWebhook || !exceptionOnMissingUser)
                        user = createFakeUser(author, false);
                    else
                        throw new IllegalArgumentException(MISSING_USER); // Specifically for MESSAGE_CREATE
                }
                break;
            default: throw new IllegalArgumentException("Invalid Channel for creating a Message [" + chan.getType() + ']');
        }

        TLongSet mentionedRoles = new TLongHashSet();
        TLongSet mentionedUsers = new TLongHashSet(map(jsonObject, "mentions", (o) -> o.getLong("id")));
        JSONArray roleMentionArr = jsonObject.optJSONArray("mention_roles");
        if (roleMentionArr != null)
        {
            for (int i = 0; i < roleMentionArr.length(); i++)
                mentionedRoles.add(roleMentionArr.getLong(i));
        }

        MessageType type = MessageType.fromId(jsonObject.getInt("type"));
        switch (type)
        {
            case DEFAULT:
                return new ReceivedMessage(id, chan, type, fromWebhook,
                    mentionsEveryone, mentionedUsers, mentionedRoles, tts, pinned,
                    content, nonce, user, editTime, reactions, attachments, embeds);
            case UNKNOWN:
                throw new IllegalArgumentException(UNKNOWN_MESSAGE_TYPE);
            default:
                return new SystemMessage(id, chan, type, fromWebhook,
                    mentionsEveryone, mentionedUsers, mentionedRoles, tts, pinned,
                    content, nonce, user, editTime, reactions, attachments, embeds);
        }

    }

    public MessageReaction createMessageReaction(MessageChannel chan, long id, JSONObject obj)
    {
        JSONObject emoji = obj.getJSONObject("emoji");
        final Long emojiID = emoji.isNull("id") ? null : emoji.getLong("id");
        final String name = emoji.optString("name", null);
        final boolean animated = emoji.optBoolean("animated");
        final int count = Helpers.optInt(obj, "count", -1);
        final boolean me = Helpers.optBoolean(obj, "me");

        final MessageReaction.ReactionEmote reactionEmote;
        if (emojiID != null)
        {
            Emote emote = api.getEmoteById(emojiID);
            // creates fake emoji because no guild has this emoji id
            if (emote == null)
                emote = new EmoteImpl(emojiID, api).setAnimated(animated).setName(name);
            reactionEmote = new MessageReaction.ReactionEmote(emote);
        }
        else
        {
            reactionEmote = new MessageReaction.ReactionEmote(name, null, api);
        }

        return new MessageReaction(chan, reactionEmote, id, me, count);
    }

    public Message.Attachment createMessageAttachment(JSONObject jsonObject)
    {
        final int width = Helpers.optInt(jsonObject, "width", -1);
        final int height = Helpers.optInt(jsonObject, "height", -1);
        final int size = jsonObject.getInt("size");
        final String url = jsonObject.optString("url", null);
        final String proxyUrl = jsonObject.optString("proxy_url", null);
        final String filename = jsonObject.getString("filename");
        final long id = jsonObject.getLong("id");
        return new Message.Attachment(id, url, proxyUrl, filename, size, height, width, api);
    }

    public MessageEmbed createMessageEmbed(JSONObject content)
    {
        if (content.isNull("type"))
            throw new JSONException("Encountered embed object with missing/null type field for Json: " + content);
        EmbedType type = EmbedType.fromKey(content.getString("type"));
        final String url = content.optString("url", null);
        final String title = content.optString("title", null);
        final String description = content.optString("description", null);
        final OffsetDateTime timestamp = content.isNull("timestamp") ? null : OffsetDateTime.parse(content.getString("timestamp"));
        final int color = content.isNull("color") ? Role.DEFAULT_COLOR_RAW : content.getInt("color");

        final Thumbnail thumbnail;
        if (content.isNull("thumbnail"))
        {
            thumbnail = null;
        }
        else
        {
            JSONObject obj = content.getJSONObject("thumbnail");
            thumbnail = new Thumbnail(obj.optString("url", null),
                                      obj.optString("proxy_url", null),
                                      Helpers.optInt(obj, "width", -1),
                                      Helpers.optInt(obj, "height", -1));
        }

        final Provider provider;
        if (content.isNull("provider"))
        {
            provider = null;
        }
        else
        {
            JSONObject obj = content.getJSONObject("provider");
            provider = new Provider(obj.optString("name", null),
                                    obj.optString("url", null));
        }

        final AuthorInfo author;
        if (content.isNull("author"))
        {
            author = null;
        }
        else
        {
            JSONObject obj = content.getJSONObject("author");
            author = new AuthorInfo(obj.optString("name", null),
                                    obj.optString("url", null),
                                    obj.optString("icon_url", null),
                                    obj.optString("proxy_icon_url", null));
        }

        final VideoInfo video;
        if (content.isNull("video"))
        {
            video = null;
        }
        else
        {
            JSONObject obj = content.getJSONObject("video");
            video = new VideoInfo(obj.optString("url"),
                                  Helpers.optInt(obj, "width", -1),
                                  Helpers.optInt(obj, "height", -1));
        }

        final Footer footer;
        if (content.isNull("footer"))
        {
            footer = null;
        }
        else
        {
            JSONObject obj = content.getJSONObject("footer");
            footer = new Footer(obj.optString("text", null),
                                obj.optString("icon_url", null),
                                obj.optString("proxy_icon_url", null));
        }

        final ImageInfo image;
        if (content.isNull("image"))
        {
            image = null;
        }
        else
        {
            JSONObject obj = content.getJSONObject("image");
            image = new ImageInfo(obj.optString("url", null),
                                  obj.optString("proxy_url", null),
                                  Helpers.optInt(obj, "width", -1),
                                  Helpers.optInt(obj, "height", -1));
        }

        final List<Field> fields = map(content, "fields", (obj) ->
            new Field(obj.optString("name", null),
                      obj.optString("value", null),
                      Helpers.optBoolean(obj, "inline"),
                      false)
        );

        return createMessageEmbed(url, title, description, type, timestamp,
                color, thumbnail, provider, author, video, footer, image, fields);
    }

    public static MessageEmbed createMessageEmbed(String url, String title, String description, EmbedType type, OffsetDateTime timestamp,
                                           int color, Thumbnail thumbnail, Provider siteProvider, AuthorInfo author,
                                           VideoInfo videoInfo, Footer footer, ImageInfo image, List<Field> fields)
    {
        return new MessageEmbed(url, title, description, type, timestamp,
            color, thumbnail, siteProvider, author, videoInfo, footer, image, fields);
    }

    public PermissionOverride createPermissionOverride(JSONObject override, Channel chan)
    {
        PermissionOverrideImpl permOverride;
        final long id = override.getLong("id");
        long allow = override.getLong("allow");
        long deny = override.getLong("deny");

        //Throwing NoSuchElementException for common issues with overrides that are not cleared properly by discord
        // when a member leaves or a role is deleted
        switch (override.getString("type"))
        {
            case "member":
                Member member = chan.getGuild().getMemberById(id);
                if (member == null)
                    throw new NoSuchElementException("Attempted to create a PermissionOverride for a non-existent user. Guild: " + chan.getGuild() + ", Channel: " + chan + ", JSON: " + override);

                permOverride = (PermissionOverrideImpl) chan.getPermissionOverride(member);
                if (permOverride == null)
                {
                    permOverride = new PermissionOverrideImpl(chan, member.getUser().getIdLong(), member);
                    ((AbstractChannelImpl<?>) chan).getOverrideMap().put(member.getUser().getIdLong(), permOverride);
                }
                break;
            case "role":
                Role role = ((GuildImpl) chan.getGuild()).getRolesMap().get(id);
                if (role == null)
                    throw new NoSuchElementException("Attempted to create a PermissionOverride for a non-existent role! JSON: " + override);

                permOverride = (PermissionOverrideImpl) chan.getPermissionOverride(role);
                if (permOverride == null)
                {
                    permOverride = new PermissionOverrideImpl(chan, role.getIdLong(), role);
                    ((AbstractChannelImpl<?>) chan).getOverrideMap().put(role.getIdLong(), permOverride);
                }
                break;
            default:
                throw new IllegalArgumentException("Provided with an unknown PermissionOverride type! JSON: " + override);
        }
        return permOverride.setAllow(allow).setDeny(deny);
    }

    public WebhookImpl createWebhook(JSONObject object)
    {
        final long id = object.getLong("id");
        final long guildId = object.getLong("guild_id");
        final long channelId = object.getLong("channel_id");
        final String token = object.optString("token", null);

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

        JSONObject ownerJson = object.optJSONObject("user");
        User owner = null;
        
        if (ownerJson != null)
        {
            final long userId = ownerJson.getLong("id");

            owner = api.getUserById(userId);
            if (owner == null)
            {
                ownerJson.put("id", userId);
                owner = createFakeUser(ownerJson, false);
            }
        }
        
        return new WebhookImpl(channel, id)
                .setToken(token)
                .setOwner(owner == null ? null : channel.getGuild().getMember(owner))
                .setUser(defaultUser);
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

        Relationship relationship = api.asClient().getRelationshipById(user.getIdLong(), type);
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
            api.asClient().getRelationshipMap().put(user.getIdLong(), relationship);
        }
        return relationship;
    }

    public Group createGroup(JSONObject groupJson)
    {
        if (api.getAccountType() != AccountType.CLIENT)
            throw new AccountTypeException(AccountType.CLIENT, "Attempted to create a Group but the logged in account is not a CLIENT!");

        final long groupId = groupJson.getLong("id");
        JSONArray recipients = groupJson.getJSONArray("recipients");
        final long ownerId = groupJson.getLong("owner_id");
        final String name = groupJson.optString("name", null);
        final String iconId = groupJson.optString("icon", null);
        final long lastMessage = Helpers.optLong(groupJson, "last_message_id", 0);

        GroupImpl group = (GroupImpl) api.asClient().getGroupById(groupId);
        if (group == null)
        {
            group = new GroupImpl(groupId, api);
            api.asClient().getGroupMap().put(groupId, group);
        }

        TLongObjectMap<User> groupUsers = group.getUserMap();
        groupUsers.put(api.getSelfUser().getIdLong(), api.getSelfUser());
        for (int i = 0; i < recipients.length(); i++)
        {
            JSONObject groupUser = recipients.getJSONObject(i);
            groupUsers.put(groupUser.getLong("id"), createFakeUser(groupUser, true));
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

        final User inviter = object.has("inviter") ? this.createFakeUser(object.getJSONObject("inviter"), false) : null;

        final JSONObject channelObject = object.getJSONObject("channel");

        final ChannelType channelType = ChannelType.fromId(channelObject.getInt("type"));
        final long channelId = channelObject.getLong("id");
        final String channelName = channelObject.getString("name");

        final Invite.Channel channel = new InviteImpl.ChannelImpl(channelId, channelName, channelType);

        final JSONObject guildObject = object.getJSONObject("guild");

        final String guildIconId = guildObject.optString("icon", null);
        final long guildId = guildObject.getLong("id");
        final String guildName = guildObject.getString("name");
        final String guildSplashId = guildObject.optString("splash", null);
        final VerificationLevel guildVerificationLevel = VerificationLevel.fromKey(Helpers.optInt(guildObject, "verification_level", -1));
        final int presenceCount = Helpers.optInt(object, "approximate_presence_count", -1);
        final int memberCount = Helpers.optInt(object, "approximate_member_count", -1);
        final Set<String> guildFeatures;

        if (guildObject.isNull("features"))
        {
            guildFeatures = Collections.emptySet();
        }
        else
        {
            guildFeatures = Collections.unmodifiableSet(StreamSupport.stream(guildObject.getJSONArray("features").spliterator(), false).map(String::valueOf).collect(Collectors.toSet()));
        }

        final Invite.Guild guild = new InviteImpl.GuildImpl(guildId, guildIconId, guildName, guildSplashId, guildVerificationLevel, presenceCount, memberCount, guildFeatures);

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

        return new InviteImpl(api, code, expanded, inviter,
                              maxAge, maxUses, temporary,
                              timeCreated, uses, channel, guild);
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
        final String iconId = object.optString("icon", null);
        final long id = object.getLong("id");
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
        final long authId = object.getLong("id");

        JSONArray scopeArray = object.getJSONArray("scopes");
        List<String> scopes = new ArrayList<>(scopeArray.length());
        for (int i = 0; i < scopeArray.length(); i++)
        {
            scopes.add(scopeArray.getString(i));
        }
        JSONObject application = object.getJSONObject("application");

        final String description = application.getString("description");
        final String iconId = application.has("icon") ? application.getString("icon") : null;
        final long id = application.getLong("id");
        final String name = application.getString("name");

        return new AuthorizedApplicationImpl(api, authId, description, iconId, id, name, scopes);
    }

    public AuditLogEntry createAuditLogEntry(GuildImpl guild, JSONObject entryJson, JSONObject userJson, JSONObject webhookJson)
    {
        final long targetId = Helpers.optLong(entryJson, "target_id", 0);
        final long id = entryJson.getLong("id");
        final int typeKey = entryJson.getInt("action_type");
        final JSONArray changes = entryJson.isNull("changes") ? null : entryJson.getJSONArray("changes");
        final JSONObject options = entryJson.isNull("options") ? null : entryJson.getJSONObject("options");
        final String reason = entryJson.optString("reason", null);

        final UserImpl user = userJson == null ? null : createFakeUser(userJson, false);
        final WebhookImpl webhook = webhookJson == null ? null : createWebhook(webhookJson);
        final Set<AuditLogChange> changesList;
        final ActionType type = ActionType.from(typeKey);

        if (changes != null)
        {
            changesList = new HashSet<>(changes.length());
            for (int i = 0; i < changes.length(); i++)
            {
                final JSONObject object = changes.getJSONObject(i);
                AuditLogChange change = createAuditLogChange(object);
                changesList.add(change);
            }
        }
        else
        {
            changesList = Collections.emptySet();
        }

        CaseInsensitiveMap<String, AuditLogChange> changeMap = new CaseInsensitiveMap<>(changeToMap(changesList));
        CaseInsensitiveMap<String, Object> optionMap = options != null
                ? new CaseInsensitiveMap<>(options.toMap()) : null;

        return new AuditLogEntry(type, id, targetId, guild, user, webhook, reason, changeMap, optionMap);
    }

    public AuditLogChange createAuditLogChange(JSONObject change)
    {
        final String key = change.getString("key");
        Object oldValue = change.isNull("old_value") ? null : change.get("old_value");
        Object newValue = change.isNull("new_value") ? null : change.get("new_value");

        // Don't confront users with JSON
        if (oldValue instanceof JSONArray || newValue instanceof JSONArray)
        {
            oldValue = oldValue instanceof JSONArray ? ((JSONArray) oldValue).toList() : oldValue;
            newValue = newValue instanceof JSONArray ? ((JSONArray) newValue).toList() : newValue;
        }
        else if (oldValue instanceof JSONObject || newValue instanceof JSONObject)
        {
            oldValue = oldValue instanceof JSONObject ? ((JSONObject) oldValue).toMap() : oldValue;
            newValue = newValue instanceof JSONObject ? ((JSONObject) newValue).toMap() : newValue;
        }

        return new AuditLogChange(oldValue, newValue, key);
    }

    private Map<String, AuditLogChange> changeToMap(Set<AuditLogChange> changesList)
    {
        return changesList.stream().collect(Collectors.toMap(AuditLogChange::getKey, UnaryOperator.identity()));
    }

    private <T> List<T> map(JSONObject jsonObject, String key, Function<JSONObject, T> convert)
    {
        if (jsonObject.isNull(key))
            return Collections.emptyList();

        final JSONArray arr = jsonObject.getJSONArray(key);
        final List<T> mappedObjects = new ArrayList<>(arr.length());
        for (int i = 0; i < arr.length(); i++)
        {
            JSONObject obj = arr.getJSONObject(i);
            mappedObjects.add(convert.apply(obj));
        }

        return mappedObjects;
    }
}
