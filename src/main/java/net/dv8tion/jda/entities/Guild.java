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

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.Region;

import java.util.List;

/**
 * Represents a Discord {@link net.dv8tion.jda.entities.Guild Guild}. This should contain all information provided from Discord about a Guild.
 */
public interface Guild
{
    /**
     * The Id of the {@link net.dv8tion.jda.entities.Guild Guild}. This is typically 18 characters long.
     *
     * @return
     *      Never-null String containing the Id.
     */
    String getId();

    /**
     * The human readable name of the {@link net.dv8tion.jda.entities.Guild Guild}. If no name has been set, this returns null.
     *
     * @return
     *      Never-null String containing the Guild's name.
     */
    String getName();

    /**
     * The Discord Id of the {@link net.dv8tion.jda.entities.Guild Guild} icon image. If no icon has been set, this returns null.
     *
     * @return
     *      Possibly-null String containing the Guild's icon id.
     */
    String getIconId();

    /**
     * The URL of the {@link net.dv8tion.jda.entities.Guild Guild} icon image. If no icon has been set, this returns null.
     *
     * @return
     *      Possibly-null String containing the Guild's icon URL.
     */
    String getIconUrl();

    /**
     * The Id of the AFK Voice Channel.
     *
     * @return
     *      Never-null String containing the AFK Voice Channel id.
     */
    String getAfkChannelId();

    /**
     * The {@link net.dv8tion.jda.entities.User User} Id of the owner of this {@link net.dv8tion.jda.entities.Guild Guild}.<br>
     * Currently, there is no way to transfer ownership of a discord {@link net.dv8tion.jda.entities.Guild Guild},
     *   and a such this {@link net.dv8tion.jda.entities.User User} is also the original creator.
     *
     * @return
     *      Never-null String containing the Guild owner's User id.
     */
    String getOwnerId();

    /**
     * The amount of time (in seconds) that must pass with no activity to be considered AFK by this {@link net.dv8tion.jda.entities.Guild Guild}.
     * Default is 300 seconds (5 minutes)
     *
     * @return
     *      Positive int representing the timeout value.
     */
    int getAfkTimeout();

    /**
     * The {@link net.dv8tion.jda.Region Region} that this {@link net.dv8tion.jda.entities.Guild Guild} exists in.<br>
     * If the {@link net.dv8tion.jda.Region Region} is not recognized, returns {@link net.dv8tion.jda.Region#UNKNOWN UNKNOWN}.
     *
     * @return
     *      The the Region location that the Guild is hosted in. Can return Region.UNKNOWN.
     */
    Region getRegion();

    /**
     * The {@link net.dv8tion.jda.entities.User Users} that are part of this {@link net.dv8tion.jda.entities.Guild Guild}.
     *
     * @return
     *      An Immutable List of {@link net.dv8tion.jda.entities.User Users}.
     */
    List<User> getUsers();

    /**
     * The {@link net.dv8tion.jda.entities.TextChannel TextChannels} available on the {@link net.dv8tion.jda.entities.Guild Guild}.
     *
     * @return
     *      An Immutable List of {@link net.dv8tion.jda.entities.TextChannel TextChannels}.
     */
    List<TextChannel> getTextChannels();

    /**
     * The {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannels} available on the {@link net.dv8tion.jda.entities.Guild Guild}.
     *
     * @return
     *      An Immutable List of {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannels}.
     */
    List<VoiceChannel> getVoiceChannels();

    /**
     * The {@link net.dv8tion.jda.entities.Role Roles} of this {@link net.dv8tion.jda.entities.Guild Guild}
     *
     * @return
     *      An Immutable List of {@link net.dv8tion.jda.entities.Role Roles}.
     */
    List<Role> getRoles();

    /**
     * Provides all of the {@link net.dv8tion.jda.entities.Role Roles} that the provided {@link net.dv8tion.jda.entities.User User}
     *  has been assigned.
     * @param user
     *          The {@link net.dv8tion.jda.entities.User User} that we wish to get the {@link net.dv8tion.jda.entities.Role Roles} related to.
     * @return
     *      An Immutable List of {@link net.dv8tion.jda.entities.Role Roles}.
     */
    List<Role> getRolesForUser(User user);

    /**
     * The @everyone {@link net.dv8tion.jda.entities.Role Role} of this {@link net.dv8tion.jda.entities.Guild Guild}
     *
     * @return The @everyone {@link net.dv8tion.jda.entities.Role Role}
     */
    Role getPublicRole();

    /**
     * Leave the guild.
     * <b>This will delete the guild if the current account owns it!</b>
     */
    void leave();

    /**
     * Returns the {@link net.dv8tion.jda.JDA JDA} instance of this Guild
     * @return
     *      the corresponding JDA instance
     */
    JDA getJDA();
}
