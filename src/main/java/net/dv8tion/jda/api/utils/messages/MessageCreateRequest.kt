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
package net.dv8tion.jda.api.utils.messages

import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.ActionRow.Companion.of
import net.dv8tion.jda.api.interactions.components.ItemComponent
import net.dv8tion.jda.api.interactions.components.LayoutComponent
import net.dv8tion.jda.api.utils.AttachedFile
import net.dv8tion.jda.api.utils.FileUpload
import net.dv8tion.jda.internal.utils.Checks
import java.util.*
import java.util.function.Function
import java.util.stream.Collectors
import javax.annotation.Nonnull

/**
 * Specialized abstraction of setters and accumulators for creating messages throughout the API.
 *
 * @param <R>
 * The return type for method chaining convenience
 *
 * @see MessageCreateBuilder
 *
 * @see MessageCreateData
 *
 * @see net.dv8tion.jda.api.requests.restaction.MessageCreateAction MessageCreateAction
</R> */
open interface MessageCreateRequest<R : MessageCreateRequest<R>?> : MessageRequest<R> {
    /**
     * Appends the content to the currently set content of this request.
     * <br></br>Use [.setContent] instead, to replace the content entirely.
     *
     *
     * **Example**<br></br>
     * Sending a message with the content `"Hello World!"`:
     * <pre>`channel.sendMessage("Hello ").addContent("World!").queue();
    `</pre> *
     *
     * @param  content
     * The content to append
     *
     * @throws IllegalArgumentException
     * If the provided content is `null` or the accumulated content is longer than {@value Message#MAX_CONTENT_LENGTH} characters
     *
     * @return The same instance for chaining
     */
    @Nonnull
    fun addContent(@Nonnull content: String?): R

    /**
     * Appends the provided [MessageEmbeds][MessageEmbed] to the request.
     * <br></br>Use [.setEmbeds] instead, to replace the embeds entirely.
     *
     *
     * **Example**<br></br>
     * Sending a message with multiple embeds:
     * <pre>`channel.sendMessageEmbeds(embed1).addEmbeds(embed2).queue();
    `</pre> *
     *
     * @param  embeds
     * The embeds to add
     *
     * @throws IllegalArgumentException
     * If null is provided or the accumulated embed list is longer than {@value Message#MAX_EMBED_COUNT}
     *
     * @return The same instance for chaining
     */
    @Nonnull
    fun addEmbeds(@Nonnull embeds: Collection<MessageEmbed?>): R

    /**
     * Appends the provided [MessageEmbeds][MessageEmbed] to the request.
     * <br></br>Use [.setEmbeds] instead, to replace the embeds entirely.
     *
     *
     * **Example**<br></br>
     * Sending a message with multiple embeds:
     * <pre>`channel.sendMessageEmbeds(embed1).addEmbeds(embed2).queue();
    `</pre> *
     *
     * @param  embeds
     * The embeds to add
     *
     * @throws IllegalArgumentException
     * If null is provided or the accumulated embed list is longer than {@value  Message#MAX_EMBED_COUNT}
     *
     * @return The same instance for chaining
     */
    @Nonnull
    fun addEmbeds(@Nonnull vararg embeds: MessageEmbed?): R {
        return addEmbeds(Arrays.asList(*embeds))
    }

    /**
     * Appends the provided [LayoutComponents][LayoutComponent] to the request.
     * <br></br>Use [.setComponents] instead, to replace the components entirely.
     *
     *
     * **Example**<br></br>
     * Sending a message with multiple action rows:
     * <pre>`channel.sendMessageComponents(ActionRow.of(selectMenu))
     * .addComponents(ActionRow.of(button1, button2))
     * .queue();
    `</pre> *
     *
     * @param  components
     * The layout components to add
     *
     * @throws IllegalArgumentException
     *
     *  * If `null` is provided
     *  * If any of the components is not [message compatible][LayoutComponent.isMessageCompatible]
     *  * If the accumulated list of components is longer than {@value Message#MAX_COMPONENT_COUNT}
     *
     *
     * @return The same instance for chaining
     *
     * @see ActionRow
     */
    @Nonnull
    fun addComponents(@Nonnull components: Collection<LayoutComponent?>): R

    /**
     * Appends the provided [LayoutComponents][LayoutComponent] to the request.
     * <br></br>Use [.setComponents] instead, to replace the components entirely.
     *
     *
     * **Example**<br></br>
     * Sending a message with multiple action rows:
     * <pre>`channel.sendMessageComponents(ActionRow.of(selectMenu))
     * .addComponents(ActionRow.of(button1, button2))
     * .queue();
    `</pre> *
     *
     * @param  components
     * The layout components to add
     *
     * @throws IllegalArgumentException
     *
     *  * If `null` is provided
     *  * If any of the components is not [message compatible][LayoutComponent.isMessageCompatible]
     *  * If the accumulated list of components is longer than {@value Message#MAX_COMPONENT_COUNT}
     *
     *
     * @return The same instance for chaining
     *
     * @see ActionRow
     */
    @Nonnull
    fun addComponents(@Nonnull vararg components: LayoutComponent?): R {
        return addComponents(Arrays.asList(*components))
    }

    /**
     * Appends a single [ActionRow] to the request.
     * <br></br>Use [.setComponents] instead, to replace the components entirely.
     *
     *
     * **Example**<br></br>
     * Sending a message with multiple action rows:
     * <pre>`channel.sendMessageComponents(ActionRow.of(selectMenu))
     * .addActionRow(button1, button2)
     * .queue();
    `</pre> *
     *
     * @param  components
     * The [components][ItemComponent] to add to the action row, must not be empty
     *
     * @throws IllegalArgumentException
     *
     *  * If `null` is provided
     *  * If any of the components is not [message compatible][ItemComponent.isMessageCompatible]
     *  * If the accumulated list of components is longer than {@value Message#MAX_COMPONENT_COUNT}
     *  * In all the same cases as [ActionRow.of] throws an exception
     *
     *
     * @return The same instance for chaining
     *
     * @see ActionRow.of
     */
    @Nonnull
    fun addActionRow(@Nonnull components: Collection<ItemComponent?>?): R {
        return addComponents(of(components))
    }

    /**
     * Appends a single [ActionRow] to the request.
     * <br></br>Use [.setComponents] instead, to replace the components entirely.
     *
     *
     * **Example**<br></br>
     * Sending a message with multiple action rows:
     * <pre>`channel.sendMessageComponents(ActionRow.of(selectMenu))
     * .addActionRow(button1, button2)
     * .queue();
    `</pre> *
     *
     * @param  components
     * The [components][ItemComponent] to add to the action row, must not be empty
     *
     * @throws IllegalArgumentException
     *
     *  * If `null` is provided
     *  * If any of the components is not [message compatible][ItemComponent.isMessageCompatible]
     *  * If the accumulated list of components is longer than {@value Message#MAX_COMPONENT_COUNT}
     *  * In all the same cases as [ActionRow.of] throws an exception
     *
     *
     * @return The same instance for chaining
     *
     * @see ActionRow.of
     */
    @Nonnull
    fun addActionRow(@Nonnull vararg components: ItemComponent?): R {
        return addComponents(of(*components))
    }

    /**
     * Appends the provided [FileUploads][FileUpload] to the request.
     * <br></br>Use [.setFiles] instead, to replace the file attachments entirely.
     *
     *
     * **Resource Handling Note:** Once the request is handed off to the requester, for example when you call [RestAction.queue],
     * the requester will automatically clean up all opened files by itself. You are only responsible to close them yourself if it is never handed off properly.
     * For instance, if an exception occurs after using [FileUpload.fromData], before calling [RestAction.queue].
     * You can safely use a try-with-resources to handle this, since [FileUpload.close] becomes ineffective once the request is handed off.
     *
     *
     * **Example**<br></br>
     * Sending a message with multiple files:
     * <pre>`channel.sendFiles(file1).addFiles(file2).queue();
    `</pre> *
     *
     * @param  files
     * The files to add
     *
     * @throws IllegalArgumentException
     * If null is provided
     *
     * @return The same instance for chaining
     */
    @Nonnull
    fun addFiles(@Nonnull files: Collection<FileUpload?>?): R

    /**
     * Appends the provided [FileUploads][FileUpload] to the request.
     * <br></br>Use [.setFiles] instead, to replace the file attachments entirely.
     *
     *
     * **Resource Handling Note:** Once the request is handed off to the requester, for example when you call [RestAction.queue],
     * the requester will automatically clean up all opened files by itself. You are only responsible to close them yourself if it is never handed off properly.
     * For instance, if an exception occurs after using [FileUpload.fromData], before calling [RestAction.queue].
     * You can safely use a try-with-resources to handle this, since [FileUpload.close] becomes ineffective once the request is handed off.
     *
     *
     * **Example**<br></br>
     * Sending a message with multiple files:
     * <pre>`channel.sendFiles(file1).addFiles(file2).queue();
    `</pre> *
     *
     * @param  files
     * The files to add
     *
     * @throws IllegalArgumentException
     * If null is provided
     *
     * @return The same instance for chaining
     */
    @Nonnull
    fun addFiles(@Nonnull vararg files: FileUpload?): R {
        return addFiles(Arrays.asList(*files))
    }

    @get:Nonnull
    abstract override val attachments: List<AttachedFile?>?

    /**
     * Whether the message should use *Text-to-Speech* (TTS).
     *
     *
     * Requires [Permission.MESSAGE_TTS][net.dv8tion.jda.api.Permission.MESSAGE_TTS] to be enabled.
     *
     * @param  tts
     * True, if the message should use TTS
     *
     * @return The same instance for chaining
     */
    @Nonnull
    fun setTTS(tts: Boolean): R

    /**
     * Set whether this message should trigger push/desktop notifications to other users.
     * <br></br>When a message is suppressed, it will not trigger push/desktop notifications.
     *
     * @param  suppressed
     * True, if this message should not trigger push/desktop notifications
     *
     * @return The same reply action, for chaining convenience
     */
    @Nonnull
    fun setSuppressedNotifications(suppressed: Boolean): R

    /**
     * Applies the provided [MessageCreateData] to this request.
     *
     * @param  data
     * The message create data to apply
     *
     * @throws IllegalArgumentException
     * If the data is null
     *
     * @return The same instance for chaining
     */
    @Nonnull
    fun applyData(@Nonnull data: MessageCreateData): R? {
        Checks.notNull(data, "MessageCreateData")
        val layoutComponents: List<LayoutComponent?> = data.getComponents().stream()
            .map(Function({ obj: LayoutComponent? -> obj!!.createCopy() }))
            .collect(Collectors.toList())
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
            .setFiles(data.getFiles())
    }

    @Nonnull
    public override fun applyMessage(@Nonnull message: Message): R {
        Checks.notNull(message, "Message")
        Checks.check(!message.type!!.isSystem(), "Cannot copy a system message")
        val embeds: List<MessageEmbed?> = message.embeds
            .stream()
            .filter({ e -> e!!.type === EmbedType.RICH })
            .collect(Collectors.toList())
        return setContent(message.contentRaw)
            .setEmbeds(embeds)
            .setTTS(message.isTTS)
            .setSuppressedNotifications(message.isSuppressedNotifications)
            .setComponents((message.actionRows)!!)
    }

    /**
     * Applies the provided [MessageEditData] to this request.
     * <br></br>This will only set fields which were explicitly set on the [MessageEditBuilder],
     * unless it was configured to be [replacing][MessageEditRequest.setReplace].
     *
     *
     * This will **not** copy the message's attachments, only any configured [FileUploads][FileUpload].
     * To copy attachments, you must download them explicitly instead.
     *
     * @param  data
     * The message create data to apply
     *
     * @throws IllegalArgumentException
     * If the data is null
     *
     * @return The same instance for chaining
     */
    @Nonnull
    fun applyEditData(@Nonnull data: MessageEditData): R {
        Checks.notNull(data, "MessageEditData")
        if (data.isSet(MessageEditBuilder.Companion.CONTENT)) setContent(data.getContent())
        if (data.isSet(MessageEditBuilder.Companion.EMBEDS)) setEmbeds(data.getEmbeds())
        if (data.isSet(MessageEditBuilder.Companion.COMPONENTS)) {
            val layoutComponents: List<LayoutComponent?> = data.getComponents().stream()
                .map(Function({ obj: LayoutComponent? -> obj!!.createCopy() }))
                .collect(Collectors.toList())
            setComponents(layoutComponents)
        }
        if (data.isSet(MessageEditBuilder.Companion.ATTACHMENTS)) setFiles(data.getFiles())
        if (data.isSet(MessageEditBuilder.Companion.MENTIONS)) {
            setAllowedMentions(data.getAllowedMentions())
            mentionUsers(data.getMentionedUsers())
            mentionRoles(data.getMentionedRoles())
            mentionRepliedUser(data.isMentionRepliedUser())
        }
        return this as R
    }
}
