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

import net.dv8tion.jda.client.events.group.GroupJoinEvent;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.events.channel.category.CategoryCreateEvent;
import net.dv8tion.jda.core.events.channel.priv.PrivateChannelCreateEvent;
import net.dv8tion.jda.core.events.channel.text.TextChannelCreateEvent;
import net.dv8tion.jda.core.events.channel.voice.VoiceChannelCreateEvent;
import org.json.JSONObject;

public class ChannelCreateHandler extends SocketHandler
{
    public ChannelCreateHandler(JDAImpl api)
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

        switch (type)
        {
            case TEXT:
            {
                getJDA().getEventManager().handle(
                    new TextChannelCreateEvent(
                        getJDA(), responseNumber,
                        getJDA().getEntityBuilder().createTextChannel(content, guildId)));
                break;
            }
            case VOICE:
            {
                getJDA().getEventManager().handle(
                    new VoiceChannelCreateEvent(
                        getJDA(), responseNumber,
                        getJDA().getEntityBuilder().createVoiceChannel(content, guildId)));
                break;
            }
            case CATEGORY:
            {
                getJDA().getEventManager().handle(
                    new CategoryCreateEvent(
                        getJDA(), responseNumber,
                        getJDA().getEntityBuilder().createCategory(content, guildId)));
                break;
            }
            case PRIVATE:
            {
                getJDA().getEventManager().handle(
                    new PrivateChannelCreateEvent(
                        getJDA(), responseNumber,
                        getJDA().getEntityBuilder().createPrivateChannel(content)));
                break;
            }
            case GROUP:
            {
                getJDA().getEventManager().handle(
                    new GroupJoinEvent(
                        getJDA(), responseNumber,
                        getJDA().getEntityBuilder().createGroup(content)));
                break;
            }
            default:
                throw new IllegalArgumentException("Discord provided an CREATE_CHANNEL event with an unknown channel type! JSON: " + content);
        }
        return null;
    }
}
