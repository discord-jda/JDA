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
package net.dv8tion.jda.api.interactions

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.WebhookClient
import net.dv8tion.jda.api.interactions.callbacks.IMessageEditCallback
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.LayoutComponent
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction
import net.dv8tion.jda.api.utils.AttachedFile
import net.dv8tion.jda.api.utils.messages.MessageEditData
import net.dv8tion.jda.internal.interactions.InteractionHookImpl
import net.dv8tion.jda.internal.utils.Checks
import java.util.*
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Webhook API for an interaction. Valid for up to 15 minutes after the interaction.
 * <br></br>This can be used to send followup messages or edit the original message of an interaction.
 *
 *
 * The interaction has to be acknowledged before any of these actions can be performed.
 * First, you need to call one of:
 *
 *  * [deferReply(...)][IReplyCallback.deferReply]
 *  * [reply(...)][IReplyCallback.reply]
 *  * [deferEdit()][IMessageEditCallback.deferEdit]
 *  * [editMessage(...)][IMessageEditCallback.editMessage]
 *
 *
 *
 * When [IReplyCallback.deferReply] is used, the first message will act identically to [editOriginal(...)][.editOriginal].
 * This means that you cannot make your deferred reply ephemeral through this interaction hook.
 * You need to specify whether your reply is ephemeral or not directly in [deferReply(boolean)][IReplyCallback.deferReply].
 *
 * @see IReplyCallback
 *
 * @see IMessageEditCallback
 *
 * @see .editOriginal
 * @see .deleteOriginal
 * @see .sendMessage
 */
interface InteractionHook : WebhookClient<Message?> {
    @get:Nonnull
    val interaction: Interaction?

    /**
     * The unix millisecond timestamp for the expiration of this interaction hook.
     * <br></br>An interaction hook expires after 15 minutes of its creation.
     *
     * @return The timestamp in millisecond precision
     *
     * @see System.currentTimeMillis
     * @see .isExpired
     */
    val expirationTimestamp: Long
    val isExpired: Boolean
        /**
         * Whether this interaction has expired.
         * <br></br>An interaction hook is only valid for 15 minutes.
         *
         * @return True, if this interaction hook has expired
         *
         * @see .getExpirationTimestamp
         */
        get() = System.currentTimeMillis() > expirationTimestamp

    /**
     * Whether messages sent from this interaction hook should be ephemeral by default.
     * <br></br>This does not affect message updates, including deferred replies sent with [sendMessage(...)][.sendMessage] methods.
     * <br></br>When a message is ephemeral, it will only be visible to the user that used the interaction.
     *
     *
     * Ephemeral messages have some limitations and will be removed once the user restarts their client.
     * <br></br>Limitations:
     *
     *  * Cannot contain any files/attachments
     *  * Cannot be reacted to
     *  * Cannot be retrieved
     *
     *
     * @param  ephemeral
     * True if messages should be ephemeral
     *
     * @return The same interaction hook instance
     */
    @Nonnull
    fun setEphemeral(ephemeral: Boolean): InteractionHook?

    @get:Nonnull
    abstract override val jDA: JDA?

    /**
     * Retrieves the original reply to this interaction.
     * <br></br>This doesn't work for ephemeral messages and will always cause an unknown message error response.
     *
     * @return [RestAction] - Type: [Message]
     */
    @Nonnull
    @CheckReturnValue
    fun retrieveOriginal(): RestAction<Message?>? {
        return retrieveMessageById("@original")
    }

    /**
     * Edit the source message sent by this interaction.
     * <br></br>For [IMessageEditCallback.editComponents] and [IMessageEditCallback.deferEdit] this will be the message the components are attached to.
     * For [IReplyCallback.deferReply] and [IReplyCallback.reply] this will be the reply message instead.
     *
     *
     * This method will be delayed until the interaction is acknowledged.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
     *
     *  * [UNKNOWN_WEBHOOK][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_WEBHOOK]
     * <br></br>The webhook is no longer available, either it was deleted or in case of interactions it expired.
     *  * [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE]
     * <br></br>The message for that id does not exist
     *
     *
     * @param  content
     * The new message content to use
     *
     * @throws IllegalArgumentException
     * If the provided content is null, empty, or longer than [Message.MAX_CONTENT_LENGTH]
     *
     * @return [WebhookMessageEditAction]
     */
    @Nonnull
    @CheckReturnValue
    fun editOriginal(@Nonnull content: String?): WebhookMessageEditAction<Message?>? {
        return editMessageById("@original", content)
    }

    /**
     * Edit the source message sent by this interaction.
     * <br></br>For [IMessageEditCallback.editComponents] and [IMessageEditCallback.deferEdit] this will be the message the components are attached to.
     * For [IReplyCallback.deferReply] and [IReplyCallback.reply] this will be the reply message instead.
     *
     *
     * This method will be delayed until the interaction is acknowledged.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
     *
     *  * [UNKNOWN_WEBHOOK][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_WEBHOOK]
     * <br></br>The webhook is no longer available, either it was deleted or in case of interactions it expired.
     *  * [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE]
     * <br></br>The message for that id does not exist
     *
     *
     * @param  components
     * The new component layouts for this message, such as [ActionRows][ActionRow]
     *
     * @throws IllegalArgumentException
     * If the provided components are null, or more than 5 layouts are provided
     *
     * @return [WebhookMessageEditAction]
     */
    @Nonnull
    @CheckReturnValue
    fun editOriginalComponents(@Nonnull components: Collection<LayoutComponent?>?): WebhookMessageEditAction<Message?>? {
        return editMessageComponentsById("@original", components)
    }

    /**
     * Edit the source message sent by this interaction.
     * <br></br>For [IMessageEditCallback.editComponents] and [IMessageEditCallback.deferEdit] this will be the message the components are attached to.
     * For [IReplyCallback.deferReply] and [IReplyCallback.reply] this will be the reply message instead.
     *
     *
     * This method will be delayed until the interaction is acknowledged.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
     *
     *  * [UNKNOWN_WEBHOOK][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_WEBHOOK]
     * <br></br>The webhook is no longer available, either it was deleted or in case of interactions it expired.
     *  * [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE]
     * <br></br>The message for that id does not exist
     *
     *
     * @param  components
     * The new component layouts for this message, such as [ActionRows][ActionRow]
     *
     * @throws IllegalArgumentException
     * If the provided components are null, or more than 5 layouts are provided
     *
     * @return [WebhookMessageEditAction]
     */
    @Nonnull
    @CheckReturnValue
    fun editOriginalComponents(@Nonnull vararg components: LayoutComponent?): WebhookMessageEditAction<Message?>? {
        return editMessageComponentsById("@original", *components)
    }

    /**
     * Edit the source message sent by this interaction.
     * <br></br>For [IMessageEditCallback.editComponents] and [IMessageEditCallback.deferEdit] this will be the message the components are attached to.
     * For [IReplyCallback.deferReply] and [IReplyCallback.reply] this will be the reply message instead.
     *
     *
     * This method will be delayed until the interaction is acknowledged.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
     *
     *  * [UNKNOWN_WEBHOOK][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_WEBHOOK]
     * <br></br>The webhook is no longer available, either it was deleted or in case of interactions it expired.
     *  * [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE]
     * <br></br>The message for that id does not exist
     *
     *
     * @param  embeds
     * [MessageEmbeds][MessageEmbed] to use (up to {@value Message#MAX_EMBED_COUNT} in total)
     *
     * @throws IllegalArgumentException
     * If the provided embeds are null, or more than {@value Message#MAX_EMBED_COUNT}
     *
     * @return [WebhookMessageEditAction]
     */
    @Nonnull
    @CheckReturnValue
    fun editOriginalEmbeds(@Nonnull embeds: Collection<MessageEmbed?>?): WebhookMessageEditAction<Message?>? {
        return editMessageEmbedsById("@original", embeds)
    }

    /**
     * Edit the source message sent by this interaction.
     * <br></br>For [IMessageEditCallback.editComponents] and [IMessageEditCallback.deferEdit] this will be the message the components are attached to.
     * For [IReplyCallback.deferReply] and [IReplyCallback.reply] this will be the reply message instead.
     *
     *
     * This method will be delayed until the interaction is acknowledged.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
     *
     *  * [UNKNOWN_WEBHOOK][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_WEBHOOK]
     * <br></br>The webhook is no longer available, either it was deleted or in case of interactions it expired.
     *  * [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE]
     * <br></br>The message for that id does not exist
     *
     *
     * @param  embeds
     * The new [MessageEmbeds][MessageEmbed] to use
     *
     * @throws IllegalArgumentException
     * If the provided embeds are null, or more than 10
     *
     * @return [WebhookMessageEditAction]
     */
    @Nonnull
    @CheckReturnValue
    fun editOriginalEmbeds(@Nonnull vararg embeds: MessageEmbed?): WebhookMessageEditAction<Message?>? {
        return editMessageEmbedsById("@original", *embeds)
    }

    /**
     * Edit the source message sent by this interaction.
     * <br></br>For [IMessageEditCallback.editComponents] and [IMessageEditCallback.deferEdit] this will be the message the components are attached to.
     * For [IReplyCallback.deferReply] and [IReplyCallback.reply] this will be the reply message instead.
     *
     *
     * This method will be delayed until the interaction is acknowledged.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
     *
     *  * [UNKNOWN_WEBHOOK][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_WEBHOOK]
     * <br></br>The webhook is no longer available, either it was deleted or in case of interactions it expired.
     *  * [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE]
     * <br></br>The message for that id does not exist
     *
     *
     * @param  message
     * The new message to replace the existing message with
     *
     * @throws IllegalArgumentException
     * If the provided message is null
     *
     * @return [WebhookMessageEditAction]
     */
    @Nonnull
    @CheckReturnValue
    fun editOriginal(@Nonnull message: MessageEditData?): WebhookMessageEditAction<Message?>? {
        return editMessageById("@original", message)
    }

    /**
     * Edit the source message sent by this interaction.
     * <br></br>For [IMessageEditCallback.editComponents] and [IMessageEditCallback.deferEdit] this will be the message the components are attached to.
     * For [IReplyCallback.deferReply] and [IReplyCallback.reply] this will be the reply message instead.
     *
     *
     * This method will be delayed until the interaction is acknowledged.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
     *
     *  * [UNKNOWN_WEBHOOK][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_WEBHOOK]
     * <br></br>The webhook is no longer available, either it was deleted or in case of interactions it expired.
     *  * [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE]
     * <br></br>The message for that id does not exist
     *
     *
     * @param  format
     * Format string for the message content
     * @param  args
     * Format arguments for the content
     *
     * @throws IllegalArgumentException
     * If the formatted string is null, empty, or longer than [Message.MAX_CONTENT_LENGTH]
     *
     * @return [WebhookMessageEditAction]
     */
    @Nonnull
    @CheckReturnValue
    fun editOriginalFormat(@Nonnull format: String?, @Nonnull vararg args: Any?): WebhookMessageEditAction<Message?>? {
        Checks.notNull(format, "Format String")
        return editOriginal(String.format(format!!, *args))
    }

    @Nonnull
    @CheckReturnValue
    fun editOriginalAttachments(@Nonnull attachments: Collection<AttachedFile?>?): WebhookMessageEditAction<Message?>? {
        return editMessageAttachmentsById("@original", attachments)
    }

    @Nonnull
    @CheckReturnValue
    fun editOriginalAttachments(@Nonnull vararg attachments: AttachedFile?): WebhookMessageEditAction<Message?>? {
        Checks.noneNull(attachments, "Attachments")
        return editOriginalAttachments(Arrays.asList(*attachments))
    }

    /**
     * Delete the original reply.
     *
     * @return [RestAction]
     */
    @Nonnull
    @CheckReturnValue
    fun deleteOriginal(): RestAction<Void?>? {
        return deleteMessageById("@original")
    }

    companion object {
        /**
         * Creates an instance of [InteractionHook] capable of executing webhook requests.
         *
         * Messages created by this client may not have a fully accessible channel or guild available, and [.getInteraction] throws.
         * The messages might report a channel of type [UNKNOWN][net.dv8tion.jda.api.entities.channel.ChannelType.UNKNOWN],
         * in which case the channel is assumed to be inaccessible and limited to only webhook requests.
         *
         * @param  jda
         * The JDA instance, used to handle rate-limits
         * @param  token
         * The interaction token for the webhook
         *
         * @throws IllegalArgumentException
         * If null is provided or the token is blank
         *
         * @return The [InteractionHook] instance
         */
        @Nonnull
        fun from(@Nonnull jda: JDA?, @Nonnull token: String?): InteractionHook? {
            Checks.notNull(jda, "JDA")
            Checks.notBlank(token, "Token")
            return InteractionHookImpl(jda!!, token!!)
        }
    }
}
