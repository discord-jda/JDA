/*
 * Copyright 2015-2020 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

import net.dv8tion.jda.annotations.DeprecatedSince;
import net.dv8tion.jda.annotations.ReplaceWith;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild.VerificationLevel;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.internal.entities.InviteImpl;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

/**
 * Representation of a Discord Invite.
 * This class is immutable.
 *
 * @since  3.0
 * @author Aljoscha Grebe
 *
 * @see    #resolve(JDA, String)
 * @see    #resolve(JDA, String, boolean)
 *
 * @see    net.dv8tion.jda.api.entities.Guild#retrieveInvites() Guild.retrieveInvites()
 * @see    GuildChannel#retrieveInvites()
 */
public interface Invite
{
    /**
     * Retrieves a new {@link net.dv8tion.jda.api.entities.Invite Invite} instance for the given invite code.
     * <br><b>You cannot resolve invites if you were banned from the origin Guild!</b>
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INVITE Unknown Invite}
     *     <br>The Invite did not exist (possibly deleted) or the account is banned in the guild.</li>
     * </ul>
     *
     * @param  api
     *         The JDA instance
     * @param  code
     *         A valid invite code
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.api.entities.Invite Invite}
     *         <br>The Invite object
     */
    @Nonnull
    static RestAction<Invite> resolve(@Nonnull final JDA api, @Nonnull final String code)
    {
        return resolve(api, code, false);
    }
    
    /**
     * Retrieves a new {@link net.dv8tion.jda.api.entities.Invite Invite} instance for the given invite code.
     * <br><b>You cannot resolve invites if you were banned from the origin Guild!</b>
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INVITE Unknown Invite}
     *     <br>The Invite did not exist (possibly deleted) or the account is banned in the guild.</li>
     * </ul>
     *
     * @param  api
     *         The JDA instance
     * @param  code
     *         A valid invite code
     * @param  withCounts
     *         Whether or not to include online and member counts for guild invites or users for group invites
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.api.entities.Invite Invite}
     *         <br>The Invite object
     */
    @Nonnull
    static RestAction<Invite> resolve(@Nonnull final JDA api, @Nonnull final String code, final boolean withCounts)
    {
        return InviteImpl.resolve(api, code, withCounts);
    }

    /**
     * Deletes this invite.
     * <br>Requires {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} in the invite's channel.
     * Will throw a {@link net.dv8tion.jda.api.exceptions.InsufficientPermissionException InsufficientPermissionException} otherwise.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         if the account does not have {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER} in the invite's channel
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @CheckReturnValue
    AuditableRestAction<Void> delete();

    /**
     * Tries to retrieve a new expanded {@link net.dv8tion.jda.api.entities.Invite Invite} with more info.
     * <br>As bots can't be in groups this is only available for guild invites and will throw an {@link java.lang.IllegalStateException IllegalStateException}
     * for other types.
     * <br>Requires either {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER} in the invite's guild or
     * {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} in the invite's channel.
     * Will throw a {@link net.dv8tion.jda.api.exceptions.InsufficientPermissionException InsufficientPermissionException} otherwise.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         if the account neither has {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER} in the invite's guild nor
     *         {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} in the invite's channel
     * @throws java.lang.IllegalStateException
     *         If this is a group invite
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.api.entities.Invite Invite}
     *         <br>The expanded Invite object
     *
     * @see    #getType()
     * @see    #isExpanded()
     */
    @Nonnull
    @CheckReturnValue
    RestAction<Invite> expand();

    /**
     * The type of this invite.
     *
     * @return The invites's type
     */
    @Nonnull
    Invite.InviteType getType();

    /**
     * An {@link net.dv8tion.jda.api.entities.Invite.Channel Invite.Channel} object
     * containing information about this invite's origin channel.
     *
     * @return Information about this invite's origin channel or null in case of a group invite
     * 
     * @see    net.dv8tion.jda.api.entities.Invite.Channel
     */
    @Nullable
    Channel getChannel();

    /**
     * The invite code
     *
     * @return the invite code
     */
    @Nonnull
    String getCode();

    /**
     * An {@link net.dv8tion.jda.api.entities.Invite.Group Invite.Group} object
     * containing information about this invite's origin group.
     *
     * @return Information about this invite's origin group or null in case of a guild invite
     *
     * @see    net.dv8tion.jda.api.entities.Invite.Group
     */
    @Nullable
    Group getGroup();

    /**
     * The invite URL for this invite in the format of:
     * {@code "https://discord.gg/" + getCode()}
     *
     * @return Invite URL for this Invite
     */
    @Nonnull
    default String getUrl()
    {
        return "https://discord.gg/" + getCode();
    }

    /**
     * Returns creation date of this invite.
     *
     * <p>This works only for expanded invites and will throw a {@link IllegalStateException} otherwise!
     *
     * @throws IllegalStateException
     *         if this invite is not expanded
     *
     * @return The creation date of this invite
     *
     * @see    #expand()
     * @see    #isExpanded()
     *
     * @deprecated
     *         Use {@link #getTimeCreated()} instead
     */
    @Nonnull
    @Deprecated
    @DeprecatedSince("4.0.0")
    @ReplaceWith("getTimeCreated()")
    OffsetDateTime getCreationTime();

    /**
     * An {@link net.dv8tion.jda.api.entities.Invite.Guild Invite.Guild} object
     * containing information about this invite's origin guild.
     *
     * @return Information about this invite's origin guild or null in case of a group invite
     * 
     * @see    net.dv8tion.jda.api.entities.Invite.Guild
     */
    @Nullable
    Guild getGuild();

    /**
     * The user who created this invite. This may be a fake user. For not expanded invites this may be null.
     *
     * @return The user who created this invite
     */
    @Nullable
    User getInviter();

    /**
     * The {@link net.dv8tion.jda.api.JDA JDA} instance used to create this Invite
     *
     * @return the corresponding JDA instance
     */
    @Nonnull
    JDA getJDA();

    /**
     * The max age of this invite in seconds.
     *
     * <p>This works only for expanded invites and will throw a {@link IllegalStateException} otherwise!
     *
     * @throws IllegalStateException
     *         if this invite is not expanded
     *
     * @return The max age of this invite in seconds
     *
     * @see    #expand()
     * @see    #isExpanded()
     */
    int getMaxAge();

    /**
    * The max uses of this invite. If there is no limit thus will return {@code 0}.
    *
    * <p>This works only for expanded invites and will throw a {@link IllegalStateException} otherwise!
    *
    * @throws IllegalStateException
     *        if this invite is not expanded
    *
    * @return The max uses of this invite or {@code 0} if there is no limit
    *
    * @see    #expand()
    * @see    #isExpanded()
    */
    int getMaxUses();

    /**
     * Returns creation date of this invite.
     *
     * <p>This works only for expanded invites and will throw a {@link IllegalStateException} otherwise!
     *
     * @throws IllegalStateException
     *         if this invite is not expanded
     *
     * @return The creation date of this invite
     *
     * @see    #expand()
     * @see    #isExpanded()
     */
    @Nonnull
    OffsetDateTime getTimeCreated();

    /**
     * How often this invite has been used.
     *
     * <p>This works only for expanded invites and will throw a {@link IllegalStateException} otherwise!
     *
     * @throws IllegalStateException
     *         if this invite is not expanded
     *
     * @return The uses of this invite
     *
     * @see    #expand()
     * @see    #isExpanded()
     */
    int getUses();

    /**
     * Whether this Invite is expanded or not. Expanded invites contain more information, but they can only be
     * obtained by {@link net.dv8tion.jda.api.entities.Guild#retrieveInvites() Guild#retrieveInvites()} (requires
     * {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER Permission.MANAGE_SERVER}) or
     * {@link net.dv8tion.jda.api.entities.GuildChannel#retrieveInvites() GuildChannel#retrieveInvites()} (requires
     * {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL Permission.MANAGE_CHANNEL}).
     *
     * <p>There is a convenience method {@link #expand()} to get the expanded invite for an unexpanded one.
     *
     * @return Whether is invite expanded or not
     *
     * @see    #expand()
     */
    boolean isExpanded();

    /**
     * Whether this Invite grants only temporary access or not
     *
     * @throws IllegalStateException
     *         if this invite is not expanded
     *
     * @return Whether is invite expanded or not
     */
    boolean isTemporary();

    /**
     * POJO for the channel information provided by an invite.
     * 
     * @see #getChannel()
     */
    interface Channel extends ISnowflake
    {
        /**
         * The name of this channel.
         *
         * @return The channels's name
         */
        @Nonnull
        String getName();

        /**
         * The {@link net.dv8tion.jda.api.entities.ChannelType ChannelType} of this channel.
         * <br>Valid values are only {@link net.dv8tion.jda.api.entities.ChannelType#TEXT TEXT} or {@link net.dv8tion.jda.api.entities.ChannelType#VOICE VOICE}
         *
         * @return The channel's type
         */
        @Nonnull
        ChannelType getType();
    }

    /**
     * POJO for the guild information provided by an invite.
     * 
     * @see #getGuild()
     */
    interface Guild extends ISnowflake
    {
        /**
         * The icon id of this guild.
         *
         * @return The guild's icon id
         *
         * @see    #getIconUrl()
         */
        @Nullable
        String getIconId();

        /**
         * The icon url of this guild.
         *
         * @return The guild's icon url
         *
         * @see    #getIconId()
         */
        @Nullable
        String getIconUrl();

        /**
         * The name of this guild.
         *
         * @return The guilds's name
         */
        @Nonnull
        String getName();

        /**
         * The splash image id of this guild.
         *
         * @return The guild's splash image id or {@code null} if the guild has no splash image
         *
         * @see    #getSplashUrl()
         */
        @Nullable
        String getSplashId();

        /**
         * Returns the splash image url of this guild.
         *
         * @return The guild's splash image url or {@code null} if the guild has no splash image
         *
         * @see    #getSplashId()
         */
        @Nullable
        String getSplashUrl();
        
        /**
         * Returns the {@link net.dv8tion.jda.api.entities.Guild.VerificationLevel VerificationLevel} of this guild.
         * 
         * @return the verification level of the guild
         */
        @Nonnull
        VerificationLevel getVerificationLevel();
        
        /**
         * Returns the approximate count of online members in the guild. If the online member count was not included in the
         * invite, this will return -1. Counts will usually only be returned when resolving the invite via the 
         * {@link #resolve(net.dv8tion.jda.api.JDA, java.lang.String, boolean) Invite.resolve()} method with the
         * withCounts boolean set to {@code true}
         * 
         * @return the approximate count of online members in the guild, or -1 if not present in the invite
         */
        int getOnlineCount();
        
        /**
         * Returns the approximate count of total members in the guild. If the total member count was not included in the
         * invite, this will return -1. Counts will usually only be returned when resolving the invite via the 
         * {@link #resolve(net.dv8tion.jda.api.JDA, java.lang.String, boolean) Invite.resolve()} method with the
         * withCounts boolean set to {@code true}
         * 
         * @return the approximate count of total members in the guild, or -1 if not present in the invite
         */
        int getMemberCount();

        /**
         * The Features of the {@link net.dv8tion.jda.api.entities.Invite.Guild Guild}.
         * <p>
         * <b>Possible known features:</b>
         * <ul>
         *     <li>VIP_REGIONS - Guild has VIP voice regions</li>
         *     <li>VANITY_URL - Guild a vanity URL (custom invite link)</li>
         *     <li>INVITE_SPLASH - Guild has custom invite splash. See {@link #getSplashId()} and {@link #getSplashUrl()}</li>
         *     <li>VERIFIED - Guild is "verified"</li>
         *     <li>MORE_EMOJI - Guild is able to use more than 50 emoji</li>
         * </ul>
         *
         * @return Never-null, unmodifiable Set containing all of the Guild's features.
         */
        @Nonnull
        Set<String> getFeatures();
    }

    /**
     * POJO for the group information provided by an invite.
     *
     * @see #getChannel()
     */
    interface Group extends ISnowflake
    {
        /**
         * The icon id of this group or {@code null} if the group has no icon.
         *
         * @return The group's icon id
         *
         * @see    #getIconUrl()
         */
        @Nullable
        String getIconId();

        /**
         * The icon url of this group or {@code null} if the group has no icon.
         *
         * @return The group's icon url
         *
         * @see    #getIconId()
         */
        @Nullable
        String getIconUrl();

        /**
         * The name of this group or {@code null} if the group has no name.
         *
         * @return The group's name
         */
        @Nullable
        String getName();

        /**
         * The names of all users in this group. If the users were not included in the
         * invite, this will return {@code null}. Users will only be returned when resolving the invite via the
         * {@link #resolve(net.dv8tion.jda.api.JDA, java.lang.String, boolean) Invite.resolve()} method with the
         * {@code withCounts} boolean set to {@code true}.
         *
         * @return The names of the groups's users or null if not preset in the invite
         */
        @Nullable
        List<String> getUsers();
    }

    /**
     * Enum representing the type of an invite.
     *
     * @see #getType()
     */
    enum InviteType
    {
        GUILD,
        GROUP,
        UNKNOWN
    }
}
