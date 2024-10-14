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

package net.dv8tion.jda.api.interactions.commands.build.attributes;

import javax.annotation.Nonnull;

//TODO docs
public interface IScopedCommandData extends INamedCommandData
{
    /**
     * Sets whether this command is only usable in a guild (Default: false).
     * <br>This only has an effect if this command is registered globally.
     *
     * @param  guildOnly
     *         Whether to restrict this command to guilds
     *
     * @return The builder instance, for chaining
     */
    @Nonnull
    IScopedCommandData setGuildOnly(boolean guildOnly);

    /**
     * Whether the command can only be used inside a guild.
     * <br>Always true for guild commands.
     *
     * @return True, if this command is restricted to guilds.
     */
    boolean isGuildOnly();
}
