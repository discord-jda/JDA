/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

package net.dv8tion.jda.api.managers;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.Region;
import net.dv8tion.jda.api.entities.*;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Manager providing functionality to update one or more fields for a {@link net.dv8tion.jda.api.entities.GuildChannel GuildChannel}.
 *
 * <p><b>Example</b>
 * <pre>{@code
 * manager.setName("github-log")
 *        .setTopic("logs for github commits")
 *        .setNSFW(false)
 *        .queue();
 * manager.reset(ChannelManager.TOPIC | ChannelManager.NAME)
 *        .setName("nsfw-commits")
 *        .setTopic(null)
 *        .setNSFW(true)
 *        .queue();
 * }</pre>
 *
 * @see net.dv8tion.jda.api.entities.GuildChannel#getManager()
 */
public interface ChannelManager extends Manager<ChannelManager>
{
    /** Used to reset the name field */
    long NAME       = 0x1;
    /** Used to reset the parent field */
    long PARENT     = 0x2;
    /** Used to reset the topic field */
    long TOPIC      = 0x4;
    /** Used to reset the position field */
    long POSITION   = 0x8;
    /** Used to reset the nsfw field */
    long NSFW       = 0x10;
    /** Used to reset the userlimit field */
    long USERLIMIT  = 0x20;
    /** Used to reset the bitrate field */
    long BITRATE    = 0x40;
    /** Used to reset the permission field */
    long PERMISSION = 0x80;
    /** Used to reset the rate-limit per user field */
    long SLOWMODE   = 0x100;
    /** Used to reset the channel type field */
    long NEWS       = 0x200;
    /** Used to reset the region field */
    long REGION     = 0x400;

    /**
     * Resets the fields specified by the provided bit-flag pattern.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br>Example: {@code manager.reset(ChannelManager.NAME | ChannelManager.PARENT);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #NAME}</li>
     *     <li>{@link #PARENT}</li>
     *     <li>{@link #TOPIC}</li>
     *     <li>{@link #POSITION}</li>
     *     <li>{@link #NSFW}</li>
     *     <li>{@link #SLOWMODE}</li>
     *     <li>{@link #USERLIMIT}</li>
     *     <li>{@link #BITRATE}</li>
     *     <li>{@link #PERMISSION}</li>
     *     <li>{@link #NEWS}</li>
     *     <li>{@link #REGION}</li>
     * </ul>
     *
     * @param  fields
     *         Integer value containing the flags to reset.
     *
     * @return ChannelManager for chaining convenience
     */
    @Nonnull
    @Override
    ChannelManager reset(long fields);

    /**
     * Resets the fields specified by the provided bit-flag patterns.
     * <br>Example: {@code manager.reset(ChannelManager.NAME, ChannelManager.PARENT);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #NAME}</li>
     *     <li>{@link #PARENT}</li>
     *     <li>{@link #TOPIC}</li>
     *     <li>{@link #POSITION}</li>
     *     <li>{@link #NSFW}</li>
     *     <li>{@link #USERLIMIT}</li>
     *     <li>{@link #BITRATE}</li>
     *     <li>{@link #PERMISSION}</li>
     *     <li>{@link #NEWS}</li>
     *     <li>{@link #REGION}</li>
     * </ul>
     *
     * @param  fields
     *         Integer values containing the flags to reset.
     *
     * @return ChannelManager for chaining convenience
     */
    @Nonnull
    @Override
    ChannelManager reset(long... fields);

    /**
     * The {@link net.dv8tion.jda.api.entities.GuildChannel GuildChannel} that will
     * be modified by this Manager instance
     *
     * @return The {@link net.dv8tion.jda.api.entities.GuildChannel GuildChannel}
     */
    @Nonnull
    GuildChannel getChannel();

    /**
     * The {@link net.dv8tion.jda.api.entities.ChannelType ChannelType}
     *
     * @return The ChannelType
     */
    @Nonnull
    default ChannelType getType()
    {
        return getChannel().getType();
    }

    /**
     * The {@link net.dv8tion.jda.api.entities.Guild Guild} this Manager's
     * {@link net.dv8tion.jda.api.entities.GuildChannel GuildChannel} is in.
     * <br>This is logically the same as calling {@code getChannel().getGuild()}
     *
     * @return The parent {@link net.dv8tion.jda.api.entities.Guild Guild}
     */
    @Nonnull
    default Guild getGuild()
    {
        return getChannel().getGuild();
    }

    /**
     * Clears the overrides added via {@link #putPermissionOverride(IPermissionHolder, Collection, Collection)}.
     *
     * @return ChannelManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    ChannelManager clearOverridesAdded();

    /**
     * Clears the overrides removed via {@link #removePermissionOverride(IPermissionHolder)}.
     *
     * @return ChannelManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    ChannelManager clearOverridesRemoved();

    /**
     * Adds an override for the specified {@link net.dv8tion.jda.api.entities.IPermissionHolder IPermissionHolder}
     * with the provided raw bitmasks as allowed and denied permissions. If the permission holder already
     * had an override on this channel it will be replaced instead.
     *
     * @param  permHolder
     *         The permission holder
     * @param  allow
     *         The bitmask to grant
     * @param  deny
     *         The bitmask to deny
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided permission holder is {@code null}
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.api.Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS}
     *         in this channel, or tries to set permissions it does not have without having {@link Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS} explicitly for this channel through an override.
     *
     * @return ChannelManager for chaining convenience
     *
     * @see    #putPermissionOverride(IPermissionHolder, Collection, Collection)
     * @see    net.dv8tion.jda.api.Permission#getRaw(Permission...) Permission.getRaw(Permission...)
     */
    @Nonnull
    @CheckReturnValue
    ChannelManager putPermissionOverride(@Nonnull IPermissionHolder permHolder, long allow, long deny);

    /**
     * Adds an override for the specified {@link net.dv8tion.jda.api.entities.IPermissionHolder IPermissionHolder}
     * with the provided permission sets as allowed and denied permissions. If the permission holder already
     * had an override on this channel it will be replaced instead.
     * <br>Example: {@code putPermissionOverride(guild.getSelfMember(), EnumSet.of(Permission.MESSAGE_WRITE, Permission.MESSAGE_READ), null)}
     *
     * @param  permHolder
     *         The permission holder
     * @param  allow
     *         The permissions to grant, or null
     * @param  deny
     *         The permissions to deny, or null
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided permission holder is {@code null}
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.api.Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS}
     *         in this channel, or tries to set permissions it does not have without having {@link Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS} explicitly for this channel through an override.
     *
     * @return ChannelManager for chaining convenience
     *
     * @see    #putPermissionOverride(IPermissionHolder, long, long)
     * @see    java.util.EnumSet EnumSet
     */
    @Nonnull
    @CheckReturnValue
    default ChannelManager putPermissionOverride(@Nonnull IPermissionHolder permHolder, @Nullable Collection<Permission> allow, @Nullable Collection<Permission> deny)
    {
        long allowRaw = allow == null ? 0 : Permission.getRaw(allow);
        long denyRaw  = deny  == null ? 0 : Permission.getRaw(deny);
        return putPermissionOverride(permHolder, allowRaw, denyRaw);
    }

    /**
     * Adds an override for the specified role with the provided raw bitmasks as allowed and denied permissions.
     * If the role already had an override on this channel it will be replaced instead.
     *
     * @param  roleId
     *         The ID of the role to set permissions for
     * @param  allow
     *         The bitmask to grant
     * @param  deny
     *         The bitmask to deny
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.api.Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS}
     *         in this channel, or tries to set permissions it does not have without having {@link Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS} explicitly for this channel through an override.
     *
     * @return ChannelManager for chaining convenience
     *
     * @see    #putRolePermissionOverride(long, Collection, Collection)
     * @see    net.dv8tion.jda.api.Permission#getRaw(Permission...) Permission.getRaw(Permission...)
     */
    @Nonnull
    @CheckReturnValue
    ChannelManager putRolePermissionOverride(long roleId, long allow, long deny);

    /**
     * Adds an override for the specified role with the provided permission sets as allowed and denied permissions.
     * If the role already had an override on this channel it will be replaced instead.
     *
     * @param  roleId
     *         The ID of the role to set permissions for
     * @param  allow
     *         The permissions to grant, or null
     * @param  deny
     *         The permissions to deny, or null
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.api.Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS}
     *         in this channel, or tries to set permissions it does not have without having {@link Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS} explicitly for this channel through an override.
     *
     * @return ChannelManager for chaining convenience
     *
     * @see    #putRolePermissionOverride(long, long, long)
     * @see    java.util.EnumSet EnumSet
     */
    @Nonnull
    @CheckReturnValue
    default ChannelManager putRolePermissionOverride(long roleId, @Nullable Collection<Permission> allow, @Nullable Collection<Permission> deny)
    {
        long allowRaw = allow == null ? 0 : Permission.getRaw(allow);
        long denyRaw  = deny  == null ? 0 : Permission.getRaw(deny);
        return putRolePermissionOverride(roleId, allowRaw, denyRaw);
    }

    /**
     * Adds an override for the specified member with the provided raw bitmasks as allowed and denied permissions.
     * If the member already had an override on this channel it will be replaced instead.
     *
     * @param  memberId
     *         The ID of the member to set permissions for
     * @param  allow
     *         The bitmask to grant
     * @param  deny
     *         The bitmask to deny
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.api.Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS}
     *         in this channel, or tries to set permissions it does not have without having {@link Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS} explicitly for this channel through an override.
     *
     * @return ChannelManager for chaining convenience
     *
     * @see    #putMemberPermissionOverride(long, Collection, Collection)
     * @see    net.dv8tion.jda.api.Permission#getRaw(Permission...) Permission.getRaw(Permission...)
     */
    @Nonnull
    @CheckReturnValue
    ChannelManager putMemberPermissionOverride(long memberId, long allow, long deny);

    /**
     * Adds an override for the specified member with the provided permission sets as allowed and denied permissions.
     * If the member already had an override on this channel it will be replaced instead.
     *
     * @param  memberId
     *         The ID of the member to set permissions for
     * @param  allow
     *         The permissions to grant, or null
     * @param  deny
     *         The permissions to deny, or null
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.api.Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS}
     *         in this channel, or tries to set permissions it does not have without having {@link Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS} explicitly for this channel through an override.
     *
     * @return ChannelManager for chaining convenience
     *
     * @see    #putMemberPermissionOverride(long, long, long)
     * @see    java.util.EnumSet EnumSet
     */
    @Nonnull
    @CheckReturnValue
    default ChannelManager putMemberPermissionOverride(long memberId, @Nullable Collection<Permission> allow, @Nullable Collection<Permission> deny)
    {
        long allowRaw = allow == null ? 0 : Permission.getRaw(allow);
        long denyRaw  = deny  == null ? 0 : Permission.getRaw(deny);
        return putMemberPermissionOverride(memberId, allowRaw, denyRaw);
    }

    /**
     * Removes the {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverride} for the specified
     * {@link net.dv8tion.jda.api.entities.IPermissionHolder IPermissionHolder}. If no override existed for this member
     * or role, this does nothing.
     *
     * @param  permHolder
     *         The permission holder
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided permission holder is {@code null}
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.api.Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS}
     *         in this channel
     *
     * @return ChannelManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    ChannelManager removePermissionOverride(@Nonnull IPermissionHolder permHolder);

    /**
     * Removes the {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverride} for the specified
     * member or role ID. If no override existed for this member or role, this does nothing.
     *
     * @param  id
     *         The ID of the permission holder
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.api.Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS}
     *         in this channel
     *
     * @return ChannelManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    ChannelManager removePermissionOverride(long id);

    /**
     * Syncs all {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverrides} of this GuildChannel with
     * its parent ({@link net.dv8tion.jda.api.entities.Category Category}).
     *
     * <p>After this operation, all {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverrides}
     * will be exactly the same as the ones from the parent.
     * <br><b>That means that all current PermissionOverrides are lost!</b>
     *
     * <p>This behaves as if calling {@link #sync(net.dv8tion.jda.api.entities.GuildChannel)} with this GuildChannel's {@link net.dv8tion.jda.api.entities.GuildChannel#getParent() Parent}.
     *
     * @throws  java.lang.IllegalStateException
     *          If this GuildChannel has no parent
     * @throws  net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *          If the currently logged in account does not have {@link net.dv8tion.jda.api.Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS}
     *          in this channel or {@link IPermissionHolder#canSync(GuildChannel, GuildChannel)} is false for the self member.
     *
     * @return  ChannelManager for chaining convenience
     *
     * @see     <a href="https://discord.com/developers/docs/topics/permissions#permission-syncing" target="_blank">Discord Documentation - Permission Syncing</a>
     */
    @Nonnull
    @CheckReturnValue
    default ChannelManager sync()
    {
        if (getChannel().getParent() == null)
            throw new IllegalStateException("sync() requires a parent category");
        return sync(getChannel().getParent());
    }

    /**
     * Syncs all {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverrides} of this GuildChannel with
     * the given ({@link net.dv8tion.jda.api.entities.GuildChannel GuildChannel}).
     *
     * <p>After this operation, all {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverrides}
     * will be exactly the same as the ones from the syncSource.
     * <br><b>That means that all current PermissionOverrides are lost!</b>
     *
     * <p>This will only work for Channels of the same {@link net.dv8tion.jda.api.entities.Guild Guild}!.
     *
     * @param   syncSource
     *          The GuildChannel from where all PermissionOverrides should be copied from
     *
     * @throws  java.lang.IllegalArgumentException
     *          If the given snySource is {@code null}, this GuildChannel or from a different Guild.
     * @throws  net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *          If the currently logged in account does not have {@link net.dv8tion.jda.api.Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS}
     *          in this channel or {@link IPermissionHolder#canSync(GuildChannel, GuildChannel)} is false for the self member.
     *
     * @return  ChannelManager for chaining convenience
     *
     * @see     <a href="https://discord.com/developers/docs/topics/permissions#permission-syncing" target="_blank">Discord Documentation - Permission Syncing</a>
     */
    @Nonnull
    @CheckReturnValue
    ChannelManager sync(@Nonnull GuildChannel syncSource);

    /**
     * Sets the <b><u>name</u></b> of the selected {@link net.dv8tion.jda.api.entities.GuildChannel GuildChannel}.
     *
     * <p>A channel name <b>must not</b> be {@code null} nor empty or more than 100 characters long!
     * <br>TextChannel names may only be populated with alphanumeric (with underscore and dash).
     *
     * <p><b>Example</b>: {@code mod-only} or {@code generic_name}
     * <br>Characters will automatically be lowercased by Discord for text channels!
     *
     * @param  name
     *         The new name for the selected {@link net.dv8tion.jda.api.entities.GuildChannel GuildChannel}
     *
     * @throws IllegalArgumentException
     *         If the provided name is {@code null} or not between 1-100 characters long
     *
     * @return ChannelManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    ChannelManager setName(@Nonnull String name);

    /**
     * Sets the <b><u>{@link net.dv8tion.jda.api.entities.Category Parent Category}</u></b>
     * of the selected {@link net.dv8tion.jda.api.entities.GuildChannel GuildChannel}.
     *
     *
     * @param  category
     *         The new parent for the selected {@link net.dv8tion.jda.api.entities.GuildChannel GuildChannel}
     *
     * @throws IllegalStateException
     *         If the target is a category itself
     * @throws IllegalArgumentException
     *         If the provided category is not from the same Guild
     *
     * @return ChannelManager for chaining convenience
     *
     * @since  3.4.0
     */
    @Nonnull
    @CheckReturnValue
    ChannelManager setParent(@Nullable Category category);

    /**
     * Sets the <b><u>position</u></b> of the selected {@link net.dv8tion.jda.api.entities.GuildChannel GuildChannel}.
     *
     * <p><b>To modify multiple channels you should use
     * <code>Guild.{@link net.dv8tion.jda.api.entities.Guild#modifyTextChannelPositions() modifyTextChannelPositions()}</code>
     * instead! This is not the same as looping through channels and using this to update positions!</b>
     *
     * @param  position
     *         The new position for the selected {@link net.dv8tion.jda.api.entities.GuildChannel GuildChannel}
     *
     * @return ChannelManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    ChannelManager setPosition(int position);

    /**
     * Sets the <b><u>topic</u></b> of the selected
     * {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} or {@link StageChannel StageChannel}.
     *
     * <p>A channel topic <b>must not</b> be more than {@code 1024} characters long!
     * <br><b>This is only available to {@link net.dv8tion.jda.api.entities.TextChannel TextChannels}</b>
     *
     * @param  topic
     *         The new topic for the selected channel,
     *         {@code null} or empty String to reset
     *
     * @throws UnsupportedOperationException
     *         If the selected {@link net.dv8tion.jda.api.entities.GuildChannel GuildChannel}'s type is not {@link net.dv8tion.jda.api.entities.ChannelType#TEXT TEXT}
     * @throws IllegalArgumentException
     *         If the provided topic is greater than {@code 1024} in length
     *
     * @return ChannelManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    ChannelManager setTopic(@Nullable String topic);

    /**
     * Sets the <b><u>nsfw flag</u></b> of the selected {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}.
     *
     * @param  nsfw
     *         The new nsfw flag for the selected {@link net.dv8tion.jda.api.entities.TextChannel TextChannel},
     *
     * @throws IllegalStateException
     *         If the selected {@link net.dv8tion.jda.api.entities.GuildChannel GuildChannel}'s type is not {@link net.dv8tion.jda.api.entities.ChannelType#TEXT TEXT}
     *
     * @return ChannelManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    ChannelManager setNSFW(boolean nsfw);

    /**
     * Sets the <b><u>slowmode</u></b> of the selected {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}.
     * <br>Provide {@code 0} to reset the slowmode of the {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}
     *
     * <p>A channel slowmode <b>must not</b> be negative nor greater than {@link net.dv8tion.jda.api.entities.TextChannel#MAX_SLOWMODE TextChannel.MAX_SLOWMODE}!
     * <br><b>This is only available to {@link net.dv8tion.jda.api.entities.TextChannel TextChannels}</b>
     *
     * <p>Note: Bots are unaffected by this.
     * <br>Having {@link net.dv8tion.jda.api.Permission#MESSAGE_MANAGE MESSAGE_MANAGE} or
     * {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} permission also
     * grants immunity to slowmode.
     *
     * @param  slowmode
     *         The new slowmode for the selected {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}
     *
     * @throws IllegalStateException
     *         If the selected {@link net.dv8tion.jda.api.entities.GuildChannel GuildChannel}'s type is not {@link net.dv8tion.jda.api.entities.ChannelType#TEXT TEXT}
     * @throws IllegalArgumentException
     *         If the provided slowmode is negative or greater than {@link net.dv8tion.jda.api.entities.TextChannel#MAX_SLOWMODE TextChannel.MAX_SLOWMODE}
     *
     * @return ChannelManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    ChannelManager setSlowmode(int slowmode);

    /**
     * Sets the <b><u>user-limit</u></b> of the selected {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannel}.
     * <br>Provide {@code 0} to reset the user-limit of the {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannel}
     *
     * <p>A channel user-limit <b>must not</b> be negative nor greater than {@code 99}!
     * <br><b>This is only available to {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannels}</b>
     *
     * @param  userLimit
     *         The new user-limit for the selected {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannel}
     *
     * @throws IllegalStateException
     *         If the selected {@link net.dv8tion.jda.api.entities.GuildChannel GuildChannel}'s type is not {@link net.dv8tion.jda.api.entities.ChannelType#VOICE VOICE}
     * @throws IllegalArgumentException
     *         If the provided user-limit is negative or greater than {@code 99}
     *
     * @return ChannelManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    ChannelManager setUserLimit(int userLimit);

    /**
     * Sets the <b><u>bitrate</u></b> of the selected {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannel}.
     * <br>The default value is {@code 64000}
     *
     * <p>A channel bitrate <b>must not</b> be less than {@code 8000} nor greater than {@link Guild#getMaxBitrate()}!
     * <br><b>This is only available to {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannels}</b>
     *
     * @param  bitrate
     *         The new bitrate for the selected {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannel}
     *
     * @throws IllegalStateException
     *         If the selected {@link net.dv8tion.jda.api.entities.GuildChannel GuildChannel}'s type is not {@link net.dv8tion.jda.api.entities.ChannelType#VOICE VOICE}
     * @throws IllegalArgumentException
     *         If the provided bitrate is less than 8000 or greater than {@link net.dv8tion.jda.api.entities.Guild#getMaxBitrate()}.
     *
     * @return ChannelManager for chaining convenience
     *
     * @see    net.dv8tion.jda.api.entities.Guild#getFeatures()
     */
    @Nonnull
    @CheckReturnValue
    ChannelManager setBitrate(int bitrate);

    /**
     * Sets the {@link net.dv8tion.jda.api.Region Region} of the selected {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannel}.
     * <br>The default value is {@link Region#AUTOMATIC}
     *
     * Possible values are:
     * <ul>
     *     <li>{@link Region#AUTOMATIC}</li>
     *     <li>{@link Region#US_WEST}</li>
     *     <li>{@link Region#US_EAST}</li>
     *     <li>{@link Region#US_CENTRAL}</li>
     *     <li>{@link Region#US_SOUTH}</li>
     *     <li>{@link Region#SINGAPORE}</li>
     *     <li>{@link Region#SOUTH_AFRICA}</li>
     *     <li>{@link Region#SYDNEY}</li>
     *     <li>{@link Region#EUROPE}</li>
     *     <li>{@link Region#INDIA}</li>
     *     <li>{@link Region#SOUTH_KOREA}</li>
     *     <li>{@link Region#BRAZIL}</li>
     *     <li>{@link Region#JAPAN}</li>
     *     <li>{@link Region#RUSSIA}</li>
     * </ul>
     *
     * <br><b>This is only available to {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannels}!</b>
     *
     * @param region
     *        The new {@link net.dv8tion.jda.api.Region Region}
     * @throws IllegalStateException
     *         If the selected {@link net.dv8tion.jda.api.entities.GuildChannel GuildChannel}'s type is not {@link net.dv8tion.jda.api.entities.ChannelType#VOICE VOICE}
     * @throws IllegalArgumentException
     *         If the provided Region is not in the list of usable values
     * @return ChannelManager for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    ChannelManager setRegion(Region region);

    /**
     * Sets the <b><u>news flag</u></b> of the selected {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}.
     * Announcement-/News-Channels can be used to crosspost messages to other guilds.
     *
     * @param  news
     *         The new news flag for the selected {@link net.dv8tion.jda.api.entities.TextChannel TextChannel},
     *
     * @throws IllegalStateException
     *         If the selected {@link net.dv8tion.jda.api.entities.GuildChannel GuildChannel}'s type is not {@link net.dv8tion.jda.api.entities.ChannelType#TEXT TEXT}
     * @throws IllegalStateException
     *         If {@code news} is {@code true} and the guild doesn't have the NEWS feature
     *
     * @return ChannelManager for chaining convenience
     *
     * @see    net.dv8tion.jda.api.entities.Guild#getFeatures()
     * @see    net.dv8tion.jda.api.entities.TextChannel#isNews()
     *
     * @since  4.2.1
     */
    @Nonnull
    @CheckReturnValue
    ChannelManager setNews(boolean news);
}
