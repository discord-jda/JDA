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
package net.dv8tion.jda.events.user;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.User;

import java.time.OffsetDateTime;

/**
 * <b><u>UserTypingUpdateEvent</u></b><br/>
 * Fired if a {@link net.dv8tion.jda.entities.User User} starts typing. (Similar to the typing indicator in the Discord client)<br/>
 * <br/>
 * Use: Retrieve the User who started typing and when and in which MessageChannel they started typing.
 */
public class UserTypingEvent extends GenericUserEvent
{
    private final MessageChannel channel;
    private final OffsetDateTime timestamp;

    public UserTypingEvent(JDA api, int responseNumber, User user, MessageChannel channel, OffsetDateTime timestamp)
    {
        super(api, responseNumber, user);
        this.channel = channel;
        this.timestamp = timestamp;
    }

    public MessageChannel getChannel()
    {
        return channel;
    }

    public OffsetDateTime getTimestamp()
    {
        return timestamp;
    }
}
