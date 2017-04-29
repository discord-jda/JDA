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

package net.dv8tion.jda.core.handle;

import gnu.trove.TDecorators;
import gnu.trove.list.TLongList;
import gnu.trove.list.linked.TLongLinkedList;
import net.dv8tion.jda.client.entities.impl.GroupImpl;
import net.dv8tion.jda.client.events.group.update.GroupUpdateIconEvent;
import net.dv8tion.jda.client.events.group.update.GroupUpdateNameEvent;
import net.dv8tion.jda.client.events.group.update.GroupUpdateOwnerEvent;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.entities.impl.*;
import net.dv8tion.jda.core.events.channel.text.update.TextChannelUpdateNameEvent;
import net.dv8tion.jda.core.events.channel.text.update.TextChannelUpdatePermissionsEvent;
import net.dv8tion.jda.core.events.channel.text.update.TextChannelUpdatePositionEvent;
import net.dv8tion.jda.core.events.channel.text.update.TextChannelUpdateTopicEvent;
import net.dv8tion.jda.core.events.channel.voice.update.*;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

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
    protected Long handleInternally(JSONObject content)
    {
        ChannelType type = ChannelType.fromId(content.getInt("type"));
        if (type == ChannelType.GROUP)
        {
            handleGroup(content);
            return null;
        }

        List<IPermissionHolder> changed = new ArrayList<>();
        List<IPermissionHolder> contained = new ArrayList<>();

        final long channelId = content.getLong("id");
        String name = content.getString("name");
        int position = content.getInt("position");
        JSONArray permOverwrites = content.getJSONArray("permission_overwrites");
        switch (type)
        {
            case TEXT:
            {
                String topic = content.isNull("topic") ? null : content.getString("topic");
                TextChannelImpl channel = (TextChannelImpl) api.getTextChannelMap().get(channelId);
                if (channel == null)
                {
                    api.getEventCache().cache(EventCache.Type.CHANNEL, channelId, () -> handle(responseNumber, allContent));
                    EventCache.LOG.debug("CHANNEL_UPDATE attempted to update a TextChannel that does not exist. JSON: " + content);
                    return null;
                }

                //If any properties changed, update the values and fire the proper events.
                if (!StringUtils.equals(channel.getName(), name))
                {
                    String oldName = channel.getName();
                    channel.setName(name);
                    api.getEventManager().handle(
                            new TextChannelUpdateNameEvent(
                                    api, responseNumber,
                                    channel, oldName));
                }
                if (!StringUtils.equals(channel.getTopic(), topic))
                {
                    String oldTopic = channel.getTopic();
                    channel.setTopic(topic);
                    api.getEventManager().handle(
                            new TextChannelUpdateTopicEvent(
                                    api, responseNumber,
                                    channel, oldTopic));
                }
                if (channel.getPositionRaw() != position)
                {
                    int oldPosition = channel.getPositionRaw();
                    channel.setRawPosition(position);
                    api.getEventManager().handle(
                            new TextChannelUpdatePositionEvent(
                                    api, responseNumber,
                                    channel, oldPosition));
                }

                //Determines if a new PermissionOverride was created or updated.
                //If a PermissionOverride was created or updated it stores it in the proper Map to be reported by the Event.
                for (int i = 0; i < permOverwrites.length(); i++)
                {
                    handlePermissionOverride(permOverwrites.getJSONObject(i), channel, content, changed, contained);
                }

                //Check if any overrides were deleted because of this event.
                //Get the current overrides. (we copy them to a new list because the Set returned is backed by the Map, meaning our removes would remove from the Map. Not good.
                //Loop through all of the json defined overrides. If we find a match, remove the User or Role from our lists.
                //Any entries remaining in these lists after this for loop is over will be removed from the Channel's overrides.
                TLongList toRemove = new TLongLinkedList();
                TDecorators.wrap(channel.getOverrideMap().keySet()).stream()
                        .map(id -> mapPermissionHolder(id, channel.getGuild()))
                        .filter(Objects::nonNull)
                        .filter(permHolder -> !contained.contains(permHolder))
                        .forEach(permHolder -> {
                            changed.add(permHolder);
                            toRemove.add(getIdLong(permHolder));
                        });

                toRemove.forEach((id) -> {
                    channel.getOverrideMap().remove(id);
                    return true;
                });

                //If this update modified permissions in any way.
                if (!changed.isEmpty())
                {
                    api.getEventManager().handle(
                            new TextChannelUpdatePermissionsEvent(
                                    api, responseNumber,
                                    channel, changed));
                }
                break;  //Finish the TextChannelUpdate case
            }
            case VOICE:
            {
                VoiceChannelImpl channel = (VoiceChannelImpl) api.getVoiceChannelMap().get(channelId);
                int userLimit = content.getInt("user_limit");
                int bitrate = content.getInt("bitrate");
                if (channel == null)
                {
                    api.getEventCache().cache(EventCache.Type.CHANNEL, channelId, () -> handle(responseNumber, allContent));
                    EventCache.LOG.debug("CHANNEL_UPDATE attempted to update a VoiceChannel that does not exist. JSON: " + content);
                    return null;
                }
                //If any properties changed, update the values and fire the proper events.
                if (!StringUtils.equals(channel.getName(), name))
                {
                    String oldName = channel.getName();
                    channel.setName(name);
                    api.getEventManager().handle(
                            new VoiceChannelUpdateNameEvent(
                                    api, responseNumber,
                                    channel, oldName));
                }
                if (channel.getPositionRaw() != position)
                {
                    int oldPosition = channel.getPositionRaw();
                    channel.setRawPosition(position);
                    api.getEventManager().handle(
                            new VoiceChannelUpdatePositionEvent(
                                    api, responseNumber,
                                    channel, oldPosition));
                }
                if (channel.getUserLimit() != userLimit)
                {
                    int oldLimit = channel.getUserLimit();
                    channel.setUserLimit(userLimit);
                    api.getEventManager().handle(
                            new VoiceChannelUpdateUserLimitEvent(
                                    api, responseNumber,
                                    channel, oldLimit));
                }
                if (channel.getBitrate() != bitrate)
                {
                    int oldBitrate = channel.getBitrate();
                    channel.setBitrate(bitrate);
                    api.getEventManager().handle(
                            new VoiceChannelUpdateBitrateEvent(
                                    api, responseNumber,
                                    channel, oldBitrate));
                }

                //Determines if a new PermissionOverride was created or updated.
                //If a PermissionOverride was created or updated it stores it in the proper Map to be reported by the Event.
                for (int i = 0; i < permOverwrites.length(); i++)
                {
                    handlePermissionOverride(permOverwrites.getJSONObject(i), channel, content, changed, contained);
                }

                //Check if any overrides were deleted because of this event.
                //Get the current overrides. (we copy them to a new list because the Set returned is backed by the Map, meaning our removes would remove from the Map. Not good.
                //Loop through all of the json defined overrides. If we find a match, remove the User or Role from our lists.
                //Any entries remaining in these lists after this for loop is over will be removed from the Channel's overrides.
                TLongList toRemove = new TLongLinkedList();
                TDecorators.wrap(channel.getOverrideMap().keySet()).stream()
                    .map(id -> mapPermissionHolder(id, channel.getGuild()))
                    .filter(Objects::nonNull)
                    .filter(permHolder -> !contained.contains(permHolder))
                    .forEach(permHolder -> {
                        changed.add(permHolder);
                        toRemove.add(getIdLong(permHolder));
                    });


                toRemove.forEach((id) -> {
                    channel.getOverrideMap().remove(id);
                    return true;
                });

                //If this update modified permissions in any way.
                if (!changed.isEmpty())
                {
                    api.getEventManager().handle(
                            new VoiceChannelUpdatePermissionsEvent(
                                    api, responseNumber,
                                    channel, changed));
                }
                break;  //Finish the TextChannelUpdate case
            }
            default:
                throw new IllegalArgumentException("CHANNEL_UPDATE provided an unrecognized channel type JSON: " + content);
        }
        return null;
    }

    private long getIdLong(IPermissionHolder permHolder)
    {
        if (permHolder instanceof Member)
            return ((Member) permHolder).getUser().getIdLong();
        else
            return ((Role) permHolder).getIdLong();
    }

    private IPermissionHolder mapPermissionHolder(long id, Guild guild)
    {
        final Role holder = guild.getRoleById(id);
        return holder == null ? guild.getMemberById(id) : holder;
    }

    private void handlePermissionOverride(JSONObject override, AbstractChannelImpl<?> channel, JSONObject content,
                                          List<IPermissionHolder> changedPermHolders, List<IPermissionHolder> containedPermHolders)
    {
        final long id = content.getLong("id");
        int allow = override.getInt("allow");
        int deny = override.getInt("deny");
        final IPermissionHolder permHolder;

        switch (override.getString("type"))
        {
            case "role":
            {
                permHolder = channel.getGuild().getRoleById(id);

                if (permHolder == null)
                {
                    api.getEventCache().cache(EventCache.Type.ROLE, id, () ->
                            handlePermissionOverride(override, channel, content, changedPermHolders, containedPermHolders));
                    EventCache.LOG.debug("CHANNEL_UPDATE attempted to create or update a PermissionOverride for a Role that doesn't exist! JSON: " + content);
                    return;
                }
                break;
            }
            case "member":
            {
                permHolder = channel.getGuild().getMemberById(id);
                if (permHolder == null)
                {
                    api.getEventCache().cache(EventCache.Type.USER, id, () ->
                            handlePermissionOverride(override, channel, content, changedPermHolders, containedPermHolders));
                    EventCache.LOG.debug("CHANNEL_UPDATE attempted to create or update a PermissionOverride for User that doesn't exist in this Guild! JSON: " + content);
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
            api.getEntityBuilder().createPermissionOverride(override, channel);
            changedPermHolders.add(permHolder);
        }
        else if (permOverride.getAllowedRaw() != allow || permOverride.getDeniedRaw() != deny) //Updated
        {
            permOverride.setAllow(allow);
            permOverride.setDeny(deny);
            changedPermHolders.add(permHolder);
        }
        changedPermHolders.add(permHolder);
    }

    private void handleGroup(JSONObject content)
    {
        final long groupId = content.getLong("id");
        final long ownerId = content.getLong("owner_id");
        String name = !content.isNull("name") ? content.getString("name") : null;
        String iconId = !content.isNull("icon") ? content.getString("icon") : null;

        GroupImpl group = (GroupImpl) api.asClient().getGroupById(groupId);
        if (group == null)
        {
            api.getEventCache().cache(EventCache.Type.CHANNEL, groupId, () -> handle(responseNumber, allContent));
            EventCache.LOG.debug("Received CHANNEL_UPDATE for a group that was not yet cached. JSON: " + content);
            return;
        }

        User owner = group.getUserMap().get(ownerId);

        if (!Objects.equals(name, group.getName()))
        {
            String oldName = group.getName();
            group.setName(name);
            api.getEventManager().handle(
                    new GroupUpdateNameEvent(
                            api, responseNumber,
                            group, oldName));
        }
        if (!Objects.equals(iconId, group.getIconId()))
        {
            String oldIconId = group.getIconId();
            group.setIconId(iconId);
            api.getEventManager().handle(
                    new GroupUpdateIconEvent(
                            api, responseNumber,
                            group, oldIconId));
        }
        if (!Objects.equals(owner, group.getOwner()))
        {
            User oldOwner = group.getOwner();
            group.setOwner(owner);
            api.getEventManager().handle(
                    new GroupUpdateOwnerEvent(
                            api, responseNumber,
                            group, oldOwner));
        }
    }
}
