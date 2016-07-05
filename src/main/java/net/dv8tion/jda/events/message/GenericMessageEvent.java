/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
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
package net.dv8tion.jda.events.message;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.Event;

/**
 * <b><u>GenericMessageEvent</u></b><br>
 * Fired whenever a {@link net.dv8tion.jda.entities.Message Message} event is fired.<br>
 * Every MessageEvent is an instance of this event and can be casted. (no exceptions)<br>
 * <br>
 * Use: Detect any MessageEvent. <i>(No real use for the JDA user)</i>
 */
public abstract class GenericMessageEvent extends Event
{
    protected final Message message;

    public GenericMessageEvent(JDA api, int responseNumber, Message message)
    {
        super(api, responseNumber);
        this.message = message;
    }

    public Message getMessage()
    {
        return message;
    }

    public User getAuthor()
    {
        return message == null ? null : getMessage().getAuthor();
    }
}
