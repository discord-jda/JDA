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

package net.dv8tion.jda.api.utils.messages;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.AttachedFile;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Specialized abstraction of setters for editing existing messages throughout the API.
 *
 * @param <R>
 *        The return type for method chaining convenience
 *
 * @see   MessageEditBuilder
 * @see   MessageEditData
 * @see   net.dv8tion.jda.api.requests.restaction.MessageEditAction MessageEditAction
 */
public interface MessageEditRequest<R extends MessageEditRequest<R>> extends MessageRequest<R>
{
    /**
     * The {@link AttachedFile AttachedFiles} that should be attached to the message.
     * <br>This will replace all the existing attachments on the message, you can use {@link Collections#emptyList()} or {@code null} to clear all attachments.
     *
     * <p>Note that you are responsible to properly clean up your files, if the request is unsuccessful.
     * The {@link FileUpload} class will try to close it when its collected as garbage, but that can take a long time to happen.
     * You can always use {@link FileUpload#close()} and close it manually, however this should not be done until the request went through successfully.
     * The library reads the underlying resource <em>just in time</em> for the request, and will keep it open until then.
     *
     * <p><b>Example</b><br>
     * <pre>{@code
     * // Here "message" is an instance of the Message interface
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
     *        .setAttachments(attachments)
     *        .queue();
     * }</pre>
     *
     * @param  attachments
     *         The {@link AttachedFile AttachedFiles} to attach to the message,
     *         null or an empty list will set the attachments to an empty list and remove them from the message
     *
     * @throws IllegalArgumentException
     *         If null is provided inside the collection
     *
     * @return The same instance for chaining
     *
     * @see    Collections#emptyList()
     * @see    AttachedFile#fromAttachment(String)
     * @see    AttachedFile#fromData(InputStream, String)
     */
    @Nonnull
    R setAttachments(@Nullable Collection<? extends AttachedFile> attachments);

    /**
     * The {@link AttachedFile AttachedFiles} that should be attached to the message.
     * <br>This will replace all the existing attachments on the message, you can use {@code new FileAttachment[0]} to clear all attachments.
     *
     * <p>Note that you are responsible to properly clean up your files, if the request is unsuccessful.
     * The {@link FileUpload} class will try to close it when its collected as garbage, but that can take a long time to happen.
     * You can always use {@link FileUpload#close()} and close it manually, however this should not be done until the request went through successfully.
     * The library reads the underlying resource <em>just in time</em> for the request, and will keep it open until then.
     *
     * <p><b>Example</b><br>
     * <pre>{@code
     * // Here "message" is an instance of the Message interface
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
     *        .setAttachments(attachments)
     *        .queue();
     * }</pre>
     *
     * @param  attachments
     *         The {@link AttachedFile AttachedFiles} to attach to the message,
     *         null or an empty array will set the attachments to an empty list and remove them from the message
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return The same instance for chaining
     *
     * @see    AttachedFile#fromAttachment(String)
     * @see    AttachedFile#fromData(InputStream, String)
     */
    @Nonnull
    default R setAttachments(@Nonnull AttachedFile... attachments)
    {
        Checks.noneNull(attachments, "Attachments");
        return setAttachments(Arrays.asList(attachments));
    }

    @Nonnull
    @Override
    default R setFiles(@Nullable Collection<? extends FileUpload> files)
    {
        return setAttachments(files);
    }

    /**
     * Whether to replace the existing message completely.
     *
     * <p>By default, edit requests will only update the message fields which were explicitly set.
     * Changing this to {@code true}, will instead replace everything and remove all unset fields.
     *
     * <p><b>Example Default</b><br>
     * A request such as this will only edit the {@code content} of the message, and leave any existing embeds or attachments intact.
     * <pre>{@code
     * message.editMessage("hello").queue();
     * }</pre>
     *
     * <p><b>Example Replace</b><br>
     * A request such as this will replace the entire message, and remove any existing embeds, attachments, components, etc.
     * <pre>{@code
     * message.editMessage("hello").replace(true).queue();
     * }</pre>
     *
     * @param  isReplace
     *         True, if only things explicitly set on this request should be present after the message is edited.
     *
     * @return The same message edit request builder
     */
    @Nonnull
    R replace(boolean isReplace);

    /**
     * Whether this request will replace the message and remove everything that is not currently set.
     *
     * <p>If this is false, the request will only edit the message fields which were explicitly set.
     *
     * @return True, if this is a replacing request
     *
     * @see    #replace(boolean)
     */
    boolean isReplace();

    /**
     * Applies the provided {@link MessageEditData} to this request.
     * <br>If the data has all fields configured, it will also make this request a {@link #replace(boolean)} replace request.
     *
     * <p>Note that this method will only call the setters which were also configured when building the message edit data instance,
     * unless it was set to {@link #replace(boolean)}.
     *
     * @param  data
     *         The message edit data to apply
     *
     * @throws IllegalArgumentException
     *         If the data is null
     *
     * @return The same instance for chaining
     */
    @Nonnull
    R applyData(@Nonnull MessageEditData data);

    /**
     * Replaces all the fields configured on this request with the data provided by {@link MessageCreateData}.
     * <br>This will make this request a {@link #replace(boolean) replace request}.
     *
     * @param  data
     *         The message create data to apply
     *
     * @throws IllegalArgumentException
     *         If the data is null
     *
     * @return The same instance for chaining
     */
    @Nonnull
    default R applyCreateData(@Nonnull MessageCreateData data)
    {
        return replace(true)
                .setContent(data.getContent())
                .setEmbeds(data.getEmbeds())
                .setComponents(data.getComponents())
                .setFiles(data.getFiles());
    }

    @Nonnull
    default R applyMessage(@Nonnull Message message)
    {
        Checks.notNull(message, "Message");
        Checks.check(!message.getType().isSystem(), "Cannot copy a system message");
        return applyCreateData(MessageCreateData.fromMessage(message));
    }
}
