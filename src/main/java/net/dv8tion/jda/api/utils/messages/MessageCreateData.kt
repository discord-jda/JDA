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
import net.dv8tion.jda.api.entities.Message.MentionType
import net.dv8tion.jda.api.interactions.components.LayoutComponent
import net.dv8tion.jda.api.utils.AttachedFile
import net.dv8tion.jda.api.utils.FileUpload
import net.dv8tion.jda.api.utils.data.DataArray
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.api.utils.data.SerializableData
import net.dv8tion.jda.internal.utils.IOUtil
import java.util.*
import java.util.function.Consumer
import javax.annotation.Nonnull

/**
 * Output of a [MessageCreateBuilder] and used for sending messages to channels/webhooks/interactions.
 *
 * @see MessageCreateBuilder
 *
 * @see MessageChannel.sendMessage
 * @see net.dv8tion.jda.api.interactions.callbacks.IReplyCallback.reply
 * @see net.dv8tion.jda.api.entities.WebhookClient.sendMessage
 */
class MessageCreateData(
    /**
     * The content of the message.
     *
     * @return The content or an empty string if none was provided
     */
    @get:Nonnull override val content: String,
    embeds: List<MessageEmbed?>?, files: List<FileUpload?>?, components: List<LayoutComponent?>?,
    private val mentions: AllowedMentionsData?,
    /**
     * Whether this message uses *Text-to-Speech* (TTS).
     *
     * @return True, if text to speech will be used when this is sent
     */
    val isTTS: Boolean, private val flags: Int
) : MessageData, AutoCloseable, SerializableData {

    /**
     * The embeds of the message.
     *
     * @return The embeds or an empty list if none were provided
     */
    @get:Nonnull
    override val embeds: List<MessageEmbed?>

    /**
     * The [FileUploads][FileUpload] attached to this message.
     *
     * @return The list of file uploads
     */
    @JvmField
    @get:Nonnull
    val files: List<FileUpload?>?

    /**
     * The components of the message.
     *
     * @return The components or an empty list if none were provided
     */
    @get:Nonnull
    override val components: List<LayoutComponent?>

    init {
        this.embeds = Collections.unmodifiableList(embeds)
        this.files = Collections.unmodifiableList(files)
        this.components = Collections.unmodifiableList(components)
    }

    @get:Nonnull
    override val attachments: List<AttachedFile?>?
        get() {
            return files
        }
    override val isSuppressEmbeds: Boolean
        get() {
            return (flags and Message.MessageFlag.EMBEDS_SUPPRESSED.value) != 0
        }
    val isSuppressedNotifications: Boolean
        /**
         * Whether this message is silent.
         *
         * @return True, if the message will not trigger push and desktop notifications
         */
        get() {
            return (flags and Message.MessageFlag.NOTIFICATIONS_SUPPRESSED.value) != 0
        }

    @get:Nonnull
    override val mentionedUsers: Set<String?>?
        /**
         * The IDs for users which are allowed to be mentioned, or an empty list.
         *
         * @return The user IDs which are mention whitelisted
         */
        get() {
            return mentions.getMentionedUsers()
        }

    @get:Nonnull
    override val mentionedRoles: Set<String?>?
        /**
         * The IDs for roles which are allowed to be mentioned, or an empty list.
         *
         * @return The role IDs which are mention whitelisted
         */
        get() {
            return mentions.getMentionedRoles()
        }

    @get:Nonnull
    override val allowedMentions: EnumSet<MentionType?>?
        /**
         * The mention types which are whitelisted.
         *
         * @return The mention types which can be mentioned by this message
         */
        get() {
            return mentions.getAllowedMentions()
        }
    override val isMentionRepliedUser: Boolean
        /**
         * Whether this message would mention a user, if it is sent as a reply.
         *
         * @return True, if this would mention with the reply
         */
        get() {
            return mentions!!.isMentionRepliedUser()
        }

    @Nonnull
    public override fun toData(): DataObject {
        val json: DataObject = DataObject.empty()
        json.put("content", content)
        json.put("embeds", DataArray.fromCollection(embeds))
        json.put("components", DataArray.fromCollection(components))
        json.put("tts", isTTS)
        json.put("flags", flags)
        json.put("allowed_mentions", mentions)
        if (files != null && !files!!.isEmpty()) {
            val attachments: DataArray = DataArray.empty()
            json.put("attachments", attachments)
            for (i in files!!.indices) attachments.add(files!!.get(i)!!.toAttachmentData(i))
        }
        return json
    }

    public override fun close() {
        files!!.forEach(Consumer({ closeable: FileUpload? -> IOUtil.silentClose(closeable) }))
    }

    companion object {
        /**
         * Shortcut for `new MessageCreateBuilder().setContent(content).build()`.
         *
         * @param  content
         * The message content (up to {@value Message#MAX_CONTENT_LENGTH})
         *
         * @throws IllegalArgumentException
         * If the content is null, empty, or longer than {@value Message#MAX_CONTENT_LENGTH}
         *
         * @return New valid instance of MessageCreateData
         *
         * @see MessageCreateBuilder.setContent
         */
        @Nonnull
        fun fromContent(@Nonnull content: String?): MessageCreateData? {
            return MessageCreateBuilder().setContent(content).build()
        }

        /**
         * Shortcut for `new MessageCreateBuilder().setEmbeds(embeds).build()`.
         *
         * @param  embeds
         * The message embeds (up to {@value Message#MAX_EMBED_COUNT})
         *
         * @throws IllegalArgumentException
         * If the embed list is null, empty, or longer than {@value Message#MAX_EMBED_COUNT}
         *
         * @return New valid instance of MessageCreateData
         *
         * @see MessageCreateBuilder.setEmbeds
         */
        @Nonnull
        fun fromEmbeds(@Nonnull embeds: Collection<MessageEmbed?>?): MessageCreateData? {
            return MessageCreateBuilder().setEmbeds(embeds).build()
        }

        /**
         * Shortcut for `new MessageCreateBuilder().setEmbeds(embeds).build()`.
         *
         * @param  embeds
         * The message embeds (up to {@value Message#MAX_EMBED_COUNT})
         *
         * @throws IllegalArgumentException
         * If the embed list is null, empty, or longer than {@value Message#MAX_EMBED_COUNT}
         *
         * @return New valid instance of MessageCreateData
         *
         * @see MessageCreateBuilder.setEmbeds
         */
        @Nonnull
        fun fromEmbeds(@Nonnull vararg embeds: MessageEmbed?): MessageCreateData? {
            return MessageCreateBuilder().setEmbeds(*embeds)!!.build()
        }

        /**
         * Shortcut for `new MessageCreateBuilder().setFiles(embeds).build()`.
         *
         * @param  files
         * The file uploads
         *
         * @throws IllegalArgumentException
         * If the null is provided or the list is empty
         *
         * @return New valid instance of MessageCreateData
         *
         * @see MessageCreateBuilder.setFiles
         */
        @Nonnull
        fun fromFiles(@Nonnull files: Collection<FileUpload?>?): MessageCreateData? {
            return MessageCreateBuilder().setFiles(files).build()
        }

        /**
         * Shortcut for `new MessageCreateBuilder().setFiles(embeds).build()`.
         *
         * @param  files
         * The file uploads
         *
         * @throws IllegalArgumentException
         * If the null is provided or the list is empty
         *
         * @return New valid instance of MessageCreateData
         *
         * @see MessageCreateBuilder.setFiles
         */
        @Nonnull
        fun fromFiles(@Nonnull vararg files: FileUpload?): MessageCreateData? {
            return MessageCreateBuilder().setFiles(*files).build()
        }

        /**
         * Shortcut for `new MessageCreateBuilder().applyMessage(message).build()`.
         *
         * @param  message
         * The message to apply
         *
         * @throws IllegalArgumentException
         * If the message is null or a system message
         *
         * @return New valid instance of MessageCreateData
         *
         * @see MessageCreateBuilder.applyMessage
         */
        @Nonnull
        fun fromMessage(@Nonnull message: Message?): MessageCreateData? {
            return MessageCreateBuilder().applyMessage((message)!!).build()
        }

        /**
         * Shortcut for `new MessageCreateBuilder().applyEditData(data).build()`.
         *
         * @param  data
         * The message edit data to apply
         *
         * @throws IllegalArgumentException
         * If the data is null or empty
         *
         * @return New valid instance of MessageCreateData
         *
         * @see MessageCreateBuilder.applyEditData
         */
        @Nonnull
        fun fromEditData(@Nonnull data: MessageEditData?): MessageCreateData? {
            return MessageCreateBuilder().applyEditData((data)!!).build()
        }
    }
}
