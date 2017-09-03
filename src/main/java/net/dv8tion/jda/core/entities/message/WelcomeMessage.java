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

package net.dv8tion.jda.core.entities.message;

import net.dv8tion.jda.core.entities.*;

public class WelcomeMessage extends SystemMessage
{
    public WelcomeMessage(User author, TextChannel channel, long messageId, String content)
    {
        super(author, channel, messageId, content);
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.User User} that joined this {@link net.dv8tion.jda.core.entities.Guild Guild}
     *
     * @return The joining User
     */
    @Override
    public User getAuthor()
    {
        return super.getAuthor();
    }

    @Override
    public ChannelType getChannelType()
    {
        return ChannelType.TEXT;
    }

    @Override
    public TextChannel getChannel()
    {
        return (TextChannel) channel;
    }

    @Override
    public MessageType getType()
    {
        return MessageType.GUILD_MEMBER_JOIN;
    }
}
