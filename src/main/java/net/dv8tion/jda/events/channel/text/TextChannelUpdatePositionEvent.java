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
import net.dv8tion.jda.entities.TextChannel;

/**
 * <b><u>TextChannelUpdatePositionEvent</u></b><br>
 * Fired if a {@link net.dv8tion.jda.entities.TextChannel TextChannel}'s position changes.<br>
 * <br>
 * Use: Detect when a TextChannel position changes and get it's previous position.
 */
public class TextChannelUpdatePositionEvent extends GenericTextChannelUpdateEvent
{
    private final int oldPosition;

    public TextChannelUpdatePositionEvent(JDA api, int responseNumber, TextChannel channel, int oldPosition)
    {
        super(api, responseNumber, channel);
        this.oldPosition = oldPosition;
    }

    public int getOldPosition()
    {
        return oldPosition;
    }
}
