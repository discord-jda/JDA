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
package net.dv8tion.jda.api.entities

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.LayoutComponent
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction
import net.dv8tion.jda.api.requests.restaction.WebhookMessageDeleteAction
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction
import net.dv8tion.jda.api.requests.restaction.WebhookMessageRetrieveAction
import net.dv8tion.jda.api.utils.AttachedFile
import net.dv8tion.jda.api.utils.FileUpload
import net.dv8tion.jda.api.utils.MiscUtil
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.dv8tion.jda.api.utils.messages.MessageEditData
import net.dv8tion.jda.internal.requests.IncomingWebhookClientImpl
import net.dv8tion.jda.internal.utils.Checks
import java.util.*
import java.util.regex.Matcher
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Interface which allows sending messages through the webhooks API.
 * <br></br>Interactions can use these through [IDeferrableCallback.getHook].
 *
 * @see Webhook
 *
 * @see InteractionHook
 */
interface WebhookClient<T> : ISnowflake {
    /**
     * The token of this webhook.
     *
     * @return The token, or null if this webhook does not have a token available
     */
    @JvmField
    val token: String?

    @get:Nonnull
    val jDA: JDA?

    /**
     * Send a message to this webhook.
     *
     *
     * If this is an [InteractionHook] this method will be delayed until the interaction is acknowledged.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
     *
     *  * [UNKNOWN_WEBHOOK][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_WEBHOOK]
     * <br></br>The webhook is no longer available, either it was deleted or in case of interactions it expired.
     *
     *  * [MESSAGE_BLOCKED_BY_AUTOMOD][net.dv8tion.jda.api.requests.ErrorResponse.MESSAGE_BLOCKED_BY_AUTOMOD]
     * <br></br>If this message was blocked by an [AutoModRule][net.dv8tion.jda.api.entities.automod.AutoModRule]
     *
     *  * [MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER][net.dv8tion.jda.api.requests.ErrorResponse.MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER]
     * <br></br>If this message was blocked by the harmful link filter
     *
     *
     * @param  content
     * The message content
     *
     * @throws IllegalArgumentException
     * If the content is null or longer than [Message.MAX_CONTENT_LENGTH] characters
     *
     * @return [net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction]
     */
    @Nonnull
    @CheckReturnValue
    fun sendMessage(@Nonnull content: String?): WebhookMessageCreateAction<T>?

    /**
     * Send a message to this webhook.
     *
     *
     * If this is an [InteractionHook] this method will be delayed until the interaction is acknowledged.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
     *
     *  * [UNKNOWN_WEBHOOK][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_WEBHOOK]
     * <br></br>The webhook is no longer available, either it was deleted or in case of interactions it expired.
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
     * @return [net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction]
     *
     * @see net.dv8tion.jda.api.utils.messages.MessageCreateBuilder MessageCreateBuilder
     */
    @Nonnull
    @CheckReturnValue
    fun sendMessage(@Nonnull message: MessageCreateData?): WebhookMessageCreateAction<T>?

    /**
     * Send a message to this webhook.
     *
     *
     * If this is an [InteractionHook] this method will be delayed until the interaction is acknowledged.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
     *
     *  * [UNKNOWN_WEBHOOK][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_WEBHOOK]
     * <br></br>The webhook is no longer available, either it was deleted or in case of interactions it expired.
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
     * @return [net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction]
     */
    @Nonnull
    @CheckReturnValue
    fun sendMessageFormat(@Nonnull format: String?, @Nonnull vararg args: Any?): WebhookMessageCreateAction<T>? {
        Checks.notNull(format, "Format String")
        return sendMessage(String.format(format!!, *args))
    }

    /**
     * Send a message to this webhook.
     *
     *
     * If this is an [InteractionHook] this method will be delayed until the interaction is acknowledged.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
     *
     *  * [UNKNOWN_WEBHOOK][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_WEBHOOK]
     * <br></br>The webhook is no longer available, either it was deleted or in case of interactions it expired.
     *
     *  * [MESSAGE_BLOCKED_BY_AUTOMOD][net.dv8tion.jda.api.requests.ErrorResponse.MESSAGE_BLOCKED_BY_AUTOMOD]
     * <br></br>If this message was blocked by an [AutoModRule][net.dv8tion.jda.api.entities.automod.AutoModRule]
     *
     *  * [MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER][net.dv8tion.jda.api.requests.ErrorResponse.MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER]
     * <br></br>If this message was blocked by the harmful link filter
     *
     *
     *
     * **Example: Attachment Images**
     * <pre>`// Make a file upload instance which refers to a local file called "myFile.png"
     * // The second parameter "image.png" is the filename we tell discord to use for the attachment
     * FileUpload file = FileUpload.fromData(new File("myFile.png"), "image.png");
     *
     * // Build a message embed which refers to this attachment by the given name.
     * // Note that this must be the same name as configured for the attachment, not your local filename.
     * MessageEmbed embed = new EmbedBuilder()
     * .setDescription("This is my cute cat :)")
     * .setImage("attachment://image.png") // refer to the file by using the "attachment://" schema with the filename we gave it above
     * .build();
     *
     * webhook.sendMessageEmbeds(Collections.singleton(embed)) // send the embeds
     * .addFiles(file) // add the file as attachment
     * .queue();
    `</pre> *
     *
     * @param  embeds
     * [MessageEmbeds][MessageEmbed] to use (up to {@value Message#MAX_EMBED_COUNT})
     *
     * @throws IllegalArgumentException
     * If any of the embeds are null, more than {@value Message#MAX_EMBED_COUNT}, or longer than [MessageEmbed.EMBED_MAX_LENGTH_BOT].
     *
     * @return [net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction]
     */
    @Nonnull
    @CheckReturnValue
    fun sendMessageEmbeds(@Nonnull embeds: Collection<MessageEmbed>?): WebhookMessageCreateAction<T>?

    /**
     * Send a message to this webhook.
     *
     *
     * If this is an [InteractionHook] this method will be delayed until the interaction is acknowledged.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
     *
     *  * [UNKNOWN_WEBHOOK][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_WEBHOOK]
     * <br></br>The webhook is no longer available, either it was deleted or in case of interactions it expired.
     *
     *  * [MESSAGE_BLOCKED_BY_AUTOMOD][net.dv8tion.jda.api.requests.ErrorResponse.MESSAGE_BLOCKED_BY_AUTOMOD]
     * <br></br>If this message was blocked by an [AutoModRule][net.dv8tion.jda.api.entities.automod.AutoModRule]
     *
     *  * [MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER][net.dv8tion.jda.api.requests.ErrorResponse.MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER]
     * <br></br>If this message was blocked by the harmful link filter
     *
     *
     *
     * **Example: Attachment Images**
     * <pre>`// Make a file upload instance which refers to a local file called "myFile.png"
     * // The second parameter "image.png" is the filename we tell discord to use for the attachment
     * FileUpload file = FileUpload.fromData(new File("myFile.png"), "image.png");
     *
     * // Build a message embed which refers to this attachment by the given name.
     * // Note that this must be the same name as configured for the attachment, not your local filename.
     * MessageEmbed embed = new EmbedBuilder()
     * .setDescription("This is my cute cat :)")
     * .setImage("attachment://image.png") // refer to the file by using the "attachment://" schema with the filename we gave it above
     * .build();
     *
     * webhook.sendMessageEmbeds(embed) // send the embed
     * .addFiles(file) // add the file as attachment
     * .queue();
    `</pre> *
     *
     * @param  embed
     * [MessageEmbed] to use
     * @param  embeds
     * Additional [MessageEmbeds][MessageEmbed] to use (up to {@value Message#MAX_EMBED_COUNT} in total)
     *
     * @throws IllegalArgumentException
     * If any of the embeds are null, more than {@value Message#MAX_EMBED_COUNT}, or longer than [MessageEmbed.EMBED_MAX_LENGTH_BOT].
     *
     * @return [net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction]
     */
    @Nonnull
    @CheckReturnValue
    fun sendMessageEmbeds(
        @Nonnull embed: MessageEmbed,
        @Nonnull vararg embeds: MessageEmbed?
    ): WebhookMessageCreateAction<T>? {
        Checks.notNull(embed, "MessageEmbeds")
        Checks.noneNull(embeds, "MessageEmbeds")
        val embedList: MutableList<MessageEmbed> = ArrayList()
        embedList.add(embed)
        Collections.addAll(embedList, *embeds)
        return sendMessageEmbeds(embedList)
    }

    /**
     * Send a message to this webhook.
     *
     *
     * If this is an [InteractionHook] this method will be delayed until the interaction is acknowledged.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
     *
     *  * [UNKNOWN_WEBHOOK][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_WEBHOOK]
     * <br></br>The webhook is no longer available, either it was deleted or in case of interactions it expired.
     *
     *  * [MESSAGE_BLOCKED_BY_AUTOMOD][net.dv8tion.jda.api.requests.ErrorResponse.MESSAGE_BLOCKED_BY_AUTOMOD]
     * <br></br>If this message was blocked by an [AutoModRule][net.dv8tion.jda.api.entities.automod.AutoModRule]
     *
     *  * [MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER][net.dv8tion.jda.api.requests.ErrorResponse.MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER]
     * <br></br>If this message was blocked by the harmful link filter
     *
     *
     * @param  components
     * [LayoutComponents][LayoutComponent] to use (up to {@value Message#MAX_COMPONENT_COUNT})
     *
     * @throws IllegalArgumentException
     * If any of the components are null or more than {@value Message#MAX_COMPONENT_COUNT} component layouts are provided
     *
     * @return [net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction]
     */
    @Nonnull
    @CheckReturnValue
    fun sendMessageComponents(@Nonnull components: Collection<LayoutComponent>?): WebhookMessageCreateAction<T>?

    /**
     * Send a message to this webhook.
     *
     *
     * If this is an [InteractionHook] this method will be delayed until the interaction is acknowledged.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
     *
     *  * [UNKNOWN_WEBHOOK][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_WEBHOOK]
     * <br></br>The webhook is no longer available, either it was deleted or in case of interactions it expired.
     *
     *  * [MESSAGE_BLOCKED_BY_AUTOMOD][net.dv8tion.jda.api.requests.ErrorResponse.MESSAGE_BLOCKED_BY_AUTOMOD]
     * <br></br>If this message was blocked by an [AutoModRule][net.dv8tion.jda.api.entities.automod.AutoModRule]
     *
     *  * [MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER][net.dv8tion.jda.api.requests.ErrorResponse.MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER]
     * <br></br>If this message was blocked by the harmful link filter
     *
     *
     * @param  component
     * [LayoutComponent] to use
     * @param  other
     * Additional [LayoutComponents][LayoutComponent] to use (up to {@value Message#MAX_COMPONENT_COUNT} in total)
     *
     * @throws IllegalArgumentException
     * If any of the components are null or more than {@value Message#MAX_COMPONENT_COUNT} component layouts are provided
     *
     * @return [net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction]
     */
    @Nonnull
    @CheckReturnValue
    fun sendMessageComponents(
        @Nonnull component: LayoutComponent,
        @Nonnull vararg other: LayoutComponent?
    ): WebhookMessageCreateAction<T>? {
        Checks.notNull(component, "LayoutComponents")
        Checks.noneNull(other, "LayoutComponents")
        val embedList: MutableList<LayoutComponent> = ArrayList()
        embedList.add(component)
        Collections.addAll(embedList, *other)
        return sendMessageComponents(embedList)
    }

    /**
     * Send a message to this webhook.
     *
     *
     * If this is an [InteractionHook] this method will be delayed until the interaction is acknowledged.
     *
     *
     * **Resource Handling Note:** Once the request is handed off to the requester, for example when you call [RestAction.queue],
     * the requester will automatically clean up all opened files by itself. You are only responsible to close them yourself if it is never handed off properly.
     * For instance, if an exception occurs after using [FileUpload.fromData], before calling [RestAction.queue].
     * You can safely use a try-with-resources to handle this, since [FileUpload.close] becomes ineffective once the request is handed off.
     *
     *
     * **Example: Attachment Images**
     * <pre>`// Make a file upload instance which refers to a local file called "myFile.png"
     * // The second parameter "image.png" is the filename we tell discord to use for the attachment
     * FileUpload file = FileUpload.fromData(new File("myFile.png"), "image.png");
     *
     * // Build a message embed which refers to this attachment by the given name.
     * // Note that this must be the same name as configured for the attachment, not your local filename.
     * MessageEmbed embed = new EmbedBuilder()
     * .setDescription("This is my cute cat :)")
     * .setImage("attachment://image.png") // refer to the file by using the "attachment://" schema with the filename we gave it above
     * .build();
     *
     * webhook.sendFiles(Collections.singleton(file)) // send the file upload
     * .addEmbeds(embed) // add the embed you want to reference the file with
     * .queue();
    `</pre> *
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
     *
     *  * [UNKNOWN_WEBHOOK][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_WEBHOOK]
     * <br></br>The webhook is no longer available, either it was deleted or in case of interactions it expired.
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
     * @return [WebhookMessageCreateAction]
     *
     * @see FileUpload.fromData
     */
    @Nonnull
    @CheckReturnValue
    fun sendFiles(@Nonnull files: Collection<FileUpload?>?): WebhookMessageCreateAction<T>?

    /**
     * Send a message to this webhook.
     *
     *
     * If this is an [InteractionHook] this method will be delayed until the interaction is acknowledged.
     *
     *
     * **Resource Handling Note:** Once the request is handed off to the requester, for example when you call [RestAction.queue],
     * the requester will automatically clean up all opened files by itself. You are only responsible to close them yourself if it is never handed off properly.
     * For instance, if an exception occurs after using [FileUpload.fromData], before calling [RestAction.queue].
     * You can safely use a try-with-resources to handle this, since [FileUpload.close] becomes ineffective once the request is handed off.
     *
     *
     * **Example: Attachment Images**
     * <pre>`// Make a file upload instance which refers to a local file called "myFile.png"
     * // The second parameter "image.png" is the filename we tell discord to use for the attachment
     * FileUpload file = FileUpload.fromData(new File("myFile.png"), "image.png");
     *
     * // Build a message embed which refers to this attachment by the given name.
     * // Note that this must be the same name as configured for the attachment, not your local filename.
     * MessageEmbed embed = new EmbedBuilder()
     * .setDescription("This is my cute cat :)")
     * .setImage("attachment://image.png") // refer to the file by using the "attachment://" schema with the filename we gave it above
     * .build();
     *
     * webhook.sendFiles(file) // send the file upload
     * .addEmbeds(embed) // add the embed you want to reference the file with
     * .queue();
    `</pre> *
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
     *
     *  * [UNKNOWN_WEBHOOK][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_WEBHOOK]
     * <br></br>The webhook is no longer available, either it was deleted or in case of interactions it expired.
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
     * @return [WebhookMessageCreateAction]
     *
     * @see FileUpload.fromData
     */
    @Nonnull
    @CheckReturnValue
    fun sendFiles(@Nonnull vararg files: FileUpload?): WebhookMessageCreateAction<T>? {
        Checks.noneNull(files, "Files")
        Checks.notEmpty(files, "Files")
        return sendFiles(Arrays.asList(*files))
    }

    /**
     * Edit an existing message sent by this webhook.
     *
     *
     * If this is an [InteractionHook] this method will be delayed until the interaction is acknowledged.
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
     * @param  messageId
     * The message id. For interactions this supports `"@original"` to edit the source message of the interaction.
     * @param  content
     * The new message content to use
     *
     * @throws IllegalArgumentException
     * If the provided content is null or longer than [Message.MAX_CONTENT_LENGTH] characters
     *
     * @return [WebhookMessageEditAction]
     */
    @Nonnull
    @CheckReturnValue
    fun editMessageById(@Nonnull messageId: String?, @Nonnull content: String?): WebhookMessageEditAction<T>?

    /**
     * Edit an existing message sent by this webhook.
     *
     *
     * If this is an [InteractionHook] this method will be delayed until the interaction is acknowledged.
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
     * @param  messageId
     * The message id. For interactions this supports `"@original"` to edit the source message of the interaction.
     * @param  content
     * The new message content to use
     *
     * @throws IllegalArgumentException
     * If the provided content is null or longer than [Message.MAX_CONTENT_LENGTH] characters
     *
     * @return [WebhookMessageEditAction]
     */
    @Nonnull
    @CheckReturnValue
    fun editMessageById(messageId: Long, @Nonnull content: String?): WebhookMessageEditAction<T>? {
        return editMessageById(java.lang.Long.toUnsignedString(messageId), content)
    }

    /**
     * Edit an existing message sent by this webhook.
     *
     *
     * If this is an [InteractionHook] this method will be delayed until the interaction is acknowledged.
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
     * @param  messageId
     * The message id. For interactions this supports `"@original"` to edit the source message of the interaction.
     * @param  message
     * The [MessageEditData] containing the update information
     *
     * @throws IllegalArgumentException
     * If the provided message is null
     *
     * @return [WebhookMessageEditAction]
     *
     * @see net.dv8tion.jda.api.utils.messages.MessageEditBuilder MessageEditBuilder
     */
    @Nonnull
    @CheckReturnValue
    fun editMessageById(@Nonnull messageId: String?, @Nonnull message: MessageEditData?): WebhookMessageEditAction<T>?

    /**
     * Edit an existing message sent by this webhook.
     *
     *
     * If this is an [InteractionHook] this method will be delayed until the interaction is acknowledged.
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
     * @param  messageId
     * The message id. For interactions this supports `"@original"` to edit the source message of the interaction.
     * @param  message
     * The [MessageEditData] containing the update information
     *
     * @throws IllegalArgumentException
     * If the provided message is null
     *
     * @return [WebhookMessageEditAction]
     *
     * @see net.dv8tion.jda.api.utils.messages.MessageEditBuilder MessageEditBuilder
     */
    @Nonnull
    @CheckReturnValue
    fun editMessageById(messageId: Long, message: MessageEditData?): WebhookMessageEditAction<T>? {
        return editMessageById(java.lang.Long.toUnsignedString(messageId), message)
    }

    /**
     * Edit an existing message sent by this webhook.
     *
     *
     * If this is an [InteractionHook] this method will be delayed until the interaction is acknowledged.
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
     * @param  messageId
     * The message id. For interactions this supports `"@original"` to edit the source message of the interaction.
     * @param  format
     * Format string for the message content
     * @param  args
     * Format arguments for the content
     *
     * @throws IllegalArgumentException
     * If the formatted string is null or longer than [Message.MAX_CONTENT_LENGTH] characters
     *
     * @return [WebhookMessageEditAction]
     */
    @Nonnull
    @CheckReturnValue
    fun editMessageFormatById(
        @Nonnull messageId: String?,
        @Nonnull format: String?,
        @Nonnull vararg args: Any?
    ): WebhookMessageEditAction<T>? {
        Checks.notNull(format, "Format String")
        return editMessageById(messageId, String.format(format!!, *args))
    }

    /**
     * Edit an existing message sent by this webhook.
     *
     *
     * If this is an [InteractionHook] this method will be delayed until the interaction is acknowledged.
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
     * @param  messageId
     * The message id. For interactions this supports `"@original"` to edit the source message of the interaction.
     * @param  format
     * Format string for the message content
     * @param  args
     * Format arguments for the content
     *
     * @throws IllegalArgumentException
     * If the formatted string is null or longer than [Message.MAX_CONTENT_LENGTH] characters
     *
     * @return [WebhookMessageEditAction]
     */
    @Nonnull
    @CheckReturnValue
    fun editMessageFormatById(
        messageId: Long,
        @Nonnull format: String?,
        @Nonnull vararg args: Any?
    ): WebhookMessageEditAction<T>? {
        return editMessageFormatById(java.lang.Long.toUnsignedString(messageId), format, *args)
    }

    /**
     * Edit an existing message sent by this webhook.
     *
     *
     * If this is an [InteractionHook] this method will be delayed until the interaction is acknowledged.
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
     * @param  messageId
     * The message id. For interactions this supports `"@original"` to edit the source message of the interaction.
     * @param  embeds
     * [MessageEmbeds][MessageEmbed] to use (up to {@value Message#MAX_EMBED_COUNT} in total)
     *
     * @throws IllegalArgumentException
     * If null or more than {@value Message#MAX_EMBED_COUNT} embeds are provided
     *
     * @return [WebhookMessageEditAction]
     */
    @Nonnull
    @CheckReturnValue
    fun editMessageEmbedsById(
        @Nonnull messageId: String?,
        @Nonnull embeds: Collection<MessageEmbed?>?
    ): WebhookMessageEditAction<T>?

    /**
     * Edit an existing message sent by this webhook.
     *
     *
     * If this is an [InteractionHook] this method will be delayed until the interaction is acknowledged.
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
     * @param  messageId
     * The message id. For interactions this supports `"@original"` to edit the source message of the interaction.
     * @param  embeds
     * [MessageEmbeds][MessageEmbed] to use (up to {@value Message#MAX_EMBED_COUNT} in total)
     *
     * @throws IllegalArgumentException
     * If null or more than {@value Message#MAX_EMBED_COUNT} embeds are provided
     *
     * @return [WebhookMessageEditAction]
     */
    @Nonnull
    @CheckReturnValue
    fun editMessageEmbedsById(
        messageId: Long,
        @Nonnull embeds: Collection<MessageEmbed?>?
    ): WebhookMessageEditAction<T>? {
        return editMessageEmbedsById(java.lang.Long.toUnsignedString(messageId), embeds)
    }

    /**
     * Edit an existing message sent by this webhook.
     *
     *
     * If this is an [InteractionHook] this method will be delayed until the interaction is acknowledged.
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
     * @param  messageId
     * The message id. For interactions this supports `"@original"` to edit the source message of the interaction.
     * @param  embeds
     * The new [MessageEmbeds][MessageEmbed] to use
     *
     * @throws IllegalArgumentException
     * If null or more than {@value Message#MAX_EMBED_COUNT} embeds are provided
     *
     * @return [WebhookMessageEditAction]
     */
    @Nonnull
    @CheckReturnValue
    fun editMessageEmbedsById(
        @Nonnull messageId: String?,
        @Nonnull vararg embeds: MessageEmbed?
    ): WebhookMessageEditAction<T>? {
        Checks.noneNull(embeds, "MessageEmbeds")
        return editMessageEmbedsById(messageId, Arrays.asList(*embeds))
    }

    /**
     * Edit an existing message sent by this webhook.
     *
     *
     * If this is an [InteractionHook] this method will be delayed until the interaction is acknowledged.
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
     * @param  messageId
     * The message id. For interactions this supports `"@original"` to edit the source message of the interaction.
     * @param  embeds
     * The new [MessageEmbeds][MessageEmbed] to use
     *
     * @throws IllegalArgumentException
     * If null or more than {@value Message#MAX_EMBED_COUNT} embeds are provided
     *
     * @return [WebhookMessageEditAction]
     */
    @Nonnull
    @CheckReturnValue
    fun editMessageEmbedsById(messageId: Long, @Nonnull vararg embeds: MessageEmbed?): WebhookMessageEditAction<T>? {
        return editMessageEmbedsById(java.lang.Long.toUnsignedString(messageId), *embeds)
    }

    /**
     * Edit an existing message sent by this webhook.
     *
     *
     * If this is an [InteractionHook] this method will be delayed until the interaction is acknowledged.
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
     * @param  messageId
     * The message id. For interactions this supports `"@original"` to edit the source message of the interaction.
     * @param  components
     * The new component layouts for this message, such as [ActionRows][ActionRow]
     *
     * @throws IllegalArgumentException
     *
     *  * If `null` is provided
     *  * If any of the components is not [message compatible][LayoutComponent.isMessageCompatible]
     *  * If more than {@value Message#MAX_COMPONENT_COUNT} component layouts are provided
     *
     *
     * @return [WebhookMessageEditAction]
     */
    @Nonnull
    @CheckReturnValue
    fun editMessageComponentsById(
        @Nonnull messageId: String?,
        @Nonnull components: Collection<LayoutComponent?>?
    ): WebhookMessageEditAction<T>?

    /**
     * Edit an existing message sent by this webhook.
     *
     *
     * If this is an [InteractionHook] this method will be delayed until the interaction is acknowledged.
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
     * @param  messageId
     * The message id. For interactions this supports `"@original"` to edit the source message of the interaction.
     * @param  components
     * The new component layouts for this message, such as [ActionRows][ActionRow]
     *
     * @throws IllegalArgumentException
     *
     *  * If `null` is provided
     *  * If any of the components is not [message compatible][LayoutComponent.isMessageCompatible]
     *  * If more than {@value Message#MAX_COMPONENT_COUNT} component layouts are provided
     *
     *
     * @return [WebhookMessageEditAction]
     */
    @Nonnull
    @CheckReturnValue
    fun editMessageComponentsById(
        messageId: Long,
        @Nonnull components: Collection<LayoutComponent?>?
    ): WebhookMessageEditAction<T>? {
        return editMessageComponentsById(java.lang.Long.toUnsignedString(messageId), components)
    }

    /**
     * Edit an existing message sent by this webhook.
     *
     *
     * If this is an [InteractionHook] this method will be delayed until the interaction is acknowledged.
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
     * @param  messageId
     * The message id. For interactions this supports `"@original"` to edit the source message of the interaction.
     * @param  components
     * The new component layouts for this message, such as [ActionRows][ActionRow]
     *
     * @throws IllegalArgumentException
     *
     *  * If `null` is provided
     *  * If any of the components is not [message compatible][LayoutComponent.isMessageCompatible]
     *  * If more than {@value Message#MAX_COMPONENT_COUNT} component layouts are provided
     *
     *
     * @return [WebhookMessageEditAction]
     */
    @Nonnull
    @CheckReturnValue
    fun editMessageComponentsById(
        @Nonnull messageId: String?,
        @Nonnull vararg components: LayoutComponent?
    ): WebhookMessageEditAction<T>? {
        Checks.noneNull(components, "LayoutComponents")
        return editMessageComponentsById(messageId, Arrays.asList(*components))
    }

    /**
     * Edit an existing message sent by this webhook.
     *
     *
     * If this is an [InteractionHook] this method will be delayed until the interaction is acknowledged.
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
     * @param  messageId
     * The message id. For interactions this supports `"@original"` to edit the source message of the interaction.
     * @param  components
     * The new component layouts for this message, such as [ActionRows][ActionRow]
     *
     * @throws IllegalArgumentException
     *
     *  * If `null` is provided
     *  * If any of the components is not [message compatible][LayoutComponent.isMessageCompatible]
     *  * If more than {@value Message#MAX_COMPONENT_COUNT} component layouts are provided
     *
     *
     * @return [WebhookMessageEditAction]
     */
    @Nonnull
    @CheckReturnValue
    fun editMessageComponentsById(
        messageId: Long,
        @Nonnull vararg components: LayoutComponent?
    ): WebhookMessageEditAction<T>? {
        return editMessageComponentsById(java.lang.Long.toUnsignedString(messageId), *components)
    }

    /**
     * Edit an existing message sent by this webhook.
     *
     *
     * If this is an [InteractionHook] this method will be delayed until the interaction is acknowledged.
     *
     *
     * **Resource Handling Note:** Once the request is handed off to the requester, for example when you call [RestAction.queue],
     * the requester will automatically clean up all opened files by itself. You are only responsible to close them yourself if it is never handed off properly.
     * For instance, if an exception occurs after using [FileUpload.fromData], before calling [RestAction.queue].
     * You can safely use a try-with-resources to handle this, since [FileUpload.close] becomes ineffective once the request is handed off.
     *
     * @param  messageId
     * The message id. For interactions this supports `"@original"` to edit the source message of the interaction.
     * @param  attachments
     * The new attachments of the message (Can be [FileUploads][FileUpload] or [AttachmentUpdates][net.dv8tion.jda.api.utils.AttachmentUpdate])
     *
     * @throws IllegalArgumentException
     * If null is provided
     *
     * @return [MessageEditCallbackAction] that can be used to further update the message
     *
     * @see AttachedFile.fromAttachment
     * @see FileUpload.fromData
     */
    @Nonnull
    @CheckReturnValue
    fun editMessageAttachmentsById(
        @Nonnull messageId: String?,
        @Nonnull attachments: Collection<AttachedFile?>?
    ): WebhookMessageEditAction<T>?

    /**
     * Edit an existing message sent by this webhook.
     *
     *
     * If this is an [InteractionHook] this method will be delayed until the interaction is acknowledged.
     *
     *
     * **Resource Handling Note:** Once the request is handed off to the requester, for example when you call [RestAction.queue],
     * the requester will automatically clean up all opened files by itself. You are only responsible to close them yourself if it is never handed off properly.
     * For instance, if an exception occurs after using [FileUpload.fromData], before calling [RestAction.queue].
     * You can safely use a try-with-resources to handle this, since [FileUpload.close] becomes ineffective once the request is handed off.
     *
     * @param  messageId
     * The message id. For interactions this supports `"@original"` to edit the source message of the interaction.
     * @param  attachments
     * The new attachments of the message (Can be [FileUploads][FileUpload] or [AttachmentUpdates][net.dv8tion.jda.api.utils.AttachmentUpdate])
     *
     * @throws IllegalArgumentException
     * If null is provided
     *
     * @return [MessageEditCallbackAction] that can be used to further update the message
     *
     * @see AttachedFile.fromAttachment
     * @see FileUpload.fromData
     */
    @Nonnull
    @CheckReturnValue
    fun editMessageAttachmentsById(
        @Nonnull messageId: String?,
        @Nonnull vararg attachments: AttachedFile?
    ): WebhookMessageEditAction<T>? {
        Checks.noneNull(attachments, "Attachments")
        return editMessageAttachmentsById(messageId, Arrays.asList(*attachments))
    }

    /**
     * Edit an existing message sent by this webhook.
     *
     *
     * If this is an [InteractionHook] this method will be delayed until the interaction is acknowledged.
     *
     *
     * **Resource Handling Note:** Once the request is handed off to the requester, for example when you call [RestAction.queue],
     * the requester will automatically clean up all opened files by itself. You are only responsible to close them yourself if it is never handed off properly.
     * For instance, if an exception occurs after using [FileUpload.fromData], before calling [RestAction.queue].
     * You can safely use a try-with-resources to handle this, since [FileUpload.close] becomes ineffective once the request is handed off.
     *
     * @param  messageId
     * The message id. For interactions this supports `"@original"` to edit the source message of the interaction.
     * @param  attachments
     * The new attachments of the message (Can be [FileUploads][FileUpload] or [AttachmentUpdates][net.dv8tion.jda.api.utils.AttachmentUpdate])
     *
     * @throws IllegalArgumentException
     * If null is provided
     *
     * @return [MessageEditCallbackAction] that can be used to further update the message
     *
     * @see AttachedFile.fromAttachment
     * @see FileUpload.fromData
     */
    @Nonnull
    @CheckReturnValue
    fun editMessageAttachmentsById(
        messageId: Long,
        @Nonnull attachments: Collection<AttachedFile?>?
    ): WebhookMessageEditAction<T>? {
        return editMessageAttachmentsById(java.lang.Long.toUnsignedString(messageId), attachments)
    }

    /**
     * Edit an existing message sent by this webhook.
     *
     *
     * If this is an [InteractionHook] this method will be delayed until the interaction is acknowledged.
     *
     *
     * **Resource Handling Note:** Once the request is handed off to the requester, for example when you call [RestAction.queue],
     * the requester will automatically clean up all opened files by itself. You are only responsible to close them yourself if it is never handed off properly.
     * For instance, if an exception occurs after using [FileUpload.fromData], before calling [RestAction.queue].
     * You can safely use a try-with-resources to handle this, since [FileUpload.close] becomes ineffective once the request is handed off.
     *
     * @param  messageId
     * The message id. For interactions this supports `"@original"` to edit the source message of the interaction.
     * @param  attachments
     * The new attachments of the message (Can be [FileUploads][FileUpload] or [AttachmentUpdates][net.dv8tion.jda.api.utils.AttachmentUpdate])
     *
     * @throws IllegalArgumentException
     * If null is provided
     *
     * @return [MessageEditCallbackAction] that can be used to further update the message
     *
     * @see AttachedFile.fromAttachment
     * @see FileUpload.fromData
     */
    @Nonnull
    @CheckReturnValue
    fun editMessageAttachmentsById(
        messageId: Long,
        @Nonnull vararg attachments: AttachedFile?
    ): WebhookMessageEditAction<T>? {
        return editMessageAttachmentsById(java.lang.Long.toUnsignedString(messageId), *attachments)
    }

    /**
     * Delete a message from this webhook.
     *
     *
     * Use [setThreadId(threadId)][WebhookMessageRetrieveAction.setThreadId] to delete messages from threads.
     *
     *
     * If this is an [InteractionHook] this method will be delayed until the interaction is acknowledged.
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
     * @param  messageId
     * The id for the message to delete
     *
     * @throws IllegalArgumentException
     * If the provided message id is null or not a valid snowflake
     *
     * @return [WebhookMessageDeleteAction]
     */
    @Nonnull
    @CheckReturnValue
    fun deleteMessageById(@Nonnull messageId: String?): WebhookMessageDeleteAction?

    /**
     * Delete a message from this webhook.
     *
     *
     * Use [setThreadId(threadId)][WebhookMessageRetrieveAction.setThreadId] to delete messages from threads.
     *
     *
     * If this is an [InteractionHook] this method will be delayed until the interaction is acknowledged.
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
     * @param  messageId
     * The id for the message to delete
     *
     * @return [WebhookMessageDeleteAction]
     */
    @Nonnull
    @CheckReturnValue
    fun deleteMessageById(messageId: Long): WebhookMessageDeleteAction? {
        return deleteMessageById(java.lang.Long.toUnsignedString(messageId))
    }

    /**
     * Retrieves the message with the provided id.
     * <br></br>This only works for messages sent by this webhook. All other messages are unknown.
     *
     *
     * Use [setThreadId(threadId)][WebhookMessageRetrieveAction.setThreadId] to retrieve messages from threads.
     *
     *
     * If this is an [InteractionHook] this method will be delayed until the interaction is acknowledged.
     *
     *
     * Possible [ErrorResponses][net.dv8tion.jda.api.requests.ErrorResponse] include:
     *
     *  * [UNKNOWN_WEBHOOK][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_WEBHOOK]
     * <br></br>The webhook is no longer available, either it was deleted or in case of interactions it expired.
     *
     *  * [UNKNOWN_MESSAGE][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE]
     * <br></br>If the message is inaccessible to this webhook or does not exist.
     *
     *
     * @return [WebhookMessageRetrieveAction]
     */
    @Nonnull
    @CheckReturnValue
    fun retrieveMessageById(@Nonnull messageId: String?): WebhookMessageRetrieveAction?

    companion object {
        /**
         * Creates an instance of [IncomingWebhookClient] capable of executing webhook requests.
         *
         * Messages created by this client may not have a fully accessible channel or guild available.
         * The messages might report a channel of type [UNKNOWN][net.dv8tion.jda.api.entities.channel.ChannelType.UNKNOWN],
         * in which case the channel is assumed to be inaccessible and limited to only webhook requests.
         *
         * @param  api
         * The JDA instance, used to handle rate-limits
         * @param  url
         * The webhook url, must include a webhook token
         *
         * @throws IllegalArgumentException
         * If null is provided or the provided url is not a valid webhook url
         *
         * @return The [IncomingWebhookClient] instance
         *
         * @see InteractionHook.from
         */
        @Nonnull
        fun createClient(@Nonnull api: JDA?, @Nonnull url: String?): IncomingWebhookClient? {
            Checks.notNull(url, "URL")
            val matcher: Matcher = Webhook.Companion.WEBHOOK_URL.matcher(url)
            require(matcher.matches()) { "Provided invalid webhook URL" }
            val id = matcher.group(1)
            val token = matcher.group(2)
            return createClient(api, id, token)
        }

        /**
         * Creates an instance of [IncomingWebhookClient] capable of executing webhook requests.
         *
         * Messages created by this client may not have a fully accessible channel or guild available.
         * The messages might report a channel of type [UNKNOWN][net.dv8tion.jda.api.entities.channel.ChannelType.UNKNOWN],
         * in which case the channel is assumed to be inaccessible and limited to only webhook requests.
         *
         * @param  api
         * The JDA instance, used to handle rate-limits
         * @param  webhookId
         * The id of the webhook, for interactions this is the application id
         * @param  webhookToken
         * The token of the webhook, for interactions this is the interaction token
         *
         * @throws IllegalArgumentException
         * If null is provided or the provided webhook id is not a valid snowflake or the token is blank
         *
         * @return The [IncomingWebhookClient] instance
         *
         * @see InteractionHook.from
         */
        @JvmStatic
        @Nonnull
        fun createClient(
            @Nonnull api: JDA?,
            @Nonnull webhookId: String?,
            @Nonnull webhookToken: String?
        ): IncomingWebhookClient? {
            Checks.notNull(api, "JDA")
            Checks.notBlank(webhookToken, "Token")
            return IncomingWebhookClientImpl(MiscUtil.parseSnowflake(webhookId), webhookToken, api)
        }
    }
}
