/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Discord API connection internals.
 * Includes REST and WebSocket connections
 *
 * <p><u><b>REST API</b></u>
 * <br>The {@link net.dv8tion.jda.core.requests.Requester Requester}
 * is used to create HTTPs requests with the Discord API through {@link net.dv8tion.jda.core.requests.Route Routes}.
 * <br>This automatically handles rate limitations (429) that restrict our request rates. For that it uses an implementation
 * of {@link net.dv8tion.jda.core.requests.RateLimiter RateLimiter} specific for a certain {@link net.dv8tion.jda.core.AccountType AccountType}.
 * <br>{@link net.dv8tion.jda.core.requests.restaction Learn More}
 *
 * <p>The {@link net.dv8tion.jda.core.requests.RestAction RestAction} can be found throughout JDA and allows
 * to specify how the Requester should deal with rate limits.
 * It has extensions with additional functionalities in {@link net.dv8tion.jda.core.requests.restaction}.
 *
 * <p>In the case of a failed Request the RestAction will be provided with an {@link net.dv8tion.jda.core.exceptions.ErrorResponseException ErrorResponseException}
 * which contains a {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponse} representing the failure cause!
 *
 * <p><u><b>WebSocket API</b></u>
 * <br>The {@link net.dv8tion.jda.core.requests.WebSocketClient WebSocketClient} is used to
 * handle the {@link net.dv8tion.jda.core.events.Event Event} flow and connection to the Discord gateway.
 * <br>It can send {@link net.dv8tion.jda.core.managers.Presence Presence} updates that will determine how
 * the Discord Client will display the currently connected account (Online Status / Game)
 */
package net.dv8tion.jda.core.requests;
