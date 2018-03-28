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
 * Indicates that the Icon of a {@link net.dv8tion.jda.core.entities.Guild Guild} changed.
 *
 * <p>Can be used to detect when a guild icon changes and retrieve the old one
 *
 * <p>Identifier: {@code icon}
 */
public class GuildUpdateIconEvent extends GenericGuildUpdateEvent<String>
{
    public static final String IDENTIFIER = "icon";

    public GuildUpdateIconEvent(JDA api, long responseNumber, Guild guild, String oldIconId)
    {
        super(api, responseNumber, guild, oldIconId, guild.getIconId(), IDENTIFIER);
    }

    /**
     * The old icon id
     *
     * @return The old icon id, or null
     */
    public String getOldIconId()
    {
        return getOldValue();
    }

    /**
     * The url of the old icon
     *
     * @return The url of the old icon, or null
     */
    public String getOldIconUrl()
    {
        return previous == null ? null : "https://cdn.discordapp.com/icons/" + guild.getId() + "/" + previous + ".png";
    }

    /**
     * The old icon id
     *
     * @return The old icon id, or null
     */
    public String getNewIconId()
    {
        return getNewValue();
    }

    /**
     * The url of the new icon
     *
     * @return The url of the new icon, or null
     */
    public String getNewIconUrl()
    {
        return next == null ? null : "https://cdn.discordapp.com/icons/" + guild.getId() + "/" + next + ".png";
    }
}
