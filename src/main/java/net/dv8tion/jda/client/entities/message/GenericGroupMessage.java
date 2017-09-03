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
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.message.SystemMessage;

abstract class GenericGroupMessage extends SystemMessage
{
    public GenericGroupMessage(User author, Group channel, long messageId, String content)
    {
        super(author, channel, messageId, content);
    }

    @Override
    public ChannelType getChannelType()
    {
        return ChannelType.GROUP;
    }

    @Override
    public boolean isFromType(ChannelType channelType)
    {
        return channelType == ChannelType.GROUP;
    }

    @Override
    public Group getChannel()
    {
        return (Group) channel;
    }
}
