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

import net.dv8tion.jda.Region;

import java.util.List;

/**
 * Represents a Discord Guild. This should contain all information provided from Discord about a Guild.
 */
public interface Guild
{
    /**
     * The Id of the Guild. This is typically 18 characters long.
     * @return
     */
    String getId();

    /**
     * The human readable name of the Guild. If no name has been set, this returns null.
     * @return
     */
    String getName();

    /**
     * The Discord Id for this server's Icon image. If no icon has been set, this returns null.
     * @return
     */
    String getIconId();

    /**
     * The URL for this server's Icon image. If no icon has been set, this returns null.
     * @return
     */
    String getIconUrl();

    /**
     * The Id of the AFK Voice Channel.
     * @return
     */
    String getAfkChannelId();

    /**
     * The user Id of the owner of this Guild. Currently, there is no way to transfer ownership of a discord
     *   Guild, and a such this user is also the original creator.
     * @return
     */
    String getOwnerId();

    /**
     * The amount of time (in seconds) that must pass with no activity to be considered AFK by this server.
     * Default is 300 seconds (5 minutes)
     * @return
     */
    int getAfkTimeout();

    /**
     * The Region that this server exists in.
     * @return
     */
    Region getRegion();

    /**
     * The text based Channels available on the Guild.
     * @return
     *      An Immutable List of Channels.
     */
    List<TextChannel> getTextChannels();

    /**
     * The VoiceChannels available on the Guild.
     * @return
     *      An Immutable List of VoiceChannels.
     */
    List<VoiceChannel> getVoiceChannels();

    /**
     * The Roles of this Guild
     *
     * @return An Immutable List of Roles
     */
    List<Role> getRoles();

    List<Role> getRolesForUser(User user);

}
