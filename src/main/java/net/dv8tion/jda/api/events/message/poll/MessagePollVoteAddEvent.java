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

package net.dv8tion.jda.api.events.message.poll;

import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import javax.annotation.Nonnull;

/**
 * Indicates that a user voted for a poll answer.
 * <br>If the poll allows selecting multiple answers, one event per vote is sent.
 *
 * <p>Can be used to track when a user votes for a poll answer
 *
 * <p><b>Requirements</b><br>
 *
 * <p>These events require at least one of the following intents (Will not fire at all if neither is enabled):
 * <ul>
 *     <li>{@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_MESSAGE_POLLS GUILD_MESSAGE_POLLS} to work in guild text channels</li>
 *     <li>{@link net.dv8tion.jda.api.requests.GatewayIntent#DIRECT_MESSAGE_POLLS DIRECT_MESSAGE_POLLS} to work in private channels</li>
 * </ul>
 */
public class MessagePollVoteAddEvent extends GenericMessagePollVoteEvent
{
    public MessagePollVoteAddEvent(@Nonnull MessageChannel channel, long responseNumber, long messageId, long userId, long answerId)
    {
        super(channel, responseNumber, messageId, userId, answerId);
    }
}
