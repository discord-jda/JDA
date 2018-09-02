/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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
import net.dv8tion.jda.client.events.group.GroupLeaveEvent;
import net.dv8tion.jda.core.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.entities.impl.GuildImpl;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.entities.impl.UserImpl;
import net.dv8tion.jda.core.events.channel.category.CategoryDeleteEvent;
import net.dv8tion.jda.core.events.channel.priv.PrivateChannelDeleteEvent;
import net.dv8tion.jda.core.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.core.events.channel.voice.VoiceChannelDeleteEvent;
import net.dv8tion.jda.core.managers.impl.AudioManagerImpl;
import net.dv8tion.jda.core.requests.WebSocketClient;
import org.json.JSONObject;

public class ChannelDeleteHandler extends SocketHandler
{
    public ChannelDeleteHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(JSONObject content)
    {
        ChannelType type = ChannelType.fromId(content.getInt("type"));

        long guildId = 0;
        if (type.isGuild())
        {
            guildId = content.getLong("guild_id");
            if (getJDA().getGuildSetupController().isLocked(guildId))
                return guildId;
        }

        final long channelId = content.getLong("id");

        switch (type)
        {
            case TEXT:
            {
                GuildImpl guild = (GuildImpl) getJDA().getGuildMap().get(guildId);
                TextChannel channel = getJDA().getTextChannelMap().remove(channelId);
                if (channel == null)
                {
//                    getJDA().getEventCache().cache(EventCache.Type.CHANNEL, channelId, () -> handle(responseNumber, allContent));
                    WebSocketClient.LOG.debug("CHANNEL_DELETE attempted to delete a text channel that is not yet cached. JSON: {}", content);
                    return null;
                }

                guild.getTextChannelsMap().remove(channel.getIdLong());
                getJDA().getEventManager().handle(
                    new TextChannelDeleteEvent(
                        getJDA(), responseNumber,
                        channel));
                break;
            }
            case VOICE:
            {
                GuildImpl guild = (GuildImpl) getJDA().getGuildMap().get(guildId);
                VoiceChannel channel = getJDA().getVoiceChannelMap().remove(channelId);
                if (channel == null)
                {
//                    getJDA().getEventCache().cache(EventCache.Type.CHANNEL, channelId, () -> handle(responseNumber, allContent));
                    WebSocketClient.LOG.debug("CHANNEL_DELETE attempted to delete a voice channel that is not yet cached. JSON: {}", content);
                    return null;
                }

                //We use this instead of getAudioManager(Guild) so we don't create a new instance. Efficiency!
                AudioManagerImpl manager = (AudioManagerImpl) getJDA().getAudioManagerMap().get(guild.getIdLong());
                if (manager != null && manager.isConnected()
                        && manager.getConnectedChannel().getIdLong() == channel.getIdLong())
                {
                    manager.closeAudioConnection(ConnectionStatus.DISCONNECTED_CHANNEL_DELETED);
                }
                guild.getVoiceChannelsMap().remove(channel.getIdLong());
                getJDA().getEventManager().handle(
                    new VoiceChannelDeleteEvent(
                        getJDA(), responseNumber,
                        channel));
                break;
            }
            case CATEGORY:
            {
                GuildImpl guild = (GuildImpl) getJDA().getGuildMap().get(guildId);
                Category category = getJDA().getCategoryMap().remove(channelId);
                if (category == null)
                {
//                    getJDA().getEventCache().cache(EventCache.Type.CHANNEL, channelId, () -> handle(responseNumber, allContent));
                    WebSocketClient.LOG.debug("CHANNEL_DELETE attempted to delete a category channel that is not yet cached. JSON: {}", content);
                    return null;
                }

                guild.getCategoriesMap().remove(channelId);
                getJDA().getEventManager().handle(
                    new CategoryDeleteEvent(
                        getJDA(), responseNumber,
                        category));
                break;
            }
            case PRIVATE:
            {
                PrivateChannel channel = getJDA().getPrivateChannelMap().remove(channelId);

                if (channel == null)
                    channel = getJDA().getFakePrivateChannelMap().remove(channelId);
                if (channel == null)
                {
//                    getJDA().getEventCache().cache(EventCache.Type.CHANNEL, channelId, () -> handle(responseNumber, allContent));
                    WebSocketClient.LOG.debug("CHANNEL_DELETE attempted to delete a private channel that is not yet cached. JSON: {}", content);
                    return null;
                }

                if (channel.getUser().isFake())
                    getJDA().getFakeUserMap().remove(channel.getUser().getIdLong());

                ((UserImpl) channel.getUser()).setPrivateChannel(null);

                getJDA().getEventManager().handle(
                    new PrivateChannelDeleteEvent(
                        getJDA(), responseNumber,
                        channel));
                break;
            }
            case GROUP:
            {
                //TODO: close call on group leave (kill audio manager)
                final long groupId = content.getLong("id");
                GroupImpl group = (GroupImpl) getJDA().asClient().getGroupMap().remove(groupId);
                if (group == null)
                {
//                    getJDA().getEventCache().cache(EventCache.Type.CHANNEL, channelId, () -> handle(responseNumber, allContent));
                    WebSocketClient.LOG.debug("CHANNEL_DELETE attempted to delete a group that is not yet cached. JSON: {}", content);
                    return null;
                }

                group.getUserMap().forEachEntry((userId, user) ->
                {
                    //User is fake, has no privateChannel, is not in a relationship, and is not in any other groups
                    // then we remove the fake user from the fake cache as it was only in this group
                    //Note: we getGroups() which gets all groups, however we already removed the current group above.
                    if (user.isFake()
                            && !user.hasPrivateChannel()
                            && getJDA().asClient().getRelationshipMap().get(userId) == null
                            && getJDA().asClient().getGroups().stream().noneMatch(g -> g.getUsers().contains(user)))
                    {
                        getJDA().getFakeUserMap().remove(userId);
                    }

                    return true;
                });

                getJDA().getEventManager().handle(
                    new GroupLeaveEvent(
                        getJDA(), responseNumber,
                        group));
                break;
            }
            default:
                throw new IllegalArgumentException("CHANNEL_DELETE provided an unknown channel type. JSON: " + content);
        }
        getJDA().getEventCache().clear(EventCache.Type.CHANNEL, channelId);
        return null;
    }
}
