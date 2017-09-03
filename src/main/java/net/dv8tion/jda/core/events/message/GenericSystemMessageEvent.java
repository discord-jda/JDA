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

package net.dv8tion.jda.core.events.message;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageType;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.message.SystemMessage;

/**
 * Fired for every system message (messages not send by a user)
 */
public abstract class GenericSystemMessageEvent extends GenericMessageEvent
{
    protected SystemMessage message;

    public GenericSystemMessageEvent(JDA api, long responseNumber, SystemMessage message, MessageChannel channel)
    {
        super(api, responseNumber, message.getIdLong(), channel);
        this.message = message;
    }

    /**
     * The author of this SystemMessage.
     * <br>More details may be provided by implementations of this class.
     *
     * @return Author for this message
     *
     * @see    net.dv8tion.jda.core.entities.message.SystemMessage#getAuthor() SystemMessage.getAuthor()
     */
    public User getAuthor()
    {
        return message.getAuthor();
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.MessageType MessageType}
     * that specifies what kind of message this is.
     *
     * <p>This is never {@link net.dv8tion.jda.core.entities.MessageType#DEFAULT MessageType.DEFAULT}!
     *
     * @return {@link net.dv8tion.jda.core.entities.MessageType MessageType}
     */
    public MessageType getType()
    {
        return message.getType();
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.message.SystemMessage SystemMessage}
     * <br>Implementations of this event may provide a more specific implementation.
     *
     * @return The {@link net.dv8tion.jda.core.entities.message.SystemMessage SystemMessage}
     */
    public SystemMessage getMessage()
    {
        return message;
    }
}
