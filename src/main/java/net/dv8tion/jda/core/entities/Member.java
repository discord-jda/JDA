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

package net.dv8tion.jda.core.entities;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.Permission;

import javax.annotation.Nullable;
import java.awt.Color;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Represents a Guild-specific User.
 *
 * <p>Contains all guild-specific information about a User. (Roles, Nickname, VoiceStatus etc.)
 *
 * @since 3.0
 */
public interface Member extends IMentionable, IPermissionHolder
{
    /**
     * The user wrapped by this Entity.
     *
     * @return {@link net.dv8tion.jda.core.entities.User User}
     */
    User getUser();

    /**
     * The Guild in which this Member is represented.
     *
     * @return {@link net.dv8tion.jda.core.entities.Guild Guild}
     */
    Guild getGuild();

    /**
     * The JDA instance.
     *
     * @return The current JDA instance.
     */
    JDA getJDA();

    /**
     * The {@link java.time.OffsetDateTime Time} this Member joined the Guild.
     *
     * @return The Join Date.
     */
    OffsetDateTime getJoinDate();

    /**
     * The {@link net.dv8tion.jda.core.entities.GuildVoiceState VoiceState} of this Member.
     *
     * <p>This can be used to get the Member's VoiceChannel using {@link GuildVoiceState#getChannel()}.
     *
     * @return {@link net.dv8tion.jda.core.entities.GuildVoiceState VoiceState}
     */
    GuildVoiceState getVoiceState();

    /**
     * The game that the user is currently playing.
     * <br>If the user is not currently playing a game, this will return null.
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.Game Game} containing the game
     *         that the {@link net.dv8tion.jda.core.entities.User User} is currently playing.
     */
    Game getGame();

    /**
     * Returns the {@link net.dv8tion.jda.core.OnlineStatus OnlineStatus} of the User.
     * <br>If the {@link net.dv8tion.jda.core.OnlineStatus OnlineStatus} is unrecognized, will return {@link net.dv8tion.jda.core.OnlineStatus#UNKNOWN UNKNOWN}.
     *
     * @return The current {@link net.dv8tion.jda.core.OnlineStatus OnlineStatus} of the {@link net.dv8tion.jda.core.entities.User User}.
     */
    OnlineStatus getOnlineStatus();

    /**
     * Returns the current nickname of this Member for the parent Guild.
     *
     * <p>This can be changed using
     * {@link net.dv8tion.jda.core.managers.GuildController#setNickname(Member, String) GuildController.setNickname(Member, String)}.
     *
     * @return The nickname or null, if no nickname is set.
     */
    String getNickname();

    /**
     * Retrieves the Name displayed in the official Discord Client.
     *
     * @return The Nickname of this Member or the Username if no Nickname is present.
     */
    String getEffectiveName();

    /**
     * The roles applied to this Member.
     * <br>The roles are ordered based on their position.
     *
     * <p>A Member's roles can be changed using the <b>addRolesToMember</b>, <b>removeRolesFromMember</b>, and <b>modifyMemberRoles</b>
     * methods in {@link net.dv8tion.jda.core.managers.GuildController GuildController}.
     *
     * <p><b>The Public Role ({@code @everyone}) is not included in the returned immutable list of roles
     * <br>It is implicit that every member holds the Public Role in a Guild thus it is not listed here!</b>
     *
     * @return An immutable List of {@link net.dv8tion.jda.core.entities.Role Roles} for this Member.
     */
    List<Role> getRoles();

    /**
     * The {@link java.awt.Color Color} of this Member's name in a Guild.
     *
     * <p>This is determined by the color of the highest role assigned to them that does not have the default color.
     * <br>If all roles have default color, this returns null.
     *
     * @return The display Color for this Member.
     *
     * @see    #getColorRaw()
     */
    Color getColor();

    /**
     * The raw RGB value for the color of this member.
     * <br>Defaulting to {@link net.dv8tion.jda.core.entities.Role#DEFAULT_COLOR_RAW Role.DEFAULT_COLOR_RAW}
     * if this member uses the default color (special property, it changes depending on theme used in the client)
     *
     * @return The raw RGB value or the role default
     */
    int getColorRaw();

    /**
     * The Permissions this Member holds in the specified {@link net.dv8tion.jda.core.entities.Channel Channel}.
     * <br>Permissions returned by this may be different from {@link #getPermissions()}
     * due to the Channel's {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverrides}.
     *
     * @param  channel
     *         The {@link net.dv8tion.jda.core.entities.Channel Channel} of which to get Permissions for
     *
     * @return An immutable List of Permissions granted to this Member.
     */
    List<Permission> getPermissions(Channel channel);

    /**
     * Whether this Member can interact with the provided Member
     * (kick/ban/etc.)
     *
     * @param  member
     *         The target Member to check
     *
     * @throws NullPointerException
     *         if the specified Member is null
     * @throws IllegalArgumentException
     *         if the specified Member is not from the same guild
     *
     * @return True, if this Member is able to interact with the specified Member
     *
     * @see    net.dv8tion.jda.core.utils.PermissionUtil#canInteract(Member, Member)
     */
    boolean canInteract(Member member);

    /**
     * Whether this Member can interact with the provided {@link net.dv8tion.jda.core.entities.Role Role}
     * (kick/ban/move/modify/delete/etc.)
     *
     * @param  role
     *         The target Role to check
     *
     * @throws NullPointerException
     *         if the specified Role is null
     * @throws IllegalArgumentException
     *         if the specified Role is not from the same guild
     *
     * @return True, if this member is able to interact with the specified Role
     *
     * @see    net.dv8tion.jda.core.utils.PermissionUtil#canInteract(Member, Role)
     */
    boolean canInteract(Role role);

    /**
     * Whether this Member can interact with the provided {@link net.dv8tion.jda.core.entities.Emote Emote}
     * (use in a message)
     *
     * @param  emote
     *         The target Emote to check
     *
     * @throws NullPointerException
     *         if the specified Emote is null
     * @throws IllegalArgumentException
     *         if the specified Emote is not from the same guild
     *
     * @return True, if this Member is able to interact with the specified Emote
     *
     * @see    net.dv8tion.jda.core.utils.PermissionUtil#canInteract(Member, Emote)
     */
    boolean canInteract(Emote emote);

    /**
     * Checks whether this member is the owner of its related {@link net.dv8tion.jda.core.entities.Guild Guild}.
     *
     * @return True, if this member is the owner of the attached Guild.
     */
    boolean isOwner();

    /**
     * The default {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} for a {@link net.dv8tion.jda.core.entities.Member Member}.
     * <br>This is the channel that the Discord client will default to opening when a Guild is opened for the first time
     * after joining the guild.
     * <br>The default channel is the channel with the highest position in which the member has
     * {@link net.dv8tion.jda.core.Permission#MESSAGE_READ Permission.MESSAGE_READ} permissions. If this requirement doesn't apply for
     * any channel in the guild, this method returns {@code null}.
     *
     * @return The {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} representing the default channel for this member
     *         or null if no such channel exists.
     */
    @Nullable
    TextChannel getDefaultChannel();
}
