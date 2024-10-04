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
import net.dv8tion.jda.api.entities.Message;
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
import java.util.List;
import java.util.regex.Matcher;

import static net.dv8tion.jda.api.entities.Message.INVITE_PATTERN;

public class MessageSnapshot
{
    private final Object mutex = new Object();

    private final MessageType type;
    private final Mentions mentions;
    private final OffsetDateTime editTime;
    private final String content;
    private final List<Message.Attachment> attachments;
    private final List<MessageEmbed> embeds;
    private final List<LayoutComponent> components;
    private final List<StickerItem> stickers;
    private final long flags;

    private List<String> invites;

    public MessageSnapshot(
        MessageType type, Mentions mentions, OffsetDateTime editTime, String content,
        List<Message.Attachment> attachments,
        List<MessageEmbed> embeds, List<LayoutComponent> components,
        List<StickerItem> stickers, long flags
    ) {
        this.type = type;
        this.mentions = mentions;
        this.editTime = editTime;
        this.content = content;
        this.attachments = attachments;
        this.embeds = embeds;
        this.components = components;
        this.stickers = stickers;
        this.flags = flags;
    }

    @Nonnull
    public MessageType getType()
    {
        return type;
    }

    @Nonnull
    public Mentions getMentions()
    {
        return mentions;
    }

    public boolean isEdited()
    {
        return editTime != null;
    }

    @Nullable
    public OffsetDateTime getTimeEdited()
    {
        return editTime;
    }

    @Nonnull
    public String getContentRaw()
    {
        return content;
    }

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

    @Nonnull
    @Unmodifiable
    public List<Message.Attachment> getAttachments()
    {
        return attachments;
    }

    @Nonnull
    @Unmodifiable
    public List<MessageEmbed> getEmbeds()
    {
        return embeds;
    }

    @Nonnull
    @Unmodifiable
    public List<LayoutComponent> getComponents()
    {
        return components;
    }

    @Nonnull
    @Unmodifiable
    public List<StickerItem> getStickers()
    {
        return stickers;
    }

    public long getFlagsRaw()
    {
        return flags;
    }
}
