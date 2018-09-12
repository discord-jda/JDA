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

import net.dv8tion.jda.annotations.Incubating;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.TextChannel;

// TODO Docs
@Incubating
public class TextChannelUpdateUserRateLimitEvent extends GenericTextChannelUpdateEvent<Integer>
{
    // We have reached spring bootstrap levels of class name.

    public static final String IDENTIFIER = "rate_limit_per_user";

    public TextChannelUpdateUserRateLimitEvent(JDA api, long responseNumber, TextChannel channel, int oldRateLimitPerUser)
    {
        super(api, responseNumber, channel, oldRateLimitPerUser, channel.getRateLimitPerUser(), IDENTIFIER);
    }

    /**
     * The old rate-limit per user.
     *
     * @return The old rate-limit per user.
     */
    public int getOldRateLimitPerUser()
    {
        return getOldValue();
    }

    /**
     * The old rate-limit per user.
     *
     * @return The old rate-limit per user.
     */
    public int getNewRateLimitPerUser()
    {
        return getNewValue();
    }
}
