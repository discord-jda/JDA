/**
 *    Copyright 2015-2016 Austin Keener & Michael Ritter
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
import net.dv8tion.jda.managers.ChannelManager;
import net.dv8tion.jda.managers.GuildManager;
import net.dv8tion.jda.managers.RoleManager;

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
     * Creates a new {@link net.dv8tion.jda.entities.TextChannel TextChannel} in this Guild.
     * For this to be successful, the logged in account has to have the {@link net.dv8tion.jda.Permission#MANAGE_CHANNEL MANAGE_CHANNEL Permission}
     *
     * @param name
     *      the name of the TextChannel to create
     * @return
     *      the ChannelManager for the created TextChannel
     */
    ChannelManager createTextChannel(String name);

    /**
     * The {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannels} available on the {@link net.dv8tion.jda.entities.Guild Guild}.
     *
     * @return
     *      An Immutable List of {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannels}.
     */
    List<VoiceChannel> getVoiceChannels();

    /**
     * Creates a new {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannel} in this Guild.
     * For this to be successful, the logged in account has to have the {@link net.dv8tion.jda.Permission#MANAGE_CHANNEL MANAGE_CHANNEL Permission}
     *
     * @param name
     *      the name of the VoiceChannel to create
     * @return
     *      the ChannelManager for the created VoiceChannel
     */
    ChannelManager createVoiceChannel(String name);

    /**
     * The {@link net.dv8tion.jda.entities.Role Roles} of this {@link net.dv8tion.jda.entities.Guild Guild}
     *
     * @return
     *      An Immutable List of {@link net.dv8tion.jda.entities.Role Roles}.
     */
    List<Role> getRoles();

    /**
     * Creates a new {@link net.dv8tion.jda.entities.Role Role} in this Guild.
     * For this to be successful, the logged in account has to have the {@link net.dv8tion.jda.Permission#MANAGE_ROLES MANAGE_ROLES Permission}
     *
     * @return
     *  the RoleManager for the created Role
     */
    RoleManager createRole();

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
     * Returns the {@link net.dv8tion.jda.managers.GuildManager GuildManager} for this Guild.
     * In the GuildManager, you can modify most of its properties, and leave or delete it.
     *
     * @return
     *      The GuildManager of this Guild
     */
    GuildManager getManager();

    /**
     * Returns the {@link net.dv8tion.jda.JDA JDA} instance of this Guild
     * @return
     *      the corresponding JDA instance
     */
    JDA getJDA();

    /**
     * Returns the current {@link net.dv8tion.jda.entities.VoiceStatus VoiceStatus} of the provide {@link net.dv8tion.jda.entities.User User}
     * on this {@link net.dv8tion.jda.entities.Guild Guild}. Every {@link net.dv8tion.jda.entities.User User} in this guild
     * will have a matching {@link net.dv8tion.jda.entities.VoiceStatus VoiceStatus}.<br>
     * If a {@link net.dv8tion.jda.entities.User User}
     * that is not in this {@link net.dv8tion.jda.entities.Guild Guild} is provided, this will return <code>null</code>.
     *
     * @param user
     *          The {@link net.dv8tion.jda.entities.User User} whose {@link net.dv8tion.jda.entities.VoiceStatus VoiceStatus} is requested.
     * @return
     *      Possibly-null instance of the provided user's {@link net.dv8tion.jda.entities.VoiceStatus VoiceStatus}.
     */
    VoiceStatus getVoiceStatusOfUser(User user);

    /**
     * A list containing the {@link net.dv8tion.jda.entities.VoiceStatus VoiceStatus} of every {@link net.dv8tion.jda.entities.User User}
     * in this {@link net.dv8tion.jda.entities.Guild Guild}.<br>
     * This will never return an empty list because if it were empty, that would imply that there are no
     * {@link net.dv8tion.jda.entities.User Users} in this {@link net.dv8tion.jda.entities.Guild Guild}, which is
     * impossible.
     *
     * @return
     *      Never-empty list containing all the {@link net.dv8tion.jda.entities.VoiceStatus VoiceStatuses} on this {@link net.dv8tion.jda.entities.Guild Guild}.
     */
    List<VoiceStatus> getVoiceStatuses();
}
