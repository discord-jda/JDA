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

/**
 * Events indicating the {@link net.dv8tion.jda.api.entities.MessageReaction MessageReactions} of a {@link net.dv8tion.jda.api.entities.Message Message} changed.
 * <br>These events
 * cannot provide a Message instance due to missing cache but instead provide the Message ID as
 * long representation.
 *
 * <h2>Requirements</h2>
 *
 * <p>These events require the {@link net.dv8tion.jda.api.requests.GatewayIntent#DIRECT_MESSAGE_REACTIONS DIRECT_MESSAGE_REACTIONS} intent to be enabled.
 *
 * @since 3.1
 */
package net.dv8tion.jda.api.events.message.priv.react;
