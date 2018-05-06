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
package net.dv8tion.jda.core.events.channel.text.update;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.TextChannel;

/**
 * Indicates that a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}'s NSFW status changed.
 *
 * <p>Can be used to detect when a TextChannel NSFW status changes and get its previous value.
 *
 * <p>Identifier: {@code nsfw}
 */
public class TextChannelUpdateNSFWEvent extends GenericTextChannelUpdateEvent<Boolean>
{
    public static final String IDENTIFIER = "nsfw";

    public TextChannelUpdateNSFWEvent(JDA api, long responseNumber, TextChannel channel, boolean oldNsfw)
    {
        super(api, responseNumber, channel, oldNsfw, channel.isNSFW(), IDENTIFIER);
    }

    /**
     * Whether the channel was marked NSFW before
     *
     * @return True, if the channel was marked NSFW before
     */
    public boolean getOldNSFW()
    {
        return getOldValue();
    }
}
