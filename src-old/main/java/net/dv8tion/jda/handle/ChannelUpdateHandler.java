/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
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
package net.dv8tion.jda.handle;

import net.dv8tion.jda.entities.*;
import net.dv8tion.jda.entities.impl.*;
import net.dv8tion.jda.events.channel.text.TextChannelUpdateNameEvent;
import net.dv8tion.jda.events.channel.text.TextChannelUpdatePermissionsEvent;
import net.dv8tion.jda.events.channel.text.TextChannelUpdatePositionEvent;
import net.dv8tion.jda.events.channel.text.TextChannelUpdateTopicEvent;
import net.dv8tion.jda.events.channel.voice.*;
import net.dv8tion.jda.requests.GuildLock;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChannelUpdateHandler extends SocketHandler
{
    private List<Role> changedRoles = new ArrayList<>();
    private List<User> changedUsers = new ArrayList<>();
    private List<Role> containedRoles = new ArrayList<>();
    private List<User> containedUsers = new ArrayList<>();

    public ChannelUpdateHandler(JDAImpl api, int responseNumber)
    {
        super(api, responseNumber);
    }

    @Override
    protected String handleInternally(JSONObject content)
    {
        if (GuildLock.get(api).isLocked(content.getString("guild_id")))
        {
            return content.getString("guild_id");
        }

        String name = content.getString("name");
        int position = content.getInt("position");
        JSONArray permOverwrites = content.getJSONArray("permission_overwrites");
        ChannelType type = ChannelType.fromId(content.getInt("type"));
        switch (type)
        {
            case TEXT:
            {
                String topic = content.isNull("topic") ? null : content.getString("topic");
                TextChannelImpl channel = (TextChannelImpl) api.getChannelMap().get(content.getString("id"));
                if (channel == null)
                {
                    EventCache.get(api).cache(EventCache.Type.CHANNEL, content.getString("id"), () ->
                    {
                        handle(allContent);
                    });
                    EventCache.LOG.debug("CHANNEL_UPDATE attemped to update a TextChannel that does not exist. JSON: " + content);
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
                    channel.setPosition(position);
                    api.getEventManager().handle(
                            new TextChannelUpdatePositionEvent(
                                    api, responseNumber,
                                    channel, oldPosition));
                }

                //Determines if a new PermissionOverride was created or updated.
                //If a PermissionOverride was created or updated it stores it in the proper Map to be reported by the Event.
                for (int i = 0; i < permOverwrites.length(); i++)
                {
                    handlePermissionOverride(permOverwrites.getJSONObject(i), channel, content);
                }

                //Check if any overrides were deleted because of this event.
                //Get the current overrides. (we copy them to a new list because the Set returned is backed by the Map, meaning our removes would remove from the Map. Not good.
                //Loop through all of the json defined overrides. If we find a match, remove the User or Role from our lists.
                //Any entries remaining in these lists after this for loop is over will be removed from the Channel's overrides.
                List<Role> collect = channel.getRolePermissionOverridesMap().keySet().stream().filter(role -> !containedRoles.contains(role)).collect(Collectors.toList());
                collect.forEach(role -> {
                    changedRoles.add(role);
                    channel.getRolePermissionOverridesMap().remove(role);
                });
                List<User> collect1 = channel.getUserPermissionOverridesMap().keySet().stream().filter(user -> !containedUsers.contains(user)).collect(Collectors.toList());
                collect1.forEach(user -> {
                    changedUsers.add(user);
                    channel.getUserPermissionOverridesMap().remove(user);
                });

                //If this update modified permissions in any way.
                if (!changedRoles.isEmpty()
                        || !changedUsers.isEmpty())
                {
                    api.getEventManager().handle(
                            new TextChannelUpdatePermissionsEvent(
                                    api, responseNumber,
                                    channel,
                                    changedRoles, changedUsers));
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
                        handle(allContent);
                    });
                    EventCache.LOG.debug("CHANNEL_UPDATE attemped to update a VoiceChannel that does not exist. JSON: " + content);
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
                    channel.setPosition(position);
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
                    handlePermissionOverride(permOverwrites.getJSONObject(i), channel, content);
                }

                //Check if any overrides were deleted because of this event.
                //Get the current overrides. (we copy them to a new list because the Set returned is backed by the Map, meaning our removes would remove from the Map. Not good.
                //Loop through all of the json defined overrides. If we find a match, remove the User or Role from our lists.
                //Any entries remaining in these lists after this for loop is over will be removed from the Channel's overrides.
                List<Role> collect = channel.getRolePermissionOverridesMap().keySet().stream().filter(role -> !containedRoles.contains(role)).collect(Collectors.toList());
                collect.forEach(role -> {
                    changedRoles.add(role);
                    channel.getRolePermissionOverridesMap().remove(role);
                });
                List<User> collect1 = channel.getUserPermissionOverridesMap().keySet().stream().filter(user -> !containedUsers.contains(user)).collect(Collectors.toList());
                collect1.forEach(user -> {
                    changedUsers.add(user);
                    channel.getUserPermissionOverridesMap().remove(user);
                });

                //If this update modified permissions in any way.
                if (!changedRoles.isEmpty()
                        || !changedUsers.isEmpty())
                {
                    api.getEventManager().handle(
                            new VoiceChannelUpdatePermissionsEvent(
                                    api, responseNumber,
                                    channel,
                                    changedRoles, changedUsers));
                }
                break;  //Finish the TextChannelUpdate case
            }
            default:
                throw new IllegalArgumentException("CHANNEL_UPDATE provided an unrecognized channel type JSON: " + content);
        }
        return null;
    }

    private void handlePermissionOverride(JSONObject override, Channel channel, JSONObject content)
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
                        handlePermissionOverride(override, channel, content);
                    });
                    EventCache.LOG.debug("CHANNEL_UPDATE attempted to create or update a PermissionOverride for a Role that doesn't exist! JSON: " + content);
                    return;
                }

                PermissionOverride permOverride;
                if (channel instanceof TextChannel)
                    permOverride = ((TextChannelImpl) channel).getRolePermissionOverridesMap().get(role);
                else
                    permOverride = ((VoiceChannelImpl) channel).getRolePermissionOverridesMap().get(role);

                if (permOverride == null)    //Created
                {
                    permOverride = new EntityBuilder(api).createPermissionOverride(override, channel);
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
                User user = api.getUserMap().get(override.getString("id"));
                if (user == null || !channel.getGuild().getUsers().contains(user))
                {
                    EventCache.get(api).cache(EventCache.Type.USER, id, () ->
                    {
                        handlePermissionOverride(override, channel, content);
                    });
                    EventCache.LOG.debug("CHANNEL_UPDATE attempted to create or update a PermissionOverride for User that doesn't exist in this Guild! JSON: " + content);
                    return;
                }

                PermissionOverride permOverride;
                if (channel instanceof TextChannel)
                    permOverride = ((TextChannelImpl) channel).getUserPermissionOverridesMap().get(user);
                else
                    permOverride = ((VoiceChannelImpl) channel).getUserPermissionOverridesMap().get(user);

                if (permOverride == null)    //Created
                {
                    permOverride = new EntityBuilder(api).createPermissionOverride(override, channel);
                    changedUsers.add(user);
                }
                else if (permOverride.getAllowedRaw() != allow || permOverride.getDeniedRaw() != deny)  //Updated
                {
                    ((PermissionOverrideImpl) permOverride).setAllow(allow);
                    ((PermissionOverrideImpl) permOverride).setDeny(deny);
                    changedUsers.add(user);
                }
                containedUsers.add(user);
                break;
            }
            default:
                throw new IllegalArgumentException("CHANNEL_UPDATE provided an unrecognized PermissionOverride type. JSON: " + content);
        }
    }
}
