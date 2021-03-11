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

package net.dv8tion.jda.api.events.guild.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

import javax.annotation.Nonnull;
import java.util.Locale;

/**
 * Indicates that the {@link Locale} of a {@link net.dv8tion.jda.api.entities.Guild Guild} changed.
 *
 * <p>Can be used to detect when a Locale changes and retrieve the old one
 *
 * <p>Identifier: {@code locale}
 */
@SuppressWarnings("ConstantConditions")
public class GuildUpdateLocaleEvent extends GenericGuildUpdateEvent<Locale>
{
    public static final String IDENTIFIER = "locale";

    public GuildUpdateLocaleEvent(@Nonnull JDA api, long responseNumber, @Nonnull Guild guild, @Nonnull Locale previous)
    {
        super(api, responseNumber, guild, previous, guild.getLocale(), IDENTIFIER);
    }

    @Nonnull
    @Override
    public Locale getOldValue()
    {
        return super.getOldValue();
    }

    @Nonnull
    @Override
    public Locale getNewValue()
    {
        return super.getNewValue();
    }
}
