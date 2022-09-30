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

/**
 * Manager providing functionality to update one or more fields for a {@link GuildWelcomeScreen}.
 *
 * <p><b>Example</b>
 * <pre>{@code
 * manager.setEnabled(false)
 *        .clearDescription()
 *        .setWelcomeChannels()
 *        .queue();
 * manager.setEnabled(true)
 *        .setDescription("Bot desc")
 *        .setWelcomeChannels(Arrays.asList(
 *                GuildWelcomeScreen.Channel.of(rulesChannel, "Read the rules first"),
 *                GuildWelcomeScreen.Channel.of(generalChannel, "Go have a chat", Emoji.fromUnicode("U+1F4AC"))
 *        ))
 *        .queue();
 * }</pre>
 *
 * @see Guild#modifyWelcomeScreen()
 */
//TODO docs
public interface GuildWelcomeScreenManager extends Manager<GuildWelcomeScreenManager>
{
    /** Used to reset the description field */
    long ENABLED     = 1;
    /** Used to reset the description field */
    long DESCRIPTION = 1 << 1;
    /** Used to reset the channels field */
    long CHANNELS    = 1 << 2;

    /**
     * The {@link Guild} this Manager's {@link GuildWelcomeScreen} is in.
     *
     * @return The parent {@link Guild}
     */
    @Nonnull
    Guild getGuild();

    /**
     * Resets the fields specified by the provided bit-flag pattern.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br>Example: {@code manager.reset(GuildWelcomeScreenManager.DESCRIPTION | GuildWelcomeScreenManager.CHANNELS);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #ENABLED}</li>
     *     <li>{@link #DESCRIPTION}</li>
     *     <li>{@link #CHANNELS}</li>
     * </ul>
     *
     * @param  fields
     *         Integer value containing the flags to reset.
     *
     * @return GuildWelcomeScreenManager for chaining convenience
     */
    @Nonnull
    @Override
    GuildWelcomeScreenManager reset(long fields);

    /**
     * Resets the specified fields.
     * <br>Example: {@code manager.reset(GuildWelcomeScreenManager.DESCRIPTION, GuildWelcomeScreenManager.CHANNELS);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #ENABLED}</li>
     *     <li>{@link #DESCRIPTION}</li>
     *     <li>{@link #CHANNELS}</li>
     * </ul>
     *
     * @param  fields
     *         Integer value containing the flags to reset.
     *
     * @return GuildWelcomeScreenManager for chaining convenience
     */
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
