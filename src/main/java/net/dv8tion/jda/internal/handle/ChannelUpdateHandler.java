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
import net.dv8tion.jda.api.entities.channel.ChannelFlag;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
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
import net.dv8tion.jda.internal.entities.*;
import net.dv8tion.jda.internal.entities.channel.concrete.*;
import net.dv8tion.jda.internal.entities.channel.mixin.attribute.IPermissionContainerMixin;
import net.dv8tion.jda.internal.requests.WebSocketClient;
import net.dv8tion.jda.internal.utils.UnlockHook;
import net.dv8tion.jda.internal.utils.cache.SnowflakeCacheViewImpl;
import net.dv8tion.jda.internal.utils.cache.SortedSnowflakeCacheViewImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChannelUpdateHandler extends SocketHandler
{
    public ChannelUpdateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content)
    {
        final ChannelType type = ChannelType.fromId(content.getInt("type"));
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

        final long channelId = content.getLong("id");
        final long parentId = content.isNull("parent_id") ? 0 : content.getLong("parent_id");
        final int position = content.getInt("position");
        final int flags = content.getInt("flags", 0);
        final String name = content.getString("name");
        final boolean nsfw = content.getBoolean("nsfw");
        final int defaultThreadSlowmode = content.getInt("default_thread_rate_limit_per_user", 0);
        final int slowmode = content.getInt("rate_limit_per_user", 0);
        final DataArray permOverwrites = content.getArray("permission_overwrites");

        //We assume the CHANNEL_UPDATE was for a GuildChannel because PrivateChannels don't emit CHANNEL_UPDATE for 1:1 DMs, only Groups.
        GuildChannel channel = getJDA().getGuildChannelById(channelId);
        if (channel == null)
        {
            getJDA().getEventCache().cache(EventCache.Type.CHANNEL, channelId, responseNumber, allContent, this::handle);
            EventCache.LOG.debug("CHANNEL_UPDATE attempted to update a channel that does not exist. JSON: {}", content);
            return null;
        }

        //Detect if we changed the channel type at all and reconstruct the channel entity if needed
        channel = handleChannelTypeChange(channel, content, type);

        switch (type)
        {
            case TEXT:
            {
                final String topic = content.getString("topic", null);

                TextChannelImpl textChannel = (TextChannelImpl) channel;

                //If any properties changed, update the values and fire the proper events.
                final long oldParentId = textChannel.getParentCategoryIdLong();
                final String oldName = textChannel.getName();
                final String oldTopic = textChannel.getTopic();
                final int oldPosition = textChannel.getPositionRaw();
                final boolean oldNsfw = textChannel.isNSFW();
                final int oldSlowmode = textChannel.getSlowmode();
                final int oldDefaultThreadSlowmode = textChannel.getDefaultThreadSlowmode();
                if (!Objects.equals(oldName, name))
                {
                    textChannel.setName(name);
                    getJDA().handleEvent(
                            new ChannelUpdateNameEvent(
                                    getJDA(), responseNumber,
                                    textChannel, oldName, name));
                }
                if (oldParentId != parentId)
                {
                    final Category oldParent = textChannel.getParentCategory();
                    textChannel.setParentCategory(parentId);
                    getJDA().handleEvent(
                           new ChannelUpdateParentEvent(
                               getJDA(), responseNumber,
                               textChannel, oldParent, textChannel.getParentCategory()));
                }
                if (!Objects.equals(oldTopic, topic))
                {
                    textChannel.setTopic(topic);
                    getJDA().handleEvent(
                            new ChannelUpdateTopicEvent(
                                    getJDA(), responseNumber,
                                    textChannel, oldTopic, topic));
                }
                if (oldPosition != position)
                {
                    textChannel.setPosition(position);
                    getJDA().handleEvent(
                            new ChannelUpdatePositionEvent(
                                    getJDA(), responseNumber,
                                    textChannel, oldPosition, position));
                }
                if (oldNsfw != nsfw)
                {
                    textChannel.setNSFW(nsfw);
                    getJDA().handleEvent(
                            new ChannelUpdateNSFWEvent(
                                    getJDA(), responseNumber,
                                    textChannel, oldNsfw, nsfw));
                }
                if (oldSlowmode != slowmode)
                {
                    textChannel.setSlowmode(slowmode);
                    getJDA().handleEvent(
                            new ChannelUpdateSlowmodeEvent(
                                    getJDA(), responseNumber,
                                    textChannel, oldSlowmode, slowmode));
                }
                if (oldDefaultThreadSlowmode != defaultThreadSlowmode)
                {
                    textChannel.setDefaultThreadSlowmode(defaultThreadSlowmode);
                    getJDA().handleEvent(
                            new ChannelUpdateDefaultThreadSlowmodeEvent(
                                    getJDA(), responseNumber,
                                    textChannel, oldDefaultThreadSlowmode, defaultThreadSlowmode));
                }
                break;
            }
            case FORUM:
            {
                final String topic = content.getString("topic", null);
                final EmojiUnion defaultReaction = content.optObject("default_reaction_emoji")
                        .map(json -> {
                            json.opt("emoji_id").ifPresent(id -> json.put("id", id));
                            json.opt("emoji_name").ifPresent(n -> json.put("name", n));
                            return EntityBuilder.createEmoji(json);
                        })
                        .orElse(null);

                ForumChannelImpl forumChannel = (ForumChannelImpl) channel;
                content.optArray("available_tags").ifPresent(array -> handleTagsUpdate(forumChannel, array));
//                int sortOrder = content.getInt("default_sort_order", ((ForumChannelImpl) channel).getRawSortOrder());

                //If any properties changed, update the values and fire the proper events.
                final long oldParentId = forumChannel.getParentCategoryIdLong();
                final String oldName = forumChannel.getName();
                final String oldTopic = forumChannel.getTopic();
                final int oldPosition = forumChannel.getPositionRaw();
                final boolean oldNsfw = forumChannel.isNSFW();
                final int oldSlowmode = forumChannel.getSlowmode();
                final int oldDefaultThreadSlowmode = forumChannel.getDefaultThreadSlowmode();
                final int oldFlags = forumChannel.getRawFlags();
//                final int oldSortOrder = forumChannel.getRawSortOrder();
                final EmojiUnion oldDefaultReaction = forumChannel.getDefaultReaction();

                if (!Objects.equals(oldName, name))
                {
                    forumChannel.setName(name);
                    getJDA().handleEvent(
                            new ChannelUpdateNameEvent(
                                    getJDA(), responseNumber,
                                    forumChannel, oldName, name));
                }
                if (oldParentId != parentId)
                {
                    final Category oldParent = forumChannel.getParentCategory();
                    forumChannel.setParentCategory(parentId);
                    getJDA().handleEvent(
                            new ChannelUpdateParentEvent(
                                    getJDA(), responseNumber,
                                    forumChannel, oldParent, forumChannel.getParentCategory()));
                }
                if (!Objects.equals(oldTopic, topic))
                {
                    forumChannel.setTopic(topic);
                    getJDA().handleEvent(
                            new ChannelUpdateTopicEvent(
                                    getJDA(), responseNumber,
                                    forumChannel, oldTopic, topic));
                }
                if (oldPosition != position)
                {
                    forumChannel.setPosition(position);
                    getJDA().handleEvent(
                            new ChannelUpdatePositionEvent(
                                    getJDA(), responseNumber,
                                    forumChannel, oldPosition, position));
                }
                if (oldNsfw != nsfw)
                {
                    forumChannel.setNSFW(nsfw);
                    getJDA().handleEvent(
                            new ChannelUpdateNSFWEvent(
                                    getJDA(), responseNumber,
                                    forumChannel, oldNsfw, nsfw));
                }
                if (oldSlowmode != slowmode)
                {
                    forumChannel.setSlowmode(slowmode);
                    getJDA().handleEvent(
                            new ChannelUpdateSlowmodeEvent(
                                    getJDA(), responseNumber,
                                    forumChannel, oldSlowmode, slowmode));
                }
                if (oldDefaultThreadSlowmode != defaultThreadSlowmode)
                {
                    forumChannel.setDefaultThreadSlowmode(defaultThreadSlowmode);
                    getJDA().handleEvent(
                            new ChannelUpdateDefaultThreadSlowmodeEvent(
                                    getJDA(), responseNumber,
                                    forumChannel, oldDefaultThreadSlowmode, defaultThreadSlowmode));
                }
                if (oldFlags != flags)
                {
                    forumChannel.setFlags(flags);
                    getJDA().handleEvent(
                            new ChannelUpdateFlagsEvent(
                                    getJDA(), responseNumber,
                                    forumChannel, ChannelFlag.fromRaw(oldFlags), ChannelFlag.fromRaw(flags)));
                }
                if (!Objects.equals(oldDefaultReaction, defaultReaction))
                {
                    forumChannel.setDefaultReaction(content.optObject("default_reaction_emoji").orElse(null));
                    getJDA().handleEvent(
                            new ChannelUpdateDefaultReactionEvent(
                                    getJDA(), responseNumber,
                                    forumChannel, oldDefaultReaction, defaultReaction));
                }
//                if (oldSortOrder != sortOrder)
//                {
//                    forumChannel.setDefaultSortOrder(sortOrder);
//                    getJDA().handleEvent(
//                            new ChannelUpdateDefaultSortOrderEvent(
//                                    getJDA(), responseNumber,
//                                    forumChannel, ForumChannel.SortOrder.fromKey(oldSortOrder), ForumChannel.SortOrder.fromKey(sortOrder)));
//                }
                break;
            }
            case NEWS:
            {
                final String topic = content.getString("topic", null);

                NewsChannelImpl newsChannel = (NewsChannelImpl) channel;

                //If any properties changed, update the values and fire the proper events.
                final long oldParentId = newsChannel.getParentCategoryIdLong();
                final String oldName = newsChannel.getName();
                final String oldTopic = newsChannel.getTopic();
                final int oldPosition = newsChannel.getPositionRaw();
                final boolean oldNsfw = newsChannel.isNSFW();
                if (!Objects.equals(oldName, name))
                {
                    newsChannel.setName(name);
                    getJDA().handleEvent(
                            new ChannelUpdateNameEvent(
                                    getJDA(), responseNumber,
                                    newsChannel, oldName, name));
                }
                if (oldParentId != parentId)
                {
                    final Category oldParent = newsChannel.getParentCategory();
                    newsChannel.setParentCategory(parentId);
                    getJDA().handleEvent(
                            new ChannelUpdateParentEvent(
                                    getJDA(), responseNumber,
                                    newsChannel, oldParent, newsChannel.getParentCategory()));
                }
                if (!Objects.equals(oldTopic, topic))
                {
                    newsChannel.setTopic(topic);
                    getJDA().handleEvent(
                            new ChannelUpdateTopicEvent(
                                    getJDA(), responseNumber,
                                    newsChannel, oldTopic, topic));
                }
                if (oldPosition != position)
                {
                    newsChannel.setPosition(position);
                    getJDA().handleEvent(
                            new ChannelUpdatePositionEvent(
                                    getJDA(), responseNumber,
                                    newsChannel, oldPosition, position));
                }

                if (oldNsfw != nsfw)
                {
                    newsChannel.setNSFW(nsfw);
                    getJDA().handleEvent(
                            new ChannelUpdateNSFWEvent(
                                    getJDA(), responseNumber,
                                    newsChannel, oldNsfw, nsfw));
                }
                break;
            }
            case VOICE:
            {
                final int userLimit = content.getInt("user_limit");
                final int bitrate = content.getInt("bitrate");
                final String regionRaw = content.getString("rtc_region", null);

                VoiceChannelImpl voiceChannel = (VoiceChannelImpl) channel;

                //If any properties changed, update the values and fire the proper events.
                final long oldParentId = voiceChannel.getParentCategoryIdLong();
                final String oldName = voiceChannel.getName();
                final String oldRegionRaw = voiceChannel.getRegionRaw();
                final int oldPosition = voiceChannel.getPositionRaw();
                final int oldLimit = voiceChannel.getUserLimit();
                final int oldBitrate = voiceChannel.getBitrate();
                final boolean oldNsfw = voiceChannel.isNSFW();
                if (!Objects.equals(oldName, name))
                {
                    voiceChannel.setName(name);
                    getJDA().handleEvent(
                            new ChannelUpdateNameEvent(
                                    getJDA(), responseNumber,
                                    voiceChannel, oldName, name));
                }
                if (!Objects.equals(oldRegionRaw, regionRaw))
                {
                    final Region oldRegion = Region.fromKey(oldRegionRaw);
                    voiceChannel.setRegion(regionRaw);
                    getJDA().handleEvent(
                            new ChannelUpdateRegionEvent(
                                    getJDA(), responseNumber,
                                    voiceChannel, oldRegion, voiceChannel.getRegion()));
                }
                if (oldParentId != parentId)
                {
                    final Category oldParent = voiceChannel.getParentCategory();
                    voiceChannel.setParentCategory(parentId);
                    getJDA().handleEvent(
                            new ChannelUpdateParentEvent(
                                    getJDA(), responseNumber,
                                    voiceChannel, oldParent, voiceChannel.getParentCategory()));
                }
                if (oldPosition != position)
                {
                    voiceChannel.setPosition(position);
                    getJDA().handleEvent(
                            new ChannelUpdatePositionEvent(
                                    getJDA(), responseNumber,
                                    voiceChannel, oldPosition, position));
                }
                if (oldLimit != userLimit)
                {
                    voiceChannel.setUserLimit(userLimit);
                    getJDA().handleEvent(
                            new ChannelUpdateUserLimitEvent(
                                    getJDA(), responseNumber,
                                    voiceChannel, oldLimit, userLimit));
                }
                if (oldBitrate != bitrate)
                {
                    voiceChannel.setBitrate(bitrate);
                    getJDA().handleEvent(
                            new ChannelUpdateBitrateEvent(
                                    getJDA(), responseNumber,
                                    voiceChannel, oldBitrate, bitrate));
                }
                if (oldNsfw != nsfw)
                {
                    voiceChannel.setNSFW(nsfw);
                    getJDA().handleEvent(
                            new ChannelUpdateNSFWEvent(
                                    getJDA(), responseNumber,
                                    voiceChannel, oldNsfw, nsfw));
                }

                break;
            }
            case STAGE:
            {
                final int bitrate = content.getInt("bitrate");
                final String regionRaw = content.getString("rtc_region", null);

                StageChannelImpl stageChannel = (StageChannelImpl) channel;

                //If any properties changed, update the values and fire the proper events.
                final long oldParentId = stageChannel.getParentCategoryIdLong();
                final String oldName = stageChannel.getName();
                final String oldRegionRaw = stageChannel.getRegionRaw();
                final int oldPosition = stageChannel.getPositionRaw();
                final int oldBitrate = stageChannel.getBitrate();
                if (!Objects.equals(oldName, name))
                {
                    stageChannel.setName(name);
                    getJDA().handleEvent(
                            new ChannelUpdateNameEvent(
                                    getJDA(), responseNumber,
                                    stageChannel, oldName, name));
                }
                if (!Objects.equals(oldRegionRaw, regionRaw))
                {
                    final Region oldRegion = Region.fromKey(oldRegionRaw);
                    stageChannel.setRegion(regionRaw);
                    getJDA().handleEvent(
                            new ChannelUpdateRegionEvent(
                                    getJDA(), responseNumber,
                                    stageChannel, oldRegion, stageChannel.getRegion()));
                }
                if (oldParentId != parentId)
                {
                    final Category oldParent = stageChannel.getParentCategory();
                    stageChannel.setParentCategory(parentId);
                    getJDA().handleEvent(
                            new ChannelUpdateParentEvent(
                                    getJDA(), responseNumber,
                                    stageChannel, oldParent, stageChannel.getParentCategory()));
                }
                if (oldPosition != position)
                {
                    stageChannel.setPosition(position);
                    getJDA().handleEvent(
                            new ChannelUpdatePositionEvent(
                                    getJDA(), responseNumber,
                                    stageChannel, oldPosition, position));
                }
                if (oldBitrate != bitrate)
                {
                    stageChannel.setBitrate(bitrate);
                    getJDA().handleEvent(
                            new ChannelUpdateBitrateEvent(
                                    getJDA(), responseNumber,
                                    stageChannel, oldBitrate, bitrate));
                }

                break;
            }
            case CATEGORY:
            {
                CategoryImpl category = (CategoryImpl) channel;

                final String oldName = category.getName();
                final int oldPosition = category.getPositionRaw();

                if (!Objects.equals(oldName, name))
                {
                    category.setName(name);
                    getJDA().handleEvent(
                            new ChannelUpdateNameEvent(
                                getJDA(), responseNumber,
                                category, oldName, name));
                }
                if (!Objects.equals(oldPosition, position))
                {
                    category.setPosition(position);
                    getJDA().handleEvent(
                            new ChannelUpdatePositionEvent(
                                getJDA(), responseNumber,
                                category, oldPosition, position));
                }

                break;
            }
            default:
                WebSocketClient.LOG.debug("CHANNEL_UPDATE provided an unrecognized channel type JSON: {}", content);
        }

        applyPermissions((IPermissionContainerMixin<?>) channel, permOverwrites);

        boolean hasAccessToChannel = channel.getGuild().getSelfMember().hasPermission(channel, Permission.VIEW_CHANNEL);
        if (channel instanceof IThreadContainer && !hasAccessToChannel)
            handleHideChildThreads((IThreadContainer) channel);

        return null;
    }

    private GuildChannel handleChannelTypeChange(GuildChannel channel, DataObject content, ChannelType newChannelType)
    {
        if (channel.getType() == newChannelType) {
            return channel;
        }

        EntityBuilder builder = getJDA().getEntityBuilder();
        GuildImpl guild = (GuildImpl) channel.getGuild();

        if (newChannelType == ChannelType.TEXT)
        {
            //This assumes that if we're moving to a TextChannel that we're transitioning from a NewsChannel
            NewsChannel newsChannel = (NewsChannel) channel;
            getJDA().getNewsChannelView().remove(newsChannel.getIdLong());
            guild.getNewsChannelView().remove(newsChannel.getIdLong());

            TextChannelImpl textChannel = (TextChannelImpl) builder.createTextChannel(guild, content, guild.getIdLong());

            //CHANNEL_UPDATE doesn't track last_message_id, so make sure to copy it over.
            textChannel.setLatestMessageIdLong(newsChannel.getLatestMessageIdLong());

            getJDA().handleEvent(
                new ChannelUpdateTypeEvent(
                    getJDA(), responseNumber,
                    textChannel, ChannelType.NEWS, ChannelType.TEXT));

            return textChannel;
        }

        if (newChannelType == ChannelType.NEWS)
        {
            //This assumes that if we're moving to a NewsChannel that we're transitioning from a TextChannel
            TextChannel textChannel = (TextChannel) channel;
            getJDA().getTextChannelsView().remove(textChannel.getIdLong());
            guild.getTextChannelsView().remove(textChannel.getIdLong());

            NewsChannelImpl newsChannel = (NewsChannelImpl) builder.createNewsChannel(guild, content, guild.getIdLong());

            //CHANNEL_UPDATE doesn't track last_message_id, so make sure to copy it over.
            newsChannel.setLatestMessageIdLong(textChannel.getLatestMessageIdLong());

            getJDA().handleEvent(
                new ChannelUpdateTypeEvent(
                    getJDA(), responseNumber,
                    newsChannel, ChannelType.TEXT, ChannelType.NEWS));

            return newsChannel;
        }

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
            SnowflakeCacheViewImpl<ThreadChannel>
                    guildThreadView = guild.getThreadChannelsView(),
                    threadView = getJDA().getThreadChannelsView();
            try (
                    UnlockHook vlock = guildThreadView.writeLock();
                    UnlockHook jlock = threadView.writeLock())
            {
                //TODO-threads: When we figure out how member chunking is going to work for thread related members
                // we may need to revisit this to ensure they kicked out of the cache if needed.
                threadView.getMap().remove(thread.getIdLong());
                guildThreadView.getMap().remove(thread.getIdLong());
            }
        }

        //Fire these events outside the write locks
        for (ThreadChannel thread : threads)
        {
            api.handleEvent(new ThreadHiddenEvent(api, responseNumber, thread));
        }
    }

    private void handleTagsUpdate(ForumChannelImpl channel, DataArray tags)
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
}
