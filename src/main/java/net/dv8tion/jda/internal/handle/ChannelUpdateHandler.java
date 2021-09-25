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
import net.dv8tion.jda.api.Region;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.channel.category.update.CategoryUpdatePermissionsEvent;
import net.dv8tion.jda.api.events.channel.store.update.StoreChannelUpdatePermissionsEvent;
import net.dv8tion.jda.api.events.channel.text.update.TextChannelUpdatePermissionsEvent;
import net.dv8tion.jda.api.events.channel.update.*;
import net.dv8tion.jda.api.events.channel.voice.update.VoiceChannelUpdatePermissionsEvent;
import net.dv8tion.jda.api.events.guild.override.PermissionOverrideCreateEvent;
import net.dv8tion.jda.api.events.guild.override.PermissionOverrideDeleteEvent;
import net.dv8tion.jda.api.events.guild.override.PermissionOverrideUpdateEvent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.*;
import net.dv8tion.jda.internal.requests.WebSocketClient;

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
        int rawType = content.getInt("type");
        boolean news = rawType == 5;
        ChannelType type = ChannelType.fromId(rawType);
        if (type == ChannelType.GROUP)
        {
            WebSocketClient.LOG.warn("Ignoring CHANNEL_UPDATE for a group which we don't support");
            return null;
        }

        final long channelId = content.getLong("id");
        final long parentId = content.isNull("parent_id") ? 0 : content.getLong("parent_id");
        final int position = content.getInt("position");
        final String name = content.getString("name");
        final boolean nsfw = content.getBoolean("nsfw");
        final int slowmode = content.getInt("rate_limit_per_user", 0);
        DataArray permOverwrites = content.getArray("permission_overwrites");
        switch (type)
        {
            case STORE:
            {
                StoreChannelImpl storeChannel = (StoreChannelImpl) getJDA().getStoreChannelById(channelId);
                if (storeChannel == null)
                {
                    getJDA().getEventCache().cache(EventCache.Type.CHANNEL, channelId, responseNumber, allContent, this::handle);
                    EventCache.LOG.debug("CHANNEL_UPDATE attempted to update a StoreChannel that does not exist. JSON: {}", content);
                    return null;
                }
                final String oldName = storeChannel.getName();
                final int oldPosition = storeChannel.getPositionRaw();

                if (!Objects.equals(oldName, name))
                {
                    storeChannel.setName(name);
                    getJDA().handleEvent(
                        new ChannelUpdateNameEvent(
                            getJDA(), responseNumber,
                            storeChannel, oldName, name
                        ));
                }
                if (!Objects.equals(oldPosition, position))
                {
                    storeChannel.setPosition(position);
                    getJDA().handleEvent(
                        new ChannelUpdatePositionEvent(
                            getJDA(), responseNumber,
                            storeChannel, oldPosition, position));
                }

                applyPermissions(storeChannel, permOverwrites);
                break;
            }
            case TEXT:
            {
                String topic = content.getString("topic", null);
                TextChannelImpl textChannel = (TextChannelImpl) getJDA().getTextChannelsView().get(channelId);
                if (textChannel == null)
                {
                    getJDA().getEventCache().cache(EventCache.Type.CHANNEL, channelId, responseNumber, allContent, this::handle);
                    EventCache.LOG.debug("CHANNEL_UPDATE attempted to update a TextChannel that does not exist. JSON: {}", content);
                    return null;
                }

                //If any properties changed, update the values and fire the proper events.
                final long oldParentId = textChannel.getParentCategoryIdLong();
                final String oldName = textChannel.getName();
                final String oldTopic = textChannel.getTopic();
                final int oldPosition = textChannel.getPositionRaw();
                final boolean oldNsfw = textChannel.isNSFW();
                final int oldSlowmode = textChannel.getSlowmode();
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
                    textChannel.setParent(parentId);
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

                //TODO-v5: Address this event as TextChannels no longer have isNews on them.
                //TODO-v5: This probably needs to be some form of ChannelUpdateTypeEvent
//                if (news != textChannel.isNews())
//                {
//                    textChannel.setNews(news);
//                    getJDA().handleEvent(
//                        new TextChannelUpdateNewsEvent(
//                            getJDA(), responseNumber,
//                            textChannel));
//                }

                applyPermissions(textChannel, permOverwrites);
                break;  //Finish the TextChannelUpdate case
            }
            case NEWS:
            {
                String topic = content.getString("topic", null);
                NewsChannelImpl newsChannel = (NewsChannelImpl) getJDA().getNewsChannelView().get(channelId);
                if (newsChannel == null)
                {
                    getJDA().getEventCache().cache(EventCache.Type.CHANNEL, channelId, responseNumber, allContent, this::handle);
                    EventCache.LOG.debug("CHANNEL_UPDATE attempted to update a NewsChannel that does not exist. JSON: {}", content);
                    return null;
                }

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
                    newsChannel.setParent(parentId);
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

                applyPermissions(newsChannel, permOverwrites);
                break;  //Finish the TextChannelUpdate case
            }
            case VOICE:
            {
                VoiceChannelImpl voiceChannel = (VoiceChannelImpl) getJDA().getVoiceChannelsView().get(channelId);
                int userLimit = content.getInt("user_limit");
                int bitrate = content.getInt("bitrate");
                final String regionRaw = content.getString("rtc_region", null);
                if (voiceChannel == null)
                {
                    getJDA().getEventCache().cache(EventCache.Type.CHANNEL, channelId, responseNumber, allContent, this::handle);
                    EventCache.LOG.debug("CHANNEL_UPDATE attempted to update a VoiceChannel that does not exist. JSON: {}", content);
                    return null;
                }
                //If any properties changed, update the values and fire the proper events.
                final long oldParentId = voiceChannel.getParentCategoryIdLong();
                final String oldName = voiceChannel.getName();
                final String oldRegionRaw = voiceChannel.getRegionRaw();
                final int oldPosition = voiceChannel.getPositionRaw();
                final int oldLimit = voiceChannel.getUserLimit();
                final int oldBitrate = voiceChannel.getBitrate();
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
                    voiceChannel.setParent(parentId);
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

                applyPermissions(voiceChannel, permOverwrites);
                break;  //Finish the VoiceChannelUpdate case
            }
            case STAGE:
            {
                StageChannelImpl stageChannel = (StageChannelImpl) getJDA().getStageChannelView().get(channelId);
                int bitrate = content.getInt("bitrate");
                final String regionRaw = content.getString("rtc_region", null);
                if (stageChannel == null)
                {
                    getJDA().getEventCache().cache(EventCache.Type.CHANNEL, channelId, responseNumber, allContent, this::handle);
                    EventCache.LOG.debug("CHANNEL_UPDATE attempted to update a StageChannel that does not exist. JSON: {}", content);
                    return null;
                }
                //TODO-v5: Restore these events for StageChannels once we decide how we're going to handle XChannelUpdateYEvent
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
                    stageChannel.setParent(parentId);
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

                applyPermissions(stageChannel, permOverwrites);
                break;  //Finish the StageChannelUpdate case
            }
            case CATEGORY:
            {
                CategoryImpl category = (CategoryImpl) getJDA().getCategoryById(channelId);
                if (category == null)
                {
                    getJDA().getEventCache().cache(EventCache.Type.CHANNEL, channelId, responseNumber, allContent, this::handle);
                    EventCache.LOG.debug("CHANNEL_UPDATE attempted to update a Category that does not exist. JSON: {}", content);
                    return null;
                }
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

                applyPermissions(category, permOverwrites);
                break;  //Finish the CategoryUpdate case
            }
            default:
                WebSocketClient.LOG.debug("CHANNEL_UPDATE provided an unrecognized channel type JSON: {}", content);
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    private void applyPermissions(AbstractChannelImpl<?,?> channel, DataArray permOverwrites)
    {
        TLongObjectMap<PermissionOverride> currentOverrides = new TLongObjectHashMap<>(channel.getOverrideMap());
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
            channel.getOverrideMap().remove(override.getIdLong());
            addPermissionHolder(changed, guild, override.getIdLong());
            api.handleEvent(
                new PermissionOverrideDeleteEvent(
                    api, responseNumber,
                    channel, override));
            return true;
        });

        if (changed.isEmpty())
            return;
        switch (channel.getType())
        {
        case CATEGORY:
            api.handleEvent(
                new CategoryUpdatePermissionsEvent(
                    api, responseNumber,
                    (Category) channel, changed));
            break;
        case STORE:
            api.handleEvent(
                new StoreChannelUpdatePermissionsEvent(
                    api, responseNumber,
                    (StoreChannel) channel, changed));
            break;
        case VOICE:
            api.handleEvent(
                new VoiceChannelUpdatePermissionsEvent(
                    api, responseNumber,
                    (VoiceChannel) channel, changed));
            break;
        case STAGE:
            //TODO-v5: We are killing all of these events in v5, so don't add new event here
            break;
        case NEWS:
            //TODO-v5: We are killing all of these events in v5, so don't add new event here
            break;
        case TEXT:
            api.handleEvent(
                new TextChannelUpdatePermissionsEvent(
                    api, responseNumber,
                    (TextChannel) channel, changed));
            break;
        }
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
    private boolean handlePermissionOverride(PermissionOverride currentOverride, DataObject override, long overrideId, AbstractChannelImpl<?,?> channel)
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
                channel.getOverrideMap().remove(overrideId);
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
            channel.getOverrideMap().put(overrideId, currentOverride);
            api.handleEvent(
                new PermissionOverrideCreateEvent(
                    api, responseNumber,
                    channel, currentOverride));
        }

        return true;
    }
}
