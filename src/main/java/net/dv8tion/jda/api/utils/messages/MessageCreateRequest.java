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

import net.dv8tion.jda.annotations.ForRemoval;
import net.dv8tion.jda.annotations.ReplaceWith;
import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.interactions.components.action_row.ActionRow;
import net.dv8tion.jda.api.interactions.components.action_row.ActionRowChildComponent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.ComponentsUtil;
import okhttp3.MediaType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Specialized abstraction of setters and accumulators for creating messages throughout the API.
 *
 * @param <R>
 *        The return type for method chaining convenience
 *
 * @see   MessageCreateBuilder
 * @see   MessageCreateData
 * @see   net.dv8tion.jda.api.requests.restaction.MessageCreateAction MessageCreateAction
 */
public interface MessageCreateRequest<R extends MessageCreateRequest<R>> extends MessageRequest<R>
{
    /**
     * Appends the content to the currently set content of this request.
     * <br>Use {@link #setContent(String)} instead, to replace the content entirely.
     *
     * <p><b>Example</b><br>
     * Sending a message with the content {@code "Hello World!"}:
     * <pre>{@code
     * channel.sendMessage("Hello ").addContent("World!").queue();
     * }</pre>
     *
     * @param  content
     *         The content to append
     *
     * @throws IllegalArgumentException
     *         If the provided content is {@code null} or the accumulated content is longer than {@value Message#MAX_CONTENT_LENGTH} characters
     *
     * @return The same instance for chaining
     */
    @Nonnull
    R addContent(@Nonnull String content);

    /**
     * Appends the provided {@link MessageEmbed MessageEmbeds} to the request.
     * <br>Use {@link #setEmbeds(Collection)} instead, to replace the embeds entirely.
     *
     * <p><b>Example</b><br>
     * Sending a message with multiple embeds:
     * <pre>{@code
     * channel.sendMessageEmbeds(embed1).addEmbeds(embed2).queue();
     * }</pre>
     *
     * @param  embeds
     *         The embeds to add
     *
     * @throws IllegalArgumentException
     *         If null is provided or the accumulated embed list is longer than {@value Message#MAX_EMBED_COUNT}
     *
     * @return The same instance for chaining
     */
    @Nonnull
    R addEmbeds(@Nonnull Collection<? extends MessageEmbed> embeds);

    /**
     * Appends the provided {@link MessageEmbed MessageEmbeds} to the request.
     * <br>Use {@link #setEmbeds(Collection)} instead, to replace the embeds entirely.
     *
     * <p><b>Example</b><br>
     * Sending a message with multiple embeds:
     * <pre>{@code
     * channel.sendMessageEmbeds(embed1).addEmbeds(embed2).queue();
     * }</pre>
     *
     * @param  embeds
     *         The embeds to add
     *
     * @throws IllegalArgumentException
     *         If null is provided or the accumulated embed list is longer than {@value  Message#MAX_EMBED_COUNT}
     *
     * @return The same instance for chaining
     */
    @Nonnull
    default R addEmbeds(@Nonnull MessageEmbed... embeds)
    {
        return addEmbeds(Arrays.asList(embeds));
    }

    /**
     * Appends the provided {@link MessageTopLevelComponent MessageTopLevelComponents} to the request.
     * <br>Use {@link #setComponentTree(Collection)} instead, to replace the components entirely.
     *
     * <p><b>Example</b><br>
     * Sending a message with multiple action rows:
     * <pre>{@code
     * channel.sendMessageComponents(ActionRow.of(selectMenu))
     *        .addComponents(ActionRow.of(button1, button2))
     *        .queue();
     * }</pre>
     *
     * @param  components
     *         The layout components to add
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code null} is provided</li>
     *             <li>If any of the components is not {@link MessageTopLevelComponent#isMessageCompatible() message compatible}</li>
     *             <li>If the accumulated list of components is longer than {@value Message#MAX_COMPONENT_COUNT}</li>
     *         </ul>
     *
     * @return The same instance for chaining
     *
     * @see    ActionRow
     */
    @Nonnull
    R addComponentTree(@Nonnull Collection<? extends MessageTopLevelComponent> components);

    /**
     * Appends the provided {@link MessageTopLevelComponent MessageTopLevelComponents} to the request.
     * <br>Use {@link #setComponentTree(Collection)} instead, to replace the components entirely.
     *
     * <p><b>Example</b><br>
     * Sending a message with multiple action rows:
     * <pre>{@code
     * channel.sendMessageComponents(ActionRow.of(selectMenu))
     *        .addComponents(ActionRow.of(button1, button2))
     *        .queue();
     * }</pre>
     *
     * @param  components
     *         The layout components to add
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code null} is provided</li>
     *             <li>If any of the components is not {@link MessageTopLevelComponent#isMessageCompatible() message compatible}</li>
     *             <li>If the accumulated list of components is longer than {@value Message#MAX_COMPONENT_COUNT}</li>
     *         </ul>
     *
     * @return The same instance for chaining
     *
     * @see    ActionRow
     */
    @Nonnull
    default R addComponentTree(@Nonnull MessageTopLevelComponent... components)
    {
        return addComponentTree(Arrays.asList(components));
    }

    /**
     * Appends the provided {@link LayoutComponent LayoutComponents} to the request.
     * <br>Use {@link #setComponentTree(Collection)} instead, to replace the components entirely.
     *
     * <p><b>Example</b><br>
     * Sending a message with multiple action rows:
     * <pre>{@code
     * channel.sendMessageComponents(ActionRow.of(selectMenu))
     *        .addComponents(ActionRow.of(button1, button2))
     *        .queue();
     * }</pre>
     *
     * @param  components
     *         The layout components to add
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code null} is provided</li>
     *             <li>If any of the components is not {@link LayoutComponent#isMessageCompatible() message compatible}</li>
     *             <li>If the accumulated list of components is longer than {@value Message#MAX_COMPONENT_COUNT}</li>
     *         </ul>
     *
     * @return The same instance for chaining
     *
     * @see    ActionRow
     *
     * @deprecated
     *         Replaced with {@link #addActionRows(Collection)}
     */
    @Nonnull
    @Deprecated
    @ForRemoval
    default R addComponents(@Nonnull Collection<? extends LayoutComponent<?>> components)
    {
        return addComponentTree(ComponentsUtil.ensureIsActionRow(components));
    }

    /**
     * Appends the provided {@link LayoutComponent LayoutComponents} to the request.
     * <br>Use {@link #setComponentTree(Collection)} instead, to replace the components entirely.
     *
     * <p><b>Example</b><br>
     * Sending a message with multiple action rows:
     * <pre>{@code
     * channel.sendMessageComponents(ActionRow.of(selectMenu))
     *        .addComponents(ActionRow.of(button1, button2))
     *        .queue();
     * }</pre>
     *
     * @param  components
     *         The layout components to add
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code null} is provided</li>
     *             <li>If any of the components is not {@link LayoutComponent#isMessageCompatible() message compatible}</li>
     *             <li>If the accumulated list of components is longer than {@value Message#MAX_COMPONENT_COUNT}</li>
     *         </ul>
     *
     * @return The same instance for chaining
     *
     * @see    ActionRow
     *
     * @deprecated
     *         Replaced with {@link #addActionRows(ActionRow...)}
     */
    @Nonnull
    @Deprecated
    @ForRemoval
    default R addComponents(@Nonnull LayoutComponent<?>... components)
    {
        return addComponentTree(Arrays.asList(ComponentsUtil.ensureIsActionRow(components)));
    }

    /**
     * Appends the provided {@link LayoutComponent LayoutComponents} to the request.
     * <br>Use {@link #setComponentTree(Collection)} instead, to replace the components entirely.
     *
     * <p><b>Example</b><br>
     * Sending a message with multiple action rows:
     * <pre>{@code
     * channel.sendMessageComponents(ActionRow.of(selectMenu))
     *        .addComponents(ActionRow.of(button1, button2))
     *        .queue();
     * }</pre>
     *
     * @param  components
     *         The layout components to add
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code null} is provided</li>
     *             <li>If any of the components is not {@link LayoutComponent#isMessageCompatible() message compatible}</li>
     *             <li>If the accumulated list of components is longer than {@value Message#MAX_COMPONENT_COUNT}</li>
     *         </ul>
     *
     * @return The same instance for chaining
     *
     * @see    ActionRow
     */
    @Nonnull
    R addActionRows(@Nonnull Collection<? extends ActionRow> components);

    /**
     * Appends the provided {@link LayoutComponent LayoutComponents} to the request.
     * <br>Use {@link #setComponentTree(Collection)} instead, to replace the components entirely.
     *
     * <p><b>Example</b><br>
     * Sending a message with multiple action rows:
     * <pre>{@code
     * channel.sendMessageComponents(ActionRow.of(selectMenu))
     *        .addComponents(ActionRow.of(button1, button2))
     *        .queue();
     * }</pre>
     *
     * @param  components
     *         The layout components to add
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code null} is provided</li>
     *             <li>If any of the components is not {@link LayoutComponent#isMessageCompatible() message compatible}</li>
     *             <li>If the accumulated list of components is longer than {@value Message#MAX_COMPONENT_COUNT}</li>
     *         </ul>
     *
     * @return The same instance for chaining
     *
     * @see    ActionRow
     */
    @Nonnull
    default R addActionRows(@Nonnull ActionRow... components)
    {
        return addActionRows(Arrays.asList(components));
    }

    /**
     * Appends a single {@link ActionRow} to the request.
     * <br>Use {@link #setComponentTree(Collection)} instead, to replace the components entirely.
     *
     * <p><b>Example</b><br>
     * Sending a message with multiple action rows:
     * <pre>{@code
     * channel.sendMessageComponents(ActionRow.of(selectMenu))
     *        .addActionRow(button1, button2)
     *        .queue();
     * }</pre>
     *
     * @param  components
     *         The {@link ActionRowChildComponent components} to add to the action row, must not be empty
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code null} is provided</li>
     *             <li>If any of the components is not {@link Component#isMessageCompatible() message compatible}</li>
     *             <li>If the accumulated list of components is longer than {@value Message#MAX_COMPONENT_COUNT}</li>
     *             <li>In all the same cases as {@link ActionRow#of(ActionRowChildComponent...)} throws an exception</li>
     *         </ul>
     *
     * @return The same instance for chaining
     *
     * @see    ActionRow#of(ActionRowChildComponent...)
     *
     * @deprecated
     *         Replace with {@link #addActionRows(ActionRow...) addActionRows(ActionRow.of(components))}
     */
    @Nonnull
    @Deprecated
    @ReplaceWith("setActionRows(ActionRow.of(components))")
    default R addActionRow(@Nonnull Collection<? extends ItemComponent> components)
    {
        return addActionRows(ActionRow.of(components));
    }

    /**
     * Appends a single {@link ActionRow} to the request.
     * <br>Use {@link #setComponentTree(Collection)} instead, to replace the components entirely.
     *
     * <p><b>Example</b><br>
     * Sending a message with multiple action rows:
     * <pre>{@code
     * channel.sendMessageComponents(ActionRow.of(selectMenu))
     *        .addActionRow(button1, button2)
     *        .queue();
     * }</pre>
     *
     * @param  components
     *         The {@link ActionRowChildComponent components} to add to the action row, must not be empty
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code null} is provided</li>
     *             <li>If any of the components is not {@link Component#isMessageCompatible() message compatible}</li>
     *             <li>If the accumulated list of components is longer than {@value Message#MAX_COMPONENT_COUNT}</li>
     *             <li>In all the same cases as {@link ActionRow#of(ActionRowChildComponent...)} throws an exception</li>
     *         </ul>
     *
     * @return The same instance for chaining
     *
     * @see    ActionRow#of(ActionRowChildComponent...)
     *
     * @deprecated
     *         Replace with {@link #addActionRows(ActionRow...) addActionRows(ActionRow.of(components))}
     */
    @Nonnull
    @Deprecated
    @ReplaceWith("setActionRows(ActionRow.of(components))")
    default R addActionRow(@Nonnull ItemComponent... components)
    {
        return addActionRows(ActionRow.of(components));
    }

    /**
     * Appends the provided {@link FileUpload FileUploads} to the request.
     * <br>Use {@link #setFiles(Collection)} instead, to replace the file attachments entirely.
     *
     * <p><b>Resource Handling Note:</b> Once the request is handed off to the requester, for example when you call {@link RestAction#queue()},
     * the requester will automatically clean up all opened files by itself. You are only responsible to close them yourself if it is never handed off properly.
     * For instance, if an exception occurs after using {@link FileUpload#fromData(File)}, before calling {@link RestAction#queue()}.
     * You can safely use a try-with-resources to handle this, since {@link FileUpload#close()} becomes ineffective once the request is handed off.
     *
     * <p><b>Example</b><br>
     * Sending a message with multiple files:
     * <pre>{@code
     * channel.sendFiles(file1).addFiles(file2).queue();
     * }</pre>
     *
     * @param  files
     *         The files to add
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return The same instance for chaining
     */
    @Nonnull
    R addFiles(@Nonnull Collection<? extends FileUpload> files);

    /**
     * Appends the provided {@link FileUpload FileUploads} to the request.
     * <br>Use {@link #setFiles(Collection)} instead, to replace the file attachments entirely.
     *
     * <p><b>Resource Handling Note:</b> Once the request is handed off to the requester, for example when you call {@link RestAction#queue()},
     * the requester will automatically clean up all opened files by itself. You are only responsible to close them yourself if it is never handed off properly.
     * For instance, if an exception occurs after using {@link FileUpload#fromData(File)}, before calling {@link RestAction#queue()}.
     * You can safely use a try-with-resources to handle this, since {@link FileUpload#close()} becomes ineffective once the request is handed off.
     *
     * <p><b>Example</b><br>
     * Sending a message with multiple files:
     * <pre>{@code
     * channel.sendFiles(file1).addFiles(file2).queue();
     * }</pre>
     *
     * @param  files
     *         The files to add
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return The same instance for chaining
     */
    @Nonnull
    default R addFiles(@Nonnull FileUpload... files)
    {
        return addFiles(Arrays.asList(files));
    }

    @Nonnull
    @Override
    List<FileUpload> getAttachments();

    /**
     * The poll attached to this message
     *
     * @return The attached poll, or null if no poll is present
     */
    @Nullable
    MessagePollData getPoll();

    /**
     * Add a poll to this message.
     *
     * @param  poll
     *         The poll to send
     *
     * @return The same instance for chaining
     *
     * @see    MessagePollBuilder
     */
    @Nonnull
    R setPoll(@Nullable MessagePollData poll);

    /**
     * Whether the message should use <em>Text-to-Speech</em> (TTS).
     *
     * <p>Requires {@link net.dv8tion.jda.api.Permission#MESSAGE_TTS Permission.MESSAGE_TTS} to be enabled.
     *
     * @param  tts
     *         True, if the message should use TTS
     *
     * @return The same instance for chaining
     */
    @Nonnull
    R setTTS(boolean tts);

    /**
     * Set whether this message should trigger push/desktop notifications to other users.
     * <br>When a message is suppressed, it will not trigger push/desktop notifications.
     *
     * @param  suppressed
     *         True, if this message should not trigger push/desktop notifications
     *
     * @return The same instance for chaining
     */
    @Nonnull
    R setSuppressedNotifications(boolean suppressed);

    /**
     * Whether this message should be considered a voice message.
     * <br>Voice messages must upload a valid voice message attachment, using {@link FileUpload#asVoiceMessage(MediaType, byte[], double)}.
     *
     * @param  voiceMessage
     *         True, if this message is a voice message.
     *         Turned on automatically if attachment is a valid voice message attachment.
     *
     * @return The same instance for chaining
     */
    @Nonnull
    R setVoiceMessage(boolean voiceMessage);

    /**
     * Applies the provided {@link MessageCreateData} to this request.
     *
     * @param  data
     *         The message create data to apply
     *
     * @throws IllegalArgumentException
     *         If the data is null
     *
     * @return The same instance for chaining
     */
    @Nonnull
    default R applyData(@Nonnull MessageCreateData data)
    {
        Checks.notNull(data, "MessageCreateData");

        if (data.isUsingComponentsV2())
            setComponentTree(data.getComponents());
        else
            // TODO-components-v2 - fix by adding getActionRows(), maybe the method should be gated to isUsingComponentsV2()
            setActionRows((Collection<? extends ActionRow>) (Collection<?>) data.getComponents());
        return setContent(data.getContent())
                .setAllowedMentions(data.getAllowedMentions())
                .mentionUsers(data.getMentionedUsers())
                .mentionRoles(data.getMentionedRoles())
                .mentionRepliedUser(data.isMentionRepliedUser())
                .setEmbeds(data.getEmbeds())
                .setTTS(data.isTTS())
                .setSuppressEmbeds(data.isSuppressEmbeds())
                .setSuppressedNotifications(data.isSuppressedNotifications())
                .setVoiceMessage(data.isVoiceMessage())
                .setPoll(data.getPoll())
                .setFiles(data.getFiles());
    }

    @Nonnull
    default R applyMessage(@Nonnull Message message)
    {
        Checks.notNull(message, "Message");
        Checks.check(!message.getType().isSystem(), "Cannot copy a system message");
        List<MessageEmbed> embeds = message.getEmbeds()
                .stream()
                .filter(e -> e.getType() == EmbedType.RICH)
                .collect(Collectors.toList());
        if (message.isUsingComponentsV2())
            setComponentTree(message.getComponents());
        else
            setActionRows(message.getActionRows());
        return setContent(message.getContentRaw())
                .setEmbeds(embeds)
                .setTTS(message.isTTS())
                .setSuppressedNotifications(message.isSuppressedNotifications())
                .setVoiceMessage(message.isVoiceMessage())
                .setPoll(message.getPoll() != null ? MessagePollData.from(message.getPoll()) : null);
    }

    /**
     * Applies the provided {@link MessageEditData} to this request.
     * <br>This will only set fields which were explicitly set on the {@link MessageEditBuilder},
     * unless it was configured to be {@link MessageEditRequest#setReplace(boolean) replacing}.
     *
     * <p>This will <b>not</b> copy the message's attachments, only any configured {@link FileUpload FileUploads}.
     * To copy attachments, you must download them explicitly instead.
     *
     * @param  data
     *         The message create data to apply
     *
     * @throws IllegalArgumentException
     *         If the data is null
     *
     * @return The same instance for chaining
     */
    @Nonnull
    @SuppressWarnings({"unchecked", "ResultOfMethodCallIgnored"})
    default R applyEditData(@Nonnull MessageEditData data)
    {
        Checks.notNull(data, "MessageEditData");
        if (data.isSet(MessageEditBuilder.CONTENT))
            setContent(data.getContent());
        if (data.isSet(MessageEditBuilder.EMBEDS))
            setEmbeds(data.getEmbeds());
        if (data.isSet(MessageEditBuilder.COMPONENTS))
        {
            if (data.isUsingComponentsV2())
                setComponentTree(data.getComponents());
            else
                // TODO-components-v2 fix with data.getActionRows();
                setActionRows((Collection<? extends ActionRow>) (Collection<?>) data.getComponents());
        }
        if (data.isSet(MessageEditBuilder.ATTACHMENTS))
            setFiles(data.getFiles());
        if (data.isSet(MessageEditBuilder.MENTIONS))
        {
            setAllowedMentions(data.getAllowedMentions());
            mentionUsers(data.getMentionedUsers());
            mentionRoles(data.getMentionedRoles());
            mentionRepliedUser(data.isMentionRepliedUser());
        }

        return (R) this;
    }
}
