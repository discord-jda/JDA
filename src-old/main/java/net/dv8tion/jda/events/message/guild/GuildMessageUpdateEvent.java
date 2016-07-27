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
package net.dv8tion.jda.events.message.guild;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;

/**
 * <b><u>GuildMessageReceivedEvent</u></b><br/>
 * Fired if a Message is edited in a {@link net.dv8tion.jda.entities.TextChannel TextChannel}.<br/>
 * <br/>
 * Use: Retrieve affected TextChannel and Message.
 */
public class GuildMessageUpdateEvent extends GenericGuildMessageEvent
{
    public GuildMessageUpdateEvent(JDA api, int responseNumber, Message message, TextChannel channel)
    {
        super(api, responseNumber, message, channel);
    }
}
