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

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.utils.AttachmentOption;
import net.dv8tion.jda.internal.requests.restaction.interactions.ReplyCallbackActionImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.io.File;
import java.io.InputStream;
import java.util.Collection;

/**
 * Interactions which allow message replies in the channel they were used in.
 *
 * <p>These replies automatically acknowledge the interaction and support deferring.
 *
 * <h2>Deferred Replies</h2>
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
     *     <li>Cannot be deleted by the bot</li>
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
     * @param  message
     *         The message to send
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return {@link ReplyCallbackAction}
     */
    @Nonnull
    @CheckReturnValue
    default ReplyCallbackAction reply(@Nonnull Message message)
    {
        Checks.notNull(message, "Message");
        ReplyCallbackActionImpl action = (ReplyCallbackActionImpl) deferReply();
        return action.applyMessage(message);
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
     * @param  content
     *         The message content to send
     *
     * @throws IllegalArgumentException
     *         If null is provided or the content is empty or longer than {@link Message#MAX_CONTENT_LENGTH}
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

//    /**
//     * Reply to this interaction and acknowledge it.
//     * <br>This will send a reply message for this interaction.
//     * You can use {@link ReplyAction#setEphemeral(boolean) setEphemeral(true)} to only let the target user see the message.
//     * Replies are non-ephemeral by default.
//     *
//     * <p><b>You only have 3 seconds to acknowledge an interaction!</b>
//     * <br>When the acknowledgement is sent after the interaction expired, you will receive {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INTERACTION ErrorResponse.UNKNOWN_INTERACTION}.
//     * <p>If your handling can take longer than 3 seconds, due to various rate limits or other conditions, you should use {@link #deferReply()} instead.
//     *
//     * @param  components
//     *         The {@link LayoutComponent LayoutComponents} to send, such as {@link ActionRow}
//     *
//     * @throws IllegalArgumentException
//     *         If null is provided
//     *
//     * @return {@link ReplyAction}
//     */
//    @Nonnull
//    @CheckReturnValue
//    default ReplyAction replyComponents(@Nonnull Collection<? extends LayoutComponent> components)
//    {
//        if (components.stream().anyMatch(it -> !(it instanceof ActionRow)))
//            throw new UnsupportedOperationException("Only ActionRow layouts are currently supported.");
//        List<ActionRow> rows = components.stream()
//                .map(ActionRow.class::cast)
//                .collect(Collectors.toList());
//        return deferReply().addActionRows(rows);
//    }
//
//    /**
//     * Reply to this interaction and acknowledge it.
//     * <br>This will send a reply message for this interaction.
//     * You can use {@link ReplyAction#setEphemeral(boolean) setEphemeral(true)} to only let the target user see the message.
//     * Replies are non-ephemeral by default.
//     *
//     * <p><b>You only have 3 seconds to acknowledge an interaction!</b>
//     * <br>When the acknowledgement is sent after the interaction expired, you will receive {@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INTERACTION ErrorResponse.UNKNOWN_INTERACTION}.
//     * <p>If your handling can take longer than 3 seconds, due to various rate limits or other conditions, you should use {@link #deferReply()} instead.
//     *
//     * @param  components
//     *         The {@link LayoutComponent LayoutComponents} to send, such as {@link ActionRow}
//     *
//     * @throws IllegalArgumentException
//     *         If null is provided
//     *
//     * @return {@link ReplyAction}
//     */
//    @Nonnull
//    @CheckReturnValue
//    default ReplyAction replyComponents(@Nonnull LayoutComponent component, @Nonnull LayoutComponent... components)
//    {
//        Checks.notNull(component, "LayoutComponents");
//        Checks.noneNull(components, "LayoutComponents");
//        List<LayoutComponent> layouts = new ArrayList<>();
//        layouts.add(component);
//        Collections.addAll(layouts, components);
//        return replyComponents(layouts);
//    }

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
     * @param  format
     *         Format string for the message content
     * @param  args
     *         Format arguments for the content
     *
     * @throws IllegalArgumentException
     *         If the format string is null or the resulting content is longer than {@link Message#MAX_CONTENT_LENGTH}
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
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#REQUEST_ENTITY_TOO_LARGE REQUEST_ENTITY_TOO_LARGE}
     *     <br>The file exceeds the maximum upload size of {@link Message#MAX_FILE_SIZE}</li>
     * </ul>
     *
     * @param  data
     *         The InputStream data to upload
     * @param  name
     *         The file name that should be sent to discord
     *         <br>Refer to the documentation for {@link #replyFile(java.io.File, String, AttachmentOption...)} for information about this parameter.
     * @param  options
     *         Possible options to apply to this attachment, such as marking it as spoiler image
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided file or filename is {@code null} or {@code empty}.
     *
     * @return {@link ReplyCallbackAction}
     */
    @Nonnull
    @CheckReturnValue
    default ReplyCallbackAction replyFile(@Nonnull InputStream data, @Nonnull String name, @Nonnull AttachmentOption... options)
    {
        return deferReply().addFile(data, name, options);
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
     * <p>This is a shortcut to {@link #replyFile(java.io.File, String, AttachmentOption...)} by way of using {@link java.io.File#getName()}.
     * <pre>sendFile(file, file.getName())</pre>
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#REQUEST_ENTITY_TOO_LARGE REQUEST_ENTITY_TOO_LARGE}
     *     <br>The file exceeds the maximum upload size of {@link Message#MAX_FILE_SIZE}</li>
     * </ul>
     *
     * @param  file
     *         The {@link File} data to upload
     * @param  options
     *         Possible options to apply to this attachment, such as marking it as spoiler image
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided file is {@code null}.
     *
     * @return {@link ReplyCallbackAction}
     */
    @Nonnull
    @CheckReturnValue
    default ReplyCallbackAction replyFile(@Nonnull File file, @Nonnull AttachmentOption... options)
    {
        return deferReply().addFile(file, options);
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
     * <p>The {@code name} parameter is used to inform Discord about what the file should be called. This is 2 fold:
     * <ol>
     *     <li>The file name provided is the name that is found in {@link net.dv8tion.jda.api.entities.Message.Attachment#getFileName()}
     *          after upload and it is the name that will show up in the client when the upload is displayed.
     *     <br>Note: The fileName does not show up on the Desktop client for images. It does on mobile however.</li>
     *     <li>The extension of the provided fileName also determines how Discord will treat the file. Discord currently only
     *         has special handling for image file types, but the fileName's extension must indicate that it is an image file.
     *         This means it has to end in something like .png, .jpg, .jpeg, .gif, etc. As a note, you can also not provide
     *         a full name for the file and instead ONLY provide the extension like "png" or "gif" and Discord will generate
     *         a name for the upload and append the fileName as the extension.</li>
     * </ol>
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#REQUEST_ENTITY_TOO_LARGE REQUEST_ENTITY_TOO_LARGE}
     *     <br>The file exceeds the maximum upload size of {@link Message#MAX_FILE_SIZE}</li>
     * </ul>
     *
     * @param  file
     *         The {@link File} data to upload
     * @param  name
     *         The file name that should be sent to discord
     * @param  options
     *         Possible options to apply to this attachment, such as marking it as spoiler image
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided file or filename is {@code null} or {@code empty}.
     *
     * @return {@link ReplyCallbackAction}
     */
    @Nonnull
    @CheckReturnValue
    default ReplyCallbackAction replyFile(@Nonnull File file, @Nonnull String name, @Nonnull AttachmentOption... options)
    {
        return deferReply().addFile(file, name, options);
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
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#REQUEST_ENTITY_TOO_LARGE REQUEST_ENTITY_TOO_LARGE}
     *     <br>The file exceeds the maximum upload size of {@link Message#MAX_FILE_SIZE}</li>
     * </ul>
     *
     * @param  data
     *         The {@code byte[]} data to upload
     * @param  name
     *         The file name that should be sent to discord
     *         <br>Refer to the documentation for {@link #replyFile(java.io.File, String, AttachmentOption...)} for information about this parameter.
     * @param  options
     *         Possible options to apply to this attachment, such as marking it as spoiler image
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided file or filename is {@code null} or {@code empty}.
     *
     * @return {@link ReplyCallbackAction}
     */
    @Nonnull
    @CheckReturnValue
    default ReplyCallbackAction replyFile(@Nonnull byte[] data, @Nonnull String name, @Nonnull AttachmentOption... options)
    {
        return deferReply().addFile(data, name, options);
    }
}
