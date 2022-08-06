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
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.AllowedMentions;
import net.dv8tion.jda.api.utils.AttachedFile;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Abstraction of the common setters used for messages in the API.
 * <br>These setters can both be applied to {@link MessageEditRequest edit requests} and {@link MessageCreateRequest create requests} for messages in various parts of the API.
 *
 * @param <R>
 *        Return type used for chaining method calls
 *
 * @see   MessageCreateRequest
 * @see   MessageEditRequest
 * @see   AbstractMessageBuilder
 * @see   MessageCreateBuilder
 * @see   MessageEditBuilder
 */
public interface MessageRequest<R extends MessageRequest<R>> extends AllowedMentions<R>
{
    /**
     * The message content, which shows above embeds and attachments.
     *
     * @param  content
     *         The content (up to {@value Message#MAX_CONTENT_LENGTH} characters)
     *
     * @throws IllegalArgumentException
     *         If the content is longer than {@value Message#MAX_CONTENT_LENGTH} characters
     *
     * @return The same instance for chaining
     */
    @Nonnull
    R setContent(@Nullable String content);

    /**
     * The configured message content, this is the opposite for {@link #setContent(String)} and only returns what was set using that setter.
     *
     * <p>For message edit requests, this will not be the current content of the message.
     *
     * @return The currently configured content, or an empty string if none was set yet
     *
     * @see    #setContent(String)
     */
    @Nonnull
    String getContent();

    /**
     * The {@link MessageEmbed MessageEmbeds} that should be attached to the message.
     * <br>You can use {@link Collections#emptyList()} to remove all embeds from the message.
     *
     * <p>This requires {@link net.dv8tion.jda.api.Permission#MESSAGE_EMBED_LINKS Permission.MESSAGE_EMBED_LINKS} in the channel.
     *
     * @param  embeds
     *         The embeds to attach to the message (up to {@value Message#MAX_EMBED_COUNT})
     *
     * @throws IllegalArgumentException
     *         If null or more than {@value Message#MAX_EMBED_COUNT} embeds are provided
     *
     * @return The same instance for chaining
     *
     * @see    Collections#emptyList()
     */
    @Nonnull
    R setEmbeds(@Nonnull Collection<? extends MessageEmbed> embeds);

    /**
     * The {@link MessageEmbed MessageEmbeds} that should be attached to the message.
     * <br>You can use {@code new MessageEmbed[0]} to remove all embeds from the message.
     *
     * <p>This requires {@link net.dv8tion.jda.api.Permission#MESSAGE_EMBED_LINKS Permission.MESSAGE_EMBED_LINKS} in the channel.
     *
     * @param  embeds
     *         The embeds to attach to the message (up to {@value Message#MAX_EMBED_COUNT})
     *
     * @throws IllegalArgumentException
     *         If null or more than {@value Message#MAX_EMBED_COUNT} embeds are provided
     *
     * @return The same instance for chaining
     */
    @Nonnull
    default R setEmbeds(@Nonnull MessageEmbed... embeds)
    {
        return setEmbeds(Arrays.asList(embeds));
    }

    /**
     * The configured message embeds, this is the opposite of {@link #setEmbeds(Collection)} and only returns what was set using that setter.
     *
     * <p>For message edit requests, this will not be the current embeds of the message.
     *
     * @return The currently configured embeds, or an empty list if none were set yet
     *
     * @see    #setEmbeds(Collection)
     */
    @Nonnull
    List<? extends MessageEmbed> getEmbeds();

    /**
     * The {@link LayoutComponent LayoutComponents} that should be attached to the message.
     * <br>You can use {@link Collections#emptyList()} to remove all components from the message.
     *
     * <p>The most commonly used layout is {@link ActionRow}.
     *
     * <p><b>Example</b><br>
     * <pre>{@code
     * channel.sendMessage("Content is still required")
     *   .setComponents(
     *     ActionRow.of(selectMenu) // first row
     *     ActionRow.of(button1, button2)) // second row (shows below the first)
     *   .queue();
     * }</pre>
     *
     * @param  components
     *         The components for the message (up to {@value Message#MAX_COMPONENT_COUNT})
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code null} is provided</li>
     *             <li>If any component is not {@link LayoutComponent#isMessageCompatible() message compatible}</li>
     *             <li>If more than {@value Message#MAX_COMPONENT_COUNT} components are provided</li>
     *         </ul>
     *
     * @return The same instance for chaining
     */
    @Nonnull
    R setComponents(@Nonnull Collection<? extends LayoutComponent> components);

    /**
     * The {@link LayoutComponent LayoutComponents} that should be attached to the message.
     * <br>You can use {@link Collections#emptyList()} to remove all components from the message.
     *
     * <p>The most commonly used layout is {@link ActionRow}.
     *
     * <p><b>Example</b><br>
     * <pre>{@code
     * channel.sendMessage("Content is still required")
     *   .setComponents(
     *     ActionRow.of(selectMenu) // first row
     *     ActionRow.of(button1, button2)) // second row (shows below the first)
     *   .queue();
     * }</pre>
     *
     * @param  components
     *         The components for the message (up to {@value Message#MAX_COMPONENT_COUNT})
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code null} is provided</li>
     *             <li>If any component is not {@link LayoutComponent#isMessageCompatible() message compatible}</li>
     *             <li>If more than {@value Message#MAX_COMPONENT_COUNT} components are provided</li>
     *         </ul>
     *
     * @return The same instance for chaining
     */
    @Nonnull
    default R setComponents(@Nonnull LayoutComponent... components)
    {
        return setComponents(Arrays.asList(components));
    }

    /**
     * Convenience method to set the components of a message to a single {@link ActionRow} of components.
     * <br>To remove components, you should use {@link #setComponents(LayoutComponent...)} instead.
     *
     * <p><b>Example</b><br>
     *
     * <pre>{@code
     * channel.sendMessage("Content is still required")
     *   .setActionRow(button1, button2)
     *   .queue();
     * }</pre>
     *
     * is equivalent to:
     *
     * <pre>{@code
     * channel.sendMessage("Content is still required")
     *   .setComponents(ActionRow.of(button1, button2))
     *   .queue();
     * }</pre><br>
     *
     * @param  components
     *         The {@link ItemComponent ItemComponents} for the message (up to {@value Message#MAX_COMPONENT_COUNT})
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code null} is provided</li>
     *             <li>If any component is not {@link ItemComponent#isMessageCompatible() message compatible}</li>
     *             <li>In all the same cases as {@link ActionRow#of(ItemComponent...)} throws an exception</li>
     *         </ul>
     *
     * @return The same instance for chaining
     */
    @Nonnull
    default R setActionRow(@Nonnull Collection<? extends ItemComponent> components)
    {
        return setComponents(ActionRow.of(components));
    }

    /**
     * Convenience method to set the components of a message to a single {@link ActionRow} of components.
     * <br>To remove components, you should use {@link #setComponents(LayoutComponent...)} instead.
     *
     * <p><b>Example</b><br>
     *
     * <pre>{@code
     * channel.sendMessage("Content is still required")
     *   .setActionRow(button1, button2)
     *   .queue();
     * }</pre>
     *
     * is equivalent to:
     *
     * <pre>{@code
     * channel.sendMessage("Content is still required")
     *   .setComponents(ActionRow.of(button1, button2))
     *   .queue();
     * }</pre><br>
     *
     * @param  components
     *         The {@link ItemComponent ItemComponents} for the message (up to {@value Message#MAX_COMPONENT_COUNT})
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code null} is provided</li>
     *             <li>If any component is not {@link ItemComponent#isMessageCompatible() message compatible}</li>
     *             <li>In all the same cases as {@link ActionRow#of(ItemComponent...)} throws an exception</li>
     *         </ul>
     *
     * @return The same instance for chaining
     */
    @Nonnull
    default R setActionRow(@Nonnull ItemComponent... components)
    {
        return setComponents(ActionRow.of(components));
    }

    /**
     * The configured message components, this is the opposite of {@link #setComponents(Collection)} and only returns what was set using that setter.
     *
     * <p>For message edit requests, this will not be the current components of the message.
     *
     * @return The currently configured components, or an empty list if none were set yet
     *
     * @see    #setEmbeds(Collection)
     */
    @Nonnull
    List<? extends LayoutComponent> getComponents();

    /**
     * The {@link FileUpload FileUploads} that should be attached to the message.
     * <br>This will replace all the existing attachments on the message, if this is an edit request.
     * You can use {@link MessageEditRequest#setAttachments(Collection)} to keep existing attachments, instead of this method.
     *
     * <p><b>Resource Handling Note:</b> Once the request is handed off to the requester, for example when you call {@link RestAction#queue()},
     * the requester will automatically clean up all opened files by itself. You are only responsible to close them yourself if it is never handed off properly.
     * For instance, if an exception occurs after using {@link FileUpload#fromData(File)}, before calling {@link RestAction#queue()}.
     * You can safely use a try-with-resources to handle this, since {@link FileUpload#close()} becomes ineffective once the request is handed off.
     *
     * <p><b>Example</b><br>
     * Create an embed with a custom image, uploaded alongside the message:
     * <pre>{@code
     * MessageEmbed embed = new EmbedBuilder()
     *         .setDescription("Image of a cute cat")
     *         .setImage("attachment://cat.png") // here "cat.png" is the name used in the FileUpload.fromData factory method
     *         .build();
     *
     * // The name here will be "cat.png" to discord, what the file is called on your computer is irrelevant and only used to read the data of the image.
     * FileUpload file = FileUpload.fromData(new File("mycat-final-copy.png"), "cat.png"); // Opens the file called "cat.png" and provides the data used for sending
     *
     * channel.sendMessageEmbeds(embed)
     *        .setFiles(file)
     *        .queue();
     * }</pre>
     *
     * @param  files
     *         The {@link FileUpload FileUploads} to attach to the message,
     *         null or an empty list will set the attachments to an empty list and remove them from the message
     *
     * @throws IllegalArgumentException
     *         If null is provided inside the collection
     *
     * @return The same instance for chaining
     */
    @Nonnull
    R setFiles(@Nullable Collection<? extends FileUpload> files);

    /**
     * The {@link FileUpload FileUploads} that should be attached to the message.
     * <br>This will replace all the existing attachments on the message, if this is an edit request.
     * You can use {@link MessageEditRequest#setAttachments(AttachedFile...)} to keep existing attachments, instead of this method.
     *
     * <p><b>Resource Handling Note:</b> Once the request is handed off to the requester, for example when you call {@link RestAction#queue()},
     * the requester will automatically clean up all opened files by itself. You are only responsible to close them yourself if it is never handed off properly.
     * For instance, if an exception occurs after using {@link FileUpload#fromData(File)}, before calling {@link RestAction#queue()}.
     * You can safely use a try-with-resources to handle this, since {@link FileUpload#close()} becomes ineffective once the request is handed off.
     *
     * <p><b>Example</b><br>
     * Create an embed with a custom image, uploaded alongside the message:
     * <pre>{@code
     * MessageEmbed embed = new EmbedBuilder()
     *         .setDescription("Image of a cute cat")
     *         .setImage("attachment://cat.png") // here "cat.png" is the name used in the FileUpload.fromData factory method
     *         .build();
     *
     * // The name here will be "cat.png" to discord, what the file is called on your computer is irrelevant and only used to read the data of the image.
     * FileUpload file = FileUpload.fromData(new File("mycat-final-copy.png"), "cat.png"); // Opens the file called "cat.png" and provides the data used for sending
     *
     * channel.sendMessageEmbeds(embed)
     *        .setFiles(file)
     *        .queue();
     * }</pre>
     *
     * @param  files
     *         The {@link FileUpload FileUploads} to attach to the message,
     *         null or an empty list will set the attachments to an empty list and remove them from the message
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return The same instance for chaining
     */
    @Nonnull
    default R setFiles(@Nonnull FileUpload... files)
    {
        Checks.noneNull(files, "Files");
        return setFiles(Arrays.asList(files));
    }

    // Returns attachment interface for abstraction purposes, however you can only abstract the setter to allow FileUploads

    /**
     * The configured message attachments as {@link AttachedFile}, this is the opposite of {@link #setFiles(Collection)} and only returns what was set using that setter.
     *
     * <p>For message edit requests, this will not be the current file attachments of the message.
     *
     * @return The currently configured attachments, or an empty list if none were set yet
     *
     * @see    #setFiles(Collection)
     */
    @Nonnull
    List<? extends AttachedFile> getAttachments();

    /**
     * Applies all the data of the provided {@link Message} and attempts to copy it.
     * <br>This cannot copy the file attachments of the message, they must be manually downloaded and provided to {@link #setFiles(FileUpload...)}.
     * <br>The {@link AllowedMentions} are not updated to reflect the provided message, and might mention users that the message did not.
     *
     * <p>For edit requests, this will set {@link MessageEditRequest#replace(boolean)} to {@code true}, and replace the existing message completely.
     *
     * @param  message
     *         The message to copy the data from
     *
     * @throws IllegalArgumentException
     *         If null is provided or the message is a system message
     *
     * @return The same instance for chaining
     */
    @Nonnull
    R applyMessage(@Nonnull Message message);
}
