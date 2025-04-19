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

import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.messages.MessageEditRequest;

/**
 * Specialized {@link RestAction} used to update an existing message sent by a {@link net.dv8tion.jda.api.entities.Webhook Webhook} or {@link net.dv8tion.jda.api.interactions.InteractionHook InteractionHook}.
 *
 * @param <T>
 *        The type of message that will be returned
 *
 * @see   net.dv8tion.jda.api.interactions.InteractionHook#editOriginal(String)
 * @see   net.dv8tion.jda.api.entities.WebhookClient#editMessageById(long, String)
 */
public interface WebhookMessageEditAction<T> extends MessageEditRequest<WebhookMessageEditAction<T>>, AbstractWebhookMessageAction<T, WebhookMessageEditAction<T>>
{
}
