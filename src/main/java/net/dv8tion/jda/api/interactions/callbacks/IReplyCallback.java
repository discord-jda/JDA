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

package net.dv8tion.jda.api.interactions.callbacks;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.internal.requests.restaction.interactions.ReplyCallbackActionImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Interactions which allow message replies in the channel they were used in.
 *
 * <p>These replies automatically acknowledge the interaction and support deferring.
 *
 * <p><b>Deferred Replies</b><br>
 *
 * If an interaction reply is deferred using {@link #deferReply()} or {@link #deferReply(boolean)},
 * the {@link #getHook() interaction hook} can be used to send a delayed/deferred reply with {@link InteractionHook#sendMessage(String)}.
 * When using {@link #deferReply()} the first message sent to the {@link InteractionHook} will be identical to using {@link InteractionHook#editOriginal(String)}.
 * You must decide whether your reply will be ephemeral or not before calling {@link #deferReply()}. So design your code flow with that in mind!
 *
 * <p>If a reply is {@link #deferReply() deferred}, it becomes the <b>original</b> message of the interaction hook.
 * This means all the methods with {@code original} in the name, such as {@link InteractionHook#editOriginal(String)},
 * will affect that original reply.
 */
public interface IReplyCallback extends IDeferrableCallback
{
    /**
     * Acknowledge this interaction and defer the reply to a later time.
     * <br>This will send a {@code <Bot> is thinking...} message in chat that will be updated later through either {@link InteractionHook#editOriginal(String)} or {@link InteractionHook#sendMessage(String)}.
     *
     * <p>You can use {@link #deferReply(boolean) deferReply(true)} to send a deferred ephemeral reply. If your initial deferred message is not ephemeral it cannot be made ephemeral later.
     * Your first message to the {@link InteractionHook} will inherit whether the message is ephemeral or not from this deferred reply.
     *
     * <p><b>You only have 3 seconds to acknowledge an interaction!</b>
     * <br>When the acknowledgement is sent after the interaction expired, you will receive {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INTERACTION ErrorResponse.UNKNOWN_INTERACTION}.
     * <p>Use {@link #reply(String)} to reply directly.
     *
     * @return {@link ReplyCallbackAction}
     */
    @Nonnull
    @CheckReturnValue
    ReplyCallbackAction deferReply();

    /**
     * Acknowledge this interaction and defer the reply to a later time.
     * <br>This will send a {@code <Bot> is thinking...} message in chat that will be updated later through either {@link InteractionHook#editOriginal(String)} or {@link InteractionHook#sendMessage(String)}.
     *
     * <p>You can use {@code deferReply()} or {@code deferReply(false)} to send a non-ephemeral deferred reply. If your initial deferred message is ephemeral it cannot be made non-ephemeral later.
     * Your first message to the {@link InteractionHook} will inherit whether the message is ephemeral or not from this deferred reply.
     *
     * <p><b>You only have 3 seconds to acknowledge an interaction!</b>
     * <br>When the acknowledgement is sent after the interaction expired, you will receive {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INTERACTION ErrorResponse.UNKNOWN_INTERACTION}.
     * <p>Use {@link #reply(String)} to reply directly.
     *
     * <p>Ephemeral messages have some limitations and will be removed once the user restarts their client.
     * <br>When a message is ephemeral, it will only be visible to the user that used the interaction.
     * <br>Limitations:
     * <ul>
     *     <li>Cannot contain any files/attachments</li>
     *     <li>Cannot be reacted to</li>
     *     <li>Cannot be retrieved</li>
     * </ul>
     *
     * @param  ephemeral
     *         True, if this message should only be visible to the interaction user
     *
     * @return {@link ReplyCallbackAction}
     */
    @Nonnull
    @CheckReturnValue
    default ReplyCallbackAction deferReply(boolean ephemeral)
    {
        return deferReply().setEphemeral(ephemeral);
    }

    /**
     * Reply to this interaction and acknowledge it.
     * <br>This will send a reply message for this interaction.
     * You can use {@link ReplyCallbackAction#setEphemeral(boolean) setEphemeral(true)} to only let the target user see the message.
     * Replies are non-ephemeral by default.
     *
     * <p><b>You only have 3 seconds to acknowledge an interaction!</b>
     * <br>When the acknowledgement is sent after the interaction expired, you will receive {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INTERACTION ErrorResponse.UNKNOWN_INTERACTION}.
     * <p>If your handling can take longer than 3 seconds, due to various rate limits or other conditions, you should use {@link #deferReply()} instead.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INTERACTION UNKNOWN_INTERACTION}
     *     <br>If the interaction has already been acknowledged or timed out</li>
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
     * @return {@link ReplyCallbackAction}
     *
     * @see    net.dv8tion.jda.api.utils.messages.MessageCreateBuilder MessageCreateBuilder
     */
    @Nonnull
    @CheckReturnValue
    default ReplyCallbackAction reply(@Nonnull MessageCreateData message)
    {
        Checks.notNull(message, "Message");
        ReplyCallbackActionImpl action = (ReplyCallbackActionImpl) deferReply();
        return action.applyData(message);
    }

    /**
     * Reply to this interaction and acknowledge it.
     * <br>This will send a reply message for this interaction.
     * You can use {@link ReplyCallbackAction#setEphemeral(boolean) setEphemeral(true)} to only let the target user see the message.
     * Replies are non-ephemeral by default.
     *
     * <p><b>You only have 3 seconds to acknowledge an interaction!</b>
     * <br>When the acknowledgement is sent after the interaction expired, you will receive {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INTERACTION ErrorResponse.UNKNOWN_INTERACTION}.
     * <p>If your handling can take longer than 3 seconds, due to various rate limits or other conditions, you should use {@link #deferReply()} instead.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INTERACTION UNKNOWN_INTERACTION}
     *     <br>If the interaction has already been acknowledged or timed out</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_AUTOMOD MESSAGE_BLOCKED_BY_AUTOMOD}
     *     <br>If this message was blocked by an {@link net.dv8tion.jda.api.entities.automod.AutoModRule AutoModRule}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER}
     *     <br>If this message was blocked by the harmful link filter</li>
     * </ul>
     *
     * @param  content
     *         The message content to send
     *
     * @throws IllegalArgumentException
     *         If null is provided or the content is longer than {@link Message#MAX_CONTENT_LENGTH} characters
     *
     * @return {@link ReplyCallbackAction}
     */
    @Nonnull
    @CheckReturnValue
    default ReplyCallbackAction reply(@Nonnull String content)
    {
        Checks.notNull(content, "Content");
        return deferReply().setContent(content);
    }

    /**
     * Reply to this interaction and acknowledge it.
     * <br>This will send a reply message for this interaction.
     * You can use {@link ReplyCallbackAction#setEphemeral(boolean) setEphemeral(true)} to only let the target user see the message.
     * Replies are non-ephemeral by default.
     *
     * <p><b>You only have 3 seconds to acknowledge an interaction!</b>
     * <br>When the acknowledgement is sent after the interaction expired, you will receive {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INTERACTION ErrorResponse.UNKNOWN_INTERACTION}.
     * <p>If your handling can take longer than 3 seconds, due to various rate limits or other conditions, you should use {@link #deferReply()} instead.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INTERACTION UNKNOWN_INTERACTION}
     *     <br>If the interaction has already been acknowledged or timed out</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_AUTOMOD MESSAGE_BLOCKED_BY_AUTOMOD}
     *     <br>If this message was blocked by an {@link net.dv8tion.jda.api.entities.automod.AutoModRule AutoModRule}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER}
     *     <br>If this message was blocked by the harmful link filter</li>
     * </ul>
     *
     * @param  embeds
     *         The {@link MessageEmbed MessageEmbeds} to send
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return {@link ReplyCallbackAction}
     */
    @Nonnull
    @CheckReturnValue
    default ReplyCallbackAction replyEmbeds(@Nonnull Collection<? extends MessageEmbed> embeds)
    {
        return deferReply().addEmbeds(embeds);
    }

    /**
     * Reply to this interaction and acknowledge it.
     * <br>This will send a reply message for this interaction.
     * You can use {@link ReplyCallbackAction#setEphemeral(boolean) setEphemeral(true)} to only let the target user see the message.
     * Replies are non-ephemeral by default.
     *
     * <p><b>You only have 3 seconds to acknowledge an interaction!</b>
     * <br>When the acknowledgement is sent after the interaction expired, you will receive {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INTERACTION ErrorResponse.UNKNOWN_INTERACTION}.
     * <p>If your handling can take longer than 3 seconds, due to various rate limits or other conditions, you should use {@link #deferReply()} instead.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INTERACTION UNKNOWN_INTERACTION}
     *     <br>If the interaction has already been acknowledged or timed out</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_AUTOMOD MESSAGE_BLOCKED_BY_AUTOMOD}
     *     <br>If this message was blocked by an {@link net.dv8tion.jda.api.entities.automod.AutoModRule AutoModRule}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER}
     *     <br>If this message was blocked by the harmful link filter</li>
     * </ul>
     *
     * @param  embed
     *         The message embed to send
     * @param  embeds
     *         Any additional embeds to send
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return {@link ReplyCallbackAction}
     */
    @Nonnull
    @CheckReturnValue
    default ReplyCallbackAction replyEmbeds(@Nonnull MessageEmbed embed, @Nonnull MessageEmbed... embeds)
    {
        Checks.notNull(embed, "MessageEmbed");
        Checks.noneNull(embeds, "MessageEmbed");
        return deferReply().addEmbeds(embed).addEmbeds(embeds);
    }

    /**
     * Reply to this interaction and acknowledge it.
     * <br>This will send a reply message for this interaction.
     * You can use {@link ReplyCallbackAction#setEphemeral(boolean) setEphemeral(true)} to only let the target user see the message.
     * Replies are non-ephemeral by default.
     *
     * <p><b>You only have 3 seconds to acknowledge an interaction!</b>
     * <br>When the acknowledgement is sent after the interaction expired, you will receive {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INTERACTION ErrorResponse.UNKNOWN_INTERACTION}.
     * <p>If your handling can take longer than 3 seconds, due to various rate limits or other conditions, you should use {@link #deferReply()} instead.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INTERACTION UNKNOWN_INTERACTION}
     *     <br>If the interaction has already been acknowledged or timed out</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_AUTOMOD MESSAGE_BLOCKED_BY_AUTOMOD}
     *     <br>If this message was blocked by an {@link net.dv8tion.jda.api.entities.automod.AutoModRule AutoModRule}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER}
     *     <br>If this message was blocked by the harmful link filter</li>
     * </ul>
     *
     * @param  components
     *         The {@link LayoutComponent LayoutComponents} to send, such as {@link ActionRow}
     *
     * @throws IllegalArgumentException
     *         If null is provided or more than {@value Message#MAX_COMPONENT_COUNT} component layouts are provided
     *
     * @return {@link ReplyCallbackAction}
     */
    @Nonnull
    @CheckReturnValue
    default ReplyCallbackAction replyComponents(@Nonnull Collection<? extends LayoutComponent> components)
    {
        return deferReply().setComponents(components);
    }

    /**
     * Reply to this interaction and acknowledge it.
     * <br>This will send a reply message for this interaction.
     * You can use {@link ReplyCallbackAction#setEphemeral(boolean) setEphemeral(true)} to only let the target user see the message.
     * Replies are non-ephemeral by default.
     *
     * <p><b>You only have 3 seconds to acknowledge an interaction!</b>
     * <br>When the acknowledgement is sent after the interaction expired, you will receive {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INTERACTION ErrorResponse.UNKNOWN_INTERACTION}.
     * <p>If your handling can take longer than 3 seconds, due to various rate limits or other conditions, you should use {@link #deferReply()} instead.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INTERACTION UNKNOWN_INTERACTION}
     *     <br>If the interaction has already been acknowledged or timed out</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_AUTOMOD MESSAGE_BLOCKED_BY_AUTOMOD}
     *     <br>If this message was blocked by an {@link net.dv8tion.jda.api.entities.automod.AutoModRule AutoModRule}</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER}
     *     <br>If this message was blocked by the harmful link filter</li>
     * </ul>
     *
     * @param  component
     *         The {@link LayoutComponent} to send
     * @param  other
     *         Any addition {@link LayoutComponent LayoutComponents} to send
     *
     * @throws IllegalArgumentException
     *         If null is provided or more than {@value Message#MAX_COMPONENT_COUNT} component layouts are provided
     *
     * @return {@link ReplyCallbackAction}
     */
    @Nonnull
    @CheckReturnValue
    default ReplyCallbackAction replyComponents(@Nonnull LayoutComponent component, @Nonnull LayoutComponent... other)
    {
        Checks.notNull(component, "LayoutComponents");
        Checks.noneNull(other, "LayoutComponents");
        List<LayoutComponent> layouts = new ArrayList<>(1 + other.length);
        layouts.add(component);
        Collections.addAll(layouts, other);
        return replyComponents(layouts);
    }

    /**
     * Reply to this interaction and acknowledge it.
     * <br>This will send a reply message for this interaction.
     * You can use {@link ReplyCallbackAction#setEphemeral(boolean) setEphemeral(true)} to only let the target user see the message.
     * Replies are non-ephemeral by default.
     *
     * <p><b>You only have 3 seconds to acknowledge an interaction!</b>
     * <br>When the acknowledgement is sent after the interaction expired, you will receive {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INTERACTION ErrorResponse.UNKNOWN_INTERACTION}.
     * <p>If your handling can take longer than 3 seconds, due to various rate limits or other conditions, you should use {@link #deferReply()} instead.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INTERACTION UNKNOWN_INTERACTION}
     *     <br>If the interaction has already been acknowledged or timed out</li>
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
     * @return {@link ReplyCallbackAction}
     */
    @Nonnull
    @CheckReturnValue
    default ReplyCallbackAction replyFormat(@Nonnull String format, @Nonnull Object... args)
    {
        Checks.notNull(format, "Format String");
        return reply(String.format(format, args));
    }

    /**
     * Reply to this interaction and acknowledge it.
     * <br>This will send a reply message for this interaction.
     * You can use {@link ReplyCallbackAction#setEphemeral(boolean) setEphemeral(true)} to only let the target user see the message.
     * Replies are non-ephemeral by default.
     *
     * <p><b>You only have 3 seconds to acknowledge an interaction!</b>
     * <br>When the acknowledgement is sent after the interaction expired, you will receive {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INTERACTION ErrorResponse.UNKNOWN_INTERACTION}.
     * <p>If your handling can take longer than 3 seconds, due to various rate limits or other conditions, you should use {@link #deferReply()} instead.
     *
     * <p><b>Resource Handling Note:</b> Once the request is handed off to the requester, for example when you call {@link RestAction#queue()},
     * the requester will automatically clean up all opened files by itself. You are only responsible to close them yourself if it is never handed off properly.
     * For instance, if an exception occurs after using {@link FileUpload#fromData(File)}, before calling {@link RestAction#queue()}.
     * You can safely use a try-with-resources to handle this, since {@link FileUpload#close()} becomes ineffective once the request is handed off.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INTERACTION UNKNOWN_INTERACTION}
     *     <br>If the interaction has already been acknowledged or timed out</li>
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
     * @return {@link ReplyCallbackAction}
     *
     * @see    FileUpload#fromData(InputStream, String)
     */
    @Nonnull
    @CheckReturnValue
    default ReplyCallbackAction replyFiles(@Nonnull Collection<? extends FileUpload> files)
    {
        Checks.notEmpty(files, "File Collection");
        return deferReply().setFiles(files);
    }

    /**
     * Reply to this interaction and acknowledge it.
     * <br>This will send a reply message for this interaction.
     * You can use {@link ReplyCallbackAction#setEphemeral(boolean) setEphemeral(true)} to only let the target user see the message.
     * Replies are non-ephemeral by default.
     *
     * <p><b>You only have 3 seconds to acknowledge an interaction!</b>
     * <br>When the acknowledgement is sent after the interaction expired, you will receive {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INTERACTION ErrorResponse.UNKNOWN_INTERACTION}.
     * <p>If your handling can take longer than 3 seconds, due to various rate limits or other conditions, you should use {@link #deferReply()} instead.
     *
     * <p><b>Resource Handling Note:</b> Once the request is handed off to the requester, for example when you call {@link RestAction#queue()},
     * the requester will automatically clean up all opened files by itself. You are only responsible to close them yourself if it is never handed off properly.
     * For instance, if an exception occurs after using {@link FileUpload#fromData(File)}, before calling {@link RestAction#queue()}.
     * You can safely use a try-with-resources to handle this, since {@link FileUpload#close()} becomes ineffective once the request is handed off.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INTERACTION UNKNOWN_INTERACTION}
     *     <br>If the interaction has already been acknowledged or timed out</li>
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
     * @return {@link ReplyCallbackAction}
     *
     * @see    FileUpload#fromData(InputStream, String)
     */
    @Nonnull
    @CheckReturnValue
    default ReplyCallbackAction replyFiles(@Nonnull FileUpload... files)
    {
        Checks.notEmpty(files, "File Collection");
        Checks.noneNull(files, "FileUpload");
        return deferReply().setFiles(files);
    }
}
