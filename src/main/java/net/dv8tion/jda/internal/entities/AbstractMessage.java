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

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ComponentLayout;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.requests.restaction.pagination.ReactionPaginationAction;
import net.dv8tion.jda.internal.utils.Helpers;
import org.apache.commons.collections4.Bag;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.OffsetDateTime;
import java.util.*;

public abstract class AbstractMessage implements Message
{
    protected static final String UNSUPPORTED = "This operation is not supported for Messages of this type!";

    protected final String content;
    protected final String nonce;
    protected final boolean isTTS;

    public AbstractMessage(String content, String nonce, boolean isTTS)
    {
        this.content = content;
        this.nonce = nonce;
        this.isTTS = isTTS;
    }

    @Nonnull
    @Override
    public String getContentRaw()
    {
        return content;
    }

    @Override
    public String getNonce()
    {
        return nonce;
    }

    @Override
    public boolean isTTS()
    {
        return isTTS;
    }

    protected abstract void unsupported();

    @Override
    public void formatTo(Formatter formatter, int flags, int width, int precision)
    {
        boolean upper = (flags & FormattableFlags.UPPERCASE) == FormattableFlags.UPPERCASE;
        boolean leftJustified = (flags & FormattableFlags.LEFT_JUSTIFY) == FormattableFlags.LEFT_JUSTIFY;

        String out = content;

        if (upper)
            out = out.toUpperCase(formatter.locale());

        appendFormat(formatter, width, precision, leftJustified, out);
    }

    protected void appendFormat(Formatter formatter, int width, int precision, boolean leftJustified, String out)
    {
        try
        {
            Appendable appendable = formatter.out();
            if (precision > -1 && out.length() > precision)
            {
                appendable.append(Helpers.truncate(out, precision - 3)).append("...");
                return;
            }

            if (leftJustified)
                appendable.append(Helpers.rightPad(out, width));
            else
                appendable.append(Helpers.leftPad(out, width));
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }

    @Nullable
    @Override
    public MessageReference getMessageReference()
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public Bag<User> getMentionedUsersBag()
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public Bag<TextChannel> getMentionedChannelsBag()
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public Bag<Role> getMentionedRolesBag()
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public List<User> getMentionedUsers()
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public List<TextChannel> getMentionedChannels()
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public List<Role> getMentionedRoles()
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public List<Member> getMentionedMembers(@Nonnull Guild guild)
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public List<Member> getMentionedMembers()
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public List<IMentionable> getMentions(@Nonnull MentionType... types)
    {
        unsupported();
        return null;
    }

    @Override
    public boolean isMentioned(@Nonnull IMentionable mentionable, @Nonnull MentionType... types)
    {
        unsupported();
        return false;
    }

    @Override
    public boolean mentionsEveryone()
    {
        unsupported();
        return false;
    }

    @Override
    public boolean isEdited()
    {
        unsupported();
        return false;
    }

    @Override
    public OffsetDateTime getTimeEdited()
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public User getAuthor()
    {
        unsupported();
        return null;
    }

    @Override
    public Member getMember()
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public String getJumpUrl()
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public String getContentDisplay()
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public String getContentStripped()
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public List<String> getInvites()
    {
        unsupported();
        return null;
    }

    @Override
    public boolean isFromType(@Nonnull ChannelType type)
    {
        unsupported();
        return false;
    }

    @Nonnull
    @Override
    public ChannelType getChannelType()
    {
        unsupported();
        return null;
    }

    @Override
    public boolean isWebhookMessage()
    {
        unsupported();
        return false;
    }

    @Nonnull
    @Override
    public MessageChannel getChannel()
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public GuildMessageChannel getGuildChannel()
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public PrivateChannel getPrivateChannel()
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public TextChannel getTextChannel()
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public NewsChannel getNewsChannel()
    {
        unsupported();
        return null;
    }

    @Override
    public Category getCategory()
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public Guild getGuild()
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public List<Attachment> getAttachments()
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public List<MessageEmbed> getEmbeds()
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public List<ActionRow> getActionRows()
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public List<Emote> getEmotes()
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public Bag<Emote> getEmotesBag()
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public List<MessageReaction> getReactions()
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public List<MessageSticker> getStickers()
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public MessageAction editMessage(@Nonnull CharSequence newContent)
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public MessageAction editMessageEmbeds(@Nonnull Collection<? extends MessageEmbed> newContent)
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public MessageAction editMessageComponents(@Nonnull Collection<? extends ComponentLayout> components)
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public MessageAction editMessageFormat(@Nonnull String format, @Nonnull Object... args)
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public MessageAction editMessage(@Nonnull Message newContent)
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public AuditableRestAction<Void> delete()
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public JDA getJDA()
    {
        unsupported();
        return null;
    }

    @Override
    public boolean isPinned()
    {
        unsupported();
        return false;
    }

    @Nonnull
    @Override
    public RestAction<Void> pin()
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public RestAction<Void> unpin()
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public RestAction<Void> addReaction(@Nonnull Emote emote)
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public RestAction<Void> addReaction(@Nonnull String unicode)
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public RestAction<Void> clearReactions()
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public RestAction<Void> clearReactions(@Nonnull String unicode)
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public RestAction<Void> clearReactions(@Nonnull Emote emote)
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public RestAction<Void> removeReaction(@Nonnull Emote emote)
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public RestAction<Void> removeReaction(@Nonnull Emote emote, @Nonnull User user)
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public RestAction<Void> removeReaction(@Nonnull String unicode)
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public RestAction<Void> removeReaction(@Nonnull String unicode, @Nonnull User user)
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public ReactionPaginationAction retrieveReactionUsers(@Nonnull Emote emote)
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public ReactionPaginationAction retrieveReactionUsers(@Nonnull String unicode)
    {
        unsupported();
        return null;
    }

    @Override
    public MessageReaction.ReactionEmote getReactionByUnicode(@Nonnull String unicode)
    {
        unsupported();
        return null;
    }

    @Override
    public MessageReaction.ReactionEmote getReactionById(@Nonnull String id)
    {
        unsupported();
        return null;
    }

    @Override
    public MessageReaction.ReactionEmote getReactionById(long id)
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public AuditableRestAction<Void> suppressEmbeds(boolean suppressed)
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public RestAction<Message> crosspost()
    {
        unsupported();
        return null;
    }

    @Override
    public boolean isSuppressedEmbeds()
    {
        unsupported();
        return false;
    }

    @Nonnull
    @Override
    public EnumSet<MessageFlag> getFlags()
    {
        unsupported();
        return null;
    }

    @Override
    public long getFlagsRaw()
    {
        unsupported();
        return 0;
    }

    @Override
    public boolean isEphemeral()
    {
        unsupported();
        return false;
    }

    @Nonnull
    @Override
    public MessageType getType()
    {
        unsupported();
        return null;
    }

    @Nullable
    @Override
    public Message.Interaction getInteraction()
    {
        unsupported();
        return null;
    }

    @Override
    public RestAction<ThreadChannel> createThreadChannel(String name)
    {
        unsupported();
        return null;
    }
}
