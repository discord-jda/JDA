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
package net.dv8tion.jda.events.message.priv;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.PrivateChannel;

/**
 * <b><u>PrivateMessageDeleteEvent</u></b><br/>
 * Fired if a Message is deleted in a {@link net.dv8tion.jda.entities.PrivateChannel PrivateChannel}.<br/>
 * <br/>
 * Use: Retrieve affected PrivateChannel and the ID of the deleted Message.
 */
public class PrivateMessageDeleteEvent extends GenericPrivateMessageEvent
{
    private final String messageId;

    public PrivateMessageDeleteEvent(JDA api, int responseNumber, String messageId, PrivateChannel channel)
    {
        super(api, responseNumber, null, channel);
        this.messageId = messageId;
    }

    public String getMessageId()
    {
        return messageId;
    }
}
