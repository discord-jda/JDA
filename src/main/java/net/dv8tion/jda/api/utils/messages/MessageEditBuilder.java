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

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.components.MessageTopLevelComponentUnion;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.AttachedFile;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.internal.components.utils.ComponentsUtil;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;
import net.dv8tion.jda.internal.utils.IOUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
    public MessageEditBuilder setComponents(@Nonnull Collection<? extends MessageTopLevelComponent> components)
    {
        super.setComponents(components);
        configuredFields |= COMPONENTS;
        return this;
    }

    @Nonnull
    @Override
    public MessageEditBuilder useComponentsV2(boolean use) {
        super.useComponentsV2(use);
        configuredFields |= FLAGS;
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
            this.setComponents(data.getComponents());
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
        if (isUsingComponentsV2())
            return isV2Valid();
        else
            return isV1Valid();
    }

    private boolean isV1Valid()
    {
        if (isSet(CONTENT) && Helpers.codePointLength(content) > Message.MAX_CONTENT_LENGTH)
            return false;
        if (isSet(EMBEDS) && embeds.size() > Message.MAX_EMBED_COUNT)
            return false;
        if (isSet(COMPONENTS) && (components.size() > Message.MAX_COMPONENT_COUNT || ComponentsUtil.hasIllegalV1Components(components)))
            return false;

        return true;
    }

    private boolean isV2Valid()
    {
        if (isSet(EMBEDS) && !embeds.isEmpty())
            return false;
        if (isSet(COMPONENTS))
        {
            if (components.size() > Message.MAX_COMPONENT_COUNT_COMPONENTS_V2)
                return false;
            if (ComponentsUtil.getComponentTreeSize(components) > Message.MAX_COMPONENT_COUNT_IN_COMPONENT_TREE)
                return false;
            if (ComponentsUtil.getComponentTreeLength(components) > Message.MAX_CONTENT_LENGTH_COMPONENT_V2)
                return false;
        }
        if (isSet(CONTENT) && !Helpers.isBlank(content))
            return false;

        return true;
    }

    private boolean isSet(int flag)
    {
        return replace || (configuredFields & flag) != 0;
    }

    @Nonnull
    @Override
    public MessageEditData build()
    {
        if (isUsingComponentsV2())
            return buildV2();
        else
            return buildV1();
    }

    @Nonnull
    private MessageEditData buildV1()
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

        if (isSet(COMPONENTS))
        {
            if (components.size() > Message.MAX_COMPONENT_COUNT)
                throw new IllegalStateException("Cannot build message with over " + Message.MAX_COMPONENT_COUNT + " top-level components, provided " + components.size());
            final List<? extends Component> illegalComponents = ComponentsUtil.getIllegalV1Components(components);
            if (!illegalComponents.isEmpty())
                throw new IllegalStateException("Cannot build message with components other than ActionRow while using components V1, see #useComponentsV2, provided: " + illegalComponents);
        }

        return new MessageEditData(configuredFields, messageFlags, replace, content, embeds, attachments, components, mentions);
    }

    @Nonnull
    private MessageEditData buildV2()
    {
        // Copy to prevent modifying data after building
        List<AttachedFile> attachments = new ArrayList<>(this.attachments);
        List<MessageTopLevelComponentUnion> components = new ArrayList<>(this.components);
        AllowedMentionsData mentions = this.mentions.copy();

        if ((isSet(CONTENT) && content.length() > 0) || (isSet(EMBEDS) && !embeds.isEmpty()))
            throw new IllegalStateException("Cannot build a message with components V2 enabled while having content or embeds");

        if (isSet(COMPONENTS))
        {
            if (components.isEmpty())
                throw new IllegalStateException("Cannot build message with no V2 components, or did you forget to disable them?");
            if (components.size() > Message.MAX_COMPONENT_COUNT_COMPONENTS_V2)
                throw new IllegalStateException("Cannot build message with over " + Message.MAX_COMPONENT_COUNT_COMPONENTS_V2 + " top-level components, provided " + components.size());
            final long componentTreeSize = ComponentsUtil.getComponentTreeSize(components);
            if (componentTreeSize > Message.MAX_COMPONENT_COUNT_IN_COMPONENT_TREE)
                throw new IllegalStateException("Cannot build message with over " + Message.MAX_COMPONENT_COUNT_IN_COMPONENT_TREE + " total components, provided " + componentTreeSize);
            final long componentTreeLength = ComponentsUtil.getComponentTreeLength(components);
            if (componentTreeLength > Message.MAX_CONTENT_LENGTH_COMPONENT_V2)
                throw new IllegalStateException("Cannot build message with over " + Message.MAX_CONTENT_LENGTH_COMPONENT_V2 + " total characters, provided " + componentTreeLength);
        }

        return new MessageEditData(configuredFields, messageFlags, replace, "", Collections.emptyList(), attachments, components, mentions);
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
