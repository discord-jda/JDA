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

package net.dv8tion.jda.internal.handle;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.Region;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IPermissionHolder;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelFlag;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.attribute.IPostContainer;
import net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.channel.forum.ForumTagAddEvent;
import net.dv8tion.jda.api.events.channel.forum.ForumTagRemoveEvent;
import net.dv8tion.jda.api.events.channel.forum.update.ForumTagUpdateEmojiEvent;
import net.dv8tion.jda.api.events.channel.forum.update.ForumTagUpdateModeratedEvent;
import net.dv8tion.jda.api.events.channel.forum.update.ForumTagUpdateNameEvent;
import net.dv8tion.jda.api.events.channel.update.*;
import net.dv8tion.jda.api.events.guild.override.PermissionOverrideCreateEvent;
import net.dv8tion.jda.api.events.guild.override.PermissionOverrideDeleteEvent;
import net.dv8tion.jda.api.events.guild.override.PermissionOverrideUpdateEvent;
import net.dv8tion.jda.api.events.thread.ThreadHiddenEvent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.entities.ForumTagImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.PermissionOverrideImpl;
import net.dv8tion.jda.internal.entities.channel.concrete.ForumChannelImpl;
import net.dv8tion.jda.internal.entities.channel.middleman.AbstractGuildChannelImpl;
import net.dv8tion.jda.internal.entities.channel.mixin.attribute.*;
import net.dv8tion.jda.internal.entities.channel.mixin.middleman.AudioChannelMixin;
import net.dv8tion.jda.internal.entities.channel.mixin.middleman.MessageChannelMixin;
import net.dv8tion.jda.internal.requests.WebSocketClient;
import net.dv8tion.jda.internal.utils.UnlockHook;
import net.dv8tion.jda.internal.utils.cache.ChannelCacheViewImpl;
import net.dv8tion.jda.internal.utils.cache.SortedSnowflakeCacheViewImpl;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("ConstantConditions")
public class ChannelUpdateHandler extends SocketHandler
{
    public ChannelUpdateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content)
    {
        ChannelType type = ChannelType.fromId(content.getInt("type"));
        if (type == ChannelType.GROUP)
        {
            WebSocketClient.LOG.warn("Ignoring CHANNEL_UPDATE for a group which we don't support");
            return null;
        }
        if (!content.isNull("guild_id"))
        {
            long guildId = content.getUnsignedLong("guild_id");
            if (getJDA().getGuildSetupController().isLocked(guildId))
                return guildId;
        }

        long channelId = content.getUnsignedLong("id");

        //We assume the CHANNEL_UPDATE was for a GuildChannel because PrivateChannels don't emit CHANNEL_UPDATE for 1:1 DMs, only Groups.
        AbstractGuildChannelImpl<?> channel = (AbstractGuildChannelImpl<?>) getJDA().getGuildChannelById(channelId);
        if (channel == null)
        {
            getJDA().getEventCache().cache(EventCache.Type.CHANNEL, channelId, responseNumber, allContent, this::handle);
            EventCache.LOG.debug("CHANNEL_UPDATE attempted to update a channel that does not exist. JSON: {}", content);
            return null;
        }

        //Detect if we changed the channel type at all and reconstruct the channel entity if needed
        channel = handleChannelTypeChange(channel, content, type);
        if (channel == null)
            return null;

        //Handle shared properties

        String oldName = channel.getName();
        String name = content.getString("name", oldName);
        if (!Objects.equals(oldName, name))
        {
            channel.setName(name);
            getJDA().handleEvent(
                new ChannelUpdateNameEvent(
                    getJDA(), responseNumber,
                    channel, oldName, name));
        }

        if (channel instanceof ITopicChannelMixin<?>)
            handleTopic((ITopicChannelMixin<?>) channel, content.getString("topic", null));

        if (channel instanceof ISlowmodeChannelMixin<?>)
            handleSlowmode((ISlowmodeChannelMixin<?>) channel, content.getInt("rate_limit_per_user", 0));

        if (channel instanceof IAgeRestrictedChannelMixin<?>)
            handleNsfw((IAgeRestrictedChannelMixin<?>) channel, content.getBoolean("nsfw"));

        if (channel instanceof ICategorizableChannelMixin<?>)
            handleParentCategory((ICategorizableChannelMixin<?>) channel, content.getUnsignedLong("parent_id", 0));

        if (channel instanceof IPositionableChannelMixin<?>)
            handlePosition((IPositionableChannelMixin<?>) channel, content.getInt("position", 0));

        if (channel instanceof IThreadContainerMixin<?>)
            handleThreadContainer((IThreadContainerMixin<?>) channel, content);

        if (channel instanceof AudioChannelMixin<?>)
            handleAudioChannel((AudioChannelMixin<?>) channel, content);

        if (channel instanceof IPostContainerMixin<?>)
            handlePostContainer((IPostContainerMixin<?>) channel, content);

        //Handle concrete type specific properties

        switch (type)
        {
            case FORUM:
                ForumChannelImpl forumChannel = (ForumChannelImpl) channel;

                int layout = content.getInt("default_forum_layout", ((ForumChannelImpl) channel).getRawLayout());
                int oldLayout = forumChannel.getRawLayout();

                if (oldLayout != layout)
                {
                    forumChannel.setDefaultLayout(layout);
                    getJDA().handleEvent(
                            new ChannelUpdateDefaultLayoutEvent(
                                    getJDA(), responseNumber,
                                    forumChannel, ForumChannel.Layout.fromKey(oldLayout), ForumChannel.Layout.fromKey(layout)));
                }
                break;
            case VOICE:
            case TEXT:
            case NEWS:
            case STAGE:
            case CATEGORY:
                break;
            default:
                WebSocketClient.LOG.debug("CHANNEL_UPDATE provided an unrecognized channel type JSON: {}", content);
        }

        DataArray permOverwrites = content.getArray("permission_overwrites");
        applyPermissions((IPermissionContainerMixin<?>) channel, permOverwrites);

        boolean hasAccessToChannel = channel.getGuild().getSelfMember().hasPermission(channel, Permission.VIEW_CHANNEL);
        if (channel instanceof IThreadContainer && !hasAccessToChannel)
            handleHideChildThreads((IThreadContainer) channel);

        return null;
    }

    private AbstractGuildChannelImpl<?> handleChannelTypeChange(AbstractGuildChannelImpl<?> channel, DataObject content, ChannelType newChannelType)
    {
        if (channel.getType() == newChannelType)
            return channel;

        EntityBuilder builder = getJDA().getEntityBuilder();
        GuildImpl guild = (GuildImpl) channel.getGuild();

        ChannelType oldType = channel.getType();

        EnumSet<ChannelType> expectedTypes = EnumSet.complementOf(EnumSet.of(
            ChannelType.PRIVATE,
            ChannelType.GROUP,
            ChannelType.GUILD_NEWS_THREAD,
            ChannelType.GUILD_PRIVATE_THREAD,
            ChannelType.GUILD_PUBLIC_THREAD,
            ChannelType.UNKNOWN
        ));

        if (!expectedTypes.contains(oldType) || !expectedTypes.contains(newChannelType))
        {
            WebSocketClient.LOG.warn("Unexpected channel type change {}->{}, discarding from cache.", channel.getType().getId(), content.getInt("type"));
            guild.uncacheChannel(channel, false);
            return null;
        }

        guild.uncacheChannel(channel, true);
        Channel newChannel = builder.createGuildChannel(guild, content);

        if (channel instanceof IThreadContainer)
        {
            if (newChannel instanceof IThreadContainer)
            {
                // Refresh thread parents to avoid keeping strong references to parent channel
                guild.getThreadChannelCache().forEachUnordered(ThreadChannel::getParentChannel);
            }
            else
            {
                // Change introduced dangling thread channels (with no parent)
                WebSocketClient.LOG.error("ThreadContainer channel transitioned into type that is not ThreadContainer? {} -> {}", channel.getType(), newChannel.getType());
            }
        }

        if (newChannel instanceof MessageChannelMixin<?> && channel instanceof MessageChannel)
        {
            long latestMessageIdLong = ((MessageChannel) channel).getLatestMessageIdLong();
            ((MessageChannelMixin<?>) channel).setLatestMessageIdLong(latestMessageIdLong);
        }

        getJDA().handleEvent(
            new ChannelUpdateTypeEvent(
                getJDA(), responseNumber,
                newChannel, oldType, newChannelType));

        return channel;
    }

    private void applyPermissions(IPermissionContainerMixin<?> channel, DataArray permOverwrites)
    {
        TLongObjectMap<PermissionOverride> currentOverrides = new TLongObjectHashMap<>(channel.getPermissionOverrideMap());
        List<IPermissionHolder> changed = new ArrayList<>(currentOverrides.size());
        Guild guild = channel.getGuild();
        for (int i = 0; i < permOverwrites.length(); i++)
        {
            DataObject overrideJson = permOverwrites.getObject(i);
            long id = overrideJson.getUnsignedLong("id", 0);
            if (handlePermissionOverride(currentOverrides.remove(id), overrideJson, id, channel))
                addPermissionHolder(changed, guild, id);
        }

        currentOverrides.forEachValue(override -> {
            channel.getPermissionOverrideMap().remove(override.getIdLong());
            addPermissionHolder(changed, guild, override.getIdLong());
            api.handleEvent(
                new PermissionOverrideDeleteEvent(
                    api, responseNumber,
                    channel, override));
            return true;
        });
    }

    private void addPermissionHolder(List<IPermissionHolder> changed, Guild guild, long id)
    {
        IPermissionHolder holder = guild.getRoleById(id);
        if (holder == null)
            holder = guild.getMemberById(id);
        if (holder != null) // Members might not be cached
            changed.add(holder);
    }

    // True => override status changed (created/deleted/updated)
    // False => nothing changed, ignore
    private boolean handlePermissionOverride(PermissionOverride currentOverride, DataObject override, long overrideId, IPermissionContainerMixin<?> channel)
    {
        final long allow = override.getLong("allow");
        final long deny = override.getLong("deny");
        final int type = override.getInt("type");
        final boolean isRole = type == 0;
        if (!isRole)
        {
            if (type != 1)
            {
                EntityBuilder.LOG.debug("Ignoring unknown invite of type '{}'. JSON: {}", type, override);
                return false;
            }
            else if (!api.isCacheFlagSet(CacheFlag.MEMBER_OVERRIDES) && overrideId != api.getSelfUser().getIdLong())
            {
                return false;
            }
        }

        if (currentOverride != null) // Permissions were updated?
        {
            long oldAllow = currentOverride.getAllowedRaw();
            long oldDeny = currentOverride.getDeniedRaw();
            PermissionOverrideImpl impl = (PermissionOverrideImpl) currentOverride;
            if (oldAllow == allow && oldDeny == deny)
                return false;

            if (overrideId == channel.getGuild().getIdLong() && (allow | deny) == 0L)
            {
                // We delete empty overrides for the @everyone role because that's what the client also does, otherwise our sync checks don't work!
                channel.getPermissionOverrideMap().remove(overrideId);
                api.handleEvent(
                    new PermissionOverrideDeleteEvent(
                        api, responseNumber,
                        channel, currentOverride));
                return true;
            }

            impl.setAllow(allow);
            impl.setDeny(deny);
            api.handleEvent(
                new PermissionOverrideUpdateEvent(
                    api, responseNumber,
                    channel, currentOverride, oldAllow, oldDeny));
        }
        else // New override?
        {
            // Empty @everyone overrides should be treated as not existing at all
            if (overrideId == channel.getGuild().getIdLong() && (allow | deny) == 0L)
                return false;
            PermissionOverrideImpl impl;
            currentOverride = impl = new PermissionOverrideImpl(channel, overrideId, isRole);
            impl.setAllow(allow);
            impl.setDeny(deny);
            channel.getPermissionOverrideMap().put(overrideId, currentOverride);
            api.handleEvent(
                new PermissionOverrideCreateEvent(
                    api, responseNumber,
                    channel, currentOverride));
        }

        return true;
    }

    private void handleHideChildThreads(IThreadContainer channel)
    {
        List<ThreadChannel> threads = channel.getThreadChannels();
        if (threads.isEmpty())
            return;

        for (ThreadChannel thread : threads)
        {
            GuildImpl guild = (GuildImpl) channel.getGuild();
            ChannelCacheViewImpl<GuildChannel> guildThreadView = guild.getChannelView();
            ChannelCacheViewImpl<Channel> threadView = getJDA().getChannelsView();
            try (
                    UnlockHook vlock = guildThreadView.writeLock();
                    UnlockHook jlock = threadView.writeLock())
            {
                //TODO-threads: When we figure out how member chunking is going to work for thread related members
                // we may need to revisit this to ensure they kicked out of the cache if needed.
                threadView.remove(thread.getType(), thread.getIdLong());
                guildThreadView.remove(thread);
            }
        }

        //Fire these events outside the write locks
        for (ThreadChannel thread : threads)
        {
            api.handleEvent(new ThreadHiddenEvent(api, responseNumber, thread));
        }
    }

    private void handleTagsUpdate(IPostContainerMixin<?> channel, DataArray tags)
    {
        if (!api.isCacheFlagSet(CacheFlag.FORUM_TAGS))
            return;
        EntityBuilder builder = api.getEntityBuilder();

        SortedSnowflakeCacheViewImpl<ForumTag> view = channel.getAvailableTagCache();

        try (UnlockHook hook = view.writeLock())
        {
            TLongObjectMap<ForumTag> cache = view.getMap();
            TLongSet removedTags = new TLongHashSet(cache.keySet());

            for (int i = 0; i < tags.length(); i++)
            {
                DataObject tagJson = tags.getObject(i);
                long id = tagJson.getUnsignedLong("id");
                if (removedTags.remove(id))
                {
                    ForumTagImpl impl = (ForumTagImpl) cache.get(id);
                    if (impl == null)
                        continue;

                    String name = tagJson.getString("name");
                    boolean moderated = tagJson.getBoolean("moderated");

                    String oldName = impl.getName();
                    EmojiUnion oldEmoji = impl.getEmoji();

                    impl.setEmoji(tagJson);

                    impl.setPosition(i);
                    if (!Objects.equals(oldEmoji, impl.getEmoji()))
                    {
                        api.handleEvent(new ForumTagUpdateEmojiEvent(api, responseNumber, channel, impl, oldEmoji));
                    }
                    if (!name.equals(oldName))
                    {
                        impl.setName(name);
                        api.handleEvent(new ForumTagUpdateNameEvent(api, responseNumber, channel, impl, oldName));
                    }
                    if (moderated != impl.isModerated())
                    {
                        impl.setModerated(moderated);
                        api.handleEvent(new ForumTagUpdateModeratedEvent(api, responseNumber, channel, impl, moderated));
                    }
                }
                else
                {
                    ForumTag tag = builder.createForumTag(channel, tagJson, i);
                    cache.put(id, tag);
                    api.handleEvent(new ForumTagAddEvent(api, responseNumber, channel, tag));
                }
            }

            removedTags.forEach(id -> {
                ForumTag tag = cache.remove(id);
                if (tag != null)
                    api.handleEvent(new ForumTagRemoveEvent(api, responseNumber, channel, tag));
                return true;
            });
        }
    }

    private void handleTopic(ITopicChannelMixin<?> channel, String topic)
    {
        String oldTopic = channel.getTopic();
        if (Objects.equals(oldTopic, topic))
            return;

        channel.setTopic(topic);
        api.handleEvent(
            new ChannelUpdateTopicEvent(
                api, responseNumber,
                channel, oldTopic, topic));
    }

    private void handleSlowmode(ISlowmodeChannelMixin<?> channel, int slowmode)
    {
        int oldSlowmode = channel.getSlowmode();
        if (oldSlowmode == slowmode)
            return;

        channel.setSlowmode(slowmode);
        api.handleEvent(
            new ChannelUpdateSlowmodeEvent(
                api, responseNumber,
                channel, oldSlowmode, slowmode));
    }

    private void handleNsfw(IAgeRestrictedChannelMixin<?> channel, boolean nsfw)
    {
        boolean oldNsfw = channel.isNSFW();
        if (oldNsfw == nsfw)
            return;

        channel.setNSFW(nsfw);
        api.handleEvent(
                new ChannelUpdateNSFWEvent(
                        api, responseNumber,
                        channel, oldNsfw, nsfw));
    }

    private void handleParentCategory(ICategorizableChannelMixin<?> channel, long parentId)
    {
        long oldParentId = channel.getParentCategoryIdLong();
        if (oldParentId == parentId)
            return;

        Category oldParent = channel.getParentCategory();
        channel.setParentCategory(parentId);
        Category newParent = channel.getParentCategory();

        api.handleEvent(
            new ChannelUpdateParentEvent(
                api, responseNumber,
                channel, oldParent, newParent));
    }

    private void handlePosition(IPositionableChannelMixin<?> channel, int position)
    {
        int oldPosition = channel.getPositionRaw();
        if (oldPosition == position)
            return;

        channel.setPosition(position);
        api.handleEvent(
            new ChannelUpdatePositionEvent(
                api, responseNumber,
                channel, oldPosition, position));
    }

    private void handleThreadContainer(IThreadContainerMixin<?> channel, DataObject content)
    {
        int oldDefaultThreadSlowmode = channel.getDefaultThreadSlowmode();
        int defaultThreadSlowmode = content.getInt("default_thread_rate_limit_per_user", 0);
        if (oldDefaultThreadSlowmode != defaultThreadSlowmode)
        {
            channel.setDefaultThreadSlowmode(defaultThreadSlowmode);
            api.handleEvent(
                new ChannelUpdateDefaultThreadSlowmodeEvent(
                    api, responseNumber,
                    channel, oldDefaultThreadSlowmode, defaultThreadSlowmode));
        }
    }

    private void handleAudioChannel(AudioChannelMixin<?> channel, DataObject content)
    {
        int oldBitrate = channel.getBitrate();
        int bitrate = content.getInt("bitrate");

        if (oldBitrate != bitrate)
        {
            channel.setBitrate(bitrate);
            api.handleEvent(
                new ChannelUpdateBitrateEvent(
                    api, responseNumber,
                    channel, oldBitrate, bitrate));
        }

        int userLimit = content.getInt("user_limit");
        int oldLimit = channel.getUserLimit();

        if (oldLimit != userLimit)
        {
            channel.setUserLimit(userLimit);
            getJDA().handleEvent(
                new ChannelUpdateUserLimitEvent(
                    getJDA(), responseNumber,
                    channel, oldLimit, userLimit));
        }

        String oldRegion = channel.getRegionRaw();
        String regionRaw = content.getString("rtc_region", null);

        if (!Objects.equals(oldRegion, regionRaw))
        {
            channel.setRegion(regionRaw);
            api.handleEvent(
                new ChannelUpdateRegionEvent(
                    api, responseNumber,
                    channel, Region.fromKey(oldRegion), Region.fromKey(regionRaw)));
        }
    }

    private void handlePostContainer(IPostContainerMixin<?> channel, DataObject content)
    {
        content.optArray("available_tags").ifPresent(
            array -> handleTagsUpdate(channel, array)
        );

        EmojiUnion defaultReaction =  content.optObject("default_reaction_emoji")
            .map(json -> EntityBuilder.createEmoji(json, "emoji_name", "emoji_id"))
            .orElse(null);
        EmojiUnion oldDefaultReaction = channel.getDefaultReaction();

        if (!Objects.equals(oldDefaultReaction, defaultReaction))
        {
            channel.setDefaultReaction(content.optObject("default_reaction_emoji").orElse(null));
            getJDA().handleEvent(
                    new ChannelUpdateDefaultReactionEvent(
                            getJDA(), responseNumber,
                            channel, oldDefaultReaction, defaultReaction));
        }


        int sortOrder = content.getInt("default_sort_order", channel.getRawSortOrder());
        int oldSortOrder = channel.getRawSortOrder();

        if (oldSortOrder != sortOrder)
        {
            channel.setDefaultSortOrder(sortOrder);
            getJDA().handleEvent(
                new ChannelUpdateDefaultSortOrderEvent(
                    getJDA(), responseNumber,
                    channel, IPostContainer.SortOrder.fromKey(oldSortOrder)));
        }

        int newFlags = content.getInt("flags", 0);
        int oldFlags = channel.getRawFlags();

        if (oldFlags != newFlags)
        {
            channel.setFlags(newFlags);
            getJDA().handleEvent(
                new ChannelUpdateFlagsEvent(
                    getJDA(), responseNumber,
                    channel, ChannelFlag.fromRaw(oldFlags), ChannelFlag.fromRaw(newFlags)));
        }
    }
}
