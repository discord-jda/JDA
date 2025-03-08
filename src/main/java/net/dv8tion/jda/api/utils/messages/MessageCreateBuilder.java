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
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.internal.components.utils.ComponentsUtil;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;
import net.dv8tion.jda.internal.utils.IOUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Builder specialized for building a {@link MessageCreateData}.
 * <br>This can be used to build a request and send it to various API endpoints.
 *
 * <p><b>Example</b>
 * <pre>{@code
 * try (FileUpload file = FileUpload.fromData(new File("wave.gif"))) {
 *     MessageCreateData data = new MessageCreateBuilder()
 *       .setContent("Hello guys!")
 *       .setTTS(true)
 *       .setFiles(file)
 *       .build();
 *
 *     for (MessageChannel channel : channels) {
 *         channel.sendMessage(data).queue();
 *     }
 * } // closes wave.gif if an error occurred
 * }</pre>
 *
 * @see MessageChannel#sendMessage(MessageCreateData)
 * @see net.dv8tion.jda.api.interactions.callbacks.IReplyCallback#reply(MessageCreateData) IReplyCallback.reply(data)
 * @see net.dv8tion.jda.api.interactions.InteractionHook#sendMessage(MessageCreateData) InteractionHook.sendMessage(data)
 * @see MessageEditBuilder
 */
public class MessageCreateBuilder extends AbstractMessageBuilder<MessageCreateData, MessageCreateBuilder> implements MessageCreateRequest<MessageCreateBuilder>
{
    private final List<FileUpload> files = new ArrayList<>(10);
    private MessagePollData poll;
    private boolean tts;

    public MessageCreateBuilder() {}

    /**
     * Factory method to start a builder from an existing instance of {@link MessageCreateData}.
     * <br>Equivalent to {@code new MessageCreateBuilder().applyData(data)}.
     *
     * @param  data
     *         The message create data to apply
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return A new MessageCreateBuilder instance with the applied data
     *
     * @see    #applyData(MessageCreateData)
     */
    @Nonnull
    public static MessageCreateBuilder from(@Nonnull MessageCreateData data)
    {
        return new MessageCreateBuilder().applyData(data);
    }

    /**
     * Factory method to start a builder from an existing instance of {@link MessageEditData}.
     * <br>Equivalent to {@code new MessageCreateBuilder().applyEditData(data)}.
     * <br>This will only set fields which were explicitly set on the {@link MessageEditBuilder},
     * unless it was configured to be {@link MessageEditRequest#setReplace(boolean) replacing}.
     *
     * <p>This will <b>not</b> copy the message's attachments, only any configured {@link FileUpload FileUploads}.
     * To copy attachments, you must download them explicitly instead.
     *
     * @param  data
     *         The message edit data to apply
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return A new MessageCreateBuilder instance with the applied data
     *
     * @see    #applyEditData(MessageEditData)
     */
    @Nonnull
    public static MessageCreateBuilder fromEditData(@Nonnull MessageEditData data)
    {
        return new MessageCreateBuilder().applyEditData(data);
    }

    /**
     * Factory method to start a builder from an existing instance of {@link Message}.
     * <br>Equivalent to {@code new MessageCreateBuilder().applyMessage(data)}.
     * <br>The {@link MessageData} are not updated to reflect the provided message, and might mention users that the message did not.
     *
     * <p>This cannot copy the file attachments of the message, they must be manually downloaded and provided to {@link #setFiles(FileUpload...)}.
     *
     * @param  message
     *         The message data to apply
     *
     * @throws IllegalArgumentException
     *         If the message is null or a system message
     *
     * @return A new MessageCreateBuilder instance with the applied data
     *
     * @see    #applyMessage(Message)
     */
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
    public MessageCreateBuilder addComponents(@Nonnull Collection<? extends MessageTopLevelComponent> components)
    {
        Checks.noneNull(components, "MessageTopLevelComponents");
        Checks.checkComponents(
                "Provided component is invalid for messages!",
                components,
                Component::isMessageCompatible
        );
        List<MessageTopLevelComponentUnion> componentsAsUnions = ComponentsUtil.membersToUnion(
                components,
                MessageTopLevelComponentUnion.class
        );

        this.components.addAll(componentsAsUnions);
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
        {
            this.files.addAll(files);
            this.setVoiceMessageIfApplicable(files);
        }
        return this;
    }

    @Nonnull
    @Override
    public List<FileUpload> getAttachments()
    {
        return Collections.unmodifiableList(files);
    }

    @Nullable
    @Override
    public MessagePollData getPoll()
    {
        return poll;
    }

    @Nonnull
    @Override
    public MessageCreateBuilder setPoll(@Nullable MessagePollData poll)
    {
        this.poll = poll;
        return this;
    }

    @Nonnull
    @Override
    public MessageCreateBuilder addFiles(@Nonnull Collection<? extends FileUpload> files)
    {
        Checks.noneNull(files, "Files");
        this.files.addAll(files);
        this.setVoiceMessageIfApplicable(files);
        return this;
    }

    @Nonnull
    @Override
    public MessageCreateBuilder setTTS(boolean tts)
    {
        this.tts = tts;
        return this;
    }

    @Nonnull
    @Override
    public MessageCreateBuilder setSuppressedNotifications(boolean suppressed)
    {
        if (suppressed)
            messageFlags |= Message.MessageFlag.NOTIFICATIONS_SUPPRESSED.getValue();
        else
            messageFlags &= ~Message.MessageFlag.NOTIFICATIONS_SUPPRESSED.getValue();
        return this;
    }

    @Nonnull
    @Override
    public MessageCreateBuilder setVoiceMessage(boolean voiceMessage)
    {
        if (voiceMessage)
            messageFlags |= Message.MessageFlag.IS_VOICE_MESSAGE.getValue();
        else
            messageFlags &= ~Message.MessageFlag.IS_VOICE_MESSAGE.getValue();
        return this;
    }

    @Override
    public boolean isEmpty()
    {
        return Helpers.isBlank(content) && embeds.isEmpty() && files.isEmpty() && components.isEmpty() && poll == null;
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
        return !isEmpty()
                && embeds.size() <= Message.MAX_EMBED_COUNT
                && components.size() <= Message.MAX_COMPONENT_COUNT
                && !ComponentsUtil.hasIllegalV1Components(components)
                && Helpers.codePointLength(content) <= Message.MAX_CONTENT_LENGTH;
    }

    private boolean isV2Valid()
    {
        return Helpers.isBlank(content)
                && embeds.isEmpty()
                && poll == null
                && !components.isEmpty()
                && components.size() <= Message.MAX_COMPONENT_COUNT_COMPONENTS_V2
                && ComponentsUtil.getComponentTreeSize(components) <= Message.MAX_COMPONENT_COUNT_IN_COMPONENT_TREE
                && ComponentsUtil.getComponentTreeLength(components) <= Message.MAX_CONTENT_LENGTH_COMPONENT_V2;
    }

    @Nonnull
    public MessageCreateData build()
    {
        if (isUsingComponentsV2())
            return buildV2();
        else
            return buildV1();
    }

    @Nonnull
    private MessageCreateData buildV1()
    {
        // Copy to prevent modifying data after building
        String content = this.content.toString().trim();
        List<MessageEmbed> embeds = new ArrayList<>(this.embeds);
        List<FileUpload> files = new ArrayList<>(this.files);
        List<MessageTopLevelComponentUnion> components = new ArrayList<>(this.components);
        AllowedMentionsData mentions = this.mentions.copy();

        if (content.isEmpty() && embeds.isEmpty() && files.isEmpty() && components.isEmpty() && poll == null)
            throw new IllegalStateException("Cannot build an empty message. You need at least one of content, embeds, components, poll, or files");

        int length = Helpers.codePointLength(content);
        if (length > Message.MAX_CONTENT_LENGTH)
            throw new IllegalStateException("Message content is too long! Max length is " + Message.MAX_CONTENT_LENGTH + " characters, provided " + length);

        if (embeds.size() > Message.MAX_EMBED_COUNT)
            throw new IllegalStateException("Cannot build message with over " + Message.MAX_EMBED_COUNT + " embeds, provided " + embeds.size());

        if (components.size() > Message.MAX_COMPONENT_COUNT)
            throw new IllegalStateException("Cannot build message with over " + Message.MAX_COMPONENT_COUNT + " top-level components, provided " + components.size());
        final List<? extends Component> illegalV1Components = ComponentsUtil.getIllegalV1Components(components);
        if (!illegalV1Components.isEmpty())
            throw new IllegalStateException("Cannot build message with components other than ActionRow while using components V1, see #useComponentsV2, provided: " + illegalV1Components);

        return new MessageCreateData(content, embeds, files, components, mentions, poll, tts, messageFlags);
    }

    @Nonnull
    private MessageCreateData buildV2()
    {
        // Copy to prevent modifying data after building
        List<FileUpload> files = new ArrayList<>(this.files);
        List<MessageTopLevelComponentUnion> components = new ArrayList<>(this.components);
        AllowedMentionsData mentions = this.mentions.copy();

        if (content.length() > 0 || !embeds.isEmpty() || poll != null)
            throw new IllegalStateException("Cannot build a message with components V2 enabled while having content, embeds, or poll");

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

        return new MessageCreateData("", Collections.emptyList(), files, components, mentions, poll, tts, messageFlags);
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

    private void setVoiceMessageIfApplicable(@NotNull Collection<? extends FileUpload> files)
    {
        if (files.stream().anyMatch(FileUpload::isVoiceMessage))
            this.setVoiceMessage(true);
    }
}
