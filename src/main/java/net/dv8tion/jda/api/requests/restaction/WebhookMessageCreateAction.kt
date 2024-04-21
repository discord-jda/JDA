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
package net.dv8tion.jda.api.requests.restaction

import net.dv8tion.jda.api.entities.channel.forums.ForumTagSnowflake
import net.dv8tion.jda.api.utils.messages.MessageCreateRequest
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Extension of a default [RestAction][net.dv8tion.jda.api.requests.RestAction]
 * that allows setting message information before sending!
 *
 *
 * This is available as return type of all sendMessage/sendFile methods in [WebhookClient][net.dv8tion.jda.api.entities.WebhookClient].
 *
 *
 * <u>When this RestAction has been executed all provided files will be closed.</u>
 * <br></br>Note that the garbage collector also frees opened file streams when it finalizes the stream object.
 *
 * @see net.dv8tion.jda.api.entities.WebhookClient.sendMessage
 */
interface WebhookMessageCreateAction<T> : MessageCreateRequest<WebhookMessageCreateAction<T>?>,
    AbstractWebhookMessageAction<T, WebhookMessageCreateAction<T>?> {
    /**
     * Set whether this message should be visible to other users.
     * <br></br>When a message is ephemeral, it will only be visible to the user that used the interaction.
     *
     *
     * Ephemeral messages have some limitations and will be removed once the user restarts their client.
     * <br></br>Limitations:
     *
     *  * Cannot be reacted to
     *  * Cannot be retrieved
     *
     *
     *
     * This only works on [InteractionHooks][InteractionHook]!
     * For a [deferred reply][IReplyCallback.deferReply], this is not supported. When a reply is deferred,
     * the very first message sent through the [InteractionHook], inherits the ephemeral state of the initial reply.
     * To send an ephemeral deferred reply, you must use [deferReply(true)][IReplyCallback.deferReply] instead.
     *
     * @param  ephemeral
     * True, if this message should be invisible for other users
     *
     * @throws IllegalStateException
     * If this is not an interaction webhook
     *
     * @return The same message action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setEphemeral(ephemeral: Boolean): WebhookMessageCreateAction<T>?

    /**
     * Set the apparent username for the message author.
     * <br></br>This changes the username that is shown for the message author.
     *
     *
     * This cannot be used with [InteractionHooks][net.dv8tion.jda.api.interactions.InteractionHook]!
     *
     * @param  name
     * The username to use, or null to use the default
     *
     * @throws IllegalStateException
     * If this is an interaction webhook
     *
     * @return The same message action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setUsername(name: String?): WebhookMessageCreateAction<T>?

    /**
     * Set the apparent avatar for the message author.
     * <br></br>This changes the avatar that is shown for the message author.
     *
     *
     * This cannot be used with [InteractionHooks][net.dv8tion.jda.api.interactions.InteractionHook]!
     *
     * @param  iconUrl
     * The URL to the avatar, or null to use default
     *
     * @throws IllegalStateException
     * If this is an interaction webhook
     *
     * @return The same message action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun setAvatarUrl(iconUrl: String?): WebhookMessageCreateAction<T>?

    /**
     * Create a new thread channel for this webhook message.
     * <br></br>This is currently limited to forum channels.
     * <br></br>Does nothing if a [target thread][.setThread] is already configured.
     *
     *
     * This cannot be used with [InteractionHooks][net.dv8tion.jda.api.interactions.InteractionHook]!
     *
     * @param  threadMetadata
     * The metadata for the thread
     *
     * @throws IllegalStateException
     * If this is an interaction webhook
     * @throws IllegalArgumentException
     * If null is provided
     *
     * @return The same message action, for chaining convenience
     *
     * @see .createThread
     */
    @Nonnull
    @CheckReturnValue
    fun createThread(@Nonnull threadMetadata: ThreadCreateMetadata?): WebhookMessageCreateAction<T>?

    /**
     * Create a new thread channel for this webhook message.
     * <br></br>This is currently limited to forum channels.
     * <br></br>Does nothing if a [target thread][.setThread] is already configured.
     *
     *
     * This cannot be used with [InteractionHooks][net.dv8tion.jda.api.interactions.InteractionHook]!
     *
     * @param  threadName
     * The thread title
     * @param  tags
     * The tags to apply to this forum post
     *
     * @throws IllegalStateException
     * If this is an interaction webhook
     * @throws IllegalArgumentException
     * If null is provided or the name is not between 1 and 80 characters long
     *
     * @return The same message action, for chaining convenience
     *
     * @see .createThread
     */
    @Nonnull
    @CheckReturnValue
    fun createThread(
        @Nonnull threadName: String,
        @Nonnull vararg tags: ForumTagSnowflake?
    ): WebhookMessageCreateAction<T>? {
        return createThread(ThreadCreateMetadata(threadName).addTags(*tags))
    }
}
