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

package net.dv8tion.jda.internal.entities;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogChange;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.Guild.VerificationLevel;
import net.dv8tion.jda.api.entities.MessageEmbed.*;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateBoostTimeEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateAvatarEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateDiscriminatorEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateFlagsEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.handle.EventCache;
import net.dv8tion.jda.internal.utils.JDALogger;
import net.dv8tion.jda.internal.utils.UnlockHook;
import net.dv8tion.jda.internal.utils.cache.MemberCacheViewImpl;
import net.dv8tion.jda.internal.utils.cache.SnowflakeCacheViewImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.function.Function;
import java.util.function.ToLongFunction;
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

    public EntityBuilder(JDA api)
    {
        this.api = (JDAImpl) api;
    }

    public JDAImpl getJDA()
    {
        return api;
    }

    public SelfUser createSelfUser(DataObject self)
    {
        SelfUserImpl selfUser = (SelfUserImpl) (getJDA().hasSelfUser() ? getJDA().getSelfUser() : null);
        if (selfUser == null)
        {
            final long id = self.getLong("id");
            selfUser = new SelfUserImpl(id, getJDA());
            getJDA().setSelfUser(selfUser);
        }

        SnowflakeCacheViewImpl<User> userView = getJDA().getUsersView();
        try (UnlockHook hook = userView.writeLock())
        {
            if (userView.getElementById(selfUser.getIdLong()) == null)
                userView.getMap().put(selfUser.getIdLong(), selfUser);
        }

        selfUser.setVerified(self.getBoolean("verified"))
                .setMfaEnabled(self.getBoolean("mfa_enabled"))
                .setName(self.getString("username"))
                .setDiscriminator(self.getString("discriminator"))
                .setAvatarId(self.getString("avatar", null))
                .setBot(self.getBoolean("bot"));

        return selfUser;
    }

    public static Activity createActivity(String name, String url, Activity.ActivityType type)
    {
        return new ActivityImpl(name, url, type);
    }

    private void createGuildEmotePass(GuildImpl guildObj, DataArray array)
    {
        if (!getJDA().isCacheFlagSet(CacheFlag.EMOTE))
            return;
        SnowflakeCacheViewImpl<Emote> emoteView = guildObj.getEmotesView();
        try (UnlockHook hook = emoteView.writeLock())
        {
            TLongObjectMap<Emote> emoteMap = emoteView.getMap();
            for (int i = 0; i < array.length(); i++)
            {
                DataObject object = array.getObject(i);
                if (object.isNull("id"))
                {
                    LOG.error("Received GUILD_CREATE with an emoji with a null ID. JSON: {}", object);
                    continue;
                }
                final long emoteId = object.getLong("id");
                emoteMap.put(emoteId, createEmote(guildObj, object, false));
            }
        }
    }

    public TLongObjectMap<DataObject> convertToUserMap(ToLongFunction<DataObject> getId, DataArray array)
    {
        TLongObjectMap<DataObject> map = new TLongObjectHashMap<>();
        for (int i = 0; i < array.length(); i++)
        {
            DataObject obj = array.getObject(i);
            long userId = getId.applyAsLong(obj);
            map.put(userId, obj);
        }
        return map;
    }

    public GuildImpl createGuild(long guildId, DataObject guildJson, TLongObjectMap<DataObject> members, int memberCount)
    {
        final GuildImpl guildObj = new GuildImpl(getJDA(), guildId);
        final String name = guildJson.getString("name", "");
        final String iconId = guildJson.getString("icon", null);
        final String splashId = guildJson.getString("splash", null);
        final String region = guildJson.getString("region", null);
        final String description = guildJson.getString("description", null);
        final String vanityCode = guildJson.getString("vanity_url_code", null);
        final String bannerId = guildJson.getString("banner", null);
        final String locale = guildJson.getString("preferred_locale", "en");
        final DataArray roleArray = guildJson.getArray("roles");
        final DataArray channelArray = guildJson.getArray("channels");
        final DataArray emotesArray = guildJson.getArray("emojis");
        final DataArray voiceStateArray = guildJson.getArray("voice_states");
        final Optional<DataArray> featuresArray = guildJson.optArray("features");
        final Optional<DataArray> presencesArray = guildJson.optArray("presences");
        final long ownerId = guildJson.getUnsignedLong("owner_id", 0L);
        final long afkChannelId = guildJson.getUnsignedLong("afk_channel_id", 0L);
        final long systemChannelId = guildJson.getUnsignedLong("system_channel_id", 0L);
        final int boostCount = guildJson.getInt("premium_subscription_count", 0);
        final int boostTier = guildJson.getInt("premium_tier", 0);
        final int maxMembers = guildJson.getInt("max_members", 0);
        final int maxPresences = guildJson.getInt("max_presences", 5000);
        final int mfaLevel = guildJson.getInt("mfa_level", 0);
        final int afkTimeout = guildJson.getInt("afk_timeout", 0);
        final int verificationLevel = guildJson.getInt("verification_level", 0);
        final int notificationLevel = guildJson.getInt("default_message_notifications", 0);
        final int explicitContentLevel = guildJson.getInt("explicit_content_filter", 0);

        guildObj.setAvailable(true)
                .setName(name)
                .setIconId(iconId)
                .setSplashId(splashId)
                .setRegion(region)
                .setDescription(description)
                .setBannerId(bannerId)
                .setVanityCode(vanityCode)
                .setMaxMembers(maxMembers)
                .setMaxPresences(maxPresences)
                .setOwnerId(ownerId)
                .setAfkTimeout(Guild.Timeout.fromKey(afkTimeout))
                .setVerificationLevel(VerificationLevel.fromKey(verificationLevel))
                .setDefaultNotificationLevel(Guild.NotificationLevel.fromKey(notificationLevel))
                .setExplicitContentLevel(Guild.ExplicitContentLevel.fromKey(explicitContentLevel))
                .setRequiredMFALevel(Guild.MFALevel.fromKey(mfaLevel))
                .setLocale(locale)
                .setBoostCount(boostCount)
                .setBoostTier(boostTier)
                .setMemberCount(memberCount);

        SnowflakeCacheViewImpl<Guild> guildView = getJDA().getGuildsView();
        try (UnlockHook hook = guildView.writeLock())
        {
            guildView.getMap().put(guildId, guildObj);
        }

        guildObj.setFeatures(featuresArray.map(it ->
            StreamSupport.stream(it.spliterator(), false)
                         .map(String::valueOf)
                         .collect(Collectors.toSet())
        ).orElse(Collections.emptySet()));

        SnowflakeCacheViewImpl<Role> roleView = guildObj.getRolesView();
        try (UnlockHook hook = roleView.writeLock())
        {
            TLongObjectMap<Role> map = roleView.getMap();
            for (int i = 0; i < roleArray.length(); i++)
            {
                DataObject obj = roleArray.getObject(i);
                Role role = createRole(guildObj, obj, guildId);
                map.put(role.getIdLong(), role);
                if (role.getIdLong() == guildObj.getIdLong())
                    guildObj.setPublicRole(role);
            }
        }

        for (int i = 0; i < channelArray.length(); i++)
        {
            DataObject channelJson = channelArray.getObject(i);
            createGuildChannel(guildObj, channelJson);
        }

        TLongObjectMap<DataObject> voiceStates = convertToUserMap((o) -> o.getUnsignedLong("user_id", 0L), voiceStateArray);
        TLongObjectMap<DataObject> presences = presencesArray.map(o1 -> convertToUserMap(o2 -> o2.getObject("user").getUnsignedLong("id"), o1)).orElseGet(TLongObjectHashMap::new);
        try (UnlockHook h1 = guildObj.getMembersView().writeLock();
             UnlockHook h2 = getJDA().getUsersView().writeLock())
        {
            //Add members to cache when subscriptions are disabled when they appear here
            // this is done because we can still keep track of members in voice channels
            for (DataObject memberJson : members.valueCollection())
            {
                long userId = memberJson.getObject("user").getUnsignedLong("id");
                DataObject voiceState = voiceStates.get(userId);
                DataObject presence = presences.get(userId);
                updateMemberCache(createMember(guildObj, memberJson, voiceState, presence));
            }
        }

        if (guildObj.getOwner() == null)
            LOG.debug("Finished setup for guild with a null owner. GuildId: {} OwnerId: {}", guildId, guildJson.opt("owner_id").orElse(null));


        createGuildEmotePass(guildObj, emotesArray);

        guildObj.setAfkChannel(guildObj.getVoiceChannelById(afkChannelId))
                .setSystemChannel(guildObj.getTextChannelById(systemChannelId));

        return guildObj;
    }

    private void createGuildChannel(GuildImpl guildObj, DataObject channelData)
    {
        final ChannelType channelType = ChannelType.fromId(channelData.getInt("type"));
        switch (channelType)
        {
        case TEXT:
            createTextChannel(guildObj, channelData, guildObj.getIdLong());
            break;
        case VOICE:
            createVoiceChannel(guildObj, channelData, guildObj.getIdLong());
            break;
        case CATEGORY:
            createCategory(guildObj, channelData, guildObj.getIdLong());
            break;
        case STORE:
            createStoreChannel(guildObj, channelData, guildObj.getIdLong());
            break;
        default:
            LOG.debug("Cannot create channel for type " + channelData.getInt("type"));
        }
    }

    public UserImpl createUser(DataObject user)
    {
        boolean newUser = false;
        final long id = user.getLong("id");
        UserImpl userObj;

        SnowflakeCacheViewImpl<User> userView = getJDA().getUsersView();
        try (UnlockHook hook = userView.readLock())
        {
            userObj = (UserImpl) userView.getElementById(id);
            if (userObj == null)
            {
                userObj = new UserImpl(id, getJDA());
                newUser = true;
            }
        }

        if (newUser)
        {
            // Initial creation
            userObj.setName(user.getString("username"))
                   .setDiscriminator(user.get("discriminator").toString())
                   .setAvatarId(user.getString("avatar", null))
                   .setBot(user.getBoolean("bot"))
                   .setFlags(user.getInt("public_flags", 0));
        }
        else
        {
            // Fire update events
            updateUser(userObj, user);
        }

        return userObj;
    }

    public void updateUser(UserImpl userObj, DataObject user)
    {
        String oldName = userObj.getName();
        String newName = user.getString("username");
        String oldDiscriminator = userObj.getDiscriminator();
        String newDiscriminator = user.get("discriminator").toString();
        String oldAvatar = userObj.getAvatarId();
        String newAvatar = user.getString("avatar", null);
        int oldFlags = userObj.getFlagsRaw();
        int newFlags = user.getInt("public_flags", 0);

        JDAImpl jda = getJDA();
        long responseNumber = jda.getResponseTotal();
        if (!oldName.equals(newName))
        {
            userObj.setName(newName);
            jda.handleEvent(
                new UserUpdateNameEvent(
                    jda, responseNumber,
                    userObj, oldName));
        }

        if (!oldDiscriminator.equals(newDiscriminator))
        {
            userObj.setDiscriminator(newDiscriminator);
            jda.handleEvent(
                new UserUpdateDiscriminatorEvent(
                    jda, responseNumber,
                    userObj, oldDiscriminator));
        }

        if (!Objects.equals(oldAvatar, newAvatar))
        {
            userObj.setAvatarId(newAvatar);
            jda.handleEvent(
                new UserUpdateAvatarEvent(
                    jda, responseNumber,
                    userObj, oldAvatar));
        }

        if (oldFlags != newFlags)
        {
            userObj.setFlags(newFlags);
            jda.handleEvent(
                    new UserUpdateFlagsEvent(
                        jda, responseNumber,
                        userObj, User.UserFlag.getFlags(oldFlags)));
        }
    }

    public boolean updateMemberCache(MemberImpl member)
    {
        return updateMemberCache(member, false);
    }

    public boolean updateMemberCache(MemberImpl member, boolean forceRemove)
    {
        GuildImpl guild = member.getGuild();
        UserImpl user = (UserImpl) member.getUser();
        MemberCacheViewImpl membersView = guild.getMembersView();
        if (forceRemove || !getJDA().cacheMember(member))
        {
            if (membersView.remove(member.getIdLong()) == null)
                return false;
            LOG.trace("Unloading member {}", member);
            if (user.getMutualGuilds().isEmpty())
            {
                // we no longer share any guilds/channels with this user so remove it from cache
                user.setFake(true);
                getJDA().getUsersView().remove(user.getIdLong());
            }

            GuildVoiceStateImpl voiceState = (GuildVoiceStateImpl) member.getVoiceState();
            if (voiceState != null)
            {
                VoiceChannelImpl connectedChannel = (VoiceChannelImpl) voiceState.getChannel();
                if (connectedChannel != null)
                    connectedChannel.getConnectedMembersMap().remove(member.getIdLong());
                voiceState.setConnectedChannel(null);
            }

            return false;
        }
        else if (guild.getMemberById(member.getIdLong()) != null)
        {
            // Member should be added to cache but already is cached -> do nothing
            return true;
        }

        LOG.trace("Loading member {}", member);

        if (getJDA().getUserById(user.getIdLong()) == null)
        {
            SnowflakeCacheViewImpl<User> usersView = getJDA().getUsersView();
            try (UnlockHook hook1 = usersView.writeLock())
            {
                usersView.getMap().put(user.getIdLong(), user);
            }
        }

        try (UnlockHook hook = membersView.writeLock())
        {
            membersView.getMap().put(member.getIdLong(), member);
            if (member.isOwner())
                guild.setOwner(member);
        }

        long hashId = guild.getIdLong() ^ user.getIdLong();
        getJDA().getEventCache().playbackCache(EventCache.Type.USER, member.getIdLong());
        getJDA().getEventCache().playbackCache(EventCache.Type.MEMBER, hashId);
        return true;
    }

    public MemberImpl createMember(GuildImpl guild, DataObject memberJson)
    {
        return createMember(guild, memberJson, null, null);
    }

    public MemberImpl createMember(GuildImpl guild, DataObject memberJson, DataObject voiceStateJson, DataObject presence)
    {
        boolean playbackCache = false;
        User user = createUser(memberJson.getObject("user"));
        DataArray roleArray = memberJson.getArray("roles");
        MemberImpl member = (MemberImpl) guild.getMember(user);
        if (member == null)
        {
            // Create a brand new member
            member = new MemberImpl(guild, user);
            member.setNickname(memberJson.getString("nick", null));
            long epoch = 0;
            if (!memberJson.isNull("premium_since"))
            {
                TemporalAccessor date = DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(memberJson.getString("premium_since"));
                epoch = Instant.from(date).toEpochMilli();
            }
            member.setBoostDate(epoch);
            Set<Role> roles = member.getRoleSet();
            for (int i = 0; i < roleArray.length(); i++)
            {
                long roleId = roleArray.getUnsignedLong(i);
                Role role = guild.getRoleById(roleId);
                if (role != null)
                    roles.add(role);
            }
        }
        else
        {
            // Update cached member and fire events
            List<Role> roles = new ArrayList<>(roleArray.length());
            for (int i = 0; i < roleArray.length(); i++)
            {
                long roleId = roleArray.getUnsignedLong(i);
                Role role = guild.getRoleById(roleId);
                if (role != null)
                    roles.add(role);
            }
            updateMember(guild, member, memberJson, roles);
        }

        // Load joined_at if necessary
        if (!memberJson.isNull("joined_at") && !member.hasTimeJoined())
        {
            String joinedAtRaw = memberJson.getString("joined_at");
            TemporalAccessor joinedAt = DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(joinedAtRaw);
            long joinEpoch = Instant.from(joinedAt).toEpochMilli();
            member.setJoinDate(joinEpoch);
        }

        // Load voice state and presence if necessary
        if (voiceStateJson != null && member.getVoiceState() != null)
            createVoiceState(guild, voiceStateJson, user, member);
        if (presence != null)
            createPresence(member, presence);
        return member;
    }

    private void createVoiceState(GuildImpl guild, DataObject voiceStateJson, User user, MemberImpl member)
    {
        GuildVoiceStateImpl voiceState = (GuildVoiceStateImpl) member.getVoiceState();

        final long channelId = voiceStateJson.getLong("channel_id");
        VoiceChannelImpl voiceChannel = (VoiceChannelImpl) guild.getVoiceChannelsView().get(channelId);
        if (voiceChannel != null)
            voiceChannel.getConnectedMembersMap().put(member.getIdLong(), member);
        else
            LOG.error("Received a GuildVoiceState with a channel ID for a non-existent channel! ChannelId: {} GuildId: {} UserId: {}",
                      channelId, guild.getId(), user.getId());

        // VoiceState is considered volatile so we don't expect anything to actually exist
        voiceState.setSelfMuted(voiceStateJson.getBoolean("self_mute"))
                  .setSelfDeafened(voiceStateJson.getBoolean("self_deaf"))
                  .setGuildMuted(voiceStateJson.getBoolean("mute"))
                  .setGuildDeafened(voiceStateJson.getBoolean("deaf"))
                  .setSuppressed(voiceStateJson.getBoolean("suppress"))
                  .setSessionId(voiceStateJson.getString("session_id"))
                  .setStream(voiceStateJson.getBoolean("self_stream"))
                  .setConnectedChannel(voiceChannel);
    }

    public void updateMember(GuildImpl guild, MemberImpl member, DataObject content, List<Role> newRoles)
    {
        //If newRoles is null that means that we didn't find a role that was in the array and was cached this event
        long responseNumber = getJDA().getResponseTotal();
        if (newRoles != null)
        {
            updateMemberRoles(member, newRoles, responseNumber);
        }

        if (content.hasKey("nick"))
        {
            String oldNick = member.getNickname();
            String newNick = content.getString("nick", null);
            if (!Objects.equals(oldNick, newNick))
            {
                member.setNickname(newNick);
                getJDA().handleEvent(
                    new GuildMemberUpdateNicknameEvent(
                        getJDA(), responseNumber,
                        member, oldNick));
            }
        }
        if (content.hasKey("premium_since"))
        {
            long epoch = 0;
            if (!content.isNull("premium_since"))
            {
                TemporalAccessor date = DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(content.getString("premium_since"));
                epoch = Instant.from(date).toEpochMilli();
            }
            if (epoch != member.getBoostDateRaw())
            {
                OffsetDateTime oldTime = member.getTimeBoosted();
                member.setBoostDate(epoch);
                getJDA().handleEvent(
                    new GuildMemberUpdateBoostTimeEvent(
                        getJDA(), responseNumber,
                        member, oldTime));
            }
        }

        if (!content.isNull("joined_at") && !member.hasTimeJoined())
        {
            String joinedAtRaw = content.getString("joined_at");
            TemporalAccessor joinedAt = DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(joinedAtRaw);
            long joinEpoch = Instant.from(joinedAt).toEpochMilli();
            member.setJoinDate(joinEpoch);
        }

        updateUser((UserImpl) member.getUser(), content.getObject("user"));
    }

    private void updateMemberRoles(MemberImpl member, List<Role> newRoles, long responseNumber)
    {
        Set<Role> currentRoles = member.getRoleSet();
        //Find the roles removed.
        List<Role> removedRoles = new LinkedList<>();
        each:
        for (Role role : currentRoles)
        {
            for (Iterator<Role> it = newRoles.iterator(); it.hasNext(); )
            {
                Role r = it.next();
                if (role.equals(r))
                {
                    it.remove();
                    continue each;
                }
            }
            removedRoles.add(role);
        }

        if (removedRoles.size() > 0)
            currentRoles.removeAll(removedRoles);
        if (newRoles.size() > 0)
            currentRoles.addAll(newRoles);

        if (removedRoles.size() > 0)
        {
            getJDA().handleEvent(
                new GuildMemberRoleRemoveEvent(
                    getJDA(), responseNumber,
                    member, removedRoles));
        }
        if (newRoles.size() > 0)
        {
            getJDA().handleEvent(
                new GuildMemberRoleAddEvent(
                    getJDA(), responseNumber,
                    member, newRoles));
        }
    }

    public void createPresence(MemberImpl member, DataObject presenceJson)
    {
        if (member == null)
            throw new NullPointerException("Provided member was null!");
        boolean cacheGame = getJDA().isCacheFlagSet(CacheFlag.ACTIVITY);
        boolean cacheStatus = getJDA().isCacheFlagSet(CacheFlag.CLIENT_STATUS);

        DataArray activityArray = !cacheGame || presenceJson.isNull("activities") ? null : presenceJson.getArray("activities");
        DataObject clientStatusJson = !cacheStatus || presenceJson.isNull("client_status") ? null : presenceJson.getObject("client_status");
        OnlineStatus onlineStatus = OnlineStatus.fromKey(presenceJson.getString("status"));
        List<Activity> activities = new ArrayList<>();
        boolean parsedActivity = false;

        if (cacheGame && activityArray != null)
        {
            for (int i = 0; i < activityArray.length(); i++)
            {
                try
                {
                    activities.add(createActivity(activityArray.getObject(i)));
                    parsedActivity = true;
                }
                catch (Exception ex)
                {
                    String userId;
                    userId = member.getUser().getId();
                    if (LOG.isDebugEnabled())
                        LOG.warn("Encountered exception trying to parse a presence! UserId: {} JSON: {}", userId, activityArray, ex);
                    else
                        LOG.warn("Encountered exception trying to parse a presence! UserId: {} Message: {} Enable debug for details", userId, ex.getMessage());
                }
            }
        }
        if (cacheGame && parsedActivity)
            member.setActivities(activities);
        member.setOnlineStatus(onlineStatus);
        if (clientStatusJson != null)
        {
            for (String key : clientStatusJson.keys())
            {
                ClientType type = ClientType.fromKey(key);
                OnlineStatus status = OnlineStatus.fromKey(clientStatusJson.getString(key));
                member.setOnlineStatus(type, status);
            }
        }
    }

    public static Activity createActivity(DataObject gameJson)
    {
        String name = String.valueOf(gameJson.get("name"));
        String url = gameJson.isNull("url") ? null : String.valueOf(gameJson.get("url"));
        Activity.ActivityType type;
        try
        {
            type = gameJson.isNull("type")
                ? Activity.ActivityType.DEFAULT
                : Activity.ActivityType.fromKey(Integer.parseInt(gameJson.get("type").toString()));
        }
        catch (NumberFormatException e)
        {
            type = Activity.ActivityType.DEFAULT;
        }

        RichPresence.Timestamps timestamps = null;
        if (!gameJson.isNull("timestamps"))
        {
            DataObject obj = gameJson.getObject("timestamps");
            long start, end;
            start = obj.getLong("start", 0L);
            end = obj.getLong("end", 0L);
            timestamps = new RichPresence.Timestamps(start, end);
        }

        Activity.Emoji emoji = null;
        if (!gameJson.isNull("emoji"))
        {
            DataObject emojiJson = gameJson.getObject("emoji");
            String emojiName = emojiJson.getString("name");
            long emojiId = emojiJson.getUnsignedLong("id", 0);
            boolean emojiAnimated = emojiJson.getBoolean("animated");
            emoji = new Activity.Emoji(emojiName, emojiId, emojiAnimated);
        }

        if (type == Activity.ActivityType.CUSTOM_STATUS)
        {
            if (gameJson.hasKey("state") && name.equalsIgnoreCase("Custom Status"))
            {
                name = gameJson.getString("state", "");
                gameJson = gameJson.remove("state");
            }
        }

        if (!CollectionUtils.containsAny(gameJson.keys(), richGameFields))
            return new ActivityImpl(name, url, type, timestamps, emoji);

        // data for spotify
        long id = gameJson.getLong("application_id", 0L);
        String sessionId = gameJson.getString("session_id", null);
        String syncId = gameJson.getString("sync_id", null);
        int flags = gameJson.getInt("flags", 0);
        String details = gameJson.isNull("details") ? null : String.valueOf(gameJson.get("details"));
        String state = gameJson.isNull("state") ? null : String.valueOf(gameJson.get("state"));

        RichPresence.Party party = null;
        if (!gameJson.isNull("party"))
        {
            DataObject obj = gameJson.getObject("party");
            String partyId = obj.isNull("id") ? null : obj.getString("id");
            DataArray sizeArr = obj.isNull("size") ? null : obj.getArray("size");
            long size = 0, max = 0;
            if (sizeArr != null && sizeArr.length() > 0)
            {
                size = sizeArr.getLong(0);
                max = sizeArr.length() < 2 ? 0 : sizeArr.getLong(1);
            }
            party = new RichPresence.Party(partyId, size, max);
        }

        String smallImageKey = null, smallImageText = null;
        String largeImageKey = null, largeImageText = null;
        if (!gameJson.isNull("assets"))
        {
            DataObject assets = gameJson.getObject("assets");
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

        return new RichPresenceImpl(type, name, url,
            id, emoji, party, details, state, timestamps, syncId, sessionId, flags,
            largeImageKey, largeImageText, smallImageKey, smallImageText);
    }

    public EmoteImpl createEmote(GuildImpl guildObj, DataObject json, boolean fake)
    {
        DataArray emoteRoles = json.optArray("roles").orElseGet(DataArray::empty);
        final long emoteId = json.getLong("id");
        final User user = json.isNull("user") ? null : createUser(json.getObject("user"));
        EmoteImpl emoteObj = (EmoteImpl) guildObj.getEmoteById(emoteId);
        if (emoteObj == null)
            emoteObj = new EmoteImpl(emoteId, guildObj, fake);
        Set<Role> roleSet = emoteObj.getRoleSet();

        roleSet.clear();
        for (int j = 0; j < emoteRoles.length(); j++)
        {
            Role role = guildObj.getRoleById(emoteRoles.getString(j));
            if (role != null)
                roleSet.add(role);
        }
        if (user != null)
            emoteObj.setUser(user);
        return emoteObj
                .setName(json.getString("name", ""))
                .setAnimated(json.getBoolean("animated"))
                .setManaged(json.getBoolean("managed"))
                .setAvailable(json.getBoolean("available", true));
    }

    public Category createCategory(DataObject json, long guildId)
    {
        return createCategory(null, json, guildId);
    }

    public Category createCategory(GuildImpl guild, DataObject json, long guildId)
    {
        boolean playbackCache = false;
        final long id = json.getLong("id");
        CategoryImpl channel = (CategoryImpl) getJDA().getCategoriesView().get(id);
        if (channel == null)
        {
            if (guild == null)
                guild = (GuildImpl) getJDA().getGuildsView().get(guildId);
            SnowflakeCacheViewImpl<Category>
                    guildCategoryView = guild.getCategoriesView(),
                    categoryView = getJDA().getCategoriesView();
            try (
                UnlockHook glock = guildCategoryView.writeLock();
                UnlockHook jlock = categoryView.writeLock())
            {
                channel = new CategoryImpl(id, guild);
                guildCategoryView.getMap().put(id, channel);
                playbackCache = categoryView.getMap().put(id, channel) == null;
            }
        }

        channel
            .setName(json.getString("name"))
            .setPosition(json.getInt("position"));

        createOverridesPass(channel, json.getArray("permission_overwrites"));
        if (playbackCache)
            getJDA().getEventCache().playbackCache(EventCache.Type.CHANNEL, id);
        return channel;
    }

    public StoreChannel createStoreChannel(DataObject json, long guildId)
    {
        return createStoreChannel(null, json, guildId);
    }

    public StoreChannel createStoreChannel(GuildImpl guild, DataObject json, long guildId)
    {
        boolean playbackCache = false;
        final long id = json.getLong("id");
        StoreChannelImpl channel = (StoreChannelImpl) getJDA().getStoreChannelsView().get(id);
        if (channel == null)
        {
            if (guild == null)
                guild = (GuildImpl) getJDA().getGuildById(guildId);
            SnowflakeCacheViewImpl<StoreChannel>
                    guildStoreView = guild.getStoreChannelView(),
                    storeView = getJDA().getStoreChannelsView();
            try (
                UnlockHook glock = guildStoreView.writeLock();
                UnlockHook jlock = storeView.writeLock())
            {
                channel = new StoreChannelImpl(id, guild);
                guildStoreView.getMap().put(id, channel);
                playbackCache = storeView.getMap().put(id, channel) == null;
            }
        }

        channel
            .setParent(json.getLong("parent_id", 0))
            .setName(json.getString("name"))
            .setPosition(json.getInt("position"));

        createOverridesPass(channel, json.getArray("permission_overwrites"));
        if (playbackCache)
            getJDA().getEventCache().playbackCache(EventCache.Type.CHANNEL, id);
        return channel;
    }

    public TextChannel createTextChannel(DataObject json, long guildId)
    {
        return createTextChannel(null, json, guildId);

    }

    public TextChannel createTextChannel(GuildImpl guildObj, DataObject json, long guildId)
    {
        boolean playbackCache = false;
        final long id = json.getLong("id");
        TextChannelImpl channel = (TextChannelImpl) getJDA().getTextChannelsView().get(id);
        if (channel == null)
        {
            if (guildObj == null)
                guildObj = (GuildImpl) getJDA().getGuildsView().get(guildId);
            SnowflakeCacheViewImpl<TextChannel>
                    guildTextView = guildObj.getTextChannelsView(),
                    textView = getJDA().getTextChannelsView();
            try (
                UnlockHook glock = guildTextView.writeLock();
                UnlockHook jlock = textView.writeLock())
            {
                channel = new TextChannelImpl(id, guildObj);
                guildTextView.getMap().put(id, channel);
                playbackCache = textView.getMap().put(id, channel) == null;
            }
        }

        channel
            .setParent(json.getLong("parent_id", 0))
            .setLastMessageId(json.getLong("last_message_id", 0))
            .setName(json.getString("name"))
            .setTopic(json.getString("topic", null))
            .setPosition(json.getInt("position"))
            .setNSFW(json.getBoolean("nsfw"))
            .setNews(json.getInt("type") == 5)
            .setSlowmode(json.getInt("rate_limit_per_user", 0));

        createOverridesPass(channel, json.getArray("permission_overwrites"));
        if (playbackCache)
            getJDA().getEventCache().playbackCache(EventCache.Type.CHANNEL, id);
        return channel;
    }

    public VoiceChannel createVoiceChannel(DataObject json, long guildId)
    {
        return createVoiceChannel(null, json, guildId);
    }

    public VoiceChannel createVoiceChannel(GuildImpl guild, DataObject json, long guildId)
    {
        boolean playbackCache = false;
        final long id = json.getLong("id");
        VoiceChannelImpl channel = ((VoiceChannelImpl) getJDA().getVoiceChannelsView().get(id));
        if (channel == null)
        {
            if (guild == null)
                guild = (GuildImpl) getJDA().getGuildsView().get(guildId);
            SnowflakeCacheViewImpl<VoiceChannel>
                    guildVoiceView = guild.getVoiceChannelsView(),
                    voiceView = getJDA().getVoiceChannelsView();
            try (
                UnlockHook vlock = guildVoiceView.writeLock();
                UnlockHook jlock = voiceView.writeLock())
            {
                channel = new VoiceChannelImpl(id, guild);
                guildVoiceView.getMap().put(id, channel);
                playbackCache = voiceView.getMap().put(id, channel) == null;
            }
        }

        channel
            .setParent(json.getLong("parent_id", 0))
            .setName(json.getString("name"))
            .setPosition(json.getInt("position"))
            .setUserLimit(json.getInt("user_limit"))
            .setBitrate(json.getInt("bitrate"));

        createOverridesPass(channel, json.getArray("permission_overwrites"));
        if (playbackCache)
            getJDA().getEventCache().playbackCache(EventCache.Type.CHANNEL, id);
        return channel;
    }

    public PrivateChannel createPrivateChannel(DataObject json)
    {
        return createPrivateChannel(json, false);
    }

    public PrivateChannel createPrivateChannel(DataObject json, boolean modifyCache)
    {
        final long channelId = json.getUnsignedLong("id");
        PrivateChannel channel = api.getPrivateChannelById(channelId);
        if (channel != null)
            return channel;

        DataObject recipient = json.hasKey("recipients") ?
            json.getArray("recipients").getObject(0) :
            json.getObject("recipient");
        final long userId = recipient.getLong("id");
        UserImpl user = (UserImpl) getJDA().getUserById(userId);
        if (user == null)
        {   //The getJDA() can give us private channels connected to Users that we can no longer communicate with.
            // As such, make a fake user and fake private channel.
            user = createUser(recipient);
        }

        return createPrivateChannel(json, user, modifyCache);
    }

    public PrivateChannel createPrivateChannel(DataObject json, UserImpl user)
    {
        return createPrivateChannel(json, user, false);
    }

    public PrivateChannel createPrivateChannel(DataObject json, UserImpl user, boolean modifyCache)
    {
        final long channelId = json.getLong("id");
        PrivateChannelImpl priv = new PrivateChannelImpl(channelId, user)
                .setLastMessageId(json.getLong("last_message_id", 0));
        user.setPrivateChannel(priv);

        if (modifyCache)
        {
            // only add channels to cache when they come from an event, otherwise we would never remove the channel
            SnowflakeCacheViewImpl<PrivateChannel> privateView = getJDA().getPrivateChannelsView();
            try (UnlockHook hook = privateView.writeLock())
            {
                privateView.getMap().put(channelId, priv);
            }
            getJDA().getEventCache().playbackCache(EventCache.Type.CHANNEL, channelId);
        }
        return priv;
    }

    public void createOverridesPass(AbstractChannelImpl<?,?> channel, DataArray overrides)
    {
        for (int i = 0; i < overrides.length(); i++)
        {
            try
            {
                createPermissionOverride(overrides.getObject(i), channel);
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

    public Role createRole(GuildImpl guild, DataObject roleJson, long guildId)
    {
        boolean playbackCache = false;
        final long id = roleJson.getLong("id");
        if (guild == null)
            guild = (GuildImpl) getJDA().getGuildsView().get(guildId);
        RoleImpl role = (RoleImpl) guild.getRolesView().get(id);
        if (role == null)
        {
            SnowflakeCacheViewImpl<Role> roleView = guild.getRolesView();
            try (UnlockHook hook = roleView.writeLock())
            {
                role = new RoleImpl(id, guild);
                playbackCache = roleView.getMap().put(id, role) == null;
            }
        }
        final int color = roleJson.getInt("color");
        role.setName(roleJson.getString("name"))
            .setRawPosition(roleJson.getInt("position"))
            .setRawPermissions(roleJson.getLong("permissions_new"))
            .setManaged(roleJson.getBoolean("managed"))
            .setHoisted(roleJson.getBoolean("hoist"))
            .setColor(color == 0 ? Role.DEFAULT_COLOR_RAW : color)
            .setMentionable(roleJson.getBoolean("mentionable"))
            .setTags(roleJson.optObject("tags").orElseGet(DataObject::empty));
        if (playbackCache)
            getJDA().getEventCache().playbackCache(EventCache.Type.ROLE, id);
        return role;
    }

    public Message createMessage(DataObject jsonObject) { return createMessage(jsonObject, false); }
    public Message createMessage(DataObject jsonObject, boolean modifyCache)
    {
        final long channelId = jsonObject.getLong("channel_id");

        MessageChannel chan = getJDA().getTextChannelById(channelId);
        if (chan == null)
            chan = getJDA().getPrivateChannelById(channelId);
        if (chan == null)
            throw new IllegalArgumentException(MISSING_CHANNEL);

        return createMessage(jsonObject, chan, modifyCache);
    }
    public Message createMessage(DataObject jsonObject, MessageChannel chan, boolean modifyCache)
    {
        final long id = jsonObject.getLong("id");
        final DataObject author = jsonObject.getObject("author");
        final long authorId = author.getLong("id");
        MemberImpl member = null;

        if (chan.getType().isGuild() && !jsonObject.isNull("member"))
        {
            DataObject memberJson = jsonObject.getObject("member");
            memberJson.put("user", author);
            GuildChannel guildChannel = (GuildChannel) chan;
            Guild guild = guildChannel.getGuild();
            member = createMember((GuildImpl) guild, memberJson);
            if (modifyCache)
            {
                // Update member cache with new information if needed
                updateMemberCache(member);
            }
        }

        final String content = jsonObject.getString("content", "");
        final boolean fromWebhook = jsonObject.hasKey("webhook_id");
        final boolean pinned = jsonObject.getBoolean("pinned");
        final boolean tts = jsonObject.getBoolean("tts");
        final boolean mentionsEveryone = jsonObject.getBoolean("mention_everyone");
        final OffsetDateTime editTime = jsonObject.isNull("edited_timestamp") ? null : OffsetDateTime.parse(jsonObject.getString("edited_timestamp"));
        final String nonce = jsonObject.isNull("nonce") ? null : jsonObject.get("nonce").toString();
        final int flags = jsonObject.getInt("flags", 0);

        final List<Message.Attachment> attachments = map(jsonObject, "attachments", this::createMessageAttachment);
        final List<MessageEmbed>       embeds      = map(jsonObject, "embeds",      this::createMessageEmbed);
        final List<MessageReaction>    reactions   = map(jsonObject, "reactions",   (obj) -> createMessageReaction(chan, id, obj));

        MessageActivity activity = null;

        if (!jsonObject.isNull("activity"))
            activity = createMessageActivity(jsonObject);

        User user;
        switch (chan.getType())
        {
            case PRIVATE:
                if (authorId == getJDA().getSelfUser().getIdLong())
                    user = getJDA().getSelfUser();
                else
                    user = ((PrivateChannel) chan).getUser();
                break;
            case GROUP:
                throw new IllegalStateException("Cannot build a message for a group channel, how did this even get here?");
            case TEXT:
                Guild guild = ((TextChannel) chan).getGuild();
                if (member == null)
                    member = (MemberImpl) guild.getMemberById(authorId);
                user = member != null ? member.getUser() : null;
                if (user == null)
                {
                    if (fromWebhook || !modifyCache)
                        user = createUser(author);
                    else
                        throw new IllegalArgumentException(MISSING_USER); // Specifically for MESSAGE_CREATE
                }
                break;
            default: throw new IllegalArgumentException("Invalid Channel for creating a Message [" + chan.getType() + ']');
        }

        if (modifyCache && !fromWebhook) // update the user information on message receive
            updateUser((UserImpl) user, author);

        TLongSet mentionedRoles = new TLongHashSet();
        TLongSet mentionedUsers = new TLongHashSet(map(jsonObject, "mentions", (o) -> o.getLong("id")));
        Optional<DataArray> roleMentionArr = jsonObject.optArray("mention_roles");
        roleMentionArr.ifPresent((arr) ->
        {
            for (int i = 0; i < arr.length(); i++)
                mentionedRoles.add(arr.getLong(i));
        });

        MessageType type = MessageType.fromId(jsonObject.getInt("type"));
        ReceivedMessage message;
        Message referencedMessage = null;
        if (!jsonObject.isNull("referenced_message"))
        {
            referencedMessage = createMessage(jsonObject.getObject("referenced_message"), chan, false);
            if (type == MessageType.DEFAULT)
                type = MessageType.INLINE_REPLY;
        }
        switch (type)
        {
            case INLINE_REPLY:
            case DEFAULT:
                message = new ReceivedMessage(id, chan, type, referencedMessage, fromWebhook,
                    mentionsEveryone, mentionedUsers, mentionedRoles, tts, pinned,
                    content, nonce, user, member, activity, editTime, reactions, attachments, embeds, flags);
                break;
            case UNKNOWN:
                throw new IllegalArgumentException(UNKNOWN_MESSAGE_TYPE);
            default:
                message = new SystemMessage(id, chan, type, fromWebhook,
                    mentionsEveryone, mentionedUsers, mentionedRoles, tts, pinned,
                    content, nonce, user, member, activity, editTime, reactions, attachments, embeds, flags);
                break;
        }

        GuildImpl guild = message.isFromGuild() ? (GuildImpl) message.getGuild() : null;

        // Load users/members from message object through mentions
        List<User> mentionedUsersList = new ArrayList<>();
        List<Member> mentionedMembersList = new ArrayList<>();
        DataArray userMentions = jsonObject.getArray("mentions");

        for (int i = 0; i < userMentions.length(); i++)
        {
            DataObject mentionJson = userMentions.getObject(i);
            if (guild == null || mentionJson.isNull("member"))
            {
                // Can't load user without member context so fake them if possible
                User mentionedUser = createUser(mentionJson);
                mentionedUsersList.add(mentionedUser);
                if (guild != null)
                {
                    Member mentionedMember = guild.getMember(mentionedUser);
                    if (mentionedMember != null)
                        mentionedMembersList.add(mentionedMember);
                }
                continue;
            }

            // Load member/user from mention (gateway messages only)
            DataObject memberJson = mentionJson.getObject("member");
            mentionJson.remove("member");
            memberJson.put("user", mentionJson);
            Member mentionedMember = createMember(guild, memberJson);
            mentionedMembersList.add(mentionedMember);
            mentionedUsersList.add(mentionedMember.getUser());
        }

        message.setMentions(mentionedUsersList, mentionedMembersList);
        return message;
    }

    private static MessageActivity createMessageActivity(DataObject jsonObject)
    {
        DataObject activityData = jsonObject.getObject("activity");
        final MessageActivity.ActivityType activityType = MessageActivity.ActivityType.fromId(activityData.getInt("type"));
        final String partyId = activityData.getString("party_id", null);
        MessageActivity.Application application = null;

        if (!jsonObject.isNull("application"))
        {
            DataObject applicationData = jsonObject.getObject("application");

            final String name = applicationData.getString("name");
            final String description = applicationData.getString("description", "");
            final String iconId = applicationData.getString("icon", null);
            final String coverId = applicationData.getString("cover_image", null);
            final long applicationId = applicationData.getLong("id");

            application = new MessageActivity.Application(name, description, iconId, coverId, applicationId);
        }
        if (activityType == MessageActivity.ActivityType.UNKNOWN)
        {
            LOG.debug("Received an unknown ActivityType, Activity: {}", activityData);
        }

        return new MessageActivity(activityType, partyId, application);
    }

    public MessageReaction createMessageReaction(MessageChannel chan, long id, DataObject obj)
    {
        DataObject emoji = obj.getObject("emoji");
        final Long emojiID = emoji.isNull("id") ? null : emoji.getLong("id");
        final String name = emoji.getString("name", "");
        final boolean animated = emoji.getBoolean("animated");
        final int count = obj.getInt("count", -1);
        final boolean me = obj.getBoolean("me");

        final MessageReaction.ReactionEmote reactionEmote;
        if (emojiID != null)
        {
            Emote emote = getJDA().getEmoteById(emojiID);
            // creates fake emoji because no guild has this emoji id
            if (emote == null)
                emote = new EmoteImpl(emojiID, getJDA()).setAnimated(animated).setName(name);
            reactionEmote = MessageReaction.ReactionEmote.fromCustom(emote);
        }
        else
        {
            reactionEmote = MessageReaction.ReactionEmote.fromUnicode(name, getJDA());
        }

        return new MessageReaction(chan, reactionEmote, id, me, count);
    }

    public Message.Attachment createMessageAttachment(DataObject jsonObject)
    {
        final int width = jsonObject.getInt("width", -1);
        final int height = jsonObject.getInt("height", -1);
        final int size = jsonObject.getInt("size");
        final String url = jsonObject.getString("url");
        final String proxyUrl = jsonObject.getString("proxy_url");
        final String filename = jsonObject.getString("filename");
        final long id = jsonObject.getLong("id");
        return new Message.Attachment(id, url, proxyUrl, filename, size, height, width, getJDA());
    }

    public MessageEmbed createMessageEmbed(DataObject content)
    {
        if (content.isNull("type"))
            throw new IllegalStateException("Encountered embed object with missing/null type field for Json: " + content);
        EmbedType type = EmbedType.fromKey(content.getString("type"));
        final String url = content.getString("url", null);
        final String title = content.getString("title", null);
        final String description = content.getString("description", null);
        final OffsetDateTime timestamp = content.isNull("timestamp") ? null : OffsetDateTime.parse(content.getString("timestamp"));
        final int color = content.isNull("color") ? Role.DEFAULT_COLOR_RAW : content.getInt("color");

        final Thumbnail thumbnail;
        if (content.isNull("thumbnail"))
        {
            thumbnail = null;
        }
        else
        {
            DataObject obj = content.getObject("thumbnail");
            thumbnail = new Thumbnail(obj.getString("url", null),
                                      obj.getString("proxy_url", null),
                                      obj.getInt("width", -1),
                                      obj.getInt("height", -1));
        }

        final Provider provider;
        if (content.isNull("provider"))
        {
            provider = null;
        }
        else
        {
            DataObject obj = content.getObject("provider");
            provider = new Provider(obj.getString("name", null),
                                    obj.getString("url", null));
        }

        final AuthorInfo author;
        if (content.isNull("author"))
        {
            author = null;
        }
        else
        {
            DataObject obj = content.getObject("author");
            author = new AuthorInfo(obj.getString("name", null),
                                    obj.getString("url", null),
                                    obj.getString("icon_url", null),
                                    obj.getString("proxy_icon_url", null));
        }

        final VideoInfo video;
        if (content.isNull("video"))
        {
            video = null;
        }
        else
        {
            DataObject obj = content.getObject("video");
            video = new VideoInfo(obj.getString("url", null),
                                  obj.getInt("width", -1),
                                  obj.getInt("height", -1));
        }

        final Footer footer;
        if (content.isNull("footer"))
        {
            footer = null;
        }
        else
        {
            DataObject obj = content.getObject("footer");
            footer = new Footer(obj.getString("text", null),
                                obj.getString("icon_url", null),
                                obj.getString("proxy_icon_url", null));
        }

        final ImageInfo image;
        if (content.isNull("image"))
        {
            image = null;
        }
        else
        {
            DataObject obj = content.getObject("image");
            image = new ImageInfo(obj.getString("url", null),
                                  obj.getString("proxy_url", null),
                                  obj.getInt("width", -1),
                                  obj.getInt("height", -1));
        }

        final List<Field> fields = map(content, "fields", (obj) ->
            new Field(obj.getString("name", null),
                      obj.getString("value", null),
                      obj.getBoolean("inline"),
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

    @Nullable
    public PermissionOverride createPermissionOverride(DataObject override, AbstractChannelImpl<?, ?> chan)
    {
        String type = override.getString("type");
        final long id = override.getLong("id");
        boolean role = type.equals("role");
        if (role && chan.getGuild().getRoleById(id) == null)
            throw new NoSuchElementException("Attempted to create a PermissionOverride for a non-existent role! JSON: " + override);
        if (!role && !type.equals("member"))
            throw new IllegalArgumentException("Provided with an unknown PermissionOverride type! JSON: " + override);
        if (!role && id != api.getSelfUser().getIdLong() && !api.isCacheFlagSet(CacheFlag.MEMBER_OVERRIDES))
            return null;

        long allow = override.getLong("allow_new");
        long deny = override.getLong("deny_new");

        PermissionOverrideImpl permOverride = (PermissionOverrideImpl) chan.getOverrideMap().get(id);
        if (permOverride == null)
        {
            permOverride = new PermissionOverrideImpl(chan, id, role);
            chan.getOverrideMap().put(id, permOverride);
        }

        return permOverride.setAllow(allow).setDeny(deny);
    }

    public WebhookImpl createWebhook(DataObject object)
    {
        final long id = object.getLong("id");
        final long guildId = object.getUnsignedLong("guild_id");
        final long channelId = object.getUnsignedLong("channel_id");
        final String token = object.getString("token", null);
        final WebhookType type = WebhookType.fromKey(object.getInt("type", -1));

        TextChannel channel = getJDA().getTextChannelById(channelId);
        if (channel == null)
            throw new NullPointerException(String.format("Tried to create Webhook for an un-cached TextChannel! WebhookId: %s ChannelId: %s GuildId: %s",
                    id, channelId, guildId));

        Object name = !object.isNull("name") ? object.get("name") : null;
        Object avatar = !object.isNull("avatar") ? object.get("avatar") : null;

        DataObject fakeUser = DataObject.empty()
                    .put("username", name)
                    .put("discriminator", "0000")
                    .put("id", id)
                    .put("avatar", avatar);
        User defaultUser = createUser(fakeUser);

        Optional<DataObject> ownerJson = object.optObject("user");
        User owner = null;
        
        if (ownerJson.isPresent())
        {
            DataObject json = ownerJson.get();
            final long userId = json.getLong("id");

            owner = getJDA().getUserById(userId);
            if (owner == null)
            {
                json.put("id", userId);
                owner = createUser(json);
            }
        }
        
        return new WebhookImpl(channel, id, type)
                .setToken(token)
                .setOwner(owner == null ? null : channel.getGuild().getMember(owner))
                .setUser(defaultUser);
    }

    public Invite createInvite(DataObject object)
    {
        final String code = object.getString("code");
        final User inviter = object.hasKey("inviter") ? createUser(object.getObject("inviter")) : null;

        final DataObject channelObject = object.getObject("channel");
        final ChannelType channelType = ChannelType.fromId(channelObject.getInt("type"));

        final Invite.InviteType type;
        final Invite.Guild guild;
        final Invite.Channel channel;
        final Invite.Group group;

        if (channelType == ChannelType.GROUP)
        {
            type = Invite.InviteType.GROUP;
            guild = null;
            channel = null;

            final String groupName = channelObject.getString("name", "");
            final long groupId = channelObject.getLong("id");
            final String groupIconId = channelObject.getString("icon", null);

            final List<String> usernames;
            if (channelObject.isNull("recipients"))
                usernames = null;
            else
                usernames = map(channelObject, "recipients", (json) -> json.getString("username"));

            group = new InviteImpl.GroupImpl(groupIconId, groupName, groupId, usernames);
        }
        else if (channelType.isGuild())
        {
            type = Invite.InviteType.GUILD;

            final DataObject guildObject = object.getObject("guild");

            final String guildIconId = guildObject.getString("icon", null);
            final long guildId = guildObject.getLong("id");
            final String guildName = guildObject.getString("name");
            final String guildSplashId = guildObject.getString("splash", null);
            final VerificationLevel guildVerificationLevel = VerificationLevel.fromKey(guildObject.getInt("verification_level", -1));
            final int presenceCount = object.getInt("approximate_presence_count", -1);
            final int memberCount = object.getInt("approximate_member_count", -1);

            final Set<String> guildFeatures;
            if (guildObject.isNull("features"))
                guildFeatures = Collections.emptySet();
            else
                guildFeatures = Collections.unmodifiableSet(StreamSupport.stream(guildObject.getArray("features").spliterator(), false).map(String::valueOf).collect(Collectors.toSet()));

            guild = new InviteImpl.GuildImpl(guildId, guildIconId, guildName, guildSplashId, guildVerificationLevel, presenceCount, memberCount, guildFeatures);

            final String channelName = channelObject.getString("name");
            final long channelId = channelObject.getLong("id");

            channel = new InviteImpl.ChannelImpl(channelId, channelName, channelType);
            group = null;
        }
        else
        {
            // Unknown channel type for invites

            type = Invite.InviteType.UNKNOWN;
            guild = null;
            channel = null;
            group = null;
        }

        final int maxAge;
        final int maxUses;
        final boolean temporary;
        final OffsetDateTime timeCreated;
        final int uses;
        final boolean expanded;

        if (object.hasKey("max_uses"))
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

        return new InviteImpl(getJDA(), code, expanded, inviter,
                              maxAge, maxUses, temporary,
                              timeCreated, uses, channel, guild, group, type);
    }

    public ApplicationInfo createApplicationInfo(DataObject object)
    {
        final String description = object.getString("description");
        final boolean doesBotRequireCodeGrant = object.getBoolean("bot_require_code_grant");
        final String iconId = object.getString("icon", null);
        final long id = object.getLong("id");
        final String name = object.getString("name");
        final boolean isBotPublic = object.getBoolean("bot_public");
        final User owner = createUser(object.getObject("owner"));
        final ApplicationTeam team = !object.isNull("team") ? createApplicationTeam(object.getObject("team")) : null;

        return new ApplicationInfoImpl(getJDA(), description, doesBotRequireCodeGrant, iconId, id, isBotPublic, name, owner, team);
    }

    public ApplicationTeam createApplicationTeam(DataObject object)
    {
        String iconId = object.getString("icon", null);
        long id = object.getUnsignedLong("id");
        long ownerId = object.getUnsignedLong("owner_user_id", 0);
        List<TeamMember> members = map(object, "members", (o) -> {
            DataObject userJson = o.getObject("user");
            TeamMember.MembershipState state = TeamMember.MembershipState.fromKey(o.getInt("membership_state"));
            User user = createUser(userJson);
            return new TeamMemberImpl(user, state, id);
        });
        return new ApplicationTeamImpl(iconId, members, id, ownerId);
    }

    public AuditLogEntry createAuditLogEntry(GuildImpl guild, DataObject entryJson, DataObject userJson, DataObject webhookJson)
    {
        final long targetId = entryJson.getLong("target_id", 0);
        final long id = entryJson.getLong("id");
        final int typeKey = entryJson.getInt("action_type");
        final DataArray changes = entryJson.isNull("changes") ? null : entryJson.getArray("changes");
        final DataObject options = entryJson.isNull("options") ? null : entryJson.getObject("options");
        final String reason = entryJson.getString("reason", null);

        final UserImpl user = userJson == null ? null : createUser(userJson);
        final WebhookImpl webhook = webhookJson == null ? null : createWebhook(webhookJson);
        final Set<AuditLogChange> changesList;
        final ActionType type = ActionType.from(typeKey);

        if (changes != null)
        {
            changesList = new HashSet<>(changes.length());
            for (int i = 0; i < changes.length(); i++)
            {
                final DataObject object = changes.getObject(i);
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

        return new AuditLogEntry(type, typeKey, id, targetId, guild, user, webhook, reason, changeMap, optionMap);
    }

    public AuditLogChange createAuditLogChange(DataObject change)
    {
        final String key = change.getString("key");
        Object oldValue = change.isNull("old_value") ? null : change.get("old_value");
        Object newValue = change.isNull("new_value") ? null : change.get("new_value");
        return new AuditLogChange(oldValue, newValue, key);
    }

    private Map<String, AuditLogChange> changeToMap(Set<AuditLogChange> changesList)
    {
        return changesList.stream().collect(Collectors.toMap(AuditLogChange::getKey, UnaryOperator.identity()));
    }

    private <T> List<T> map(DataObject jsonObject, String key, Function<DataObject, T> convert)
    {
        if (jsonObject.isNull(key))
            return Collections.emptyList();

        final DataArray arr = jsonObject.getArray(key);
        final List<T> mappedObjects = new ArrayList<>(arr.length());
        for (int i = 0; i < arr.length(); i++)
        {
            DataObject obj = arr.getObject(i);
            T result = convert.apply(obj);
            if (result != null)
                mappedObjects.add(result);
        }

        return mappedObjects;
    }
}
