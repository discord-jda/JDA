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

package net.dv8tion.jda.api.interactions.commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import javax.annotation.Nonnull;

public interface UserCommandInteraction extends CommandInteraction
{
    /**
     * The id of the user who undergoes this interaction.
     *
     * @return The id
     */
    long getInteractedIdLong();

    /**
     * The id of the user who undergoes this interaction.
     *
     * @return The id
     */
    @Nonnull
    default String getInteractedId() {
        return Long.toUnsignedString(getInteractedIdLong());
    }

    /**
     * The {@link User} who undergoes this interaction.
     *
     * @return The {@link User}
     */
    @Nonnull
    User getInteractedUser();

    /**
     * The {@link Member} who undergoes this interaction.
     * <br>This is null if the interaction is not from a guild.
     *
     * @return The {@link Member}
     */
    Member getInteractedMember();
}
