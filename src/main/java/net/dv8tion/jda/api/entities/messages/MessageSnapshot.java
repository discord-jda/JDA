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

package net.dv8tion.jda.api.entities.messages;

import net.dv8tion.jda.api.entities.Mentions;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.Message.MessageFlag;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.entities.sticker.StickerItem;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import org.jetbrains.annotations.Unmodifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Matcher;

import static net.dv8tion.jda.api.entities.Message.INVITE_PATTERN;

/**
 * Snapshot of a forwarded message.
 */
public class MessageSnapshot
{
    private final Object mutex = new Object();

    private final MessageType type;
    private final Mentions mentions;
    private final OffsetDateTime editTime;
    private final String content;
    private final List<Attachment> attachments;
    private final List<MessageEmbed> embeds;
    private final List<LayoutComponent> components;
    private final List<StickerItem> stickers;
    private final long flags;

    private List<String> invites;

    public MessageSnapshot(
        MessageType type, Mentions mentions, OffsetDateTime editTime, String content,
        List<Attachment> attachments,
        List<MessageEmbed> embeds, List<LayoutComponent> components,
        List<StickerItem> stickers, long flags
    ) {
        this.type = type;
        this.mentions = mentions;
        this.editTime = editTime;
        this.content = content;
        this.attachments = Collections.unmodifiableList(attachments);
        this.embeds =  Collections.unmodifiableList(embeds);
        this.components =  Collections.unmodifiableList(components);
        this.stickers = Collections.unmodifiableList(stickers);
        this.flags = flags;
    }

    /**
     * The {@link MessageType} of the forwarded message.
     *
     * @return The {@link MessageType}
     */
    @Nonnull
    public MessageType getType()
    {
        return type;
    }

    /**
     * The mentions of the forwarded message.
     *
     * <p>Mentions are only used for resolving users and roles in a forwarded message.
     * Mentions for cross-guild forwarded messages are usually not resolved.
     *
     * @return {@link Mentions}
     */
    @Nonnull
    public Mentions getMentions()
    {
        return mentions;
    }

    /**
     * Whether the forwarded message was edited.
     *
     * <p>Since this is a snapshot, the edited timestamp is only relevant to the time it was forwarded.
     * If the message is edited after the fact, this is not updated.
     *
     * @return True, if the message was edited when it was forwarded
     */
    public boolean isEdited()
    {
        return editTime != null;
    }

    /**
     * The last time the forwarded message was edited before being forwarded.
     *
     * <p>Since this is a snapshot, the edited timestamp is only relevant to the time it was forwarded.
     * If the message is edited after the fact, this is not updated.
     *
     * @return {@link OffsetDateTime} when the message was edited (up to the time it was forwarded)
     */
    @Nullable
    public OffsetDateTime getTimeEdited()
    {
        return editTime;
    }

    /**
     * The raw content of the message, including markdown and mentions.
     *
     * @return The raw message content.
     */
    @Nonnull
    public String getContentRaw()
    {
        return content;
    }

    /**
     * Invite codes found in the message content.
     *
     * @return The invite codes
     */
    @Nonnull
    @Unmodifiable
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

    /**
     * Message attachments of the forwarded message.
     *
     * @return Immutable {@link List} of {@link Attachment}
     */
    @Nonnull
    @Unmodifiable
    public List<Attachment> getAttachments()
    {
        return attachments;
    }

    /**
     * Message embeds of the forwarded message.
     *
     * @return Immutable {@link List} of {@link MessageEmbed}
     */
    @Nonnull
    @Unmodifiable
    public List<MessageEmbed> getEmbeds()
    {
        return embeds;
    }

    /**
     * Components of the forwarded message.
     *
     * <p>Buttons and other interactive components are non-functional in forwarded messages.
     *
     * @return Immutable {@link List} of {@link LayoutComponent}
     */
    @Nonnull
    @Unmodifiable
    public List<LayoutComponent> getComponents()
    {
        return components;
    }

    /**
     * Stickers of the forwarded message.
     *
     * @return Immutable {@link List} of {@link StickerItem}
     */
    @Nonnull
    @Unmodifiable
    public List<StickerItem> getStickers()
    {
        return stickers;
    }

    /**
     * The raw message flags of the forwarded message.
     *
     * @return The message flags
     */
    public long getFlagsRaw()
    {
        return flags;
    }

    /**
     * The message flags fo the forwarded message.
     *
     * @return {@link EnumSet} of {@link MessageFlag}
     */
    @Nonnull
    public EnumSet<MessageFlag> getFlags()
    {
        return MessageFlag.fromBitField((int) getFlagsRaw());
    }
}
