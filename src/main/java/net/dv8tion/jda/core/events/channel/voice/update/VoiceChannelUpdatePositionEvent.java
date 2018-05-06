/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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
package net.dv8tion.jda.core.events.channel.voice.update;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.VoiceChannel;

/**
 * Indicates that a {@link VoiceChannel VoiceChannel}'s position changed.
 *
 * <p>Can be used to get affected VoiceChannel, affected Guild and previous position.
 *
 * <p>Identifier: {@code position}
 */
public class VoiceChannelUpdatePositionEvent extends GenericVoiceChannelUpdateEvent<Integer>
{
    public static final String IDENTIFIER = "position";

    private final int oldPosition;
    private final int newPosition;

    public VoiceChannelUpdatePositionEvent(JDA api, long responseNumber, VoiceChannel channel, int oldPosition)
    {
        super(api, responseNumber, channel);
        this.oldPosition = oldPosition;
        this.newPosition = channel.getPositionRaw();
    }

    /**
     * The old position
     *
     * @return The old position
     */
    public int getOldPosition()
    {
        return oldPosition;
    }

    /**
     * The new position
     *
     * @return The new position
     */
    public int getNewPosition()
    {
        return newPosition;
    }

    @Override
    public String getPropertyIdentifier()
    {
        return IDENTIFIER;
    }

    @Override
    public Integer getOldValue()
    {
        return oldPosition;
    }

    @Override
    public Integer getNewValue()
    {
        return newPosition;
    }
}
