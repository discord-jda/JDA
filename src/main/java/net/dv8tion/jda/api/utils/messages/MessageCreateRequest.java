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

package net.dv8tion.jda.api.utils.messages;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.utils.FileUpload;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;

public interface MessageCreateRequest<R extends MessageCreateRequest<R>> extends MessageRequest<R>
{
    @Nonnull
    R addContent(@Nonnull String content);

    @Nonnull
    R addEmbeds(@Nonnull Collection<? extends MessageEmbed> embeds);

    @Nonnull
    default R addEmbeds(@Nonnull MessageEmbed... embeds)
    {
        return setEmbeds(Arrays.asList(embeds));
    }

    @Nonnull
    R addComponents(@Nonnull Collection<? extends LayoutComponent> layouts);

    @Nonnull
    default R addComponents(@Nonnull LayoutComponent... components)
    {
        return setComponents(Arrays.asList(components));
    }

    @Nonnull
    default R addActionRow(@Nonnull Collection<? extends ItemComponent> components)
    {
        return setComponents(ActionRow.of(components));
    }

    @Nonnull
    default R addActionRow(@Nonnull ItemComponent... components)
    {
        return setComponents(ActionRow.of(components));
    }

    @Nonnull
    R setFiles(@Nullable Collection<? extends FileUpload> files);

    @Nonnull
    R addFiles(@Nonnull Collection<? extends FileUpload> files);

    @Nonnull
    default R addFiles(@Nonnull FileUpload... files)
    {
        return addFiles(Arrays.asList(files));
    }

    @Nonnull
    R setTTS(boolean tts);

    @Nonnull
    default R applyData(@Nonnull MessageCreateData data)
    {
        return setContent(data.getContent())
                .setEmbeds(data.getEmbeds())
                .setTTS(data.isTTS())
                .setComponents(data.getComponents())
                .setFiles(data.getFiles());
    }

    // Explicitly explain that mentions are not filtered by allowed mentions this way

    @Nonnull
    default R applyMessage(@Nonnull Message message)
    {
        return setContent(message.getContentRaw())
                .setEmbeds(message.getEmbeds())
                .setTTS(message.isTTS())
                .setComponents(message.getActionRows());
    }

    @Nonnull
    @SuppressWarnings({"unchecked", "ResultOfMethodCallIgnored"})
    default R applyEditData(@Nonnull MessageEditData data)
    {
        int flags = data.getFlags();
        if ((flags & MessageEditBuilder.CONTENT) != 0)
            setContent(data.getContent());
        if ((flags & MessageEditBuilder.EMBEDS) != 0)
            setEmbeds(data.getEmbeds());
        if ((flags & MessageEditBuilder.COMPONENTS) != 0)
            setComponents(data.getComponents());
        if ((flags & MessageEditBuilder.ATTACHMENTS) != 0)
            setFiles(data.getFiles());
        if ((flags & MessageEditBuilder.MENTIONS) != 0)
        {
            String[] array = new String[0];
            allowedMentions(data.getAllowedMentions());
            mentionUsers(data.getMentionedUsers().toArray(array));
            mentionRoles(data.getMentionedRoles().toArray(array));
            mentionRepliedUser(data.isMentionRepliedUser());
        }

        return (R) this;
    }
}
