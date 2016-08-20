/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
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
 *
 */
package net.dv8tion.jda.entities;

import java.util.List;

public interface Emote
{

    /**
     * The Id of this Emote
     *
     * @return The emote ID associated with this emote.
     */
    String getId();

    /**
     * The name of this emote
     *
     * @return The name associated with this emote
     */
    String getName();

    /**
     * Some emotes are not available to the user because they do not share a server with that emote.
     *
     * @return Whether the emote is available or not.
     */
    boolean isAvailable();

    /**
     * The {@link net.dv8tion.jda.entities.Guild Guild} using this emote.
     *
     * @return The guild using this emote or null if none of the to us known guilds use it.
     */
    Guild getGuild();

    /**
     * <b><u>This method is deprecated and will be removed soon. Use {@link #getGuild()} instead.</u></b>
     * @return List
     */
    @Deprecated
    List<Guild> getGuilds();

    /**
     * Returns a URL as String that leads to the image displayed by this emote.
     *
     * @return A URL to this emote's image.
     */
    String getImageUrl();

    /**
     * Return a formatted usable version of this emote (&lt;:name:id&gt;)
     *
     * @return A formatted version of this emote.
     */
    String getAsEmote();

}
