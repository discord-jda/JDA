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

package net.dv8tion.jda.client.entities.message;

import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.core.entities.MessageType;
import net.dv8tion.jda.core.entities.User;

public class GroupRecipientMessage extends GenericGroupMessage
{
    protected boolean add;

    public GroupRecipientMessage(User author, Group channel, long messageId, String content, boolean add)
    {
        super(author, channel, messageId, content);
        this.add = add;
    }

    @Override
    public MessageType getType()
    {
        return add ? MessageType.RECIPIENT_ADD : MessageType.RECIPIENT_REMOVE;
    }
}
