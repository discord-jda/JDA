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

package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;

import javax.annotation.Nonnull;
import java.time.OffsetDateTime;

/**
 * A {@link ThreadMember} represents a {@link Member Member's} participation in a Thread.
 * <br>ThreadMembers are subscribed to {@link ThreadChannel Threads} and receive updates for them, like new message and thread changes.
 * Only subscribed members are shown in a thread's sidebar.
 */
public interface ThreadMember extends IMentionable
{
    /**
     * Returns the {@link net.dv8tion.jda.api.JDA JDA} instance of this thread member.
     *
     * @return the corresponding JDA instance
     */
    @Nonnull
    JDA getJDA();

    /**
     * The {@link Guild} containing this {@link ThreadMember} and its {@link ThreadChannel}.
     *
     * @return The {@link Guild} containing this {@link ThreadMember ThreadMembers} and its {@link ThreadChannel}.
     */
    @Nonnull
    Guild getGuild();

    /**
     * The {@link ThreadChannel} this thread member is subscribed to.
     *
     * @return The {@link ThreadChannel} this thread member is subscribed to.
     */
    @Nonnull
    ThreadChannel getThread();

    /**
     * The {@link net.dv8tion.jda.api.entities.User User} instance
     * <br>Shortcut for {@code getMember().getUser()}
     *
     * @return The User instance
     */
    //We might not actually be able to provide a user because we only get the `userId` in the ThreadMember object.
    @Nonnull
    User getUser();

    /**
     * The corresponding guild {@link Member} to this thread member.
     *
     * @return The corresponding guild {@link Member} to this thread member.
     */
    @Nonnull
    Member getMember();

    /**
     * The time this {@link ThreadMember} joined the subscribed {@link ThreadChannel}.
     *
     * @return The time this {@link ThreadMember} joined the subscribed {@link ThreadChannel}.
     */
    @Nonnull
    OffsetDateTime getTimeJoined();

    /**
     * True, if this {@link ThreadMember} owns the subscribed {@link ThreadChannel}.
     *
     * @return True, if this {@link ThreadMember} owns the subscribed {@link ThreadChannel}.
     */
    default boolean isThreadOwner()
    {
        return getThread().getOwnerIdLong() == getIdLong();
    }
}
