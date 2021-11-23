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

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.user.UserTypingEvent;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.MemberImpl;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class TypingStartHandler extends SocketHandler
{

    public TypingStartHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content)
    {
        GuildImpl guild = null;
        if (!content.isNull("guild_id"))
        {
            long guildId = content.getUnsignedLong("guild_id");
            guild = (GuildImpl) getJDA().getGuildById(guildId);
            if (getJDA().getGuildSetupController().isLocked(guildId))
                return guildId;
            else if (guild == null)
                return null; // Don't cache typing events
        }

        final long channelId = content.getLong("channel_id");

        //TODO-v5-unified-channel-cache
        MessageChannel channel = getJDA().getTextChannelsView().get(channelId);
        if (channel == null)
            channel = getJDA().getNewsChannelView().get(channelId);
        if (channel == null)
            channel = getJDA().getPrivateChannelsView().get(channelId);
        if (channel == null)
            return null;    //We don't have the channel cached yet. We chose not to cache this event
                            // because that happen very often and could easily fill up the EventCache if
                            // we, for some reason, never get the channel. Especially in an active channel.

        final long userId = content.getLong("user_id");
        User user;
        MemberImpl member = null;
        if (channel instanceof PrivateChannel)
            user = ((PrivateChannel) channel).getUser();
        else
            user = getJDA().getUsersView().get(userId);
        if (!content.isNull("member"))
        {
            // Try to load member for the typing event
            EntityBuilder entityBuilder = getJDA().getEntityBuilder();
            member = entityBuilder.createMember(guild, content.getObject("member"));
            entityBuilder.updateMemberCache(member);
            user = member.getUser();
        }

        if (user == null)
            return null;    //Just like in the comment above, if for some reason we don't have the user
                            // then we will just throw the event away.

        OffsetDateTime timestamp = Instant.ofEpochSecond(content.getInt("timestamp")).atOffset(ZoneOffset.UTC);
        getJDA().handleEvent(
            new UserTypingEvent(
                getJDA(), responseNumber,
                user, channel, timestamp, member));
        return null;
    }
}
