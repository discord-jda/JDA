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
import java.util.stream.Collectors;

public class ChannelUpdateHandler extends SocketHandler
{
    public ChannelUpdateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected String handleInternally(JSONObject content)
    {
        ChannelType type = ChannelType.fromId(content.getInt("type"));
        if (type == ChannelType.GROUP)
        {
            handleGroup(content);
            return null;
        }

        List<Role> changedRoles = new ArrayList<>();
        List<Member> changedMembers = new ArrayList<>();
        List<Role> containedRoles = new ArrayList<>();
        List<Member> containedMembers = new ArrayList<>();

        String name = content.getString("name");
        int position = content.getInt("position");
        JSONArray permOverwrites = content.getJSONArray("permission_overwrites");
        switch (type)
        {
            case TEXT:
            {
                String topic = content.isNull("topic") ? null : content.getString("topic");
                TextChannelImpl channel = (TextChannelImpl) api.getTextChannelMap().get(content.getString("id"));
                if (channel == null)
                {
                    EventCache.get(api).cache(EventCache.Type.CHANNEL, content.getString("id"), () ->
                    {
                        handle(responseNumber, allContent);
                    });
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
                    handlePermissionOverride(permOverwrites.getJSONObject(i), channel, content, changedRoles, containedRoles, changedMembers, containedMembers);
                }

                //Check if any overrides were deleted because of this event.
                //Get the current overrides. (we copy them to a new list because the Set returned is backed by the Map, meaning our removes would remove from the Map. Not good.
                //Loop through all of the json defined overrides. If we find a match, remove the User or Role from our lists.
                //Any entries remaining in these lists after this for loop is over will be removed from the Channel's overrides.
                List<Role> collect = channel.getRoleOverrideMap().keySet().stream().filter(role -> !containedRoles.contains(role)).collect(Collectors.toList());
                collect.forEach(role -> {
                    changedRoles.add(role);
                    channel.getRoleOverrideMap().remove(role);
                });
                List<Member> collect1 = channel.getMemberOverrideMap().keySet().stream().filter(user -> !containedMembers.contains(user)).collect(Collectors.toList());
                collect1.forEach(member -> {
                    changedMembers.add(member);
                    channel.getMemberOverrideMap().remove(member);
                });

                //If this update modified permissions in any way.
                if (!changedRoles.isEmpty()
                        || !changedMembers.isEmpty())
                {
                    api.getEventManager().handle(
                            new TextChannelUpdatePermissionsEvent(
                                    api, responseNumber,
                                    channel,
                                    changedRoles, changedMembers));
                }
                break;  //Finish the TextChannelUpdate case
            }
            case VOICE:
            {
                VoiceChannelImpl channel = (VoiceChannelImpl) api.getVoiceChannelMap().get(content.getString("id"));
                int userLimit = content.getInt("user_limit");
                int bitrate = content.getInt("bitrate");
                if (channel == null)
                {
                    EventCache.get(api).cache(EventCache.Type.CHANNEL, content.getString("id"), () ->
                    {
                        handle(responseNumber, allContent);
                    });
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
                    handlePermissionOverride(permOverwrites.getJSONObject(i), channel, content, changedRoles, containedRoles, changedMembers, containedMembers);
                }

                //Check if any overrides were deleted because of this event.
                //Get the current overrides. (we copy them to a new list because the Set returned is backed by the Map, meaning our removes would remove from the Map. Not good.
                //Loop through all of the json defined overrides. If we find a match, remove the User or Role from our lists.
                //Any entries remaining in these lists after this for loop is over will be removed from the Channel's overrides.
                List<Role> collect = channel.getRoleOverrideMap().keySet().stream().filter(role -> !containedRoles.contains(role)).collect(Collectors.toList());
                collect.forEach(role -> {
                    changedRoles.add(role);
                    channel.getRoleOverrideMap().remove(role);
                });
                List<Member> collect1 = channel.getMemberOverrideMap().keySet().stream().filter(user -> !containedMembers.contains(user)).collect(Collectors.toList());
                collect1.forEach(member -> {
                    changedMembers.add(member);
                    channel.getMemberOverrideMap().remove(member);
                });

                //If this update modified permissions in any way.
                if (!changedRoles.isEmpty()
                        || !changedMembers.isEmpty())
                {
                    api.getEventManager().handle(
                            new VoiceChannelUpdatePermissionsEvent(
                                    api, responseNumber,
                                    channel,
                                    changedRoles, changedMembers));
                }
                break;  //Finish the TextChannelUpdate case
            }
            default:
                throw new IllegalArgumentException("CHANNEL_UPDATE provided an unrecognized channel type JSON: " + content);
        }
        return null;
    }

    private void handlePermissionOverride(JSONObject override, Channel channel, JSONObject content,
                                          List<Role> changedRoles, List<Role> containedRoles,List<Member> changedMembers, List<Member> containedMembers)
    {
        String id = override.getString("id");
        int allow = override.getInt("allow");
        int deny = override.getInt("deny");

        switch (override.getString("type"))
        {
            case "role":
            {
                Role role = ((GuildImpl) channel.getGuild()).getRolesMap().get(id);
                if (role == null)
                {
                    EventCache.get(api).cache(EventCache.Type.ROLE, id, () ->
                    {
                        handlePermissionOverride(override, channel, content, changedRoles, containedRoles, changedMembers, containedMembers);
                    });
                    EventCache.LOG.debug("CHANNEL_UPDATE attempted to create or update a PermissionOverride for a Role that doesn't exist! JSON: " + content);
                    return;
                }

                PermissionOverride permOverride;
                if (channel instanceof TextChannel)
                    permOverride = ((TextChannelImpl) channel).getRoleOverrideMap().get(role);
                else
                    permOverride = ((VoiceChannelImpl) channel).getRoleOverrideMap().get(role);

                if (permOverride == null)    //Created
                {
                    permOverride = EntityBuilder.get(api).createPermissionOverride(override, channel);
                    changedRoles.add(role);
                }
                else if (permOverride.getAllowedRaw() != allow || permOverride.getDeniedRaw() != deny) //Updated
                {
                    ((PermissionOverrideImpl) permOverride).setAllow(allow);
                    ((PermissionOverrideImpl) permOverride).setDeny(deny);
                    changedRoles.add(role);
                }
                containedRoles.add(role);
                break;
            }
            case "member":
            {
                Member member = channel.getGuild().getMemberById(override.getString("id"));
                if (member == null)
                {
                    EventCache.get(api).cache(EventCache.Type.USER, id, () ->
                    {
                        handlePermissionOverride(override, channel, content, changedRoles, containedRoles, changedMembers, containedMembers);
                    });
                    EventCache.LOG.debug("CHANNEL_UPDATE attempted to create or update a PermissionOverride for User that doesn't exist in this Guild! JSON: " + content);
                    return;
                }

                PermissionOverride permOverride;
                if (channel instanceof TextChannel)
                    permOverride = ((TextChannelImpl) channel).getMemberOverrideMap().get(member);
                else
                    permOverride = ((VoiceChannelImpl) channel).getMemberOverrideMap().get(member);

                if (permOverride == null)    //Created
                {
                    permOverride = EntityBuilder.get(api).createPermissionOverride(override, channel);
                    changedMembers.add(member);
                }
                else if (permOverride.getAllowedRaw() != allow || permOverride.getDeniedRaw() != deny)  //Updated
                {
                    ((PermissionOverrideImpl) permOverride).setAllow(allow);
                    ((PermissionOverrideImpl) permOverride).setDeny(deny);
                    changedMembers.add(member);
                }
                containedMembers.add(member);
                break;
            }
            default:
                throw new IllegalArgumentException("CHANNEL_UPDATE provided an unrecognized PermissionOverride type. JSON: " + content);
        }
    }

    private void handleGroup(JSONObject content)
    {
        String groupId = content.getString("id");
        String name = !content.isNull("name") ? content.getString("name") : null;
        String iconId = !content.isNull("icon") ? content.getString("icon") : null;
        String ownerId = content.getString("owner_id");

        GroupImpl group = (GroupImpl) api.asClient().getGroupById(groupId);
        if (group == null)
        {
            EventCache.get(api).cache(EventCache.Type.CHANNEL, groupId, () ->
            {
                handle(responseNumber, allContent);
            });
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
