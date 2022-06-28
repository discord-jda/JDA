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
import net.dv8tion.jda.api.utils.ImageProxy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates that the splash of a {@link net.dv8tion.jda.api.entities.Guild Guild} changed.
 *
 * <p>Can be used to detect when a guild splash changes and retrieve the old one
 *
 * <p>Identifier: {@code splash}
 */
public class GuildUpdateSplashEvent extends GenericGuildUpdateEvent<String>
{
    public static final String IDENTIFIER = "splash";

    public GuildUpdateSplashEvent(@Nonnull JDA api, long responseNumber, @Nonnull Guild guild, @Nullable String oldSplashId)
    {
        super(api, responseNumber, guild, oldSplashId, guild.getSplashId(), IDENTIFIER);
    }

    /**
     * The old splash id
     *
     * @return The old splash id, or null
     */
    @Nullable
    public String getOldSplashId()
    {
        return getOldValue();
    }

    /**
     * The url of the old splash
     *
     * @return The url of the old splash, or null
     */
    @Nullable
    public String getOldSplashUrl()
    {
        return previous == null ? null : String.format(Guild.SPLASH_URL, guild.getId(), previous);
    }

    /**
     * Returns an {@link ImageProxy} for this guild's old splash image.
     * <p>
     * <b>Note:</b> the old splash may not always be downloadable as it might have been removed from Discord.
     *
     * @return Possibly-null {@link ImageProxy} of this guild's old splash image
     *
     * @see    #getOldSplashUrl() ()
     */
    @Nullable
    public ImageProxy getOldSplash()
    {
        final String oldSplashUrl = getOldSplashUrl();
        return oldSplashUrl == null ? null : new ImageProxy(oldSplashUrl);
    }

    /**
     * The new splash id
     *
     * @return The new splash id, or null
     */
    @Nullable
    public String getNewSplashId()
    {
        return getNewValue();
    }

    /**
     * The url of the new splash
     *
     * @return The url of the new splash, or null
     */
    @Nullable
    public String getNewSplashUrl()
    {
        return next == null ? null : String.format(Guild.SPLASH_URL, guild.getId(), next);
    }

    /**
     * Returns an {@link ImageProxy} for this guild's new splash image.
     *
     * @return Possibly-null {@link ImageProxy} of this guild's new splash image
     *
     * @see    #getNewSplashUrl()
     */
    @Nullable
    public ImageProxy getNewSplash()
    {
        final String newSplashUrl = getNewSplashUrl();
        return newSplashUrl == null ? null : new ImageProxy(newSplashUrl);
    }
}
