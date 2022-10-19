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

package net.dv8tion.jda.internal.entities;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildWelcomeScreen;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class GuildWelcomeScreenImpl implements GuildWelcomeScreen
{
    private final Guild guild;
    private final String description;
    private final List<Channel> channels;

    public GuildWelcomeScreenImpl(@Nullable Guild guild, @Nullable String description, @Nonnull List<Channel> channels)
    {
        this.guild = guild;
        this.description = description;
        this.channels = channels;
    }

    @Nullable
    @Override
    public Guild getGuild()
    {
        return guild;
    }

    @Nullable
    @Override
    public String getDescription()
    {
        return description;
    }

    @Nonnull
    @Override
    public List<Channel> getChannels()
    {
        return channels;
    }
}
