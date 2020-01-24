/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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
import net.dv8tion.jda.api.events.channel.category.override.CategoryCreateOverrideEvent;
import net.dv8tion.jda.api.events.channel.category.override.CategoryDeleteOverrideEvent;
import net.dv8tion.jda.api.events.channel.category.override.CategoryUpdateOverrideEvent;
import net.dv8tion.jda.api.events.channel.category.update.CategoryUpdateNameEvent;
import net.dv8tion.jda.api.events.channel.category.update.CategoryUpdatePositionEvent;
import net.dv8tion.jda.api.events.channel.store.override.StoreChannelCreateOverrideEvent;
import net.dv8tion.jda.api.events.channel.store.override.StoreChannelDeleteOverrideEvent;
import net.dv8tion.jda.api.events.channel.store.override.StoreChannelUpdateOverrideEvent;
import net.dv8tion.jda.api.events.channel.store.update.StoreChannelUpdateNameEvent;
import net.dv8tion.jda.api.events.channel.store.update.StoreChannelUpdatePositionEvent;
import net.dv8tion.jda.api.events.channel.text.override.TextChannelCreateOverrideEvent;
import net.dv8tion.jda.api.events.channel.text.override.TextChannelDeleteOverrideEvent;
import net.dv8tion.jda.api.events.channel.text.override.TextChannelUpdateOverrideEvent;
import net.dv8tion.jda.api.events.channel.text.update.*;
import net.dv8tion.jda.api.events.channel.voice.override.VoiceChannelCreateOverrideEvent;
import net.dv8tion.jda.api.events.channel.voice.override.VoiceChannelDeleteOverrideEvent;
import net.dv8tion.jda.api.events.channel.voice.override.VoiceChannelUpdateOverrideEvent;
import net.dv8tion.jda.api.events.channel.voice.update.*;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.*;
import net.dv8tion.jda.internal.requests.WebSocketClient;

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
        ChannelType type = ChannelType.fromId(content.getInt("type"));
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

                applyPermissions(textChannel, permOverwrites);
                break;  //Finish the TextChannelUpdate case
            }
            case VOICE:
            {
                VoiceChannelImpl voiceChannel = (VoiceChannelImpl) getJDA().getVoiceChannelsView().get(channelId);
                int userLimit = content.getInt("user_limit");
                int bitrate = content.getInt("bitrate");
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

    private void applyPermissions(AbstractChannelImpl<?,?> channel, DataArray permOverwrites)
    {
        TLongObjectMap<PermissionOverride> currentOverrides = new TLongObjectHashMap<>(channel.getOverrideMap());
        for (int i = 0; i < permOverwrites.length(); i++)
        {
            DataObject overrideJson = permOverwrites.getObject(i);
            long id = overrideJson.getUnsignedLong("id", 0);
            handlePermissionOverride(currentOverrides.remove(id), overrideJson, id, channel);
        }

        currentOverrides.forEachValue(override -> {
            switch (channel.getType())
            {
            case CATEGORY:
                api.handleEvent(
                    new CategoryDeleteOverrideEvent(
                        api, responseNumber,
                        (Category) channel, override));
                break;
            case STORE:
                api.handleEvent(
                    new StoreChannelDeleteOverrideEvent(
                        api, responseNumber,
                        (StoreChannel) channel, override));
                break;
            case VOICE:
                api.handleEvent(
                    new VoiceChannelDeleteOverrideEvent(
                        api, responseNumber,
                        (VoiceChannel) channel, override));
                break;
            case TEXT:
                api.handleEvent(
                    new TextChannelDeleteOverrideEvent(
                        api, responseNumber,
                        (TextChannel) channel, override));
                break;
            default:
                WebSocketClient.LOG.warn("Unable to fire permission override delete event for unknown channel type {}", channel.getType());
            }
            return true;
        });
    }

    private void handlePermissionOverride(PermissionOverride currentOverride, DataObject override, long id, AbstractChannelImpl<?,?> channel)
    {
        final long allow = override.getLong("allow");
        final long deny = override.getLong("deny");
        final String type = override.getString("type");
        final boolean role = type.equals("role");
        if (!role)
        {
            if (!type.equals("member"))
            {
                EntityBuilder.LOG.debug("Ignoring unknown invite of type '{}'. JSON: {}", type, override);
                return;
            }
            else if (!api.isCacheFlagSet(CacheFlag.MEMBER_OVERRIDES) && id != api.getSelfUser().getIdLong())
            {
                return;
            }
        }

        if (currentOverride != null)
        {
            long oldAllow = currentOverride.getAllowedRaw();
            long oldDeny = currentOverride.getDeniedRaw();
            PermissionOverrideImpl impl = (PermissionOverrideImpl) currentOverride;
            impl.setAllow(allow);
            impl.setDeny(deny);
            switch (channel.getType())
            {
            case TEXT:
                api.handleEvent(
                    new TextChannelUpdateOverrideEvent(
                        api, responseNumber,
                        (TextChannel) channel, currentOverride, oldAllow, oldDeny));
                break;
            case VOICE:
                api.handleEvent(
                    new VoiceChannelUpdateOverrideEvent(
                        api, responseNumber,
                        (VoiceChannel) channel, currentOverride, oldAllow, oldDeny));
                break;
            case STORE:
                api.handleEvent(
                    new StoreChannelUpdateOverrideEvent(
                        api, responseNumber,
                        (StoreChannel) channel, currentOverride, oldAllow, oldDeny));
                break;
            case CATEGORY:
                api.handleEvent(
                    new CategoryUpdateOverrideEvent(
                        api, responseNumber,
                        (Category) channel, currentOverride, oldAllow, oldDeny));
                break;
            default:
                WebSocketClient.LOG.warn("Unable to fire permission override update event for unknown channel type {}", channel.getType());
            }
        }
        else
        {
            currentOverride = new PermissionOverrideImpl(channel, id, role);
            channel.getOverrideMap().put(id, currentOverride);
            switch (channel.getType())
            {
            case TEXT:
                api.handleEvent(
                    new TextChannelCreateOverrideEvent(
                        api, responseNumber,
                        (TextChannel) channel, currentOverride));
                break;
            case VOICE:
                api.handleEvent(
                    new VoiceChannelCreateOverrideEvent(
                        api, responseNumber,
                        (VoiceChannel) channel, currentOverride));
                break;
            case STORE:
                api.handleEvent(
                    new StoreChannelCreateOverrideEvent(
                        api, responseNumber,
                        (StoreChannel) channel, currentOverride));
                break;
            case CATEGORY:
                api.handleEvent(
                    new CategoryCreateOverrideEvent(
                        api, responseNumber,
                        (Category) channel, currentOverride));
                break;
            default:
                WebSocketClient.LOG.warn("Unable to fire permission override update event for unknown channel type {}", channel.getType());
            }
        }


    }
}
