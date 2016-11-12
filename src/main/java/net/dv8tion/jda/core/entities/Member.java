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
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.Permission;

import java.awt.Color;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Represents a Guild-specific User.
 * <p>
 * Contains all guild-specific information about a User. (Roles, Nickname, VoiceStatus etc.)
 */
public interface Member extends IMentionable
{
    /**
     * The user wrapped by this Entity.
     *
     * @return
     *      {@link net.dv8tion.jda.core.entities.User User}
     */
    User getUser();

    /**
     * The Guild in which this Member is represented.
     *
     * @return
     *      {@link net.dv8tion.jda.core.entities.Guild Guild}
     */
    Guild getGuild();

    /**
     * The JDA instance.
     *
     * @return
     *      The current JDA instance.
     */
    JDA getJDA();

    /**
     * The {@link java.time.OffsetDateTime Time} this Member joined the Guild.
     *
     * @return
     *      The Join Date.
     */
    OffsetDateTime getJoinDate();

    /**
     * The {@link net.dv8tion.jda.core.entities.GuildVoiceState VoiceState} of this Member.<p>
     * This can be used to get the Member's VoiceChannel.
     *
     * @return
     *      {@link net.dv8tion.jda.core.entities.GuildVoiceState VoiceState}
     */
    GuildVoiceState getVoiceState();

    /**
     * The game that the user is currently playing.
     * If the user is not currently playing a game, this will return null.
     *
     * @return
     *      Possibly-null {@link net.dv8tion.jda.core.entities.Game Game} containing the game that the {@link net.dv8tion.jda.core.entities.User User} is currently playing.
     */
    Game getGame();

    /**
     * Returns the {@link net.dv8tion.jda.core.OnlineStatus OnlineStatus} of the User.<br>
     * If the {@link net.dv8tion.jda.core.OnlineStatus OnlineStatus} is unrecognized, will return {@link net.dv8tion.jda.core.OnlineStatus#UNKNOWN UNKNOWN}.
     *
     * @return
     *      The current {@link net.dv8tion.jda.core.OnlineStatus OnlineStatus} of the {@link net.dv8tion.jda.core.entities.User User}.
     */
    OnlineStatus getOnlineStatus();

    /**
     * Returns the current nickname of this Member for the parent Guild.
     *
     * @return
     *      The nickname or null, if no nickname is set.
     */
    String getNickname();

    /**
     * Retrieves the Name displayed in the official Discord Client.
     *
     * @return
     *      The Nickname of this Member or the Username if no Nickname is present.
     */
    String getEffectiveName();

    /**
     * The roles applied to this Member.
     *
     * @return
     *      An immutable List of {@link net.dv8tion.jda.core.entities.Role Roles} for this Member.
     */
    List<Role> getRoles();

    /**
     * The {@link java.awt.Color Color} of this Member's name in a Guild.
     * <p>
     * This is determined by the color of the highest role assigned to them that does not have the default color.<br>
     * If all roles have default color, this returns null.
     *
     * @return
     *      The display Color for this Member.
     */
    Color getColor();

    /**
     * The Guild-Wide Permissions this Member holds.
     *
     * @return
     *      An immutable List of Permissions granted to this Member.
     */
    List<Permission> getPermissions();

    /**
     * The Permissions this Member holds in the specified {@link net.dv8tion.jda.core.entities.Channel Channel}.<br>
     * Permissions returned by this may be different from {@link #getPermissions()} due to the Channel's {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverrides }.
     *
     * @return
     *      An immutable List of Permissions granted to this Member.
     */
    List<Permission> getPermissions(Channel channel);

    /**
     * Checks whether or not this Member has the given {@link net.dv8tion.jda.core.Permission Permissions} in the Guild.
     *
     * @param permissions
     *          Permissions to check for.
     * @return
     *      True - if all of the specified Permissions are granted to this Member.
     */
    boolean hasPermission(Permission... permissions);

    /**
     * Checks whether or not this Member has the {@link net.dv8tion.jda.core.Permission Permissions} in the provided
     * Collection&lt;Permission&gt; in the Guild.
     *
     * @param permissions
     *          Permissions to check for.
     * @return
     *      True - if all of the specified Permissions are granted to this Member.
     */
    boolean hasPermission(Collection<Permission> permissions);

    /**
     * Checks whether or not this Member has the given {@link net.dv8tion.jda.core.Permission Permissions} in the specified Channel.
     *
     * @param channel
     *          The {@link net.dv8tion.jda.core.entities.Channel Channel} in which to check.
     * @param permissions
     *          Permissions to check for.
     * @return
     *      True - if all of the specified Permissions are granted to this Member in the provided Channel.
     */
    boolean hasPermission(Channel channel, Permission... permissions);

    /**
     * Checks whether or not this Member has the {@link net.dv8tion.jda.core.Permission Permissions} in the provided
     * Collection&lt;Permission&gt; in the specified Channel.
     *
     * @param channel
     *          The {@link net.dv8tion.jda.core.entities.Channel Channel} in which to check.
     * @param permissions
     *          Permissions to check for.
     * @return
     *      True - if all of the specified Permissions are granted to this Member in the provided Channel.
     */
    boolean hasPermission(Channel channel, Collection<Permission> permissions);

    boolean canInteract(Member member);

    boolean canInteract(Role role);

    boolean canInteract(Emote emote);
}
