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
import net.dv8tion.jda.internal.utils.Helpers
import net.dv8tion.jda.internal.utils.IOUtil
import java.util.*
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate
import javax.annotation.Nonnull

/**
 * Output of a [MessageEditRequest] and used for editing messages in channels/webhooks/interactions.
 *
 * @see MessageEditBuilder
 *
 * @see MessageChannel.editMessageById
 * @see net.dv8tion.jda.api.interactions.callbacks.IMessageEditCallback.editMessage
 * @see net.dv8tion.jda.api.entities.WebhookClient.editMessageById
 * @see net.dv8tion.jda.api.interactions.InteractionHook.editOriginal
 */
class MessageEditData(
    val configuredFields: Int, val flags: Int, val isReplace: Boolean,
    /**
     * The content of the message.
     *
     * @return The content or an empty string if none was set
     */
    @get:Nonnull override val content: String,
    embeds: List<MessageEmbed?>?, files: List<AttachedFile?>?, components: List<LayoutComponent?>?,
    val mentions: AllowedMentionsData?
) : MessageData, AutoCloseable, SerializableData {

    /**
     * The embeds of the message.
     *
     * @return The embeds or an empty list if none were set
     */
    @get:Nonnull
    override val embeds: List<MessageEmbed?>
    private val files: List<AttachedFile?>

    /**
     * The components of the message.
     *
     * @return The components or an empty list if none were set
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
        /**
         * The [AttachedFiles][AttachedFile] attached to this message.
         *
         * @return The list of attachments, or an empty list if none were set
         */
        get() {
            return files
        }
    override val isSuppressEmbeds: Boolean
        get() {
            return isSet(Message.MessageFlag.EMBEDS_SUPPRESSED.value)
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
    @Synchronized
    public override fun toData(): DataObject {
        val json: DataObject = DataObject.empty()
        if (isSet(MessageEditBuilder.Companion.CONTENT)) json.put("content", content)
        if (isSet(MessageEditBuilder.Companion.EMBEDS)) json.put("embeds", DataArray.fromCollection(embeds))
        if (isSet(MessageEditBuilder.Companion.COMPONENTS)) json.put("components", DataArray.fromCollection(components))
        if (isSet(MessageEditBuilder.Companion.MENTIONS)) json.put("allowed_mentions", mentions)
        if (isSet(MessageEditBuilder.Companion.FLAGS)) json.put("flags", flags)
        if (isSet(MessageEditBuilder.Companion.ATTACHMENTS)) {
            val attachments: DataArray = DataArray.empty()
            var fileUploadCount: Int = 0
            for (file: AttachedFile? in files) {
                attachments.add(file!!.toAttachmentData(fileUploadCount))
                if (file is FileUpload) fileUploadCount++
            }
            json.put("attachments", attachments)
        }
        return json
    }

    /**
     * The [FileUploads][FileUpload] attached to this message.
     *
     * @return The list of file uploads
     */
    @Nonnull
    @Synchronized
    fun getFiles(): List<FileUpload> {
        return files.stream()
            .filter(Predicate({ obj: AttachedFile? -> FileUpload::class.java.isInstance(obj) }))
            .map(Function({ obj: AttachedFile? -> FileUpload::class.java.cast(obj) }))
            .collect(Helpers.toUnmodifiableList())
    }

    @Synchronized
    public override fun close() {
        files.forEach(Consumer({ closeable: AttachedFile? -> IOUtil.silentClose(closeable) }))
    }

    fun isSet(flag: Int): Boolean {
        return isReplace || (configuredFields and flag) != 0
    }

    companion object {
        /**
         * Shortcut for `new MessageEditBuilder().setContent(content).build()`.
         *
         * @param  content
         * The message content (up to {@value Message#MAX_CONTENT_LENGTH})
         *
         * @throws IllegalArgumentException
         * If the content is null, empty, or longer than {@value Message#MAX_CONTENT_LENGTH}
         *
         * @return New valid instance of MessageEditData
         *
         * @see MessageEditBuilder.setContent
         */
        @Nonnull
        fun fromContent(@Nonnull content: String?): MessageEditData? {
            return MessageEditBuilder().setContent(content)!!.build()
        }

        /**
         * Shortcut for `new MessageEditBuilder().setEmbeds(embeds).build()`.
         *
         * @param  embeds
         * The message embeds (up to {@value Message#MAX_EMBED_COUNT})
         *
         * @throws IllegalArgumentException
         * If the embed list is null, empty, or longer than {@value Message#MAX_EMBED_COUNT}
         *
         * @return New valid instance of MessageEditData
         *
         * @see MessageEditBuilder.setEmbeds
         */
        @Nonnull
        fun fromEmbeds(@Nonnull embeds: Collection<MessageEmbed?>?): MessageEditData? {
            return MessageEditBuilder().setEmbeds(embeds)!!.build()
        }

        /**
         * Shortcut for `new MessageEditBuilder().setEmbeds(embeds).build()`.
         *
         * @param  embeds
         * The message embeds (up to {@value Message#MAX_EMBED_COUNT})
         *
         * @throws IllegalArgumentException
         * If the embed list is null, empty, or longer than {@value Message#MAX_EMBED_COUNT}
         *
         * @return New valid instance of MessageEditData
         *
         * @see MessageEditBuilder.setEmbeds
         */
        @Nonnull
        fun fromEmbeds(@Nonnull vararg embeds: MessageEmbed?): MessageEditData? {
            return MessageEditBuilder().setEmbeds(*embeds)!!.build()
        }

        /**
         * Shortcut for `new MessageEditBuilder().setFiles(embeds).build()`.
         *
         * @param  files
         * The file uploads
         *
         * @throws IllegalArgumentException
         * If the null is provided or the list is empty
         *
         * @return New valid instance of MessageEditData
         *
         * @see MessageEditBuilder.setFiles
         */
        @Nonnull
        fun fromFiles(@Nonnull files: Collection<FileUpload?>?): MessageEditData? {
            return MessageEditBuilder().setFiles(files).build()
        }

        /**
         * Shortcut for `new MessageEditBuilder().setFiles(embeds).build()`.
         *
         * @param  files
         * The file uploads
         *
         * @throws IllegalArgumentException
         * If the null is provided or the list is empty
         *
         * @return New valid instance of MessageEditData
         *
         * @see MessageEditBuilder.setFiles
         */
        @Nonnull
        fun fromFiles(@Nonnull vararg files: FileUpload?): MessageEditData? {
            return MessageEditBuilder().setFiles(*files).build()
        }

        /**
         * Shortcut for `new MessageEditBuilder().applyMessage(message).build()`.
         *
         * @param  message
         * The message to apply
         *
         * @throws IllegalArgumentException
         * If the message is null or a system message
         *
         * @return New valid instance of MessageEditData
         *
         * @see MessageEditBuilder.applyMessage
         */
        @Nonnull
        fun fromMessage(@Nonnull message: Message?): MessageEditData? {
            return MessageEditBuilder().applyMessage((message)!!).build()
        }

        /**
         * Shortcut for `new MessageCreateBuilder().applyCreateData(data).build()`.
         *
         * @param  data
         * The message create data to apply
         *
         * @throws IllegalArgumentException
         * If the data is null or empty
         *
         * @return New valid instance of MessageEditData
         *
         * @see MessageEditBuilder.applyCreateData
         */
        @Nonnull
        fun fromCreateData(@Nonnull data: MessageCreateData?): MessageEditData? {
            return MessageEditBuilder().applyCreateData(data).build()
        }
    }
}
