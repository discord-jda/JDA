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
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.mentions.AbstractMentions;
import org.apache.commons.collections4.CollectionUtils;

import java.util.regex.Matcher;

public class InteractionMentions extends AbstractMentions
{
    protected final TLongObjectMap<Object> resolved;

    public InteractionMentions(String content, TLongObjectMap<Object> resolved, JDAImpl jda, GuildImpl guild)
    {
        super(content, jda, guild, false);
        this.resolved = resolved;
    }

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

    protected Member matchMember(Matcher matcher)
    {
        long userId = MiscUtil.parseSnowflake(matcher.group(1));
        Object it = resolved.get(userId);
        return it instanceof Member ? (Member) it : null;
    }

    protected GuildChannel matchChannel(Matcher matcher)
    {
        long channelId = MiscUtil.parseSnowflake(matcher.group(1));
        Object it = resolved.get(channelId);
        return it instanceof GuildChannel ? (GuildChannel) it : null;
    }

    protected Role matchRole(Matcher matcher)
    {
        long roleId = MiscUtil.parseSnowflake(matcher.group(1));
        Object it = resolved.get(roleId);
        return it instanceof Role ? (Role) it : null;
    }

    protected boolean isUserMentioned(IMentionable mentionable)
    {
        return resolved.containsKey(mentionable.getIdLong());
    }

    protected boolean isRoleMentioned(IMentionable mentionable)
    {
        if (mentionable instanceof Role)
        {
            return getRoles().contains(mentionable);
        }
        else if (mentionable instanceof Member)
        {
            final Member member = (Member) mentionable;
            return CollectionUtils.containsAny(getRoles(), member.getRoles());
        }
        else if (guild != null && mentionable instanceof User)
        {
            final Member member = guild.getMember((User) mentionable);
            return member != null && CollectionUtils.containsAny(getRoles(), member.getRoles());
        }
        return false;
    }
}
