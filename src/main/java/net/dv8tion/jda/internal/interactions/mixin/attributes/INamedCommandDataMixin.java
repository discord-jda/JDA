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
import net.dv8tion.jda.api.interactions.commands.build.attributes.INamedCommandData;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;

public interface INamedCommandDataMixin extends INamedCommandData
{
    default void checkName(@Nonnull String name)
    {
        Checks.inRange(name, 1, MAX_NAME_LENGTH, "Name");
        if (getType() == Command.Type.SLASH)
        {
            Checks.matches(name, Checks.ALPHANUMERIC_WITH_DASH, "Name");
            Checks.isLowercase(name, "Name");
        }
    }
}
