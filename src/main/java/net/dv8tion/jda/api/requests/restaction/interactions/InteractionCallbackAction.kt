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
package net.dv8tion.jda.api.requests.restaction.interactions

import net.dv8tion.jda.api.requests.RestAction
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * A callback action is used to **acknowledge** an [Interaction][net.dv8tion.jda.api.interactions.Interaction].
 */
interface InteractionCallbackAction<T> : RestAction<T> {
    /**
     * Closes all owned resources used for this request.
     *
     *
     * This closes all files added, if applicable.
     *
     * @return This instance for chaining.
     */
    @Nonnull
    @CheckReturnValue
    fun closeResources(): InteractionCallbackAction<T>?

    /**
     * The possible types of interaction responses.
     * <br></br>This is currently only used internally to reduce interface complexity.
     */
    enum class ResponseType(
        /**
         * The raw integer key for this response type
         *
         * @return The raw key
         */
        @JvmField val raw: Int
    ) {
        /** Immediately respond to an interaction with a message  */
        CHANNEL_MESSAGE_WITH_SOURCE(4),

        /** Delayed or Deferred response to an interaction, this sends a "Thinking..." message to the channel  */
        DEFERRED_CHANNEL_MESSAGE_WITH_SOURCE(5),

        /** Defer the update of the message for a component interaction  */
        DEFERRED_MESSAGE_UPDATE(6),

        /** Update the message for a component interaction  */
        MESSAGE_UPDATE(7),

        /** Provide auto-complete choices for a command  */
        COMMAND_AUTOCOMPLETE_CHOICES(8),

        /** Respond with a modal  */
        MODAL(9),

        /** Respond with the "Premium required" default Discord message for premium App subscriptions  */
        PREMIUM_REQUIRED(10)

    }
}
