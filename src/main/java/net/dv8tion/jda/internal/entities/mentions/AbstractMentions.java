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

package net.dv8tion.jda.internal.entities.mentions;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.EmoteImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.utils.Checks;
import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.bag.HashBag;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public abstract class AbstractMentions implements Mentions
{
    protected final String content;
    protected final JDAImpl jda;
    protected final GuildImpl guild;
    protected final boolean mentionsEveryone;

    protected List<User> mentionedUsers;
    protected List<Member> mentionedMembers;
    protected List<Role> mentionedRoles;
    protected List<GuildChannel> mentionedChannels;
    protected List<Emote> mentionedEmotes;

    public AbstractMentions(String content, JDAImpl jda, GuildImpl guild, boolean mentionsEveryone)
    {
        this.content = content;
        this.jda = jda;
        this.guild = guild;
        this.mentionsEveryone = mentionsEveryone;
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
    public synchronized List<User> getUsers()
    {
        if (mentionedUsers != null)
            return mentionedUsers;
        return mentionedUsers = Collections.unmodifiableList(processMentions(Message.MentionType.USER, new ArrayList<>(), true, this::matchUser));
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
        if (mentionedChannels != null)
            return mentionedChannels;
        return mentionedChannels = Collections.unmodifiableList(processMentions(Message.MentionType.CHANNEL, new ArrayList<>(), true, this::matchChannel));
    }

    @Nonnull
    @Override
    public Bag<GuildChannel> getChannelsBag()
    {
        return processMentions(Message.MentionType.CHANNEL, new HashBag<>(), false, this::matchChannel);
    }

    @Nonnull
    @Override
    public <T extends GuildChannel> List<T> getChannels(@Nonnull Class<T> clazz)
    {
        Checks.notNull(clazz, "clazz");
        return getChannels().stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .collect(Collectors.toList());
    }

    @Nonnull
    @Override
    public <T extends GuildChannel> Bag<T> getChannelsBag(@Nonnull Class<T> clazz)
    {
        Checks.notNull(clazz, "clazz");
        Function<Matcher, T> matchTypedChannel = matcher -> {
            GuildChannel channel = this.matchChannel(matcher);
            return clazz.isInstance(channel) ? clazz.cast(channel) : null;
        };

        return processMentions(Message.MentionType.CHANNEL, new HashBag<>(), false, matchTypedChannel);
    }

    @Nonnull
    @Override
    public synchronized List<Role> getRoles()
    {
        if (guild == null)
            return Collections.emptyList();
        if (mentionedRoles != null)
            return mentionedRoles;
        return mentionedRoles = Collections.unmodifiableList(processMentions(Message.MentionType.ROLE, new ArrayList<>(), true, this::matchRole));
    }

    @Nonnull
    @Override
    public Bag<Role> getRolesBag()
    {
        if (guild == null)
            return new HashBag<>();
        return processMentions(Message.MentionType.ROLE, new HashBag<>(), false, this::matchRole);
    }

    @Nonnull
    @Override
    public synchronized List<Emote> getEmotes()
    {
        if (mentionedEmotes != null)
            return mentionedEmotes;
        return mentionedEmotes = Collections.unmodifiableList(processMentions(Message.MentionType.EMOTE, new ArrayList<>(), true, this::matchEmote));
    }

    @Nonnull
    @Override
    public Bag<Emote> getEmotesBag()
    {
        return processMentions(Message.MentionType.EMOTE, new HashBag<>(), false, this::matchEmote);
    }

    @Nonnull
    @Override
    public synchronized List<Member> getMembers()
    {
        if (guild == null)
            return Collections.emptyList();
        if (mentionedMembers != null)
            return mentionedMembers;
        return mentionedMembers = Collections.unmodifiableList(processMentions(Message.MentionType.USER, new ArrayList<>(), true, this::matchMember));
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
    @SuppressWarnings("ConstantConditions")
    public List<IMentionable> getMentions(@Nonnull Message.MentionType... types)
    {
        if (types == null || types.length == 0)
            return getMentions(Message.MentionType.values());
        List<IMentionable> mentions = new ArrayList<>();
        // Conversion to set to prevent duplication of types
        for (Message.MentionType type : EnumSet.of(types[0], types))
        {
            switch (type)
            {
            case CHANNEL:
                mentions.addAll(getChannels());
                break;
            case USER:
                TLongObjectMap<IMentionable> set = new TLongObjectHashMap<>();
                for (User u : getUsers())
                    set.put(u.getIdLong(), u);
                for (Member m : getMembers())
                    set.put(m.getIdLong(), m);
                mentions.addAll(set.valueCollection());
                break;
            case ROLE:
                mentions.addAll(getRoles());
                break;
            case EMOTE:
                mentions.addAll(getEmotes());
                break;
//            case EVERYONE:
//            case HERE:
//            default: continue;
            }
        }

        // Sort mentions by occurrence
        mentions.sort(Comparator.comparingInt(it -> content.indexOf(it.getId())));
        return Collections.unmodifiableList(mentions);
    }

    @Override
    public boolean isMentioned(@Nonnull IMentionable mentionable, @Nonnull Message.MentionType... types)
    {
        Checks.notNull(types, "Mention Types");
        if (types.length == 0)
            return isMentioned(mentionable, Message.MentionType.values());
        for (Message.MentionType type : types)
        {
            switch (type)
            {
            case HERE:
                if (isMass("@here") && mentionable instanceof UserSnowflake)
                    return true;
                break;
            case EVERYONE:
                if (isMass("@everyone") && mentionable instanceof UserSnowflake)
                    return true;
                break;
            case USER:
                if (isUserMentioned(mentionable))
                    return true;
                break;
            case ROLE:
                if (isRoleMentioned(mentionable))
                    return true;
                break;
            case CHANNEL:
                if (mentionable instanceof GuildChannel && getChannels().contains(mentionable))
                    return true;
                break;
            case EMOTE:
                if (mentionable instanceof Emote && getEmotes().contains(mentionable))
                    return true;
                break;
//           default: continue;
            }
        }
        return false;
    }

    // Internal parsing methods

    protected  <T, C extends Collection<T>> C processMentions(Message.MentionType type, C collection, boolean distinct, Function<Matcher, T> map)
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

    protected abstract User matchUser(Matcher matcher);

    protected abstract Member matchMember(Matcher matcher);

    protected abstract GuildChannel matchChannel(Matcher matcher);

    protected abstract Role matchRole(Matcher matcher);

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

    protected abstract boolean isUserMentioned(IMentionable mentionable);

    protected boolean isRoleMentioned(IMentionable mentionable)
    {
        if (mentionable instanceof Role)
            return getRoles().contains(mentionable);
        Member member = null;
        if (mentionable instanceof Member)
            member = (Member) mentionable;
        else if (guild != null && mentionable instanceof User)
            member = guild.getMember((User) mentionable);
        return member != null && CollectionUtils.containsAny(getRoles(), member.getRoles());
    }

    protected boolean isMass(String s)
    {
        return mentionsEveryone && content.contains(s);
    }
}
