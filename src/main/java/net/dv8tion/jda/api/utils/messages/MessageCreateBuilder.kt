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
import net.dv8tion.jda.api.interactions.components.LayoutComponent
import net.dv8tion.jda.api.utils.AttachedFile
import net.dv8tion.jda.api.utils.FileUpload
import net.dv8tion.jda.internal.utils.Checks
import net.dv8tion.jda.internal.utils.Helpers
import net.dv8tion.jda.internal.utils.IOUtil
import java.util.*
import java.util.function.Consumer
import javax.annotation.Nonnull

/**
 * Builder specialized for building a [MessageCreateData].
 * <br></br>This can be used to build a request and send it to various API endpoints.
 *
 *
 * **Example**
 * <pre>`try (FileUpload file = FileUpload.fromData(new File("wave.gif"))) {
 * MessageCreateData data = new MessageCreateBuilder()
 * .setContent("Hello guys!")
 * .setTTS(true)
 * .setFiles(file)
 * .build();
 *
 * for (MessageChannel channel : channels) {
 * channel.sendMessage(data).queue();
 * }
 * } // closes wave.gif if an error occurred
`</pre> *
 *
 * @see MessageChannel.sendMessage
 * @see net.dv8tion.jda.api.interactions.callbacks.IReplyCallback.reply
 * @see net.dv8tion.jda.api.interactions.InteractionHook.sendMessage
 * @see MessageEditBuilder
 */
class MessageCreateBuilder : AbstractMessageBuilder<MessageCreateData, MessageCreateBuilder>(),
    MessageCreateRequest<MessageCreateBuilder> {
    private val files: MutableList<FileUpload?> = ArrayList(10)
    private var tts = false
    @Nonnull
    override fun addContent(@Nonnull content: String?): MessageCreateBuilder {
        Checks.notNull(content, "Content")
        Checks.check(
            Helpers.codePointLength(this.content) + Helpers.codePointLength(content) <= Message.MAX_CONTENT_LENGTH,
            "Cannot have content longer than %d characters", Message.MAX_CONTENT_LENGTH
        )
        this.content.append(content)
        return this
    }

    @Nonnull
    override fun addEmbeds(@Nonnull embeds: Collection<MessageEmbed?>): MessageCreateBuilder {
        Checks.noneNull(embeds, "Embeds")
        Checks.check(
            this.embeds.size + embeds.size <= Message.MAX_EMBED_COUNT,
            "Cannot add more than %d embeds", Message.MAX_EMBED_COUNT
        )
        this.embeds.addAll(embeds)
        return this
    }

    @Nonnull
    override fun addComponents(@Nonnull components: Collection<LayoutComponent?>): MessageCreateBuilder {
        Checks.noneNull(components, "ComponentLayouts")
        for (layout in components) Checks.check(
            layout!!.isMessageCompatible,
            "Provided component layout is invalid for messages!"
        )
        Checks.check(
            this.components.size + components.size <= Message.MAX_COMPONENT_COUNT,
            "Cannot add more than %d component layouts", Message.MAX_COMPONENT_COUNT
        )
        this.components.addAll(components)
        return this
    }

    @Nonnull
    override fun setFiles(files: Collection<FileUpload?>?): MessageCreateBuilder {
        if (files != null) Checks.noneNull(files, "Files")
        this.files.clear()
        if (files != null) this.files.addAll(files)
        return this
    }

    @get:Nonnull
    override val attachments: List<AttachedFile?>?
        get() = Collections.unmodifiableList(files)

    @Nonnull
    override fun addFiles(@Nonnull files: Collection<FileUpload?>?): MessageCreateBuilder {
        Checks.noneNull(files, "Files")
        this.files.addAll(files!!)
        return this
    }

    @Nonnull
    override fun setTTS(tts: Boolean): MessageCreateBuilder {
        this.tts = tts
        return this
    }

    @Nonnull
    override fun setSuppressedNotifications(suppressed: Boolean): MessageCreateBuilder {
        messageFlags =
            if (suppressed) messageFlags or Message.MessageFlag.NOTIFICATIONS_SUPPRESSED.value else messageFlags and Message.MessageFlag.NOTIFICATIONS_SUPPRESSED.value.inv()
        return this
    }

    override val isEmpty: Boolean
        get() = Helpers.isBlank(content) && embeds.isEmpty() && files.isEmpty() && components.isEmpty()
    override val isValid: Boolean
        get() = !isEmpty && (embeds.size <= Message.MAX_EMBED_COUNT
                ) && (components.size <= Message.MAX_COMPONENT_COUNT
                ) && (Helpers.codePointLength(content) <= Message.MAX_CONTENT_LENGTH)

    @Nonnull
    override fun build(): MessageCreateData {
        // Copy to prevent modifying data after building
        val content = content.toString().trim { it <= ' ' }
        val embeds: List<MessageEmbed?> = ArrayList(embeds)
        val files: List<FileUpload?> = ArrayList(files)
        val components: List<LayoutComponent?> = ArrayList(components)
        val mentions = mentions.copy()
        check(!(content.isEmpty() && embeds.isEmpty() && files.isEmpty() && components.isEmpty())) { "Cannot build an empty message. You need at least one of content, embeds, components, or files" }
        val length = Helpers.codePointLength(content)
        check(!(length > Message.MAX_CONTENT_LENGTH)) { "Message content is too long! Max length is " + Message.MAX_CONTENT_LENGTH + " characters, provided " + length }
        if (embeds.size > Message.MAX_EMBED_COUNT) throw IllegalStateException("Cannot build message with over " + Message.MAX_EMBED_COUNT + " embeds, provided " + embeds.size)
        if (components.size > Message.MAX_COMPONENT_COUNT) throw IllegalStateException("Cannot build message with over " + Message.MAX_COMPONENT_COUNT + " component layouts, provided " + components.size)
        return MessageCreateData(content, embeds, files, components, mentions, tts, messageFlags)
    }

    @Nonnull
    override fun clear(): MessageCreateBuilder {
        super.clear()
        files.clear()
        tts = false
        return this
    }

    @Nonnull
    override fun closeFiles(): MessageCreateBuilder {
        files.forEach(Consumer { closeable: FileUpload? -> IOUtil.silentClose(closeable) })
        files.clear()
        return this
    }

    companion object {
        /**
         * Factory method to start a builder from an existing instance of [MessageCreateData].
         * <br></br>Equivalent to `new MessageCreateBuilder().applyData(data)`.
         *
         * @param  data
         * The message create data to apply
         *
         * @throws IllegalArgumentException
         * If null is provided
         *
         * @return A new MessageCreateBuilder instance with the applied data
         *
         * @see .applyData
         */
        @Nonnull
        fun from(@Nonnull data: MessageCreateData?): MessageCreateBuilder? {
            return MessageCreateBuilder().applyData(data!!)
        }

        /**
         * Factory method to start a builder from an existing instance of [MessageEditData].
         * <br></br>Equivalent to `new MessageCreateBuilder().applyEditData(data)`.
         * <br></br>This will only set fields which were explicitly set on the [MessageEditBuilder],
         * unless it was configured to be [replacing][MessageEditRequest.setReplace].
         *
         *
         * This will **not** copy the message's attachments, only any configured [FileUploads][FileUpload].
         * To copy attachments, you must download them explicitly instead.
         *
         * @param  data
         * The message edit data to apply
         *
         * @throws IllegalArgumentException
         * If null is provided
         *
         * @return A new MessageCreateBuilder instance with the applied data
         *
         * @see .applyEditData
         */
        @Nonnull
        fun fromEditData(@Nonnull data: MessageEditData?): MessageCreateBuilder? {
            return MessageCreateBuilder().applyEditData(data!!)
        }

        /**
         * Factory method to start a builder from an existing instance of [Message].
         * <br></br>Equivalent to `new MessageCreateBuilder().applyMessage(data)`.
         * <br></br>The [MessageData] are not updated to reflect the provided message, and might mention users that the message did not.
         *
         *
         * This cannot copy the file attachments of the message, they must be manually downloaded and provided to [.setFiles].
         *
         * @param  message
         * The message data to apply
         *
         * @throws IllegalArgumentException
         * If the message is null or a system message
         *
         * @return A new MessageCreateBuilder instance with the applied data
         *
         * @see .applyMessage
         */
        @Nonnull
        fun fromMessage(@Nonnull message: Message?): MessageCreateBuilder {
            return MessageCreateBuilder().applyMessage(message!!)
        }
    }
}
