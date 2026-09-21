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

package net.dv8tion.jda.api.endpoints;

import com.google.errorprone.annotations.FormatMethod;
import com.google.errorprone.annotations.FormatString;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.components.tree.ComponentTree;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessagePollData;
import net.dv8tion.jda.api.utils.messages.MessageRequest;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;

import java.io.File;
import java.io.InputStream;
import java.util.*;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Common interface for API requests relative to messages.
 *
 * <h2>Stability note</h2>
 * The API and ABI of this interface is <b>subject to changes without warning</b>.
 *
 * @see ApiEndpoints#messages(JDA, long)
 * @see net.dv8tion.jda.api.entities.channel.middleman.MessageChannel MessageChannel
 */
public interface MessageApi {
    /**
     * Send a message to this channel.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>if this channel was deleted</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#CANNOT_SEND_TO_USER CANNOT_SEND_TO_USER}
     *     <br>If this is a {@link net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel PrivateChannel} and the currently logged in account
     *         cannot message the recipient User</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#NO_MUTUAL_GUILDS NO_MUTUAL_GUILDS}
     *     <br>If this is a {@link net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel PrivateChannel} and the currently logged in account
     *         does not share any Guilds with the recipient User</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_AUTOMOD MESSAGE_BLOCKED_BY_AUTOMOD}
     *     <br>If this message was blocked by an {@link net.dv8tion.jda.api.entities.automod.AutoModRule AutoModRule}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER}
     *     <br>If this message was blocked by the harmful link filter</li>
     * </ul>
     *
     * @param  text
     *         The message content
     *
     * @throws UnsupportedOperationException
     *         If this is a {@link PrivateChannel} and the recipient is a bot
     * @throws IllegalArgumentException
     *         If the content is null or longer than {@value Message#MAX_CONTENT_LENGTH} characters
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel} and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL} or {@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         If this is a {@link net.dv8tion.jda.api.entities.detached.IDetachableEntity#isDetached() detached} channel
     *
     * @return {@link MessageCreateAction}
     */
    @Nonnull
    @CheckReturnValue
    MessageCreateAction sendMessage(@Nonnull CharSequence text);

    /**
     * Send a message to this channel.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>if this channel was deleted</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#CANNOT_SEND_TO_USER CANNOT_SEND_TO_USER}
     *     <br>If this is a {@link net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel PrivateChannel} and the currently logged in account
     *         cannot message the recipient User</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#NO_MUTUAL_GUILDS NO_MUTUAL_GUILDS}
     *     <br>If this is a {@link net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel PrivateChannel} and the currently logged in account
     *         does not share any Guilds with the recipient User</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_AUTOMOD MESSAGE_BLOCKED_BY_AUTOMOD}
     *     <br>If this message was blocked by an {@link net.dv8tion.jda.api.entities.automod.AutoModRule AutoModRule}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER}
     *     <br>If this message was blocked by the harmful link filter</li>
     * </ul>
     *
     * @param  msg
     *         The {@link MessageCreateData} to send
     *
     * @throws UnsupportedOperationException
     *         If this is a {@link PrivateChannel} and the recipient is a bot
     * @throws IllegalArgumentException
     *         If null is provided
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel} and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL} or {@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         If this is a {@link net.dv8tion.jda.api.entities.detached.IDetachableEntity#isDetached() detached} channel
     *
     * @return {@link MessageCreateAction}
     *
     * @see    net.dv8tion.jda.api.utils.messages.MessageCreateBuilder MessageCreateBuilder
     */
    @Nonnull
    @CheckReturnValue
    MessageCreateAction sendMessage(@Nonnull MessageCreateData msg);

    /**
     * Send a message to this channel.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>if this channel was deleted</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#CANNOT_SEND_TO_USER CANNOT_SEND_TO_USER}
     *     <br>If this is a {@link net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel PrivateChannel} and the currently logged in account
     *         cannot message the recipient User</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#NO_MUTUAL_GUILDS NO_MUTUAL_GUILDS}
     *     <br>If this is a {@link net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel PrivateChannel} and the currently logged in account
     *         does not share any Guilds with the recipient User</li>
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
     * @throws UnsupportedOperationException
     *         If this is a {@link PrivateChannel} and the recipient is a bot
     * @throws IllegalArgumentException
     *         If the format string is null or the resulting content is longer than {@value Message#MAX_CONTENT_LENGTH} characters
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel} and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL} or {@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}
     * @throws java.util.IllegalFormatException
     *         If a format string contains an illegal syntax, a format
     *         specifier that is incompatible with the given arguments,
     *         insufficient arguments given the format string, or other
     *         illegal conditions.  For specification of all possible
     *         formatting errors, see the <a href="../util/Formatter.html#detail">Details</a> section of the
     *         formatter class specification.
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         If this is a {@link net.dv8tion.jda.api.entities.detached.IDetachableEntity#isDetached() detached} channel
     *
     * @return {@link MessageCreateAction}
     */
    @Nonnull
    @FormatMethod
    @CheckReturnValue
    default MessageCreateAction sendMessageFormat(@Nonnull @FormatString String format, @Nonnull Object... args) {
        Checks.notEmpty(format, "Format");
        return sendMessage(String.format(format, args));
    }

    /**
     * Send a message to this channel.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>if this channel was deleted</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#CANNOT_SEND_TO_USER CANNOT_SEND_TO_USER}
     *     <br>If this is a {@link net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel PrivateChannel} and the currently logged in account
     *         cannot message the recipient User</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#NO_MUTUAL_GUILDS NO_MUTUAL_GUILDS}
     *     <br>If this is a {@link net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel PrivateChannel} and the currently logged in account
     *         does not share any Guilds with the recipient User</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_AUTOMOD MESSAGE_BLOCKED_BY_AUTOMOD}
     *     <br>If this message was blocked by an {@link net.dv8tion.jda.api.entities.automod.AutoModRule AutoModRule}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER}
     *     <br>If this message was blocked by the harmful link filter</li>
     * </ul>
     *
     * <p><b>Example: Attachment Images</b>
     * {@snippet lang="java":
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
     * channel.sendMessageEmbeds(embed) // send the embed
     *        .addFiles(file) // add the file as attachment
     *        .queue();
     * }
     *
     * @param  embed
     *         {@link MessageEmbed} to send
     * @param  other
     *         Additional {@link MessageEmbed MessageEmbeds} to use (up to {@value Message#MAX_EMBED_COUNT})
     *
     * @throws UnsupportedOperationException
     *         If this is a {@link PrivateChannel} and the recipient is a bot
     * @throws IllegalArgumentException
     *         If any of the embeds are null, more than {@value Message#MAX_EMBED_COUNT}, or longer than {@link MessageEmbed#EMBED_MAX_LENGTH_BOT}.
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel} and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL} or {@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         If this is a {@link net.dv8tion.jda.api.entities.detached.IDetachableEntity#isDetached() detached} channel
     *
     * @return {@link MessageCreateAction}
     */
    @Nonnull
    @CheckReturnValue
    default MessageCreateAction sendMessageEmbeds(@Nonnull MessageEmbed embed, @Nonnull MessageEmbed... other) {
        Checks.notNull(embed, "MessageEmbeds");
        Checks.noneNull(other, "MessageEmbeds");
        List<MessageEmbed> embeds = new ArrayList<>(1 + other.length);
        embeds.add(embed);
        Collections.addAll(embeds, other);
        return sendMessageEmbeds(embeds);
    }

    /**
     * Send a message to this channel.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>if this channel was deleted</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#CANNOT_SEND_TO_USER CANNOT_SEND_TO_USER}
     *     <br>If this is a {@link PrivateChannel PrivateChannel} and the currently logged in account
     *         cannot message the recipient User</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#NO_MUTUAL_GUILDS NO_MUTUAL_GUILDS}
     *     <br>If this is a {@link net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel PrivateChannel} and the currently logged in account
     *         does not share any Guilds with the recipient User</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_AUTOMOD MESSAGE_BLOCKED_BY_AUTOMOD}
     *     <br>If this message was blocked by an {@link net.dv8tion.jda.api.entities.automod.AutoModRule AutoModRule}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER}
     *     <br>If this message was blocked by the harmful link filter</li>
     * </ul>
     *
     * <p><b>Example: Attachment Images</b>
     * {@snippet lang="java":
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
     * channel.sendMessageEmbeds(Collections.singleton(embed)) // send the embeds
     *        .addFiles(file) // add the file as attachment
     *        .queue();
     * }
     *
     * @param  embeds
     *         {@link MessageEmbed MessageEmbeds} to use (up to {@value Message#MAX_EMBED_COUNT})
     *
     * @throws UnsupportedOperationException
     *         If this is a {@link PrivateChannel} and the recipient is a bot
     * @throws IllegalArgumentException
     *         If any of the embeds are null, more than {@value Message#MAX_EMBED_COUNT}, or longer than {@link MessageEmbed#EMBED_MAX_LENGTH_BOT}.
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel} and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL} or {@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         If this is a {@link net.dv8tion.jda.api.entities.detached.IDetachableEntity#isDetached() detached} channel
     *
     * @return {@link MessageCreateAction}
     */
    @Nonnull
    @CheckReturnValue
    MessageCreateAction sendMessageEmbeds(@Nonnull Collection<? extends MessageEmbed> embeds);

    /**
     * Send a message to this channel.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>if this channel was deleted</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#CANNOT_SEND_TO_USER CANNOT_SEND_TO_USER}
     *     <br>If this is a {@link PrivateChannel} and the currently logged in account
     *         cannot message the recipient User</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#NO_MUTUAL_GUILDS NO_MUTUAL_GUILDS}
     *     <br>If this is a {@link net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel PrivateChannel} and the currently logged in account
     *         does not share any Guilds with the recipient User</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_AUTOMOD MESSAGE_BLOCKED_BY_AUTOMOD}
     *     <br>If this message was blocked by an {@link net.dv8tion.jda.api.entities.automod.AutoModRule AutoModRule}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER}
     *     <br>If this message was blocked by the harmful link filter</li>
     * </ul>
     *
     * @param  components
     *         The {@link MessageTopLevelComponent MessageTopLevelComponents} to send,
     *         can contain up to {@value Message#MAX_COMPONENT_COUNT} V1 components.
     *         There are no limits for {@linkplain MessageRequest#isUsingComponentsV2() V2 components}
     *         outside the {@linkplain Message#MAX_COMPONENT_COUNT_IN_COMPONENT_TREE total tree size} ({@value Message#MAX_COMPONENT_COUNT_IN_COMPONENT_TREE}).
     *
     * @throws UnsupportedOperationException
     *         If this is a {@link PrivateChannel} and the recipient is a bot
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code null} is provided</li>
     *             <li>If any of the provided components are not {@linkplain Component.Type#isMessageCompatible() compatible with messages}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel} and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL} or {@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         If this is a {@link net.dv8tion.jda.api.entities.detached.IDetachableEntity#isDetached() detached} channel
     *
     * @return {@link MessageCreateAction}
     */
    @Nonnull
    @CheckReturnValue
    MessageCreateAction sendMessageComponents(@Nonnull Collection<? extends MessageTopLevelComponent> components);

    /**
     * Send a message to this channel.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>if this channel was deleted</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#CANNOT_SEND_TO_USER CANNOT_SEND_TO_USER}
     *     <br>If this is a {@link PrivateChannel} and the currently logged in account
     *         cannot message the recipient User</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#NO_MUTUAL_GUILDS NO_MUTUAL_GUILDS}
     *     <br>If this is a {@link net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel PrivateChannel} and the currently logged in account
     *         does not share any Guilds with the recipient User</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_AUTOMOD MESSAGE_BLOCKED_BY_AUTOMOD}
     *     <br>If this message was blocked by an {@link net.dv8tion.jda.api.entities.automod.AutoModRule AutoModRule}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER}
     *     <br>If this message was blocked by the harmful link filter</li>
     * </ul>
     *
     * @param  component
     *         The {@link MessageTopLevelComponent} to send
     * @param  other
     *         Additional {@link MessageTopLevelComponent MessageTopLevelComponents} to send,
     *         can contain up to {@value Message#MAX_COMPONENT_COUNT} V1 components.
     *         There are no limits for {@linkplain MessageRequest#isUsingComponentsV2() V2 components}
     *         outside the {@linkplain Message#MAX_COMPONENT_COUNT_IN_COMPONENT_TREE total tree size} ({@value Message#MAX_COMPONENT_COUNT_IN_COMPONENT_TREE}).
     *
     * @throws UnsupportedOperationException
     *         If this is a {@link PrivateChannel} and the recipient is a bot
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code null} is provided</li>
     *             <li>If any of the provided components are not {@linkplain Component.Type#isMessageCompatible() compatible with messages}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel} and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL} or {@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         If this is a {@link net.dv8tion.jda.api.entities.detached.IDetachableEntity#isDetached() detached} channel
     *
     * @return {@link MessageCreateAction}
     */
    @Nonnull
    @CheckReturnValue
    default MessageCreateAction sendMessageComponents(
            @Nonnull MessageTopLevelComponent component, @Nonnull MessageTopLevelComponent... other) {
        Checks.notNull(component, "MessageTopLevelComponent");
        Checks.noneNull(other, "MessageTopLevelComponents");
        return sendMessageComponents(Helpers.mergeVararg(component, other));
    }

    /**
     * Send a message to this channel.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>if this channel was deleted</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#CANNOT_SEND_TO_USER CANNOT_SEND_TO_USER}
     *     <br>If this is a {@link PrivateChannel} and the currently logged in account
     *         cannot message the recipient User</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#NO_MUTUAL_GUILDS NO_MUTUAL_GUILDS}
     *     <br>If this is a {@link net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel PrivateChannel} and the currently logged in account
     *         does not share any Guilds with the recipient User</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_AUTOMOD MESSAGE_BLOCKED_BY_AUTOMOD}
     *     <br>If this message was blocked by an {@link net.dv8tion.jda.api.entities.automod.AutoModRule AutoModRule}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER}
     *     <br>If this message was blocked by the harmful link filter</li>
     * </ul>
     *
     * @param  tree
     *         The {@link ComponentTree} to send,
     *         containing up to {@value Message#MAX_COMPONENT_COUNT} V1 components.
     *         There are no limits for {@linkplain MessageRequest#isUsingComponentsV2() V2 components}
     *         outside the {@linkplain Message#MAX_COMPONENT_COUNT_IN_COMPONENT_TREE total tree size} ({@value Message#MAX_COMPONENT_COUNT_IN_COMPONENT_TREE}).
     *
     * @throws UnsupportedOperationException
     *         If this is a {@link PrivateChannel} and the recipient is a bot
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code null} is provided</li>
     *             <li>If any of the provided components are not {@linkplain Component.Type#isMessageCompatible() compatible with messages}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel} and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL} or {@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         If this is a {@link net.dv8tion.jda.api.entities.detached.IDetachableEntity#isDetached() detached} channel
     *
     * @return {@link MessageCreateAction}
     *
     * @see    net.dv8tion.jda.api.components.tree.MessageComponentTree MessageComponentTree
     */
    @Nonnull
    @CheckReturnValue
    default MessageCreateAction sendMessageComponents(@Nonnull ComponentTree<? extends MessageTopLevelComponent> tree) {
        Checks.notNull(tree, "ComponentTree");
        return sendMessageComponents(tree.getComponents());
    }

    /**
     * Send a message to this channel.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>if this channel was deleted</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#CANNOT_SEND_TO_USER CANNOT_SEND_TO_USER}
     *     <br>If this is a {@link PrivateChannel} and the currently logged in account
     *         cannot message the recipient User</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#NO_MUTUAL_GUILDS NO_MUTUAL_GUILDS}
     *     <br>If this is a {@link net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel PrivateChannel} and the currently logged in account
     *         does not share any Guilds with the recipient User</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_AUTOMOD MESSAGE_BLOCKED_BY_AUTOMOD}
     *     <br>If this message was blocked by an {@link net.dv8tion.jda.api.entities.automod.AutoModRule AutoModRule}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER}
     *     <br>If this message was blocked by the harmful link filter</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#POLL_INVALID_CHANNEL_TYPE POLL_INVALID_CHANNEL_TYPE}
     *     <br>This channel does not allow polls</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#POLL_WITH_UNUSABLE_EMOJI POLL_WITH_UNUSABLE_EMOJI}
     *     <br>This poll uses an external emoji that the bot is not allowed to use</li>
     * </ul>
     *
     * @param  poll
     *         The poll to send
     *
     * @throws UnsupportedOperationException
     *         If this is a {@link PrivateChannel} and the recipient is a bot
     * @throws IllegalArgumentException
     *         If the poll is null
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel} and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL} or {@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}
     *
     * @return {@link MessageCreateAction}
     */
    @Nonnull
    @CheckReturnValue
    MessageCreateAction sendMessagePoll(@Nonnull MessagePollData poll);

    /**
     * Send a message to this channel.
     *
     * <p><b>Resource Handling Note:</b> Once the request is handed off to the requester, for example when you call {@link RestAction#queue()},
     * the requester will automatically clean up all opened files by itself. You are only responsible to close them yourself if it is never handed off properly.
     * For instance, if an exception occurs after using {@link FileUpload#fromData(File)}, before calling {@link RestAction#queue()}.
     * You can safely use a try-with-resources to handle this, since {@link FileUpload#close()} becomes ineffective once the request is handed off.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>if this channel was deleted</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#CANNOT_SEND_TO_USER CANNOT_SEND_TO_USER}
     *     <br>If this is a {@link net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel PrivateChannel} and the currently logged in account
     *         cannot message the recipient User</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#NO_MUTUAL_GUILDS NO_MUTUAL_GUILDS}
     *     <br>If this is a {@link net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel PrivateChannel} and the currently logged in account
     *         does not share any Guilds with the recipient User</li>
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
     * <p><b>Example: Attachment Images</b>
     * {@snippet lang="java":
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
     * channel.sendFiles(Collections.singleton(file)) // send the file upload
     *        .addEmbeds(embed) // add the embed you want to reference the file with
     *        .queue();
     * }
     *
     * @param  files
     *         The {@link FileUpload FileUploads} to attach to the message
     *
     * @throws UnsupportedOperationException
     *         If this is a {@link PrivateChannel} and the recipient is a bot
     * @throws IllegalArgumentException
     *         If null is provided
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel} and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL} or {@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         If this is a {@link net.dv8tion.jda.api.entities.detached.IDetachableEntity#isDetached() detached} channel
     *
     * @return {@link MessageCreateAction}
     *
     * @see    FileUpload#fromData(InputStream, String)
     */
    @Nonnull
    @CheckReturnValue
    MessageCreateAction sendFiles(@Nonnull Collection<? extends FileUpload> files);

    /**
     * Send a message to this channel.
     *
     * <p><b>Resource Handling Note:</b> Once the request is handed off to the requester, for example when you call {@link RestAction#queue()},
     * the requester will automatically clean up all opened files by itself. You are only responsible to close them yourself if it is never handed off properly.
     * For instance, if an exception occurs after using {@link FileUpload#fromData(File)}, before calling {@link RestAction#queue()}.
     * You can safely use a try-with-resources to handle this, since {@link FileUpload#close()} becomes ineffective once the request is handed off.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>if this channel was deleted</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#CANNOT_SEND_TO_USER CANNOT_SEND_TO_USER}
     *     <br>If this is a {@link PrivateChannel PrivateChannel} and the currently logged in account
     *         cannot message the recipient User</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#NO_MUTUAL_GUILDS NO_MUTUAL_GUILDS}
     *     <br>If this is a {@link net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel PrivateChannel} and the currently logged in account
     *         does not share any Guilds with the recipient User</li>
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
     * <p><b>Example: Attachment Images</b>
     * {@snippet lang="java":
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
     * channel.sendFiles(file) // send the file upload
     *        .addEmbeds(embed) // add the embed you want to reference the file with
     *        .queue();
     * }
     *
     * @param  files
     *         The {@link FileUpload FileUploads} to attach to the message
     *
     * @throws UnsupportedOperationException
     *         If this is a {@link PrivateChannel} and the recipient is a bot
     * @throws IllegalArgumentException
     *         If null is provided
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If this is a {@link GuildMessageChannel} and this account does not have
     *         {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL} or {@link net.dv8tion.jda.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         If this is a {@link net.dv8tion.jda.api.entities.detached.IDetachableEntity#isDetached() detached} channel
     *
     * @return {@link MessageCreateAction}
     *
     * @see    FileUpload#fromData(InputStream, String)
     */
    @Nonnull
    @CheckReturnValue
    default MessageCreateAction sendFiles(@Nonnull FileUpload... files) {
        Checks.notEmpty(files, "File Collection");
        Checks.noneNull(files, "Files");
        return sendFiles(Arrays.asList(files));
    }
}
