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

package net.dv8tion.jda.api.managers;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildWelcomeScreen;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

//TODO docs
public interface GuildWelcomeScreenManager extends Manager<GuildWelcomeScreenManager>
{
    /** Used to reset the description field */
    long ENABLED     = 1;
    /** Used to reset the description field */
    long DESCRIPTION = 1 << 1;
    /** Used to reset the channels field */
    long CHANNELS    = 1 << 2;

    @Nonnull
    Guild getGuild();

    @Nonnull
    @Override
    GuildWelcomeScreenManager reset(long fields);

    @Nonnull
    @Override
    GuildWelcomeScreenManager reset(long... fields);

    @Nonnull
    @CheckReturnValue
    GuildWelcomeScreenManager setEnabled(boolean enabled);

    @Nonnull //Can be blank
    @CheckReturnValue
    GuildWelcomeScreenManager setDescription(@Nullable String description);

    @Nonnull
    @CheckReturnValue
    default GuildWelcomeScreenManager clearDescription()
    {
        return setDescription(null);
    }

    //unmodifiable
    @Nonnull
    List<GuildWelcomeScreen.Channel> getWelcomeChannels();

    @Nonnull
    @CheckReturnValue
    GuildWelcomeScreenManager clearWelcomeChannels();

    //Used to set channels AND order
    @Nonnull
    @CheckReturnValue
    GuildWelcomeScreenManager setWelcomeChannels(@Nonnull List<GuildWelcomeScreen.Channel> channels);

    @Nonnull
    @CheckReturnValue
    default GuildWelcomeScreenManager setWelcomeChannels(@Nonnull GuildWelcomeScreen.Channel... channels)
    {
        return setWelcomeChannels(Arrays.asList(channels));
    }

    //Add to last
    @Nonnull
    @CheckReturnValue
    GuildWelcomeScreenManager addWelcomeChannel(@Nonnull GuildWelcomeScreen.Channel channel);

    //Remove ig
    @Nonnull
    @CheckReturnValue
    GuildWelcomeScreenManager removeWelcomeChannel(@Nonnull GuildWelcomeScreen.Channel channel);
}
