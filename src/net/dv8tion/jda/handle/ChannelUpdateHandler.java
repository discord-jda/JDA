/**
 *    Copyright 2015 Austin Keener & Michael Ritter
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

import java.util.*;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.Role;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.entities.impl.GuildImpl;
import net.dv8tion.jda.entities.impl.PermissionOverride;
import net.dv8tion.jda.entities.impl.TextChannelImpl;
import net.dv8tion.jda.entities.impl.VoiceChannelImpl;
import net.dv8tion.jda.events.channel.text.TextChannelUpdateNameEvent;
import net.dv8tion.jda.events.channel.text.TextChannelUpdatePermissionsEvent;
import net.dv8tion.jda.events.channel.text.TextChannelUpdatePositionEvent;
import net.dv8tion.jda.events.channel.text.TextChannelUpdateTopicEvent;
import net.dv8tion.jda.events.channel.voice.VoiceChannelUpdateNameEvent;
import net.dv8tion.jda.events.channel.voice.VoiceChannelUpdatePermissionsEvent;
import net.dv8tion.jda.events.channel.voice.VoiceChannelUpdatePositionEvent;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class ChannelUpdateHandler extends SocketHandler
{

    public ChannelUpdateHandler(JDA api, int responseNumber)
    {
        super(api, responseNumber);
    }

    @Override
    public void handle(JSONObject content)
    {
        switch (content.getString("type"))
        {
            case "text":
            {
                HashMap<Role, PermissionOverride> roleOverrideCreate = new HashMap<>();
                HashMap<Role, PermissionOverride> roleOverrideOld = new HashMap<>();
                HashMap<Role, PermissionOverride> roleOverrideRemoved = new HashMap<>();
                HashMap<User, PermissionOverride> userOverrideCreate = new HashMap<>();
                HashMap<User, PermissionOverride> userOverrideOld = new HashMap<>();
                HashMap<User, PermissionOverride> userOverrideRemoved = new HashMap<>();

                TextChannelImpl channel = (TextChannelImpl) api.getChannelMap().get(content.getString("id"));
                if (channel == null)
                    throw new IllegalArgumentException("CHANNEL_UPDATE attemped to update a TextChannel that does not exist. JSON: " + content);

                String name = content.getString("name");
                String topic = content.isNull("topic") ? null : content.getString("topic");
                int position = content.getInt("position");
                JSONArray permOverwrites = content.getJSONArray("permission_overwrites");

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
                if (channel.getPosition() != position)
                {
                    int oldPosition = channel.getPosition();
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
                    JSONObject override = permOverwrites.getJSONObject(i);
                    PermissionOverride newOverride = new PermissionOverride(override.getInt("allow"), override.getInt("deny"));
                    switch (override.getString("type"))
                    {
                        case "role":
                        {
                            Role role = ((GuildImpl) channel.getGuild()).getRolesMap().get(override.getString("id"));
                            if (role == null)
                                throw new IllegalArgumentException("CHANNEL_UPDATE attempted to create or update a PermissionOverride for a Role that doesn't exist! JSON: " + content);
                            PermissionOverride oldOverride = channel.getRolePermissionOverrides().get(role);

                            if (oldOverride == null)    //Created
                            {
                                roleOverrideCreate.put(role, newOverride);
                                channel.getRolePermissionOverrides().put(role, newOverride);
                            }
                            if (!newOverride.equals(oldOverride))   //Updated
                            {
                                roleOverrideOld.put(role, oldOverride);
                                channel.getRolePermissionOverrides().put(role, newOverride);
                            }
                            break;
                        }
                        case "member":
                        {
                            User user = api.getUserMap().get(override.getString("id"));
                            if (user == null)
                                throw new IllegalArgumentException("CHANNEL_UPDATE attempted to create or update a PermissionOverride for User that doesn't exist! JSON: " + content);
                            PermissionOverride oldOverride = channel.getUserPermissionOverrides().get(user);

                            if (oldOverride == null)    //Created
                            {
                                userOverrideCreate.put(user, newOverride);
                                channel.getUserPermissionOverrides().put(user, newOverride);
                            }
                            else if (!newOverride.equals(oldOverride))  //Updated
                            {
                                userOverrideOld.put(user, oldOverride);
                                channel.getUserPermissionOverrides().put(user, newOverride);
                            }
                            break;
                        }
                        default:
                            throw new IllegalArgumentException("CHANNEL_UPDATE provided an unrecognized PermissionOverride type. JSON: " + content);
                    }
                }

                //Check if any overrides were deleted because of this event.
                //Get the current overrides. (we copy them to a new list because the Set returned is backed by the Map, meaning our removes would remove from the Map. Not good.
                //Loop through all of the json defined overrides. If we find a match, remove the User or Role from our lists.
                //Any entries remaining in these lists after this for loop is over will be removed from the Channel's overrides.
                List<User> userOverrides = toList(channel.getUserPermissionOverrides().keySet()) ;
                List<Role> roleOverrides = toList(channel.getRolePermissionOverrides().keySet());
                for (int i = 0; i < permOverwrites.length(); i++)
                {
                    JSONObject json = permOverwrites.getJSONObject(i);
                    switch (json.getString("type"))
                    {
                        case "role":
                        {
                            String roleId = json.getString("id");
                            for (Iterator<Role> it = roleOverrides.iterator(); it.hasNext();)
                            {
                                Role role = it.next();
                                if (role.getId().equals(roleId))
                                {
                                    it.remove();    //It still exists in the channel's permissions, exclude it from removal.
                                    break;
                                }
                            }
                            break;
                        }
                        case "member":
                        {
                            String userId = json.getString("id");
                            for (Iterator<User> it = userOverrides.iterator(); it.hasNext();)
                            {
                                User user = it.next();
                                if (user == null)
                                {
                                    System.out.println("this is null");
                                    continue;
                                }
                                if (user.getId().equals(userId))
                                {
                                    it.remove();    //It still exists in the channel's permissions, exclude it from removal.
                                    break;
                                }
                            }
                            break;
                        }
                        default:
                            throw new IllegalArgumentException("CHANNEL_UPDATE provided an unrecognized PermissionOverride type. JSON: " + content);
                    }
                }

                //If there were any overrides they didn't match the ones in the json, then they were removed.
                //Remove them from the channel's overrides map and put them in the event's map.
                if (!roleOverrides.isEmpty())
                {
                    for (Role role : roleOverrides)
                    {
                        roleOverrideRemoved.put(role, channel.getRolePermissionOverrides().remove(role));
                    }
                }
                if (!userOverrides.isEmpty())
                {
                    for (User user : userOverrides)
                    {
                        userOverrideRemoved.put(user, channel.getUserPermissionOverrides().remove(user));
                    }
                }

                //If this update modified permissions in any way.
                if (!roleOverrideCreate.isEmpty()
                        || !roleOverrideOld.isEmpty()
                        || !roleOverrideRemoved.isEmpty()
                        || !userOverrideCreate.isEmpty()
                        || !userOverrideOld.isEmpty()
                        || !userOverrideRemoved.isEmpty())
                {
                    api.getEventManager().handle(
                            new TextChannelUpdatePermissionsEvent(
                                    api, responseNumber,
                                    channel,
                                    roleOverrideCreate, roleOverrideOld, roleOverrideRemoved,
                                    userOverrideCreate, userOverrideOld, userOverrideRemoved));
                }
                break;  //Finish the TextChannelUpdate case
            }
            case "voice":
            {
                HashMap<Role, PermissionOverride> roleOverrideCreate = new HashMap<>();
                HashMap<Role, PermissionOverride> roleOverrideOld = new HashMap<>();
                HashMap<Role, PermissionOverride> roleOverrideRemoved = new HashMap<>();
                HashMap<User, PermissionOverride> userOverrideCreate = new HashMap<>();
                HashMap<User, PermissionOverride> userOverrideOld = new HashMap<>();
                HashMap<User, PermissionOverride> userOverrideRemoved = new HashMap<>();

                VoiceChannelImpl channel = (VoiceChannelImpl) api.getVoiceChannelMap().get(content.getString("id"));
                if (channel == null)
                    throw new IllegalArgumentException("CHANNEL_UPDATE attemped to update a VoiceChannel that does not exist. JSON: " + content);

                String name = content.getString("name");
                int position = content.getInt("position");
                JSONArray permOverwrites = content.getJSONArray("permission_overwrites");

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
                if (channel.getPosition() != position)
                {
                    int oldPosition = channel.getPosition();
                    channel.setPosition(position);
                    api.getEventManager().handle(
                            new VoiceChannelUpdatePositionEvent(
                                    api, responseNumber,
                                    channel, oldPosition));
                }

                //Determines if a new PermissionOverride was created or updated.
                //If a PermissionOverride was created or updated it stores it in the proper Map to be reported by the Event.
                for (int i = 0; i < permOverwrites.length(); i++)
                {
                    JSONObject override = permOverwrites.getJSONObject(i);
                    PermissionOverride newOverride = new PermissionOverride(override.getInt("allow"), override.getInt("deny"));
                    switch (override.getString("type"))
                    {
                        case "role":
                        {
                            Role role = ((GuildImpl) channel.getGuild()).getRolesMap().get(override.getString("id"));
                            if (role == null)
                                throw new IllegalArgumentException("CHANNEL_UPDATE attempted to create or update a PermissionOverride for a Role that doesn't exist! JSON: " + content);
                            PermissionOverride oldOverride = channel.getRolePermissionOverrides().get(role);

                            if (oldOverride == null)    //Created
                            {
                                roleOverrideCreate.put(role, newOverride);
                                channel.getRolePermissionOverrides().put(role, newOverride);
                            }
                            if (!newOverride.equals(oldOverride))    //Updated
                            {
                                roleOverrideOld.put(role, oldOverride);
                                channel.getRolePermissionOverrides().put(role, newOverride);
                            }
                            break;
                        }
                        case "member":
                        {
                            User user = api.getUserMap().get(override.getString("id"));
                            if (user == null)
                                throw new IllegalArgumentException("CHANNEL_UPDATE attempted to create or update a PermissionOverride for User that doesn't exist! JSON: " + content);
                            PermissionOverride oldOverride = channel.getUserPermissionOverrides().get(user);

                            if (oldOverride == null)    //Created
                            {
                                userOverrideCreate.put(user, newOverride);
                                channel.getUserPermissionOverrides().put(user, newOverride);
                            }
                            else if (!newOverride.equals(oldOverride))  //Updated
                            {
                                userOverrideOld.put(user, oldOverride);
                                channel.getUserPermissionOverrides().put(user, newOverride);
                            }
                            break;
                        }
                        default:
                            throw new IllegalArgumentException("CHANNEL_UPDATE provided an unrecognized PermissionOverride type. JSON: " + content);
                    }
                }

                //Check if any overrides were deleted because of this event.
                //Get the current overrides. (we copy them to a new list because the Set returned is backed by the Map, meaning our removes would remove from the Map. Not good.
                //Loop through all of the json defined overrides. If we find a match, remove the User or Role from our lists.
                //Any entries remaining in these lists after this for loop is over will be removed from the Channel's overrides.
                List<User> userOverrides = toList(channel.getUserPermissionOverrides().keySet()) ;
                List<Role> roleOverrides = toList(channel.getRolePermissionOverrides().keySet());
                for (int i = 0; i < permOverwrites.length(); i++)
                {
                    JSONObject json = permOverwrites.getJSONObject(i);
                    switch (json.getString("type"))
                    {
                        case "role":
                        {
                            String roleId = json.getString("id");
                            for (Iterator<Role> it = roleOverrides.iterator(); it.hasNext();)
                            {
                                Role role = it.next();
                                if (role.getId().equals(roleId))
                                {
                                    it.remove();    //It still exists in the channel's permissions, exclude it from removal.
                                    break;
                                }
                            }
                            break;
                        }
                        case "member":
                        {
                            String userId = json.getString("id");
                            for (Iterator<User> it = userOverrides.iterator(); it.hasNext();)
                            {
                                User user = it.next();
                                if (user.getId().equals(userId))
                                {
                                    it.remove();    //It still exists in the channel's permissions, exclude it from removal.
                                    break;
                                }
                            }
                            break;
                        }
                        default:
                            throw new IllegalArgumentException("CHANNEL_UPDATE provided an unrecognized PermissionOverride type. JSON: " + content);
                    }
                }

                //If there were any overrides they didn't match the ones in the json, then they were removed.
                //Remove them from the channel's overrides map and put them in the event's map.
                if (!roleOverrides.isEmpty())
                {
                    for (Role role : roleOverrides)
                    {
                        roleOverrideRemoved.put(role, channel.getRolePermissionOverrides().remove(role));
                    }
                }
                if (!userOverrides.isEmpty())
                {
                    for (User user : userOverrides)
                    {
                        userOverrideRemoved.put(user, channel.getUserPermissionOverrides().remove(user));
                    }
                }

                //If this update modified permissions in any way.
                if (!roleOverrideCreate.isEmpty()
                        || !roleOverrideOld.isEmpty()
                        || !roleOverrideRemoved.isEmpty()
                        || !userOverrideCreate.isEmpty()
                        || !userOverrideOld.isEmpty()
                        || !userOverrideRemoved.isEmpty())
                {
                    api.getEventManager().handle(
                            new VoiceChannelUpdatePermissionsEvent(
                                    api, responseNumber,
                                    channel,
                                    roleOverrideCreate, roleOverrideOld, roleOverrideRemoved,
                                    userOverrideCreate, userOverrideOld, userOverrideRemoved));
                }
                break;  //Finish the VoiceChannelUpdate Case
            }
            default:
                throw new IllegalArgumentException("CHANNEL_UPDATE provided an unrecognized channel type JSON: " + content);
        }
    }

    private <T> List<T> toList(Set<T> set)
    {
        ArrayList<T> list = new ArrayList<T>();
        for (T t : set)
        {
            list.add(t);
        }
        return list;
    }
}
