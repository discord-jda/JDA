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

package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild.VerificationLevel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.InviteUpdateTargetUsersAction;
import net.dv8tion.jda.api.utils.ImageProxy;
import net.dv8tion.jda.internal.entities.InviteImpl;
import org.jetbrains.annotations.Unmodifiable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static net.dv8tion.jda.api.entities.Guild.BANNER_URL;
import static net.dv8tion.jda.api.entities.Guild.NSFWLevel;

/**
 * Representation of a Discord Invite.
 * This class is immutable.
 *
 * @see    #resolve(JDA, String)
 * @see    #resolve(JDA, String, boolean)
 * @see    net.dv8tion.jda.api.entities.Guild#retrieveInvites() Guild.retrieveInvites()
 * @see    net.dv8tion.jda.api.entities.channel.attribute.IInviteContainer#retrieveInvites()
 *
 * @author Aljoscha Grebe
 */
public interface Invite {
    /**
     * Retrieves a new {@link Invite Invite} instance for the given invite code.
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
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: {@link Invite Invite}
     *         <br>The Invite object
     */
    @Nonnull
    @CheckReturnValue
    static RestAction<Invite> resolve(@Nonnull JDA api, @Nonnull String code) {
        return resolve(api, code, false);
    }

    /**
     * Retrieves a new {@link Invite Invite} instance for the given invite code.
     *
     * <p>An invitation cannot be resolved if:
     * <ul>
     *     <li>It does not exist</li>
     *     <li>Your bot is banned from the guild</li>
     *     <li>Your bot is not part of the {@linkplain #retrieveTargetUsers(JDA, String) target users}</li>
     * </ul>
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INVITE Unknown Invite}
     *     <br>The Invite did not exist (possibly deleted) or the account is banned in the guild.</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS Missing Permissions}
     *     <br>The Invite is restricted to a set of users and your bot is not one of them.</li>
     * </ul>
     *
     * @param  api
     *         The JDA instance
     * @param  code
     *         A valid invite code
     * @param  withCounts
     *         Whether or not to include online and member counts for guild invites or users for group invites
     *
     * @throws IllegalArgumentException
     *         If any of the provided arguments is {@code null}
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: {@link Invite Invite}
     *         <br>The Invite object
     */
    @Nonnull
    @CheckReturnValue
    static RestAction<Invite> resolve(@Nonnull JDA api, @Nonnull String code, boolean withCounts) {
        return InviteImpl.resolve(api, code, withCounts);
    }

    /**
     * Updates the list of users that are able to use the provided invite code.
     *
     * <p>The target users are processed asynchronously, the action may complete before all targeted users are set,
     * you can use {@link #retrieveTargetUsersJobStatus(JDA, String)} to check the status.
     *
     * <p>This endpoint requires the {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER} permission in the target guild.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INVITE Unknown Invite}
     *     <br>The Invite did not exist (possibly deleted), or is a group DM invite, or the account is banned in the guild.</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS Missing Permissions}
     *     <br>If the bot does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER} permission in the target guild.</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#INVALID_FORM_BODY Invalid Form Body}
     *     <br>If at least one user ID is invalid.</li>
     * </ul>
     *
     * @param  api
     *         The JDA instance
     * @param  code
     *         A valid invite code
     *
     * @throws IllegalArgumentException
     *         If any of the provided arguments is {@code null}
     *
     * @return A {@link InviteUpdateTargetUsersAction} to update the target users
     */
    @Nonnull
    @CheckReturnValue
    static InviteUpdateTargetUsersAction updateTargetUsers(@Nonnull JDA api, @Nonnull String code) {
        return InviteImpl.updateTargetUsers(api, code);
    }

    /**
     * Retrieves a list of {@linkplain UserSnowflake user IDs} to which the given invite code is restricted to.
     * <br>Only users in the returned list are able to use the invite. This can be changed with {@link #updateTargetUsers(JDA, String)}.
     *
     * <p>This endpoint requires the {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER} permission in the target guild.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INVITE Unknown Invite}
     *     <br>The Invite did not exist (possibly deleted), or is a group DM invite, or the account is banned in the guild.</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INVITE_TARGET_USERS Unknown Invite Target Users}
     *     <br>If the invite does not have any target users.</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#INVITE_TARGET_USERS_FILE_NOT_PROCESSED Invite Target Users File Not Processed}
     *     <br>If the invite has target users, but they were not processed yet.</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS Missing Permissions}
     *     <br>If the bot does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER} permission in the target guild.</li>
     * </ul>
     *
     * @param  api
     *         The JDA instance
     * @param  code
     *         A valid invite code
     *
     * @throws IllegalArgumentException
     *         If any of the provided arguments is {@code null}
     *
     * @return A {@link RestAction} returning a list of {@link UserSnowflake}
     */
    @Nonnull
    @CheckReturnValue
    static RestAction<List<? extends UserSnowflake>> retrieveTargetUsers(@Nonnull JDA api, @Nonnull String code) {
        return InviteImpl.retrieveTargetUsers(api, code);
    }

    /**
     * Retrieves a {@link TargetUsersJobStatus} representing the status of a {@link #updateTargetUsers()} request.
     *
     * <p>This endpoint requires the {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER} permission in the target guild.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INVITE Unknown Invite}
     *     <br>The Invite did not exist (possibly deleted), or is a group DM invite, or the account is banned in the guild.</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INVITE_TARGET_USERS_JOB Unknown Invite Target Users Job}
     *     <br>If the invite does not have any target users job.</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS Missing Permissions}
     *     <br>If the bot does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER} permission in the target guild.</li>
     * </ul>
     *
     * @param  api
     *         The JDA instance
     * @param  code
     *         A valid invite code
     *
     * @throws IllegalArgumentException
     *         If any of the provided arguments is {@code null}
     *
     * @return A {@link RestAction} returning a {@link TargetUsersJobStatus}
     */
    @Nonnull
    @CheckReturnValue
    static RestAction<TargetUsersJobStatus> retrieveTargetUsersJobStatus(@Nonnull JDA api, @Nonnull String code) {
        return InviteImpl.retrieveTargetUsersJobStatus(api, code);
    }

    /**
     * Deletes this invite.
     * <br>Requires {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} in the invite's channel.
     * Will throw an {@link net.dv8tion.jda.api.exceptions.InsufficientPermissionException InsufficientPermissionException} otherwise.
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
     * Tries to retrieve a new expanded {@link Invite Invite} with more info.
     * <br>As bots can't be in groups this is only available for guild invites and will throw an {@link java.lang.IllegalStateException IllegalStateException}
     * for other types.
     * <br>Requires either {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER} in the invite's guild or
     * {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} in the invite's channel.
     * Will throw an {@link net.dv8tion.jda.api.exceptions.InsufficientPermissionException InsufficientPermissionException} otherwise.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         if the account neither has {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER} in the invite's guild nor
     *         {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} in the invite's channel
     * @throws java.lang.IllegalStateException
     *         If this is a group invite
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: {@link Invite Invite}
     *         <br>The expanded Invite object
     *
     * @see    #getType()
     * @see    #isExpanded()
     */
    @Nonnull
    @CheckReturnValue
    RestAction<Invite> expand();

    /**
     * Updates the list of users that are able to use the provided invite code.
     *
     * <p>The target users are processed asynchronously, the action may complete before all targeted users are set,
     * you can use {@link #retrieveTargetUsersJobStatus()} to check the status.
     *
     * <p>This endpoint requires the {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER} permission in the target guild.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INVITE Unknown Invite}
     *     <br>The Invite did not exist (possibly deleted), or is a group DM invite, or the account is banned in the guild.</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS Missing Permissions}
     *     <br>If the bot does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER} permission in the target guild.</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#INVALID_FORM_BODY Invalid Form Body}
     *     <br>If at least one user ID is invalid.</li>
     * </ul>
     *
     * @throws IllegalStateException
     *         If the invite is not from a guild
     *
     * @return A {@link InviteUpdateTargetUsersAction} to update the target users
     */
    @Nonnull
    @CheckReturnValue
    InviteUpdateTargetUsersAction updateTargetUsers();

    /**
     * Retrieves a list of {@linkplain UserSnowflake user IDs} to which the given invite code is restricted to.
     * <br>Only users in the returned list are able to use the invite. This can be changed with {@link #updateTargetUsers()}.
     *
     * <p>This endpoint requires the {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER} permission in the target guild.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INVITE Unknown Invite}
     *     <br>The Invite did not exist (possibly deleted), or is a group DM invite, or the account is banned in the guild.</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INVITE_TARGET_USERS Unknown Invite Target Users}
     *     <br>If the invite does not have any target users.</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#INVITE_TARGET_USERS_FILE_NOT_PROCESSED Invite Target Users File Not Processed}
     *     <br>If the invite has target users, but they were not processed yet.</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS Missing Permissions}
     *     <br>If the bot does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER} permission in the target guild.</li>
     * </ul>
     *
     * @throws IllegalStateException
     *         If the invite is not from a guild
     *
     * @return A {@link RestAction} returning a list of {@link UserSnowflake}
     */
    @Nonnull
    @CheckReturnValue
    RestAction<List<? extends UserSnowflake>> retrieveTargetUsers();

    /**
     * Retrieves a {@link TargetUsersJobStatus} representing the status of a {@link #updateTargetUsers()} request.
     *
     * <p>This endpoint requires the {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER} permission in the target guild.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INVITE Unknown Invite}
     *     <br>The Invite did not exist (possibly deleted), or is a group DM invite, or the account is banned in the guild.</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_INVITE_TARGET_USERS_JOB Unknown Invite Target Users Job}
     *     <br>If the invite does not have any target users job.</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS Missing Permissions}
     *     <br>If the bot does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_SERVER MANAGE_SERVER} permission in the target guild.</li>
     * </ul>
     *
     * @throws IllegalStateException
     *         If the invite is not from a guild
     *
     * @return A {@link RestAction} returning a {@link TargetUsersJobStatus}
     */
    @Nonnull
    @CheckReturnValue
    RestAction<TargetUsersJobStatus> retrieveTargetUsersJobStatus();

    /**
     * The type of this invite.
     *
     * @return The invite's type
     */
    @Nonnull
    Invite.InviteType getType();

    /**
     * The target type of this invite or {@link TargetType#NONE} if this invite does not have a {@link #getTarget() InviteTarget}.
     *
     * @return The invite's target type or {@link TargetType#NONE}
     *
     * @see    Invite.TargetType
     */
    @Nonnull
    Invite.TargetType getTargetType();

    /**
     * An {@link Invite.Channel Invite.Channel} object
     * containing information about this invite's origin channel.
     *
     * @return Information about this invite's origin channel or null in case of a group invite
     *
     * @see    Invite.Channel
     */
    @Nullable
    Channel getChannel();

    /**
     * An {@link Invite.Group Invite.Group} object
     * containing information about this invite's origin group.
     *
     * @return Information about this invite's origin group or null in case of a guild invite
     *
     * @see    Invite.Group
     */
    @Nullable
    Group getGroup();

    /**
     * An {@link Invite.InviteTarget Invite.InviteTarget} object
     * containing information about this invite's target or {@code null}
     * if this invite does not have a target.
     *
     * @return Information about this invite's target or {@code null}
     *
     * @see    Invite.InviteTarget
     */
    @Nullable
    InviteTarget getTarget();

    /**
     * The invite code
     *
     * @return the invite code
     */
    @Nonnull
    String getCode();

    /**
     * The invite URL for this invite in the format of:
     * {@code "https://discord.gg/" + getCode()}
     *
     * @return Invite URL for this Invite
     */
    @Nonnull
    default String getUrl() {
        return "https://discord.gg/" + getCode();
    }

    /**
     * An {@link Invite.Guild Invite.Guild} object
     * containing information about this invite's origin guild.
     *
     * @return Information about this invite's origin guild or null in case of a group invite
     *
     * @see    Invite.Guild
     */
    @Nullable
    Guild getGuild();

    /**
     * The roles which will be assigned to the members using this invite.
     *
     * @return Unmodifiable list of roles assigned by this invite
     */
    @Nonnull
    @Unmodifiable
    List<Role> getRoles();

    /**
     * The user who created this invite. For not expanded invites this may be null.
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
     * {@link net.dv8tion.jda.api.entities.channel.attribute.IInviteContainer#retrieveInvites() IInviteContainer#retrieveInvites()} (requires
     * {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL Permission.MANAGE_CHANNEL}).
     *
     * <p>There is a convenience method {@link #expand()} to get the expanded invite for an unexpanded one.
     *
     * @return Whether this invite is expanded or not
     *
     * @see    #expand()
     */
    boolean isExpanded();

    /**
     * Whether this Invite grants only temporary access or not.
     *
     * <p>This works only for expanded invites and will throw a {@link IllegalStateException} otherwise!
     *
     * @throws IllegalStateException
     *         if this invite is not expanded
     *
     * @return Whether this invite is temporary or not
     *
     * @see    #expand()
     * @see    #isExpanded()
     */
    boolean isTemporary();

    /**
     * Whether this Invite is a guest invite for a voice channel or not. A guest is able to join a voice call
     * from the invite without being granted guild membership nor having visibility to other guild channels.
     * Once the user leaves the voice call, their ability to rejoin the voice call and see the guild is revoked.
     *
     * <p>Applicable only to invites to a specific {@linkplain #getChannel() channel}.
     *
     * @return Whether this invite is a guest invite for a voice channel or not
     *
     * @see #getChannel()
     */
    boolean isGuest();

    /**
     * POJO for the channel information provided by an invite.
     *
     * @see #getChannel()
     */
    interface Channel extends ISnowflake {
        /**
         * The name of this channel.
         *
         * @return The channel's name
         */
        @Nonnull
        String getName();

        /**
         * The {@link ChannelType ChannelType} of this channel.
         * <br>Valid values are only {@link ChannelType#TEXT TEXT} or {@link ChannelType#VOICE VOICE}
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
    interface Guild extends ISnowflake {
        /**
         * The vanity url code for this Guild. The vanity url is the custom invite code of partnered / official / boosted Guilds.
         * <br>The returned String will be the code that can be provided to {@code discord.gg/{code}} to get the invite link.
         *
         * @return The vanity code or null
         *
         * @see    #getVanityUrl()
         */
        @Nullable
        String getVanityCode();

        /**
         * The vanity url for this Guild. The vanity url is the custom invite code of partnered / official / boosted Guilds.
         * <br>The returned String will be the vanity invite link to this guild.
         *
         * @return The vanity url or null
         */
        @Nullable
        default String getVanityUrl() {
            return getVanityCode() == null ? null : "https://discord.gg/" + getVanityCode();
        }

        /**
         * The guild banner id.
         * <br>This is shown in guilds below the guild name.
         *
         * @return The guild banner id or null
         *
         * @see    #getBannerUrl()
         */
        @Nullable
        String getBannerId();

        /**
         * The guild banner url.
         * <br>This is shown in guilds below the guild name.
         *
         * @return The guild banner url or null
         */
        @Nullable
        default String getBannerUrl() {
            String bannerId = getBannerId();
            return bannerId == null
                    ? null
                    : String.format(BANNER_URL, getId(), bannerId, bannerId.startsWith("a_") ? "gif" : "png");
        }

        /**
         * Returns an {@link ImageProxy} for this guild's banner image.
         *
         * @return Possibly-null {@link ImageProxy} of this guild's banner image
         *
         * @see    #getBannerUrl()
         */
        @Nullable
        default ImageProxy getBanner() {
            String bannerUrl = getBannerUrl();
            return bannerUrl == null ? null : new ImageProxy(bannerUrl);
        }

        /**
         * The description for this guild.
         * <br>This is displayed in the server browser below the guild name for verified guilds,
         * and in embedded invite links.
         *
         * @return The guild's description
         */
        @Nullable
        String getDescription();

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
         * Returns an {@link ImageProxy} for this guild's icon
         *
         * @return Possibly-null {@link ImageProxy} of this guild's icon
         *
         * @see    #getIconUrl()
         */
        @Nullable
        default ImageProxy getIcon() {
            String iconUrl = getIconUrl();
            return iconUrl == null ? null : new ImageProxy(iconUrl);
        }

        /**
         * The name of this guild.
         *
         * @return The guild's name
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
         * Returns an {@link ImageProxy} for this invite guild's splash image.
         *
         * @return Possibly-null {@link ImageProxy} of this invite guild's splash image
         *
         * @see    #getSplashUrl()
         */
        @Nullable
        default ImageProxy getSplash() {
            String splashUrl = getSplashUrl();
            return splashUrl == null ? null : new ImageProxy(splashUrl);
        }

        /**
         * Returns the {@link net.dv8tion.jda.api.entities.Guild.VerificationLevel VerificationLevel} of this guild.
         *
         * @return the verification level of the guild
         */
        @Nonnull
        VerificationLevel getVerificationLevel();

        /**
         * Returns the {@link NSFWLevel} of this guild.
         *
         * @return the nsfw level of the guild
         */
        @Nonnull
        NSFWLevel getNSFWLevel();

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
         * The Features of the {@link Invite.Guild Guild}.
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

        /**
         * The welcome screen of the {@link Invite.Guild Guild}.
         * <br>This will be {@code null} if the Guild has no welcome screen,
         * or if the invite came from a {@link net.dv8tion.jda.api.events.guild.invite.GuildInviteCreateEvent GuildInviteCreateEvent}.
         *
         * @return The welcome screen of this Guild or {@code null}
         */
        @Nullable
        GuildWelcomeScreen getWelcomeScreen();
    }

    /**
     * Represents a role assigned by an invitation.
     *
     * @see #getRoles()
     */
    interface Role extends IMentionable {
        /**
         * The actual position of the Role as stored and given by Discord.
         * <br>Role positions are actually based on a pairing of the creation time (as stored in the snowflake id)
         * and the position. If 2 or more roles share the same position then they are sorted based on their creation date.
         * <br>The more recent a role was created, the lower it is in the hierarchy.
         *
         * @return The true, Discord stored, position of the Role.
         */
        int getPositionRaw();

        /**
         * The Name of this Role.
         *
         * @return Never-null String containing the name of this Role.
         */
        @Nonnull
        String getName();

        /**
         * Whether this Role is managed by an integration
         *
         * @return True, if this Role is managed.
         */
        boolean isManaged();

        /**
         * Whether this Role is hoisted
         * <br>Members in a hoisted role are displayed in their own grouping on the user-list
         *
         * @return True, if this Role is hoisted.
         */
        boolean isHoisted();

        /**
         * Whether this Role is mentionable
         *
         * @return True, if Role is mentionable.
         */
        boolean isMentionable();

        /**
         * The {@code long} representation of the literal permissions that this Role has.
         * <br><b>NOTE:</b> these do not necessarily represent the permissions this role will have in a {@link net.dv8tion.jda.api.entities.channel.middleman.GuildChannel GuildChannel}.
         *
         * @return Never-negative long containing offset permissions of this role.
         */
        long getPermissionsRaw();

        /**
         * The colors this Role is displayed in.
         *
         * <p>See {@link RoleColors} for detailed information on how these work.
         *
         * @return {@link RoleColors}
         *
         * @see RoleColors#isDefault()
         * @see RoleColors#isGradient()
         * @see RoleColors#isHolographic()
         */
        @Nonnull
        RoleColors getColors();

        /**
         * The {@link RoleIcon Icon} of this role or {@code null} if no custom image or emoji is set.
         * This icon will be displayed next to the role's name in the members tab and in chat.
         *
         * @return Possibly-null {@link RoleIcon Icon} of this role
         */
        @Nullable
        RoleIcon getIcon();

        // TODO add getFlags()

        /**
         * The {@code long} representation of the flags that this Role has.
         *
         * @return {@code long} bitset containing the flags of this role
         */
        long getFlagsRaw();

        @Nonnull
        @Override
        default String getAsMention() {
            // Can't be @everyone
            return "<@&" + getId() + ">";
        }
    }

    /**
     * POJO for the group information provided by an invite.
     *
     * @see #getChannel()
     */
    interface Group extends ISnowflake {
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
         * Returns an {@link ImageProxy} for this group invite's icon.
         *
         * @return Possibly-null {@link ImageProxy} of this group invite's icon
         *
         * @see    #getIconUrl()
         */
        @Nullable
        default ImageProxy getIcon() {
            String iconUrl = getIconUrl();
            return iconUrl == null ? null : new ImageProxy(iconUrl);
        }

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
         * @return The names of the group's users or null if not preset in the invite
         */
        @Nullable
        List<String> getUsers();
    }

    /**
     * POJO for the target of this invite.
     *
     * @see #getTarget()
     */
    interface InviteTarget {

        /**
         * The type of this invite target.
         *
         * @return The type of this invite target
         */
        @Nonnull
        TargetType getType();

        /**
         * The Snowflake id of the target entity of this invite.
         *
         * @throws IllegalStateException
         *         If there is no target entity, {@link #getType() TargetType} is {@link TargetType#UNKNOWN}
         *
         * @return The id of the target entity
         */
        @Nonnull
        String getId();

        /**
         * The Snowflake id of the target entity of this invite.
         *
         * @throws IllegalStateException
         *         If there is no target entity, {@link #getType() TargetType} is {@link TargetType#UNKNOWN}
         *
         * @return The id of the target entity
         */
        long getIdLong();

        /**
         * The target {@link User} of this invite or {@code null} if the {@link #getType() TargeType} is not {@link TargetType#STREAM}
         *
         * @return The target user of this invite
         *
         * @see    net.dv8tion.jda.api.entities.User
         */
        @Nullable
        User getUser();

        /**
         * The target {@link EmbeddedApplication} of this invite or {@code null} if the {@link #getType() TargeType} is not {@link TargetType#EMBEDDED_APPLICATION}
         *
         * @return The target application of this invite
         *
         * @see    Invite.EmbeddedApplication
         */
        @Nullable
        EmbeddedApplication getApplication();
    }

    /**
     * POJO for the target application information provided by an invite.
     *
     * @see InviteTarget#getApplication()
     */
    interface EmbeddedApplication extends ISnowflake {
        /**
         * The name of this application.
         *
         * @return The name of this application.
         */
        @Nonnull
        String getName();

        /**
         * The description of this application.
         *
         * @return The description of this application.
         */
        @Nonnull
        String getDescription();

        /**
         * The summary of this application or {@code null} if this application has no summary.
         *
         * @return The summary of this application.
         */
        @Nullable
        String getSummary();

        /**
         * The icon id of this application or {@code null} if the application has no icon.
         *
         * @return The application's icon id
         *
         * @see    #getIconUrl()
         */
        @Nullable
        String getIconId();

        /**
         * The icon url of this application or {@code null} if the application has no icon.
         *
         * @return The application's icon url
         *
         * @see    #getIconId()
         */
        @Nullable
        String getIconUrl();

        /**
         * Returns an {@link ImageProxy} for this application invite's icon.
         *
         * @return Possibly-null {@link ImageProxy} of this application invite's icon
         *
         * @see    #getIconUrl()
         */
        @Nullable
        default ImageProxy getIcon() {
            String iconUrl = getIconUrl();
            return iconUrl == null ? null : new ImageProxy(iconUrl);
        }

        /**
         * The max participant count of this application or {@code -1} if no max participant count is set
         *
         * @return {@code -1} if this application does not have a max participant count
         */
        int getMaxParticipants();
    }

    /**
     * Represents the status of an invitation's target users job.
     *
     * @see #retrieveTargetUsersJobStatus()
     * @see #retrieveTargetUsersJobStatus(JDA, String)
     */
    interface TargetUsersJobStatus {
        /**
         * The job status
         *
         * @return The job status
         */
        @Nonnull
        Status getStatus();

        /**
         * The number of users allowed to use the invite.
         *
         * @return number of users allowed to use the invite
         */
        int getTotalUsers();

        /**
         * The number of users that have been processed so far.
         *
         * @return Number of users processed so far
         */
        int getProcessedUsers();

        /**
         * The moment at which this job was started.
         *
         * @return The moment at which this job was started
         */
        @Nonnull
        OffsetDateTime getCreatedAt();

        /**
         * The moment at which this job was finished successfully,
         * or {@code null} if it has failed or not finished yet.
         *
         * @return The moment at which this job was finished successfully, or {@code null}
         */
        @Nullable
        OffsetDateTime getCompletedAt();

        /**
         * The error message if this job failed, or {@code null}
         *
         * @return The error message, or {@code null}
         */
        @Nullable
        String getErrorMessage();

        /**
         * Status of an invite's target users job
         */
        enum Status {
            UNKNOWN(-1),
            /** The default value */
            UNSPECIFIED(0),
            /** The job is still being processed */
            PROCESSING(1),
            /** The job has been completed successfully */
            COMPLETED(2),
            /** The job has failed, see {@link #getErrorMessage()} */
            FAILED(3);

            private final int key;

            Status(int key) {
                this.key = key;
            }

            /**
             * The key corresponding to this status.
             *
             * @return Key corresponding to this status
             */
            public int getKey() {
                return key;
            }

            /**
             * Resolves the provided raw API key to the enum constant.
             *
             * @param  key
             *         The api key to check
             *
             * @return The resolved Status or {@link #UNKNOWN}
             */
            @Nonnull
            public static Status fromKey(int key) {
                for (Status status : values()) {
                    if (status.key == key) {
                        return status;
                    }
                }
                return UNKNOWN;
            }
        }
    }

    /**
     * Enum representing the type of an invite.
     *
     * @see #getType()
     */
    enum InviteType {
        GUILD,
        GROUP,
        UNKNOWN
    }

    /**
     * A TargetType indicates additional action to be taken by the client on accepting the invite,
     * typically connecting external services or launching external applications depending on the specific TargetType.
     *
     * <p>Some actions might not be available or show up on certain devices.
     *
     * @see InviteTarget#getType()
     */
    enum TargetType {
        /**
         * The invite does not have a target type, {@link Invite#getTarget()} will return {@code null}.
         */
        NONE(0),

        /**
         * The invite points to a user's stream in a voice channel.
         * The user to whose stream the invite goes can be get with {@link InviteTarget#getUser() InviteTarget.getUser} and is not {@code null}.
         *
         * @see InviteTarget#getUser()
         */
        STREAM(1),

        /**
         * The invite points to an application in a voice channel.
         * The application to which the invite goes can be get with {@link InviteTarget#getApplication() InviteTarget.getApplication} and is not {@code null}.
         *
         * @see InviteTarget#getApplication()
         */
        EMBEDDED_APPLICATION(2),

        /**
         * The invite points to a role subscription listing in a guild.
         * <br>These cannot be created by bots.
         */
        ROLE_SUBSCRIPTIONS_PURCHASE(3),

        /**
         * Unknown Discord invite target type. Should never happen and would only possibly happen if Discord implemented a new
         * target type and JDA had yet to implement support for it.
         */
        UNKNOWN(-1);

        private final int id;

        TargetType(int id) {
            this.id = id;
        }

        /**
         * The Discord id key used to represent the target type.
         *
         * @return The id key used by discord for this channel type.
         */
        public int getId() {
            return id;
        }

        /**
         * Static accessor for retrieving a target type based on its Discord id key.
         *
         * @param  id
         *         The id key of the requested target type.
         *
         * @return The TargetType that is referred to by the provided key. If the id key is unknown, {@link #UNKNOWN} is returned.
         */
        @Nonnull
        public static TargetType fromId(int id) {
            for (TargetType type : values()) {
                if (type.id == id) {
                    return type;
                }
            }
            return UNKNOWN;
        }
    }
}
