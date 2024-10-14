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

package net.dv8tion.jda.internal.interactions.mixin.attributes;

import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.attributes.IDescribedCommandData;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;

public interface IDescribedCommandDataMixin extends INamedCommandDataMixin, IDescribedCommandData
{
    default void checkDescription(@Nonnull String description)
    {
        if (getType() != Command.Type.SLASH && getType() != Command.Type.PRIMARY_ENTRY_POINT)
            throw new IllegalStateException("Cannot set description for commands of type " + getType());
        Checks.inRange(description, 1, MAX_DESCRIPTION_LENGTH, "Description");
    }
}
