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

import net.dv8tion.jda.api.entities.ActivityInstanceResource;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.restaction.interactions.InteractionCallbackAction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Interaction callback response that is created by interactions such as:
 * <ul>
 *     <li>Interaction replies like {@link net.dv8tion.jda.api.interactions.callbacks.IReplyCallback#reply(String) IReplyCallback#reply()}</li>
 *     <li>Interaction updates like {@link net.dv8tion.jda.api.interactions.callbacks.IMessageEditCallback#editMessage(String) IMessageEditCallback#editMessage()}</li>
 *     <li>{@link net.dv8tion.jda.api.interactions.callbacks.ILaunchActivityCallback#replyLaunchActivity() ILaunchActivityCallback#replyLaunchActivity()}</li>
 * </ul>
 *
 * @see net.dv8tion.jda.api.interactions.InteractionHook#getCallbackResponse() InteractionHook#getCallbackResponse
 */
public interface InteractionCallbackResponse
{
    /**
     * Returns the {@link Message} contained in this callback response, or {@code null} if none was created or updated by the response.
     *
     * @return Message contained in this callback response, or null.
     */
    @Nullable
    Message getMessage();

    /**
     * Returns the {@link ActivityInstanceResource} contained in this callback response, or {@code null} if the response was not a launched activity.
     *
     * @return ActivityInstance contained in this callback response, or null.
     */
    @Nullable
    ActivityInstanceResource getActivityInstance();

    /**
     * Returns the {@link InteractionCallbackAction.ResponseType ResponseType} that was used for replying to the interaction.
     *
     * @return ResponseType that was used for replying to the interaction.
     */
    @Nonnull
    InteractionCallbackAction.ResponseType getResponseType();
}
