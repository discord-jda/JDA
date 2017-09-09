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
package net.dv8tion.jda.core.events.channel.category;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.CategoryChannel;
import net.dv8tion.jda.core.events.Event;

/**
 * <b><u>GenericCategoryChannelEvent</u></b><br>
 * Fired whenever a {@link CategoryChannel CategoryChannel} event is fired.<br>
 * Every CategoryChannelEvent is an instance of this event and can be casted. (no exceptions)<br>
 * <br>
 * Use: Detect any CategoryChannelEvent. <i>(No real use for JDA user)</i>
 */
public abstract class GenericCategoryChannelEvent extends Event
{
    private final CategoryChannel channel;

    public GenericCategoryChannelEvent(JDA api, long responseNumber, CategoryChannel channel)
    {
        super(api, responseNumber);
        this.channel = channel;
    }

    public CategoryChannel getChannel()
    {
        return channel;
    }

    public Guild getGuild()
    {
        return channel.getGuild();
    }
}
