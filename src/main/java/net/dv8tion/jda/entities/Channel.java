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
 */
package net.dv8tion.jda.entities;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.managers.ChannelManager;
import net.dv8tion.jda.managers.PermissionOverrideManager;
import net.dv8tion.jda.utils.InviteUtil;

import java.util.List;

/**
 * Represents a {@link net.dv8tion.jda.entities.Guild Guild} channel.
 */
public interface Channel
{
    /**
     * The Id of the Channel. This is typically 18 characters long.
     * @return
     *      The Id of this Channel
     */
    String getId();

    /**
     * The human readable name of the  Channel.<br>
     * If no name has been set, this returns null.
     *
     * @return
     *      The name of this Channel
     */
    String getName();

    /**
     * The topic set for this Channel.
     * If no topic has been set, this returns null.
     *
     * @return
     *      Possibly-null String containing the topic of this Channel.
     */
    String getTopic();

    /**
     * Returns the {@link net.dv8tion.jda.entities.Guild Guild} that this Channel is part of.
     *
     * @return
     *      Never-null {@link net.dv8tion.jda.entities.Guild Guild} that this Channel is part of.
     */
    Guild getGuild();

    /**
     * A List of all {@link net.dv8tion.jda.entities.User Users} that are in this Channel
     * For {@link net.dv8tion.jda.entities.TextChannel TextChannels}, this returns all Users with the {@link net.dv8tion.jda.Permission#MESSAGE_READ} Permission.
     * In {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannels}, this returns all Users that joined that VoiceChannel.
     *
     * @return
     *      A List of {@link net.dv8tion.jda.entities.User Users} that are in this Channel.
     */
    List<User> getUsers();

    /**
     * The position this Channel is displayed at.<br>
     * Higher values mean they are displayed lower in the Client. Position 0 is the top most Channel
     * Channels of a {@link net.dv8tion.jda.entities.Guild Guild} do not have to have continuous positions
     *
     * @return
     *      Zero-based int of position of the Channel.
     */
    int getPosition();

    /**
     * The actual position of the {@link net.dv8tion.jda.entities.Channel Channel} as stored and given by Discord.
     * Role positions are actually based on a pairing of the creation time (as stored in the snowflake id)
     * and the position. If 2 or more roles share the same position then they are sorted based on their creation date.
     * The more recent a role was created, the lower it is in the hierachy. This is handled by {@link #getPosition()}
     * and it is most likely the method you want. If, for some reason, you want the actual position of the
     * Role then this method will give you that value.
     *
     * @return
     *      The true, Discord stored, position of the {@link net.dv8tion.jda.entities.Channel Channel}.
     */
    int getPositionRaw();

    /**
     * Checks if the given {@link net.dv8tion.jda.entities.User User} has the given {@link net.dv8tion.jda.Permission Permission}
     * in this Channel
     *
     * @param user
     *          the User to check the Permission against
     * @param permission
     *          the Permission to check for
     * @return
     *      if the given User has the given Permission in this Channel
     */
    boolean checkPermission(User user, Permission permission);

    /**
     * Returns the {@link net.dv8tion.jda.managers.ChannelManager ChannelManager} for this Channel.
     * In the ChannelManager, you can modify the name, topic and position of this Channel.
     *
     * @return
     *      The ChannelManager of this Channel
     */
    ChannelManager getManager();

    /**
     * Returns the {@link net.dv8tion.jda.JDA JDA} instance of this Channel
     * @return
     *      the corresponding JDA instance
     */
    JDA getJDA();

    /**
     * The {@link PermissionOverride} relating to the specified {@link net.dv8tion.jda.entities.User User}.
     * If there is no {@link net.dv8tion.jda.entities.PermissionOverride PermissionOverride} for this {@link net.dv8tion.jda.entities.Channel Channel}
     * relating to the provided {@link net.dv8tion.jda.entities.User User}, then this returns <code>null</code>.
     *
     * @param user
     *          The {@link net.dv8tion.jda.entities.User User} whose {@link net.dv8tion.jda.entities.PermissionOverride PermissionOverride} is requested.
     * @return
     *      Possibly-null {@link net.dv8tion.jda.entities.PermissionOverride PermissionOverride} relating to the provided {@link net.dv8tion.jda.entities.User User}.
     */
    PermissionOverride getOverrideForUser(User user);

    /**
     * The {@link PermissionOverride} relating to the specified {@link net.dv8tion.jda.entities.Role Role}.
     * If there is no {@link net.dv8tion.jda.entities.PermissionOverride PermissionOverride} for this {@link net.dv8tion.jda.entities.Channel Channel}
     * relating to the provided {@link net.dv8tion.jda.entities.Role Role}, then this returns <code>null</code>.
     *
     * @param role
     *          The {@link net.dv8tion.jda.entities.User Role} whose {@link net.dv8tion.jda.entities.PermissionOverride PermissionOverride} is requested.
     * @return
     *      Possibly-null {@link net.dv8tion.jda.entities.PermissionOverride PermissionOverride} relating to the provided {@link net.dv8tion.jda.entities.Role Role}.
     */
    PermissionOverride getOverrideForRole(Role role);

    /**
     * Gets all of the {@link net.dv8tion.jda.entities.PermissionOverride PermissionOverrides} that are part
     * of this {@link net.dv8tion.jda.entities.Channel Channel}.<br>
     * This combines {@link net.dv8tion.jda.entities.User User} and {@link net.dv8tion.jda.entities.Role Role} overrides.
     * If you would like only {@link net.dv8tion.jda.entities.User} overrides or only {@link net.dv8tion.jda.entities.Role Role}
     * overrides, use {@link #getUserPermissionOverrides()} or {@link #getRolePermissionOverrides()} respectively.
     *
     * @return
     *      Possibly-empty list of all {@link net.dv8tion.jda.entities.PermissionOverride PermissionOverrides} for this {@link net.dv8tion.jda.entities.Channel Channel}.
     */
    List<PermissionOverride> getPermissionOverrides();

    /**
     * Gets all of the {@link net.dv8tion.jda.entities.User User} {@link net.dv8tion.jda.entities.PermissionOverride PermissionOverrides}
     * that are part of this {@link net.dv8tion.jda.entities.Channel Channel}.
     *
     * @return
     *      Possibly-empty list of all {@link net.dv8tion.jda.entities.PermissionOverride PermissionOverrides} for {@link net.dv8tion.jda.entities.User Users}
     *      for this {@link net.dv8tion.jda.entities.Channel Channel}.
     */
    List<PermissionOverride> getUserPermissionOverrides();

    /**
     * Gets all of the {@link net.dv8tion.jda.entities.Role Role} {@link net.dv8tion.jda.entities.PermissionOverride PermissionOverrides}
     * that are part of this {@link net.dv8tion.jda.entities.Channel Channel}.
     *
     * @return
     *      Possibly-empty list of all {@link net.dv8tion.jda.entities.PermissionOverride PermissionOverrides} for {@link net.dv8tion.jda.entities.Role Roles}
     *      for this {@link net.dv8tion.jda.entities.Channel Channel}.
     */
    List<PermissionOverride> getRolePermissionOverrides();

    /**
     * Creates a new {@link net.dv8tion.jda.entities.PermissionOverride PermissionOverride} for a given {@link net.dv8tion.jda.entities.User User}.
     * For this to be successful, the logged in account has to have the {@link net.dv8tion.jda.Permission#MANAGE_PERMISSIONS MANAGE_PERMISSIONS Permission}
     *
     * @param user
     *      the User to create an Override for
     * @return
     *      the PermissionOverrideManager for the created PermissionOverride
     */
    PermissionOverrideManager createPermissionOverride(User user);

    /**
     * Creates a new {@link net.dv8tion.jda.entities.PermissionOverride PermissionOverride} for a given {@link net.dv8tion.jda.entities.Role Role}.
     * For this to be successful, the logged in account has to have the {@link net.dv8tion.jda.Permission#MANAGE_PERMISSIONS MANAGE_PERMISSIONS Permission}
     *
     * @param role
     *      the Role to create an Override for
     * @return
     *      the PermissionOverrideManager for the created PermissionOverride
     */
    PermissionOverrideManager createPermissionOverride(Role role);

    /**
     * Provides a list of all {@link net.dv8tion.jda.utils.InviteUtil.AdvancedInvite Invites} for this Channel.
     *
     * @return
     *      An Immutable List of {@link net.dv8tion.jda.utils.InviteUtil.AdvancedInvite Invites} for this channel.
     */
    List<InviteUtil.AdvancedInvite> getInvites();
}
