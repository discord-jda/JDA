/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.requests.restaction.pagination.ReactionPaginationAction;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.requests.restaction.pagination.ReactionPaginationActionImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.EncodingUtil;
import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.bag.HashBag;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReceivedMessage extends AbstractMessage
{
    private final Object mutex = new Object();

    protected final JDAImpl api;
    protected final long id;
    protected final MessageType type;
    protected final MessageChannel channel;
    protected final boolean fromWebhook;
    protected final boolean mentionsEveryone;
    protected final boolean pinned;
    protected final User author;
    protected final Member member;
    protected final MessageActivity activity;
    protected final OffsetDateTime editedTime;
    protected final List<MessageReaction> reactions;
    protected final List<Attachment> attachments;
    protected final List<MessageEmbed> embeds;
    protected final TLongSet mentionedUsers;
    protected final TLongSet mentionedRoles;

    // LAZY EVALUATED
    protected String altContent = null;
    protected String strippedContent = null;

    protected List<User> userMentions = null;
    protected List<Member> memberMentions = null;
    protected List<Emote> emoteMentions = null;
    protected List<Role> roleMentions = null;
    protected List<TextChannel> channelMentions = null;
    protected List<String> invites = null;

    public ReceivedMessage(
        long id, MessageChannel channel, MessageType type,
        boolean fromWebhook, boolean mentionsEveryone, TLongSet mentionedUsers, TLongSet mentionedRoles, boolean tts, boolean pinned,
        String content, String nonce, User author, Member member, MessageActivity activity, OffsetDateTime editTime,
        List<MessageReaction> reactions, List<Attachment> attachments, List<MessageEmbed> embeds)
    {
        super(content, nonce, tts);
        this.id = id;
        this.channel = channel;
        this.type = type;
        this.api = (channel != null) ? (JDAImpl) channel.getJDA() : null;
        this.fromWebhook = fromWebhook;
        this.mentionsEveryone = mentionsEveryone;
        this.pinned = pinned;
        this.author = author;
        this.member = member;
        this.activity = activity;
        this.editedTime = editTime;
        this.reactions = Collections.unmodifiableList(reactions);
        this.attachments = Collections.unmodifiableList(attachments);
        this.embeds = Collections.unmodifiableList(embeds);
        this.mentionedUsers = mentionedUsers;
        this.mentionedRoles = mentionedRoles;
    }

    @Nonnull
    @Override
    public JDA getJDA()
    {
        return api;
    }

    @Override
    public boolean isPinned()
    {
        return pinned;
    }

    @Nonnull
    @Override
    public RestAction<Void> pin()
    {
        return channel.pinMessageById(getId());
    }

    @Nonnull
    @Override
    public RestAction<Void> unpin()
    {
        return channel.unpinMessageById(getId());
    }

    @Nonnull
    @Override
    public RestAction<Void> addReaction(@Nonnull Emote emote)
    {
        Checks.notNull(emote, "Emote");

        boolean missingReaction = reactions.stream()
                   .map(MessageReaction::getReactionEmote)
                   .filter(MessageReaction.ReactionEmote::isEmote)
                   .noneMatch(r -> r.getIdLong() == emote.getIdLong());

        if (missingReaction)
        {
            Checks.check(emote.canInteract(getJDA().getSelfUser(), channel),
                         "Cannot react with the provided emote because it is not available in the current channel.");
        }
        return channel.addReactionById(getId(), emote);
    }

    @Nonnull
    @Override
    public RestAction<Void> addReaction(@Nonnull String unicode)
    {
        return channel.addReactionById(getId(), unicode);
    }

    @Nonnull
    @Override
    public RestAction<Void> clearReactions()
    {
        if (!isFromType(ChannelType.TEXT))
            throw new IllegalStateException("Cannot clear reactions from a message in a Group or PrivateChannel.");
        return getTextChannel().clearReactionsById(getId());
    }

    @Nonnull
    @Override
    public RestAction<Void> removeReaction(@Nonnull Emote emote)
    {
        return channel.removeReactionById(getId(), emote);
    }

    @Nonnull
    @Override
    public RestAction<Void> removeReaction(@Nonnull Emote emote, @Nonnull User user)
    {
        return getTextChannel().removeReactionById(getIdLong(), emote, user);
    }

    @Nonnull
    @Override
    public RestAction<Void> removeReaction(@Nonnull String unicode)
    {
        return channel.removeReactionById(getId(), unicode);
    }

    @Nonnull
    @Override
    public RestAction<Void> removeReaction(@Nonnull String unicode, @Nonnull User user)
    {
        return getTextChannel().removeReactionById(getId(), unicode, user);
    }


    @Nonnull
    @Override
    public ReactionPaginationAction retrieveReactionUsers(@Nonnull Emote emote)
    {
        Checks.notNull(emote, "Emote");

        MessageReaction reaction = this.reactions.stream()
            .filter(r -> r.getReactionEmote().isEmote() && r.getReactionEmote().getEmote().equals(emote))
            .findFirst().orElse(null);

        if (reaction == null)
            return new ReactionPaginationActionImpl(this, String.format("%s:%s", emote, emote.getId()));
        return new ReactionPaginationActionImpl(reaction);
    }

    @Nonnull
    @Override
    public ReactionPaginationAction retrieveReactionUsers(@Nonnull String unicode)
    {
        Checks.noWhitespace(unicode, "Emoji");

        MessageReaction reaction = this.reactions.stream()
            .filter(r -> r.getReactionEmote().isEmoji() && r.getReactionEmote().getEmoji().equals(unicode))
            .findFirst().orElse(null);

        if (reaction == null)
            return new ReactionPaginationActionImpl(this, EncodingUtil.encodeUTF8(unicode));
        return new ReactionPaginationActionImpl(reaction);
    }

    @Override
    public MessageReaction.ReactionEmote getReactionByUnicode(@Nonnull String unicode)
    {
        Checks.noWhitespace(unicode, "Emoji");

        return this.reactions.stream()
            .map(MessageReaction::getReactionEmote)
            .filter(r -> r.isEmoji() && r.getEmoji().equals(unicode))
            .findFirst().orElse(null);
    }

    @Override
    public MessageReaction.ReactionEmote getReactionById(@Nonnull String id)
    {
        return getReactionById(MiscUtil.parseSnowflake(id));
    }

    @Override
    public MessageReaction.ReactionEmote getReactionById(long id)
    {
        Checks.notNull(id, "Reaction ID");

        return this.reactions.stream()
            .map(MessageReaction::getReactionEmote)
            .filter(r -> r.isEmote() && r.getIdLong() == id)
            .findFirst().orElse(null);
    }

    @Nonnull
    @Override
    public MessageType getType()
    {
        return type;
    }

    @Override
    public long getIdLong()
    {
        return id;
    }

    @Nonnull
    @Override
    public String getJumpUrl()
    {
        return String.format("https://discordapp.com/channels/%s/%s/%s", isFromGuild() ? getGuild().getId() : "@me", getChannel().getId(), getId());
    }

    private User matchUser(Matcher matcher)
    {
        long userId = MiscUtil.parseSnowflake(matcher.group(1));
        if (!mentionedUsers.contains(userId))
            return null;
        User user = getJDA().getUserById(userId);
        if (user == null)
            user = api.getFakeUserMap().get(userId);
        if (user == null && userMentions != null)
            user = userMentions.stream().filter(it -> it.getIdLong() == userId).findFirst().orElse(null);
        return user;
    }

    @Nonnull
    @Override
    public synchronized List<User> getMentionedUsers()
    {
        if (userMentions == null)
            userMentions = Collections.unmodifiableList(processMentions(MentionType.USER, new ArrayList<>(), true, this::matchUser));
        return userMentions;
    }

    @Nonnull
    @Override
    public Bag<User> getMentionedUsersBag()
    {
        return processMentions(MentionType.USER, new HashBag<>(), false, this::matchUser);
    }

    private TextChannel matchTextChannel(Matcher matcher)
    {
        long channelId = MiscUtil.parseSnowflake(matcher.group(1));
        return getJDA().getTextChannelById(channelId);
    }

    @Nonnull
    @Override
    public synchronized List<TextChannel> getMentionedChannels()
    {
        if (channelMentions == null)
            channelMentions = Collections.unmodifiableList(processMentions(MentionType.CHANNEL, new ArrayList<>(), true, this::matchTextChannel));
        return channelMentions;
    }

    @Nonnull
    @Override
    public Bag<TextChannel> getMentionedChannelsBag()
    {
        return processMentions(MentionType.CHANNEL, new HashBag<>(), false, this::matchTextChannel);
    }

    private Role matchRole(Matcher matcher)
    {
        long roleId = MiscUtil.parseSnowflake(matcher.group(1));
        if (!mentionedRoles.contains(roleId))
            return null;
        if (getChannelType().isGuild())
            return getGuild().getRoleById(roleId);
        else
            return getJDA().getRoleById(roleId);
    }

    @Nonnull
    @Override
    public synchronized List<Role> getMentionedRoles()
    {
        if (roleMentions == null)
            roleMentions = Collections.unmodifiableList(processMentions(MentionType.ROLE, new ArrayList<>(), true, this::matchRole));
        return roleMentions;
    }

    @Nonnull
    @Override
    public Bag<Role> getMentionedRolesBag()
    {
        return processMentions(MentionType.ROLE, new HashBag<>(), false, this::matchRole);
    }

    @Nonnull
    @Override
    public List<Member> getMentionedMembers(@Nonnull Guild guild)
    {
        Checks.notNull(guild, "Guild");
        if (isFromGuild() && guild.equals(getGuild()) && memberMentions != null)
            return memberMentions;
        List<User> mentionedUsers = getMentionedUsers();
        List<Member> members = new ArrayList<>();
        for (User user : mentionedUsers)
        {
            Member member = guild.getMember(user);
            if (member != null)
                members.add(member);
        }

        return Collections.unmodifiableList(members);
    }

    @Nonnull
    @Override
    public List<Member> getMentionedMembers()
    {
        if (isFromType(ChannelType.TEXT))
            return getMentionedMembers(getGuild());
        else
            throw new IllegalStateException("You must specify a Guild for Messages which are not sent from a TextChannel!");
    }

    @Nonnull
    @Override
    public List<IMentionable> getMentions(@Nonnull MentionType... types)
    {
        if (types == null || types.length == 0)
            return getMentions(MentionType.values());
        List<IMentionable> mentions = new ArrayList<>();
        // boolean duplicate checks
        // not using Set because channel and role might have the same ID
        boolean channel = false;
        boolean role = false;
        boolean user = false;
        boolean emote = false;
        for (MentionType type : types)
        {
            switch (type)
            {
                case EVERYONE:
                case HERE:
                default: continue;
                case CHANNEL:
                    if (!channel)
                        mentions.addAll(getMentionedChannels());
                    channel = true;
                    break;
                case USER:
                    if (!user)
                        mentions.addAll(getMentionedUsers());
                    user = true;
                    break;
                case ROLE:
                    if (!role)
                        mentions.addAll(getMentionedRoles());
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
    public boolean isMentioned(@Nonnull IMentionable mentionable, @Nonnull MentionType... types)
    {
        Checks.notNull(types, "Mention Types");
        if (types.length == 0)
            return isMentioned(mentionable, MentionType.values());
        final boolean isUserEntity = mentionable instanceof User || mentionable instanceof Member;
        for (MentionType type : types)
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
                        if (getMentionedChannels().contains(mentionable))
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

    private boolean isUserMentioned(IMentionable mentionable)
    {
        if (mentionable instanceof User)
        {
            return getMentionedUsers().contains(mentionable);
        }
        else if (mentionable instanceof Member)
        {
            final Member member = (Member) mentionable;
            return getMentionedUsers().contains(member.getUser());
        }
        return false;
    }

    private boolean isRoleMentioned(IMentionable mentionable)
    {
        if (mentionable instanceof Role)
        {
            return getMentionedRoles().contains(mentionable);
        }
        else if (mentionable instanceof Member)
        {
            final Member member = (Member) mentionable;
            return CollectionUtils.containsAny(getMentionedRoles(), member.getRoles());
        }
        else if (isFromType(ChannelType.TEXT) && mentionable instanceof User)
        {
            final Member member = getGuild().getMember((User) mentionable);
            return member != null && CollectionUtils.containsAny(getMentionedRoles(), member.getRoles());
        }
        return false;
    }

    private boolean isMass(String s)
    {
        return mentionsEveryone && content.contains(s);
    }

    @Override
    public boolean mentionsEveryone()
    {
        return mentionsEveryone;
    }

    @Override
    public boolean isEdited()
    {
        return editedTime != null;
    }

    @Override
    public OffsetDateTime getTimeEdited()
    {
        return editedTime;
    }

    @Nonnull
    @Override
    public User getAuthor()
    {
        return author;
    }

    @Override
    public Member getMember()
    {
        return member;
    }

    @Nonnull
    @Override
    public String getContentStripped()
    {
        if (strippedContent != null)
            return strippedContent;
        synchronized (mutex)
        {
            if (strippedContent != null)
                return strippedContent;
            return strippedContent = MarkdownSanitizer.sanitize(getContentDisplay());
        }
    }

    @Nonnull
    @Override
    public String getContentDisplay()
    {
        if (altContent != null)
            return altContent;
        synchronized (mutex)
        {
            if (altContent != null)
                return altContent;
            String tmp = content;
            for (User user : getMentionedUsers())
            {
                String name;
                if (isFromType(ChannelType.TEXT) && getGuild().isMember(user))
                    name = getGuild().getMember(user).getEffectiveName();
                else
                    name = user.getName();
                tmp = tmp.replaceAll("<@!?" + Pattern.quote(user.getId()) + '>', '@' + Matcher.quoteReplacement(name));
            }
            for (Emote emote : getEmotes())
            {
                tmp = tmp.replace(emote.getAsMention(), ":" + emote.getName() + ":");
            }
            for (TextChannel mentionedChannel : getMentionedChannels())
            {
                tmp = tmp.replace(mentionedChannel.getAsMention(), '#' + mentionedChannel.getName());
            }
            for (Role mentionedRole : getMentionedRoles())
            {
                tmp = tmp.replace(mentionedRole.getAsMention(), '@' + mentionedRole.getName());
            }
            return altContent = tmp;
        }
    }

    @Nonnull
    @Override
    public String getContentRaw()
    {
        return content;
    }

    @Nonnull
    @Override
    public List<String> getInvites()
    {
        if (invites != null)
            return invites;
        synchronized (mutex)
        {
            if (invites != null)
                return invites;
            invites = new ArrayList<>();
            Matcher m = INVITE_PATTERN.matcher(getContentRaw());
            while (m.find())
                invites.add(m.group(1));
            return invites = Collections.unmodifiableList(invites);
        }
    }

    @Override
    public String getNonce()
    {
        return nonce;
    }

    @Override
    public boolean isFromType(@Nonnull ChannelType type)
    {
        return getChannelType() == type;
    }

    @Nonnull
    @Override
    public ChannelType getChannelType()
    {
        return channel.getType();
    }

    @Nonnull
    @Override
    public MessageChannel getChannel()
    {
        return channel;
    }

    @Nonnull
    @Override
    public PrivateChannel getPrivateChannel()
    {
        if (!isFromType(ChannelType.PRIVATE))
            throw new IllegalStateException("This message was not sent in a private channel");
        return (PrivateChannel) channel;
    }

    @Nonnull
    @Override
    public TextChannel getTextChannel()
    {
        if (!isFromType(ChannelType.TEXT))
            throw new IllegalStateException("This message was not sent in a text channel");
        return (TextChannel) channel;
    }

    @Override
    public Category getCategory()
    {
        return isFromType(ChannelType.TEXT) ? getTextChannel().getParent() : null;
    }

    @Nonnull
    @Override
    public Guild getGuild()
    {
        return getTextChannel().getGuild();
    }

    @Nonnull
    @Override
    public List<Attachment> getAttachments()
    {
        return attachments;
    }

    @Nonnull
    @Override
    public List<MessageEmbed> getEmbeds()
    {
        return embeds;
    }

    private Emote matchEmote(Matcher m)
    {
        long emoteId = MiscUtil.parseSnowflake(m.group(2));
        String name = m.group(1);
        boolean animated = m.group(0).startsWith("<a:");
        Emote emote = getJDA().getEmoteById(emoteId);
        if (emote == null)
            emote = new EmoteImpl(emoteId, api).setName(name).setAnimated(animated);
        return emote;
    }

    @Nonnull
    @Override
    public synchronized List<Emote> getEmotes()
    {
        if (this.emoteMentions == null)
            emoteMentions = Collections.unmodifiableList(processMentions(MentionType.EMOTE, new ArrayList<>(), true, this::matchEmote));
        return emoteMentions;
    }

    @Nonnull
    @Override
    public Bag<Emote> getEmotesBag()
    {
        return processMentions(MentionType.EMOTE, new HashBag<>(), false, this::matchEmote);
    }

    @Nonnull
    @Override
    public List<MessageReaction> getReactions()
    {
        return reactions;
    }

    @Override
    public boolean isWebhookMessage()
    {
        return fromWebhook;
    }

    @Override
    public boolean isTTS()
    {
        return isTTS;
    }

    @Nullable
    @Override
    public MessageActivity getActivity()
    {
        return activity;
    }

    @Nonnull
    @Override
    public MessageAction editMessage(@Nonnull CharSequence newContent)
    {
        return editMessage(new MessageBuilder().append(newContent).build());
    }

    @Nonnull
    @Override
    public MessageAction editMessage(@Nonnull MessageEmbed newContent)
    {
        return editMessage(new MessageBuilder().setEmbed(newContent).build());
    }

    @Nonnull
    @Override
    public MessageAction editMessageFormat(@Nonnull String format, @Nonnull Object... args)
    {
        Checks.notBlank(format, "Format String");
        return editMessage(new MessageBuilder().appendFormat(format, args).build());
    }

    @Nonnull
    @Override
    public MessageAction editMessage(@Nonnull Message newContent)
    {
        if (!getJDA().getSelfUser().equals(getAuthor()))
            throw new IllegalStateException("Attempted to update message that was not sent by this account. You cannot modify other User's messages!");

        return getChannel().editMessageById(getIdLong(), newContent);
    }

    @Nonnull
    @Override
    public AuditableRestAction<Void> delete()
    {
        if (!getJDA().getSelfUser().equals(getAuthor()))
        {
            if (isFromType(ChannelType.PRIVATE))
                throw new IllegalStateException("Cannot delete another User's messages in a PrivateChannel.");
            else if (!getGuild().getSelfMember()
                    .hasPermission((TextChannel) getChannel(), Permission.MESSAGE_MANAGE))
                throw new InsufficientPermissionException(getTextChannel(), Permission.MESSAGE_MANAGE);
        }
        return channel.deleteMessageById(getIdLong());
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == this)
            return true;
        if (!(o instanceof ReceivedMessage))
            return false;
        ReceivedMessage oMsg = (ReceivedMessage) o;
        return this.id == oMsg.id;
    }

    @Override
    public int hashCode()
    {
        return Long.hashCode(id);
    }

    @Override
    public String toString()
    {
        return author != null
            ? String.format("M:%#s:%.20s(%s)", author, this, getId())
            : String.format("M:%.20s", this); // this message was made using MessageBuilder
    }

    @Override
    protected void unsupported()
    {
        throw new UnsupportedOperationException("This operation is not supported on received messages!");
    }

    @Override
    public void formatTo(Formatter formatter, int flags, int width, int precision)
    {
        boolean upper = (flags & FormattableFlags.UPPERCASE) == FormattableFlags.UPPERCASE;
        boolean leftJustified = (flags & FormattableFlags.LEFT_JUSTIFY) == FormattableFlags.LEFT_JUSTIFY;
        boolean alt = (flags & FormattableFlags.ALTERNATE) == FormattableFlags.ALTERNATE;

        String out = alt ? getContentRaw() : getContentDisplay();

        if (upper)
            out = out.toUpperCase(formatter.locale());

        appendFormat(formatter, width, precision, leftJustified, out);
    }

    public void setMentions(List<User> users, List<Member> members)
    {
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

    private <T, C extends Collection<T>> C processMentions(MentionType type, C collection, boolean distinct, Function<Matcher, T> map)
    {
        Matcher matcher = type.getPattern().matcher(getContentRaw());
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

    private static class FormatToken
    {
        public final String format;
        public final int start;

        public FormatToken(String format, int start)
        {
            this.format = format;
            this.start = start;
        }
    }
}
