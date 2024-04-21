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
import java.util.*
import java.util.function.Function
import java.util.stream.Collectors
import javax.annotation.Nonnull

/**
 * Specialized abstraction of setters for editing existing messages throughout the API.
 *
 * @param <R>
 * The return type for method chaining convenience
 *
 * @see MessageEditBuilder
 *
 * @see MessageEditData
 *
 * @see net.dv8tion.jda.api.requests.restaction.MessageEditAction MessageEditAction
</R> */
open interface MessageEditRequest<R : MessageEditRequest<R>?> : MessageRequest<R> {
    /**
     * The [AttachedFiles][AttachedFile] that should be attached to the message.
     * <br></br>This will replace all the existing attachments on the message, you can use [Collections.emptyList] or `null` to clear all attachments.
     *
     *
     * **Resource Handling Note:** Once the request is handed off to the requester, for example when you call [RestAction.queue],
     * the requester will automatically clean up all opened files by itself. You are only responsible to close them yourself if it is never handed off properly.
     * For instance, if an exception occurs after using [FileUpload.fromData], before calling [RestAction.queue].
     * You can safely use a try-with-resources to handle this, since [FileUpload.close] becomes ineffective once the request is handed off.
     *
     *
     * **Example**<br></br>
     * <pre>`// Here "message" is an instance of the Message interface
     *
     * // Creates a list of the currently attached files of the message, important to get the generic parameter of the list right
     * List<AttachedFile> attachments = new ArrayList<>(message.getAttachments());
     *
     * // The name here will be "cat.png" to discord, what the file is called on your computer is irrelevant and only used to read the data of the image.
     * FileUpload file = FileUpload.fromData(new File("mycat-final-copy.png"), "cat.png"); // Opens the file called "cat.png" and provides the data used for sending
     *
     * // Adds another file to upload in addition the current attachments of the message
     * attachments.add(file);
     *
     * message.editMessage("New content")
     * .setAttachments(attachments)
     * .queue();
    `</pre> *
     *
     * @param  attachments
     * The [AttachedFiles][AttachedFile] to attach to the message,
     * null or an empty list will set the attachments to an empty list and remove them from the message
     *
     * @throws IllegalArgumentException
     * If null is provided inside the collection
     *
     * @return The same instance for chaining
     *
     * @see Collections.emptyList
     * @see AttachedFile.fromAttachment
     * @see AttachedFile.fromData
     */
    @Nonnull
    fun setAttachments(attachments: Collection<AttachedFile?>?): R

    /**
     * The [AttachedFiles][AttachedFile] that should be attached to the message.
     * <br></br>This will replace all the existing attachments on the message, you can use `new FileAttachment[0]` to clear all attachments.
     *
     *
     * **Resource Handling Note:** Once the request is handed off to the requester, for example when you call [RestAction.queue],
     * the requester will automatically clean up all opened files by itself. You are only responsible to close them yourself if it is never handed off properly.
     * For instance, if an exception occurs after using [FileUpload.fromData], before calling [RestAction.queue].
     * You can safely use a try-with-resources to handle this, since [FileUpload.close] becomes ineffective once the request is handed off.
     *
     *
     * **Example**<br></br>
     * <pre>`// Here "message" is an instance of the Message interface
     *
     * // Take the first attachment of the message, all others will be removed
     * AttachedFile attachment = message.getAttachments().get(0);
     *
     * // The name here will be "cat.png" to discord, what the file is called on your computer is irrelevant and only used to read the data of the image.
     * FileUpload file = FileUpload.fromData(new File("mycat-final-copy.png"), "cat.png"); // Opens the file called "cat.png" and provides the data used for sending
     *
     * // Edit request to keep the first attachment, and add one more file to the message
     * message.editMessage("New content")
     * .setAttachments(attachment, file)
     * .queue();
    `</pre> *
     *
     * @param  attachments
     * The [AttachedFiles][AttachedFile] to attach to the message,
     * null or an empty array will set the attachments to an empty list and remove them from the message
     *
     * @throws IllegalArgumentException
     * If null is provided
     *
     * @return The same instance for chaining
     *
     * @see AttachedFile.fromAttachment
     * @see AttachedFile.fromData
     */
    @Nonnull
    fun setAttachments(@Nonnull vararg attachments: AttachedFile?): R {
        Checks.noneNull(attachments, "Attachments")
        return setAttachments(Arrays.asList(*attachments))
    }

    @Nonnull
    public override fun setFiles(files: Collection<FileUpload?>?): R {
        return setAttachments(files)
    }

    /**
     * Whether to replace the existing message completely.
     *
     *
     * By default, edit requests will only update the message fields which were explicitly set.
     * Changing this to `true`, will instead replace everything and remove all unset fields.
     *
     *
     * **Example Default**<br></br>
     * A request such as this will only edit the `content` of the message, and leave any existing embeds or attachments intact.
     * <pre>`message.editMessage("hello").queue();
    `</pre> *
     *
     *
     * **Example Replace**<br></br>
     * A request such as this will replace the entire message, and remove any existing embeds, attachments, components, etc.
     * <pre>`message.editMessage("hello").setReplace(true).queue();
    `</pre> *
     *
     * @param  isReplace
     * True, if only things explicitly set on this request should be present after the message is edited.
     *
     * @return The same message edit request builder
     */
    @Nonnull
    fun setReplace(isReplace: Boolean): R

    /**
     * Whether this request will replace the message and remove everything that is not currently set.
     *
     *
     * If this is false, the request will only edit the message fields which were explicitly set.
     *
     * @return True, if this is a replacing request
     *
     * @see .setReplace
     */
    val isReplace: Boolean

    /**
     * Applies the provided [MessageEditData] to this request.
     *
     *
     * Note that this method will only call the setters which were also configured when building the message edit data instance,
     * unless it was set to [replace][.setReplace].
     *
     * @param  data
     * The message edit data to apply
     *
     * @throws IllegalArgumentException
     * If the data is null
     *
     * @return The same instance for chaining
     */
    @Nonnull
    fun applyData(@Nonnull data: MessageEditData): R

    /**
     * Replaces all the fields configured on this request with the data provided by [MessageCreateData].
     * <br></br>This will make this request a [replace request][.setReplace].
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
    fun applyCreateData(@Nonnull data: MessageCreateData?): R {
        val layoutComponents: List<LayoutComponent?> = data.getComponents().stream()
            .map(Function({ obj: LayoutComponent? -> obj!!.createCopy() }))
            .collect(Collectors.toList())
        return setReplace(true)
            .setContent(data.getContent())
            .setAllowedMentions(data.getAllowedMentions())
            .mentionUsers(data.getMentionedUsers())
            .mentionRoles(data.getMentionedRoles())
            .mentionRepliedUser(data!!.isMentionRepliedUser())
            .setEmbeds(data.getEmbeds())
            .setComponents(layoutComponents)
            .setFiles(data.getFiles())
    }

    @Nonnull
    public override fun applyMessage(@Nonnull message: Message): R {
        Checks.notNull(message, "Message")
        Checks.check(!message.type!!.isSystem(), "Cannot copy a system message")
        return applyCreateData(MessageCreateData.Companion.fromMessage(message))
    }
}
