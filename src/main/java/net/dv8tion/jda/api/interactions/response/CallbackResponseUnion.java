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

package net.dv8tion.jda.api.interactions.response;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.restaction.interactions.InteractionCallbackAction;

import javax.annotation.Nonnull;

/**
 * Interaction callback response that is created by interaction replies like
 * {@link net.dv8tion.jda.api.interactions.callbacks.IReplyCallback#reply(String) IReplyCallback#reply()} or interaction updates like
 * {@link net.dv8tion.jda.api.interactions.callbacks.IMessageEditCallback#editMessage(String) IMessageEditCallback#editMessage()}.
 *
 * @see    net.dv8tion.jda.api.interactions.InteractionHook#getCallbackResponse() InteractionHook#getCallbackResponse
 */
public interface CallbackResponseUnion
{
    /**
     * Returns the {@link Message} contained in this callback response.
     *
     * @throws IllegalStateException
     *         If this callback response does not contain a message.
     *
     * @return Message contained in this callback response.
     * @see    InteractionCallbackAction.ResponseType#doesCallbackResponseContainMessage() ResponseType#doesCallbackResponseContainMessage()
     */
    @Nonnull
    Message asMessage();

    /**
     * Returns the {@link InteractionCallbackAction.ResponseType ResponseType} that was used for replying to the interaction.
     *
     * @return ResponseType that was used for replying to the interaction.
     */
    @Nonnull
    InteractionCallbackAction.ResponseType getResponseType();
}
