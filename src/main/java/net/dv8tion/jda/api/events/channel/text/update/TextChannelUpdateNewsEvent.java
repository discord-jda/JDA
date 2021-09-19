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
 * Indicates that a {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}'s has been converted into a news channel
 * or reverted into a normal channel.
 *
 * <p>Can be used to detect when a TextChannel becomes a news channel.
 *
 * <p>Identifier: {@code news}
 *
 * @since  4.2.1
 */
@SuppressWarnings("ConstantConditions")
public class TextChannelUpdateNewsEvent extends GenericTextChannelUpdateEvent<Boolean>
{
    public static final String IDENTIFIER = "news";

    public TextChannelUpdateNewsEvent(@Nonnull JDA api, long responseNumber, @Nonnull TextChannel channel)
    {
        //TODO-v5: Address this event as TextChannels no longer have isNews on them anymore.
        super(api, responseNumber, channel, false, true, IDENTIFIER);
    }

    @Nonnull
    @Override
    public Boolean getOldValue()
    {
        return super.getOldValue();
    }

    @Nonnull
    @Override
    public Boolean getNewValue()
    {
        return super.getNewValue();
    }
}
