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

package net.dv8tion.jda.internal.entities;

import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.mentions.AbstractMentions;

import javax.annotation.Nullable;
import java.util.regex.Matcher;

public class InteractionMentions extends AbstractMentions
{
    protected final TLongObjectMap<Object> resolved;

    public InteractionMentions(String content, TLongObjectMap<Object> resolved, JDAImpl jda, @Nullable Guild guild)
    {
        super(content, jda, guild, false);
        this.resolved = resolved;
    }

    @Override
    protected User matchUser(Matcher matcher)
    {
        long userId = MiscUtil.parseSnowflake(matcher.group(1));
        Object it = resolved.get(userId);
        return it instanceof User
                ? (User) it
                : it instanceof Member
                    ? ((Member) it).getUser()
                    : null;
    }

    @Override
    protected Member matchMember(Matcher matcher)
    {
        long userId = MiscUtil.parseSnowflake(matcher.group(1));
        Object it = resolved.get(userId);
        return it instanceof Member ? (Member) it : null;
    }

    @Override
    protected GuildChannel matchChannel(Matcher matcher)
    {
        long channelId = MiscUtil.parseSnowflake(matcher.group(1));
        Object it = resolved.get(channelId);
        return it instanceof GuildChannel ? (GuildChannel) it : null;
    }

    @Override
    protected Role matchRole(Matcher matcher)
    {
        long roleId = MiscUtil.parseSnowflake(matcher.group(1));
        Object it = resolved.get(roleId);
        return it instanceof Role ? (Role) it : null;
    }

    @Override
    protected boolean isUserMentioned(IMentionable mentionable)
    {
        return resolved.containsKey(mentionable.getIdLong())
                && (content.contains("<@!" + mentionable.getId() + ">") || content.contains("<@" + mentionable.getId() + ">"));
    }
}
