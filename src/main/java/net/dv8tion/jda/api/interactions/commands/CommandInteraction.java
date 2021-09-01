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

import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.InteractionType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

/**
 * Interaction of a generic command
 */
public interface CommandInteraction extends Interaction
{
    /**
     * The command name.
     * <br>This can be useful for abstractions.
     *
     * @return The command name
     */
    @Nonnull
    String getName();

    /**
     * The command id
     *
     * @return The command id
     */
    long getCommandIdLong();

    /**
     * The command id
     *
     * @return The command id
     */
    @Nonnull
    default String getCommandId()
    {
        return Long.toUnsignedString(getCommandIdLong());
    }

    @NotNull
    @Override
    default InteractionType getType()
    {
        return InteractionType.COMMAND;
    }
}
