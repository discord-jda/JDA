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
import net.dv8tion.jda.internal.utils.Checks
import net.dv8tion.jda.internal.utils.Helpers
import net.dv8tion.jda.internal.utils.IOUtil
import java.util.*
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate
import java.util.stream.Collectors
import javax.annotation.Nonnull

/**
 * Builder specialized for building a [MessageEditData].
 *
 *
 * These are used to edit messages and allow configuration that either [replaces][.setReplace] the message or only updates specific fields.
 *
 * @see MessageCreateBuilder
 */
class MessageEditBuilder() : AbstractMessageBuilder<MessageEditData, MessageEditBuilder?>(),
    MessageEditRequest<MessageEditBuilder> {
    override var isReplace: Boolean = false
        private set
    private var configuredFields: Int = 0
    private override val attachments: MutableList<AttachedFile?> = ArrayList(10)
    @Nonnull
    public override fun mentionRepliedUser(mention: Boolean): MessageEditBuilder? {
        super.mentionRepliedUser(mention)
        configuredFields = configuredFields or MENTIONS
        return this
    }

    @Nonnull
    public override fun setAllowedMentions(allowedMentions: Collection<MentionType?>?): MessageEditBuilder? {
        super.setAllowedMentions(allowedMentions)
        configuredFields = configuredFields or MENTIONS
        return this
    }

    @Nonnull
    public override fun mention(@Nonnull mentions: Collection<IMentionable?>): MessageEditBuilder? {
        super.mention(mentions)
        configuredFields = configuredFields or MENTIONS
        return this
    }

    @Nonnull
    public override fun mentionUsers(@Nonnull userIds: Collection<String?>?): MessageEditBuilder? {
        super.mentionUsers(userIds)
        configuredFields = configuredFields or MENTIONS
        return this
    }

    @Nonnull
    public override fun mentionRoles(@Nonnull roleIds: Collection<String?>?): MessageEditBuilder? {
        super.mentionRoles(roleIds)
        configuredFields = configuredFields or MENTIONS
        return this
    }

    @Nonnull
    public override fun setAttachments(attachments: Collection<AttachedFile?>?): MessageEditBuilder {
        this.attachments.clear()
        configuredFields = configuredFields or ATTACHMENTS
        if (attachments != null) this.attachments.addAll(attachments)
        return this
    }

    @Nonnull
    public override fun getAttachments(): List<AttachedFile?>? {
        return Collections.unmodifiableList(attachments)
    }

    @Nonnull
    public override fun setReplace(isReplace: Boolean): MessageEditBuilder {
        this.isReplace = isReplace
        return this
    }

    @Nonnull
    public override fun setContent(content: String?): MessageEditBuilder? {
        super.setContent(content)
        configuredFields = configuredFields or CONTENT
        return this
    }

    @Nonnull
    public override fun setEmbeds(@Nonnull embeds: Collection<MessageEmbed?>?): MessageEditBuilder? {
        super.setEmbeds(embeds)
        configuredFields = configuredFields or EMBEDS
        return this
    }

    @Nonnull
    public override fun setComponents(@Nonnull components: Collection<LayoutComponent?>): MessageEditBuilder? {
        super.setComponents(components)
        configuredFields = configuredFields or COMPONENTS
        return this
    }

    @Nonnull
    public override fun setSuppressEmbeds(suppress: Boolean): MessageEditBuilder? {
        super.setSuppressEmbeds(suppress)
        configuredFields = configuredFields or FLAGS
        return this
    }

    @Nonnull
    public override fun applyData(@Nonnull data: MessageEditData): MessageEditBuilder {
        Checks.notNull(data, "Data")
        configuredFields = configuredFields or data.getConfiguredFields()
        isReplace = isReplace or data.isReplace()
        if (data.isSet(CONTENT)) setContent(data.getContent())
        if (data.isSet(EMBEDS)) this.setEmbeds(data.getEmbeds())
        if (data.isSet(COMPONENTS)) {
            val layoutComponents: List<LayoutComponent?> = data.getComponents().stream()
                .map(Function({ obj: LayoutComponent? -> obj!!.createCopy() }))
                .collect(Collectors.toList())
            this.setComponents(layoutComponents)
        }
        if (data.isSet(ATTACHMENTS)) this.setAttachments(data.getAttachments())
        if (data.isSet(MENTIONS)) mentions = data.mentions!!.copy()
        if (data.isSet(FLAGS)) messageFlags = data.getFlags()
        return this
    }

    override val isEmpty: Boolean
        get() {
            return !isReplace && configuredFields == 0
        }
    override val isValid: Boolean
        get() {
            if (isSet(EMBEDS) && embeds.size > Message.MAX_EMBED_COUNT) return false
            if (isSet(COMPONENTS) && components.size > Message.MAX_COMPONENT_COUNT) return false
            return !isSet(CONTENT) || Helpers.codePointLength(content) <= Message.MAX_CONTENT_LENGTH
        }

    private fun isSet(flag: Int): Boolean {
        return isReplace || (configuredFields and flag) != 0
    }

    @Nonnull
    public override fun build(): MessageEditData {
        // Copy to prevent modifying data after building
        val content: String = content.toString().trim({ it <= ' ' })
        val embeds: List<MessageEmbed?> = ArrayList(embeds)
        val attachments: List<AttachedFile?> = ArrayList(attachments)
        val components: List<LayoutComponent?> = ArrayList(components)
        val mentions: AllowedMentionsData? = mentions.copy()
        val length: Int = if (isSet(CONTENT)) Helpers.codePointLength(content) else 0
        if (length > Message.MAX_CONTENT_LENGTH) throw IllegalStateException("Message content is too long! Max length is " + Message.MAX_CONTENT_LENGTH + " characters, provided " + length)
        if (isSet(EMBEDS) && embeds.size > Message.MAX_EMBED_COUNT) throw IllegalStateException("Cannot build message with over " + Message.MAX_EMBED_COUNT + " embeds, provided " + embeds.size)
        if (isSet(COMPONENTS) && components.size > Message.MAX_COMPONENT_COUNT) throw IllegalStateException("Cannot build message with over " + Message.MAX_COMPONENT_COUNT + " component layouts, provided " + components.size)
        return MessageEditData(
            configuredFields,
            messageFlags,
            isReplace,
            content,
            embeds,
            attachments,
            components,
            mentions
        )
    }

    @Nonnull
    public override fun clear(): MessageEditBuilder? {
        configuredFields = 0
        attachments.clear()
        return super.clear()
    }

    @Nonnull
    public override fun closeFiles(): MessageEditBuilder? {
        attachments.forEach(Consumer({ closeable: AttachedFile? -> IOUtil.silentClose(closeable) }))
        attachments.removeIf(Predicate({ obj: AttachedFile? -> FileUpload::class.java.isInstance(obj) }))
        return this
    }

    companion object {
        val CONTENT: Int = 1
        val EMBEDS: Int = 1 shl 1
        val COMPONENTS: Int = 1 shl 2
        val ATTACHMENTS: Int = 1 shl 3
        val MENTIONS: Int = 1 shl 4
        val FLAGS: Int = 1 shl 5

        /**
         * Factory method to start a builder from an existing instance of [MessageEditData].
         * <br></br>Equivalent to `new MessageEditBuilder().applyData(data)`.
         *
         * @param  data
         * The message edit data to apply
         *
         * @throws IllegalArgumentException
         * If null is provided
         *
         * @return A new MessageEditBuilder instance with the applied data
         *
         * @see .applyData
         */
        @Nonnull
        fun from(@Nonnull data: MessageEditData): MessageEditBuilder {
            return MessageEditBuilder().applyData(data)
        }

        /**
         * Factory method to start a builder from an existing instance of [MessageCreateData].
         * <br></br>Equivalent to `new MessageEditBuilder().applyCreateData(data)`.
         *
         *
         * This will set the request to be [replacing][.setReplace].
         *
         * @param  data
         * The message create data to apply
         *
         * @throws IllegalArgumentException
         * If null is provided
         *
         * @return A new MessageEditBuilder instance with the applied data
         *
         * @see .applyCreateData
         */
        @Nonnull
        fun fromCreateData(@Nonnull data: MessageCreateData?): MessageEditBuilder? {
            return MessageEditBuilder().applyCreateData(data)
        }

        /**
         * Factory method to start a builder from an existing instance of [Message].
         * <br></br>Equivalent to `new MessageEditBuilder().applyMessage(data)`.
         *
         *
         * This will set the request to be [replacing][.setReplace].
         *
         * @param  message
         * The message to apply
         *
         * @throws IllegalArgumentException
         * If null is provided or the message is a system message
         *
         * @return A new MessageEditBuilder instance with the applied data
         *
         * @see .applyMessage
         */
        @Nonnull
        fun fromMessage(@Nonnull message: Message?): MessageEditBuilder {
            return MessageEditBuilder().applyMessage((message)!!)
        }
    }
}
