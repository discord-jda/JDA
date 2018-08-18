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
import net.dv8tion.jda.core.managers.ChannelManager;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.core.requests.restaction.ChannelAction;
import net.dv8tion.jda.core.requests.restaction.InviteAction;
import net.dv8tion.jda.core.requests.restaction.PermissionOverrideAction;

import javax.annotation.CheckReturnValue;
import java.util.List;

/**
 * Represents a {@link net.dv8tion.jda.core.entities.Guild Guild} channel.
 */
public interface Channel extends ISnowflake
{
    /**
     * The {@link net.dv8tion.jda.core.entities.ChannelType ChannelType} for this Channel
     *
     * @return The channel type
     */
    ChannelType getType();

    /**
     * The human readable name of the  Channel.
     * <br>If no name has been set, this returns null.
     *
     * @return The name of this Channel
     */
    String getName();

    /**
     * Returns the {@link net.dv8tion.jda.core.entities.Guild Guild} that this Channel is part of.
     *
     * @return Never-null {@link net.dv8tion.jda.core.entities.Guild Guild} that this Channel is part of.
     */
    Guild getGuild();

    /**
     * Parent {@link net.dv8tion.jda.core.entities.Category Category} of this
     * Channel. Channels need not have a parent Category.
     * <br>Note that an {@link net.dv8tion.jda.core.entities.Category Category} will
     * always return {@code null} for this method as nested categories are not supported.
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.Category Category} for this Channel
     */
    Category getParent();

    /**
     * A List of all {@link net.dv8tion.jda.core.entities.Member Members} that are in this Channel
     * For {@link net.dv8tion.jda.core.entities.TextChannel TextChannels},
     * this returns all Members with the {@link net.dv8tion.jda.core.Permission#MESSAGE_READ} Permission.
     * In {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannels},
     * this returns all Members that joined that VoiceChannel.
     *
     * @return A List of {@link net.dv8tion.jda.core.entities.Member Members} that are in this Channel.
     */
    List<Member> getMembers();

    /**
     * The position this Channel is displayed at.
     * <br>Higher values mean they are displayed lower in the Client. Position 0 is the top most Channel
     * Channels of a {@link net.dv8tion.jda.core.entities.Guild Guild} do not have to have continuous positions
     *
     * @return Zero-based int of position of the Channel.
     */
    int getPosition();

    /**
     * The actual position of the {@link net.dv8tion.jda.core.entities.Channel Channel} as stored and given by Discord.
     * Role positions are actually based on a pairing of the creation time (as stored in the snowflake id)
     * and the position. If 2 or more roles share the same position then they are sorted based on their creation date.
     * The more recent a role was created, the lower it is in the hierarchy. This is handled by {@link #getPosition()}
     * and it is most likely the method you want. If, for some reason, you want the actual position of the
     * Role then this method will give you that value.
     *
     * @return The true, Discord stored, position of the {@link net.dv8tion.jda.core.entities.Channel Channel}.
     */
    int getPositionRaw();

    /**
     * Returns the {@link net.dv8tion.jda.core.JDA JDA} instance of this Channel
     *
     * @return the corresponding JDA instance
     */
    JDA getJDA();

    /**
     * The {@link PermissionOverride} relating to the specified {@link net.dv8tion.jda.core.entities.Member Member}.
     * If there is no {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride} for this {@link net.dv8tion.jda.core.entities.Channel Channel}
     * relating to the provided {@link net.dv8tion.jda.core.entities.Member Member}, then this returns {@code null}.
     *
     * @param  member
     *         The {@link net.dv8tion.jda.core.entities.Member Member} whose
     *         {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride} is requested.
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}
     *         relating to the provided {@link net.dv8tion.jda.core.entities.Member Member}.
     */
    PermissionOverride getPermissionOverride(Member member);

    /**
     * The {@link PermissionOverride} relating to the specified {@link net.dv8tion.jda.core.entities.Role Role}.
     * If there is no {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride} for this {@link net.dv8tion.jda.core.entities.Channel Channel}
     * relating to the provided {@link net.dv8tion.jda.core.entities.Role Role}, then this returns {@code null}.
     *
     * @param  role
     *         The {@link net.dv8tion.jda.core.entities.User Role} whose {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride} is requested.
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}
     *         relating to the provided {@link net.dv8tion.jda.core.entities.Role Role}.
     */
    PermissionOverride getPermissionOverride(Role role);

    /**
     * Gets all of the {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverrides} that are part
     * of this {@link net.dv8tion.jda.core.entities.Channel Channel}.
     * <br>This combines {@link net.dv8tion.jda.core.entities.Member Member} and {@link net.dv8tion.jda.core.entities.Role Role} overrides.
     * If you would like only {@link net.dv8tion.jda.core.entities.Member Member} overrides or only {@link net.dv8tion.jda.core.entities.Role Role}
     * overrides, use {@link #getMemberPermissionOverrides()} or {@link #getRolePermissionOverrides()} respectively.
     *
     * @return Possibly-empty list of all {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverrides}
     *         for this {@link net.dv8tion.jda.core.entities.Channel Channel}.
     */
    List<PermissionOverride> getPermissionOverrides();

    /**
     * Gets all of the {@link net.dv8tion.jda.core.entities.Member Member} {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverrides}
     * that are part of this {@link net.dv8tion.jda.core.entities.Channel Channel}.
     *
     * @return Possibly-empty list of all {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverrides}
     *         for {@link net.dv8tion.jda.core.entities.Member Member}
     *         for this {@link net.dv8tion.jda.core.entities.Channel Channel}.
     */
    List<PermissionOverride> getMemberPermissionOverrides();

    /**
     * Gets all of the {@link net.dv8tion.jda.core.entities.Role Role} {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverrides}
     * that are part of this {@link net.dv8tion.jda.core.entities.Channel Channel}.
     *
     * @return Possibly-empty list of all {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverrides}
     *         for {@link net.dv8tion.jda.core.entities.Role Roles}
     *         for this {@link net.dv8tion.jda.core.entities.Channel Channel}.
     */
    List<PermissionOverride> getRolePermissionOverrides();

    /**
     * Creates a copy of the specified {@link net.dv8tion.jda.core.entities.Channel Channel}
     * in the specified {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * <br>If the provided target guild is not the same Guild this channel is in then
     * the parent category and permissions will not be copied due to technical difficulty and ambiguity.
     *
     * <p>This copies the following elements:
     * <ol>
     *     <li>Name</li>
     *     <li>Parent Category (if present)</li>
     *     <li>Voice Elements (Bitrate, Userlimit)</li>
     *     <li>Text Elements (Topic, NSFW)</li>
     *     <li>All permission overrides for Members/Roles</li>
     * </ol>
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The channel could not be created due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  guild
     *         The {@link net.dv8tion.jda.core.entities.Guild Guild} to create the channel in
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided guild is {@code null}
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If the currently logged in account does not have the {@link net.dv8tion.jda.core.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} Permission
     *
     * @return A specific {@link net.dv8tion.jda.core.requests.restaction.ChannelAction ChannelAction}
     *         <br>This action allows to set fields for the new Channel before creating it!
     */
    @CheckReturnValue
    ChannelAction createCopy(Guild guild);

    /**
     * Creates a copy of the specified {@link net.dv8tion.jda.core.entities.Channel Channel}.
     *
     * <p>This copies the following elements:
     * <ol>
     *     <li>Name</li>
     *     <li>Parent Category (if present)</li>
     *     <li>Voice Elements (Bitrate, Userlimit)</li>
     *     <li>Text Elements (Topic, NSFW)</li>
     *     <li>All permission overrides for Members/Roles</li>
     * </ol>
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The channel could not be created due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If the currently logged in account does not have the {@link net.dv8tion.jda.core.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} Permission
     *
     * @return A specific {@link net.dv8tion.jda.core.requests.restaction.ChannelAction ChannelAction}
     *         <br>This action allows to set fields for the new Channel before creating it!
     */
    @CheckReturnValue
    default ChannelAction createCopy()
    {
        return createCopy(getGuild());
    }

    /**
     * Returns the {@link net.dv8tion.jda.core.managers.ChannelManager ChannelManager} for this Channel.
     * <br>In the ChannelManager, you can modify the name, topic and position of this Channel.
     * You modify multiple fields in one request by chaining setters before calling {@link net.dv8tion.jda.core.requests.RestAction#queue() RestAction.queue()}.
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.core.Permission#MANAGE_CHANNEL Permission.MANAGE_CHANNEL}
     *
     * @return The ChannelManager of this Channel
     */
    ChannelManager getManager();

    /**
     * Deletes this Channel.
     *
     * <p>Possible ErrorResponses include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>If this channel was already deleted</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The send request was attempted after the account lost
     *         {@link net.dv8tion.jda.core.Permission#MANAGE_CHANNEL Permission.MANAGE_CHANNEL} in the channel.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>If we were removed from the Guild</li>
     * </ul>
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         if the currently logged in account doesn't have {@link net.dv8tion.jda.core.Permission#MANAGE_CHANNEL MANAGE_CHANNEL}
     *         for the channel.
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @CheckReturnValue
    AuditableRestAction<Void> delete();

    /**
     * Creates a {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}
     * for the specified {@link net.dv8tion.jda.core.entities.Member Member} in this Channel.
     * You can use {@link #putPermissionOverride(Member)} to replace existing overrides.
     *
     * <p>Possible ErrorResponses include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>If this channel was already deleted</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>If we were removed from the Guild</li>
     * </ul>
     *
     * @param  member
     *         The Member to create an override for
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         if we don't have the permission to {@link net.dv8tion.jda.core.Permission#MANAGE_PERMISSIONS MANAGE_PERMISSIONS}
     * @throws IllegalArgumentException
     *         if the specified Member is null or the Member is not from {@link #getGuild()}
     * @throws java.lang.IllegalStateException
     *         If the specified Member already has a PermissionOverride. Use {@link #getPermissionOverride(Member)} to retrieve it.
     *         You can use {@link #putPermissionOverride(Member)} to replace existing overrides.
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.PermissionOverrideAction PermissionOverrideAction}
     *         Provides the newly created PermissionOverride for the specified Role
     *
     * @see    #createPermissionOverride(Role)
     * @see    #putPermissionOverride(Member)
     */
    @CheckReturnValue
    PermissionOverrideAction createPermissionOverride(Member member);

    /**
     * Creates a {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}
     * for the specified {@link net.dv8tion.jda.core.entities.Role Role} in this Channel.
     * You can use {@link #putPermissionOverride(Role)} to replace existing overrides.
     *
     * <p>Possible ErrorResponses include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>If this channel was already deleted</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>If we were removed from the Guild</li>
     * </ul>
     *
     * @param  role
     *         The Role to create an override for
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         if we don't have the permission to {@link net.dv8tion.jda.core.Permission#MANAGE_PERMISSIONS MANAGE_PERMISSIONS}
     * @throws IllegalArgumentException
     *         if the specified Role is null or the Role is not from {@link #getGuild()}
     * @throws java.lang.IllegalStateException
     *         If the specified Role already has a PermissionOverride. Use {@link #getPermissionOverride(Role)} to retrieve it.
     *         You can use {@link #putPermissionOverride(Role)} to replace existing overrides.
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.PermissionOverrideAction PermissionOverrideAction}
     *         Provides the newly created PermissionOverride for the specified Role
     *
     * @see    #createPermissionOverride(Member)
     * @see    #putPermissionOverride(Role)
     */
    @CheckReturnValue
    PermissionOverrideAction createPermissionOverride(Role role);

    /**
     * Creates a {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}
     * for the specified {@link net.dv8tion.jda.core.entities.Member Member} in this Channel.
     * <br>If the member already has an existing override it will be replaced.
     *
     * @param  member
     *         The Member to create the override for
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         if we don't have the permission to {@link net.dv8tion.jda.core.Permission#MANAGE_PERMISSIONS MANAGE_PERMISSIONS}
     * @throws java.lang.IllegalArgumentException
     *         If the provided member is null or from a different guild
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.PermissionOverrideAction PermissionOverrideAction}
     *         Provides the newly created PermissionOverride for the specified Member
     *
     * @see    #putPermissionOverride(Role)
     */
    @CheckReturnValue
    PermissionOverrideAction putPermissionOverride(Member member);

    /**
     * Creates a {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}
     * for the specified {@link net.dv8tion.jda.core.entities.Role Role} in this Channel.
     * <br>If the role already has an existing override it will be replaced.
     *
     * @param  role
     *         The Role to create the override for
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         if we don't have the permission to {@link net.dv8tion.jda.core.Permission#MANAGE_PERMISSIONS MANAGE_PERMISSIONS}
     * @throws java.lang.IllegalArgumentException
     *         If the provided role is null or from a different guild
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.PermissionOverrideAction PermissionOverrideAction}
     *         Provides the newly created PermissionOverride for the specified Role
     *
     * @see    #putPermissionOverride(Member)
     */
    @CheckReturnValue
    PermissionOverrideAction putPermissionOverride(Role role);

    /**
     * Creates a new {@link net.dv8tion.jda.core.requests.restaction.InviteAction InviteAction} which can be used to create a
     * new {@link net.dv8tion.jda.core.entities.Invite Invite}. 
     * <br>Requires {@link net.dv8tion.jda.core.Permission#CREATE_INSTANT_INVITE CREATE_INSTANT_INVITE} in this channel.
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the account does not have {@link net.dv8tion.jda.core.Permission#CREATE_INSTANT_INVITE CREATE_INSTANT_INVITE} in this channel
     * @throws java.lang.IllegalArgumentException
     *         If this is an instance of a {@link net.dv8tion.jda.core.entities.Category Category}
     *
     * @return A new {@link net.dv8tion.jda.core.requests.restaction.InviteAction InviteAction}
     * 
     * @see    net.dv8tion.jda.core.requests.restaction.InviteAction
     */
    @CheckReturnValue
    InviteAction createInvite();

    /**
     * Returns all invites for this channel.
     * <br>Requires {@link net.dv8tion.jda.core.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} in this channel.
     * Will throw a {@link net.dv8tion.jda.core.exceptions.InsufficientPermissionException InsufficientPermissionException} otherwise.
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         if the account does not have {@link net.dv8tion.jda.core.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} in this channel
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: List{@literal <}{@link net.dv8tion.jda.core.entities.Invite Invite}{@literal >}
     *         <br>The list of expanded Invite objects
     *
     * @see    net.dv8tion.jda.core.entities.Guild#getInvites()
     */
    @CheckReturnValue
    RestAction<List<Invite>> getInvites();
}
