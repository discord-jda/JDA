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
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package net.dv8tion.jda.core.entities;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.managers.ChannelManager;
import net.dv8tion.jda.core.managers.ChannelManagerUpdatable;
import net.dv8tion.jda.core.requests.RestAction;
//import net.dv8tion.jda.core.managers.ChannelManager;
//import net.dv8tion.jda.core.managers.PermissionOverrideManager;
//import net.dv8tion.jda.core.utils.InviteUtil;

import java.util.List;

/**
 * Represents a {@link net.dv8tion.jda.core.entities.Guild Guild} channel.
 */
public interface Channel extends ISnowflake
{

    /**
     * The human readable name of the  Channel.<br>
     * If no name has been set, this returns null.
     *
     * @return
     *      The name of this Channel
     */
    String getName();

    /**
     * Returns the {@link net.dv8tion.jda.core.entities.Guild Guild} that this Channel is part of.
     *
     * @return
     *      Never-null {@link net.dv8tion.jda.core.entities.Guild Guild} that this Channel is part of.
     */
    Guild getGuild();

    /**
     * A List of all {@link net.dv8tion.jda.core.entities.Member Members} that are in this Channel
     * For {@link net.dv8tion.jda.core.entities.TextChannel TextChannels}, this returns all Members with the {@link net.dv8tion.jda.core.Permission#MESSAGE_READ} Permission.
     * In {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannels}, this returns all Members that joined that VoiceChannel.
     *
     * @return
     *      A List of {@link net.dv8tion.jda.core.entities.Member Members} that are in this Channel.
     */
    List<Member> getMembers();

    /**
     * The position this Channel is displayed at.<br>
     * Higher values mean they are displayed lower in the Client. Position 0 is the top most Channel
     * Channels of a {@link net.dv8tion.jda.core.entities.Guild Guild} do not have to have continuous positions
     *
     * @return
     *      Zero-based int of position of the Channel.
     */
    int getPosition();

    /**
     * The actual position of the {@link net.dv8tion.jda.core.entities.Channel Channel} as stored and given by Discord.
     * Role positions are actually based on a pairing of the creation time (as stored in the snowflake id)
     * and the position. If 2 or more roles share the same position then they are sorted based on their creation date.
     * The more recent a role was created, the lower it is in the hierachy. This is handled by {@link #getPosition()}
     * and it is most likely the method you want. If, for some reason, you want the actual position of the
     * Role then this method will give you that value.
     *
     * @return
     *      The true, Discord stored, position of the {@link net.dv8tion.jda.core.entities.Channel Channel}.
     */
    int getPositionRaw();

//    /**
//     * Returns the {@link net.dv8tion.jda.managers.ChannelManager ChannelManager} for this Channel.
//     * In the ChannelManager, you can modify the name, topic and position of this Channel.
//     *
//     * @return
//     *      The ChannelManager of this Channel
//     */
//    ChannelManager getManager();

    /**
     * Returns the {@link net.dv8tion.jda.core.JDA JDA} instance of this Channel
     * @return
     *      the corresponding JDA instance
     */
    JDA getJDA();

    /**
     * The {@link PermissionOverride} relating to the specified {@link net.dv8tion.jda.core.entities.User User}.
     * If there is no {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride} for this {@link net.dv8tion.jda.core.entities.Channel Channel}
     * relating to the provided {@link net.dv8tion.jda.core.entities.User User}, then this returns <code>null</code>.
     *
     * @param member
     *          The {@link net.dv8tion.jda.core.entities.Member Member} whose {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride} is requested.
     * @return
     *      Possibly-null {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride} relating to the provided {@link net.dv8tion.jda.core.entities.User User}.
     */
    PermissionOverride getPermissionOverride(Member member);

    /**
     * The {@link PermissionOverride} relating to the specified {@link net.dv8tion.jda.core.entities.Role Role}.
     * If there is no {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride} for this {@link net.dv8tion.jda.core.entities.Channel Channel}
     * relating to the provided {@link net.dv8tion.jda.core.entities.Role Role}, then this returns <code>null</code>.
     *
     * @param role
     *          The {@link net.dv8tion.jda.core.entities.User Role} whose {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride} is requested.
     * @return
     *      Possibly-null {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride} relating to the provided {@link net.dv8tion.jda.core.entities.Role Role}.
     */
    PermissionOverride getPermissionOverride(Role role);

    /**
     * Gets all of the {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverrides} that are part
     * of this {@link net.dv8tion.jda.core.entities.Channel Channel}.<br>
     * This combines {@link net.dv8tion.jda.core.entities.User User} and {@link net.dv8tion.jda.core.entities.Role Role} overrides.
     * If you would like only {@link net.dv8tion.jda.core.entities.User} overrides or only {@link net.dv8tion.jda.core.entities.Role Role}
     * overrides, use {@link #getMemberPermissionOverrides()} ()} or {@link #getRolePermissionOverrides()} respectively.
     *
     * @return
     *      Possibly-empty list of all {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverrides} for this {@link net.dv8tion.jda.core.entities.Channel Channel}.
     */
    List<PermissionOverride> getPermissionOverrides();

    /**
     * Gets all of the {@link net.dv8tion.jda.core.entities.Member Member} {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverrides}
     * that are part of this {@link net.dv8tion.jda.core.entities.Channel Channel}.
     *
     * @return
     *      Possibly-empty list of all {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverrides} for {@link net.dv8tion.jda.core.entities.Member Member}
     *      for this {@link net.dv8tion.jda.core.entities.Channel Channel}.
     */
    List<PermissionOverride> getMemberPermissionOverrides();

    /**
     * Gets all of the {@link net.dv8tion.jda.core.entities.Role Role} {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverrides}
     * that are part of this {@link net.dv8tion.jda.core.entities.Channel Channel}.
     *
     * @return
     *      Possibly-empty list of all {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverrides} for {@link net.dv8tion.jda.core.entities.Role Roles}
     *      for this {@link net.dv8tion.jda.core.entities.Channel Channel}.
     */
    List<PermissionOverride> getRolePermissionOverrides();

    /**
     * Returns the {@link net.dv8tion.jda.core.managers.ChannelManager ChannelManager} for this Channel.
     * In the ChannelManager, you can modify the name, topic and position of this Channel.
     *
     * @return
     *      The ChannelManager of this Channel
     */
    ChannelManager getManager();

    ChannelManagerUpdatable getManagerUpdatable();

    RestAction<Void> delete();

    RestAction<PermissionOverride> createPermissionOverride(Member member);

    RestAction<PermissionOverride> createPermissionOverride(Role role);

//    /**
//     * Provides a list of all {@link net.dv8tion.jda.utils.InviteUtil.AdvancedInvite Invites} for this Channel.
//     *
//     * @return
//     *      An Immutable List of {@link net.dv8tion.jda.utils.InviteUtil.AdvancedInvite Invites} for this channel.
//     */
//    List<InviteUtil.AdvancedInvite> getInvites();
}
