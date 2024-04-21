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
import net.dv8tion.jda.api.requests.restaction.interactions.MessageEditCallbackAction
import net.dv8tion.jda.api.utils.AttachedFile
import net.dv8tion.jda.api.utils.FileUpload
import net.dv8tion.jda.api.utils.messages.MessageEditData
import net.dv8tion.jda.internal.requests.restaction.interactions.MessageEditCallbackActionImpl
import net.dv8tion.jda.internal.utils.Checks
import java.util.*
import java.util.stream.Collectors
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Interactions which allow a target message to be edited on use.
 *
 *
 * Editing a message using these methods will automatically acknowledge the interaction.
 *
 *
 * **Deferred Edits**<br></br>
 *
 * Similar to [IReplyCallback], message edits can be deferred and performed later with [.deferEdit].
 * A deferred edit tells Discord, that you intend to edit the message this interaction was performed on, but will do so later.
 * However, you can defer the edit and never do it, which is effectively a no-operation acknowledgement of the interaction.
 *
 *
 * If an edit is [deferred][.deferEdit], it becomes the **original** message of the interaction hook.
 * This means all the methods with `original` in the name, such as [InteractionHook.editOriginal],
 * will affect that original message you edited.
 */
interface IMessageEditCallback : IDeferrableCallback {
    /**
     * No-op acknowledgement of this interaction.
     * <br></br>This tells discord you intend to update the message that the triggering component is a part of using the [InteractionHook][.getHook] instead of sending a reply message.
     * You are not required to actually update the message, this will simply acknowledge that you accepted the interaction.
     *
     *
     * **You only have 3 seconds to acknowledge an interaction!**
     * <br></br>When the acknowledgement is sent after the interaction expired, you will receive [ErrorResponse.UNKNOWN_INTERACTION][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_INTERACTION].
     *
     * Use [.editMessage] to edit it directly.
     *
     * @return [MessageEditCallbackAction] that can be used to update the message
     *
     * @see .editMessage
     */
    @Nonnull
    @CheckReturnValue
    fun deferEdit(): MessageEditCallbackAction

    /**
     * Acknowledgement of this interaction with a message update.
     * <br></br>You can use [.getHook] to edit the message further.
     *
     *
     * **You can only use deferEdit() or editMessage() once per interaction!** Use [.getHook] for any additional updates.
     *
     *
     * **You only have 3 seconds to acknowledge an interaction!**
     * <br></br>When the acknowledgement is sent after the interaction expired, you will receive [ErrorResponse.UNKNOWN_INTERACTION][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_INTERACTION].
     *
     * @param  message
     * The new message content to use
     *
     * @throws IllegalArgumentException
     * If the provided message is null
     *
     * @return [MessageEditCallbackAction] that can be used to further update the message
     */
    @Nonnull
    @CheckReturnValue
    fun editMessage(@Nonnull message: MessageEditData?): MessageEditCallbackAction? {
        Checks.notNull(message, "Message")
        val action = deferEdit() as MessageEditCallbackActionImpl
        return action.applyData(message!!)
    }

    /**
     * Acknowledgement of this interaction with a message update.
     * <br></br>You can use [.getHook] to edit the message further.
     *
     *
     * **You can only use deferEdit() or editMessage() once per interaction!** Use [.getHook] for any additional updates.
     *
     *
     * **You only have 3 seconds to acknowledge an interaction!**
     * <br></br>When the acknowledgement is sent after the interaction expired, you will receive [ErrorResponse.UNKNOWN_INTERACTION][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_INTERACTION].
     *
     * @param  content
     * The new message content to use
     *
     * @throws IllegalArgumentException
     * If the provided content is null or longer than {@value Message#MAX_CONTENT_LENGTH} characters
     *
     * @return [MessageEditCallbackAction] that can be used to further update the message
     */
    @Nonnull
    @CheckReturnValue
    fun editMessage(@Nonnull content: String?): MessageEditCallbackAction? {
        Checks.notNull(content, "Content")
        return deferEdit().setContent(content)
    }

    /**
     * Acknowledgement of this interaction with a message update.
     * <br></br>You can use [.getHook] to edit the message further.
     *
     *
     * **You can only use deferEdit() or editMessage() once per interaction!** Use [.getHook] for any additional updates.
     *
     *
     * **You only have 3 seconds to acknowledge an interaction!**
     * <br></br>When the acknowledgement is sent after the interaction expired, you will receive [ErrorResponse.UNKNOWN_INTERACTION][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_INTERACTION].
     *
     * @param  components
     * The new message components, such as [ActionRow]
     *
     * @throws IllegalArgumentException
     *
     *  * If any of the provided LayoutComponents is null
     *  * If any of the provided Components are not compatible with messages
     *  * If more than {@value Message#MAX_COMPONENT_COUNT} component layouts are provided
     *
     *
     * @return [MessageEditCallbackAction] that can be used to further update the message
     *
     * @see LayoutComponent.isMessageCompatible
     */
    @Nonnull
    @CheckReturnValue
    fun editComponents(@Nonnull components: Collection<LayoutComponent?>): MessageEditCallbackAction? {
        Checks.noneNull(components, "Components")
        if (components.stream()
                .anyMatch { it: LayoutComponent? -> it !is ActionRow }
        ) throw UnsupportedOperationException("The provided component layout is not supported")
        val actionRows =
            components.stream().map { obj: Any? -> ActionRow::class.java.cast(obj) }.collect(Collectors.toList())
        return deferEdit().setComponents(actionRows)
    }

    /**
     * Acknowledgement of this interaction with a message update.
     * <br></br>You can use [.getHook] to edit the message further.
     *
     *
     * **You can only use deferEdit() or editMessage() once per interaction!** Use [.getHook] for any additional updates.
     *
     *
     * **You only have 3 seconds to acknowledge an interaction!**
     * <br></br>When the acknowledgement is sent after the interaction expired, you will receive [ErrorResponse.UNKNOWN_INTERACTION][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_INTERACTION].
     *
     * @param  components
     * The new message components, such as [ActionRow]
     *
     * @throws IllegalArgumentException
     *
     *  * If any of the provided LayoutComponents are null
     *  * If any of the provided Components are not compatible with messages
     *  * If more than {@value Message#MAX_COMPONENT_COUNT} component layouts are provided
     *
     *
     * @return [MessageEditCallbackAction] that can be used to further update the message
     *
     * @see LayoutComponent.isMessageCompatible
     */
    @Nonnull
    @CheckReturnValue
    fun editComponents(@Nonnull vararg components: LayoutComponent?): MessageEditCallbackAction? {
        Checks.noneNull(components, "LayoutComponents")
        return editComponents(Arrays.asList(*components))
    }

    /**
     * Acknowledgement of this interaction with a message update.
     * <br></br>You can use [.getHook] to edit the message further.
     *
     *
     * **You can only use deferEdit() or editMessage() once per interaction!** Use [.getHook] for any additional updates.
     *
     *
     * **You only have 3 seconds to acknowledge an interaction!**
     * <br></br>When the acknowledgement is sent after the interaction expired, you will receive [ErrorResponse.UNKNOWN_INTERACTION][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_INTERACTION].
     *
     * @param  embeds
     * The new [MessageEmbeds][MessageEmbed]
     *
     * @throws IllegalArgumentException
     * If null or more than {@value Message#MAX_EMBED_COUNT} embeds are provided
     *
     * @return [MessageEditCallbackAction] that can be used to further update the message
     */
    @Nonnull
    @CheckReturnValue
    fun editMessageEmbeds(@Nonnull embeds: Collection<MessageEmbed?>?): MessageEditCallbackAction? {
        Checks.noneNull(embeds, "MessageEmbed")
        return deferEdit().setEmbeds(embeds!!)
    }

    /**
     * Acknowledgement of this interaction with a message update.
     * <br></br>You can use [.getHook] to edit the message further.
     *
     *
     * **You can only use deferEdit() or editMessage() once per interaction!** Use [.getHook] for any additional updates.
     *
     *
     * **You only have 3 seconds to acknowledge an interaction!**
     * <br></br>When the acknowledgement is sent after the interaction expired, you will receive [ErrorResponse.UNKNOWN_INTERACTION][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_INTERACTION].
     *
     * @param  embeds
     * The new message embeds to include in the message
     *
     * @throws IllegalArgumentException
     * If null or more than {@value Message#MAX_EMBED_COUNT} embeds are provided
     *
     * @return [MessageEditCallbackAction] that can be used to further update the message
     */
    @Nonnull
    @CheckReturnValue
    fun editMessageEmbeds(@Nonnull vararg embeds: MessageEmbed?): MessageEditCallbackAction? {
        Checks.noneNull(embeds, "MessageEmbed")
        return deferEdit().setEmbeds(*embeds)
    }

    /**
     * Acknowledgement of this interaction with a message update.
     * <br></br>You can use [.getHook] to edit the message further.
     *
     *
     * **You can only use deferEdit() or editMessage() once per interaction!** Use [.getHook] for any additional updates.
     *
     *
     * **You only have 3 seconds to acknowledge an interaction!**
     * <br></br>When the acknowledgement is sent after the interaction expired, you will receive [ErrorResponse.UNKNOWN_INTERACTION][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_INTERACTION].
     *
     * @param  format
     * The format string for the new message content
     * @param  args
     * The format arguments
     *
     * @throws IllegalArgumentException
     * If the provided format is null
     *
     * @return [MessageEditCallbackAction] that can be used to further update the message
     */
    @Nonnull
    @CheckReturnValue
    fun editMessageFormat(@Nonnull format: String?, @Nonnull vararg args: Any?): MessageEditCallbackAction? {
        Checks.notNull(format, "Format String")
        return editMessage(String.format(format!!, *args))
    }

    /**
     * Acknowledgement of this interaction with a message update.
     * <br></br>You can use [.getHook] to edit the message further.
     *
     *
     * **You can only use deferEdit() or editMessage() once per interaction!** Use [.getHook] for any additional updates.
     *
     *
     * **You only have 3 seconds to acknowledge an interaction!**
     * <br></br>When the acknowledgement is sent after the interaction expired, you will receive [ErrorResponse.UNKNOWN_INTERACTION][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_INTERACTION].
     *
     *
     * **Resource Handling Note:** Once the request is handed off to the requester, for example when you call [RestAction.queue],
     * the requester will automatically clean up all opened files by itself. You are only responsible to close them yourself if it is never handed off properly.
     * For instance, if an exception occurs after using [FileUpload.fromData], before calling [RestAction.queue].
     * You can safely use a try-with-resources to handle this, since [FileUpload.close] becomes ineffective once the request is handed off.
     *
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
    fun editMessageAttachments(@Nonnull attachments: Collection<AttachedFile?>?): MessageEditCallbackAction? {
        Checks.noneNull(attachments, "Attachments")
        return deferEdit().setAttachments(attachments)
    }

    /**
     * Acknowledgement of this interaction with a message update.
     * <br></br>You can use [.getHook] to edit the message further.
     *
     *
     * **You can only use deferEdit() or editMessage() once per interaction!** Use [.getHook] for any additional updates.
     *
     *
     * **You only have 3 seconds to acknowledge an interaction!**
     * <br></br>When the acknowledgement is sent after the interaction expired, you will receive [ErrorResponse.UNKNOWN_INTERACTION][net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_INTERACTION].
     *
     *
     * **Resource Handling Note:** Once the request is handed off to the requester, for example when you call [RestAction.queue],
     * the requester will automatically clean up all opened files by itself. You are only responsible to close them yourself if it is never handed off properly.
     * For instance, if an exception occurs after using [FileUpload.fromData], before calling [RestAction.queue].
     * You can safely use a try-with-resources to handle this, since [FileUpload.close] becomes ineffective once the request is handed off.
     *
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
    fun editMessageAttachments(@Nonnull vararg attachments: AttachedFile?): MessageEditCallbackAction? {
        Checks.noneNull(attachments, "Attachments")
        return deferEdit().setAttachments(*attachments)
    }
}
