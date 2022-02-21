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

package net.dv8tion.jda.api.interactions.commands.context;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Interaction with a user context menu command
 */
public interface UserContextInteraction extends ContextInteraction<User>
{
    @Nonnull
    @Override
    default ContextTarget getTargetType()
    {
        return ContextTarget.USER;
    }

    /**
     * If this context menu command was used in a {@link net.dv8tion.jda.api.entities.Guild Guild},
     * this returns the member instance for the target user.
     *
     * @return The target member instance, or null if this was not in a guild.
     */
    @Nullable
    Member getTargetMember();
}
