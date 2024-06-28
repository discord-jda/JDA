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

package net.dv8tion.jda.api.interactions;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.WebhookClient;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.interactions.callbacks.IMessageEditCallback;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;
import net.dv8tion.jda.api.utils.AttachedFile;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.dv8tion.jda.internal.interactions.InteractionHookImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;

/**
 * Webhook API for an interaction. Valid for up to 15 minutes after the interaction.
 * <br>This can be used to send followup messages or edit the original message of an interaction.
 *
 * <p>The interaction has to be acknowledged before any of these actions can be performed.
 * First, you need to call one of:
 * <ul>
 *     <li>{@link IReplyCallback#deferReply() deferReply(...)}</li>
 *     <li>{@link IReplyCallback#reply(String) reply(...)}</li>
 *     <li>{@link IMessageEditCallback#deferEdit() deferEdit()}</li>
 *     <li>{@link IMessageEditCallback#editMessage(String) editMessage(...)}</li>
 * </ul>
 *
 * <p>When {@link IReplyCallback#deferReply()} is used, the first message will act identically to {@link #editOriginal(String) editOriginal(...)}.
 * This means that you cannot make your deferred reply ephemeral through this interaction hook.
 * You need to specify whether your reply is ephemeral or not directly in {@link IReplyCallback#deferReply(boolean) deferReply(boolean)}.
 *
 * @see IReplyCallback
 * @see IMessageEditCallback
 * @see #editOriginal(String)
 * @see #deleteOriginal()
 * @see #sendMessage(String)
 */
public interface InteractionHook extends WebhookClient<Message>
{
    /**
     * The interaction attached to this hook.
     * 
     * @throws IllegalStateException
     *         If this instance was created through {@link #from(JDA, String)}
     *
     * @return The {@link Interaction}
     */
    @Nonnull
    Interaction getInteraction();

    /**
     * The unix millisecond timestamp for the expiration of this interaction hook.
     * <br>An interaction hook expires after 15 minutes of its creation.
     *
     * @return The timestamp in millisecond precision
     *
     * @see    System#currentTimeMillis()
     * @see    #isExpired()
     */
    long getExpirationTimestamp();

    /**
     * Whether this interaction has expired.
     * <br>An interaction hook is only valid for 15 minutes.
     *
     * @return True, if this interaction hook has expired
     *
     * @see    #getExpirationTimestamp()
     */
    default boolean isExpired()
    {
        return System.currentTimeMillis() > getExpirationTimestamp();
    }

    /**
     * Whether messages sent from this interaction hook should be ephemeral by default.
     * <br>This does not affect message updates, including deferred replies sent with {@link #sendMessage(String) sendMessage(...)} methods.
     * <br>When a message is ephemeral, it will only be visible to the user that used the interaction.
     *
     * <p>Ephemeral messages have some limitations and will be removed once the user restarts their client.
     * <br>Limitations:
     * <ul>
     *     <li>Cannot be reacted to</li>
     *     <li>Cannot be {@link net.dv8tion.jda.api.entities.channel.middleman.MessageChannel#retrieveMessageById(long) retrieved by ID}</li>
     * </ul>
     *
     * <b>Note:</b> Your message will always appear ephemeral
     * if the guild has {@link net.dv8tion.jda.api.Permission#USE_EXTERNAL_APPLICATIONS Permission.USE_EXTERNAL_APPLICATIONS} disabled.
     *
     * @param  ephemeral
     *         True if messages should be ephemeral
     *
     * @return The same interaction hook instance
     */
    @Nonnull
    InteractionHook setEphemeral(boolean ephemeral);

    /**
     * The JDA instance for this interaction
     *
     * @return The JDA instance
     */
    @Nonnull
    JDA getJDA();

    /**
     * Retrieves the original reply to this interaction.
     * <br>This doesn't work for ephemeral messages and will always cause an unknown message error response.
     *
     * @return {@link RestAction} - Type: {@link Message}
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Message> retrieveOriginal()
    {
        return retrieveMessageById("@original");
    }

    /**
     * Edit the source message sent by this interaction.
     * <br>For {@link IMessageEditCallback#editComponents(Collection)} and {@link IMessageEditCallback#deferEdit()} this will be the message the components are attached to.
     * For {@link IReplyCallback#deferReply()} and {@link IReplyCallback#reply(String)} this will be the reply message instead.
     *
     * <p>This method will be delayed until the interaction is acknowledged.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_WEBHOOK UNKNOWN_WEBHOOK}
     *     <br>The webhook is no longer available, either it was deleted or in case of interactions it expired.</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The message for that id does not exist</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_AUTOMOD MESSAGE_BLOCKED_BY_AUTOMOD}
     *     <br>If this message was blocked by an {@link net.dv8tion.jda.api.entities.automod.AutoModRule AutoModRule}</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER}
     *     <br>If this message was blocked by the harmful link filter</li>
     * </ul>
     *
     * @param  content
     *         The new message content to use
     *
     * @throws IllegalArgumentException
     *         If the provided content is null, empty, or longer than {@link Message#MAX_CONTENT_LENGTH}
     *
     * @return {@link WebhookMessageEditAction}
     */
    @Nonnull
    @CheckReturnValue
    default WebhookMessageEditAction<Message> editOriginal(@Nonnull String content)
    {
        return editMessageById("@original", content);
    }

    /**
     * Edit the source message sent by this interaction.
     * <br>For {@link IMessageEditCallback#editComponents(Collection)} and {@link IMessageEditCallback#deferEdit()} this will be the message the components are attached to.
     * For {@link IReplyCallback#deferReply()} and {@link IReplyCallback#reply(String)} this will be the reply message instead.
     *
     * <p>This method will be delayed until the interaction is acknowledged.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_WEBHOOK UNKNOWN_WEBHOOK}
     *     <br>The webhook is no longer available, either it was deleted or in case of interactions it expired.</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The message for that id does not exist</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_AUTOMOD MESSAGE_BLOCKED_BY_AUTOMOD}
     *     <br>If this message was blocked by an {@link net.dv8tion.jda.api.entities.automod.AutoModRule AutoModRule}</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER}
     *     <br>If this message was blocked by the harmful link filter</li>
     * </ul>
     *
     * @param  components
     *         The new component layouts for this message, such as {@link ActionRow ActionRows}
     *
     * @throws IllegalArgumentException
     *         If the provided components are null, or more than 5 layouts are provided
     *
     * @return {@link WebhookMessageEditAction}
     */
    @Nonnull
    @CheckReturnValue
    default WebhookMessageEditAction<Message> editOriginalComponents(@Nonnull Collection<? extends LayoutComponent> components)
    {
        return editMessageComponentsById("@original", components);
    }

    /**
     * Edit the source message sent by this interaction.
     * <br>For {@link IMessageEditCallback#editComponents(Collection)} and {@link IMessageEditCallback#deferEdit()} this will be the message the components are attached to.
     * For {@link IReplyCallback#deferReply()} and {@link IReplyCallback#reply(String)} this will be the reply message instead.
     *
     * <p>This method will be delayed until the interaction is acknowledged.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_WEBHOOK UNKNOWN_WEBHOOK}
     *     <br>The webhook is no longer available, either it was deleted or in case of interactions it expired.</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The message for that id does not exist</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_AUTOMOD MESSAGE_BLOCKED_BY_AUTOMOD}
     *     <br>If this message was blocked by an {@link net.dv8tion.jda.api.entities.automod.AutoModRule AutoModRule}</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER}
     *     <br>If this message was blocked by the harmful link filter</li>
     * </ul>
     *
     * @param  components
     *         The new component layouts for this message, such as {@link ActionRow ActionRows}
     *
     * @throws IllegalArgumentException
     *         If the provided components are null, or more than 5 layouts are provided
     *
     * @return {@link WebhookMessageEditAction}
     */
    @Nonnull
    @CheckReturnValue
    default WebhookMessageEditAction<Message> editOriginalComponents(@Nonnull LayoutComponent... components)
    {
        return editMessageComponentsById("@original", components);
    }

    /**
     * Edit the source message sent by this interaction.
     * <br>For {@link IMessageEditCallback#editComponents(Collection)} and {@link IMessageEditCallback#deferEdit()} this will be the message the components are attached to.
     * For {@link IReplyCallback#deferReply()} and {@link IReplyCallback#reply(String)} this will be the reply message instead.
     *
     * <p>This method will be delayed until the interaction is acknowledged.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_WEBHOOK UNKNOWN_WEBHOOK}
     *     <br>The webhook is no longer available, either it was deleted or in case of interactions it expired.</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The message for that id does not exist</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_AUTOMOD MESSAGE_BLOCKED_BY_AUTOMOD}
     *     <br>If this message was blocked by an {@link net.dv8tion.jda.api.entities.automod.AutoModRule AutoModRule}</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER}
     *     <br>If this message was blocked by the harmful link filter</li>
     * </ul>
     *
     * @param  embeds
     *         {@link MessageEmbed MessageEmbeds} to use (up to {@value Message#MAX_EMBED_COUNT} in total)
     *
     * @throws IllegalArgumentException
     *         If the provided embeds are null, or more than {@value Message#MAX_EMBED_COUNT}
     *
     * @return {@link WebhookMessageEditAction}
     */
    @Nonnull
    @CheckReturnValue
    default WebhookMessageEditAction<Message> editOriginalEmbeds(@Nonnull Collection<? extends MessageEmbed> embeds)
    {
        return editMessageEmbedsById("@original", embeds);
    }

    /**
     * Edit the source message sent by this interaction.
     * <br>For {@link IMessageEditCallback#editComponents(Collection)} and {@link IMessageEditCallback#deferEdit()} this will be the message the components are attached to.
     * For {@link IReplyCallback#deferReply()} and {@link IReplyCallback#reply(String)} this will be the reply message instead.
     *
     * <p>This method will be delayed until the interaction is acknowledged.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_WEBHOOK UNKNOWN_WEBHOOK}
     *     <br>The webhook is no longer available, either it was deleted or in case of interactions it expired.</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The message for that id does not exist</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_AUTOMOD MESSAGE_BLOCKED_BY_AUTOMOD}
     *     <br>If this message was blocked by an {@link net.dv8tion.jda.api.entities.automod.AutoModRule AutoModRule}</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER}
     *     <br>If this message was blocked by the harmful link filter</li>
     * </ul>
     *
     * @param  embeds
     *         The new {@link MessageEmbed MessageEmbeds} to use
     *
     * @throws IllegalArgumentException
     *         If the provided embeds are null, or more than 10
     *
     * @return {@link WebhookMessageEditAction}
     */
    @Nonnull
    @CheckReturnValue
    default WebhookMessageEditAction<Message> editOriginalEmbeds(@Nonnull MessageEmbed... embeds)
    {
        return editMessageEmbedsById("@original", embeds);
    }

    /**
     * Edit the source message sent by this interaction.
     * <br>For {@link IMessageEditCallback#editComponents(Collection)} and {@link IMessageEditCallback#deferEdit()} this will be the message the components are attached to.
     * For {@link IReplyCallback#deferReply()} and {@link IReplyCallback#reply(String)} this will be the reply message instead.
     *
     * <p>This method will be delayed until the interaction is acknowledged.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_WEBHOOK UNKNOWN_WEBHOOK}
     *     <br>The webhook is no longer available, either it was deleted or in case of interactions it expired.</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The message for that id does not exist</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_AUTOMOD MESSAGE_BLOCKED_BY_AUTOMOD}
     *     <br>If this message was blocked by an {@link net.dv8tion.jda.api.entities.automod.AutoModRule AutoModRule}</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER}
     *     <br>If this message was blocked by the harmful link filter</li>
     * </ul>
     *
     * @param  message
     *         The new message to replace the existing message with
     *
     * @throws IllegalArgumentException
     *         If the provided message is null
     *
     * @return {@link WebhookMessageEditAction}
     */
    @Nonnull
    @CheckReturnValue
    default WebhookMessageEditAction<Message> editOriginal(@Nonnull MessageEditData message)
    {
        return editMessageById("@original", message);
    }

    /**
     * Edit the source message sent by this interaction.
     * <br>For {@link IMessageEditCallback#editComponents(Collection)} and {@link IMessageEditCallback#deferEdit()} this will be the message the components are attached to.
     * For {@link IReplyCallback#deferReply()} and {@link IReplyCallback#reply(String)} this will be the reply message instead.
     *
     * <p>This method will be delayed until the interaction is acknowledged.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_WEBHOOK UNKNOWN_WEBHOOK}
     *     <br>The webhook is no longer available, either it was deleted or in case of interactions it expired.</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The message for that id does not exist</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_AUTOMOD MESSAGE_BLOCKED_BY_AUTOMOD}
     *     <br>If this message was blocked by an {@link net.dv8tion.jda.api.entities.automod.AutoModRule AutoModRule}</li>
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
     *         If the formatted string is null, empty, or longer than {@link Message#MAX_CONTENT_LENGTH}
     *
     * @return {@link WebhookMessageEditAction}
     */
    @Nonnull
    @CheckReturnValue
    default WebhookMessageEditAction<Message> editOriginalFormat(@Nonnull String format, @Nonnull Object... args)
    {
        Checks.notNull(format, "Format String");
        return editOriginal(String.format(format, args));
    }

    /**
     * Edit the source message sent by this interaction.
     * <br>For {@link IMessageEditCallback#editComponents(Collection)} and {@link IMessageEditCallback#deferEdit()} this will be the message the components are attached to.
     * For {@link IReplyCallback#deferReply()} and {@link IReplyCallback#reply(String)} this will be the reply message instead.
     *
     * <p>This method will be delayed until the interaction is acknowledged.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#REQUEST_ENTITY_TOO_LARGE REQUEST_ENTITY_TOO_LARGE}
     *     <br>If any of the provided files is bigger than {@link Guild#getMaxFileSize()}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link GuildMessageChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted. This might also be triggered for ephemeral messages.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_AUTOMOD MESSAGE_BLOCKED_BY_AUTOMOD}
     *     <br>If this message was blocked by an {@link net.dv8tion.jda.api.entities.automod.AutoModRule AutoModRule}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER}
     *     <br>If this message was blocked by the harmful link filter</li>
     * </ul>
     *
     * <p><b>Resource Handling Note:</b> Once the request is handed off to the requester, for example when you call {@link RestAction#queue()},
     * the requester will automatically clean up all opened files by itself. You are only responsible to close them yourself if it is never handed off properly.
     * For instance, if an exception occurs after using {@link FileUpload#fromData(File)}, before calling {@link RestAction#queue()}.
     * You can safely use a try-with-resources to handle this, since {@link FileUpload#close()} becomes ineffective once the request is handed off.
     *
     * @param  attachments
     *         The new attachments of the message (Can be {@link FileUpload FileUploads} or {@link net.dv8tion.jda.api.utils.AttachmentUpdate AttachmentUpdates})
     *
     * @throws IllegalArgumentException
     *         If {@code null} is provided
     *
     * @return {@link WebhookMessageEditAction}
     *
     * @see    AttachedFile#fromAttachment(Message.Attachment)
     * @see    FileUpload#fromData(InputStream, String)
     */
    @Nonnull
    @CheckReturnValue
    default WebhookMessageEditAction<Message> editOriginalAttachments(@Nonnull Collection<? extends AttachedFile> attachments)
    {
        return editMessageAttachmentsById("@original", attachments);
    }

    /**
     * Edit the source message sent by this interaction.
     * <br>For {@link IMessageEditCallback#editComponents(Collection)} and {@link IMessageEditCallback#deferEdit()} this will be the message the components are attached to.
     * For {@link IReplyCallback#deferReply()} and {@link IReplyCallback#reply(String)} this will be the reply message instead.
     *
     * <p>This method will be delayed until the interaction is acknowledged.
     *
     * <p>The following {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#REQUEST_ENTITY_TOO_LARGE REQUEST_ENTITY_TOO_LARGE}
     *     <br>If any of the provided files is bigger than {@link Guild#getMaxFileSize()}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link Guild Guild}
     *         typically due to being kicked or removed, or after {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link GuildMessageChannel}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted. This might also be triggered for ephemeral messages.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_AUTOMOD MESSAGE_BLOCKED_BY_AUTOMOD}
     *     <br>If this message was blocked by an {@link net.dv8tion.jda.api.entities.automod.AutoModRule AutoModRule}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER}
     *     <br>If this message was blocked by the harmful link filter</li>
     * </ul>
     *
     * <p><b>Resource Handling Note:</b> Once the request is handed off to the requester, for example when you call {@link RestAction#queue()},
     * the requester will automatically clean up all opened files by itself. You are only responsible to close them yourself if it is never handed off properly.
     * For instance, if an exception occurs after using {@link FileUpload#fromData(File)}, before calling {@link RestAction#queue()}.
     * You can safely use a try-with-resources to handle this, since {@link FileUpload#close()} becomes ineffective once the request is handed off.
     *
     * @param  attachments
     *         The new attachments of the message (Can be {@link FileUpload FileUploads} or {@link net.dv8tion.jda.api.utils.AttachmentUpdate AttachmentUpdates})
     *
     * @throws IllegalArgumentException
     *         If {@code null} is provided
     *
     * @return {@link WebhookMessageEditAction}
     *
     * @see    AttachedFile#fromAttachment(Message.Attachment)
     * @see    FileUpload#fromData(InputStream, String)
     */
    @Nonnull
    @CheckReturnValue
    default WebhookMessageEditAction<Message> editOriginalAttachments(@Nonnull AttachedFile... attachments)
    {
        Checks.noneNull(attachments, "Attachments");
        return editOriginalAttachments(Arrays.asList(attachments));
    }


    /**
     * Delete the original reply.
     *
     * @return {@link RestAction}
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Void> deleteOriginal()
    {
        return deleteMessageById("@original");
    }

    /**
     * Creates an instance of {@link InteractionHook} capable of executing webhook requests.
     * <p>Messages created by this client may not have a fully accessible channel or guild available, and {@link #getInteraction()} throws.
     * The messages might report a channel of type {@link net.dv8tion.jda.api.entities.channel.ChannelType#UNKNOWN UNKNOWN},
     * in which case the channel is assumed to be inaccessible and limited to only webhook requests.
     *
     * @param  jda
     *         The JDA instance, used to handle rate-limits
     * @param  token
     *         The interaction token for the webhook
     *
     * @throws IllegalArgumentException
     *         If null is provided or the token is blank
     *
     * @return The {@link InteractionHook} instance
     */
    @Nonnull
    static InteractionHook from(@Nonnull JDA jda, @Nonnull String token)
    {
        Checks.notNull(jda, "JDA");
        Checks.notBlank(token, "Token");

        return new InteractionHookImpl(jda, token);
    }
}
