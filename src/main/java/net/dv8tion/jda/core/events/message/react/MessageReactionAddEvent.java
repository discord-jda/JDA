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

package net.dv8tion.jda.core.events.message.react;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.entities.User;

/**
 * Indicates that a user added a reaction to a message
 * <br>This includes unicode and custom emoji
 *
 * <p>Can be used to track when a user adds a reaction to a message
 */
public class MessageReactionAddEvent extends GenericMessageReactionEvent
{
    public MessageReactionAddEvent(JDA api, long responseNumber, User user, MessageReaction reaction)
    {
        super(api, responseNumber, user, reaction);
    }
}
