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
package net.dv8tion.jda.api.events.channel.text.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;

import javax.annotation.Nonnull;

/**
 * Indicates that a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}'s slowmode changed.
 *
 * <p>Can be used to detect when a TextChannel slowmode changes and get its previous value.
 *
 * <p>Identifier: {@code slowmode}
 */
public class TextChannelUpdateSlowmodeEvent extends GenericTextChannelUpdateEvent<Integer>
{
    public static final String IDENTIFIER = "slowmode";

    public TextChannelUpdateSlowmodeEvent(@Nonnull JDA api, long responseNumber, @Nonnull TextChannel channel, int oldSlowmode)
    {
        super(api, responseNumber, channel, oldSlowmode, channel.getSlowmode(), IDENTIFIER);
    }

    /**
     * The old slowmode.
     *
     * @return The old slowmode.
     */
    public int getOldSlowmode()
    {
        return getOldValue();
    }

    /**
     * The new slowmode.
     *
     * @return The new slowmode.
     */
    public int getNewSlowmode()
    {
        return getNewValue();
    }
}
