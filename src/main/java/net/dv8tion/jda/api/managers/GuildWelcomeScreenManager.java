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
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Manager providing functionality to update one or more fields for a {@link GuildWelcomeScreen}.
 *
 * <p><b>Example</b>
 * <pre>{@code
 * manager.setEnabled(false)
 *        .setDescription(null)
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
public interface GuildWelcomeScreenManager extends Manager<GuildWelcomeScreenManager>
{
    /** Used to reset the enabled field */
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

    /**
     * Sets the enabled state of the welcome screen.
     *
     * @param  enabled
     *         {@code True} if the welcome screen should be enabled
     *
     * @return GuildWelcomeScreenManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    GuildWelcomeScreenManager setEnabled(boolean enabled);

    /**
     * Sets the description of the welcome screen.
     *
     * <p>The description must not be longer than {@value GuildWelcomeScreen#MAX_DESCRIPTION_LENGTH}
     *
     * @param  description
     *         The new description of the welcome screen, or {@code null} to remove the description
     *
     * @throws IllegalArgumentException
     *         If the description longer than {@value GuildWelcomeScreen#MAX_DESCRIPTION_LENGTH}
     *
     * @return GuildWelcomeScreenManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    GuildWelcomeScreenManager setDescription(@Nullable String description);

    /**
     * Returns an immutable list of the welcome channels
     * <br>These channels are those which are <b>being modified</b>, not the ones currently shown on Discord
     *
     * @return An immutable list of the welcome channels to be set by the manager
     */
    @Nonnull
    List<GuildWelcomeScreen.Channel> getWelcomeChannels();

    /**
     * Removes all welcome channels.
     *
     * @return GuildWelcomeScreenManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    GuildWelcomeScreenManager clearWelcomeChannels();

    /**
     * Sets the welcome channels of the welcome screen.
     *
     * <p>The order of the {@link Collection} defines in what order the channels appear on Discord.
     *
     * @param  channels
     *         The new welcome channels to use, can be an empty list to remove all welcome channels.
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code channels} is {@code null}</li>
     *             <li>If more than {@value GuildWelcomeScreen#MAX_WELCOME_CHANNELS} welcome channels are set</li>
     *         </ul>
     *
     * @return GuildWelcomeScreenManager for chaining convenience
     *
     * @see    #setWelcomeChannels(GuildWelcomeScreen.Channel...)
     */
    @Nonnull
    @CheckReturnValue
    GuildWelcomeScreenManager setWelcomeChannels(@Nonnull Collection<? extends GuildWelcomeScreen.Channel> channels);

    /**
     * Sets the welcome channels of the welcome screen.
     *
     * <p>The order of the parameters defines in what order the channels appear on Discord.
     *
     * @param  channels
     *         The new welcome channels to use, you can provide nothing in order to remove all welcome channels.
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If {@code channels} is {@code null}</li>
     *             <li>If more than {@value GuildWelcomeScreen#MAX_WELCOME_CHANNELS} welcome channels are set</li>
     *         </ul>
     *
     * @return GuildWelcomeScreenManager for chaining convenience
     *
     * @see    #setWelcomeChannels(Collection)
     */
    @Nonnull
    @CheckReturnValue
    default GuildWelcomeScreenManager setWelcomeChannels(@Nonnull GuildWelcomeScreen.Channel... channels)
    {
        Checks.notNull(channels, "Welcome channels");
        return setWelcomeChannels(Arrays.asList(channels));
    }
}
