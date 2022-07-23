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
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.internal.utils.AllowedMentionsImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;
import net.dv8tion.jda.internal.utils.IOUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MessageCreateBuilder extends AbstractMessageBuilder<MessageCreateData, MessageCreateBuilder> implements MessageCreateRequest<MessageCreateBuilder>
{
    private final List<FileUpload> files = new ArrayList<>(10);
    private boolean tts;

    public MessageCreateBuilder() {}

    @Nonnull
    public static MessageCreateBuilder from(@Nonnull MessageCreateData data)
    {
        return new MessageCreateBuilder().applyData(data);
    }

    @Nonnull
    public static MessageCreateBuilder fromEdit(@Nonnull MessageEditData data)
    {
        return new MessageCreateBuilder().applyEditData(data);
    }

    @Nonnull
    public static MessageCreateBuilder fromMessage(@Nonnull Message message)
    {
        return new MessageCreateBuilder().applyMessage(message);
    }
    
    @Nonnull
    @Override
    public MessageCreateBuilder addContent(@Nonnull String content)
    {
        Checks.notNull(content, "Content");
        Checks.check(
            Helpers.codePointLength(this.content) + Helpers.codePointLength(content) <= Message.MAX_CONTENT_LENGTH,
            "Cannot have content longer than %d characters", Message.MAX_CONTENT_LENGTH
        );
        this.content.append(content);
        return this;
    }

    @Nonnull
    @Override
    public MessageCreateBuilder addEmbeds(@Nonnull Collection<? extends MessageEmbed> embeds)
    {
        Checks.noneNull(embeds, "Embeds");
        Checks.check(
            this.embeds.size() + embeds.size() <= Message.MAX_EMBED_COUNT,
            "Cannot add more than %d embeds", Message.MAX_EMBED_COUNT
        );
        this.embeds.addAll(embeds);
        return this;
    }

    @Nonnull
    @Override
    public MessageCreateBuilder addComponents(@Nonnull Collection<? extends LayoutComponent> layouts)
    {
        Checks.noneNull(layouts, "ComponentLayouts");
        for (LayoutComponent layout : layouts)
            Checks.check(layout.isMessageCompatible(), "Provided component layout is invalid for messages!");
        Checks.check(
            this.components.size() + layouts.size() <= Message.MAX_COMPONENT_COUNT,
            "Cannot add more than %d component layouts", Message.MAX_COMPONENT_COUNT
        );
        this.components.addAll(layouts);
        return this;
    }

    @Nonnull
    @Override
    public MessageCreateBuilder setFiles(@Nullable Collection<? extends FileUpload> files)
    {
        if (files != null)
            Checks.noneNull(files, "Files");
        this.files.clear();
        if (files != null)
            this.files.addAll(files);
        return this;
    }

    @Nonnull
    @Override
    public List<? extends FileUpload> getAttachments()
    {
        return Collections.unmodifiableList(files);
    }

    @Nonnull
    @Override
    public MessageCreateBuilder addFiles(@Nonnull Collection<? extends FileUpload> files)
    {
        Checks.noneNull(files, "Files");
        this.files.addAll(files);
        return this;
    }

    @Nonnull
    @Override
    public MessageCreateBuilder setTTS(boolean tts)
    {
        this.tts = tts;
        return this;
    }

    @Override
    public boolean isEmpty()
    {
        return Helpers.isBlank(content) && embeds.isEmpty() && files.isEmpty();
    }

    @Override
    public boolean isValid()
    {
        return !isEmpty() && embeds.size() <= Message.MAX_EMBED_COUNT
                          && components.size() <= Message.MAX_COMPONENT_COUNT
                          && Helpers.codePointLength(content) <= Message.MAX_CONTENT_LENGTH;
    }

    @Nonnull
    public MessageCreateData build()
    {
        // Copy to prevent modifying data after building
        String content = this.content.toString().trim();
        List<MessageEmbed> embeds = new ArrayList<>(this.embeds);
        List<FileUpload> files = new ArrayList<>(this.files);
        List<LayoutComponent> components = new ArrayList<>(this.components);
        AllowedMentionsImpl allowedMentions = this.allowedMentions.copy();

        if (content.isEmpty() && embeds.isEmpty() && files.isEmpty())
            throw new IllegalStateException("Cannot build an empty message. You need at least one of content, embeds, or files");

        int length = Helpers.codePointLength(content);
        if (length > Message.MAX_CONTENT_LENGTH)
            throw new IllegalStateException("Message content is too long! Max length is " + Message.MAX_CONTENT_LENGTH + " characters, provided " + length);

        if (embeds.size() > Message.MAX_EMBED_COUNT)
            throw new IllegalStateException("Cannot build message with over " + Message.MAX_EMBED_COUNT + " embeds, provided " + embeds.size());

        if (components.size() > Message.MAX_COMPONENT_COUNT)
            throw new IllegalStateException("Cannot build message with over " + Message.MAX_COMPONENT_COUNT + " component layouts, provided " + components.size());
        return new MessageCreateData(content, embeds, files, components, allowedMentions, tts);
    }

    @Nonnull
    public MessageCreateBuilder clear()
    {
        super.clear();
        this.files.clear();
        this.tts = false;
        return this;
    }

    @Nonnull
    @Override
    public MessageCreateBuilder closeFiles()
    {
        files.forEach(IOUtil::silentClose);
        files.clear();
        return this;
    }
}
