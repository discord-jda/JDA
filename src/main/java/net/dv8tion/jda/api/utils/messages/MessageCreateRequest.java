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
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
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
        return setEmbeds(Arrays.asList(embeds));
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
        return setComponents(Arrays.asList(components));
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
        return setComponents(ActionRow.of(components));
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
        return setComponents(ActionRow.of(components));
    }

    /**
     * Appends the provided {@link FileUpload FileUploads} to the request.
     * <br>Use {@link #setFiles(Collection)} instead, to replace the file attachments entirely.
     *
     * <p>Note that you are responsible to properly clean up your files, if the request is unsuccessful.
     * The {@link FileUpload} class will try to close it when its collected as garbage, but that can take a long time to happen.
     * You can always use {@link FileUpload#close()} and close it manually, however this should not be done until the request went through successfully.
     * The library reads the underlying resource <em>just in time</em> for the request, and will keep it open until then.
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
     * <p>Note that you are responsible to properly clean up your files, if the request is unsuccessful.
     * The {@link FileUpload} class will try to close it when its collected as garbage, but that can take a long time to happen.
     * You can always use {@link FileUpload#close()} and close it manually, however this should not be done until the request went through successfully.
     * The library reads the underlying resource <em>just in time</em> for the request, and will keep it open until then.
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
    List<? extends FileUpload> getAttachments();

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
        Checks.notNull(message, "Message");
        Checks.check(!message.getType().isSystem(), "Cannot copy a system message");
        List<MessageEmbed> embeds = message.getEmbeds()
                .stream()
                .filter(e -> e.getType() == EmbedType.RICH)
                .collect(Collectors.toList());
        return setContent(message.getContentRaw())
                .setEmbeds(embeds)
                .setTTS(message.isTTS())
                .setComponents(message.getActionRows());
    }

    /**
     * Applies the provided {@link MessageEditData} to this request.
     * <br>This will only set fields which were explicitly set on the {@link MessageEditBuilder},
     * unless it was configured to be {@link MessageEditRequest#replace(boolean) replacing}.
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
