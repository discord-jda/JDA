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

import net.dv8tion.jda.api.components.MessageTopLevelComponentUnion;
import net.dv8tion.jda.api.components.utils.ComponentIterator;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.internal.entities.FileContainerMixin;
import net.dv8tion.jda.internal.utils.IOUtil;
import net.dv8tion.jda.internal.utils.message.MessageUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Output of a {@link MessageCreateBuilder} and used for sending messages to channels/webhooks/interactions.
 *
 * @see MessageCreateBuilder
 * @see MessageChannel#sendMessage(MessageCreateData)
 * @see net.dv8tion.jda.api.interactions.callbacks.IReplyCallback#reply(MessageCreateData) IReplyCallback.reply(MessageCreateData)
 * @see net.dv8tion.jda.api.entities.WebhookClient#sendMessage(MessageCreateData) WebhookClient.sendMessage(MessageCreateData)
 */
public class MessageCreateData implements MessageData, AutoCloseable, SerializableData
{
    private final String content;
    private final List<MessageEmbed> embeds;
    private final List<FileUpload> files;
    private final List<MessageTopLevelComponentUnion> components;
    private final AllowedMentionsData mentions;
    private final MessagePollData poll;
    private final boolean tts;
    private final int flags;

    protected MessageCreateData(
            String content,
            List<MessageEmbed> embeds, List<FileUpload> files, List<MessageTopLevelComponentUnion> components,
            AllowedMentionsData mentions, MessagePollData poll, boolean tts, int flags)
    {
        this.content = content;
        this.embeds = Collections.unmodifiableList(embeds);
        this.files = Collections.unmodifiableList(files);
        this.components = Collections.unmodifiableList(components);
        this.mentions = mentions;
        this.poll = poll;
        this.tts = tts;
        this.flags = flags;
    }

    /**
     * Shortcut for {@code new MessageCreateBuilder().setContent(content).build()}.
     *
     * @param  content
     *         The message content (up to {@value Message#MAX_CONTENT_LENGTH})
     *
     * @throws IllegalArgumentException
     *         If the content is null, empty, or longer than {@value Message#MAX_CONTENT_LENGTH}
     *
     * @return New valid instance of MessageCreateData
     *
     * @see    MessageCreateBuilder#setContent(String)
     */
    @Nonnull
    public static MessageCreateData fromContent(@Nonnull String content)
    {
        return new MessageCreateBuilder().setContent(content).build();
    }

    /**
     * Shortcut for {@code new MessageCreateBuilder().setEmbeds(embeds).build()}.
     *
     * @param  embeds
     *         The message embeds (up to {@value Message#MAX_EMBED_COUNT})
     *
     * @throws IllegalArgumentException
     *         If the embed list is null, empty, or longer than {@value Message#MAX_EMBED_COUNT}
     *
     * @return New valid instance of MessageCreateData
     *
     * @see    MessageCreateBuilder#setEmbeds(Collection)
     */
    @Nonnull
    public static MessageCreateData fromEmbeds(@Nonnull Collection<? extends MessageEmbed> embeds)
    {
        return new MessageCreateBuilder().setEmbeds(embeds).build();
    }

    /**
     * Shortcut for {@code new MessageCreateBuilder().setEmbeds(embeds).build()}.
     *
     * @param  embeds
     *         The message embeds (up to {@value Message#MAX_EMBED_COUNT})
     *
     * @throws IllegalArgumentException
     *         If the embed list is null, empty, or longer than {@value Message#MAX_EMBED_COUNT}
     *
     * @return New valid instance of MessageCreateData
     *
     * @see    MessageCreateBuilder#setEmbeds(Collection)
     */
    @Nonnull
    public static MessageCreateData fromEmbeds(@Nonnull MessageEmbed... embeds)
    {
        return new MessageCreateBuilder().setEmbeds(embeds).build();
    }

    /**
     * Shortcut for {@code new MessageCreateBuilder().setFiles(embeds).build()}.
     *
     * @param  files
     *         The file uploads
     *
     * @throws IllegalArgumentException
     *         If the null is provided or the list is empty
     *
     * @return New valid instance of MessageCreateData
     *
     * @see    MessageCreateBuilder#setFiles(Collection)
     */
    @Nonnull
    public static MessageCreateData fromFiles(@Nonnull Collection<? extends FileUpload> files)
    {
        return new MessageCreateBuilder().setFiles(files).build();
    }

    /**
     * Shortcut for {@code new MessageCreateBuilder().setFiles(embeds).build()}.
     *
     * @param  files
     *         The file uploads
     *
     * @throws IllegalArgumentException
     *         If the null is provided or the list is empty
     *
     * @return New valid instance of MessageCreateData
     *
     * @see    MessageCreateBuilder#setFiles(Collection)
     */
    @Nonnull
    public static MessageCreateData fromFiles(@Nonnull FileUpload... files)
    {
        return new MessageCreateBuilder().setFiles(files).build();
    }

    /**
     * Shortcut for {@code new MessageCreateBuilder().applyMessage(message).build()}.
     *
     * @param  message
     *         The message to apply
     *
     * @throws IllegalArgumentException
     *         If the message is null or a system message
     *
     * @return New valid instance of MessageCreateData
     *
     * @see    MessageCreateBuilder#applyMessage(Message)
     */
    @Nonnull
    public static MessageCreateData fromMessage(@Nonnull Message message)
    {
        return new MessageCreateBuilder().applyMessage(message).build();
    }

    /**
     * Shortcut for {@code new MessageCreateBuilder().applyEditData(data).build()}.
     *
     * @param  data
     *         The message edit data to apply
     *
     * @throws IllegalArgumentException
     *         If the data is null or empty
     *
     * @return New valid instance of MessageCreateData
     *
     * @see    MessageCreateBuilder#applyEditData(MessageEditData)
     */
    @Nonnull
    public static MessageCreateData fromEditData(@Nonnull MessageEditData data)
    {
        return new MessageCreateBuilder().applyEditData(data).build();
    }

    /**
     * The content of the message.
     *
     * @return The content or an empty string if none was provided
     */
    @Nonnull
    @Override
    public String getContent()
    {
        return content;
    }

    /**
     * The embeds of the message.
     *
     * @return The embeds or an empty list if none were provided
     */
    @Nonnull
    @Override
    public List<MessageEmbed> getEmbeds()
    {
        return embeds;
    }

    /**
     * The components of the message.
     *
     * @return The components or an empty list if none were provided
     */
    @Nonnull
    @Override
    public List<MessageTopLevelComponentUnion> getComponents()
    {
        return components;
    }

    @Override
    public boolean isUsingComponentsV2()
    {
        return (flags & Message.MessageFlag.IS_COMPONENTS_V2.getValue()) != 0;
    }

    @Nonnull
    @Override
    public List<? extends FileUpload> getAttachments()
    {
        return getFiles();
    }

    /**
     * The poll to send with the message
     *
     * @return The poll, or null if no poll is sent
     */
    @Nullable
    public MessagePollData getPoll()
    {
        return poll;
    }

    @Override
    public boolean isSuppressEmbeds()
    {
        return (flags & Message.MessageFlag.EMBEDS_SUPPRESSED.getValue()) != 0;
    }

    /**
     * Whether this message uses <em>Text-to-Speech</em> (TTS).
     *
     * @return True, if text to speech will be used when this is sent
     */
    public boolean isTTS()
    {
        return tts;
    }

    /**
     * Whether this message is silent.
     *
     * @return True, if the message will not trigger push and desktop notifications.
     */
    public boolean isSuppressedNotifications()
    {
        return (flags & Message.MessageFlag.NOTIFICATIONS_SUPPRESSED.getValue()) != 0;
    }

    /**
     * Whether this message is intended as a voice message.
     *
     * @return True, if this message is intended as a voice message.
     */
    public boolean isVoiceMessage()
    {
        return (flags & Message.MessageFlag.IS_VOICE_MESSAGE.getValue()) != 0;
    }

    /**
     * The IDs for users which are allowed to be mentioned, or an empty list.
     *
     * @return The user IDs which are mention whitelisted
     */
    @Nonnull
    @Override
    public Set<String> getMentionedUsers()
    {
        return mentions.getMentionedUsers();
    }

    /**
     * The IDs for roles which are allowed to be mentioned, or an empty list.
     *
     * @return The role IDs which are mention whitelisted
     */
    @Nonnull
    @Override
    public Set<String> getMentionedRoles()
    {
        return mentions.getMentionedRoles();
    }

    /**
     * The mention types which are whitelisted.
     *
     * @return The mention types which can be mentioned by this message
     */
    @Nonnull
    @Override
    public EnumSet<Message.MentionType> getAllowedMentions()
    {
        return mentions.getAllowedMentions();
    }

    /**
     * Whether this message would mention a user, if it is sent as a reply.
     *
     * @return True, if this would mention with the reply
     */
    @Override
    public boolean isMentionRepliedUser()
    {
        return mentions.isMentionRepliedUser();
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        DataObject json = DataObject.empty();
        if (!isUsingComponentsV2())
        {
            json.put("content", content);
            json.put("poll", poll);
            json.put("embeds", DataArray.fromCollection(embeds));
        }
        json.put("components", DataArray.fromCollection(components));
        json.put("tts", tts);
        json.put("flags", flags);
        json.put("allowed_mentions", mentions);
        final List<FileUpload> additionalFiles = getAdditionalFiles();
        if ((files != null && !files.isEmpty()) || !additionalFiles.isEmpty())
            json.put("attachments", MessageUtil.getAttachmentsData(files, additionalFiles));

        return json;
    }

    /**
     * The {@link FileUpload FileUploads} attached to this message.
     *
     * @return The list of file uploads
     */
    @Nonnull
    public List<FileUpload> getFiles()
    {
        return files;
    }

    /**
     * Returns the {@link FileUpload FileUploads} that are added indirectly to this message,
     * such as from V2 components and embeds.
     *
     * @return The list of additional file uploads
     */
    @Nonnull
    public List<FileUpload> getAdditionalFiles()
    {
        return ComponentIterator.createStream(components)
                .filter(FileContainerMixin.class::isInstance)
                .map(FileContainerMixin.class::cast)
                .flatMap(FileContainerMixin::getFiles)
                .collect(Collectors.toList());
    }

    @Override
    public void close()
    {
        files.forEach(IOUtil::silentClose);
    }
}
