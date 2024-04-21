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

import net.dv8tion.jda.api.entities.IMentionable
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.Message.MentionType
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.interactions.components.LayoutComponent
import net.dv8tion.jda.internal.utils.Checks
import java.util.*
import javax.annotation.Nonnull

/**
 * Abstract builder implementation of [MessageRequest].
 *
 *
 * This builder cannot be instantiated directly. You should use [MessageCreateBuilder] or [MessageEditBuilder] instead.
 *
 * @param <T>
 * The result type used for [.build]
 * @param <R>
 * The return type used for method chaining
 *
 * @see MessageCreateBuilder
 *
 * @see MessageEditBuilder
</R></T> */
abstract class AbstractMessageBuilder<T, R : AbstractMessageBuilder<T, R>?> protected constructor() :
    MessageRequest<R> {
    protected override val embeds: MutableList<MessageEmbed?> = ArrayList(Message.MAX_EMBED_COUNT)
    protected override val components: MutableList<LayoutComponent?> = ArrayList(Message.MAX_COMPONENT_COUNT)
    protected override val content = StringBuilder(Message.MAX_CONTENT_LENGTH)
    protected var mentions = AllowedMentionsData()
    protected var messageFlags = 0
    @Nonnull
    override fun setContent(content: String?): R {
        var content = content
        if (content != null) {
            content = content.trim { it <= ' ' }
            Checks.notLonger(content, Message.MAX_CONTENT_LENGTH, "Content")
            this.content.setLength(0)
            this.content.append(content)
        } else {
            this.content.setLength(0)
        }
        return this as R
    }

    @Nonnull
    override fun getContent(): String {
        return content.toString()
    }

    @Nonnull
    override fun mentionRepliedUser(mention: Boolean): R {
        mentions.mentionRepliedUser(mention)
        return this as R
    }

    @Nonnull
    override fun setAllowedMentions(allowedMentions: Collection<MentionType?>?): R {
        mentions.setAllowedMentions(allowedMentions)
        return this as R
    }

    @Nonnull
    override fun mention(@Nonnull mentions: Collection<IMentionable?>): R {
        this.mentions.mention(mentions)
        return this as R
    }

    @Nonnull
    override fun mentionUsers(@Nonnull userIds: Collection<String?>?): R {
        mentions.mentionUsers(userIds)
        return this as R
    }

    @Nonnull
    override fun mentionRoles(@Nonnull roleIds: Collection<String?>?): R {
        mentions.mentionRoles(roleIds)
        return this as R
    }

    @get:Nonnull
    override val mentionedUsers: Set<String?>?
        get() = mentions.mentionedUsers

    @get:Nonnull
    override val mentionedRoles: Set<String?>?
        get() = mentions.mentionedRoles

    @get:Nonnull
    override val allowedMentions: EnumSet<MentionType?>?
        get() = mentions.allowedMentions
    override val isMentionRepliedUser: Boolean
        get() = mentions.isMentionRepliedUser

    @Nonnull
    override fun setEmbeds(@Nonnull embeds: Collection<MessageEmbed?>?): R {
        Checks.noneNull(embeds, "Embeds")
        Checks.check(
            embeds!!.size <= Message.MAX_EMBED_COUNT,
            "Cannot send more than %d embeds in a message!",
            Message.MAX_EMBED_COUNT
        )
        this.embeds.clear()
        this.embeds.addAll(embeds)
        return this as R
    }

    @Nonnull
    override fun getEmbeds(): List<MessageEmbed?> {
        return Collections.unmodifiableList(embeds)
    }

    @Nonnull
    override fun setComponents(@Nonnull components: Collection<LayoutComponent?>): R {
        Checks.noneNull(components, "ComponentLayouts")
        for (layout in components) Checks.check(
            layout!!.isMessageCompatible,
            "Provided component layout is invalid for messages!"
        )
        Checks.check(
            components.size <= Message.MAX_COMPONENT_COUNT,
            "Cannot send more than %d component layouts in a message!",
            Message.MAX_COMPONENT_COUNT
        )
        this.components.clear()
        this.components.addAll(components)
        return this as R
    }

    @Nonnull
    override fun getComponents(): List<LayoutComponent?> {
        return Collections.unmodifiableList(components)
    }

    @Nonnull
    override fun setSuppressEmbeds(suppress: Boolean): R {
        val flag = Message.MessageFlag.EMBEDS_SUPPRESSED.value
        if (suppress) messageFlags = messageFlags or flag else messageFlags = messageFlags and flag.inv()
        return this as R
    }

    override val isSuppressEmbeds: Boolean
        get() = messageFlags and Message.MessageFlag.EMBEDS_SUPPRESSED.value != 0

    /**
     * Whether this builder is considered empty, this checks for all *required* fields of the request type.
     * <br></br>On a create request, this checks for [content][.setContent], [embeds][.setEmbeds], [components][.setComponents], and [files][.setFiles].
     * <br></br>An edit request is only considered empty if no setters were called. And never empty, if the builder is a [replace request][MessageEditRequest.setReplace].
     *
     * @return True, if the builder state is empty
     */
    abstract val isEmpty: Boolean

    /**
     * Whether this builder has a valid state to build.
     * <br></br>If this is `false`, then [.build] throws an [IllegalStateException].
     * You can check the exception docs on [.build] for specifics.
     *
     * @return True, if the builder is in a valid state
     */
    abstract val isValid: Boolean

    /**
     * Builds a validated instance of this builder's state, which can then be used for requests.
     *
     * @throws IllegalStateException
     * For [MessageCreateBuilder]
     *
     *  * If the builder is [empty][.isEmpty]
     *  * If the content set is longer than {@value Message#MAX_CONTENT_LENGTH}
     *  * If more than {@value Message#MAX_EMBED_COUNT} embeds are set
     *  * If more than {@value Message#MAX_COMPONENT_COUNT} component layouts are set
     *
     * For [MessageEditBuilder]
     *
     *  * If the content set is longer than {@value Message#MAX_CONTENT_LENGTH}
     *  * If more than {@value Message#MAX_EMBED_COUNT} embeds are set
     *  * If more than {@value Message#MAX_COMPONENT_COUNT} component layouts are set
     *
     *
     * @return The validated data instance
     */
    @Nonnull
    abstract fun build(): T

    /**
     * Clears this builder's state, resetting it to the initial state identical to creating a new instance.
     *
     *
     * **WARNING:** This will remove all the files added to the builder, but will not close them.
     * You can use [.closeFiles] *before* calling `clear()` to close the files explicitly.
     *
     * @return The same builder instance for chaining
     */
    @Nonnull
    open fun clear(): R {
        embeds.clear()
        components.clear()
        content.setLength(0)
        mentions.clear()
        messageFlags = 0
        return this as R
    }

    /**
     * Closes and removes all [FileUploads][net.dv8tion.jda.api.utils.FileUpload] added to this builder.
     *
     *
     * This will keep any [AttachmentUpdates][net.dv8tion.jda.api.utils.AttachmentUpdate] added to this builder, as those do not require closing.
     * You can use [MessageEditRequest.setAttachments] to remove them as well.
     *
     * @return The same builder instance for chaining
     */
    @Nonnull
    abstract fun closeFiles(): R
}
