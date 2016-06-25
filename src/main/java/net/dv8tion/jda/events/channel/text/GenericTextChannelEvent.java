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
package net.dv8tion.jda.events.channel.text;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.events.Event;

/**
 * <b><u>GenericTextChannelEvent</u></b><br>
 * Fired whenever a {@link net.dv8tion.jda.entities.TextChannel TextChannel} event is fired.<br>
 * Every TextChannelEvent is an instance of this event and can be casted. (no exceptions)<br>
 * <br>
 * Use: Detect any TextChannelEvent. <i>(No real use for JDA user)</i>
 */
public abstract class GenericTextChannelEvent extends Event
{
    private final TextChannel channel;

    public GenericTextChannelEvent(JDA api, int responseNumber, TextChannel channel)
    {
        super(api, responseNumber);
        this.channel = channel;
    }

    public TextChannel getChannel()
    {
        return channel;
    }

    public Guild getGuild()
    {
        return channel.getGuild();
    }
}
