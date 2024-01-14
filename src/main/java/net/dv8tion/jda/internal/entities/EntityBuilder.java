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
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogChange;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.Guild.ExplicitContentLevel;
import net.dv8tion.jda.api.entities.Guild.NotificationLevel;
import net.dv8tion.jda.api.entities.Guild.Timeout;
import net.dv8tion.jda.api.entities.Guild.VerificationLevel;
import net.dv8tion.jda.api.entities.MessageEmbed.*;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer;
import net.dv8tion.jda.api.entities.channel.attribute.IWebhookContainer;
import net.dv8tion.jda.api.entities.channel.concrete.*;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.entities.sticker.*;
import net.dv8tion.jda.api.entities.templates.Template;
import net.dv8tion.jda.api.entities.templates.TemplateChannel;
import net.dv8tion.jda.api.entities.templates.TemplateGuild;
import net.dv8tion.jda.api.entities.templates.TemplateRole;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.*;
import net.dv8tion.jda.api.events.user.update.*;
import net.dv8tion.jda.api.exceptions.ParsingException;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.cache.CacheView;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.channel.concrete.*;
import net.dv8tion.jda.internal.entities.channel.mixin.attribute.IPermissionContainerMixin;
import net.dv8tion.jda.internal.entities.channel.mixin.attribute.IPostContainerMixin;
import net.dv8tion.jda.internal.entities.channel.mixin.middleman.AudioChannelMixin;
import net.dv8tion.jda.internal.entities.emoji.CustomEmojiImpl;
import net.dv8tion.jda.internal.entities.emoji.RichCustomEmojiImpl;
import net.dv8tion.jda.internal.entities.emoji.UnicodeEmojiImpl;
import net.dv8tion.jda.internal.entities.sticker.*;
import net.dv8tion.jda.internal.handle.EventCache;
import net.dv8tion.jda.internal.utils.Helpers;
import net.dv8tion.jda.internal.utils.JDALogger;
import net.dv8tion.jda.internal.utils.UnlockHook;
import net.dv8tion.jda.internal.utils.cache.ChannelCacheViewImpl;
import net.dv8tion.jda.internal.utils.cache.MemberCacheViewImpl;
import net.dv8tion.jda.internal.utils.cache.SnowflakeCacheViewImpl;
import net.dv8tion.jda.internal.utils.cache.SortedSnowflakeCacheViewImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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

        if (!self.isNull("application_id"))
            selfUser.setApplicationId(self.getUnsignedLong("application_id"));
        selfUser.setVerified(self.getBoolean("verified"))
                .setMfaEnabled(self.getBoolean("mfa_enabled"))
                .setName(self.getString("username"))
                .setGlobalName(self.getString("global_name", null))
                .setDiscriminator(Short.parseShort(self.getString("discriminator", "0")))
                .setAvatarId(self.getString("avatar", null))
                .setBot(self.getBoolean("bot"))
                .setSystem(false);

        return selfUser;
    }

    public static Activity createActivity(String name, String url, Activity.ActivityType type)
    {
        return new ActivityImpl(name, url, type);
    }

    // Unlike Emoji.fromData this does not check for null or empty
    public static EmojiUnion createEmoji(DataObject emoji)
    {
        return createEmoji(emoji, "name", "id");
    }

    // Unlike Emoji.fromData this does not check for null or empty
    public static EmojiUnion createEmoji(DataObject emoji, String nameKey, String idKey)
    {
        long id = emoji.getUnsignedLong(idKey, 0L);
        if (id == 0L)
            return new UnicodeEmojiImpl(emoji.getString(nameKey));
        else // name can be empty in some cases where discord fails to properly load the emoji
            return new CustomEmojiImpl(emoji.getString(nameKey, ""), id, emoji.getBoolean("animated"));
    }

    private void createGuildEmojiPass(GuildImpl guildObj, DataArray array)
    {
        if (!getJDA().isCacheFlagSet(CacheFlag.EMOJI))
            return;
        SnowflakeCacheViewImpl<RichCustomEmoji> emojiView = guildObj.getEmojisView();
        try (UnlockHook hook = emojiView.writeLock())
        {
            TLongObjectMap<RichCustomEmoji> emojiMap = emojiView.getMap();
            for (int i = 0; i < array.length(); i++)
            {
                DataObject object = array.getObject(i);
                if (object.isNull("id"))
                {
                    LOG.error("Received GUILD_CREATE with an emoji with a null ID. JSON: {}", object);
                    continue;
                }
                final long emojiId = object.getLong("id");
                emojiMap.put(emojiId, createEmoji(guildObj, object));
            }
        }
    }

    private void createScheduledEventPass(GuildImpl guildObj, DataArray array)
    {
        if (!getJDA().isCacheFlagSet(CacheFlag.SCHEDULED_EVENTS))
            return;
        for (int i = 0; i < array.length(); i++)
        {
            DataObject object = array.getObject(i);
            try
            {
                if (object.isNull("id"))
                {
                    LOG.error("Received GUILD_CREATE with a scheduled event with a null ID. JSON: {}", object);
                    continue;
                }
                createScheduledEvent(guildObj, object);
            }
            catch (ParsingException exception)
            {
                LOG.error("Received GUILD_CREATE with a scheduled event that failed to parse. JSON: {}", object, exception);
            }
        }
    }

    private void createGuildStickerPass(GuildImpl guildObj, DataArray array)
    {
        if (!getJDA().isCacheFlagSet(CacheFlag.STICKER))
            return;
        SnowflakeCacheViewImpl<GuildSticker> stickerView = guildObj.getStickersView();
        try (UnlockHook hook = stickerView.writeLock())
        {
            TLongObjectMap<GuildSticker> stickerMap = stickerView.getMap();
            for (int i = 0; i < array.length(); i++)
            {
                DataObject object = array.getObject(i);
                if (object.isNull("id"))
                {
                    LOG.error("Received GUILD_CREATE with a sticker with a null ID. GuildId: {} JSON: {}",
                              guildObj.getId(), object);
                    continue;
                }
                if (object.getInt("type", -1) != Sticker.Type.GUILD.getId())
                {
                    LOG.error("Received GUILD_CREATE with sticker that had an unexpected type. GuildId: {} Type: {} JSON: {}",
                              guildObj.getId(), object.getInt("type", -1), object);
                    continue;
                }

                RichSticker sticker = createRichSticker(object);
                stickerMap.put(sticker.getIdLong(), (GuildSticker) sticker);
            }
        }
    }

    public GuildImpl createGuild(long guildId, DataObject guildJson, TLongObjectMap<DataObject> members, int memberCount)
    {
        final GuildImpl guildObj = new GuildImpl(getJDA(), guildId);
        final String name = guildJson.getString("name", "");
        final String iconId = guildJson.getString("icon", null);
        final String splashId = guildJson.getString("splash", null);
        final String description = guildJson.getString("description", null);
        final String vanityCode = guildJson.getString("vanity_url_code", null);
        final String bannerId = guildJson.getString("banner", null);
        final String locale = guildJson.getString("preferred_locale", "en-US");
        final DataArray roleArray = guildJson.getArray("roles");
        final DataArray channelArray = guildJson.getArray("channels");
        final DataArray threadArray = guildJson.getArray("threads");
        final DataArray scheduledEventsArray = guildJson.getArray("guild_scheduled_events");
        final DataArray emojisArray = guildJson.getArray("emojis");
        final DataArray stickersArray = guildJson.getArray("stickers");
        final DataArray voiceStateArray = guildJson.getArray("voice_states");
        final Optional<DataArray> featuresArray = guildJson.optArray("features");
        final Optional<DataArray> presencesArray = guildJson.optArray("presences");
        final long ownerId = guildJson.getUnsignedLong("owner_id", 0L);
        final long afkChannelId = guildJson.getUnsignedLong("afk_channel_id", 0L);
        final long systemChannelId = guildJson.getUnsignedLong("system_channel_id", 0L);
        final long rulesChannelId = guildJson.getUnsignedLong("rules_channel_id", 0L);
        final long communityUpdatesChannelId = guildJson.getUnsignedLong("public_updates_channel_id", 0L);
        final int boostCount = guildJson.getInt("premium_subscription_count", 0);
        final int boostTier = guildJson.getInt("premium_tier", 0);
        final int maxMembers = guildJson.getInt("max_members", 0);
        final int maxPresences = guildJson.getInt("max_presences", 5000);
        final int mfaLevel = guildJson.getInt("mfa_level", 0);
        final int afkTimeout = guildJson.getInt("afk_timeout", 0);
        final int verificationLevel = guildJson.getInt("verification_level", 0);
        final int notificationLevel = guildJson.getInt("default_message_notifications", 0);
        final int explicitContentLevel = guildJson.getInt("explicit_content_filter", 0);
        final int nsfwLevel = guildJson.getInt("nsfw_level", -1);
        final boolean boostProgressBarEnabled = guildJson.getBoolean("premium_progress_bar_enabled");

        guildObj.setName(name)
                .setIconId(iconId)
                .setSplashId(splashId)
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
                .setLocale(DiscordLocale.from(locale))
                .setBoostCount(boostCount)
                .setBoostTier(boostTier)
                .setMemberCount(memberCount)
                .setNSFWLevel(Guild.NSFWLevel.fromKey(nsfwLevel))
                .setBoostProgressBarEnabled(boostProgressBarEnabled);

        SnowflakeCacheViewImpl<Guild> guildView = getJDA().getGuildsView();
        try (UnlockHook hook = guildView.writeLock())
        {
            guildView.getMap().put(guildId, guildObj);
        }

        guildObj.setFeatures(featuresArray.map(array ->
            array.stream(DataArray::getString)
                 .map(String::intern) // Prevent allocating the same feature string over and over
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

        TLongObjectMap<DataObject> voiceStates = Helpers.convertToMap((o) -> o.getUnsignedLong("user_id", 0L), voiceStateArray);
        TLongObjectMap<DataObject> presences = presencesArray.map(o1 -> Helpers.convertToMap(o2 -> o2.getObject("user").getUnsignedLong("id"), o1)).orElseGet(TLongObjectHashMap::new);
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
        if (guildObj.getMember(api.getSelfUser()) == null)
        {
            LOG.error("Guild is missing a SelfMember. GuildId: {}", guildId);
            LOG.debug("Guild is missing a SelfMember. GuildId: {} JSON: \n{}", guildId, guildJson);
            // This is actually a gateway request
            guildObj.retrieveMembersByIds(api.getSelfUser().getIdLong()).onSuccess(m -> {
                if (m.isEmpty())
                    LOG.warn("Was unable to recover SelfMember for guild with id {}. This guild might be corrupted!", guildId);
                else
                    LOG.debug("Successfully recovered SelfMember for guild with id {}.", guildId);
            });
        }

        for (int i = 0; i < threadArray.length(); i++)
        {
            DataObject threadJson = threadArray.getObject(i);
            try
            {
                createThreadChannel(guildObj, threadJson, guildObj.getIdLong());
            }
            catch (Exception ex)
            {
                if (MISSING_CHANNEL.equals(ex.getMessage()))
                    LOG.debug("Discarding thread without cached parent channel. JSON: {}", threadJson);
                else
                    LOG.warn("Failed to create thread channel for guild with id {}.\nJSON: {}", guildId, threadJson, ex);
            }
        }

        createScheduledEventPass(guildObj, scheduledEventsArray);
        createGuildEmojiPass(guildObj, emojisArray);
        createGuildStickerPass(guildObj, stickersArray);
        guildJson.optArray("stage_instances")
                .map(arr -> arr.stream(DataArray::getObject))
                .ifPresent(list -> list.forEach(it -> createStageInstance(guildObj, it)));

        guildObj.setAfkChannel(guildObj.getVoiceChannelById(afkChannelId))
                .setSystemChannel(guildObj.getTextChannelById(systemChannelId))
                .setRulesChannel(guildObj.getTextChannelById(rulesChannelId))
                .setCommunityUpdatesChannel(guildObj.getTextChannelById(communityUpdatesChannelId));

        return guildObj;
    }

    public GuildChannel createGuildChannel(GuildImpl guildObj, DataObject channelData)
    {
        final ChannelType channelType = ChannelType.fromId(channelData.getInt("type"));
        switch (channelType)
        {
        case TEXT:
            return createTextChannel(guildObj, channelData, guildObj.getIdLong());
        case NEWS:
            return createNewsChannel(guildObj, channelData, guildObj.getIdLong());
        case STAGE:
            return createStageChannel(guildObj, channelData, guildObj.getIdLong());
        case VOICE:
            return createVoiceChannel(guildObj, channelData, guildObj.getIdLong());
        case CATEGORY:
            return createCategory(guildObj, channelData, guildObj.getIdLong());
        case FORUM:
            return createForumChannel(guildObj, channelData, guildObj.getIdLong());
        case MEDIA:
            return createMediaChannel(guildObj, channelData, guildObj.getIdLong());
        default:
            LOG.debug("Cannot create channel for type " + channelData.getInt("type"));
            return null;
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

        User.Profile profile = user.hasKey("banner")
            ? new User.Profile(id, user.getString("banner", null), user.getInt("accent_color", User.DEFAULT_ACCENT_COLOR_RAW))
            : null;

        if (newUser)
        {
            // Initial creation
            userObj.setName(user.getString("username"))
                   .setGlobalName(user.getString("global_name", null))
                   .setDiscriminator(Short.parseShort(user.getString("discriminator", "0")))
                   .setAvatarId(user.getString("avatar", null))
                   .setBot(user.getBoolean("bot"))
                   .setSystem(user.getBoolean("system"))
                   .setFlags(user.getInt("public_flags", 0))
                   .setProfile(profile);
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
        String oldGlobalName = userObj.getGlobalName();
        String newGlobalName = user.getString("global_name", null);
        short oldDiscriminator = userObj.getDiscriminatorInt();
        short newDiscriminator = Short.parseShort(user.getString("discriminator", "0"));
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

        if (!Objects.equals(oldGlobalName, newGlobalName))
        {
            userObj.setGlobalName(newGlobalName);
            jda.handleEvent(
                new UserUpdateGlobalNameEvent(
                    jda, responseNumber,
                    userObj, oldGlobalName));
        }

        if (oldDiscriminator != newDiscriminator)
        {
            String oldDiscrimString = userObj.getDiscriminator();
            userObj.setDiscriminator(newDiscriminator);
            jda.handleEvent(
                new UserUpdateDiscriminatorEvent(
                    jda, responseNumber,
                    userObj, oldDiscrimString));
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
            member.setAvatarId(memberJson.getString("avatar", null));
            if (!memberJson.isNull("flags"))
                member.setFlags(memberJson.getInt("flags"));

            long boostTimestamp = memberJson.isNull("premium_since")
                ? 0
                : Helpers.toTimestamp(memberJson.getString("premium_since"));
            member.setBoostDate(boostTimestamp);

            long timeOutTimestamp = memberJson.isNull("communication_disabled_until")
                ? 0
                : Helpers.toTimestamp(memberJson.getString("communication_disabled_until"));
            member.setTimeOutEnd(timeOutTimestamp);

            if (!memberJson.isNull("pending"))
                member.setPending(memberJson.getBoolean("pending"));
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
            member.setJoinDate(Helpers.toTimestamp(memberJson.getString("joined_at")));
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
        AudioChannel audioChannel = (AudioChannel) guild.getGuildChannelById(channelId);
        if (audioChannel != null)
            ((AudioChannelMixin<?>) audioChannel).getConnectedMembersMap().put(member.getIdLong(), member);
        else
            LOG.error("Received a GuildVoiceState with a channel ID for a non-existent channel! ChannelId: {} GuildId: {} UserId: {}",
                      channelId, guild.getId(), user.getId());

        String requestToSpeak = voiceStateJson.getString("request_to_speak_timestamp", null);
        OffsetDateTime timestamp = null;
        if (requestToSpeak != null)
            timestamp = OffsetDateTime.parse(requestToSpeak);

        // VoiceState is considered volatile so we don't expect anything to actually exist
        voiceState.setSelfMuted(voiceStateJson.getBoolean("self_mute"))
                  .setSelfDeafened(voiceStateJson.getBoolean("self_deaf"))
                  .setGuildMuted(voiceStateJson.getBoolean("mute"))
                  .setGuildDeafened(voiceStateJson.getBoolean("deaf"))
                  .setSuppressed(voiceStateJson.getBoolean("suppress"))
                  .setSessionId(voiceStateJson.getString("session_id"))
                  .setStream(voiceStateJson.getBoolean("self_stream"))
                  .setRequestToSpeak(timestamp)
                  .setConnectedChannel(audioChannel);
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
        if (content.hasKey("avatar"))
        {
            String oldAvatarId = member.getAvatarId();
            String newAvatarId = content.getString("avatar", null);
            if (!Objects.equals(oldAvatarId, newAvatarId))
            {
                member.setAvatarId(newAvatarId);
                getJDA().handleEvent(
                        new GuildMemberUpdateAvatarEvent(
                                getJDA(), responseNumber,
                                member, oldAvatarId));
            }
        }
        if (content.hasKey("premium_since"))
        {
            long epoch = 0;
            if (!content.isNull("premium_since"))
                epoch = Helpers.toTimestamp(content.getString("premium_since"));
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

        if (content.hasKey("communication_disabled_until"))
        {
            long epoch = 0;
            if (!content.isNull("communication_disabled_until"))
                epoch = Helpers.toTimestamp(content.getString("communication_disabled_until"));
            if (epoch != member.getTimeOutEndRaw())
            {
                OffsetDateTime oldTime = member.getTimeOutEnd();
                member.setTimeOutEnd(epoch);
                getJDA().handleEvent(
                        new GuildMemberUpdateTimeOutEvent(
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

        if (!content.isNull("pending"))
        {
            boolean pending = content.getBoolean("pending");
            boolean oldPending = member.isPending();
            if (pending != oldPending)
            {
                member.setPending(pending);
                getJDA().handleEvent(
                    new GuildMemberUpdatePendingEvent(
                        getJDA(), responseNumber,
                        member, oldPending));
            }
        }

        if (!content.isNull("flags"))
        {
            int flags = content.getInt("flags");
            int oldFlags = member.getFlagsRaw();
            if (flags != oldFlags)
            {
                member.setFlags(flags);
                getJDA().handleEvent(
                    new GuildMemberUpdateFlagsEvent(
                        getJDA(), responseNumber,
                        member, Member.MemberFlag.fromRaw(oldFlags)));
            }
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
        OnlineStatus onlineStatus = OnlineStatus.fromKey(presenceJson.getString("status"));
        if (onlineStatus == OnlineStatus.OFFLINE)
            return; // don't cache offline member presences!
        MemberPresenceImpl presence = member.getPresence();
        if (presence == null)
        {
            CacheView.SimpleCacheView<MemberPresenceImpl> view = member.getGuild().getPresenceView();
            if (view == null)
                return;
            presence = new MemberPresenceImpl();
            try (UnlockHook lock = view.writeLock())
            {
                view.getMap().put(member.getIdLong(), presence);
            }
        }

        boolean cacheGame = getJDA().isCacheFlagSet(CacheFlag.ACTIVITY);
        boolean cacheStatus = getJDA().isCacheFlagSet(CacheFlag.CLIENT_STATUS);

        DataArray activityArray = !cacheGame || presenceJson.isNull("activities") ? null : presenceJson.getArray("activities");
        DataObject clientStatusJson = !cacheStatus || presenceJson.isNull("client_status") ? null : presenceJson.getObject("client_status");
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
                    String userId = member.getId();
                    if (LOG.isDebugEnabled())
                        LOG.warn("Encountered exception trying to parse a presence! UserId: {} JSON: {}", userId, activityArray, ex);
                    else
                        LOG.warn("Encountered exception trying to parse a presence! UserId: {} Message: {} Enable debug for details", userId, ex.getMessage());
                }
            }
        }
        if (cacheGame && parsedActivity)
            presence.setActivities(activities);
        presence.setOnlineStatus(onlineStatus);
        if (clientStatusJson != null)
        {
            for (String key : clientStatusJson.keys())
            {
                ClientType type = ClientType.fromKey(key);
                OnlineStatus status = OnlineStatus.fromKey(clientStatusJson.getString(key));
                presence.setOnlineStatus(type, status);
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
                ? Activity.ActivityType.PLAYING
                : Activity.ActivityType.fromKey(Integer.parseInt(gameJson.get("type").toString()));
        }
        catch (NumberFormatException e)
        {
            type = Activity.ActivityType.PLAYING;
        }

        Activity.Timestamps timestamps = null;
        if (!gameJson.isNull("timestamps"))
        {
            DataObject obj = gameJson.getObject("timestamps");
            long start, end;
            start = obj.getLong("start", 0L);
            end = obj.getLong("end", 0L);
            timestamps = new Activity.Timestamps(start, end);
        }

        EmojiUnion emoji = null;
        if (!gameJson.isNull("emoji"))
            emoji = createEmoji(gameJson.getObject("emoji"));

        if (type == Activity.ActivityType.CUSTOM_STATUS)
        {
            if (gameJson.hasKey("state"))
            {
                name = gameJson.getString("state", "");
                gameJson = gameJson.remove("state");
            }
        }

        String state = gameJson.isNull("state") ? null : String.valueOf(gameJson.get("state"));

        if (!CollectionUtils.containsAny(gameJson.keys(), richGameFields))
            return new ActivityImpl(name, state, url, type, timestamps, emoji);

        // data for spotify
        long id = gameJson.getLong("application_id", 0L);
        String sessionId = gameJson.getString("session_id", null);
        String syncId = gameJson.getString("sync_id", null);
        int flags = gameJson.getInt("flags", 0);
        String details = gameJson.isNull("details") ? null : String.valueOf(gameJson.get("details"));

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

    public RichCustomEmojiImpl createEmoji(GuildImpl guildObj, DataObject json)
    {
        DataArray emojiRoles = json.optArray("roles").orElseGet(DataArray::empty);
        final long emojiId = json.getLong("id");
        final User user = json.isNull("user") ? null : createUser(json.getObject("user"));
        RichCustomEmojiImpl emojiObj = (RichCustomEmojiImpl) guildObj.getEmojiById(emojiId);
        if (emojiObj == null)
            emojiObj = new RichCustomEmojiImpl(emojiId, guildObj);
        Set<Role> roleSet = emojiObj.getRoleSet();

        roleSet.clear();
        for (int j = 0; j < emojiRoles.length(); j++)
        {
            Role role = guildObj.getRoleById(emojiRoles.getString(j));
            if (role != null)
                roleSet.add(role);
        }
        if (user != null)
            emojiObj.setOwner(user);
        return emojiObj
                .setName(json.getString("name", ""))
                .setAnimated(json.getBoolean("animated"))
                .setManaged(json.getBoolean("managed"))
                .setAvailable(json.getBoolean("available", true));
    }

    public ScheduledEvent createScheduledEvent(GuildImpl guild, DataObject json)
    {
        final long id = json.getLong("id");
        ScheduledEventImpl scheduledEvent = (ScheduledEventImpl) guild.getScheduledEventsView().get(id);
        if (scheduledEvent == null)
        {
            SnowflakeCacheViewImpl<ScheduledEvent> scheduledEventView = guild.getScheduledEventsView();
            try (UnlockHook hook = scheduledEventView.writeLock())
            {
                scheduledEvent = new ScheduledEventImpl(id, guild);
                if (getJDA().isCacheFlagSet(CacheFlag.SCHEDULED_EVENTS))
                {
                    scheduledEventView.getMap().put(id, scheduledEvent);
                }
            }
        }

        scheduledEvent.setName(json.getString("name"))
                .setDescription(json.getString("description", null))
                .setStatus(ScheduledEvent.Status.fromKey(json.getInt("status", -1)))
                .setInterestedUserCount(json.getInt("user_count", -1))
                .setStartTime(json.getOffsetDateTime("scheduled_start_time"))
                .setEndTime(json.getOffsetDateTime("scheduled_end_time", null))
                .setImage(json.getString("image", null));

        final long creatorId = json.getLong("creator_id", 0);
        scheduledEvent.setCreatorId(creatorId);
        if (creatorId != 0)
        {
            if (json.hasKey("creator"))
                scheduledEvent.setCreator(createUser(json.getObject("creator")));
            else
                scheduledEvent.setCreator(getJDA().getUserById(creatorId));
        }
        final ScheduledEvent.Type type = ScheduledEvent.Type.fromKey(json.getInt("entity_type"));
        scheduledEvent.setType(type);
        switch (type)
        {
        case STAGE_INSTANCE:
        case VOICE:
            scheduledEvent.setLocation(json.getString("channel_id"));
            break;
        case EXTERNAL:
            String externalLocation;
            if (json.isNull("entity_metadata") || json.getObject("entity_metadata").isNull("location"))
                externalLocation = "";
            else
                externalLocation = json.getObject("entity_metadata").getString("location");

            scheduledEvent.setLocation(externalLocation);
        }
        return scheduledEvent;
    }


    public Category createCategory(DataObject json, long guildId)
    {
        return createCategory(null, json, guildId);
    }

    public Category createCategory(GuildImpl guild, DataObject json, long guildId)
    {
        boolean playbackCache = false;
        final long id = json.getLong("id");
        CategoryImpl channel = (CategoryImpl) getJDA().getCategoryById(id);
        if (channel == null)
        {
            if (guild == null)
                guild = (GuildImpl) getJDA().getGuildsView().get(guildId);
            ChannelCacheViewImpl<GuildChannel> guildView = guild.getChannelView();
            ChannelCacheViewImpl<Channel> globalView = getJDA().getChannelsView();
            try (
                UnlockHook glock = guildView.writeLock();
                UnlockHook jlock = globalView.writeLock())
            {
                channel = new CategoryImpl(id, guild);
                guildView.put(channel);
                playbackCache = globalView.put(channel) == null;
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

    public TextChannel createTextChannel(DataObject json, long guildId)
    {
        return createTextChannel(null, json, guildId);

    }

    public TextChannel createTextChannel(GuildImpl guildObj, DataObject json, long guildId)
    {
        boolean playbackCache = false;
        final long id = json.getLong("id");
        TextChannelImpl channel = (TextChannelImpl) getJDA().getTextChannelById(id);
        if (channel == null)
        {
            if (guildObj == null)
                guildObj = (GuildImpl) getJDA().getGuildsView().get(guildId);
            ChannelCacheViewImpl<GuildChannel> guildView = guildObj.getChannelView();
            ChannelCacheViewImpl<Channel> globalView = getJDA().getChannelsView();
            try (
                    UnlockHook glock = guildView.writeLock();
                    UnlockHook jlock = globalView.writeLock())
            {
                channel = new TextChannelImpl(id, guildObj);
                guildView.put(channel);
                playbackCache = globalView.put(channel) == null;
            }
        }

        channel
            .setParentCategory(json.getLong("parent_id", 0))
            .setLatestMessageIdLong(json.getLong("last_message_id", 0))
            .setName(json.getString("name"))
            .setTopic(json.getString("topic", null))
            .setPosition(json.getInt("position"))
            .setNSFW(json.getBoolean("nsfw"))
            .setDefaultThreadSlowmode(json.getInt("default_thread_rate_limit_per_user", 0))
            .setSlowmode(json.getInt("rate_limit_per_user", 0));

        createOverridesPass(channel, json.getArray("permission_overwrites"));
        if (playbackCache)
            getJDA().getEventCache().playbackCache(EventCache.Type.CHANNEL, id);
        return channel;
    }

    public NewsChannel createNewsChannel(DataObject json, long guildId)
    {
        return createNewsChannel(null, json, guildId);

    }

    public NewsChannel createNewsChannel(GuildImpl guildObj, DataObject json, long guildId)
    {
        boolean playbackCache = false;
        final long id = json.getLong("id");
        NewsChannelImpl channel = (NewsChannelImpl) getJDA().getNewsChannelById(id);
        if (channel == null)
        {
            if (guildObj == null)
                guildObj = (GuildImpl) getJDA().getGuildsView().get(guildId);
            ChannelCacheViewImpl<GuildChannel> guildView = guildObj.getChannelView();
            ChannelCacheViewImpl<Channel> globalView = getJDA().getChannelsView();
            try (
                    UnlockHook glock = guildView.writeLock();
                    UnlockHook jlock = globalView.writeLock())
            {
                channel = new NewsChannelImpl(id, guildObj);
                guildView.put(channel);
                playbackCache = globalView.put(channel) == null;
            }
        }

        channel
                .setParentCategory(json.getLong("parent_id", 0))
                .setLatestMessageIdLong(json.getLong("last_message_id", 0))
                .setName(json.getString("name"))
                .setTopic(json.getString("topic", null))
                .setPosition(json.getInt("position"))
                .setNSFW(json.getBoolean("nsfw"));

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
        VoiceChannelImpl channel = (VoiceChannelImpl) getJDA().getVoiceChannelById(id);
        if (channel == null)
        {
            if (guild == null)
                guild = (GuildImpl) getJDA().getGuildsView().get(guildId);
            ChannelCacheViewImpl<GuildChannel> guildView = guild.getChannelView();
            ChannelCacheViewImpl<Channel> globalView = getJDA().getChannelsView();
            try (
                    UnlockHook glock = guildView.writeLock();
                    UnlockHook jlock = globalView.writeLock())
            {
                channel = new VoiceChannelImpl(id, guild);
                guildView.put(channel);
                playbackCache = globalView.put(channel) == null;
            }
        }

        channel
            .setParentCategory(json.getLong("parent_id", 0))
            .setLatestMessageIdLong(json.getLong("last_message_id", 0))
            .setName(json.getString("name"))
            .setStatus(json.getString("status", ""))
            .setPosition(json.getInt("position"))
            .setUserLimit(json.getInt("user_limit"))
            .setNSFW(json.getBoolean("nsfw"))
            .setBitrate(json.getInt("bitrate"))
            .setRegion(json.getString("rtc_region", null))
//            .setDefaultThreadSlowmode(json.getInt("default_thread_rate_limit_per_user", 0))
            .setSlowmode(json.getInt("rate_limit_per_user", 0));

        createOverridesPass(channel, json.getArray("permission_overwrites"));
        if (playbackCache)
            getJDA().getEventCache().playbackCache(EventCache.Type.CHANNEL, id);
        return channel;
    }

    public StageChannel createStageChannel(DataObject json, long guildId)
    {
        return createStageChannel(null, json, guildId);
    }

    public StageChannel createStageChannel(GuildImpl guild, DataObject json, long guildId)
    {
        boolean playbackCache = false;
        final long id = json.getLong("id");
        StageChannelImpl channel = (StageChannelImpl) getJDA().getStageChannelById(id);
        if (channel == null)
        {
            if (guild == null)
                guild = (GuildImpl) getJDA().getGuildsView().get(guildId);
            ChannelCacheViewImpl<GuildChannel> guildView = guild.getChannelView();
            ChannelCacheViewImpl<Channel> globalView = getJDA().getChannelsView();
            try (
                    UnlockHook glock = guildView.writeLock();
                    UnlockHook jlock = globalView.writeLock())
            {
                channel = new StageChannelImpl(id, guild);
                guildView.put(channel);
                playbackCache = globalView.put(channel) == null;
            }
        }

        channel
            .setParentCategory(json.getLong("parent_id", 0))
            .setLatestMessageIdLong(json.getLong("last_message_id", 0))
            .setName(json.getString("name"))
            .setPosition(json.getInt("position"))
            .setBitrate(json.getInt("bitrate"))
            .setUserLimit(json.getInt("user_limit", 0))
            .setNSFW(json.getBoolean("nsfw"))
            .setRegion(json.getString("rtc_region", null))
//            .setDefaultThreadSlowmode(json.getInt("default_thread_rate_limit_per_user", 0))
            .setSlowmode(json.getInt("rate_limit_per_user", 0));

        createOverridesPass(channel, json.getArray("permission_overwrites"));
        if (playbackCache)
            getJDA().getEventCache().playbackCache(EventCache.Type.CHANNEL, id);
        return channel;
    }

    public ThreadChannel createThreadChannel(DataObject json, long guildId)
    {
        return createThreadChannel(null, json, guildId);
    }

    public ThreadChannel createThreadChannel(GuildImpl guild, DataObject json, long guildId)
    {
        return createThreadChannel(guild, json, guildId, true);
    }

    public ThreadChannel createThreadChannel(GuildImpl guild, DataObject json, long guildId, boolean modifyCache)
    {
        boolean playbackCache = false;
        final long id = json.getUnsignedLong("id");
        final long parentId = json.getUnsignedLong("parent_id");
        final ChannelType type = ChannelType.fromId(json.getInt("type"));

        if (guild == null)
            guild = (GuildImpl) getJDA().getGuildsView().get(guildId);

        IThreadContainer parent = guild.getChannelById(IThreadContainer.class, parentId);
        if (parent == null)
            throw new IllegalArgumentException(MISSING_CHANNEL);

        ThreadChannelImpl channel = ((ThreadChannelImpl) getJDA().getThreadChannelById(id));
        if (channel == null)
        {
            ChannelCacheViewImpl<GuildChannel> guildThreadView = guild.getChannelView();
            ChannelCacheViewImpl<Channel> threadView = getJDA().getChannelsView();
            try (
                    UnlockHook vlock = guildThreadView.writeLock();
                    UnlockHook jlock = threadView.writeLock())
            {
                channel = new ThreadChannelImpl(id, guild, type);
                if (modifyCache)
                {
                    guildThreadView.put(channel);
                    playbackCache = threadView.put(channel) == null;
                }
            }
        }

        DataObject threadMetadata = json.getObject("thread_metadata");

        if (!json.isNull("applied_tags") && api.isCacheFlagSet(CacheFlag.FORUM_TAGS))
        {
            DataArray array = json.getArray("applied_tags");
            channel.setAppliedTags(IntStream.range(0, array.length()).mapToLong(array::getUnsignedLong));
        }

        channel
                .setName(json.getString("name"))
                .setFlags(json.getInt("flags", 0))
                .setParentChannel(parent)
                .setOwnerId(json.getLong("owner_id"))
                .setMemberCount(json.getInt("member_count"))
                .setMessageCount(json.getInt("message_count"))
                .setTotalMessageCount(json.getInt("total_message_count", 0))
                .setLatestMessageIdLong(json.getLong("last_message_id", 0))
                .setSlowmode(json.getInt("rate_limit_per_user", 0))
                .setLocked(threadMetadata.getBoolean("locked"))
                .setArchived(threadMetadata.getBoolean("archived"))
                .setInvitable(threadMetadata.getBoolean("invitable"))
                .setArchiveTimestamp(Helpers.toTimestamp(threadMetadata.getString("archive_timestamp")))
                .setCreationTimestamp(threadMetadata.isNull("create_timestamp") ? 0 : Helpers.toTimestamp(threadMetadata.getString("create_timestamp")))
                .setAutoArchiveDuration(ThreadChannel.AutoArchiveDuration.fromKey(threadMetadata.getInt("auto_archive_duration")));

        //If the bot in the thread already, then create a thread member for the bot.
        if (!json.isNull("member"))
        {
            ThreadMember selfThreadMember = createThreadMember(channel, guild.getSelfMember(), json.getObject("member"));
            CacheView.SimpleCacheView<ThreadMember> view = channel.getThreadMemberView();
            try (UnlockHook lock = view.writeLock())
            {
                view.getMap().put(selfThreadMember.getIdLong(), selfThreadMember);
            }
        }

        if (playbackCache)
            getJDA().getEventCache().playbackCache(EventCache.Type.CHANNEL, id);
        return channel;
    }

    public ThreadMember createThreadMember(GuildImpl guild, ThreadChannelImpl threadChannel, DataObject json)
    {
        DataObject memberJson = json.getObject("member");
        DataObject presenceJson = json.isNull("presence") ? null : json.getObject("presence");

        Member member = createMember(guild, memberJson, null, presenceJson);
        return createThreadMember(threadChannel, member, json);
    }

    public ThreadMember createThreadMember(ThreadChannelImpl threadChannel, Member member, DataObject json)
    {
        ThreadMemberImpl threadMember = new ThreadMemberImpl(member, threadChannel);
        threadMember
            .setJoinedTimestamp(Helpers.toTimestamp(json.getString("join_timestamp")));

        return threadMember;
    }

    public ForumChannel createForumChannel(DataObject json, long guildId)
    {
        return createForumChannel(null, json, guildId);
    }

    public ForumChannel createForumChannel(GuildImpl guild, DataObject json, long guildId)
    {
        boolean playbackCache = false;
        final long id = json.getLong("id");
        ForumChannelImpl channel = (ForumChannelImpl) getJDA().getForumChannelById(id);
        if (channel == null)
        {
            if (guild == null)
                guild = (GuildImpl) getJDA().getGuildsView().get(guildId);
            ChannelCacheViewImpl<GuildChannel> guildView = guild.getChannelView();
            ChannelCacheViewImpl<Channel> globalView = getJDA().getChannelsView();
            try (
                    UnlockHook glock = guildView.writeLock();
                    UnlockHook jlock = globalView.writeLock())
            {
                channel = new ForumChannelImpl(id, guild);
                guildView.put(channel);
                playbackCache = globalView.put(channel) == null;
            }
        }

        if (api.isCacheFlagSet(CacheFlag.FORUM_TAGS))
        {
            DataArray tags = json.getArray("available_tags");
            for (int i = 0; i < tags.length(); i++)
                createForumTag(channel, tags.getObject(i), i);
        }

        channel
                .setParentCategory(json.getLong("parent_id", 0))
                .setFlags(json.getInt("flags", 0))
                .setDefaultReaction(json.optObject("default_reaction_emoji").orElse(null))
                .setDefaultSortOrder(json.getInt("default_sort_order", -1))
                .setDefaultLayout(json.getInt("default_forum_layout", -1))
                .setName(json.getString("name"))
                .setTopic(json.getString("topic", null))
                .setPosition(json.getInt("position"))
                .setDefaultThreadSlowmode(json.getInt("default_thread_rate_limit_per_user", 0))
                .setSlowmode(json.getInt("rate_limit_per_user", 0))
                .setNSFW(json.getBoolean("nsfw"));

        createOverridesPass(channel, json.getArray("permission_overwrites"));
        if (playbackCache)
            getJDA().getEventCache().playbackCache(EventCache.Type.CHANNEL, id);
        return channel;
    }

    public MediaChannel createMediaChannel(DataObject json, long guildId)
    {
        return createMediaChannel(null, json, guildId);
    }

    public MediaChannel createMediaChannel(GuildImpl guild, DataObject json, long guildId)
    {
        boolean playbackCache = false;
        final long id = json.getLong("id");
        MediaChannelImpl channel = (MediaChannelImpl) getJDA().getMediaChannelById(id);
        if (channel == null)
        {
            if (guild == null)
                guild = (GuildImpl) getJDA().getGuildsView().get(guildId);
            ChannelCacheViewImpl<GuildChannel> guildView = guild.getChannelView();
            ChannelCacheViewImpl<Channel> globalView = getJDA().getChannelsView();
            try (
                    UnlockHook glock = guildView.writeLock();
                    UnlockHook jlock = globalView.writeLock())
            {
                channel = new MediaChannelImpl(id, guild);
                guildView.put(channel);
                playbackCache = globalView.put(channel) == null;
            }
        }

        if (api.isCacheFlagSet(CacheFlag.FORUM_TAGS))
        {
            DataArray tags = json.getArray("available_tags");
            for (int i = 0; i < tags.length(); i++)
                createForumTag(channel, tags.getObject(i), i);
        }

        channel
                .setParentCategory(json.getLong("parent_id", 0))
                .setFlags(json.getInt("flags", 0))
                .setDefaultReaction(json.optObject("default_reaction_emoji").orElse(null))
                .setDefaultSortOrder(json.getInt("default_sort_order", -1))
                .setName(json.getString("name"))
                .setTopic(json.getString("topic", null))
                .setPosition(json.getInt("position"))
                .setDefaultThreadSlowmode(json.getInt("default_thread_rate_limit_per_user", 0))
                .setSlowmode(json.getInt("rate_limit_per_user", 0))
                .setNSFW(json.getBoolean("nsfw"));

        createOverridesPass(channel, json.getArray("permission_overwrites"));
        if (playbackCache)
            getJDA().getEventCache().playbackCache(EventCache.Type.CHANNEL, id);
        return channel;
    }

    public ForumTagImpl createForumTag(IPostContainerMixin<?> channel, DataObject json, int index)
    {
        final long id = json.getUnsignedLong("id");
        SortedSnowflakeCacheViewImpl<ForumTag> cache = channel.getAvailableTagCache();
        ForumTagImpl tag = (ForumTagImpl) cache.get(id);

        if (tag == null)
        {
            try (UnlockHook lock = cache.writeLock())
            {
                tag = new ForumTagImpl(id);
                cache.getMap().put(id, tag);
            }
        }

        tag.setName(json.getString("name"))
           .setModerated(json.getBoolean("moderated"))
           .setEmoji(json)
           .setPosition(index);
        return tag;
    }

    public PrivateChannel createPrivateChannel(DataObject json)
    {
        return createPrivateChannel(json, null);
    }

    public PrivateChannel createPrivateChannel(DataObject json, UserImpl user)
    {
        final long channelId = json.getUnsignedLong("id");
        PrivateChannelImpl channel = (PrivateChannelImpl) api.getPrivateChannelById(channelId);
        if (channel == null)
        {
            channel = new PrivateChannelImpl(getJDA(), channelId, user)
                    .setLatestMessageIdLong(json.getLong("last_message_id", 0));
        }
        UserImpl recipient = user;
        if (channel.getUser() == null)
        {
            if (recipient == null && (json.hasKey("recipients") || json.hasKey("recipient")))
            {
                //if we don't know the recipient, and we have information on them, we can use that
                DataObject recipientJson = json.hasKey("recipients") ?
                        json.getArray("recipients").getObject(0) :
                        json.getObject("recipient");
                final long userId = recipientJson.getUnsignedLong("id");
                recipient = (UserImpl) getJDA().getUserById(userId);
                if (recipient == null)
                {
                    recipient = createUser(recipientJson);
                }
            }
            if (recipient != null)
            {
                //update the channel if we have found the user
                channel.setUser(recipient);
            }
        }
        if (recipient != null)
        {
            recipient.setPrivateChannel(channel);
        }
        // only add channels to the cache when they come from an event, otherwise we would never remove the channel
        cachePrivateChannel(channel);
        api.usedPrivateChannel(channelId);
        return channel;
    }

    private void cachePrivateChannel(PrivateChannelImpl priv)
    {
        ChannelCacheViewImpl<Channel> privateView = getJDA().getChannelsView();
        privateView.put(priv);
        api.usedPrivateChannel(priv.getIdLong());
        getJDA().getEventCache().playbackCache(EventCache.Type.CHANNEL, priv.getIdLong());
    }

    @Nullable
    public StageInstance createStageInstance(GuildImpl guild, DataObject json)
    {
        long channelId = json.getUnsignedLong("channel_id");
        StageChannelImpl channel = (StageChannelImpl) guild.getStageChannelById(channelId);
        if (channel == null)
            return null;

        long id = json.getUnsignedLong("id");
        String topic = json.getString("topic");
        StageInstance.PrivacyLevel level = StageInstance.PrivacyLevel.fromKey(json.getInt("privacy_level", -1));


        StageInstanceImpl instance = (StageInstanceImpl) channel.getStageInstance();
        if (instance == null)
        {
            instance = new StageInstanceImpl(id, channel);
            channel.setStageInstance(instance);
        }

        return instance
                .setPrivacyLevel(level)
                .setTopic(topic);
    }

    public void createOverridesPass(IPermissionContainerMixin<?> channel, DataArray overrides)
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
            .setRawPermissions(roleJson.getLong("permissions"))
            .setManaged(roleJson.getBoolean("managed"))
            .setHoisted(roleJson.getBoolean("hoist"))
            .setColor(color == 0 ? Role.DEFAULT_COLOR_RAW : color)
            .setMentionable(roleJson.getBoolean("mentionable"))
            .setTags(roleJson.optObject("tags").orElseGet(DataObject::empty));

        final String iconId = roleJson.getString("icon", null);
        final String emoji = roleJson.getString("unicode_emoji", null);
        if (iconId == null && emoji == null)
            role.setIcon(null);
        else
            role.setIcon(new RoleIcon(iconId, emoji, id));

        if (playbackCache)
            getJDA().getEventCache().playbackCache(EventCache.Type.ROLE, id);
        return role;
    }

    public ReceivedMessage createMessageBestEffort(DataObject json, MessageChannel channel, Guild guild)
    {
        if (channel != null)
            return createMessageWithChannel(json, channel, false);
        else
            return createMessageFromWebhook(json, guild);
    }

    public ReceivedMessage createMessageFromWebhook(DataObject json, @Nullable Guild guild)
    {
        return createMessage0(json, null, (GuildImpl) guild, false);
    }

    public ReceivedMessage createMessageWithChannel(DataObject json, @Nonnull MessageChannel channel, boolean modifyCache)
    {
        // Use channel directly if message is from a known guild channel
        if (channel instanceof GuildMessageChannel)
            return createMessage0(json, channel, (GuildImpl) ((GuildMessageChannel) channel).getGuild(), modifyCache);
        // Try to resolve private channel recipient if needed
        if (channel instanceof PrivateChannel)
            return createMessageWithLookup(json, null, modifyCache);
        throw new IllegalArgumentException(MISSING_CHANNEL);
    }

    public ReceivedMessage createMessageWithLookup(DataObject json, @Nullable Guild guild, boolean modifyCache)
    {
        //Private channels may be partial in our cache and missing recipient information
        // we can try and derive the user from the message here
        if (guild == null)
            return createMessage0(json, createPrivateChannelByMessage(json), null, modifyCache);
        //If we know that the message was sent in a guild, we can use the guild to resolve the channel directly
        MessageChannel channel = guild.getChannelById(GuildMessageChannel.class, json.getUnsignedLong("channel_id"));
//        if (channel == null)
//            throw new IllegalArgumentException(MISSING_CHANNEL);
        return createMessage0(json, channel, (GuildImpl) guild, modifyCache);
    }

    // This tries to build a private channel instance through an arbitrary message object
    private PrivateChannel createPrivateChannelByMessage(DataObject message)
    {
        final long channelId = message.getLong("channel_id");
        final DataObject author = message.getObject("author");

        PrivateChannelImpl channel = (PrivateChannelImpl) getJDA().getPrivateChannelById(channelId);
        boolean isRecipient = !author.getBoolean("bot"); // bots cannot dm other bots
        if (channel == null)
        {
            DataObject channelData = DataObject.empty()
                    .put("id", channelId);

            //if we see an author that isn't us, we can assume that is the other side of this private channel
            //if the author is us, we learn no information about the user at the other end
            if (isRecipient)
                channelData.put("recipient", author);

            //even without knowing the user at the other end, we can still construct a minimal channel
            channel = (PrivateChannelImpl) createPrivateChannel(channelData);
        }
        else if (channel.getUser() == null && isRecipient)
        {
            //In this situation, we already know the channel
            // but the message provided us with the recipient
            // which we can now add to the channel
            UserImpl user = createUser(author);
            channel.setUser(user);
            user.setPrivateChannel(channel);
        }

        return channel;
    }

    private ReceivedMessage createMessage0(DataObject jsonObject, @Nullable MessageChannel channel, @Nullable GuildImpl guild, boolean modifyCache)
    {
        MessageType type = MessageType.fromId(jsonObject.getInt("type"));
        if (type == MessageType.UNKNOWN)
            throw new IllegalArgumentException(UNKNOWN_MESSAGE_TYPE);

        final long id = jsonObject.getLong("id");
        final DataObject author = jsonObject.getObject("author");
        final long authorId = author.getLong("id");
        final long channelId = jsonObject.getUnsignedLong("channel_id");
        final long guildId = channel instanceof GuildChannel
                ? ((GuildChannel) channel).getGuild().getIdLong()
                : jsonObject.getUnsignedLong("guild_id", 0L);
        MemberImpl member = null;

        // Member details for author
        if (guild != null && !jsonObject.isNull("member"))
        {
            DataObject memberJson = jsonObject.getObject("member");
            memberJson.put("user", author);
            member = createMember(guild, memberJson);
            if (modifyCache)
            {
                // Update member cache with new information if needed
                updateMemberCache(member);
            }
        }

        final String content = jsonObject.getString("content", "");
        final boolean fromWebhook = jsonObject.hasKey("webhook_id");
        final long applicationId = jsonObject.getUnsignedLong("application_id", 0);
        final boolean pinned = jsonObject.getBoolean("pinned");
        final boolean tts = jsonObject.getBoolean("tts");
        final boolean mentionsEveryone = jsonObject.getBoolean("mention_everyone");
        final OffsetDateTime editTime = jsonObject.isNull("edited_timestamp") ? null : OffsetDateTime.parse(jsonObject.getString("edited_timestamp"));
        final String nonce = jsonObject.isNull("nonce") ? null : jsonObject.get("nonce").toString();
        final int flags = jsonObject.getInt("flags", 0);

        // Message accessories
        MessageChannel tmpChannel = channel; // because java
        final List<Message.Attachment> attachments = map(jsonObject, "attachments",   this::createMessageAttachment);
        final List<MessageEmbed>       embeds      = map(jsonObject, "embeds",        this::createMessageEmbed);
        final List<MessageReaction>    reactions   = map(jsonObject, "reactions",     (obj) -> createMessageReaction(tmpChannel, channelId, id, obj));
        final List<StickerItem>        stickers    = map(jsonObject, "sticker_items", this::createStickerItem);

        // Message activity (for game invites/spotify)
        MessageActivity activity = null;
        if (!jsonObject.isNull("activity"))
            activity = createMessageActivity(jsonObject);

        // Message Author
        User user;
        if (guild != null)
        {
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
        }
        else if (channel instanceof PrivateChannel)
        {
            //Assume private channel
            if (authorId == getJDA().getSelfUser().getIdLong())
            {
                user = getJDA().getSelfUser();
            }
            else
            {
                //Note, while PrivateChannel.getUser() can produce null, this invocation of it WILL NOT produce null
                // because when the bot receives a message in a private channel that was _not authored by the bot_ then
                // the message had to have come from the user, so that means that we had all the information to build
                // the channel properly (or fill-in the missing user info of an existing partial channel)
                user = ((PrivateChannel) channel).getUser();
            }
        }
        else
        {
            user = createUser(author);
        }

        if (modifyCache && !fromWebhook) // update the user information on message receive
            updateUser((UserImpl) user, author);

        // Message Reference (Reply or Pin)
        Message referencedMessage = null;
        if (!jsonObject.isNull("referenced_message"))
        {
            DataObject referenceJson = jsonObject.getObject("referenced_message");
            try
            {
                referencedMessage = createMessage0(referenceJson, channel, guild, false);
            }
            catch (IllegalArgumentException ex)
            {
                // We can just discard the message for some trivial cases
                if (UNKNOWN_MESSAGE_TYPE.equals(ex.getMessage()))
                    LOG.debug("Received referenced message with unknown type. Type: {}", referenceJson.getInt("type", -1));
                else if (MISSING_CHANNEL.equals(ex.getMessage()))
                    LOG.debug("Received referenced message with unknown channel. channel_id: {} Type: {}",
                        referenceJson.getUnsignedLong("channel_id", 0), referenceJson.getInt("type", -1));
                else
                    throw ex;
            }
        }

        MessageReference messageReference = null;
        if (!jsonObject.isNull("message_reference")) // always contains the channel + message id for a referenced message
        {                                                // used for when referenced_message is not provided
            DataObject messageReferenceJson = jsonObject.getObject("message_reference");

            messageReference = new MessageReference(
                    messageReferenceJson.getLong("message_id", 0),
                    messageReferenceJson.getLong("channel_id", 0),
                    messageReferenceJson.getLong("guild_id", 0),
                    referencedMessage,
                    api
            );
        }

        // Message Components
        List<ActionRow> components = Collections.emptyList();
        Optional<DataArray> componentsArrayOpt = jsonObject.optArray("components");
        if (componentsArrayOpt.isPresent())
        {
            DataArray array = componentsArrayOpt.get();
            components = array.stream(DataArray::getObject)
                    .filter(it -> it.getInt("type", 0) == 1)
                    .map(ActionRow::fromData)
                    .collect(Collectors.toList());
        }

        // Application command and component replies
        Message.Interaction messageInteraction = null;
        if (!jsonObject.isNull("interaction"))
            messageInteraction = createMessageInteraction(guild, jsonObject.getObject("interaction"));

        // Lazy Mention parsing and caching (includes reply mentions)
        Mentions mentions = new MessageMentionsImpl(
            api, guild, content, mentionsEveryone,
            jsonObject.getArray("mentions"), jsonObject.getArray("mention_roles")
        );

        ThreadChannel startedThread = null;
        if (guild != null && !jsonObject.isNull("thread"))
            startedThread = createThreadChannel(guild, jsonObject.getObject("thread"), guild.getIdLong());

        int position = jsonObject.getInt("position", -1);

        return new ReceivedMessage(id, channelId, guildId, api, guild, channel, type, messageReference, fromWebhook, applicationId, tts, pinned,
                content, nonce, user, member, activity, editTime, mentions, reactions, attachments, embeds, stickers, components, flags,
                messageInteraction, startedThread, position);
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

    public MessageReaction createMessageReaction(MessageChannel chan, long channelId, long messageId, DataObject obj)
    {
        DataObject emoji = obj.getObject("emoji");
        final int[] count = new int[]{
                obj.getInt("count", 0), // total
                obj.optObject("count_details").map(o -> o.getInt("normal", 0)).orElse(0),
                obj.optObject("count_details").map(o -> o.getInt("burst", 0)).orElse(0),
        };
        final boolean[] me = new boolean[] {
                obj.getBoolean("me"), // normal
                obj.getBoolean("me_burst") // super
        };
        EmojiUnion emojiObj = createEmoji(emoji);

        return new MessageReaction(api, chan, emojiObj, channelId, messageId, me, count);
    }

    public Message.Attachment createMessageAttachment(DataObject jsonObject)
    {
        final boolean ephemeral = jsonObject.getBoolean("ephemeral", false);
        final int width = jsonObject.getInt("width", -1);
        final int height = jsonObject.getInt("height", -1);
        final int size = jsonObject.getInt("size");
        final String url = jsonObject.getString("url");
        final String proxyUrl = jsonObject.getString("proxy_url");
        final String filename = jsonObject.getString("filename");
        final String contentType = jsonObject.getString("content_type", null);
        final String description = jsonObject.getString("description", null);
        final long id = jsonObject.getLong("id");
        final String waveform = jsonObject.getString("waveform", null);
        final double duration = jsonObject.getDouble("duration_secs", 0);
        return new Message.Attachment(id, url, proxyUrl, filename, contentType, description, size, height, width, ephemeral, waveform, duration, getJDA());
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

    public StickerItem createStickerItem(DataObject content)
    {
        long id = content.getLong("id");
        String name = content.getString("name");
        Sticker.StickerFormat format = Sticker.StickerFormat.fromId(content.getInt("format_type"));
        return new StickerItemImpl(id, format, name);
    }

    public RichStickerImpl createRichSticker(DataObject content)
    {
        long id = content.getLong("id");
        String name = content.getString("name");
        Sticker.StickerFormat format = Sticker.StickerFormat.fromId(content.getInt("format_type"));
        Sticker.Type type = Sticker.Type.fromId(content.getInt("type", -1));

        String description = content.getString("description", "");
        Set<String> tags = Collections.emptySet();
        if (!content.isNull("tags"))
        {
            String[] array = content.getString("tags").split(",\\s*");
            tags = Helpers.setOf(array);
        }

        switch (type)
        {
        case GUILD:
            boolean available = content.getBoolean("available");
            long guildId = content.getUnsignedLong("guild_id", 0L);
            User owner = content.isNull("user") ? null : createUser(content.getObject("user"));
            return new GuildStickerImpl(id, format, name, tags, description, available, guildId, api, owner);
        case STANDARD:
            long packId = content.getUnsignedLong("pack_id", 0L);
            int sortValue = content.getInt("sort_value", -1);
            return new StandardStickerImpl(id, format, name, tags, description, packId, sortValue);
        default:
            throw new IllegalArgumentException("Unknown sticker type. Type: " + type  +" JSON: " + content);
        }
    }

    public StickerPack createStickerPack(DataObject content)
    {
        long id = content.getUnsignedLong("id");
        String name = content.getString("name");
        String description = content.getString("description", "");
        long skuId = content.getUnsignedLong("sku_id", 0);
        long coverId = content.getUnsignedLong("cover_sticker_id", 0);
        long bannerId = content.getUnsignedLong("banner_asset_id", 0);

        DataArray stickerArr = content.getArray("stickers");
        List<StandardSticker> stickers = new ArrayList<>(stickerArr.length());
        for (int i = 0; i < stickerArr.length(); i++)
        {
            DataObject object = null;
            try
            {
                object = stickerArr.getObject(i);
                StandardSticker sticker = (StandardSticker) createRichSticker(object);
                stickers.add(sticker);
            }
            catch (ParsingException | ClassCastException ex)
            {
                LOG.error("Sticker contained in pack {} ({}) could not be parsed. JSON: {}", name, id, object);
            }
        }

        return new StickerPackImpl(id, stickers, name, description, coverId, bannerId, skuId);
    }

    public Message.Interaction createMessageInteraction(GuildImpl guildImpl, DataObject content)
    {
        final long id = content.getLong("id");
        final int type = content.getInt("type");
        final String name = content.getString("name");
        DataObject userJson = content.getObject("user");
        User user = null;
        MemberImpl member = null;
        if (!content.isNull("member") && guildImpl != null)
        {
            DataObject memberJson = content.getObject("member");
            memberJson.put("user", userJson);
            member = createMember(guildImpl, memberJson);
            user = member.getUser();
        }
        else
        {
            user = createUser(userJson);
        }

        return new Message.Interaction(id, type, name, user, member);
    }

    @Nullable
    public PermissionOverride createPermissionOverride(DataObject override, IPermissionContainerMixin<?> chan)
    {
        int type = override.getInt("type");
        final long id = override.getLong("id");
        boolean role = type == 0;
        if (role && chan.getGuild().getRoleById(id) == null)
            throw new NoSuchElementException("Attempted to create a PermissionOverride for a non-existent role! JSON: " + override);
        if (!role && type != 1)
            throw new IllegalArgumentException("Provided with an unknown PermissionOverride type! JSON: " + override);
        if (!role && id != api.getSelfUser().getIdLong() && !api.isCacheFlagSet(CacheFlag.MEMBER_OVERRIDES))
            return null;

        long allow = override.getLong("allow");
        long deny = override.getLong("deny");
        // Don't cache empty @everyone overrides, they ruin our sync check
        if (id == chan.getGuild().getIdLong() && (allow | deny) == 0L)
            return null;

        PermissionOverrideImpl permOverride = (PermissionOverrideImpl) chan.getPermissionOverrideMap().get(id);
        if (permOverride == null)
        {
            permOverride = new PermissionOverrideImpl(chan, id, role);
            chan.getPermissionOverrideMap().put(id, permOverride);
        }

        return permOverride.setAllow(allow).setDeny(deny);
    }

    public WebhookImpl createWebhook(DataObject object)
    {
        return createWebhook(object, false);
    }

    public WebhookImpl createWebhook(DataObject object, boolean allowMissingChannel)
    {
        final long id = object.getLong("id");
        final long guildId = object.getUnsignedLong("guild_id");
        final long channelId = object.getUnsignedLong("channel_id");
        final String token = object.getString("token", null);
        final WebhookType type = WebhookType.fromKey(object.getInt("type", -1));

        IWebhookContainer channel = getJDA().getChannelById(IWebhookContainer.class, channelId);
        if (channel == null && !allowMissingChannel)
            throw new NullPointerException(String.format("Tried to create Webhook for an un-cached IWebhookContainer channel! WebhookId: %s ChannelId: %s GuildId: %s",
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

        Member ownerMember = owner == null || channel == null ? null : channel.getGuild().getMember(owner);
        WebhookImpl webhook = new WebhookImpl(channel, getJDA(), id, type)
                .setToken(token)
                .setOwner(ownerMember, owner)
                .setUser(defaultUser);

        if (!object.isNull("source_channel"))
        {
            DataObject source = object.getObject("source_channel");
            webhook.setSourceChannel(new Webhook.ChannelReference(source.getUnsignedLong("id"), source.getString("name")));
        }
        if (!object.isNull("source_guild"))
        {
            DataObject source = object.getObject("source_guild");
            webhook.setSourceGuild(new Webhook.GuildReference(source.getUnsignedLong("id"), source.getString("name")));
        }

        return webhook;
    }

    public Invite createInvite(DataObject object)
    {
        final String code = object.getString("code");
        final User inviter = object.hasKey("inviter") ? createUser(object.getObject("inviter")) : null;

        final DataObject channelObject = object.getObject("channel");
        final ChannelType channelType = ChannelType.fromId(channelObject.getInt("type"));
        final Invite.TargetType targetType = Invite.TargetType.fromId(object.getInt("target_type", 0));

        final Invite.InviteType type;
        final Invite.Guild guild;
        final Invite.Channel channel;
        final Invite.Group group;
        final Invite.InviteTarget target;

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

            final GuildWelcomeScreen welcomeScreen = guildObject.isNull("welcome_screen")
                    ? null
                    : createWelcomeScreen(null, guildObject.getObject("welcome_screen"));

            guild = new InviteImpl.GuildImpl(guildId, guildIconId, guildName, guildSplashId, guildVerificationLevel, presenceCount, memberCount, guildFeatures, welcomeScreen);

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

        switch (targetType)
        {
        case EMBEDDED_APPLICATION:
            final DataObject applicationObject = object.getObject("target_application");

            Invite.EmbeddedApplication application = new InviteImpl.EmbeddedApplicationImpl(
                    applicationObject.getString("icon", null), applicationObject.getString("name"), applicationObject.getString("description"),
                    applicationObject.getString("summary"), applicationObject.getLong("id"), applicationObject.getInt("max_participants", -1)
            );
            target = new InviteImpl.InviteTargetImpl(targetType, application, null);
            break;
        case STREAM:
            final DataObject targetUserObject = object.getObject("target_user");
            target = new InviteImpl.InviteTargetImpl(targetType, null, createUser(targetUserObject));
            break;
        case NONE:
            target = null;
            break;
        default:
            target = new InviteImpl.InviteTargetImpl(targetType, null, null);
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
                              maxAge, maxUses, temporary, timeCreated,
                              uses, channel, guild, group, target, type);
    }

    public GuildWelcomeScreen createWelcomeScreen(Guild guild, DataObject object)
    {
        final DataArray welcomeChannelsArray = object.getArray("welcome_channels");
        final List<GuildWelcomeScreen.Channel> welcomeChannels = new ArrayList<>(welcomeChannelsArray.length());
        for (int i = 0; i < welcomeChannelsArray.length(); i++)
        {
            final DataObject welcomeChannelObj = welcomeChannelsArray.getObject(i);
            EmojiUnion emoji = null;
            if (!welcomeChannelObj.isNull("emoji_id") || !welcomeChannelObj.isNull("emoji_name"))
                emoji = createEmoji(welcomeChannelObj, "emoji_name", "emoji_id");

            welcomeChannels.add(new GuildWelcomeScreenImpl.ChannelImpl(
                    guild,
                    welcomeChannelObj.getLong("channel_id"),
                    welcomeChannelObj.getString("description"),
                    emoji)
            );
        }
        return new GuildWelcomeScreenImpl(guild, object.getString("description", null), Collections.unmodifiableList(welcomeChannels));
    }

    public Template createTemplate(DataObject object)
    {
        final String code = object.getString("code");
        final String name = object.getString("name");
        final String description = object.getString("description", null);
        final int uses = object.getInt("usage_count");
        final User creator = createUser(object.getObject("creator"));
        final OffsetDateTime createdAt = OffsetDateTime.parse(object.getString("created_at"));
        final OffsetDateTime updatedAt = OffsetDateTime.parse(object.getString("updated_at"));
        final long guildId = object.getLong("source_guild_id");
        final DataObject guildObject = object.getObject("serialized_source_guild");
        final String guildName = guildObject.getString("name");
        final String guildDescription = guildObject.getString("description", null);
        final String guildIconId = guildObject.getString("icon_hash", null);
        final VerificationLevel guildVerificationLevel = VerificationLevel.fromKey(guildObject.getInt("verification_level", -1));
        final NotificationLevel notificationLevel = NotificationLevel.fromKey(guildObject.getInt("default_message_notifications", 0));
        final ExplicitContentLevel explicitContentLevel = ExplicitContentLevel.fromKey(guildObject.getInt("explicit_content_filter", 0));
        final DiscordLocale locale = DiscordLocale.from(guildObject.getString("preferred_locale", "en-US"));
        final Timeout afkTimeout = Timeout.fromKey(guildObject.getInt("afk_timeout", 0));
        final DataArray roleArray = guildObject.getArray("roles");
        final DataArray channelsArray = guildObject.getArray("channels");
        final long afkChannelId = guildObject.getLong("afk_channel_id", -1L);
        final long systemChannelId = guildObject.getLong("system_channel_id", -1L);

        final List<TemplateRole> roles = new ArrayList<>();
        for (int i = 0; i < roleArray.length(); i++)
        {
               DataObject obj = roleArray.getObject(i);
               final long roleId = obj.getLong("id");
               final String roleName = obj.getString("name");
               final int roleColor = obj.getInt("color");
               final boolean hoisted = obj.getBoolean("hoist");
               final boolean mentionable = obj.getBoolean("mentionable");
               final long rawPermissions = obj.getLong("permissions");
               roles.add(new TemplateRole(roleId, roleName, roleColor == 0 ? Role.DEFAULT_COLOR_RAW : roleColor, hoisted, mentionable, rawPermissions));
        }

        final List<TemplateChannel> channels = new ArrayList<>();
        for (int i = 0; i < channelsArray.length(); i++)
        {
            DataObject obj = channelsArray.getObject(i);
            final long channelId = obj.getLong("id");
            final int type = obj.getInt("type");
            final ChannelType channelType = ChannelType.fromId(type);
            final String channelName = obj.getString("name");
            final String topic = obj.getString("topic", null);
            final int rawPosition = obj.getInt("position");
            final long parentId = obj.getLong("parent_id", -1);

            final boolean nsfw = obj.getBoolean("nsfw");
            final int slowmode = obj.getInt("rate_limit_per_user");

            final int bitrate = obj.getInt("bitrate");
            final int userLimit = obj.getInt("user_limit");

            final List<TemplateChannel.PermissionOverride> permissionOverrides = new ArrayList<>();
            DataArray overrides = obj.getArray("permission_overwrites");

            for (int j = 0; j < overrides.length(); j++)
            {
                DataObject overrideObj = overrides.getObject(j);
                final long overrideId = overrideObj.getLong("id");
                final long allow = overrideObj.getLong("allow");
                final long deny = overrideObj.getLong("deny");
                permissionOverrides.add(new TemplateChannel.PermissionOverride(overrideId, allow, deny));
            }
            channels.add(new TemplateChannel(channelId, channelType, channelName, topic, rawPosition, parentId, type == 5, permissionOverrides, nsfw,
                    slowmode, bitrate, userLimit));
        }

        TemplateChannel afkChannel = channels.stream().filter(templateChannel -> templateChannel.getIdLong() == afkChannelId)
                .findFirst().orElse(null);
        TemplateChannel systemChannel = channels.stream().filter(templateChannel -> templateChannel.getIdLong() == systemChannelId)
                .findFirst().orElse(null);

        final TemplateGuild guild = new TemplateGuild(guildId, guildName, guildDescription, guildIconId, guildVerificationLevel, notificationLevel, explicitContentLevel, locale,
                afkTimeout, afkChannel, systemChannel, roles, channels);

        final boolean synced = !object.getBoolean("is_dirty", false);

        return new Template(getJDA(), code, name, description,
                uses, creator, createdAt, updatedAt,
                guild, synced);
    }

    public ApplicationInfo createApplicationInfo(DataObject object)
    {
        final String description = object.getString("description");
        final String termsOfServiceUrl = object.getString("terms_of_service_url", null);
        final String privacyPolicyUrl = object.getString("privacy_policy_url", null);
        final boolean doesBotRequireCodeGrant = object.getBoolean("bot_require_code_grant");
        final String iconId = object.getString("icon", null);
        final long id = object.getUnsignedLong("id");
        final long flags = object.getUnsignedLong("flags", 0);
        final String name = object.getString("name");
        final boolean isBotPublic = object.getBoolean("bot_public");
        final User owner = createUser(object.getObject("owner"));
        final ApplicationTeam team = !object.isNull("team") ? createApplicationTeam(object.getObject("team")) : null;
        final String customAuthUrl = object.getString("custom_install_url", null);
        final List<String> tags = object.optArray("tags").orElseGet(DataArray::empty)
                    .stream(DataArray::getString)
                    .collect(Collectors.toList());

        final Optional<DataObject> installParams = object.optObject("install_params");

        final long defaultAuthUrlPerms = installParams.map(o -> o.getLong("permissions"))
                    .orElse(0L);

        final List<String> defaultAuthUrlScopes = installParams.map(obj -> obj.getArray("scopes")
                            .stream(DataArray::getString)
                            .collect(Collectors.toList()))
                    .orElse(Collections.emptyList());

        return new ApplicationInfoImpl(getJDA(), description, doesBotRequireCodeGrant, iconId, id, flags, isBotPublic, name,
                termsOfServiceUrl, privacyPolicyUrl, owner, team, tags, customAuthUrl, defaultAuthUrlPerms, defaultAuthUrlScopes);
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
        final long userId = entryJson.getLong("user_id", 0);
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

        return new AuditLogEntry(type, typeKey, id, userId, targetId, guild, user, webhook, reason, changeMap, optionMap);
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
