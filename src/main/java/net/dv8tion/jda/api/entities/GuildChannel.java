/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda.api.entities;

import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.ChannelManager;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.dv8tion.jda.api.requests.restaction.InviteAction;
import net.dv8tion.jda.api.requests.restaction.PermissionOverrideAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.entities.GuildImpl;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Represents a {@link net.dv8tion.jda.api.entities.Guild Guild} channel.
 *
 * @see Guild#getGuildChannelById(long)
 * @see Guild#getGuildChannelById(ChannelType, long)
 *
 * @see JDA#getGuildChannelById(long)
 * @see JDA#getGuildChannelById(ChannelType, long)
 */
public interface GuildChannel extends ISnowflake, Comparable<GuildChannel>
{
    /**
     * The {@link net.dv8tion.jda.api.entities.ChannelType ChannelType} for this GuildChannel
     *
     * @return The channel type
     */
    @Nonnull
    ChannelType getType();

    /**
     * The human readable name of the  GuildChannel.
     * <br>If no name has been set, this returns null.
     *
     * @return The name of this GuildChannel
     */
    @Nonnull
    String getName();

    /**
     * Returns the {@link net.dv8tion.jda.api.entities.Guild Guild} that this GuildChannel is part of.
     *
     * @return Never-null {@link net.dv8tion.jda.api.entities.Guild Guild} that this GuildChannel is part of.
     */
    @Nonnull
    Guild getGuild();

    /**
     * Parent {@link net.dv8tion.jda.api.entities.Category Category} of this
     * GuildChannel. Channels need not have a parent Category.
     * <br>Note that an {@link net.dv8tion.jda.api.entities.Category Category} will
     * always return {@code null} for this method as nested categories are not supported.
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.Category Category} for this GuildChannel
     */
    @Nullable
    Category getParent();

    /**
     * A List of all {@link net.dv8tion.jda.api.entities.Member Members} that are in this GuildChannel
     * <br>For {@link net.dv8tion.jda.api.entities.TextChannel TextChannels},
     * this returns all Members with the {@link net.dv8tion.jda.api.Permission#MESSAGE_READ} Permission.
     * <br>For {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannels},
     * this returns all Members that joined that VoiceChannel.
     * <br>For {@link net.dv8tion.jda.api.entities.Category Categories},
     * this returns all Members who are in its child channels.
     *
     * @return An immutable List of {@link net.dv8tion.jda.api.entities.Member Members} that are in this GuildChannel.
     */
    @Nonnull
    List<Member> getMembers();

    /**
     * The position this GuildChannel is displayed at.
     * <br>Higher values mean they are displayed lower in the Client. Position 0 is the top most GuildChannel
     * Channels of a {@link net.dv8tion.jda.api.entities.Guild Guild} do not have to have continuous positions
     *
     * @return Zero-based int of position of the GuildChannel.
     */
    int getPosition();

    /**
     * The actual position of the {@link net.dv8tion.jda.api.entities.GuildChannel GuildChannel} as stored and given by Discord.
     * Channel positions are actually based on a pairing of the creation time (as stored in the snowflake id)
     * and the position. If 2 or more channels share the same position then they are sorted based on their creation date.
     * The more recent a channel was created, the lower it is in the hierarchy. This is handled by {@link #getPosition()}
     * and it is most likely the method you want. If, for some reason, you want the actual position of the
     * channel then this method will give you that value.
     *
     * @return The true, Discord stored, position of the {@link net.dv8tion.jda.api.entities.GuildChannel GuildChannel}.
     */
    int getPositionRaw();

    /**
     * Returns the {@link net.dv8tion.jda.api.JDA JDA} instance of this GuildChannel
     *
     * @return the corresponding JDA instance
     */
    @Nonnull
    JDA getJDA();

    /**
     * The {@link PermissionOverride} relating to the specified {@link net.dv8tion.jda.api.entities.Member Member} or {@link net.dv8tion.jda.api.entities.Role Role}.
     * If there is no {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverride} for this {@link GuildChannel GuildChannel}
     * relating to the provided Member or Role, then this returns {@code null}.
     *
     * @param  permissionHolder
     *         The {@link net.dv8tion.jda.api.entities.Member Member} or {@link net.dv8tion.jda.api.entities.Role Role} whose
     *         {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverride} is requested.
     *
     * @throws IllegalArgumentException
     *         If the provided permission holder is null, or from a different guild
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverride}
     *         relating to the provided Member or Role.
     */
    @Nullable
    PermissionOverride getPermissionOverride(@Nonnull IPermissionHolder permissionHolder);

    /**
     * Gets all of the {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverrides} that are part
     * of this {@link GuildChannel GuildChannel}.
     * <br>This combines {@link net.dv8tion.jda.api.entities.Member Member} and {@link net.dv8tion.jda.api.entities.Role Role} overrides.
     * If you would like only {@link net.dv8tion.jda.api.entities.Member Member} overrides or only {@link net.dv8tion.jda.api.entities.Role Role}
     * overrides, use {@link #getMemberPermissionOverrides()} or {@link #getRolePermissionOverrides()} respectively.
     *
     * @return Possibly-empty immutable list of all {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverrides}
     *         for this {@link GuildChannel GuildChannel}.
     */
    @Nonnull
    List<PermissionOverride> getPermissionOverrides();

    /**
     * Gets all of the {@link net.dv8tion.jda.api.entities.Member Member} {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverrides}
     * that are part of this {@link GuildChannel GuildChannel}.
     *
     * @return Possibly-empty immutable list of all {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverrides}
     *         for {@link net.dv8tion.jda.api.entities.Member Member}
     *         for this {@link GuildChannel GuildChannel}.
     */
    @Nonnull
    List<PermissionOverride> getMemberPermissionOverrides();

    /**
     * Gets all of the {@link net.dv8tion.jda.api.entities.Role Role} {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverrides}
     * that are part of this {@link GuildChannel GuildChannel}.
     *
     * @return Possibly-empty immutable list of all {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverrides}
     *         for {@link net.dv8tion.jda.api.entities.Role Roles}
     *         for this {@link GuildChannel GuildChannel}.
     */
    @Nonnull
    List<PermissionOverride> getRolePermissionOverrides();

    /**
     * Creates a copy of the specified {@link GuildChannel GuildChannel}
     * in the specified {@link net.dv8tion.jda.api.entities.Guild Guild}.
     * <br>If the provided target guild is not the same Guild this channel is in then
     * the parent category and permissions will not be copied due to technical difficulty and ambiguity.
     *
     * <p>This copies the following elements:
     * <ol>
     *     <li>Name</li>
     *     <li>Parent Category (if present)</li>
     *     <li>Voice Elements (Bitrate, Userlimit)</li>
     *     <li>Text Elements (Topic, NSFW, Slowmode)</li>
     *     <li>All permission overrides for Members/Roles</li>
     * </ol>
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The channel could not be created due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL VIEW_CHANNEL} permission was removed</li>
     * </ul>
     *
     * @param  guild
     *         The {@link net.dv8tion.jda.api.entities.Guild Guild} to create the channel in
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided guild is {@code null}
     * @throws net.dv8tion.jda.api.exceptions.PermissionException
     *         If the currently logged in account does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} Permission
     *
     * @return A specific {@link ChannelAction ChannelAction}
     *         <br>This action allows to set fields for the new GuildChannel before creating it!
     */
    @Nonnull
    @CheckReturnValue
    ChannelAction<? extends GuildChannel> createCopy(@Nonnull Guild guild);

    /**
     * Creates a copy of the specified {@link GuildChannel GuildChannel}.
     *
     * <p>This copies the following elements:
     * <ol>
     *     <li>Name</li>
     *     <li>Parent Category (if present)</li>
     *     <li>Voice Elements (Bitrate, Userlimit)</li>
     *     <li>Text Elements (Topic, NSFW, Slowmode)</li>
     *     <li>All permission overrides for Members/Roles</li>
     * </ol>
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The channel could not be created due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL VIEW_CHANNEL} permission was removed</li>
     * </ul>
     *
     * @throws net.dv8tion.jda.api.exceptions.PermissionException
     *         If the currently logged in account does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} Permission
     *
     * @return A specific {@link ChannelAction ChannelAction}
     *         <br>This action allows to set fields for the new GuildChannel before creating it!
     */
    @Nonnull
    @CheckReturnValue
    default ChannelAction<? extends GuildChannel> createCopy()
    {
        return createCopy(getGuild());
    }

    /**
     * Returns the {@link ChannelManager ChannelManager} for this GuildChannel.
     * <br>In the ChannelManager, you can modify the name, topic and position of this GuildChannel.
     * You modify multiple fields in one request by chaining setters before calling {@link net.dv8tion.jda.api.requests.RestAction#queue() RestAction.queue()}.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL Permission.MANAGE_CHANNEL}
     *
     * @return The ChannelManager of this GuildChannel
     */
    @Nonnull
    ChannelManager getManager();

    /**
     * Deletes this GuildChannel.
     *
     * <p>Possible ErrorResponses include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>If this channel was already deleted</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The send request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL Permission.MANAGE_CHANNEL} in the channel.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>If we were removed from the Guild</li>
     * </ul>
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         if the currently logged in account doesn't have {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL}
     *         for the channel.
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @CheckReturnValue
    AuditableRestAction<Void> delete();

    /**
     * Creates a {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverride}
     * for the specified {@link net.dv8tion.jda.api.entities.Member Member} or {@link net.dv8tion.jda.api.entities.Role Role} in this GuildChannel.
     * You can use {@link #putPermissionOverride(IPermissionHolder)} to replace existing overrides.
     *
     * <p>Possible ErrorResponses include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>If this channel was already deleted</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>If we were removed from the Guild</li>
     * </ul>
     *
     * @param  permissionHolder
     *         The Member or Role to create an override for
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         if we don't have the permission to {@link net.dv8tion.jda.api.Permission#MANAGE_PERMISSIONS MANAGE_PERMISSIONS}
     * @throws IllegalArgumentException
     *         if the specified permission holder is null or is not from {@link #getGuild()}
     * @throws java.lang.IllegalStateException
     *         If the specified permission holder already has a PermissionOverride. Use {@link #getPermissionOverride(IPermissionHolder)} to retrieve it.
     *         You can use {@link #putPermissionOverride(IPermissionHolder)} to replace existing overrides.
     *
     * @return {@link PermissionOverrideAction PermissionOverrideAction}
     *         Provides the newly created PermissionOverride for the specified permission holder
     */
    @Nonnull
    @CheckReturnValue
    PermissionOverrideAction createPermissionOverride(@Nonnull IPermissionHolder permissionHolder);

    /**
     * Creates a {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverride}
     * for the specified {@link net.dv8tion.jda.api.entities.Member Member} or {@link net.dv8tion.jda.api.entities.Role Role} in this GuildChannel.
     * <br>If the permission holder already has an existing override it will be replaced.
     *
     * @param  permissionHolder
     *         The Member or Role to create the override for
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         if we don't have the permission to {@link net.dv8tion.jda.api.Permission#MANAGE_PERMISSIONS MANAGE_PERMISSIONS}
     * @throws java.lang.IllegalArgumentException
     *         If the provided permission holder is null or from a different guild
     *
     * @return {@link PermissionOverrideAction PermissionOverrideAction}
     *         Provides the newly created PermissionOverride for the specified permission holder
     */
    @Nonnull
    @CheckReturnValue
    PermissionOverrideAction putPermissionOverride(@Nonnull IPermissionHolder permissionHolder);

    /**
     * Creates a new override or updates an existing one.
     * <br>This is similar to calling {@link PermissionOverride#getManager()} if an override exists.
     *
     * @param  permissionHolder
     *         The Member/Role for the override
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If we don't have the permission to {@link net.dv8tion.jda.api.Permission#MANAGE_PERMISSIONS MANAGE_PERMISSIONS}
     * @throws java.lang.IllegalArgumentException
     *         If the provided permission holder is null or not from this guild
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.PermissionOverrideAction}
     *         <br>With the current settings of an existing override or a fresh override with no permissions set
     *
     * @since  4.0.0
     */
    @Nonnull
    @CheckReturnValue
    default PermissionOverrideAction upsertPermissionOverride(@Nonnull IPermissionHolder permissionHolder)
    {
        if (!getGuild().getSelfMember().hasPermission(this, Permission.MANAGE_PERMISSIONS))
            throw new InsufficientPermissionException(this, Permission.MANAGE_PERMISSIONS);
        PermissionOverride override = getPermissionOverride(permissionHolder);
        if (override != null)
            return override.getManager();
        PermissionOverrideAction action = putPermissionOverride(permissionHolder);
        // Check if we have some information cached already
        TLongObjectMap<DataObject> cache = ((GuildImpl) getGuild()).getOverrideMap(permissionHolder.getIdLong());
        DataObject json = cache == null ? null : cache.get(getIdLong());
        if (json != null)
            action = action.setPermissions(json.getLong("allow"), json.getLong("deny"));
        return action;
    }

    /**
     * Creates a new {@link InviteAction InviteAction} which can be used to create a
     * new {@link net.dv8tion.jda.api.entities.Invite Invite}.
     * <br>Requires {@link net.dv8tion.jda.api.Permission#CREATE_INSTANT_INVITE CREATE_INSTANT_INVITE} in this channel.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the account does not have {@link net.dv8tion.jda.api.Permission#CREATE_INSTANT_INVITE CREATE_INSTANT_INVITE} in this channel
     * @throws java.lang.IllegalArgumentException
     *         If this is an instance of a {@link net.dv8tion.jda.api.entities.Category Category}
     *
     * @return A new {@link InviteAction InviteAction}
     * 
     * @see    InviteAction
     */
    @Nonnull
    @CheckReturnValue
    InviteAction createInvite();

    /**
     * Returns all invites for this channel.
     * <br>Requires {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} in this channel.
     * Will throw a {@link net.dv8tion.jda.api.exceptions.InsufficientPermissionException InsufficientPermissionException} otherwise.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         if the account does not have {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} in this channel
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: List{@literal <}{@link net.dv8tion.jda.api.entities.Invite Invite}{@literal >}
     *         <br>The list of expanded Invite objects
     *
     * @see    net.dv8tion.jda.api.entities.Guild#retrieveInvites()
     */
    @Nonnull
    @CheckReturnValue
    RestAction<List<Invite>> retrieveInvites();
}
