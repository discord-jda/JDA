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

import net.dv8tion.jda.annotations.Incubating;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Represents a Guild-specific User.
 *
 * <p>Contains all guild-specific information about a User. (Roles, Nickname, VoiceStatus etc.)
 *
 * @since 3.0
 *
 * @see   Guild#getMember(User)
 * @see   Guild#getMemberCache()
 * @see   Guild#getMemberById(long)
 * @see   Guild#getMemberByTag(String)
 * @see   Guild#getMemberByTag(String, String)
 * @see   Guild#getMembersByEffectiveName(String, boolean)
 * @see   Guild#getMembersByName(String, boolean)
 * @see   Guild#getMembersByNickname(String, boolean)
 * @see   Guild#getMembersWithRoles(Role...)
 * @see   Guild#getMembers()
 */
public interface Member extends IMentionable, IPermissionHolder
{
    /** Template for {@link #getAvatarUrl()}. */
    String AVATAR_URL = "https://cdn.discordapp.com/guilds/%s/users/%s/avatars/%s.%s";
    /** Maximum number of days a Member can be timed out for */
    int MAX_TIME_OUT_LENGTH = 28;

    /**
     * The user wrapped by this Entity.
     *
     * @return {@link net.dv8tion.jda.api.entities.User User}
     */
    @Nonnull
    User getUser();

    /**
     * The Guild in which this Member is represented.
     *
     * @return {@link net.dv8tion.jda.api.entities.Guild Guild}
     */
    @Nonnull
    Guild getGuild();

    /**
     * The JDA instance.
     *
     * @return The current JDA instance.
     */
    @Nonnull
    JDA getJDA();

    /**
     * The {@link java.time.OffsetDateTime Time} this Member joined the Guild.
     * <br>If the member was loaded through a presence update (lazy loading) this will be identical
     * to the creation time of the guild. You can use {@link #hasTimeJoined()} to test whether this time
     * can be relied on.
     *
     * <p>You can use {@link Guild#retrieveMemberById(String) guild.retrieveMemberById(member.getId())}
     * to load the join time.
     *
     * @return The time at which this user has joined the guild.
     */
    @Nonnull
    OffsetDateTime getTimeJoined();

    /**
     * Whether this member has accurate {@link #getTimeJoined()} information.
     * <br>Discord doesn't always provide this information when we load members so we have to fallback
     * to the {@link Guild} creation time.
     *
     * <p>You can use {@link Guild#retrieveMemberById(String) guild.retrieveMemberById(member.getId())}
     * to load the join time.
     *
     * @return True, if {@link #getTimeJoined()} is accurate
     */
    boolean hasTimeJoined();

    /**
     * The time when this member boosted the guild.
     * <br>Null indicates this member is not currently boosting the guild.
     *
     * @return The boosting time, or null if the member is not boosting
     *
     * @since  4.0.0
     */
    @Nullable
    OffsetDateTime getTimeBoosted();

    /**
     * Returns whether a member is boosting the guild or not.
     *
     * @return True, if it is boosting
     */
    boolean isBoosting();

    /**
     * The time this Member will be released from time out.
     * <br>If this Member is not in time out, this returns {@code null}.
     * This may also return dates in the past, in which case the time out has expired.
     *
     * @return The time this Member will be released from time out or {@code null} if not in time out
     */
    @Nullable
    OffsetDateTime getTimeOutEnd();

    /**
     * Whether this Member is in time out.
     * <br>While a Member is in time out, all permissions except {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL VIEW_CHANNEL} and
     * {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY MESSAGE_HISTORY} are removed from them.
     *
     * @return True, if this Member is in time out
     */
    default boolean isTimedOut()
    {
        return getTimeOutEnd() != null && getTimeOutEnd().isAfter(OffsetDateTime.now());
    }

    /**
     * The {@link net.dv8tion.jda.api.entities.GuildVoiceState VoiceState} of this Member.
     * <br><b>This will be null when the {@link net.dv8tion.jda.api.utils.cache.CacheFlag#VOICE_STATE} is disabled manually</b>
     *
     * <p>This can be used to get the Member's VoiceChannel using {@link GuildVoiceState#getChannel()}.
     *
     * <p>This requires {@link net.dv8tion.jda.api.utils.cache.CacheFlag#VOICE_STATE CacheFlag.VOICE_STATE} to be enabled!
     *
     * @return {@link net.dv8tion.jda.api.entities.GuildVoiceState GuildVoiceState}
     */
    @Nullable
    GuildVoiceState getVoiceState();

    /**
     * The activities of the user.
     * <br>If the user does not currently have any activity, this returns an empty list.
     *
     * <p>This requires {@link net.dv8tion.jda.api.utils.cache.CacheFlag#ACTIVITY CacheFlag.ACTIVITY} to be enabled!
     *
     * @return Immutable list of {@link Activity Activities} for the user
     */
    @Nonnull
    List<Activity> getActivities();

    /**
     * Returns the {@link net.dv8tion.jda.api.OnlineStatus OnlineStatus} of the User.
     * <br>If the {@link net.dv8tion.jda.api.OnlineStatus OnlineStatus} is unrecognized, will return {@link net.dv8tion.jda.api.OnlineStatus#UNKNOWN UNKNOWN}.
     *
     * <p>This will always return {@link OnlineStatus#OFFLINE} if {@link net.dv8tion.jda.api.utils.cache.CacheFlag#ONLINE_STATUS CacheFlag.ONLINE_STATUS} is disabled.
     *
     * @return The current {@link net.dv8tion.jda.api.OnlineStatus OnlineStatus} of the {@link net.dv8tion.jda.api.entities.User User}.
     */
    @Nonnull
    OnlineStatus getOnlineStatus();

    /**
     * The platform dependent {@link net.dv8tion.jda.api.OnlineStatus} of this member.
     * <br>Since a user can be connected from multiple different devices such as web and mobile,
     * discord specifies a status for each {@link net.dv8tion.jda.api.entities.ClientType}.
     *
     * <p>If a user is not online on the specified type,
     * {@link net.dv8tion.jda.api.OnlineStatus#OFFLINE OFFLINE} is returned.
     *
     * <p>This requires {@link net.dv8tion.jda.api.utils.cache.CacheFlag#CLIENT_STATUS CacheFlag.CLIENT_STATUS} to be enabled!
     *
     * @param  type
     *         The type of client
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided type is null
     *
     * @return The status for that specific client or OFFLINE
     *
     * @since  4.0.0
     */
    @Nonnull
    OnlineStatus getOnlineStatus(@Nonnull ClientType type);

    /**
     * A Set of all active {@link net.dv8tion.jda.api.entities.ClientType ClientTypes} of this Member.
     * Every {@link net.dv8tion.jda.api.OnlineStatus OnlineStatus} other than {@code OFFLINE} and {@code UNKNOWN}
     * is interpreted as active.
     * Since {@code INVISIBLE} is only possible for the SelfUser, other Members will never have ClientTypes show as
     * active when actually being {@code INVISIBLE}, since they will show as {@code OFFLINE}.
     * <br>If the Member is currently not active with any Client, this returns an empty Set.
     * <br>When {@link net.dv8tion.jda.api.utils.cache.CacheFlag#CLIENT_STATUS CacheFlag.CLIENT_STATUS} is disabled,
     * active clients will not be tracked and this will always return an empty Set.
     * <br>Since a user can be connected from multiple different devices such as web and mobile,
     * discord specifies a status for each {@link net.dv8tion.jda.api.entities.ClientType}.
     *
     * @return EnumSet of all active {@link net.dv8tion.jda.api.entities.ClientType ClientTypes}
     *
     * @since  4.0.0
     */
    @Nonnull
    EnumSet<ClientType> getActiveClients();

    /**
     * Returns the current nickname of this Member for the parent Guild.
     *
     * <p>This can be changed using
     * {@link net.dv8tion.jda.api.entities.Guild#modifyNickname(Member, String) modifyNickname(Member, String)}.
     *
     * @return The nickname or null, if no nickname is set.
     */
    @Nullable
    String getNickname();

    /**
     * Retrieves the Name displayed in the official Discord Client.
     *
     * @return The Nickname of this Member or the Username if no Nickname is present.
     */
    @Nonnull
    String getEffectiveName();

    /**
     * The Discord Id for this member's per guild avatar image.
     * If the member has not set a per guild avatar, this will return null.
     *
     * @return Possibly-null String containing the {@link net.dv8tion.jda.api.entities.Member} per guild avatar id.
     */
    @Nullable
    String getAvatarId();

    /**
     * The URL for the member's per guild avatar image.
     * If the member has not set a per guild avatar, this will return null.
     *
     * @return Possibly-null String containing the {@link net.dv8tion.jda.api.entities.Member} per guild avatar url.
     */
    @Nullable
    default String getAvatarUrl()
    {
        String avatarId = getAvatarId();
        return avatarId == null ? null : String.format(AVATAR_URL, getGuild().getId(), getId(), avatarId, avatarId.startsWith("a_") ? "gif" : "png");
    }

    /**
     * The URL for the member's effective avatar image.
     * If they do not have a per guild avatar set, this will return the URL of
     * their effective {@link User} avatar.
     *
     * @return Never-null String containing the {@link net.dv8tion.jda.api.entities.Member} avatar url.
     */
    @Nonnull
    default String getEffectiveAvatarUrl()
    {
        String avatarUrl = getAvatarUrl();
        return avatarUrl == null ? getUser().getEffectiveAvatarUrl() : avatarUrl;
    }

    /**
     * The roles applied to this Member.
     * <br>The roles are ordered based on their position. The highest role being at index 0
     * and the lowest at the last index.
     *
     * <p>A Member's roles can be changed using the {@link Guild#addRoleToMember(Member, Role)}, {@link Guild#removeRoleFromMember(Member, Role)}, and {@link Guild#modifyMemberRoles(Member, Collection, Collection)}
     * methods in {@link net.dv8tion.jda.api.entities.Guild Guild}.
     *
     * <p><b>The Public Role ({@code @everyone}) is not included in the returned immutable list of roles
     * <br>It is implicit that every member holds the Public Role in a Guild thus it is not listed here!</b>
     *
     * @return An immutable List of {@link net.dv8tion.jda.api.entities.Role Roles} for this Member.
     *
     * @see    Guild#addRoleToMember(Member, Role)
     * @see    Guild#removeRoleFromMember(Member, Role)
     * @see    Guild#modifyMemberRoles(Member, Collection, Collection)
     */
    @Nonnull
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
    @Nullable
    Color getColor();

    /**
     * The raw RGB value for the color of this member.
     * <br>Defaulting to {@link net.dv8tion.jda.api.entities.Role#DEFAULT_COLOR_RAW Role.DEFAULT_COLOR_RAW}
     * if this member uses the default color (special property, it changes depending on theme used in the client)
     *
     * @return The raw RGB value or the role default
     */
    int getColorRaw();

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
     */
    boolean canInteract(@Nonnull Member member);

    /**
     * Whether this Member can interact with the provided {@link net.dv8tion.jda.api.entities.Role Role}
     * (kick/ban/move/modify/delete/etc.)
     *
     * <p>If this returns true this member can assign the role to other members.
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
     */
    boolean canInteract(@Nonnull Role role);

    /**
     * Whether this Member can interact with the provided {@link net.dv8tion.jda.api.entities.Emote Emote}
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
     */
    boolean canInteract(@Nonnull Emote emote);

    /**
     * Checks whether this member is the owner of its related {@link net.dv8tion.jda.api.entities.Guild Guild}.
     *
     * @return True, if this member is the owner of the attached Guild.
     */
    boolean isOwner();

    /**
     * Checks whether this member has passed the {@link net.dv8tion.jda.api.entities.Guild Guild's}
     * Membership Screening requirements.
     *
     * @incubating Discord is still trying to figure this out
     *
     * @return True, if this member hasn't passed the guild's Membership Screening requirements
     *
     * @since  4.2.1
     */
    @Incubating
    boolean isPending();

    /**
     * The default {@link net.dv8tion.jda.api.entities.BaseGuildMessageChannel BaseGuildMessageChannel} for a {@link net.dv8tion.jda.api.entities.Member Member}.
     * <br>This is the channel that the Discord client will default to opening when a Guild is opened for the first time
     * after joining the guild.
     * <br>The default channel is the channel with the highest position in which the member has
     * {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL} permissions. If this requirement doesn't apply for
     * any channel in the guild, this method returns {@code null}.
     *
     * @return The {@link net.dv8tion.jda.api.entities.BaseGuildMessageChannel BaseGuildMessageChannel} representing the default channel for this member
     *         or null if no such channel exists.
     */
    @Nullable
    BaseGuildMessageChannel getDefaultChannel();

    /**
     * Bans this Member and deletes messages sent by the user based on the amount of delDays.
     * <br>If you wish to ban a member without deleting any messages, provide delDays with a value of 0.
     *
     * <p>You can unban a user with {@link net.dv8tion.jda.api.entities.Guild#unban(User) Guild.unban(User)}.
     *
     * <p><b>Note:</b> {@link net.dv8tion.jda.api.entities.Guild#getMembers()} will still contain the
     * {@link net.dv8tion.jda.api.entities.Member Member} until Discord sends the
     * {@link net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent GuildMemberRemoveEvent}.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be banned due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  delDays
     *         The history of messages, in days, that will be deleted.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#BAN_MEMBERS} permission.
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     *         If the logged in account cannot ban the other user due to permission hierarchy position.
     *         <br>See {@link Member#canInteract(Member)}
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the provided amount of days (delDays) is less than 0.</li>
     *             <li>If the provided amount of days (delDays) is bigger than 7.</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     *
     * @since  4.0.0
     */
    @Nonnull
    @CheckReturnValue
    default AuditableRestAction<Void> ban(int delDays)
    {
        return getGuild().ban(this, delDays);
    }

    /**
     * Bans this Member and deletes messages sent by the user based on the amount of delDays.
     * <br>If you wish to ban a member without deleting any messages, provide delDays with a value of 0.
     *
     * <p>You can unban a user with {@link net.dv8tion.jda.api.entities.Guild#unban(User) Guild.unban(User)}.
     *
     * <p><b>Note:</b> {@link net.dv8tion.jda.api.entities.Guild#getMembers()} will still contain the
     * {@link net.dv8tion.jda.api.entities.Member Member} until Discord sends the
     * {@link net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent GuildMemberRemoveEvent}.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be banned due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  delDays
     *         The history of messages, in days, that will be deleted.
     * @param  reason
     *         The reason for this action or {@code null} if there is no specified reason
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#BAN_MEMBERS} permission.
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     *         If the logged in account cannot ban the other user due to permission hierarchy position.
     *         <br>See {@link Member#canInteract(Member)}
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the provided amount of days (delDays) is less than 0.</li>
     *             <li>If the provided amount of days (delDays) is bigger than 7.</li>
     *             <li>If the provided reason is longer than 512 characters.</li>
     *         </ul>
     *
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     *
     * @since  4.0.0
     */
    @Nonnull
    @CheckReturnValue
    default AuditableRestAction<Void> ban(int delDays, @Nullable String reason)
    {
        return getGuild().ban(this, delDays, reason);
    }

    /**
     * Kicks this Member from the {@link net.dv8tion.jda.api.entities.Guild Guild}.
     *
     * <p><b>Note:</b> {@link net.dv8tion.jda.api.entities.Guild#getMembers()} will still contain the {@link net.dv8tion.jda.api.entities.User User}
     * until Discord sends the {@link net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent GuildMemberRemoveEvent}.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be kicked due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#KICK_MEMBERS} permission.
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     *         If the logged in account cannot kick the other member due to permission hierarchy position.
     *         <br>See {@link Member#canInteract(Member)}
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     *         Kicks the provided Member from the current Guild
     *
     * @since  4.0.0
     */
    @Nonnull
    @CheckReturnValue
    default AuditableRestAction<Void> kick()
    {
        return getGuild().kick(this);
    }

    /**
     * Kicks this Member from the {@link net.dv8tion.jda.api.entities.Guild Guild}.
     *
     * <p><b>Note:</b> {@link net.dv8tion.jda.api.entities.Guild#getMembers()} will still contain the {@link net.dv8tion.jda.api.entities.Member Member}
     * until Discord sends the {@link net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent GuildMemberRemoveEvent}.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be kicked due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  reason
     *         The reason for this action or {@code null} if there is no specified reason
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#KICK_MEMBERS} permission.
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     *         If the logged in account cannot kick the other member due to permission hierarchy position.
     *         <br>See {@link Member#canInteract(Member)}
     * @throws java.lang.IllegalArgumentException
     *         If the provided reason is longer than 512 characters
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     *         Kicks the provided Member from the current Guild
     *
     * @since  4.0.0
     */
    @Nonnull
    @CheckReturnValue
    default AuditableRestAction<Void> kick(@Nullable String reason)
    {
        return getGuild().kick(this, reason);
    }

    /**
     * Puts this Member in time out in this {@link net.dv8tion.jda.api.entities.Guild Guild} for a specific amount of time.
     * <br>While a Member is in time out, all permissions except {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL VIEW_CHANNEL} and
     * {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY MESSAGE_HISTORY} are removed from them.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be put into time out due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  amount
     *         The amount of the provided {@link TimeUnit unit} to put this Member in time out for
     * @param  unit
     *         The {@link TimeUnit Unit} type of {@code amount}
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#MODERATE_MEMBERS} permission.
     * @throws IllegalArgumentException
     *         If any of the following checks are true
     *         <ul>
     *             <li>The provided {@code amount} is lower than or equal to {@code 0}</li>
     *             <li>The provided {@code unit} is null</li>
     *             <li>The provided {@code amount} with the {@code unit} results in a date that is more than {@value MAX_TIME_OUT_LENGTH} days in the future</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @CheckReturnValue
    default AuditableRestAction<Void> timeoutFor(long amount, @Nonnull TimeUnit unit)
    {
        return getGuild().timeoutFor(this, amount, unit);
    }

    /**
     * Puts this Member in time out in this {@link net.dv8tion.jda.api.entities.Guild Guild} for a specific amount of time.
     * <br>While a Member is in time out, all permissions except {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL VIEW_CHANNEL} and
     * {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY MESSAGE_HISTORY} are removed from them.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be put into time out due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  duration
     *         The duration to put this Member in time out for
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#MODERATE_MEMBERS} permission.
     * @throws IllegalArgumentException
     *         If any of the following checks are true
     *         <ul>
     *             <li>The provided {@code duration} is null</li>
     *             <li>The provided {@code duration} is not positive</li>
     *             <li>The provided {@code duration} results in a date that is more than {@value MAX_TIME_OUT_LENGTH} days in the future</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @CheckReturnValue
    default AuditableRestAction<Void> timeoutFor(@Nonnull Duration duration)
    {
        return getGuild().timeoutFor(this, duration);
    }

    /**
     * Puts this Member in time out in this {@link net.dv8tion.jda.api.entities.Guild Guild} until the specified date.
     * <br>While a Member is in time out, all permissions except {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL VIEW_CHANNEL} and
     * {@link net.dv8tion.jda.api.Permission#MESSAGE_HISTORY MESSAGE_HISTORY} are removed from them.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be put into time out due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  temporal
     *         The time this Member will be released from time out, or null to remove the time out
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#MODERATE_MEMBERS} permission.
     * @throws IllegalArgumentException
     *         If any of the following checks are true
     *         <ul>
     *             <li>The provided {@code temporal} is in the past</li>
     *             <li>The provided {@code temporal} is more than {@value MAX_TIME_OUT_LENGTH} days in the future</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @CheckReturnValue
    default AuditableRestAction<Void> timeoutUntil(@Nullable TemporalAccessor temporal)
    {
        return getGuild().timeoutUntil(this, temporal);
    }

    /**
     * Removes a time out from this Member in this {@link net.dv8tion.jda.api.entities.Guild Guild}.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The time out cannot be removed due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#MODERATE_MEMBERS} permission.
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @CheckReturnValue
    default AuditableRestAction<Void> removeTimeout()
    {
        return getGuild().removeTimeout(this);
    }

    /**
     * Sets the Guild Muted state state of this Member based on the provided
     * boolean.
     *
     * <p><b>Note:</b> The Member's {@link net.dv8tion.jda.api.entities.GuildVoiceState#isGuildMuted() GuildVoiceState.isGuildMuted()} value won't change
     * until JDA receives the {@link net.dv8tion.jda.api.events.guild.voice.GuildVoiceGuildMuteEvent GuildVoiceGuildMuteEvent} event related to this change.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be muted due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#USER_NOT_CONNECTED USER_NOT_CONNECTED}
     *     <br>The specified Member is not connected to a voice channel</li>
     * </ul>
     *
     * @param  mute
     *         Whether this {@link net.dv8tion.jda.api.entities.Member Member} should be muted or unmuted.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#VOICE_DEAF_OTHERS} permission.
     * @throws java.lang.IllegalStateException
     *         If the member is not currently connected to a voice channel.
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     *
     * @since  4.0.0
     */
    @Nonnull
    @CheckReturnValue
    default AuditableRestAction<Void> mute(boolean mute)
    {
        return getGuild().mute(this, mute);
    }

    /**
     * Sets the Guild Deafened state state of this Member based on the provided boolean.
     *
     * <p><b>Note:</b> The Member's {@link net.dv8tion.jda.api.entities.GuildVoiceState#isGuildDeafened() GuildVoiceState.isGuildDeafened()} value won't change
     * until JDA receives the {@link net.dv8tion.jda.api.events.guild.voice.GuildVoiceGuildDeafenEvent GuildVoiceGuildDeafenEvent} event related to this change.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be deafened due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#USER_NOT_CONNECTED USER_NOT_CONNECTED}
     *     <br>The specified Member is not connected to a voice channel</li>
     * </ul>
     *
     * @param  deafen
     *         Whether this {@link net.dv8tion.jda.api.entities.Member Member} should be deafened or undeafened.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#VOICE_DEAF_OTHERS} permission.
     * @throws java.lang.IllegalStateException
     *         If the member is not currently connected to a voice channel.
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     *
     * @since  4.0.0
     */
    @Nonnull
    @CheckReturnValue
    default AuditableRestAction<Void> deafen(boolean deafen)
    {
        return getGuild().deafen(this, deafen);
    }

    /**
     * Changes this Member's nickname in this guild.
     * The nickname is visible to all members of this guild.
     *
     * <p>To change the nickname for the currently logged in account
     * only the Permission {@link net.dv8tion.jda.api.Permission#NICKNAME_CHANGE NICKNAME_CHANGE} is required.
     * <br>To change the nickname of <b>any</b> {@link net.dv8tion.jda.api.entities.Member Member} for this {@link net.dv8tion.jda.api.entities.Guild Guild}
     * the Permission {@link net.dv8tion.jda.api.Permission#NICKNAME_MANAGE NICKNAME_MANAGE} is required.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The nickname of the target Member is not modifiable due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  nickname
     *         The new nickname of the {@link net.dv8tion.jda.api.entities.Member Member}, provide {@code null} or an
     *         empty String to reset the nickname
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         <ul>
     *             <li>If attempting to set nickname for self and the logged in account has neither {@link net.dv8tion.jda.api.Permission#NICKNAME_CHANGE}
     *                 or {@link net.dv8tion.jda.api.Permission#NICKNAME_MANAGE}</li>
     *             <li>If attempting to set nickname for another member and the logged in account does not have {@link net.dv8tion.jda.api.Permission#NICKNAME_MANAGE}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     *         If attempting to set nickname for another member and the logged in account cannot manipulate the other user due to permission hierarchy position.
     *         <br>See {@link #canInteract(Member)}.
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     *
     * @since  4.0.0
     */
    @Nonnull
    @CheckReturnValue
    default AuditableRestAction<Void> modifyNickname(@Nullable String nickname)
    {
        return getGuild().modifyNickname(this, nickname);
    }
}
