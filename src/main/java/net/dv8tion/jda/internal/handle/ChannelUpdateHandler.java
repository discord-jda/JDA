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
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.channel.category.update.CategoryUpdateNameEvent;
import net.dv8tion.jda.api.events.channel.category.update.CategoryUpdatePermissionsEvent;
import net.dv8tion.jda.api.events.channel.category.update.CategoryUpdatePositionEvent;
import net.dv8tion.jda.api.events.channel.store.update.StoreChannelUpdateNameEvent;
import net.dv8tion.jda.api.events.channel.store.update.StoreChannelUpdatePermissionsEvent;
import net.dv8tion.jda.api.events.channel.store.update.StoreChannelUpdatePositionEvent;
import net.dv8tion.jda.api.events.channel.text.update.*;
import net.dv8tion.jda.api.events.channel.voice.update.*;
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
        final Long parentId = content.isNull("parent_id") ? null : content.getLong("parent_id");
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
                        new StoreChannelUpdateNameEvent(
                            getJDA(), responseNumber,
                            storeChannel, oldName));
                }
                if (!Objects.equals(oldPosition, position))
                {
                    storeChannel.setPosition(position);
                    getJDA().handleEvent(
                        new StoreChannelUpdatePositionEvent(
                            getJDA(), responseNumber,
                            storeChannel, oldPosition));
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
                final Category parent = textChannel.getParent();
                final Long oldParent = parent == null ? null : parent.getIdLong();
                final String oldName = textChannel.getName();
                final String oldTopic = textChannel.getTopic();
                final int oldPosition = textChannel.getPositionRaw();
                final boolean oldNsfw = textChannel.isNSFW();
                final int oldSlowmode = textChannel.getSlowmode();
                if (!Objects.equals(oldName, name))
                {
                    textChannel.setName(name);
                    getJDA().handleEvent(
                            new TextChannelUpdateNameEvent(
                                    getJDA(), responseNumber,
                                    textChannel, oldName));
                }
                if (!Objects.equals(oldParent, parentId))
                {
                    textChannel.setParent(parentId == null ? 0 : parentId);
                    getJDA().handleEvent(
                           new TextChannelUpdateParentEvent(
                               getJDA(), responseNumber,
                               textChannel, parent));
                }
                if (!Objects.equals(oldTopic, topic))
                {
                    textChannel.setTopic(topic);
                    getJDA().handleEvent(
                            new TextChannelUpdateTopicEvent(
                                    getJDA(), responseNumber,
                                    textChannel, oldTopic));
                }
                if (oldPosition != position)
                {
                    textChannel.setPosition(position);
                    getJDA().handleEvent(
                            new TextChannelUpdatePositionEvent(
                                    getJDA(), responseNumber,
                                    textChannel, oldPosition));
                }

                if (oldNsfw != nsfw)
                {
                    textChannel.setNSFW(nsfw);
                    getJDA().handleEvent(
                            new TextChannelUpdateNSFWEvent(
                                    getJDA(), responseNumber,
                                    textChannel, oldNsfw));
                }

                if (oldSlowmode != slowmode)
                {
                    textChannel.setSlowmode(slowmode);
                    getJDA().handleEvent(
                            new TextChannelUpdateSlowmodeEvent(
                                    getJDA(), responseNumber,
                                    textChannel, oldSlowmode));
                }

                if (news != textChannel.isNews())
                {
                    textChannel.setNews(news);
                    getJDA().handleEvent(
                        new TextChannelUpdateNewsEvent(
                            getJDA(), responseNumber,
                            textChannel));
                }

                applyPermissions(textChannel, permOverwrites);
                break;  //Finish the TextChannelUpdate case
            }
            case STAGE:
            case VOICE:
            {
                VoiceChannelImpl voiceChannel = (VoiceChannelImpl) getJDA().getVoiceChannelsView().get(channelId);
                int userLimit = content.getInt("user_limit");
                int bitrate = content.getInt("bitrate");
                final String region = content.getString("rtc_region", null);
                if (voiceChannel == null)
                {
                    getJDA().getEventCache().cache(EventCache.Type.CHANNEL, channelId, responseNumber, allContent, this::handle);
                    EventCache.LOG.debug("CHANNEL_UPDATE attempted to update a VoiceChannel that does not exist. JSON: {}", content);
                    return null;
                }
                //If any properties changed, update the values and fire the proper events.
                final Category parent = voiceChannel.getParent();
                final Long oldParent = parent == null ? null : parent.getIdLong();
                final String oldName = voiceChannel.getName();
                final String oldRegion = voiceChannel.getRegionRaw();
                final int oldPosition = voiceChannel.getPositionRaw();
                final int oldLimit = voiceChannel.getUserLimit();
                final int oldBitrate = voiceChannel.getBitrate();
                if (!Objects.equals(oldName, name))
                {
                    voiceChannel.setName(name);
                    getJDA().handleEvent(
                            new VoiceChannelUpdateNameEvent(
                                    getJDA(), responseNumber,
                                    voiceChannel, oldName));
                }
                if (!Objects.equals(oldRegion, region))
                {
                    voiceChannel.setRegion(region);
                    getJDA().handleEvent(
                            new VoiceChannelUpdateRegionEvent(
                                    getJDA(), responseNumber,
                                    voiceChannel, oldRegion));
                }
                if (!Objects.equals(oldParent, parentId))
                {
                    voiceChannel.setParent(parentId == null ? 0 : parentId);
                    getJDA().handleEvent(
                            new VoiceChannelUpdateParentEvent(
                                    getJDA(), responseNumber,
                                    voiceChannel, parent));
                }
                if (oldPosition != position)
                {
                    voiceChannel.setPosition(position);
                    getJDA().handleEvent(
                            new VoiceChannelUpdatePositionEvent(
                                    getJDA(), responseNumber,
                                    voiceChannel, oldPosition));
                }
                if (oldLimit != userLimit)
                {
                    voiceChannel.setUserLimit(userLimit);
                    getJDA().handleEvent(
                            new VoiceChannelUpdateUserLimitEvent(
                                    getJDA(), responseNumber,
                                    voiceChannel, oldLimit));
                }
                if (oldBitrate != bitrate)
                {
                    voiceChannel.setBitrate(bitrate);
                    getJDA().handleEvent(
                            new VoiceChannelUpdateBitrateEvent(
                                    getJDA(), responseNumber,
                                    voiceChannel, oldBitrate));
                }

                applyPermissions(voiceChannel, permOverwrites);
                break;  //Finish the VoiceChannelUpdate case
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
                            new CategoryUpdateNameEvent(
                                getJDA(), responseNumber,
                                category, oldName));
                }
                if (!Objects.equals(oldPosition, position))
                {
                    category.setPosition(position);
                    getJDA().handleEvent(
                            new CategoryUpdatePositionEvent(
                                getJDA(), responseNumber,
                                category, oldPosition));
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
        case STAGE:
        case VOICE:
            api.handleEvent(
                new VoiceChannelUpdatePermissionsEvent(
                    api, responseNumber,
                    (VoiceChannel) channel, changed));
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
