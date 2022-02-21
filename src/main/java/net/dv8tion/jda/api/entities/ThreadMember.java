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

import javax.annotation.Nonnull;
import java.time.OffsetDateTime;

//TODO-v5: docs
public interface ThreadMember extends IMentionable
{
    @Nonnull
    JDA getJDA();

    @Nonnull
    Guild getGuild();

    @Nonnull
    ThreadChannel getThread();

    //We might not actually be able to provide a user because we only get the `userId` in the ThreadMember object.
    @Nonnull
    User getUser();

    @Nonnull
    Member getMember();

    @Nonnull
    OffsetDateTime getTimeJoined();

    //TODO | Set<ThreadMemberFlags> getFlags();

    long getFlagsRaw();

    default boolean isThreadOwner()
    {
        return getThread().getOwnerIdLong() == getIdLong();
    }
}
