/*
 * Copyright 2015-2020 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

import gnu.trove.TDecorators;
import gnu.trove.list.TLongList;
import gnu.trove.list.linked.TLongLinkedList;
import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.channel.category.update.CategoryUpdateNameEvent;
import net.dv8tion.jda.api.events.channel.category.update.CategoryUpdatePermissionsEvent;
import net.dv8tion.jda.api.events.channel.category.update.CategoryUpdatePositionEvent;
import net.dv8tion.jda.api.events.channel.store.update.StoreChannelUpdateNameEvent;
import net.dv8tion.jda.api.events.channel.store.update.StoreChannelUpdatePermissionsEvent;
import net.dv8tion.jda.api.events.channel.store.update.StoreChannelUpdatePositionEvent;
import net.dv8tion.jda.api.events.channel.text.update.*;
import net.dv8tion.jda.api.events.channel.voice.update.*;
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
        ChannelType type = ChannelType.fromId(content.getInt("type"));
        if (type == ChannelType.GROUP)
        {
            WebSocketClient.LOG.warn("Ignoring CHANNEL_UPDATE for a group which we don't support");
            return null;
        }

        List<IPermissionHolder> changed = new ArrayList<>();
        List<IPermissionHolder> contained = new ArrayList<>();

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

                applyPermissions(storeChannel, content, permOverwrites, contained, changed);
                getJDA().handleEvent(
                    new StoreChannelUpdatePermissionsEvent(
                        getJDA(), responseNumber,
                        storeChannel, changed));
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

                applyPermissions(textChannel, content, permOverwrites, contained, changed);

                //If this update modified permissions in any way.
                if (!changed.isEmpty())
                {
                    getJDA().handleEvent(
                            new TextChannelUpdatePermissionsEvent(
                                    getJDA(), responseNumber,
                                    textChannel, changed));
                }
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

                applyPermissions(voiceChannel, content, permOverwrites, contained, changed);

                //If this update modified permissions in any way.
                if (!changed.isEmpty())
                {
                    getJDA().handleEvent(
                            new VoiceChannelUpdatePermissionsEvent(
                                    getJDA(), responseNumber,
                                    voiceChannel, changed));
                }
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

                applyPermissions(category, content, permOverwrites, contained, changed);
                //If this update modified permissions in any way.
                if (!changed.isEmpty())
                {
                    getJDA().handleEvent(
                            new CategoryUpdatePermissionsEvent(
                                getJDA(), responseNumber,
                                category, changed));
                }
                break;  //Finish the CategoryUpdate case
            }
            default:
                WebSocketClient.LOG.debug("CHANNEL_UPDATE provided an unrecognized channel type JSON: {}", content);
        }
        return null;
    }

    private void applyPermissions(AbstractChannelImpl<?,?> channel, DataObject content,
                                  DataArray permOverwrites, List<IPermissionHolder> contained, List<IPermissionHolder> changed)
    {

        //Determines if a new PermissionOverride was created or updated.
        //If a PermissionOverride was created or updated it stores it in the proper Map to be reported by the Event.
        for (int i = 0; i < permOverwrites.length(); i++)
        {
            handlePermissionOverride(permOverwrites.getObject(i), channel, content, changed, contained);
        }

        //Check if any overrides were deleted because of this event.
        //Get the current overrides. (we copy them to a new list because the Set returned is backed by the Map, meaning our removes would remove from the Map. Not good.
        //Loop through all of the json defined overrides. If we find a match, remove the User or Role from our lists.
        //Any entries remaining in these lists after this for loop is over will be removed from the GuildChannel's overrides.
        final TLongList toRemove = new TLongLinkedList();
        final TLongObjectMap<PermissionOverride> overridesMap = channel.getOverrideMap();

        TDecorators.wrap(overridesMap.keySet()).stream()
            .map(id -> mapPermissionHolder(id, channel.getGuild()))
            .filter(Objects::nonNull)
            .filter(permHolder -> !contained.contains(permHolder))
            .forEach(permHolder ->
            {
                changed.add(permHolder);
                toRemove.add(permHolder.getIdLong());
            });

        channel.getGuild().updateCachedOverrides(channel, permOverwrites);
        toRemove.forEach((id) ->
        {
            overridesMap.remove(id);
            return true;
        });
    }

    private IPermissionHolder mapPermissionHolder(long id, Guild guild)
    {
        final Role holder = guild.getRoleById(id);
        return holder == null ? guild.getMemberById(id) : holder;
    }

    private void handlePermissionOverride(DataObject override, AbstractChannelImpl<?,?> channel, DataObject content,
                                          List<IPermissionHolder> changedPermHolders, List<IPermissionHolder> containedPermHolders)
    {
        final long id = override.getLong("id");
        final long allow = override.getLong("allow");
        final long deny = override.getLong("deny");
        final IPermissionHolder permHolder;

        switch (override.getString("type"))
        {
            case "role":
            {
                permHolder = channel.getGuild().getRoleById(id);

                if (permHolder == null)
                {
                    getJDA().getEventCache().cache(EventCache.Type.ROLE, id, responseNumber, allContent, (a, b) ->
                            handlePermissionOverride(override, channel, content, changedPermHolders, containedPermHolders));
                    EventCache.LOG.debug("CHANNEL_UPDATE attempted to create or update a PermissionOverride for a Role that doesn't exist! RoleId: {} JSON: {}", id, content);
                    return;
                }
                break;
            }
            case "member":
            {
                permHolder = channel.getGuild().getMemberById(id);
                if (permHolder == null)
                {
                    // cache override for unloaded member (maybe loaded later)
                    channel.getGuild().cacheOverride(id, channel.getIdLong(), override);
                    return;
                }
                break;
            }
            default:
                throw new IllegalArgumentException("CHANNEL_UPDATE provided an unrecognized PermissionOverride type. JSON: " + content);
        }

        PermissionOverrideImpl permOverride = (PermissionOverrideImpl) channel.getOverrideMap().get(id);

        if (permOverride == null)    //Created
        {
            getJDA().getEntityBuilder().createPermissionOverride(override, channel);
            changedPermHolders.add(permHolder);
        }
        else if (permOverride.getAllowedRaw() != allow || permOverride.getDeniedRaw() != deny) //Updated
        {
            permOverride.setAllow(allow);
            permOverride.setDeny(deny);
            changedPermHolders.add(permHolder);
        }
        containedPermHolders.add(permHolder);
    }
}
