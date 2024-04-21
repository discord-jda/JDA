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

import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
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
     * Appends the provided {@link LayoutComponent LayoutComponents} to the request.
     * <br>Use {@link #setComponents(Collection)} instead, to replace the components entirely.
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
    R addComponents(@Nonnull Collection<? extends LayoutComponent> components);

    /**
     * Appends the provided {@link LayoutComponent LayoutComponents} to the request.
     * <br>Use {@link #setComponents(Collection)} instead, to replace the components entirely.
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
    default R addComponents(@Nonnull LayoutComponent... components)
    {
        return addComponents(Arrays.asList(components));
    }

    /**
     * Appends a single {@link ActionRow} to the request.
     * <br>Use {@link #setComponents(Collection)} instead, to replace the components entirely.
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
     *         The {@link ItemComponent components} to add to the action row, must not be empty
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code null} is provided</li>
     *             <li>If any of the components is not {@link ItemComponent#isMessageCompatible() message compatible}</li>
     *             <li>If the accumulated list of components is longer than {@value Message#MAX_COMPONENT_COUNT}</li>
     *             <li>In all the same cases as {@link ActionRow#of(Collection)} throws an exception</li>
     *         </ul>
     *
     * @return The same instance for chaining
     *
     * @see    ActionRow#of(Collection)
     */
    @Nonnull
    default R addActionRow(@Nonnull Collection<? extends ItemComponent> components)
    {
        return addComponents(ActionRow.of(components));
    }

    /**
     * Appends a single {@link ActionRow} to the request.
     * <br>Use {@link #setComponents(Collection)} instead, to replace the components entirely.
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
     *         The {@link ItemComponent components} to add to the action row, must not be empty
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code null} is provided</li>
     *             <li>If any of the components is not {@link ItemComponent#isMessageCompatible() message compatible}</li>
     *             <li>If the accumulated list of components is longer than {@value Message#MAX_COMPONENT_COUNT}</li>
     *             <li>In all the same cases as {@link ActionRow#of(ItemComponent...)} throws an exception</li>
     *         </ul>
     *
     * @return The same instance for chaining
     *
     * @see    ActionRow#of(ItemComponent...)
     */
    @Nonnull
    default R addActionRow(@Nonnull ItemComponent... components)
    {
        return addComponents(ActionRow.of(components));
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
     * @return The same reply action, for chaining convenience
     */
    @Nonnull
    R setSuppressedNotifications(boolean suppressed);

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

        final List<LayoutComponent> layoutComponents = data.getComponents().stream()
                .map(LayoutComponent::createCopy)
                .collect(Collectors.toList());
        return setContent(data.getContent())
                .setAllowedMentions(data.getAllowedMentions())
                .mentionUsers(data.getMentionedUsers())
                .mentionRoles(data.getMentionedRoles())
                .mentionRepliedUser(data.isMentionRepliedUser())
                .setEmbeds(data.getEmbeds())
                .setTTS(data.isTTS())
                .setSuppressEmbeds(data.isSuppressEmbeds())
                .setSuppressedNotifications(data.isSuppressedNotifications())
                .setComponents(layoutComponents)
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
        return setContent(message.getContentRaw())
                .setEmbeds(embeds)
                .setTTS(message.isTTS())
                .setSuppressedNotifications(message.isSuppressedNotifications())
                .setComponents(message.getActionRows());
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
            final List<LayoutComponent> layoutComponents = data.getComponents().stream()
                    .map(LayoutComponent::createCopy)
                    .collect(Collectors.toList());
            setComponents(layoutComponents);
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
