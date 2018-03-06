/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.core.events.guild.update;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;

/**
 * Indicates that the splash of a {@link net.dv8tion.jda.core.entities.Guild Guild} changed.
 *
 * <p>Can be used to detect when a guild splash changes and retrieve the old one
 *
 * <p>Identifier: {@code splash}
 */
public class GuildUpdateSplashEvent extends GenericGuildUpdateEvent<String>
{
    public static final String IDENTIFIER = "splash";

    private final String oldSplashId;
    private final String newSplashId;

    public GuildUpdateSplashEvent(JDA api, long responseNumber, Guild guild, String oldSplashId)
    {
        super(api, responseNumber, guild);
        this.oldSplashId = oldSplashId;
        this.newSplashId = guild.getSplashId();
    }

    /**
     * The old splash id
     *
     * @return The old splash id, or null
     */
    public String getOldSplashId()
    {
        return oldSplashId;
    }

    /**
     * The url of the old splash
     *
     * @return The url of the old splash, or null
     */
    public String getOldSplashUrl()
    {
        return oldSplashId == null ? null : "https://cdn.discordapp.com/splashes/" + guild.getId() + "/" + oldSplashId + ".jpg";
    }

    /**
     * The new splash id
     *
     * @return The new splash id, or null
     */
    public String getNewSplashId()
    {
        return newSplashId;
    }

    /**
     * The url of the new splash
     *
     * @return The url of the new splash, or null
     */
    public String getNewSplashUrl()
    {
        return newSplashId == null ? null : "https://cdn.discordapp.com/splashes/" + guild.getId() + "/" + newSplashId + ".jpg";
    }

    @Override
    public String getPropertyIdentifier()
    {
        return IDENTIFIER;
    }

    @Override
    public String getOldValue()
    {
        return oldSplashId;
    }

    @Override
    public String getNewValue()
    {
        return newSplashId;
    }
}
