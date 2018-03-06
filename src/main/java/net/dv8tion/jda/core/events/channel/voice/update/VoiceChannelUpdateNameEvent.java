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
 * Indicates that a {@link VoiceChannel VoiceChannel}'s name changed.
 *
 * <p>Can be used to get affected VoiceChannel, affected Guild and previous name.
 *
 * <p>Identifier: {@code name}
 */
public class VoiceChannelUpdateNameEvent extends GenericVoiceChannelUpdateEvent<String>
{
    public static final String IDENTIFIER = "name";

    private final String oldName;
    private final String newName;

    public VoiceChannelUpdateNameEvent(JDA api, long responseNumber, VoiceChannel channel, String oldName)
    {
        super(api, responseNumber, channel);
        this.oldName = oldName;
        this.newName = channel.getName();
    }

    /**
     * The old name
     *
     * @return The old name
     */
    public String getOldName()
    {
        return oldName;
    }

    /**
     * The new name
     *
     * @return The new name
     */
    public String getNewName()
    {
        return newName;
    }

    @Override
    public String getPropertyIdentifier()
    {
        return IDENTIFIER;
    }

    @Override
    public String getOldValue()
    {
        return oldName;
    }

    @Override
    public String getNewValue()
    {
        return newName;
    }
}
