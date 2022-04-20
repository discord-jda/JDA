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
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.mentions.AbstractMentions;
import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.bag.HashBag;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

public class MessageMentionsImpl extends AbstractMentions
{
    private final TLongObjectMap<DataObject> mentionedUsers;
    private final TLongObjectMap<DataObject> mentionedRoles;

    public MessageMentionsImpl(JDAImpl jda, GuildImpl guild, String content,
                               boolean mentionsEveryone, DataArray userMentions, DataArray roleMentions)
    {
        super(content, jda, guild, mentionsEveryone);
        this.mentionedUsers = new TLongObjectHashMap<>(userMentions.length());
        this.mentionedRoles = new TLongObjectHashMap<>(roleMentions.length());

        userMentions.stream(DataArray::getObject)
                .forEach(obj -> {
                    if (obj.isNull("member"))
                    {
                        this.mentionedUsers.put(obj.getUnsignedLong("id"), obj.put("is_member", false));
                        return;
                    }

                    DataObject member = obj.getObject("member");
                    obj.remove("user");
                    member.put("user", obj).put("is_member", true);
                    this.mentionedUsers.put(obj.getUnsignedLong("id"), member);
                });

        roleMentions.stream(DataArray::getObject)
                .forEach(obj -> mentionedRoles.put(obj.getUnsignedLong("id"), obj));

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
        TLongSet unseen = new TLongHashSet(mentionedUsers.keySet());
        ArrayList<Member> members = processMentions(Message.MentionType.USER, new ArrayList<>(), true, this::matchMember);

        // Add reply mentions at beginning
        for (TLongIterator iter = unseen.iterator(); iter.hasNext();)
            members.add(0, entityBuilder.createMember(guild, mentionedUsers.get(iter.next())));

        // Update member cache
        members.stream()
               .map(MemberImpl.class::cast)
               .forEach(entityBuilder::updateMemberCache);

        return mentionedMembers = Collections.unmodifiableList(members);
    }

    @Nonnull
    @Override
    public Bag<Member> getMembersBag()
    {
        if (guild == null)
            return new HashBag<>();
        return processMentions(Message.MentionType.USER, new HashBag<>(), false, this::matchMember);
    }

    protected User matchUser(Matcher matcher)
    {
        long userId = MiscUtil.parseSnowflake(matcher.group(1));
        DataObject mention = mentionedUsers.get(userId);
        if (mention == null)
            return null;
        if (!mention.getBoolean("is_member"))
            return jda.getEntityBuilder().createUser(mention);
        Member member = matchMember(matcher);
        return member == null ? null : member.getUser();
    }

    protected Member matchMember(Matcher matcher)
    {
        long id = Long.parseUnsignedLong(matcher.group(1));
        DataObject member = mentionedUsers.get(id);
        return member != null && member.getBoolean("is_member")
                ? jda.getEntityBuilder().createMember(guild, member)
                : null;
    }

    protected GuildChannel matchChannel(Matcher matcher)
    {
        long channelId = MiscUtil.parseSnowflake(matcher.group(1));
        return getJDA().getGuildChannelById(channelId);
    }

    protected Role matchRole(Matcher matcher)
    {
        long roleId = MiscUtil.parseSnowflake(matcher.group(1));
        if (!mentionedRoles.containsKey(roleId))
            return null;
        if (guild != null)
            return guild.getRoleById(roleId);
        else
            return getJDA().getRoleById(roleId);
    }

    protected Emote matchEmote(Matcher m)
    {
        long emoteId = MiscUtil.parseSnowflake(m.group(2));
        String name = m.group(1);
        boolean animated = m.group(0).startsWith("<a:");
        Emote emote = getJDA().getEmoteById(emoteId);
        if (emote == null)
            emote = new EmoteImpl(emoteId, jda).setName(name).setAnimated(animated);
        return emote;
    }
}
