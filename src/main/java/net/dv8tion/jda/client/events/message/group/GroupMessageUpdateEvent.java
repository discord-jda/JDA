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

package net.dv8tion.jda.client.events.message.group;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

public class GroupMessageUpdateEvent extends GenericGroupMessageEvent
{
    private final Message message;

    public GroupMessageUpdateEvent(JDA api, long responseNumber, Message message)
    {
        super(api, responseNumber, message.getIdLong(), message.getGroup());
        this.message = message;
    }

    public Message getMessage()
    {
        return message;
    }

    public User getAuthor()
    {
        return message.getAuthor();
    }
}
