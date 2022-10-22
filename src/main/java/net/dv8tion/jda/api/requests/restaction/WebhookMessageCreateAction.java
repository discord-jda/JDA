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

package net.dv8tion.jda.api.requests.restaction;

import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.requests.FluentRestAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateRequest;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Extension of a default {@link net.dv8tion.jda.api.requests.RestAction RestAction}
 * that allows setting message information before sending!
 *
 * <p>This is available as return type of all sendMessage/sendFile methods in {@link net.dv8tion.jda.api.entities.WebhookClient WebhookClient}.
 *
 * <p><u>When this RestAction has been executed all provided files will be closed.</u>
 * <br>Note that the garbage collector also frees opened file streams when it finalizes the stream object.
 *
 * @see    net.dv8tion.jda.api.entities.WebhookClient#sendMessage(String)
 */
// TODO: WebhookMessage type (no channel/guild attached)
public interface WebhookMessageCreateAction<T> extends MessageCreateRequest<WebhookMessageCreateAction<T>>, FluentRestAction<T, WebhookMessageCreateAction<T>>
{
//    /**
//     * Set the apparent username for the message author.
//     * <br>This changes the username that is shown for the message author.
//     *
//     * <p>This cannot be used with {@link net.dv8tion.jda.api.interactions.InteractionHook InteractionHooks}!
//     *
//     * @param  name
//     *         The username to use, or null to use the default
//     *
//     * @return The same message action, for chaining convenience
//     */
//    @Nonnull
//    @CheckReturnValue
//    WebhookMessageAction<T> setUsername(@Nullable String name);
//
//    /**
//     * Set the apparent avatar for the message author.
//     * <br>This changes the avatar that is shown for the message author.
//     *
//     * <p>This cannot be used with {@link net.dv8tion.jda.api.interactions.InteractionHook InteractionHooks}!
//     *
//     * @param  iconUrl
//     *         The URL to the avatar, or null to use default
//     *
//     * @return The same message action, for chaining convenience
//     */
//    @Nonnull
//    @CheckReturnValue
//    WebhookMessageAction<T> setAvatarUrl(@Nullable String iconUrl);

    /**
     * Set whether this message should be visible to other users.
     * <br>When a message is ephemeral, it will only be visible to the user that used the interaction.
     *
     * <p>Ephemeral messages have some limitations and will be removed once the user restarts their client.
     * <br>Limitations:
     * <ul>
     *     <li>Cannot be reacted to</li>
     *     <li>Cannot be retrieved</li>
     * </ul>
     *
     * <p>This only works on {@link InteractionHook InteractionHooks}!
     * For a {@link IReplyCallback#deferReply() deferred reply}, this is not supported. When a reply is deferred,
     * the very first message sent through the {@link InteractionHook}, inherits the ephemeral state of the initial reply.
     * To send an ephemeral deferred reply, you must use {@link IReplyCallback#deferReply(boolean) deferReply(true)} instead.
     *
     * @param  ephemeral
     *         True, if this message should be invisible for other users
     *
     * @return The same message action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    WebhookMessageCreateAction<T> setEphemeral(boolean ephemeral);
}
