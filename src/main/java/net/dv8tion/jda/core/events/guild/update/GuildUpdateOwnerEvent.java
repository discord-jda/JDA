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

package net.dv8tion.jda.core.events.guild.update;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;

/**
 * Indicates that the owner of a {@link net.dv8tion.jda.core.entities.Guild Guild} changed.
 *
 * <p>Can be used to detect when an owner of a guild changes and retrieve the old one
 *
 * <p>Identifier: {@code owner}
 */
public class GuildUpdateOwnerEvent extends GenericGuildUpdateEvent<Member>
{
    public static final String IDENTIFIER = "owner";

    public GuildUpdateOwnerEvent(JDA api, long responseNumber, Guild guild, Member oldOwner)
    {
        super(api, responseNumber, guild, oldOwner, guild.getOwner(), IDENTIFIER);
    }

    /**
     * The old owner
     *
     * @return The old owner
     */
    public Member getOldOwner()
    {
        return getOldValue();
    }

    /**
     * The new owner
     *
     * @return The new owner
     */
    public Member getNewOwner()
    {
        return getNewValue();
    }
}
