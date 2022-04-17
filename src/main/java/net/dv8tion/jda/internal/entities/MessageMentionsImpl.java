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
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.utils.Checks;
import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.bag.HashBag;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class MessageMentionsImpl implements MessageMentions
{
    private final JDAImpl jda;
    private final GuildImpl guild;
    private final String content;
    private final boolean hasMessageReference;
    private final boolean mentionsEveryone;
    private final TLongObjectMap<DataObject> mentionedMembers;
    private final TLongObjectMap<DataObject> mentionedRoles;

    private List<Member> memberMentions = null;
    private List<Emote> emoteMentions = null;
    private List<Role> roleMentions = null;
    private List<GuildChannel> channelMentions = null;

    public MessageMentionsImpl(JDAImpl jda, GuildImpl guild, String content, boolean hasReference,
                               boolean mentionsEveryone, DataArray userMentions, DataArray roleMentions)
    {
        this.jda = jda;
        this.guild = guild;
        this.content = content;
        this.hasMessageReference = hasReference;
        this.mentionsEveryone = mentionsEveryone;
        this.mentionedMembers = new TLongObjectHashMap<>(userMentions.length());
        this.mentionedRoles = new TLongObjectHashMap<>(roleMentions.length());

        userMentions.stream(DataArray::getObject)
                .forEach(obj -> mentionedMembers.put(obj.getObject("user").getUnsignedLong("id"), obj));
        roleMentions.stream(DataArray::getObject)
                .forEach(obj -> mentionedRoles.put(obj.getUnsignedLong("id"), obj));
    }

    @Nonnull
    @Override
    public JDA getJDA()
    {
        return jda;
    }

    @Override
    public boolean mentionsEveryone()
    {
        return mentionsEveryone;
    }

    @Nonnull
    @Override
    public List<User> getUsers()
    {
        List<Member> members = getMembers();
        return members.stream().map(Member::getUser).collect(Collectors.toList());
    }

    @Nonnull
    @Override
    public Bag<User> getUsersBag()
    {
        return processMentions(Message.MentionType.USER, new HashBag<>(), false, this::matchUser);
    }

    @Nonnull
    @Override
    public synchronized List<GuildChannel> getChannels()
    {
        if (channelMentions == null)
            channelMentions = Collections.unmodifiableList(processMentions(Message.MentionType.CHANNEL, new ArrayList<>(), true, this::matchChannel));
        return channelMentions;
    }

    @Nonnull
    @Override
    public Bag<GuildChannel> getChannelsBag()
    {
        return processMentions(Message.MentionType.CHANNEL, new HashBag<>(), false, this::matchChannel);
    }

    @Nonnull
    @Override
    public synchronized List<Role> getRoles()
    {
        if (roleMentions == null)
            roleMentions = Collections.unmodifiableList(processMentions(Message.MentionType.ROLE, new ArrayList<>(), true, this::matchRole));
        return roleMentions;
    }

    @Nonnull
    @Override
    public Bag<Role> getRolesBag()
    {
        return processMentions(Message.MentionType.ROLE, new HashBag<>(), false, this::matchRole);
    }

    @Nonnull
    @Override
    public synchronized List<Member> getMembers()
    {
        if (guild == null)
            return Collections.emptyList();
        if (memberMentions != null)
            return memberMentions;

        // Parse members from mentions array in order of appearance
        EntityBuilder entityBuilder = jda.getEntityBuilder();
        TLongSet unseen = new TLongHashSet(mentionedMembers.keySet());
        ArrayList<Member> members = processMentions(Message.MentionType.USER, new ArrayList<>(), true, (matcher) -> {
            long id = Long.parseUnsignedLong(matcher.group(1));
            DataObject member = mentionedMembers.get(id);
            unseen.remove(id);
            return member == null ? null : entityBuilder.createMember(guild, member);
        });

        // Add reply mention at first index
        if (hasMessageReference && !unseen.isEmpty())
            members.add(0, entityBuilder.createMember(guild, mentionedMembers.get(unseen.iterator().next())));

        // Update member cache
        members.stream()
               .map(MemberImpl.class::cast)
               .forEach(entityBuilder::updateMemberCache);

        return memberMentions = Collections.unmodifiableList(members);
    }

    @Nonnull
    @Override
    public Bag<Member> getMembersBag()
    {
        if (guild == null)
            return new HashBag<>();
        return processMentions(Message.MentionType.USER, new HashBag<>(), false, this::matchMember);
    }

    @Nonnull
    @Override
    public synchronized List<Emote> getEmotes()
    {
        if (emoteMentions == null)
            emoteMentions = Collections.unmodifiableList(processMentions(Message.MentionType.EMOTE, new ArrayList<>(), true, this::matchEmote));
        return emoteMentions;
    }

    @Nonnull
    @Override
    public Bag<Emote> getEmotesBag()
    {
        return processMentions(Message.MentionType.EMOTE, new HashBag<>(), false, this::matchEmote);
    }

    @Nonnull
    @Override
    public List<IMentionable> getMentions(@Nonnull Message.MentionType... types)
    {
        if (types == null || types.length == 0)
            return getMentions(Message.MentionType.values());
        List<IMentionable> mentions = new ArrayList<>();
        // boolean duplicate checks
        // not using Set because channel and role might have the same ID
        boolean channel = false;
        boolean role = false;
        boolean user = false;
        boolean emote = false;
        for (Message.MentionType type : types)
        {
            switch (type)
            {
            case EVERYONE:
            case HERE:
            default: continue;
            case CHANNEL:
                if (!channel)
                    mentions.addAll(getChannels());
                channel = true;
                break;
            case USER:
                if (!user)
                    mentions.addAll(getMembers());
                user = true;
                break;
            case ROLE:
                if (!role)
                    mentions.addAll(getRoles());
                role = true;
                break;
            case EMOTE:
                if (!emote)
                    mentions.addAll(getEmotes());
                emote = true;
            }
        }
        return Collections.unmodifiableList(mentions);
    }

    @Override
    public boolean isMentioned(@Nonnull IMentionable mentionable, @Nonnull Message.MentionType... types)
    {
        Checks.notNull(types, "Mention Types");
        if (types.length == 0)
            return isMentioned(mentionable, Message.MentionType.values());
        final boolean isUserEntity = mentionable instanceof User || mentionable instanceof Member;
        for (Message.MentionType type : types)
        {
            switch (type)
            {
            case HERE:
            {
                if (isMass("@here") && isUserEntity)
                    return true;
                break;
            }
            case EVERYONE:
            {
                if (isMass("@everyone") && isUserEntity)
                    return true;
                break;
            }
            case USER:
            {
                if (isUserMentioned(mentionable))
                    return true;
                break;
            }
            case ROLE:
            {
                if (isRoleMentioned(mentionable))
                    return true;
                break;
            }
            case CHANNEL:
            {
                if (mentionable instanceof TextChannel)
                {
                    if (getChannels().contains(mentionable))
                        return true;
                }
                break;
            }
            case EMOTE:
            {
                if (mentionable instanceof Emote)
                {
                    if (getEmotes().contains(mentionable))
                        return true;
                }
                break;
            }
//              default: continue;
            }
        }
        return false;
    }

    // ============= Internal Helpers =================

    private <T, C extends Collection<T>> C processMentions(Message.MentionType type, C collection, boolean distinct, Function<Matcher, T> map)
    {
        Matcher matcher = type.getPattern().matcher(content);
        while (matcher.find())
        {
            try
            {
                T elem = map.apply(matcher);
                if (elem == null || (distinct && collection.contains(elem)))
                    continue;
                collection.add(elem);
            }
            catch (NumberFormatException ignored) {}
        }
        return collection;
    }

    private User matchUser(Matcher matcher)
    {
        long userId = MiscUtil.parseSnowflake(matcher.group(1));
        if (!mentionedMembers.containsKey(userId))
            return null;
        User user = getJDA().getUserById(userId);
        if (user == null)
        {
            user = getMembers().stream()
                        .filter(it -> it.getIdLong() == userId)
                        .map(Member::getUser)
                        .findFirst()
                        .orElse(null);
        }
        return user;
    }

    private Member matchMember(Matcher matcher)
    {
        long userId = MiscUtil.parseSnowflake(matcher.group(1));
        if (!mentionedMembers.containsKey(userId))
            return null;
        Member member = guild.getMemberById(userId);
        if (member == null)
        {
            member = getMembers().stream()
                        .filter(it -> it.getIdLong() == userId)
                        .findFirst()
                        .orElse(null);
        }
        return member;
    }

    private GuildChannel matchChannel(Matcher matcher)
    {
        long channelId = MiscUtil.parseSnowflake(matcher.group(1));
        return getJDA().getGuildChannelById(channelId);
    }

    private Role matchRole(Matcher matcher)
    {
        long roleId = MiscUtil.parseSnowflake(matcher.group(1));
        if (!mentionedRoles.containsKey(roleId))
            return null;
        if (guild != null)
            return guild.getRoleById(roleId);
        else
            return getJDA().getRoleById(roleId);
    }

    private Emote matchEmote(Matcher m)
    {
        long emoteId = MiscUtil.parseSnowflake(m.group(2));
        String name = m.group(1);
        boolean animated = m.group(0).startsWith("<a:");
        Emote emote = getJDA().getEmoteById(emoteId);
        if (emote == null)
            emote = new EmoteImpl(emoteId, jda).setName(name).setAnimated(animated);
        return emote;
    }

    private boolean isUserMentioned(IMentionable mentionable)
    {
        if (mentionable instanceof User)
        {
            return getUsers().contains(mentionable);
        }
        else if (mentionable instanceof Member)
        {
            final Member member = (Member) mentionable;
            return getUsers().contains(member.getUser());
        }
        return false;
    }

    private boolean isRoleMentioned(IMentionable mentionable)
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

    private boolean isMass(String s)
    {
        return mentionsEveryone && content.contains(s);
    }
}
