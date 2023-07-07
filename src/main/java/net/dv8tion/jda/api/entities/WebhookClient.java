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

package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.api.interactions.callbacks.IDeferrableCallback;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;
import net.dv8tion.jda.api.requests.restaction.interactions.MessageEditCallbackAction;
import net.dv8tion.jda.api.utils.AttachedFile;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.io.File;
import java.io.InputStream;
import java.util.*;

/**
 * Interface which allows sending messages through the webhooks API.
 * <br>Interactions can use these through {@link IDeferrableCallback#getHook()}.
 *
 * @see Webhook
 * @see net.dv8tion.jda.api.interactions.InteractionHook
 */
public interface WebhookClient<T>
{
    /**
     * Send a message to this webhook.
     *
     * <p>If this is an {@link net.dv8tion.jda.api.interactions.InteractionHook InteractionHook} this method will be delayed until the interaction is acknowledged.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_WEBHOOK UNKNOWN_WEBHOOK}
     *     <br>The webhook is no longer available, either it was deleted or in case of interactions it expired.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_AUTOMOD MESSAGE_BLOCKED_BY_AUTOMOD}
     *     <br>If this message was blocked by an {@link net.dv8tion.jda.api.entities.automod.AutoModRule AutoModRule}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER}
     *     <br>If this message was blocked by the harmful link filter</li>
     * </ul>
     *
     * @param  content
     *         The message content
     *
     * @throws IllegalArgumentException
     *         If the content is null or longer than {@link Message#MAX_CONTENT_LENGTH} characters
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction}
     */
    @Nonnull
    @CheckReturnValue
    WebhookMessageCreateAction<T> sendMessage(@Nonnull String content);

    /**
     * Send a message to this webhook.
     *
     * <p>If this is an {@link net.dv8tion.jda.api.interactions.InteractionHook InteractionHook} this method will be delayed until the interaction is acknowledged.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_WEBHOOK UNKNOWN_WEBHOOK}
     *     <br>The webhook is no longer available, either it was deleted or in case of interactions it expired.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_AUTOMOD MESSAGE_BLOCKED_BY_AUTOMOD}
     *     <br>If this message was blocked by an {@link net.dv8tion.jda.api.entities.automod.AutoModRule AutoModRule}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER}
     *     <br>If this message was blocked by the harmful link filter</li>
     * </ul>
     *
     * @param  message
     *         The {@link MessageCreateData} to send
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction}
     *
     * @see    net.dv8tion.jda.api.utils.messages.MessageCreateBuilder MessageCreateBuilder
     */
    @Nonnull
    @CheckReturnValue
    WebhookMessageCreateAction<T> sendMessage(@Nonnull MessageCreateData message);

    /**
     * Send a message to this webhook.
     *
     * <p>If this is an {@link net.dv8tion.jda.api.interactions.InteractionHook InteractionHook} this method will be delayed until the interaction is acknowledged.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_WEBHOOK UNKNOWN_WEBHOOK}
     *     <br>The webhook is no longer available, either it was deleted or in case of interactions it expired.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_AUTOMOD MESSAGE_BLOCKED_BY_AUTOMOD}
     *     <br>If this message was blocked by an {@link net.dv8tion.jda.api.entities.automod.AutoModRule AutoModRule}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER}
     *     <br>If this message was blocked by the harmful link filter</li>
     * </ul>
     *
     * @param  format
     *         Format string for the message content
     * @param  args
     *         Format arguments for the content
     *
     * @throws IllegalArgumentException
     *         If the format string is null or the resulting content is longer than {@link Message#MAX_CONTENT_LENGTH} characters
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction}
     */
    @Nonnull
    @CheckReturnValue
    default WebhookMessageCreateAction<T> sendMessageFormat(@Nonnull String format, @Nonnull Object... args)
    {
        Checks.notNull(format, "Format String");
        return sendMessage(String.format(format, args));
    }

    /**
     * Send a message to this webhook.
     *
     * <p>If this is an {@link net.dv8tion.jda.api.interactions.InteractionHook InteractionHook} this method will be delayed until the interaction is acknowledged.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_WEBHOOK UNKNOWN_WEBHOOK}
     *     <br>The webhook is no longer available, either it was deleted or in case of interactions it expired.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_AUTOMOD MESSAGE_BLOCKED_BY_AUTOMOD}
     *     <br>If this message was blocked by an {@link net.dv8tion.jda.api.entities.automod.AutoModRule AutoModRule}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER}
     *     <br>If this message was blocked by the harmful link filter</li>
     * </ul>
     *
     * <p><b>Example: Attachment Images</b>
     * <pre>{@code
     * // Make a file upload instance which refers to a local file called "myFile.png"
     * // The second parameter "image.png" is the filename we tell discord to use for the attachment
     * FileUpload file = FileUpload.fromData(new File("myFile.png"), "image.png");
     *
     * // Build a message embed which refers to this attachment by the given name.
     * // Note that this must be the same name as configured for the attachment, not your local filename.
     * MessageEmbed embed = new EmbedBuilder()
     *   .setDescription("This is my cute cat :)")
     *   .setImage("attachment://image.png") // refer to the file by using the "attachment://" schema with the filename we gave it above
     *   .build();
     *
     * webhook.sendMessageEmbeds(Collections.singleton(embed)) // send the embeds
     *        .addFiles(file) // add the file as attachment
     *        .queue();
     * }</pre>
     *
     * @param  embeds
     *         {@link MessageEmbed MessageEmbeds} to use (up to {@value Message#MAX_EMBED_COUNT})
     *
     * @throws IllegalArgumentException
     *         If any of the embeds are null, more than {@value Message#MAX_EMBED_COUNT}, or longer than {@link MessageEmbed#EMBED_MAX_LENGTH_BOT}.
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction}
     */
    @Nonnull
    @CheckReturnValue
    WebhookMessageCreateAction<T> sendMessageEmbeds(@Nonnull Collection<? extends MessageEmbed> embeds);

    /**
     * Send a message to this webhook.
     *
     * <p>If this is an {@link net.dv8tion.jda.api.interactions.InteractionHook InteractionHook} this method will be delayed until the interaction is acknowledged.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_WEBHOOK UNKNOWN_WEBHOOK}
     *     <br>The webhook is no longer available, either it was deleted or in case of interactions it expired.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_AUTOMOD MESSAGE_BLOCKED_BY_AUTOMOD}
     *     <br>If this message was blocked by an {@link net.dv8tion.jda.api.entities.automod.AutoModRule AutoModRule}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER}
     *     <br>If this message was blocked by the harmful link filter</li>
     * </ul>
     *
     * <p><b>Example: Attachment Images</b>
     * <pre>{@code
     * // Make a file upload instance which refers to a local file called "myFile.png"
     * // The second parameter "image.png" is the filename we tell discord to use for the attachment
     * FileUpload file = FileUpload.fromData(new File("myFile.png"), "image.png");
     *
     * // Build a message embed which refers to this attachment by the given name.
     * // Note that this must be the same name as configured for the attachment, not your local filename.
     * MessageEmbed embed = new EmbedBuilder()
     *   .setDescription("This is my cute cat :)")
     *   .setImage("attachment://image.png") // refer to the file by using the "attachment://" schema with the filename we gave it above
     *   .build();
     *
     * webhook.sendMessageEmbeds(embed) // send the embed
     *        .addFiles(file) // add the file as attachment
     *        .queue();
     * }</pre>
     *
     * @param  embed
     *         {@link MessageEmbed} to use
     * @param  embeds
     *         Additional {@link MessageEmbed MessageEmbeds} to use (up to {@value Message#MAX_EMBED_COUNT} in total)
     *
     * @throws IllegalArgumentException
     *         If any of the embeds are null, more than {@value Message#MAX_EMBED_COUNT}, or longer than {@link MessageEmbed#EMBED_MAX_LENGTH_BOT}.
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction}
     */
    @Nonnull
    @CheckReturnValue
    default WebhookMessageCreateAction<T> sendMessageEmbeds(@Nonnull MessageEmbed embed, @Nonnull MessageEmbed... embeds)
    {
        Checks.notNull(embed, "MessageEmbeds");
        Checks.noneNull(embeds, "MessageEmbeds");
        List<MessageEmbed> embedList = new ArrayList<>();
        embedList.add(embed);
        Collections.addAll(embedList, embeds);
        return sendMessageEmbeds(embedList);
    }

    /**
     * Send a message to this webhook.
     *
     * <p>If this is an {@link net.dv8tion.jda.api.interactions.InteractionHook InteractionHook} this method will be delayed until the interaction is acknowledged.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_WEBHOOK UNKNOWN_WEBHOOK}
     *     <br>The webhook is no longer available, either it was deleted or in case of interactions it expired.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_AUTOMOD MESSAGE_BLOCKED_BY_AUTOMOD}
     *     <br>If this message was blocked by an {@link net.dv8tion.jda.api.entities.automod.AutoModRule AutoModRule}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER}
     *     <br>If this message was blocked by the harmful link filter</li>
     * </ul>
     *
     * @param  components
     *         {@link LayoutComponent LayoutComponents} to use (up to {@value Message#MAX_COMPONENT_COUNT})
     *
     * @throws IllegalArgumentException
     *         If any of the components are null or more than {@value Message#MAX_COMPONENT_COUNT} component layouts are provided
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction}
     */
    @Nonnull
    @CheckReturnValue
    WebhookMessageCreateAction<T> sendMessageComponents(@Nonnull Collection<? extends LayoutComponent> components);

    /**
     * Send a message to this webhook.
     *
     * <p>If this is an {@link net.dv8tion.jda.api.interactions.InteractionHook InteractionHook} this method will be delayed until the interaction is acknowledged.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_WEBHOOK UNKNOWN_WEBHOOK}
     *     <br>The webhook is no longer available, either it was deleted or in case of interactions it expired.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_AUTOMOD MESSAGE_BLOCKED_BY_AUTOMOD}
     *     <br>If this message was blocked by an {@link net.dv8tion.jda.api.entities.automod.AutoModRule AutoModRule}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER}
     *     <br>If this message was blocked by the harmful link filter</li>
     * </ul>
     *
     * @param  component
     *         {@link LayoutComponent} to use
     * @param  other
     *         Additional {@link LayoutComponent LayoutComponents} to use (up to {@value Message#MAX_COMPONENT_COUNT} in total)
     *
     * @throws IllegalArgumentException
     *         If any of the components are null or more than {@value Message#MAX_COMPONENT_COUNT} component layouts are provided
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction}
     */
    @Nonnull
    @CheckReturnValue
    default WebhookMessageCreateAction<T> sendMessageComponents(@Nonnull LayoutComponent component, @Nonnull LayoutComponent... other)
    {
        Checks.notNull(component, "LayoutComponents");
        Checks.noneNull(other, "LayoutComponents");
        List<LayoutComponent> embedList = new ArrayList<>();
        embedList.add(component);
        Collections.addAll(embedList, other);
        return sendMessageComponents(embedList);
    }

    /**
     * Send a message to this webhook.
     *
     * <p>If this is an {@link net.dv8tion.jda.api.interactions.InteractionHook InteractionHook} this method will be delayed until the interaction is acknowledged.
     *
     * <p><b>Resource Handling Note:</b> Once the request is handed off to the requester, for example when you call {@link RestAction#queue()},
     * the requester will automatically clean up all opened files by itself. You are only responsible to close them yourself if it is never handed off properly.
     * For instance, if an exception occurs after using {@link FileUpload#fromData(File)}, before calling {@link RestAction#queue()}.
     * You can safely use a try-with-resources to handle this, since {@link FileUpload#close()} becomes ineffective once the request is handed off.
     *
     * <p><b>Example: Attachment Images</b>
     * <pre>{@code
     * // Make a file upload instance which refers to a local file called "myFile.png"
     * // The second parameter "image.png" is the filename we tell discord to use for the attachment
     * FileUpload file = FileUpload.fromData(new File("myFile.png"), "image.png");
     *
     * // Build a message embed which refers to this attachment by the given name.
     * // Note that this must be the same name as configured for the attachment, not your local filename.
     * MessageEmbed embed = new EmbedBuilder()
     *   .setDescription("This is my cute cat :)")
     *   .setImage("attachment://image.png") // refer to the file by using the "attachment://" schema with the filename we gave it above
     *   .build();
     *
     * webhook.sendFiles(Collections.singleton(file)) // send the file upload
     *        .addEmbeds(embed) // add the embed you want to reference the file with
     *        .queue();
     * }</pre>
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_WEBHOOK UNKNOWN_WEBHOOK}
     *     <br>The webhook is no longer available, either it was deleted or in case of interactions it expired.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_AUTOMOD MESSAGE_BLOCKED_BY_AUTOMOD}
     *     <br>If this message was blocked by an {@link net.dv8tion.jda.api.entities.automod.AutoModRule AutoModRule}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER}
     *     <br>If this message was blocked by the harmful link filter</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#REQUEST_ENTITY_TOO_LARGE REQUEST_ENTITY_TOO_LARGE}
     *     <br>If the total sum of uploaded bytes exceeds the guild's {@link Guild#getMaxFileSize() upload limit}</li>
     * </ul>
     *
     * @param  files
     *         The {@link FileUpload FileUploads} to attach to the message
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return {@link WebhookMessageCreateAction}
     *
     * @see    FileUpload#fromData(InputStream, String)
     */
    @Nonnull
    @CheckReturnValue
    WebhookMessageCreateAction<T> sendFiles(@Nonnull Collection<? extends FileUpload> files);

    /**
     * Send a message to this webhook.
     *
     * <p>If this is an {@link net.dv8tion.jda.api.interactions.InteractionHook InteractionHook} this method will be delayed until the interaction is acknowledged.
     *
     * <p><b>Resource Handling Note:</b> Once the request is handed off to the requester, for example when you call {@link RestAction#queue()},
     * the requester will automatically clean up all opened files by itself. You are only responsible to close them yourself if it is never handed off properly.
     * For instance, if an exception occurs after using {@link FileUpload#fromData(File)}, before calling {@link RestAction#queue()}.
     * You can safely use a try-with-resources to handle this, since {@link FileUpload#close()} becomes ineffective once the request is handed off.
     *
     * <p><b>Example: Attachment Images</b>
     * <pre>{@code
     * // Make a file upload instance which refers to a local file called "myFile.png"
     * // The second parameter "image.png" is the filename we tell discord to use for the attachment
     * FileUpload file = FileUpload.fromData(new File("myFile.png"), "image.png");
     *
     * // Build a message embed which refers to this attachment by the given name.
     * // Note that this must be the same name as configured for the attachment, not your local filename.
     * MessageEmbed embed = new EmbedBuilder()
     *   .setDescription("This is my cute cat :)")
     *   .setImage("attachment://image.png") // refer to the file by using the "attachment://" schema with the filename we gave it above
     *   .build();
     *
     * webhook.sendFiles(file) // send the file upload
     *        .addEmbeds(embed) // add the embed you want to reference the file with
     *        .queue();
     * }</pre>
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_WEBHOOK UNKNOWN_WEBHOOK}
     *     <br>The webhook is no longer available, either it was deleted or in case of interactions it expired.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_AUTOMOD MESSAGE_BLOCKED_BY_AUTOMOD}
     *     <br>If this message was blocked by an {@link net.dv8tion.jda.api.entities.automod.AutoModRule AutoModRule}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER}
     *     <br>If this message was blocked by the harmful link filter</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#REQUEST_ENTITY_TOO_LARGE REQUEST_ENTITY_TOO_LARGE}
     *     <br>If the total sum of uploaded bytes exceeds the guild's {@link Guild#getMaxFileSize() upload limit}</li>
     * </ul>
     *
     * @param  files
     *         The {@link FileUpload FileUploads} to attach to the message
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return {@link WebhookMessageCreateAction}
     *
     * @see    FileUpload#fromData(InputStream, String)
     */
    @Nonnull
    @CheckReturnValue
    default WebhookMessageCreateAction<T> sendFiles(@Nonnull FileUpload... files)
    {
        Checks.noneNull(files, "Files");
        Checks.notEmpty(files, "Files");
        return sendFiles(Arrays.asList(files));
    }

    /**
     * Edit an existing message sent by this webhook.
     *
     * <p>If this is an {@link net.dv8tion.jda.api.interactions.InteractionHook InteractionHook} this method will be delayed until the interaction is acknowledged.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_WEBHOOK UNKNOWN_WEBHOOK}
     *     <br>The webhook is no longer available, either it was deleted or in case of interactions it expired.</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The message for that id does not exist</li>
     * </ul>
     *
     * @param  messageId
     *         The message id. For interactions this supports {@code "@original"} to edit the source message of the interaction.
     * @param  content
     *         The new message content to use
     *
     * @throws IllegalArgumentException
     *         If the provided content is null or longer than {@link Message#MAX_CONTENT_LENGTH} characters
     *
     * @return {@link WebhookMessageEditAction}
     */
    @Nonnull
    @CheckReturnValue
    WebhookMessageEditAction<T> editMessageById(@Nonnull String messageId, @Nonnull String content);

    /**
     * Edit an existing message sent by this webhook.
     *
     * <p>If this is an {@link net.dv8tion.jda.api.interactions.InteractionHook InteractionHook} this method will be delayed until the interaction is acknowledged.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_WEBHOOK UNKNOWN_WEBHOOK}
     *     <br>The webhook is no longer available, either it was deleted or in case of interactions it expired.</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The message for that id does not exist</li>
     * </ul>
     *
     * @param  messageId
     *         The message id. For interactions this supports {@code "@original"} to edit the source message of the interaction.
     * @param  content
     *         The new message content to use
     *
     * @throws IllegalArgumentException
     *         If the provided content is null or longer than {@link Message#MAX_CONTENT_LENGTH} characters
     *
     * @return {@link WebhookMessageEditAction}
     */
    @Nonnull
    @CheckReturnValue
    default WebhookMessageEditAction<T> editMessageById(long messageId, @Nonnull String content)
    {
        return editMessageById(Long.toUnsignedString(messageId), content);
    }

    /**
     * Edit an existing message sent by this webhook.
     *
     * <p>If this is an {@link net.dv8tion.jda.api.interactions.InteractionHook InteractionHook} this method will be delayed until the interaction is acknowledged.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_WEBHOOK UNKNOWN_WEBHOOK}
     *     <br>The webhook is no longer available, either it was deleted or in case of interactions it expired.</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The message for that id does not exist</li>
     * </ul>
     *
     * @param  messageId
     *         The message id. For interactions this supports {@code "@original"} to edit the source message of the interaction.
     * @param  message
     *         The {@link MessageEditData} containing the update information
     *
     * @throws IllegalArgumentException
     *         If the provided message is null
     *
     * @return {@link WebhookMessageEditAction}
     *
     * @see    net.dv8tion.jda.api.utils.messages.MessageEditBuilder MessageEditBuilder
     */
    @Nonnull
    @CheckReturnValue
    WebhookMessageEditAction<T> editMessageById(@Nonnull String messageId, @Nonnull MessageEditData message);

    /**
     * Edit an existing message sent by this webhook.
     *
     * <p>If this is an {@link net.dv8tion.jda.api.interactions.InteractionHook InteractionHook} this method will be delayed until the interaction is acknowledged.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_WEBHOOK UNKNOWN_WEBHOOK}
     *     <br>The webhook is no longer available, either it was deleted or in case of interactions it expired.</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The message for that id does not exist</li>
     * </ul>
     *
     * @param  messageId
     *         The message id. For interactions this supports {@code "@original"} to edit the source message of the interaction.
     * @param  message
     *         The {@link MessageEditData} containing the update information
     *
     * @throws IllegalArgumentException
     *         If the provided message is null
     *
     * @return {@link WebhookMessageEditAction}
     *
     * @see    net.dv8tion.jda.api.utils.messages.MessageEditBuilder MessageEditBuilder
     */
    @Nonnull
    @CheckReturnValue
    default WebhookMessageEditAction<T> editMessageById(long messageId, MessageEditData message)
    {
        return editMessageById(Long.toUnsignedString(messageId), message);
    }

    /**
     * Edit an existing message sent by this webhook.
     *
     * <p>If this is an {@link net.dv8tion.jda.api.interactions.InteractionHook InteractionHook} this method will be delayed until the interaction is acknowledged.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_WEBHOOK UNKNOWN_WEBHOOK}
     *     <br>The webhook is no longer available, either it was deleted or in case of interactions it expired.</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The message for that id does not exist</li>
     * </ul>
     *
     * @param  messageId
     *         The message id. For interactions this supports {@code "@original"} to edit the source message of the interaction.
     * @param  format
     *         Format string for the message content
     * @param  args
     *         Format arguments for the content
     *
     * @throws IllegalArgumentException
     *         If the formatted string is null or longer than {@link Message#MAX_CONTENT_LENGTH} characters
     *
     * @return {@link WebhookMessageEditAction}
     */
    @Nonnull
    @CheckReturnValue
    default WebhookMessageEditAction<T> editMessageFormatById(@Nonnull String messageId, @Nonnull String format, @Nonnull Object... args)
    {
        Checks.notNull(format, "Format String");
        return editMessageById(messageId, String.format(format, args));
    }

    /**
     * Edit an existing message sent by this webhook.
     *
     * <p>If this is an {@link net.dv8tion.jda.api.interactions.InteractionHook InteractionHook} this method will be delayed until the interaction is acknowledged.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_WEBHOOK UNKNOWN_WEBHOOK}
     *     <br>The webhook is no longer available, either it was deleted or in case of interactions it expired.</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The message for that id does not exist</li>
     * </ul>
     *
     * @param  messageId
     *         The message id. For interactions this supports {@code "@original"} to edit the source message of the interaction.
     * @param  format
     *         Format string for the message content
     * @param  args
     *         Format arguments for the content
     *
     * @throws IllegalArgumentException
     *         If the formatted string is null or longer than {@link Message#MAX_CONTENT_LENGTH} characters
     *
     * @return {@link WebhookMessageEditAction}
     */
    @Nonnull
    @CheckReturnValue
    default WebhookMessageEditAction<T> editMessageFormatById(long messageId, @Nonnull String format, @Nonnull Object... args)
    {
        return editMessageFormatById(Long.toUnsignedString(messageId), format, args);
    }

    /**
     * Edit an existing message sent by this webhook.
     *
     * <p>If this is an {@link net.dv8tion.jda.api.interactions.InteractionHook InteractionHook} this method will be delayed until the interaction is acknowledged.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_WEBHOOK UNKNOWN_WEBHOOK}
     *     <br>The webhook is no longer available, either it was deleted or in case of interactions it expired.</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The message for that id does not exist</li>
     * </ul>
     *
     * @param  messageId
     *         The message id. For interactions this supports {@code "@original"} to edit the source message of the interaction.
     * @param  embeds
     *         {@link MessageEmbed MessageEmbeds} to use (up to {@value Message#MAX_EMBED_COUNT} in total)
     *
     * @throws IllegalArgumentException
     *         If null or more than {@value Message#MAX_EMBED_COUNT} embeds are provided
     *
     * @return {@link WebhookMessageEditAction}
     */
    @Nonnull
    @CheckReturnValue
    WebhookMessageEditAction<T> editMessageEmbedsById(@Nonnull String messageId, @Nonnull Collection<? extends MessageEmbed> embeds);

    /**
     * Edit an existing message sent by this webhook.
     *
     * <p>If this is an {@link net.dv8tion.jda.api.interactions.InteractionHook InteractionHook} this method will be delayed until the interaction is acknowledged.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_WEBHOOK UNKNOWN_WEBHOOK}
     *     <br>The webhook is no longer available, either it was deleted or in case of interactions it expired.</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The message for that id does not exist</li>
     * </ul>
     *
     * @param  messageId
     *         The message id. For interactions this supports {@code "@original"} to edit the source message of the interaction.
     * @param  embeds
     *         {@link MessageEmbed MessageEmbeds} to use (up to {@value Message#MAX_EMBED_COUNT} in total)
     *
     * @throws IllegalArgumentException
     *         If null or more than {@value Message#MAX_EMBED_COUNT} embeds are provided
     *
     * @return {@link WebhookMessageEditAction}
     */
    @Nonnull
    @CheckReturnValue
    default WebhookMessageEditAction<T> editMessageEmbedsById(long messageId, @Nonnull Collection<? extends MessageEmbed> embeds)
    {
        return editMessageEmbedsById(Long.toUnsignedString(messageId), embeds);
    }

    /**
     * Edit an existing message sent by this webhook.
     *
     * <p>If this is an {@link net.dv8tion.jda.api.interactions.InteractionHook InteractionHook} this method will be delayed until the interaction is acknowledged.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_WEBHOOK UNKNOWN_WEBHOOK}
     *     <br>The webhook is no longer available, either it was deleted or in case of interactions it expired.</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The message for that id does not exist</li>
     * </ul>
     *
     * @param  messageId
     *         The message id. For interactions this supports {@code "@original"} to edit the source message of the interaction.
     * @param  embeds
     *         The new {@link MessageEmbed MessageEmbeds} to use
     *
     * @throws IllegalArgumentException
     *         If null or more than {@value Message#MAX_EMBED_COUNT} embeds are provided
     *
     * @return {@link WebhookMessageEditAction}
     */
    @Nonnull
    @CheckReturnValue
    default WebhookMessageEditAction<T> editMessageEmbedsById(@Nonnull String messageId, @Nonnull MessageEmbed... embeds)
    {
        Checks.noneNull(embeds, "MessageEmbeds");
        return editMessageEmbedsById(messageId, Arrays.asList(embeds));
    }

    /**
     * Edit an existing message sent by this webhook.
     *
     * <p>If this is an {@link net.dv8tion.jda.api.interactions.InteractionHook InteractionHook} this method will be delayed until the interaction is acknowledged.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_WEBHOOK UNKNOWN_WEBHOOK}
     *     <br>The webhook is no longer available, either it was deleted or in case of interactions it expired.</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The message for that id does not exist</li>
     * </ul>
     *
     * @param  messageId
     *         The message id. For interactions this supports {@code "@original"} to edit the source message of the interaction.
     * @param  embeds
     *         The new {@link MessageEmbed MessageEmbeds} to use
     *
     * @throws IllegalArgumentException
     *         If null or more than {@value Message#MAX_EMBED_COUNT} embeds are provided
     *
     * @return {@link WebhookMessageEditAction}
     */
    @Nonnull
    @CheckReturnValue
    default WebhookMessageEditAction<T> editMessageEmbedsById(long messageId, @Nonnull MessageEmbed... embeds)
    {
        return editMessageEmbedsById(Long.toUnsignedString(messageId), embeds);
    }

    /**
     * Edit an existing message sent by this webhook.
     *
     * <p>If this is an {@link net.dv8tion.jda.api.interactions.InteractionHook InteractionHook} this method will be delayed until the interaction is acknowledged.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_WEBHOOK UNKNOWN_WEBHOOK}
     *     <br>The webhook is no longer available, either it was deleted or in case of interactions it expired.</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The message for that id does not exist</li>
     * </ul>
     *
     * @param  messageId
     *         The message id. For interactions this supports {@code "@original"} to edit the source message of the interaction.
     * @param  components
     *         The new component layouts for this message, such as {@link ActionRow ActionRows}
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code null} is provided</li>
     *             <li>If any of the components is not {@link LayoutComponent#isMessageCompatible() message compatible}</li>
     *             <li>If more than {@value Message#MAX_COMPONENT_COUNT} component layouts are provided</li>
     *         </ul>
     *
     * @return {@link WebhookMessageEditAction}
     */
    @Nonnull
    @CheckReturnValue
    WebhookMessageEditAction<T> editMessageComponentsById(@Nonnull String messageId, @Nonnull Collection<? extends LayoutComponent> components);

    /**
     * Edit an existing message sent by this webhook.
     *
     * <p>If this is an {@link net.dv8tion.jda.api.interactions.InteractionHook InteractionHook} this method will be delayed until the interaction is acknowledged.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_WEBHOOK UNKNOWN_WEBHOOK}
     *     <br>The webhook is no longer available, either it was deleted or in case of interactions it expired.</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The message for that id does not exist</li>
     * </ul>
     *
     * @param  messageId
     *         The message id. For interactions this supports {@code "@original"} to edit the source message of the interaction.
     * @param  components
     *         The new component layouts for this message, such as {@link ActionRow ActionRows}
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code null} is provided</li>
     *             <li>If any of the components is not {@link LayoutComponent#isMessageCompatible() message compatible}</li>
     *             <li>If more than {@value Message#MAX_COMPONENT_COUNT} component layouts are provided</li>
     *         </ul>
     *
     * @return {@link WebhookMessageEditAction}
     */
    @Nonnull
    @CheckReturnValue
    default WebhookMessageEditAction<T> editMessageComponentsById(long messageId, @Nonnull Collection<? extends LayoutComponent> components)
    {
        return editMessageComponentsById(Long.toUnsignedString(messageId), components);
    }

    /**
     * Edit an existing message sent by this webhook.
     *
     * <p>If this is an {@link net.dv8tion.jda.api.interactions.InteractionHook InteractionHook} this method will be delayed until the interaction is acknowledged.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_WEBHOOK UNKNOWN_WEBHOOK}
     *     <br>The webhook is no longer available, either it was deleted or in case of interactions it expired.</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The message for that id does not exist</li>
     * </ul>
     *
     * @param  messageId
     *         The message id. For interactions this supports {@code "@original"} to edit the source message of the interaction.
     * @param  components
     *         The new component layouts for this message, such as {@link ActionRow ActionRows}
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code null} is provided</li>
     *             <li>If any of the components is not {@link LayoutComponent#isMessageCompatible() message compatible}</li>
     *             <li>If more than {@value Message#MAX_COMPONENT_COUNT} component layouts are provided</li>
     *         </ul>
     *
     * @return {@link WebhookMessageEditAction}
     */
    @Nonnull
    @CheckReturnValue
    default WebhookMessageEditAction<T> editMessageComponentsById(@Nonnull String messageId, @Nonnull LayoutComponent... components)
    {
        Checks.noneNull(components, "LayoutComponents");
        return editMessageComponentsById(messageId, Arrays.asList(components));
    }

    /**
     * Edit an existing message sent by this webhook.
     *
     * <p>If this is an {@link net.dv8tion.jda.api.interactions.InteractionHook InteractionHook} this method will be delayed until the interaction is acknowledged.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_WEBHOOK UNKNOWN_WEBHOOK}
     *     <br>The webhook is no longer available, either it was deleted or in case of interactions it expired.</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The message for that id does not exist</li>
     * </ul>
     *
     * @param  messageId
     *         The message id. For interactions this supports {@code "@original"} to edit the source message of the interaction.
     * @param  components
     *         The new component layouts for this message, such as {@link ActionRow ActionRows}
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code null} is provided</li>
     *             <li>If any of the components is not {@link LayoutComponent#isMessageCompatible() message compatible}</li>
     *             <li>If more than {@value Message#MAX_COMPONENT_COUNT} component layouts are provided</li>
     *         </ul>
     *
     * @return {@link WebhookMessageEditAction}
     */
    @Nonnull
    @CheckReturnValue
    default WebhookMessageEditAction<T> editMessageComponentsById(long messageId, @Nonnull LayoutComponent... components)
    {
        return editMessageComponentsById(Long.toUnsignedString(messageId), components);
    }


    /**
     * Edit an existing message sent by this webhook.
     *
     * <p>If this is an {@link net.dv8tion.jda.api.interactions.InteractionHook InteractionHook} this method will be delayed until the interaction is acknowledged.
     *
     * <p><b>Resource Handling Note:</b> Once the request is handed off to the requester, for example when you call {@link RestAction#queue()},
     * the requester will automatically clean up all opened files by itself. You are only responsible to close them yourself if it is never handed off properly.
     * For instance, if an exception occurs after using {@link FileUpload#fromData(File)}, before calling {@link RestAction#queue()}.
     * You can safely use a try-with-resources to handle this, since {@link FileUpload#close()} becomes ineffective once the request is handed off.
     *
     * @param  messageId
     *         The message id. For interactions this supports {@code "@original"} to edit the source message of the interaction.
     * @param  attachments
     *         The new attachments of the message (Can be {@link FileUpload FileUploads} or {@link net.dv8tion.jda.api.utils.AttachmentUpdate AttachmentUpdates})
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return {@link MessageEditCallbackAction} that can be used to further update the message
     *
     * @see    AttachedFile#fromAttachment(Message.Attachment)
     * @see    FileUpload#fromData(InputStream, String)
     */
    @Nonnull
    @CheckReturnValue
    WebhookMessageEditAction<T> editMessageAttachmentsById(@Nonnull String messageId, @Nonnull Collection<? extends AttachedFile> attachments);

    /**
     * Edit an existing message sent by this webhook.
     *
     * <p>If this is an {@link net.dv8tion.jda.api.interactions.InteractionHook InteractionHook} this method will be delayed until the interaction is acknowledged.
     *
     * <p><b>Resource Handling Note:</b> Once the request is handed off to the requester, for example when you call {@link RestAction#queue()},
     * the requester will automatically clean up all opened files by itself. You are only responsible to close them yourself if it is never handed off properly.
     * For instance, if an exception occurs after using {@link FileUpload#fromData(File)}, before calling {@link RestAction#queue()}.
     * You can safely use a try-with-resources to handle this, since {@link FileUpload#close()} becomes ineffective once the request is handed off.
     *
     * @param  messageId
     *         The message id. For interactions this supports {@code "@original"} to edit the source message of the interaction.
     * @param  attachments
     *         The new attachments of the message (Can be {@link FileUpload FileUploads} or {@link net.dv8tion.jda.api.utils.AttachmentUpdate AttachmentUpdates})
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return {@link MessageEditCallbackAction} that can be used to further update the message
     *
     * @see    AttachedFile#fromAttachment(Message.Attachment)
     * @see    FileUpload#fromData(InputStream, String)
     */
    @Nonnull
    @CheckReturnValue
    default WebhookMessageEditAction<T> editMessageAttachmentsById(@Nonnull String messageId, @Nonnull AttachedFile... attachments)
    {
        Checks.noneNull(attachments, "Attachments");
        return editMessageAttachmentsById(messageId, Arrays.asList(attachments));
    }

    /**
     * Edit an existing message sent by this webhook.
     *
     * <p>If this is an {@link net.dv8tion.jda.api.interactions.InteractionHook InteractionHook} this method will be delayed until the interaction is acknowledged.
     *
     * <p><b>Resource Handling Note:</b> Once the request is handed off to the requester, for example when you call {@link RestAction#queue()},
     * the requester will automatically clean up all opened files by itself. You are only responsible to close them yourself if it is never handed off properly.
     * For instance, if an exception occurs after using {@link FileUpload#fromData(File)}, before calling {@link RestAction#queue()}.
     * You can safely use a try-with-resources to handle this, since {@link FileUpload#close()} becomes ineffective once the request is handed off.
     *
     * @param  messageId
     *         The message id. For interactions this supports {@code "@original"} to edit the source message of the interaction.
     * @param  attachments
     *         The new attachments of the message (Can be {@link FileUpload FileUploads} or {@link net.dv8tion.jda.api.utils.AttachmentUpdate AttachmentUpdates})
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return {@link MessageEditCallbackAction} that can be used to further update the message
     *
     * @see    AttachedFile#fromAttachment(Message.Attachment)
     * @see    FileUpload#fromData(InputStream, String)
     */
    @Nonnull
    @CheckReturnValue
    default WebhookMessageEditAction<T> editMessageAttachmentsById(long messageId, @Nonnull Collection<? extends AttachedFile> attachments)
    {
        return editMessageAttachmentsById(Long.toUnsignedString(messageId), attachments);
    }

    /**
     * Edit an existing message sent by this webhook.
     *
     * <p>If this is an {@link net.dv8tion.jda.api.interactions.InteractionHook InteractionHook} this method will be delayed until the interaction is acknowledged.
     *
     * <p><b>Resource Handling Note:</b> Once the request is handed off to the requester, for example when you call {@link RestAction#queue()},
     * the requester will automatically clean up all opened files by itself. You are only responsible to close them yourself if it is never handed off properly.
     * For instance, if an exception occurs after using {@link FileUpload#fromData(File)}, before calling {@link RestAction#queue()}.
     * You can safely use a try-with-resources to handle this, since {@link FileUpload#close()} becomes ineffective once the request is handed off.
     *
     * @param  messageId
     *         The message id. For interactions this supports {@code "@original"} to edit the source message of the interaction.
     * @param  attachments
     *         The new attachments of the message (Can be {@link FileUpload FileUploads} or {@link net.dv8tion.jda.api.utils.AttachmentUpdate AttachmentUpdates})
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return {@link MessageEditCallbackAction} that can be used to further update the message
     *
     * @see    AttachedFile#fromAttachment(Message.Attachment)
     * @see    FileUpload#fromData(InputStream, String)
     */
    @Nonnull
    @CheckReturnValue
    default WebhookMessageEditAction<T> editMessageAttachmentsById(long messageId, @Nonnull AttachedFile... attachments)
    {
        return editMessageAttachmentsById(Long.toUnsignedString(messageId), attachments);
    }


    /**
     * Delete a message from this webhook.
     *
     * <p>If this is an {@link net.dv8tion.jda.api.interactions.InteractionHook InteractionHook} this method will be delayed until the interaction is acknowledged.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_WEBHOOK UNKNOWN_WEBHOOK}
     *     <br>The webhook is no longer available, either it was deleted or in case of interactions it expired.</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The message for that id does not exist</li>
     * </ul>
     *
     * @param  messageId
     *         The id for the message to delete
     *
     * @throws IllegalArgumentException
     *         If the provided message id is null or not a valid snowflake
     *
     * @return {@link RestAction}
     */
    @Nonnull
    @CheckReturnValue
    RestAction<Void> deleteMessageById(@Nonnull String messageId);

    /**
     * Delete a message from this webhook.
     *
     * <p>If this is an {@link net.dv8tion.jda.api.interactions.InteractionHook InteractionHook} this method will be delayed until the interaction is acknowledged.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_WEBHOOK UNKNOWN_WEBHOOK}
     *     <br>The webhook is no longer available, either it was deleted or in case of interactions it expired.</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The message for that id does not exist</li>
     * </ul>
     *
     * @param  messageId
     *         The id for the message to delete
     *
     * @return {@link RestAction}
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Void> deleteMessageById(long messageId)
    {
        return deleteMessageById(Long.toUnsignedString(messageId));
    }

//    @Nonnull
//    static WebhookClient<WebhookMessageAction> createClient(@Nonnull JDA api, @Nonnull String url)
//    {
//        Checks.notNull(url, "URL");
//        Matcher matcher = Webhook.WEBHOOK_URL.matcher(url);
//        if (!matcher.matches())
//            throw new IllegalArgumentException("Provided invalid webhook URL");
//        String id = matcher.group(1);
//        String token = matcher.group(2);
//        return createClient(api, id, token);
//    }
//
//    @Nonnull
//    static WebhookClient<WebhookMessageAction> createClient(@Nonnull JDA api, @Nonnull String webhookId, @Nonnull String webhookToken)
//    {
//        Checks.notNull(api, "JDA");
//        Checks.notBlank(webhookToken, "Token");
//        return new AbstractWebhookClient<WebhookMessageAction>(MiscUtil.parseSnowflake(webhookId), webhookToken, api)
//        {
//            @Override
//            public WebhookMessageAction sendRequest()
//            {
//                Route.CompiledRoute route = Route.Webhooks.EXECUTE_WEBHOOK.compile(webhookId, webhookToken);
//                route = route.withQueryParams("wait", "true");
//                WebhookMessageActionImpl action = new WebhookMessageActionImpl(api, route);
//                action.run();
//                return action;
//            }
//
//            @Override
//            public WebhookMessageAction editRequest(String messageId)
//            {
//                Checks.isSnowflake(messageId);
//                Route.CompiledRoute route = Route.Webhooks.EXECUTE_WEBHOOK_EDIT.compile(webhookId, webhookToken, messageId);
//                route = route.withQueryParams("wait", "true");
//                WebhookMessageActionImpl action = new WebhookMessageActionImpl(api, route);
//                action.run();
//                return action;
//            }
//        };
//    }
}
