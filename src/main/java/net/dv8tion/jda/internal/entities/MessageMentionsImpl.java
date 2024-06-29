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

import gnu.trove.iterator.TLongIterator;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.mentions.AbstractMentions;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class MessageMentionsImpl extends AbstractMentions
{
    private final TLongObjectMap<DataObject> userMentionMap;
    private final TLongSet roleMentionMap;

    public MessageMentionsImpl(JDAImpl jda, GuildImpl guild, String content,
                               boolean mentionsEveryone, DataArray userMentions, DataArray roleMentions)
    {
        super(content, jda, guild, mentionsEveryone);
        this.userMentionMap = new TLongObjectHashMap<>(userMentions.length());
        this.roleMentionMap = new TLongHashSet(roleMentions.stream(DataArray::getUnsignedLong).collect(Collectors.toList()));

        userMentions.stream(DataArray::getObject)
                .forEach(obj -> {
                    if (obj.isNull("member"))
                    {
                        this.userMentionMap.put(obj.getUnsignedLong("id"), obj.put("is_member", false));
                        return;
                    }

                    DataObject member = obj.getObject("member");
                    obj.remove("member");
                    member.put("user", obj).put("is_member", true);
                    this.userMentionMap.put(obj.getUnsignedLong("id"), member);
                });



        // Eager parsing member mentions for caching purposes
        getMembers();
    }

    @Nonnull
    @Override
    public synchronized List<Member> getMembers()
    {
        if (guild == null)
            return Collections.emptyList();
        if (mentionedMembers != null)
            return mentionedMembers;

        // Parse members from mentions array in order of appearance
        EntityBuilder entityBuilder = jda.getEntityBuilder();
        TLongSet unseen = new TLongHashSet(userMentionMap.keySet());
        List<Member> members = processMentions(Message.MentionType.USER, false, (matcher) -> {
            if (unseen.remove(Long.parseUnsignedLong(matcher.group(1))))
                return matchMember(matcher);
            return null;
        }, Collectors.toCollection(ArrayList::new));

        // Add reply mentions at beginning
        for (TLongIterator iter = unseen.iterator(); iter.hasNext();)
        {
            DataObject mention = userMentionMap.get(iter.next());
            if (mention.getBoolean("is_member"))
                members.add(0, entityBuilder.createMember((GuildImpl) guild, mention));
        }

        // Update member cache
        members.stream()
               .map(MemberImpl.class::cast)
               .forEach(entityBuilder::updateMemberCache);

        return mentionedMembers = Collections.unmodifiableList(members);
    }

    @Nonnull
    @Override
    public synchronized List<User> getUsers()
    {
        if (mentionedUsers != null)
            return mentionedUsers;

        // Parse members from mentions array in order of appearance
        EntityBuilder entityBuilder = jda.getEntityBuilder();
        TLongSet unseen = new TLongHashSet(userMentionMap.keySet());
        List<User> users = processMentions(Message.MentionType.USER, false, (matcher) -> {
            if (unseen.remove(Long.parseUnsignedLong(matcher.group(1))))
                return matchUser(matcher);
            return null;
        }, Collectors.toCollection(ArrayList::new));


        // Add reply mentions at beginning
        for (TLongIterator iter = unseen.iterator(); iter.hasNext();)
        {
            DataObject mention = userMentionMap.get(iter.next());
            if (mention.getBoolean("is_member"))
                users.add(0, entityBuilder.createUser(mention.getObject("user")));
            else
                users.add(0, entityBuilder.createUser(mention));
        }

        return mentionedUsers = Collections.unmodifiableList(users);
    }

    @Override
    protected User matchUser(Matcher matcher)
    {
        long userId = MiscUtil.parseSnowflake(matcher.group(1));
        DataObject mention = userMentionMap.get(userId);
        if (mention == null)
            return null;
        if (!mention.getBoolean("is_member"))
            return jda.getEntityBuilder().createUser(mention);
        Member member = matchMember(matcher);
        return member == null ? null : member.getUser();
    }

    @Override
    protected Member matchMember(Matcher matcher)
    {
        long id = Long.parseUnsignedLong(matcher.group(1));
        DataObject member = userMentionMap.get(id);
        return member != null && member.getBoolean("is_member")
                ? jda.getEntityBuilder().createMember((GuildImpl) guild, member)
                : null;
    }

    @Override
    protected GuildChannel matchChannel(Matcher matcher)
    {
        long channelId = MiscUtil.parseSnowflake(matcher.group(1));
        return getJDA().getGuildChannelById(channelId);
    }

    @Override
    protected Role matchRole(Matcher matcher)
    {
        long roleId = MiscUtil.parseSnowflake(matcher.group(1));
        if (!roleMentionMap.contains(roleId))
            return null;
        if (guild != null)
            return guild.getRoleById(roleId);
        else
            return getJDA().getRoleById(roleId);
    }

    @Override
    protected boolean isUserMentioned(IMentionable mentionable)
    {
        return userMentionMap.containsKey(mentionable.getIdLong());
    }
}
