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
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.commands.ICommandReference;
import net.dv8tion.jda.api.interactions.commands.SlashCommandReference;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;
import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.bag.HashBag;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public abstract class AbstractMentions implements Mentions
{
    protected final String content;
    protected final JDAImpl jda;
    @Nullable protected final Guild guild;
    protected final boolean mentionsEveryone;

    protected List<User> mentionedUsers;
    protected List<Member> mentionedMembers;
    protected List<Role> mentionedRoles;
    protected List<GuildChannel> mentionedChannels;
    protected List<CustomEmoji> mentionedEmojis;
    protected List<SlashCommandReference> mentionedSlashCommands;

    public AbstractMentions(String content, JDAImpl jda, @Nullable Guild guild, boolean mentionsEveryone)
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
        return mentionedUsers = processMentions(Message.MentionType.USER, true, this::matchUser, Helpers.toUnmodifiableList());
    }

    @Nonnull
    @Override
    public Bag<User> getUsersBag()
    {
        Bag<User> bag = processMentions(Message.MentionType.USER, false, this::matchUser, toBag());

        // Handle reply mentions
        for (User user : getUsers())
        {
            if (!bag.contains(user))
                bag.add(user, 1);
        }

        return bag;
    }

    @Nonnull
    @Override
    public synchronized List<GuildChannel> getChannels()
    {
        if (mentionedChannels != null)
            return mentionedChannels;
        return mentionedChannels = processMentions(Message.MentionType.CHANNEL, true, this::matchChannel, Helpers.toUnmodifiableList());
    }

    @Nonnull
    @Override
    public Bag<GuildChannel> getChannelsBag()
    {
        return processMentions(Message.MentionType.CHANNEL, false, this::matchChannel, toBag());
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

        return processMentions(Message.MentionType.CHANNEL, false, matchTypedChannel, toBag());
    }

    @Nonnull
    @Override
    public synchronized List<Role> getRoles()
    {
        if (guild == null)
            return Collections.emptyList();
        if (mentionedRoles != null)
            return mentionedRoles;
        return mentionedRoles = processMentions(Message.MentionType.ROLE, true, this::matchRole, Helpers.toUnmodifiableList());
    }

    @Nonnull
    @Override
    public Bag<Role> getRolesBag()
    {
        if (guild == null)
            return new HashBag<>();
        return processMentions(Message.MentionType.ROLE, false, this::matchRole, toBag());
    }

    @Nonnull
    @Override
    public synchronized List<CustomEmoji> getCustomEmojis()
    {
        if (mentionedEmojis != null)
            return mentionedEmojis;
        return mentionedEmojis = processMentions(Message.MentionType.EMOJI, true, this::matchEmoji, Helpers.toUnmodifiableList());
    }

    @Nonnull
    @Override
    public Bag<CustomEmoji> getCustomEmojisBag()
    {
        return processMentions(Message.MentionType.EMOJI, false, this::matchEmoji, toBag());
    }

    @Nonnull
    @Override
    public synchronized List<Member> getMembers()
    {
        if (guild == null)
            return Collections.emptyList();
        if (mentionedMembers != null)
            return mentionedMembers;
        return mentionedMembers = processMentions(Message.MentionType.USER, true, this::matchMember, Helpers.toUnmodifiableList());
    }

    @Nonnull
    @Override
    public Bag<Member> getMembersBag()
    {
        if (guild == null)
            return new HashBag<>();
        Bag<Member> bag = processMentions(Message.MentionType.USER, false, this::matchMember, toBag());

        // Handle reply mentions
        for (Member member : getMembers())
        {
            if (!bag.contains(member))
                bag.add(member, 1);
        }

        return bag;
    }

    @Nonnull
    @Override
    public synchronized List<SlashCommandReference> getSlashCommands()
    {
        if (mentionedSlashCommands != null)
            return mentionedSlashCommands;
        return mentionedSlashCommands = processMentions(Message.MentionType.SLASH_COMMAND, true, this::matchSlashCommand, Helpers.toUnmodifiableList());
    }

    @Nonnull
    @Override
    public Bag<SlashCommandReference> getSlashCommandsBag()
    {
        return processMentions(Message.MentionType.SLASH_COMMAND, false, this::matchSlashCommand, toBag());
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
            case EMOJI:
                mentions.addAll(getCustomEmojis());
                break;
            case SLASH_COMMAND:
                mentions.addAll(getSlashCommands());
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
            case EMOJI:
                if (mentionable instanceof CustomEmoji && getCustomEmojis().contains(mentionable))
                    return true;
                break;
            case SLASH_COMMAND:
                if (isSlashCommandMentioned(mentionable))
                    return true;
                break;
//           default: continue;
            }
        }
        return false;
    }

    // Internal parsing methods

    protected  <T, A, C extends Collection<T>> C processMentions(Message.MentionType type, boolean distinct, Function<Matcher, ? extends T> mapping, Collector<? super T, A, C> collector)
    {
        A accumulator = collector.supplier().get();
        Matcher matcher = type.getPattern().matcher(content);
        Set<T> unique = distinct ? new HashSet<>() : null;
        while (matcher.find())
        {
            try
            {
                T elem = mapping.apply(matcher);
                if (elem != null && (unique == null || unique.add(elem)))
                    collector.accumulator().accept(accumulator, elem);
            }
            catch (NumberFormatException ignored) {}
        }
        return collector.finisher().apply(accumulator);
    }

    protected static <T> Collector<T, ?, HashBag<T>> toBag()
    {
        return Collectors.toCollection(HashBag::new);
    }

    protected abstract User matchUser(Matcher matcher);

    protected abstract Member matchMember(Matcher matcher);

    protected abstract GuildChannel matchChannel(Matcher matcher);

    protected abstract Role matchRole(Matcher matcher);

    protected CustomEmoji matchEmoji(Matcher m)
    {
        long emojiId = MiscUtil.parseSnowflake(m.group(2));
        String name = m.group(1);
        boolean animated = m.group(0).startsWith("<a:");
        CustomEmoji emoji = getJDA().getEmojiById(emojiId);
        if (emoji == null)
            emoji = Emoji.fromCustom(name, emojiId, animated);
        return emoji;
    }

    protected SlashCommandReference matchSlashCommand(Matcher matcher)
    {
        return new SlashCommandReference(matcher.group(1), matcher.group(2), matcher.group(3), Long.parseLong(matcher.group(4)));
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

    protected boolean isSlashCommandMentioned(IMentionable mentionable)
    {
        if (mentionable instanceof ICommandReference)
        {
            final ICommandReference reference = (ICommandReference) mentionable;
            for (SlashCommandReference r : getSlashCommands())
                if (r.getFullCommandName().equals(reference.getFullCommandName()) && r.getIdLong() == reference.getIdLong())
                    return true;
        }
        return false;
    }

    protected boolean isMass(String s)
    {
        return mentionsEveryone && content.contains(s);
    }
}
