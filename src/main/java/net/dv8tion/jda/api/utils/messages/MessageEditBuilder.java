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

import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.interactions.components.MessageTopLevelComponentUnion;
import net.dv8tion.jda.api.utils.AttachedFile;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;
import net.dv8tion.jda.internal.utils.IOUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Builder specialized for building a {@link MessageEditData}.
 *
 * <p>These are used to edit messages and allow configuration that either {@link #setReplace(boolean) replaces} the message or only updates specific fields.
 *
 * @see MessageCreateBuilder
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class MessageEditBuilder extends AbstractMessageBuilder<MessageEditData, MessageEditBuilder> implements MessageEditRequest<MessageEditBuilder>
{
    protected static final int CONTENT       = 1;
    protected static final int EMBEDS        = 1 << 1;
    protected static final int COMPONENTS    = 1 << 2;
    protected static final int ATTACHMENTS   = 1 << 3;
    protected static final int MENTIONS      = 1 << 4;
    protected static final int FLAGS         = 1 << 5;

    private boolean replace = false;
    private int configuredFields = 0;

    private final List<AttachedFile> attachments = new ArrayList<>(10);

    public MessageEditBuilder() {}

    /**
     * Factory method to start a builder from an existing instance of {@link MessageEditData}.
     * <br>Equivalent to {@code new MessageEditBuilder().applyData(data)}.
     * 
     * @param  data
     *         The message edit data to apply
     *
     * @throws IllegalArgumentException
     *         If null is provided
     * 
     * @return A new MessageEditBuilder instance with the applied data
     * 
     * @see    #applyData(MessageEditData)
     */
    @Nonnull
    public static MessageEditBuilder from(@Nonnull MessageEditData data)
    {
        return new MessageEditBuilder().applyData(data);
    }

    /**
     * Factory method to start a builder from an existing instance of {@link MessageCreateData}.
     * <br>Equivalent to {@code new MessageEditBuilder().applyCreateData(data)}.
     *
     * <p>This will set the request to be {@link #setReplace(boolean) replacing}.
     *
     * @param  data
     *         The message create data to apply
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return A new MessageEditBuilder instance with the applied data
     * 
     * @see    #applyCreateData(MessageCreateData) 
     */
    @Nonnull
    public static MessageEditBuilder fromCreateData(@Nonnull MessageCreateData data)
    {
        return new MessageEditBuilder().applyCreateData(data);
    }

    /**
     * Factory method to start a builder from an existing instance of {@link Message}.
     * <br>Equivalent to {@code new MessageEditBuilder().applyMessage(data)}.
     *
     * <p>This will set the request to be {@link #setReplace(boolean) replacing}.
     *
     * @param  message
     *         The message to apply
     *
     * @throws IllegalArgumentException
     *         If null is provided or the message is a system message
     *
     * @return A new MessageEditBuilder instance with the applied data
     *
     * @see    #applyMessage(Message)
     */
    @Nonnull
    public static MessageEditBuilder fromMessage(@Nonnull Message message)
    {
        return new MessageEditBuilder().applyMessage(message);
    }

    @Nonnull
    @Override
    public MessageEditBuilder mentionRepliedUser(boolean mention)
    {
        super.mentionRepliedUser(mention);
        configuredFields |= MENTIONS;
        return this;
    }

    @Nonnull
    @Override
    public MessageEditBuilder setAllowedMentions(@Nullable Collection<Message.MentionType> allowedMentions)
    {
        super.setAllowedMentions(allowedMentions);
        configuredFields |= MENTIONS;
        return this;
    }

    @Nonnull
    @Override
    public MessageEditBuilder mention(@Nonnull Collection<? extends IMentionable> mentions)
    {
        super.mention(mentions);
        configuredFields |= MENTIONS;
        return this;
    }

    @Nonnull
    @Override
    public MessageEditBuilder mentionUsers(@Nonnull Collection<String> userIds)
    {
        super.mentionUsers(userIds);
        configuredFields |= MENTIONS;
        return this;
    }

    @Nonnull
    @Override
    public MessageEditBuilder mentionRoles(@Nonnull Collection<String> roleIds)
    {
        super.mentionRoles(roleIds);
        configuredFields |= MENTIONS;
        return this;
    }

    @Nonnull
    @Override
    public MessageEditBuilder setAttachments(@Nullable Collection<? extends AttachedFile> attachments)
    {
        this.attachments.clear();
        configuredFields |= ATTACHMENTS;
        if (attachments != null)
            this.attachments.addAll(attachments);
        return this;
    }

    @Nonnull
    @Override
    public List<? extends AttachedFile> getAttachments()
    {
        return Collections.unmodifiableList(attachments);
    }

    @Nonnull
    @Override
    public MessageEditBuilder setReplace(boolean isReplace)
    {
        this.replace = isReplace;
        return this;
    }

    @Override
    public boolean isReplace()
    {
        return replace;
    }

    @Nonnull
    @Override
    public MessageEditBuilder setContent(@Nullable String content)
    {
        super.setContent(content);
        configuredFields |= CONTENT;
        return this;
    }

    @Nonnull
    @Override
    public MessageEditBuilder setEmbeds(@Nonnull Collection<? extends MessageEmbed> embeds)
    {
        super.setEmbeds(embeds);
        configuredFields |= EMBEDS;
        return this;
    }

    @Nonnull
    @Override
    public MessageEditBuilder useComponentsV2(boolean useComponentsV2)
    {
        super.useComponentsV2(useComponentsV2);
        configuredFields |= FLAGS;
        return this;
    }

    @Nonnull
    @Override
    public MessageEditBuilder setComponents(@Nonnull Collection<? extends MessageTopLevelComponent> components)
    {
        super.setComponents(components);
        configuredFields |= COMPONENTS;
        return this;
    }

    @Nonnull
    @Override
    public MessageEditBuilder setSuppressEmbeds(boolean suppress)
    {
        super.setSuppressEmbeds(suppress);
        configuredFields |= FLAGS;
        return this;
    }

    @Nonnull
    @Override
    public MessageEditBuilder applyData(@Nonnull MessageEditData data)
    {
        Checks.notNull(data, "Data");
        this.configuredFields |= data.getConfiguredFields();
        this.replace |= data.isReplace();

        if (data.isSet(CONTENT))
            this.setContent(data.getContent());
        if (data.isSet(EMBEDS))
            this.setEmbeds(data.getEmbeds());
        if (data.isSet(COMPONENTS))
        {
            final List<MessageTopLevelComponentUnion> components = data.getComponents().stream()
//                  // TODO-components-v2 - restore
//                   .map(LayoutComponent::createCopy)
                    .collect(Collectors.toList());
            this.setComponents(components);
        }
        if (data.isSet(ATTACHMENTS))
            this.setAttachments(data.getAttachments());
        if (data.isSet(MENTIONS))
            this.mentions = data.mentions.copy();
        if (data.isSet(FLAGS))
            this.messageFlags = data.getFlags();

        return this;
    }

    @Override
    public boolean isEmpty()
    {
        return !replace && configuredFields == 0;
    }

    @Override
    public boolean isValid()
    {
        if (isSet(EMBEDS) && embeds.size() > Message.MAX_EMBED_COUNT)
            return false;
        if (isSet(COMPONENTS) && components.size() > Message.MAX_COMPONENT_COUNT)
            return false;
        return !isSet(CONTENT) || Helpers.codePointLength(content) <= Message.MAX_CONTENT_LENGTH;
    }

    private boolean isSet(int flag)
    {
        return replace || (configuredFields & flag) != 0;
    }

    @Nonnull
    @Override
    public MessageEditData build()
    {
        // Copy to prevent modifying data after building
        String content = this.content.toString().trim();
        List<MessageEmbed> embeds = new ArrayList<>(this.embeds);
        List<AttachedFile> attachments = new ArrayList<>(this.attachments);
        List<MessageTopLevelComponentUnion> components = new ArrayList<>(this.components);
        AllowedMentionsData mentions = this.mentions.copy();

        int length = isSet(CONTENT) ? Helpers.codePointLength(content) : 0;
        if (length > Message.MAX_CONTENT_LENGTH)
            throw new IllegalStateException("Message content is too long! Max length is " + Message.MAX_CONTENT_LENGTH + " characters, provided " + length);

        if (isSet(EMBEDS) && embeds.size() > Message.MAX_EMBED_COUNT)
            throw new IllegalStateException("Cannot build message with over " + Message.MAX_EMBED_COUNT + " embeds, provided " + embeds.size());

        if (isSet(COMPONENTS) && components.size() > Message.MAX_COMPONENT_COUNT)
            throw new IllegalStateException("Cannot build message with over " + Message.MAX_COMPONENT_COUNT + " component layouts, provided " + components.size());

        // TODO-components-v2 - Implement mutual exclusion of content/embeds, and components v2
        return new MessageEditData(configuredFields, messageFlags, replace, content, embeds, attachments, components, mentions);
    }

    @Nonnull
    @Override
    public MessageEditBuilder clear()
    {
        this.configuredFields = 0;
        this.attachments.clear();
        return super.clear();
    }

    @Nonnull
    @Override
    public MessageEditBuilder closeFiles()
    {
        attachments.forEach(IOUtil::silentClose);
        attachments.removeIf(FileUpload.class::isInstance);
        return this;
    }
}
