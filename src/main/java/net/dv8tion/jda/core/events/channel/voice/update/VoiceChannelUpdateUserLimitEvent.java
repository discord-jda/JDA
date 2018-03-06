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
 * Indicates that a {@link VoiceChannel VoiceChannel}'s user limit changed.
 *
 * <p>Can be used to get affected VoiceChannel, affected Guild and previous user limit.
 *
 * <p>Identifier: {@code userlimit}
 */
public class VoiceChannelUpdateUserLimitEvent extends GenericVoiceChannelUpdateEvent<Integer>
{
    public static final String IDENTIFIER = "userlimit";

    private final int oldUserLimit;
    private final int newUserLimit;

    public VoiceChannelUpdateUserLimitEvent(JDA api, long responseNumber, VoiceChannel channel, int oldUserLimit)
    {
        super(api, responseNumber, channel);
        this.oldUserLimit = oldUserLimit;
        this.newUserLimit = channel.getUserLimit();
    }

    /**
     * The old userlimit
     *
     * @return The old userlimit
     */
    public int getOldUserLimit()
    {
        return oldUserLimit;
    }

    /**
     * The new userlimit
     *
     * @return The new userlimit
     */
    public int getNewUserLimit()
    {
        return newUserLimit;
    }

    @Override
    public String getPropertyIdentifier()
    {
        return IDENTIFIER;
    }

    @Override
    public Integer getOldValue()
    {
        return oldUserLimit;
    }

    @Override
    public Integer getNewValue()
    {
        return newUserLimit;
    }
}
