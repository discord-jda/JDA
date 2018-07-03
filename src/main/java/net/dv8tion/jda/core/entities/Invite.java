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
import net.dv8tion.jda.core.entities.Guild.VerificationLevel;
import net.dv8tion.jda.core.entities.impl.InviteImpl;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.restaction.AuditableRestAction;

import javax.annotation.CheckReturnValue;
import java.time.OffsetDateTime;
import java.util.Set;

/**
 * Representation of a Discord Invite.
 * This class is immutable.
 *
 * @since  3.0
 * @author Aljoscha Grebe
 */
public interface Invite
{
    /**
     * Retrieves a new {@link net.dv8tion.jda.core.entities.Invite Invite} instance for the given invite code.
     * <br><b>You cannot resolve invites if you were banned from the origin Guild!</b>
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_INVITE Unknown Invite}
     *     <br>The Invite did not exist (possibly deleted) or the account is banned in the guild.</li>
     * </ul>
     *
     * @param  api
     *         The JDA instance
     * @param  code
     *         A valid invite code
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.core.entities.Invite Invite}
     *         <br>The Invite object
     */
    static RestAction<Invite> resolve(final JDA api, final String code)
    {
        return resolve(api, code, false);
    }
    
    /**
     * Retrieves a new {@link net.dv8tion.jda.core.entities.Invite Invite} instance for the given invite code.
     * <br><b>You cannot resolve invites if you were banned from the origin Guild!</b>
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_INVITE Unknown Invite}
     *     <br>The Invite did not exist (possibly deleted) or the account is banned in the guild.</li>
     * </ul>
     *
     * @param  api
     *         The JDA instance
     * @param  code
     *         A valid invite code
     * @param  withCounts
     *         Whether or not to include online and member counts
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.core.entities.Invite Invite}
     *         <br>The Invite object
     */
    static RestAction<Invite> resolve(final JDA api, final String code, final boolean withCounts)
    {
        return InviteImpl.resolve(api, code, withCounts);
    }

    /**
     * Deletes this invite.
     * <br>Requires {@link net.dv8tion.jda.core.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} in the invite's channel.
     * Will throw a {@link net.dv8tion.jda.core.exceptions.InsufficientPermissionException InsufficientPermissionException} otherwise.
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         if the account does not have {@link net.dv8tion.jda.core.Permission#MANAGE_SERVER MANAGE_SERVER} in the invite's channel
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @CheckReturnValue
    AuditableRestAction<Void> delete();

    /**
     * Tries to retrieve a new expanded {@link net.dv8tion.jda.core.entities.Invite Invite} with more info.
     * <br>Requires either {@link net.dv8tion.jda.core.Permission#MANAGE_SERVER MANAGE_SERVER} in the invite's guild or
     * {@link net.dv8tion.jda.core.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} in the invite's channel.
     * Will throw a {@link net.dv8tion.jda.core.exceptions.InsufficientPermissionException InsufficientPermissionException} otherwise.
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         if the account neither has {@link net.dv8tion.jda.core.Permission#MANAGE_SERVER MANAGE_SERVER} in the invite's guild nor
     *         {@link net.dv8tion.jda.core.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} in the invite's channel
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction} - Type: {@link net.dv8tion.jda.core.entities.Invite Invite}
     *         <br>The expanded Invite object
     *
     * @see    #isExpanded()
     */
    @CheckReturnValue
    RestAction<Invite> expand();

    /**
     * An {@link net.dv8tion.jda.core.entities.Invite.Channel Invite.Channel} object
     * containing information about this invite's origin channel.
     *
     * @return Information about this invite's origin channel
     * 
     * @see    net.dv8tion.jda.core.entities.Invite.Channel
     */
    Channel getChannel();

    /**
     * The invite code
     *
     * @return the invite code
     */
    String getCode();

    /**
     * The invite URL for this invite in the format of:
     * {@code "https://discord.gg/" + getCode()}
     *
     * @return Invite URL for this Invite
     */
    default String getURL()
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
     */
    OffsetDateTime getCreationTime();

    /**
     * An {@link net.dv8tion.jda.core.entities.Invite.Guild Invite.Guild} object
     * containing information about this invite's origin guild.
     *
     * @return Information about this invite's origin guild
     * 
     * @see    net.dv8tion.jda.core.entities.Invite.Guild
     */
    Guild getGuild();

    /**
     * The user who created this invite. This may be a fake user. For not expanded invites this may be null.
     *
     * @return The user who created this invite
     */
    User getInviter();

    /**
     * The {@link net.dv8tion.jda.core.JDA JDA} instance used to create this Invite
     *
     * @return the corresponding JDA instance
     */
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
     * obtained be {@link net.dv8tion.jda.core.entities.Guild#getInvites() Guild#getInvites()} (requires
     * {@link net.dv8tion.jda.core.Permission#MANAGE_CHANNEL Permission.MANAGE_CHANNEL}) or
     * {@link net.dv8tion.jda.core.entities.Channel#getInvites() Channel#getInvites()} (requires
     * {@link net.dv8tion.jda.core.Permission#MANAGE_SERVER Permission.MANAGE_SERVER}).
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
        String getName();

        /**
         * The {@link net.dv8tion.jda.core.entities.ChannelType ChannelType} of this channel.
         * <br>Valid values are only {@link net.dv8tion.jda.core.entities.ChannelType#TEXT TEXT} or {@link net.dv8tion.jda.core.entities.ChannelType#VOICE VOICE}
         *
         * @return The channel's type
         */
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
        String getIconId();

        /**
         * The icon url of this guild.
         *
         * @return The guild's icon url
         *
         * @see    #getIconId()
         */
        String getIconUrl();

        /**
         * The name of this guild.
         *
         * @return The guilds's name
         */
        String getName();

        /**
         * The splash image id of this guild.
         *
         * @return The guild's splash image id or {@code null} if the guild has no splash image
         *
         * @see    #getSplashUrl()
         */
        String getSplashId();

        /**
         * Returns the splash image url of this guild.
         *
         * @return The guild's splash image url or {@code null} if the guild has no splash image
         *
         * @see    #getSplashId()
         */
        String getSplashUrl();
        
        /**
         * Returns the {@link net.dv8tion.jda.core.entities.Guild.VerificationLevel VerificationLevel} of this guild.
         * 
         * @return the verification level of the guild
         */
        VerificationLevel getVerificationLevel();
        
        /**
         * Returns the approximate count of online members in the guild. If the online member count was not included in the
         * invite, this will return -1. Counts will usually only be returned when resolving the invite via the 
         * {@link #resolve(net.dv8tion.jda.core.JDA, java.lang.String, boolean) Invite.resolve()} method with the 
         * withCounts boolean set to {@code true}
         * 
         * @return the approximate count of online members in the guild, or -1 if not present in the invite
         */
        int getOnlineCount();
        
        /**
         * Returns the approximate count of total members in the guild. If the total member count was not included in the
         * invite, this will return -1. Counts will usually only be returned when resolving the invite via the 
         * {@link #resolve(net.dv8tion.jda.core.JDA, java.lang.String, boolean) Invite.resolve()} method with the 
         * withCounts boolean set to {@code true}
         * 
         * @return the approximate count of total members in the guild, or -1 if not present in the invite
         */
        int getMemberCount();

        /**
         * The Features of the {@link net.dv8tion.jda.core.entities.Invite.Guild Guild}.
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
        Set<String> getFeatures();
    }
}
