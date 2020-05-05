/*
 * Copyright 2015-2020 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

package net.dv8tion.jda.api.events.guild.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates that the owner of a {@link net.dv8tion.jda.api.entities.Guild Guild} changed.
 *
 * <p>Can be used to detect when an owner of a guild changes and retrieve the old one
 *
 * <p>Identifier: {@code owner}
 */
public class GuildUpdateOwnerEvent extends GenericGuildUpdateEvent<Member>
{
    public static final String IDENTIFIER = "owner";
    private final long prevId, nextId;

    public GuildUpdateOwnerEvent(@Nonnull JDA api, long responseNumber, @Nonnull Guild guild, @Nullable Member oldOwner,
                                 long prevId, long nextId)
    {
        super(api, responseNumber, guild, oldOwner, guild.getOwner(), IDENTIFIER);
        this.prevId = prevId;
        this.nextId = nextId;
    }

    /**
     * The previous owner user id
     *
     * @return The previous owner id
     */
    public long getNewOwnerIdLong()
    {
        return nextId;
    }

    /**
     * The previous owner user id
     *
     * @return The previous owner id
     */
    @Nonnull
    public String getNewOwnerId()
    {
        return Long.toUnsignedString(nextId);
    }

    /**
     * The new owner user id
     *
     * @return The new owner id
     */
    public long getOldOwnerIdLong()
    {
        return prevId;
    }

    /**
     * The new owner user id
     *
     * @return The new owner id
     */
    @Nonnull
    public String getOldOwnerId()
    {
        return Long.toUnsignedString(prevId);
    }

    /**
     * The old owner
     *
     * @return The old owner
     */
    @Nullable
    public Member getOldOwner()
    {
        return getOldValue();
    }

    /**
     * The new owner
     *
     * @return The new owner
     */
    @Nullable
    public Member getNewOwner()
    {
        return getNewValue();
    }
}
