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
package net.dv8tion.jda.api.interactions.callbacks

import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.LayoutComponent
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction
import net.dv8tion.jda.api.utils.FileUpload
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.dv8tion.jda.internal.requests.restaction.interactions.ReplyCallbackActionImpl
import net.dv8tion.jda.internal.utils.Checks
import java.util.*
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Interactions which allow message replies in the channel they were used in.
 *
 *
 * These replies automatically acknowledge the interaction and support deferring.
 *
 *
 * **Deferred Replies**<br></br>
 *
 * If an interaction reply is deferred using [.deferReply] or [.deferReply],
 * the [interaction hook][.getHook] can be used to send a delayed/deferred reply with [InteractionHook.sendMessage].
 * When using [.deferReply] the first message sent to the [InteractionHook] will be identical to using [InteractionHook.editOriginal].
 * You must decide whether your reply will be ephemeral or not before calling [.deferReply]. So design your code flow with that in mind!
 *
 *
 * If a reply is [deferred][.deferReply], it becomes the **original** message of the interaction hook.
 * This means all the methods with `original` in the name, such as [InteractionHook.editOriginal],
 * will affect that original reply.
 */
interface IReplyCallback : IDeferrableCallback {
    /**
     * Acknowledge this interaction and defer the reply to a later time.
     * <br></br>This will send a `<Bot> is thinking...` message in chat that will be updated later through either [InteractionHook.editOriginal] or [InteractionHook.sendMessage].
     *
     *
     * You can use [deferReply(true)][.deferReply] to send a deferred ephemeral reply. If your initial deferred message is not ephemeral it cannot be made ephemeral later.
     * Your first message to the [InteractionHook] will inherit whether the message is ephemeral or not from this deferred reply.
     *
     *
     * **You only have 3 seconds to acknowledge an interaction!**
     * <br></br>When the acknowledgement is sent after the interaction expired, you will receive [ErrorResponse.UNKNOWN_INTERACTION][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_INTERACTION].
     *
     * Use [.reply] to reply directly.
     *
     * @return [ReplyCallbackAction]
     */
    @Nonnull
    @CheckReturnValue
    fun deferReply(): ReplyCallbackAction

    /**
     * Acknowledge this interaction and defer the reply to a later time.
     * <br></br>This will send a `<Bot> is thinking...` message in chat that will be updated later through either [InteractionHook.editOriginal] or [InteractionHook.sendMessage].
     *
     *
     * You can use `deferReply()` or `deferReply(false)` to send a non-ephemeral deferred reply. If your initial deferred message is ephemeral it cannot be made non-ephemeral later.
     * Your first message to the [InteractionHook] will inherit whether the message is ephemeral or not from this deferred reply.
     *
     *
     * **You only have 3 seconds to acknowledge an interaction!**
     * <br></br>When the acknowledgement is sent after the interaction expired, you will receive [ErrorResponse.UNKNOWN_INTERACTION][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_INTERACTION].
     *
     * Use [.reply] to reply directly.
     *
     *
     * Ephemeral messages have some limitations and will be removed once the user restarts their client.
     * <br></br>When a message is ephemeral, it will only be visible to the user that used the interaction.
     * <br></br>Limitations:
     *
     *  * Cannot contain any files/attachments
     *  * Cannot be reacted to
     *  * Cannot be retrieved
     *
     *
     * @param  ephemeral
     * True, if this message should only be visible to the interaction user
     *
     * @return [ReplyCallbackAction]
     */
    @Nonnull
    @CheckReturnValue
    fun deferReply(ephemeral: Boolean): ReplyCallbackAction? {
        return deferReply().setEphemeral(ephemeral)
    }

    /**
     * Reply to this interaction and acknowledge it.
     * <br></br>This will send a reply message for this interaction.
     * You can use [setEphemeral(true)][ReplyCallbackAction.setEphemeral] to only let the target user see the message.
     * Replies are non-ephemeral by default.
     *
     *
     * **You only have 3 seconds to acknowledge an interaction!**
     * <br></br>When the acknowledgement is sent after the interaction expired, you will receive [ErrorResponse.UNKNOWN_INTERACTION][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_INTERACTION].
     *
     * If your handling can take longer than 3 seconds, due to various rate limits or other conditions, you should use [.deferReply] instead.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
     *
     *  * [UNKNOWN_INTERACTION][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_INTERACTION]
     * <br></br>If the interaction has already been acknowledged or timed out
     *
     *  * [MESSAGE_BLOCKED_BY_AUTOMOD][net.dv8tion.jda.api.requests.ErrorResponse.MESSAGE_BLOCKED_BY_AUTOMOD]
     * <br></br>If this message was blocked by an [AutoModRule][net.dv8tion.jda.api.entities.automod.AutoModRule]
     *
     *  * [MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER][net.dv8tion.jda.api.requests.ErrorResponse.MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER]
     * <br></br>If this message was blocked by the harmful link filter
     *
     *
     * @param  message
     * The [MessageCreateData] to send
     *
     * @throws IllegalArgumentException
     * If null is provided
     *
     * @return [ReplyCallbackAction]
     *
     * @see net.dv8tion.jda.api.utils.messages.MessageCreateBuilder MessageCreateBuilder
     */
    @Nonnull
    @CheckReturnValue
    fun reply(@Nonnull message: MessageCreateData?): ReplyCallbackAction? {
        Checks.notNull(message, "Message")
        val action = deferReply() as ReplyCallbackActionImpl
        return action.applyData(message!!)
    }

    /**
     * Reply to this interaction and acknowledge it.
     * <br></br>This will send a reply message for this interaction.
     * You can use [setEphemeral(true)][ReplyCallbackAction.setEphemeral] to only let the target user see the message.
     * Replies are non-ephemeral by default.
     *
     *
     * **You only have 3 seconds to acknowledge an interaction!**
     * <br></br>When the acknowledgement is sent after the interaction expired, you will receive [ErrorResponse.UNKNOWN_INTERACTION][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_INTERACTION].
     *
     * If your handling can take longer than 3 seconds, due to various rate limits or other conditions, you should use [.deferReply] instead.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
     *
     *  * [UNKNOWN_INTERACTION][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_INTERACTION]
     * <br></br>If the interaction has already been acknowledged or timed out
     *
     *  * [MESSAGE_BLOCKED_BY_AUTOMOD][net.dv8tion.jda.api.requests.ErrorResponse.MESSAGE_BLOCKED_BY_AUTOMOD]
     * <br></br>If this message was blocked by an [AutoModRule][net.dv8tion.jda.api.entities.automod.AutoModRule]
     *
     *  * [MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER][net.dv8tion.jda.api.requests.ErrorResponse.MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER]
     * <br></br>If this message was blocked by the harmful link filter
     *
     *
     * @param  content
     * The message content to send
     *
     * @throws IllegalArgumentException
     * If null is provided or the content is longer than [Message.MAX_CONTENT_LENGTH] characters
     *
     * @return [ReplyCallbackAction]
     */
    @Nonnull
    @CheckReturnValue
    fun reply(@Nonnull content: String?): ReplyCallbackAction? {
        Checks.notNull(content, "Content")
        return deferReply().setContent(content)
    }

    /**
     * Reply to this interaction and acknowledge it.
     * <br></br>This will send a reply message for this interaction.
     * You can use [setEphemeral(true)][ReplyCallbackAction.setEphemeral] to only let the target user see the message.
     * Replies are non-ephemeral by default.
     *
     *
     * **You only have 3 seconds to acknowledge an interaction!**
     * <br></br>When the acknowledgement is sent after the interaction expired, you will receive [ErrorResponse.UNKNOWN_INTERACTION][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_INTERACTION].
     *
     * If your handling can take longer than 3 seconds, due to various rate limits or other conditions, you should use [.deferReply] instead.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
     *
     *  * [UNKNOWN_INTERACTION][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_INTERACTION]
     * <br></br>If the interaction has already been acknowledged or timed out
     *
     *  * [MESSAGE_BLOCKED_BY_AUTOMOD][net.dv8tion.jda.api.requests.ErrorResponse.MESSAGE_BLOCKED_BY_AUTOMOD]
     * <br></br>If this message was blocked by an [AutoModRule][net.dv8tion.jda.api.entities.automod.AutoModRule]
     *
     *  * [MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER][net.dv8tion.jda.api.requests.ErrorResponse.MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER]
     * <br></br>If this message was blocked by the harmful link filter
     *
     *
     * @param  embeds
     * The [MessageEmbeds][MessageEmbed] to send
     *
     * @throws IllegalArgumentException
     * If null is provided
     *
     * @return [ReplyCallbackAction]
     */
    @Nonnull
    @CheckReturnValue
    fun replyEmbeds(@Nonnull embeds: Collection<MessageEmbed?>?): ReplyCallbackAction? {
        return deferReply().addEmbeds(embeds!!)
    }

    /**
     * Reply to this interaction and acknowledge it.
     * <br></br>This will send a reply message for this interaction.
     * You can use [setEphemeral(true)][ReplyCallbackAction.setEphemeral] to only let the target user see the message.
     * Replies are non-ephemeral by default.
     *
     *
     * **You only have 3 seconds to acknowledge an interaction!**
     * <br></br>When the acknowledgement is sent after the interaction expired, you will receive [ErrorResponse.UNKNOWN_INTERACTION][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_INTERACTION].
     *
     * If your handling can take longer than 3 seconds, due to various rate limits or other conditions, you should use [.deferReply] instead.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
     *
     *  * [UNKNOWN_INTERACTION][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_INTERACTION]
     * <br></br>If the interaction has already been acknowledged or timed out
     *
     *  * [MESSAGE_BLOCKED_BY_AUTOMOD][net.dv8tion.jda.api.requests.ErrorResponse.MESSAGE_BLOCKED_BY_AUTOMOD]
     * <br></br>If this message was blocked by an [AutoModRule][net.dv8tion.jda.api.entities.automod.AutoModRule]
     *
     *  * [MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER][net.dv8tion.jda.api.requests.ErrorResponse.MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER]
     * <br></br>If this message was blocked by the harmful link filter
     *
     *
     * @param  embed
     * The message embed to send
     * @param  embeds
     * Any additional embeds to send
     *
     * @throws IllegalArgumentException
     * If null is provided
     *
     * @return [ReplyCallbackAction]
     */
    @Nonnull
    @CheckReturnValue
    fun replyEmbeds(@Nonnull embed: MessageEmbed?, @Nonnull vararg embeds: MessageEmbed?): ReplyCallbackAction? {
        Checks.notNull(embed, "MessageEmbed")
        Checks.noneNull(embeds, "MessageEmbed")
        return deferReply().addEmbeds(embed).addEmbeds(*embeds)
    }

    /**
     * Reply to this interaction and acknowledge it.
     * <br></br>This will send a reply message for this interaction.
     * You can use [setEphemeral(true)][ReplyCallbackAction.setEphemeral] to only let the target user see the message.
     * Replies are non-ephemeral by default.
     *
     *
     * **You only have 3 seconds to acknowledge an interaction!**
     * <br></br>When the acknowledgement is sent after the interaction expired, you will receive [ErrorResponse.UNKNOWN_INTERACTION][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_INTERACTION].
     *
     * If your handling can take longer than 3 seconds, due to various rate limits or other conditions, you should use [.deferReply] instead.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
     *
     *  * [UNKNOWN_INTERACTION][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_INTERACTION]
     * <br></br>If the interaction has already been acknowledged or timed out
     *
     *  * [MESSAGE_BLOCKED_BY_AUTOMOD][net.dv8tion.jda.api.requests.ErrorResponse.MESSAGE_BLOCKED_BY_AUTOMOD]
     * <br></br>If this message was blocked by an [AutoModRule][net.dv8tion.jda.api.entities.automod.AutoModRule]
     *
     *  * [MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER][net.dv8tion.jda.api.requests.ErrorResponse.MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER]
     * <br></br>If this message was blocked by the harmful link filter
     *
     *
     * @param  components
     * The [LayoutComponents][LayoutComponent] to send, such as [ActionRow]
     *
     * @throws IllegalArgumentException
     * If null is provided or more than {@value Message#MAX_COMPONENT_COUNT} component layouts are provided
     *
     * @return [ReplyCallbackAction]
     */
    @Nonnull
    @CheckReturnValue
    fun replyComponents(@Nonnull components: Collection<LayoutComponent>?): ReplyCallbackAction? {
        return deferReply().setComponents(components!!)
    }

    /**
     * Reply to this interaction and acknowledge it.
     * <br></br>This will send a reply message for this interaction.
     * You can use [setEphemeral(true)][ReplyCallbackAction.setEphemeral] to only let the target user see the message.
     * Replies are non-ephemeral by default.
     *
     *
     * **You only have 3 seconds to acknowledge an interaction!**
     * <br></br>When the acknowledgement is sent after the interaction expired, you will receive [ErrorResponse.UNKNOWN_INTERACTION][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_INTERACTION].
     *
     * If your handling can take longer than 3 seconds, due to various rate limits or other conditions, you should use [.deferReply] instead.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
     *
     *  * [UNKNOWN_INTERACTION][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_INTERACTION]
     * <br></br>If the interaction has already been acknowledged or timed out
     *
     *  * [MESSAGE_BLOCKED_BY_AUTOMOD][net.dv8tion.jda.api.requests.ErrorResponse.MESSAGE_BLOCKED_BY_AUTOMOD]
     * <br></br>If this message was blocked by an [AutoModRule][net.dv8tion.jda.api.entities.automod.AutoModRule]
     *
     *  * [MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER][net.dv8tion.jda.api.requests.ErrorResponse.MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER]
     * <br></br>If this message was blocked by the harmful link filter
     *
     *
     * @param  component
     * The [LayoutComponent] to send
     * @param  other
     * Any addition [LayoutComponents][LayoutComponent] to send
     *
     * @throws IllegalArgumentException
     * If null is provided or more than {@value Message#MAX_COMPONENT_COUNT} component layouts are provided
     *
     * @return [ReplyCallbackAction]
     */
    @Nonnull
    @CheckReturnValue
    fun replyComponents(
        @Nonnull component: LayoutComponent,
        @Nonnull vararg other: LayoutComponent?
    ): ReplyCallbackAction? {
        Checks.notNull(component, "LayoutComponents")
        Checks.noneNull(other, "LayoutComponents")
        val layouts: MutableList<LayoutComponent> = ArrayList(1 + other.size)
        layouts.add(component)
        Collections.addAll(layouts, *other)
        return replyComponents(layouts)
    }

    /**
     * Reply to this interaction and acknowledge it.
     * <br></br>This will send a reply message for this interaction.
     * You can use [setEphemeral(true)][ReplyCallbackAction.setEphemeral] to only let the target user see the message.
     * Replies are non-ephemeral by default.
     *
     *
     * **You only have 3 seconds to acknowledge an interaction!**
     * <br></br>When the acknowledgement is sent after the interaction expired, you will receive [ErrorResponse.UNKNOWN_INTERACTION][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_INTERACTION].
     *
     * If your handling can take longer than 3 seconds, due to various rate limits or other conditions, you should use [.deferReply] instead.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
     *
     *  * [UNKNOWN_INTERACTION][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_INTERACTION]
     * <br></br>If the interaction has already been acknowledged or timed out
     *
     *  * [MESSAGE_BLOCKED_BY_AUTOMOD][net.dv8tion.jda.api.requests.ErrorResponse.MESSAGE_BLOCKED_BY_AUTOMOD]
     * <br></br>If this message was blocked by an [AutoModRule][net.dv8tion.jda.api.entities.automod.AutoModRule]
     *
     *  * [MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER][net.dv8tion.jda.api.requests.ErrorResponse.MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER]
     * <br></br>If this message was blocked by the harmful link filter
     *
     *
     * @param  format
     * Format string for the message content
     * @param  args
     * Format arguments for the content
     *
     * @throws IllegalArgumentException
     * If the format string is null or the resulting content is longer than [Message.MAX_CONTENT_LENGTH] characters
     *
     * @return [ReplyCallbackAction]
     */
    @Nonnull
    @CheckReturnValue
    fun replyFormat(@Nonnull format: String?, @Nonnull vararg args: Any?): ReplyCallbackAction? {
        Checks.notNull(format, "Format String")
        return reply(String.format(format!!, *args))
    }

    /**
     * Reply to this interaction and acknowledge it.
     * <br></br>This will send a reply message for this interaction.
     * You can use [setEphemeral(true)][ReplyCallbackAction.setEphemeral] to only let the target user see the message.
     * Replies are non-ephemeral by default.
     *
     *
     * **You only have 3 seconds to acknowledge an interaction!**
     * <br></br>When the acknowledgement is sent after the interaction expired, you will receive [ErrorResponse.UNKNOWN_INTERACTION][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_INTERACTION].
     *
     * If your handling can take longer than 3 seconds, due to various rate limits or other conditions, you should use [.deferReply] instead.
     *
     *
     * **Resource Handling Note:** Once the request is handed off to the requester, for example when you call [RestAction.queue],
     * the requester will automatically clean up all opened files by itself. You are only responsible to close them yourself if it is never handed off properly.
     * For instance, if an exception occurs after using [FileUpload.fromData], before calling [RestAction.queue].
     * You can safely use a try-with-resources to handle this, since [FileUpload.close] becomes ineffective once the request is handed off.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
     *
     *  * [UNKNOWN_INTERACTION][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_INTERACTION]
     * <br></br>If the interaction has already been acknowledged or timed out
     *
     *  * [MESSAGE_BLOCKED_BY_AUTOMOD][net.dv8tion.jda.api.requests.ErrorResponse.MESSAGE_BLOCKED_BY_AUTOMOD]
     * <br></br>If this message was blocked by an [AutoModRule][net.dv8tion.jda.api.entities.automod.AutoModRule]
     *
     *  * [MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER][net.dv8tion.jda.api.requests.ErrorResponse.MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER]
     * <br></br>If this message was blocked by the harmful link filter
     *
     *  * [REQUEST_ENTITY_TOO_LARGE][net.dv8tion.jda.api.requests.ErrorResponse.REQUEST_ENTITY_TOO_LARGE]
     * <br></br>If the total sum of uploaded bytes exceeds the guild's [upload limit][Guild.getMaxFileSize]
     *
     *
     * @param  files
     * The [FileUploads][FileUpload] to attach to the message
     *
     * @throws IllegalArgumentException
     * If null is provided
     *
     * @return [ReplyCallbackAction]
     *
     * @see FileUpload.fromData
     */
    @Nonnull
    @CheckReturnValue
    fun replyFiles(@Nonnull files: Collection<FileUpload?>?): ReplyCallbackAction? {
        Checks.notEmpty(files, "File Collection")
        return deferReply().setFiles(files)
    }

    /**
     * Reply to this interaction and acknowledge it.
     * <br></br>This will send a reply message for this interaction.
     * You can use [setEphemeral(true)][ReplyCallbackAction.setEphemeral] to only let the target user see the message.
     * Replies are non-ephemeral by default.
     *
     *
     * **You only have 3 seconds to acknowledge an interaction!**
     * <br></br>When the acknowledgement is sent after the interaction expired, you will receive [ErrorResponse.UNKNOWN_INTERACTION][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_INTERACTION].
     *
     * If your handling can take longer than 3 seconds, due to various rate limits or other conditions, you should use [.deferReply] instead.
     *
     *
     * **Resource Handling Note:** Once the request is handed off to the requester, for example when you call [RestAction.queue],
     * the requester will automatically clean up all opened files by itself. You are only responsible to close them yourself if it is never handed off properly.
     * For instance, if an exception occurs after using [FileUpload.fromData], before calling [RestAction.queue].
     * You can safely use a try-with-resources to handle this, since [FileUpload.close] becomes ineffective once the request is handed off.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
     *
     *  * [UNKNOWN_INTERACTION][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_INTERACTION]
     * <br></br>If the interaction has already been acknowledged or timed out
     *
     *  * [MESSAGE_BLOCKED_BY_AUTOMOD][net.dv8tion.jda.api.requests.ErrorResponse.MESSAGE_BLOCKED_BY_AUTOMOD]
     * <br></br>If this message was blocked by an [AutoModRule][net.dv8tion.jda.api.entities.automod.AutoModRule]
     *
     *  * [MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER][net.dv8tion.jda.api.requests.ErrorResponse.MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER]
     * <br></br>If this message was blocked by the harmful link filter
     *
     *  * [REQUEST_ENTITY_TOO_LARGE][net.dv8tion.jda.api.requests.ErrorResponse.REQUEST_ENTITY_TOO_LARGE]
     * <br></br>If the total sum of uploaded bytes exceeds the guild's [upload limit][Guild.getMaxFileSize]
     *
     *
     * @param  files
     * The [FileUploads][FileUpload] to attach to the message
     *
     * @throws IllegalArgumentException
     * If null is provided
     *
     * @return [ReplyCallbackAction]
     *
     * @see FileUpload.fromData
     */
    @Nonnull
    @CheckReturnValue
    fun replyFiles(@Nonnull vararg files: FileUpload?): ReplyCallbackAction? {
        Checks.notEmpty(files, "File Collection")
        Checks.noneNull(files, "FileUpload")
        return deferReply().setFiles(*files)
    }
}
