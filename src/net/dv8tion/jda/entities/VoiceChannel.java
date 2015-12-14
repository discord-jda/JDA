/**
 *    Copyright 2015 Austin Keener & Michael Ritter
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
package net.dv8tion.jda.entities;

import java.util.List;

/**
 * Represents a Discord Voice Channel.
 *
 * Place holder for now until we implement voice support.
 */
public interface VoiceChannel
{
    /**
     * The Id of the Channel. This is typically 18 characters long.
     * @return
     */
    String getId();

    /**
     * The human readable name of the Channel. If no name has been set, this returns null.
     * @return
     */
    String getName();

    /**
     * Returns the Guild that this Channel is a part of.
     * @return
     */
    Guild getGuild();

    /**
     * An Immutable {@link List} of every {@link User} that is currently connected to this {@link VoiceChannel}.
     * If there are none currently connected this List will be empty.
     *
     * @return
     *      {@link List}<{@link User}> containing all connected users.
     */
    List<User> getUsers();
}
