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

package net.dv8tion.jda.api.events.guild;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

import javax.annotation.Nonnull;

/**
 * Indicates that a {@link net.dv8tion.jda.api.entities.Guild Guild} became available.
 * <br>This is fired when a guild that was previously marked as unavailable by a {@link GuildUnavailableEvent} has become available again.
 *
 * <p>Can be used to detect that a Guild will now start sending events and can be interacted with.
 */
public class GuildAvailableEvent extends GenericGuildEvent
{
    public GuildAvailableEvent(@Nonnull JDA api, long responseNumber, @Nonnull Guild guild)
    {
        super(api, responseNumber, guild);
    }
}
