/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda.api.events.channel.voice.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.VoiceChannel;

import javax.annotation.Nonnull;

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

    public VoiceChannelUpdateUserLimitEvent(@Nonnull JDA api, long responseNumber, @Nonnull VoiceChannel channel, int oldUserLimit)
    {
        super(api, responseNumber, channel, oldUserLimit, channel.getUserLimit(), IDENTIFIER);
    }

    /**
     * The old userlimit
     *
     * @return The old userlimit, or 0 if there is no limit set.
     */
    public int getOldUserLimit()
    {
        return getOldValue();
    }

    /**
     * The new userlimit
     *
     * @return The new userlimit, or 0 if there is no limit set.
     */
    public int getNewUserLimit()
    {
        return getNewValue();
    }
}
