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
import gnu.trove.set.hash.TLongHashSet;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.utils.Checks;
import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.bag.HashBag;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.*;
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
    protected List<Emote> emoteMentions = null;
    protected List<Role> roleMentions = null;
    protected List<TextChannel> channelMentions = null;
    protected List<String> invites = null;

    public ReceivedMessage(
        long id, MessageChannel channel, MessageType type,
        boolean fromWebhook, boolean mentionsEveryone, TLongSet mentionedUsers, TLongSet mentionedRoles, boolean tts, boolean pinned,
        String content, String nonce, User author, MessageActivity activity, OffsetDateTime editTime,
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
        this.activity = activity;
        this.editedTime = editTime;
        this.reactions = Collections.unmodifiableList(reactions);
        this.attachments = Collections.unmodifiableList(attachments);
        this.embeds = Collections.unmodifiableList(embeds);
        this.mentionedUsers = mentionedUsers;
        this.mentionedRoles = mentionedRoles;
    }

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

    @Override
    public RestAction<Void> pin()
    {
        return channel.pinMessageById(getId());
    }

    @Override
    public RestAction<Void> unpin()
    {
        return channel.unpinMessageById(getId());
    }

    @Override
    public RestAction<Void> addReaction(Emote emote)
    {
        Checks.notNull(emote, "Emote");

        MessageReaction reaction = reactions.stream()
                .filter(r -> Objects.equals(r.getReactionEmote().getId(), emote.getId()))
                .findFirst().orElse(null);

        if (reaction == null)
        {
            Checks.check(emote.canInteract(getJDA().getSelfUser(), channel),
                         "Cannot react with the provided emote because it is not available in the current channel.");
        }
        return channel.addReactionById(getId(), emote);
    }

    @Override
    public RestAction<Void> addReaction(String unicode)
    {
        return channel.addReactionById(getId(), unicode);
    }

    @Override
    public RestAction<Void> clearReactions()
    {
        if (!isFromType(ChannelType.TEXT))
            throw new IllegalStateException("Cannot clear reactions from a message in a Group or PrivateChannel.");
        return getTextChannel().clearReactionsById(getId());
    }

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

    @Override
    public String getJumpUrl()
    {
        return String.format("https://discordapp.com/channels/%s/%s/%s", getGuild() == null ? "@me" : getGuild().getId(), getChannel().getId(), getId());
    }

    @Override
    public synchronized List<User> getMentionedUsers()
    {
        if (userMentions == null)
        {
            userMentions = new ArrayList<>();
            Matcher matcher = MentionType.USER.getPattern().matcher(content);
            while (matcher.find())
            {
                try
                {
                    long id = MiscUtil.parseSnowflake(matcher.group(1));
                    if (!mentionedUsers.contains(id))
                        continue;
                    User user = getJDA().getUserById(id);
                    if (user == null)
                        user = api.getFakeUserMap().get(id);
                    if (user != null && !userMentions.contains(user))
                        userMentions.add(user);
                } catch (NumberFormatException ignored) {}
            }
            userMentions = Collections.unmodifiableList(userMentions);
        }

        return userMentions;
    }

    @Nonnull
    @Override
    public Bag<User> getMentionedUsersBag()
    {
        Bag<User> bag = new HashBag<>();
        Matcher matcher = MentionType.USER.getPattern().matcher(content);
        while (matcher.find())
        {
            try
            {
                long id = MiscUtil.parseSnowflake(matcher.group(1));
                if (!mentionedUsers.contains(id))
                    continue;
                User user = getJDA().getUserById(id);
                if (user == null)
                    user = api.getFakeUserMap().get(id);
                if (user != null)
                    bag.add(user);
            }
            catch (NumberFormatException ignored) {}
        }
        return bag;
    }

    @Override
    public synchronized List<TextChannel> getMentionedChannels()
    {
        if (channelMentions == null)
        {
            channelMentions = new ArrayList<>();
            Matcher matcher = MentionType.CHANNEL.getPattern().matcher(content);
            while (matcher.find())
            {
                try
                {
                    String id = matcher.group(1);
                    TextChannel channel = getJDA().getTextChannelById(id);
                    if (channel != null && !channelMentions.contains(channel))
                        channelMentions.add(channel);
                }
                catch (NumberFormatException ignored) {}
            }
            channelMentions = Collections.unmodifiableList(channelMentions);
        }

        return channelMentions;
    }

    @Nonnull
    @Override
    public Bag<TextChannel> getMentionedChannelsBag()
    {
        Bag<TextChannel> bag = new HashBag<>();
        Matcher matcher = MentionType.CHANNEL.getPattern().matcher(content);
        while (matcher.find())
        {
            try
            {
                long id = MiscUtil.parseSnowflake(matcher.group(1));
                TextChannel channel = getJDA().getTextChannelById(id);
                if (channel != null)
                    bag.add(channel);
            }
            catch (NumberFormatException ignored) {}
        }
        return bag;
    }

    @Override
    public synchronized List<Role> getMentionedRoles()
    {
        if (roleMentions == null)
        {
            roleMentions = new ArrayList<>();
            Matcher matcher = MentionType.ROLE.getPattern().matcher(content);
            while (matcher.find())
            {
                try
                {
                    long id = MiscUtil.parseSnowflake(matcher.group(1));
                    if (!mentionedRoles.contains(id))
                        continue;
                    Role role = null;
                    if (isFromType(ChannelType.TEXT)) // role lookup is faster if its in the same guild (no global map)
                        role = getGuild().getRoleById(id);
                    if (role == null)
                        role = getJDA().getRoleById(id);
                    if (role != null && !roleMentions.contains(role))
                        roleMentions.add(role);
                }
                catch (NumberFormatException ignored) {}
            }
            roleMentions = Collections.unmodifiableList(roleMentions);
        }

        return roleMentions;
    }

    @Nonnull
    @Override
    public Bag<Role> getMentionedRolesBag()
    {
        Bag<Role> bag = new HashBag<>();
        if (!getChannelType().isGuild())
            return bag;
        Matcher matcher = MentionType.ROLE.getPattern().matcher(content);
        while (matcher.find())
        {
            try
            {
                long id = MiscUtil.parseSnowflake(matcher.group(1));
                if (!mentionedRoles.contains(id))
                    continue;
                Role role = getGuild().getRoleById(id);
                if (role != null)
                    bag.add(role);
            }
            catch (NumberFormatException ignored) {}
        }
        return bag;
    }

    @Override
    public List<Member> getMentionedMembers(Guild guild)
    {
        Checks.notNull(guild, "Guild");
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

    @Override
    public List<Member> getMentionedMembers()
    {
        if (isFromType(ChannelType.TEXT))
            return getMentionedMembers(getGuild());
        else
            throw new IllegalStateException("You must specify a Guild for Messages which are not sent from a TextChannel!");
    }

    @Override
    public List<IMentionable> getMentions(MentionType... types)
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
    public boolean isMentioned(IMentionable mentionable, MentionType... types)
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

    @Override
    public User getAuthor()
    {
        return author;
    }

    @Override
    public Member getMember()
    {
        return isFromType(ChannelType.TEXT) ? getGuild().getMember(getAuthor()) : null;
    }

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

    @Override
    public String getContentRaw()
    {
        return content;
    }

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
    public boolean isFromType(ChannelType type)
    {
        return getChannelType() == type;
    }

    @Override
    public ChannelType getChannelType()
    {
        return channel.getType();
    }

    @Override
    public MessageChannel getChannel()
    {
        return channel;
    }

    @Override
    public PrivateChannel getPrivateChannel()
    {
        return isFromType(ChannelType.PRIVATE) ? (PrivateChannel) channel : null;
    }

    @Override
    public TextChannel getTextChannel()
    {
        return isFromType(ChannelType.TEXT) ? (TextChannel) channel : null;
    }

    @Override
    public Category getCategory()
    {
        return isFromType(ChannelType.TEXT) ? getTextChannel().getParent() : null;
    }

    @Override
    public Guild getGuild()
    {
        return isFromType(ChannelType.TEXT) ? getTextChannel().getGuild() : null;
    }

    @Override
    public List<Attachment> getAttachments()
    {
        return attachments;
    }

    @Override
    public List<MessageEmbed> getEmbeds()
    {
        return embeds;
    }

    @Override
    public synchronized List<Emote> getEmotes()
    {
        if (this.emoteMentions == null)
        {
            TLongSet foundIds = new TLongHashSet();
            emoteMentions = new ArrayList<>();
            Matcher matcher = MentionType.EMOTE.getPattern().matcher(getContentRaw());
            while (matcher.find())
            {
                try
                {
                    final long emoteId = MiscUtil.parseSnowflake(matcher.group(2));
                    // ensure distinct
                    if (foundIds.contains(emoteId))
                        continue;
                    else
                        foundIds.add(emoteId);
                    final String emoteName = matcher.group(1);
                    // Check animated by verifying whether or not it starts with <a: or <:
                    final boolean animated = matcher.group(0).startsWith("<a:");

                    Emote emote = getJDA().getEmoteById(emoteId);
                    if (emote == null)
                        emote = new EmoteImpl(emoteId, api).setAnimated(animated).setName(emoteName);
                    emoteMentions.add(emote);
                }
                catch (NumberFormatException ignored) {}
            }
            emoteMentions = Collections.unmodifiableList(emoteMentions);
        }
        return emoteMentions;
    }

    @Nonnull
    @Override
    public Bag<Emote> getEmotesBag()
    {
        Bag<Emote> bag = new HashBag<>();
        Matcher matcher = MentionType.EMOTE.getPattern().matcher(getContentRaw());
        while (matcher.find())
        {
            try
            {
                long emoteId = MiscUtil.parseSnowflake(matcher.group(2));
                String name = matcher.group(1);
                boolean animated = matcher.group(0).startsWith("<a:");
                Emote emote = getJDA().getEmoteById(emoteId);
                if (emote == null)
                    emote = new EmoteImpl(emoteId, api).setAnimated(animated).setName(name);
                bag.add(emote);
            }
            catch (NumberFormatException ignored) {}
        }
        return bag;
    }

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

    @Override
    public MessageAction editMessage(CharSequence newContent)
    {
        return editMessage(new MessageBuilder().append(newContent).build());
    }

    @Override
    public MessageAction editMessage(MessageEmbed newContent)
    {
        return editMessage(new MessageBuilder().setEmbed(newContent).build());
    }

    @Override
    public MessageAction editMessageFormat(String format, Object... args)
    {
        Checks.notBlank(format, "Format String");
        return editMessage(new MessageBuilder().appendFormat(format, args).build());
    }

    @Override
    public MessageAction editMessage(Message newContent)
    {
        if (!getJDA().getSelfUser().equals(getAuthor()))
            throw new IllegalStateException("Attempted to update message that was not sent by this account. You cannot modify other User's messages!");

        return getChannel().editMessageById(getIdLong(), newContent);
    }

    @Override
    public AuditableRestAction<Void> delete()
    {
        if (!getJDA().getSelfUser().equals(getAuthor()))
        {
            if (isFromType(ChannelType.PRIVATE))
                throw new IllegalStateException("Cannot delete another User's messages in a PrivateChannel.");
            else if (!getGuild().getSelfMember()
                    .hasPermission((TextChannel) getChannel(), Permission.MESSAGE_MANAGE))
                throw new InsufficientPermissionException(Permission.MESSAGE_MANAGE);
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
