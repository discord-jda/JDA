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

package net.dv8tion.jda.client.events.message.group.recipient;

import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.client.events.message.group.GenericGroupSystemMessageEvent;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.client.entities.message.GroupRecipientMessage;
import net.dv8tion.jda.core.entities.message.SystemMessage;

public class GenericGroupRecipientMessageEvent extends GenericGroupSystemMessageEvent
{
    public GenericGroupRecipientMessageEvent(JDA api, long responseNumber, SystemMessage message, Group group)
    {
        super(api, responseNumber, message, group);
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.User User} that is either added or removed
     * from this {@link net.dv8tion.jda.client.entities.Group Group}.
     *
     * @return The affected {@link net.dv8tion.jda.core.entities.User User}
     */
    @Override
    public User getAuthor()
    {
        return super.getAuthor();
    }

    @Override
    public GroupRecipientMessage getMessage()
    {
        return (GroupRecipientMessage) message;
    }
}
