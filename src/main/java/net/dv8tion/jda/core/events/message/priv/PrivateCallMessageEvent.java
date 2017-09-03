/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter & Florian Spieß
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

package net.dv8tion.jda.core.events.message.priv;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.MessageType;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.message.CallMessage;

/**
 * Fired when a call is started in a {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel}
 * and Discord sends a {@link net.dv8tion.jda.core.entities.message.CallMessage CallMessage} to that channel.
 */
public class PrivateCallMessageEvent extends GenericPrivateMessageEvent
{
    protected final CallMessage message;

    public PrivateCallMessageEvent(JDA api, long responseNumber, CallMessage message, PrivateChannel channel)
    {
        super(api, responseNumber, message.getIdLong(), channel);
        this.message = message;
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.User User} that started the call.
     *
     * @return The responsible User
     */
    public User getAuthor()
    {
        return message.getAuthor();
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.MessageType MessageType} for this message.
     *
     * @return {@link net.dv8tion.jda.core.entities.MessageType MessageType}
     */
    public MessageType getType()
    {
        return message.getType();
    }

    /**
     * The specific {@link net.dv8tion.jda.core.entities.message.CallMessage CallMessage}
     *
     * @return The CallMessage
     */
    public CallMessage getMessage()
    {
        return message;
    }
}
