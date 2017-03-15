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
import net.dv8tion.jda.client.entities.impl.JDAClientImpl;
import net.dv8tion.jda.client.events.group.GroupLeaveEvent;
import net.dv8tion.jda.core.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.entities.impl.GuildImpl;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.entities.impl.UserImpl;
import net.dv8tion.jda.core.events.channel.priv.PrivateChannelDeleteEvent;
import net.dv8tion.jda.core.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.core.events.channel.voice.VoiceChannelDeleteEvent;
import net.dv8tion.jda.core.managers.impl.AudioManagerImpl;
import net.dv8tion.jda.core.requests.GuildLock;
import org.json.JSONObject;

public class ChannelDeleteHandler extends SocketHandler
{
    public ChannelDeleteHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected String handleInternally(JSONObject content)
    {
        ChannelType type = ChannelType.fromId(content.getInt("type"));
        if (type == ChannelType.TEXT || type == ChannelType.VOICE)
        {
            if (GuildLock.get(api).isLocked(content.getString("guild_id")))
            {
                return content.getString("guild_id");
            }
        }

        switch (type)
        {
            case TEXT:
            {
                GuildImpl guild = (GuildImpl) api.getGuildMap().get(content.getString("guild_id"));
                TextChannel channel = api.getTextChannelMap().remove(content.getString("id"));
                if (channel == null)
                {
                    EventCache.get(api).cache(EventCache.Type.CHANNEL, content.getString("id"), () ->
                    {
                        handle(responseNumber, allContent);
                    });
                    EventCache.LOG.debug("CHANNEL_DELETE attempted to delete a text channel that is not yet cached. JSON: " + content);
                    return null;
                }

                guild.getTextChannelsMap().remove(channel.getId());
                api.getEventManager().handle(
                        new TextChannelDeleteEvent(
                                api, responseNumber,
                                channel));
                break;
            }
            case VOICE:
            {
                GuildImpl guild = (GuildImpl) api.getGuildMap().get(content.getString("guild_id"));
                VoiceChannel channel = guild.getVoiceChannelMap().remove(content.getString("id"));
                if (channel == null)
                {
                    EventCache.get(api).cache(EventCache.Type.CHANNEL, content.getString("id"), () ->
                    {
                        handle(responseNumber, allContent);
                    });
                    EventCache.LOG.debug("CHANNEL_DELETE attempted to delete a voice channel that is not yet cached. JSON: " + content);
                    return null;
                }

                //We use this instead of getAudioManager(Guild) so we don't create a new instance. Efficiency!
                AudioManagerImpl manager = (AudioManagerImpl) api.getAudioManagerMap().get(guild.getId());
                if (manager != null && manager.isConnected()
                        && manager.getConnectedChannel().getId().equals(channel.getId()))
                {
                    manager.closeAudioConnection(ConnectionStatus.DISCONNECTED_CHANNEL_DELETED);
                }
                guild.getVoiceChannelMap().remove(channel.getId());
                api.getEventManager().handle(
                        new VoiceChannelDeleteEvent(
                                api, responseNumber,
                                channel));
                break;
            }
            case PRIVATE:
            {
                String channelId = content.getString("id");
                PrivateChannel channel = api.getPrivateChannelMap().remove(channelId);

                if (channel == null)
                    channel = api.getFakePrivateChannelMap().remove(channelId);
                if (channel == null)
                {
                    EventCache.get(api).cache(EventCache.Type.CHANNEL, content.getString("id"), () ->
                    {
                        handle(responseNumber, allContent);
                    });
                    EventCache.LOG.debug("CHANNEL_DELETE attempted to delete a private channel that is not yet cached. JSON: " + content);
                    return null;
                }

                if (channel.getUser().isFake())
                    api.getFakeUserMap().remove(channel.getUser().getId());

                ((UserImpl) channel.getUser()).setPrivateChannel(null);

                api.getEventManager().handle(
                        new PrivateChannelDeleteEvent(
                                api, responseNumber,
                                channel));
                break;
            }
            case GROUP:
            {
                //TODO: close call on group leave (kill audio manager)
                String groupId = content.getString("id");
                GroupImpl group = (GroupImpl) ((JDAClientImpl) api.asClient()).getGroupMap().remove(groupId);
                if (group == null)
                {
                    EventCache.get(api).cache(EventCache.Type.CHANNEL, content.getString("id"), () ->
                    {
                        handle(responseNumber, allContent);
                    });
                    EventCache.LOG.debug("CHANNEL_DELETE attempted to delete a group that is not yet cached. JSON: " + content);
                    return null;
                }

                group.getUserMap().forEach((userId, user) ->
                {
                    //User is fake, has no privateChannel, is not in a relationship, and is not in any other groups
                    // then we remove the fake user from the fake cache as it was only in this group
                    //Note: we getGroups() which gets all groups, however we already removed the current group above.
                    if (user.isFake()
                            && !user.hasPrivateChannel()
                            && api.asClient().getRelationshipById(userId) == null
                            && api.asClient().getGroups().stream().allMatch(g -> !g.getUsers().contains(user)))
                    {
                        api.getFakeUserMap().remove(userId);
                    }
                });

                api.getEventManager().handle(
                        new GroupLeaveEvent(
                                api, responseNumber,
                                group));
                break;
            }
            default:
                throw new IllegalArgumentException("CHANNEL_DELETE provided an unknown channel type. JSON: " + content);
        }
        return null;
    }
}
