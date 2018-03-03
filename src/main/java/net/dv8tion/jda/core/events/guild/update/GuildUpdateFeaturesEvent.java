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

import java.util.Set;

/**
 * Indicates that the features of a {@link net.dv8tion.jda.core.entities.Guild Guild} changed.
 *
 * <p>Can be used to detect when the features change and retrieve the old ones
 */
public class GuildUpdateFeaturesEvent extends GenericGuildUpdateEvent
{
    private final Set<String> oldFeatures;

    public GuildUpdateFeaturesEvent(JDA api, long responseNumber, Guild guild, Set<String> oldFeatures)
    {
        super(api, responseNumber, guild);
        this.oldFeatures = oldFeatures;
    }

    /**
     * The old Set of features before the {@link net.dv8tion.jda.core.entities.Guild Guild} update.
     *
     * @return Never-null, unmodifiable Set of the old features
     */
    public Set<String> getOldFeatures()
    {
        return oldFeatures;
    }
}
