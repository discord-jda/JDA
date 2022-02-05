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

import gnu.trove.set.TLongSet;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.EmoteImpl;
import net.dv8tion.jda.internal.utils.Checks;
import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.bag.HashBag;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;

//TODO-v5 | Docs
public class MessageMentionsImpl implements MessageMentions
{
    private Message message;

    private final boolean mentionsEveryone;
    private final TLongSet mentionedUsers;
    private final TLongSet mentionedRoles;

    private List<User> userMentions = null;
    private List<Member> memberMentions = null;
    private List<Emote> emoteMentions = null;
    private List<Role> roleMentions = null;
    private List<TextChannel> channelMentions = null;

    public MessageMentionsImpl(boolean mentionsEveryone, TLongSet mentionedUsers, TLongSet mentionedRoles)
    {
        this.mentionsEveryone = mentionsEveryone;
        this.mentionedUsers = mentionedUsers;
        this.mentionedRoles = mentionedRoles;
    }

    //TODO-v5 | Docs
    public JDA getJDA()
    {
        return message.getJDA();
    }

    //TODO-v5 | Docs
    public Message getMessage()
    {
        return message;
    }

    @Override
    public boolean mentionsEveryone()
    {
        return mentionsEveryone;
    }

    @Override
    public synchronized List<User> getUsers()
    {
        if (userMentions == null)
            userMentions = Collections.unmodifiableList(processMentions(Message.MentionType.USER, new ArrayList<>(), true, this::matchUser));
        return userMentions;
    }

    @Override
    @Nonnull
    public Bag<User> getUsersBag()
    {
        return processMentions(Message.MentionType.USER, new HashBag<>(), false, this::matchUser);
    }

    @Override
    @Nonnull
    public synchronized List<TextChannel> getChannels()
    {
        //TODO-MessageMentions: This needs to be updated as you can match.. quie a few more chanels than just TextChannels
        if (channelMentions == null)
            channelMentions = Collections.unmodifiableList(processMentions(Message.MentionType.CHANNEL, new ArrayList<>(), true, this::matchTextChannel));
        return channelMentions;
    }

    @Override
    @Nonnull
    public Bag<TextChannel> getChannelsBag()
    {
        return processMentions(Message.MentionType.CHANNEL, new HashBag<>(), false, this::matchTextChannel);
    }

    @Override
    @Nonnull
    public synchronized List<Role> getRoles()
    {
        if (roleMentions == null)
            roleMentions = Collections.unmodifiableList(processMentions(Message.MentionType.ROLE, new ArrayList<>(), true, this::matchRole));
        return roleMentions;
    }

    @Override
    @Nonnull
    public Bag<Role> getRolesBag()
    {
        return processMentions(Message.MentionType.ROLE, new HashBag<>(), false, this::matchRole);
    }

    @Override
    @Nonnull
    public List<Member> getMembers(@Nonnull Guild guild)
    {
        Checks.notNull(guild, "Guild");
        if (message.isFromGuild() && guild.equals(message.getGuild()) && memberMentions != null)
            return memberMentions;
        List<User> mentionedUsers = getUsers();
        List<Member> members = new ArrayList<>();
        for (User user : mentionedUsers)
        {
            Member member = guild.getMember(user);
            if (member != null)
                members.add(member);
        }

        return Collections.unmodifiableList(members);
    }

    @Override
    @Nonnull
    public List<Member> getMembers()
    {
        if (message.isFromGuild())
            return getMembers(message.getGuild());
        else
            throw new IllegalStateException("You must specify a Guild for Messages which are not sent from a TextChannel!");
    }

    @Override
    @Nonnull
    public synchronized List<Emote> getEmotes()
    {
        if (this.emoteMentions == null)
            emoteMentions = Collections.unmodifiableList(processMentions(Message.MentionType.EMOTE, new ArrayList<>(), true, this::matchEmote));
        return emoteMentions;
    }

    @Override
    @Nonnull
    public Bag<Emote> getEmotesBag()
    {
        return processMentions(Message.MentionType.EMOTE, new HashBag<>(), false, this::matchEmote);
    }

    @Override
    @Nonnull
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
                    mentions.addAll(getUsers());
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

    // ======== Setters ============

    public void setMessage(Message message)
    {
        this.message = message;
    }

    public void setUserMemberMentions(List<User> users, List<Member> members)
    {
        String content = message.getContentRaw();
        users.sort(Comparator.comparing((user) ->
                Math.max(content.indexOf("<@" + user.getId() + ">"),
                        content.indexOf("<@!" + user.getId() + ">")
                )));
        members.sort(Comparator.comparing((user) ->
                Math.max(content.indexOf("<@" + user.getId() + ">"),
                        content.indexOf("<@!" + user.getId() + ">")
                )));

        this.userMentions = Collections.unmodifiableList(users);
        this.memberMentions = Collections.unmodifiableList(members);
    }

    // ============= Internal Helpers =================

    private <T, C extends Collection<T>> C processMentions(Message.MentionType type, C collection, boolean distinct, Function<Matcher, T> map)
    {
        Matcher matcher = type.getPattern().matcher(message.getContentRaw());
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
        if (!mentionedUsers.contains(userId))
            return null;
        User user = getJDA().getUserById(userId);
        if (user == null && userMentions != null)
            user = userMentions.stream().filter(it -> it.getIdLong() == userId).findFirst().orElse(null);
        return user;
    }

    private TextChannel matchTextChannel(Matcher matcher)
    {
        long channelId = MiscUtil.parseSnowflake(matcher.group(1));
        return getJDA().getTextChannelById(channelId);
    }

    private Role matchRole(Matcher matcher)
    {
        long roleId = MiscUtil.parseSnowflake(matcher.group(1));
        if (!mentionedRoles.contains(roleId))
            return null;
        if (message.getChannelType().isGuild())
            return message.getGuild().getRoleById(roleId);
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
            emote = new EmoteImpl(emoteId, (JDAImpl) message.getJDA()).setName(name).setAnimated(animated);
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
        else if (message.isFromGuild() && mentionable instanceof User)
        {
            final Member member = message.getGuild().getMember((User) mentionable);
            return member != null && CollectionUtils.containsAny(getRoles(), member.getRoles());
        }
        return false;
    }

    private boolean isMass(String s)
    {
        return mentionsEveryone && message.getContentRaw().contains(s);
    }
}
