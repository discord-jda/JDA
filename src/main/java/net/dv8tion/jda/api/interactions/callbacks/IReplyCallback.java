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
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction;
import net.dv8tion.jda.internal.requests.restaction.interactions.ReplyActionImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.Collection;

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
     * @return {@link ReplyAction}
     */
    @Nonnull
    @CheckReturnValue
    ReplyAction deferReply();

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
     * @return {@link ReplyAction}
     */
    @Nonnull
    @CheckReturnValue
    default ReplyAction deferReply(boolean ephemeral)
    {
        return deferReply().setEphemeral(ephemeral);
    }

    /**
     * Reply to this interaction and acknowledge it.
     * <br>This will send a reply message for this interaction.
     * You can use {@link ReplyAction#setEphemeral(boolean) setEphemeral(true)} to only let the target user see the message.
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
     * @return {@link ReplyAction}
     */
    @Nonnull
    @CheckReturnValue
    default ReplyAction reply(@Nonnull Message message)
    {
        Checks.notNull(message, "Message");
        ReplyActionImpl action = (ReplyActionImpl) deferReply();
        return action.applyMessage(message);
    }

    /**
     * Reply to this interaction and acknowledge it.
     * <br>This will send a reply message for this interaction.
     * You can use {@link ReplyAction#setEphemeral(boolean) setEphemeral(true)} to only let the target user see the message.
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
     * @return {@link ReplyAction}
     */
    @Nonnull
    @CheckReturnValue
    default ReplyAction reply(@Nonnull String content)
    {
        Checks.notNull(content, "Content");
        return deferReply().setContent(content);
    }

    /**
     * Reply to this interaction and acknowledge it.
     * <br>This will send a reply message for this interaction.
     * You can use {@link ReplyAction#setEphemeral(boolean) setEphemeral(true)} to only let the target user see the message.
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
     * @return {@link ReplyAction}
     */
    @Nonnull
    @CheckReturnValue
    default ReplyAction replyEmbeds(@Nonnull Collection<? extends MessageEmbed> embeds)
    {
        return deferReply().addEmbeds(embeds);
    }

    /**
     * Reply to this interaction and acknowledge it.
     * <br>This will send a reply message for this interaction.
     * You can use {@link ReplyAction#setEphemeral(boolean) setEphemeral(true)} to only let the target user see the message.
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
     * @return {@link ReplyAction}
     */
    @Nonnull
    @CheckReturnValue
    default ReplyAction replyEmbeds(@Nonnull MessageEmbed embed, @Nonnull MessageEmbed... embeds)
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
//     *         The {@link ComponentLayout ComponentLayouts} to send, such as {@link ActionRow}
//     *
//     * @throws IllegalArgumentException
//     *         If null is provided
//     *
//     * @return {@link ReplyAction}
//     */
//    @Nonnull
//    @CheckReturnValue
//    default ReplyAction replyComponents(@Nonnull Collection<? extends ComponentLayout> components)
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
//     *         The {@link ComponentLayout ComponentLayouts} to send, such as {@link ActionRow}
//     *
//     * @throws IllegalArgumentException
//     *         If null is provided
//     *
//     * @return {@link ReplyAction}
//     */
//    @Nonnull
//    @CheckReturnValue
//    default ReplyAction replyComponents(@Nonnull ComponentLayout component, @Nonnull ComponentLayout... components)
//    {
//        Checks.notNull(component, "ComponentLayouts");
//        Checks.noneNull(components, "ComponentLayouts");
//        List<ComponentLayout> layouts = new ArrayList<>();
//        layouts.add(component);
//        Collections.addAll(layouts, components);
//        return replyComponents(layouts);
//    }

    /**
     * Reply to this interaction and acknowledge it.
     * <br>This will send a reply message for this interaction.
     * You can use {@link ReplyAction#setEphemeral(boolean) setEphemeral(true)} to only let the target user see the message.
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
     * @return {@link ReplyAction}
     */
    @Nonnull
    @CheckReturnValue
    default ReplyAction replyFormat(@Nonnull String format, @Nonnull Object... args)
    {
        Checks.notNull(format, "Format String");
        return reply(String.format(format, args));
    }
}
