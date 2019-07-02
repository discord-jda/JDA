/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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
import net.dv8tion.jda.api.entities.bean.GuildData;
import net.dv8tion.jda.internal.entities.GuildImpl;

import javax.annotation.Nonnull;

@SuppressWarnings("ConstantConditions")
public class GuildUpdateDigestEvent extends GenericGuildUpdateEvent<GuildData>
{
    public static final String IDENTIFIER = "guild-data";

    public GuildUpdateDigestEvent(@Nonnull JDA api, long responseNumber, @Nonnull Guild guild, @Nonnull GuildData previous)
    {
        super(api, responseNumber, guild, previous, ((GuildImpl) guild).getMutableGuildData(), IDENTIFIER);
    }

    @Nonnull
    @Override
    public GuildData getOldValue()
    {
        return super.getOldValue();
    }

    @Nonnull
    @Override
    public GuildData getNewValue()
    {
        return super.getNewValue();
    }
}
