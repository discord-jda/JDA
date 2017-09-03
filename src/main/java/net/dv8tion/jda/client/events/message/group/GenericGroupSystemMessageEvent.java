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

package net.dv8tion.jda.client.events.message.group;

import net.dv8tion.jda.client.entities.Friend;
import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.MessageType;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.message.SystemMessage;

/**
 * Fired when Discord sends a {@link net.dv8tion.jda.core.entities.message.SystemMessage SystemMessage}
 * to a {@link net.dv8tion.jda.client.entities.Group Group}!
 */
public abstract class GenericGroupSystemMessageEvent extends GenericGroupMessageEvent
{
    protected final SystemMessage message;

    public GenericGroupSystemMessageEvent(JDA api, long responseNumber, SystemMessage message, Group group)
    {
        super(api, responseNumber, message.getIdLong(), group);
        this.message = message;
    }

    /**
     * Author of the {@link net.dv8tion.jda.core.entities.message.SystemMessage SystemMessage}
     * may be specified further in implementation of this event.
     *
     * @return {@link net.dv8tion.jda.core.entities.User User}
     */
    public User getAuthor()
    {
        return message.getAuthor();
    }

    /**
     * If the author of this {@link net.dv8tion.jda.core.entities.message.SystemMessage SystemMessage}
     * is a {@link net.dv8tion.jda.client.entities.Friend Friend} this will return the representing instance.
     *
     * @return {@link net.dv8tion.jda.client.entities.Friend Friend} for {@link #getAuthor()} or {@code null}
     *         if the author is not a friend
     */
    public Friend getFriend()
    {
        return getJDA().asClient().getFriend(getAuthor());
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.MessageType MessageType}
     *
     * @return The {@link net.dv8tion.jda.core.entities.MessageType MessageType}
     */
    public MessageType getType()
    {
        return message.getType();
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.message.SystemMessage SystemMessage}
     * <br>This may be specified by implementations of this event.
     *
     * @return The {@link net.dv8tion.jda.core.entities.message.SystemMessage SystemMessage}
     */
    public SystemMessage getMessage()
    {
        return message;
    }
}
