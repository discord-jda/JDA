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
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.WebhookClient;
import net.dv8tion.jda.api.interactions.callbacks.IMessageEditCallback;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;
import net.dv8tion.jda.api.utils.AttachedFile;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.time.temporal.ChronoUnit;
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
    default long getExpirationTimestamp()
    {
        return getInteraction().getTimeCreated().plus(15, ChronoUnit.MINUTES).toEpochSecond() * 1000;
    }

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
     *     <li>Cannot contain any files/attachments</li>
     *     <li>Cannot be reacted to</li>
     *     <li>Cannot be retrieved</li>
     * </ul>
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
    RestAction<Message> retrieveOriginal();

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


    @Nonnull
    @CheckReturnValue
    default WebhookMessageEditAction<Message> editOriginalAttachments(@Nonnull Collection<? extends AttachedFile> attachments)
    {
        return editMessageAttachmentsById("@original", attachments);
    }

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
}
