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
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.unions.DefaultGuildChannelUnion;
import net.dv8tion.jda.api.entities.detached.IDetachableEntity;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.utils.ImageProxy;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.requests.restaction.AuditableRestActionImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;
import org.jetbrains.annotations.Unmodifiable;

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
 * @see   Guild#getMember(UserSnowflake)
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
public interface Member extends IMentionable, IPermissionHolder, IDetachableEntity, UserSnowflake
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
     * <br>While a Member is in time out, all permissions except {@link Permission#VIEW_CHANNEL VIEW_CHANNEL} and
     * {@link Permission#MESSAGE_HISTORY MESSAGE_HISTORY} are removed from them.
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
    @Unmodifiable
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
     * @return The guild nickname of this Member or the {@link User#getEffectiveName() effective user name} if no guild nickname is present.
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
     * Returns an {@link ImageProxy} for this member's avatar.
     *
     * @return Possibly-null {@link ImageProxy} of this member's avatar
     *
     * @see    #getAvatarUrl()
     */
    @Nullable
    default ImageProxy getAvatar()
    {
        final String avatarUrl = getAvatarUrl();
        return avatarUrl == null ? null : new ImageProxy(avatarUrl);
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
     * Returns an {@link ImageProxy} for this member's effective avatar image.
     *
     * @return Never-null {@link ImageProxy} of this member's effective avatar image
     *
     * @see    #getEffectiveAvatarUrl()
     */
    @Nonnull
    default ImageProxy getEffectiveAvatar()
    {
        final ImageProxy avatar = getAvatar();
        return avatar == null ? getUser().getEffectiveAvatar() : avatar;
    }

    /**
     * The roles applied to this Member.
     * <br>The roles are ordered based on their position. The highest role being at index 0
     * and the lowest at the last index.
     *
     * <p>A Member's roles can be changed using the {@link Guild#addRoleToMember(UserSnowflake, Role)}, {@link Guild#removeRoleFromMember(UserSnowflake, Role)}, and {@link Guild#modifyMemberRoles(Member, Collection, Collection)}
     * methods in {@link net.dv8tion.jda.api.entities.Guild Guild}.
     *
     * <p><b>The Public Role ({@code @everyone}) is not included in the returned immutable list of roles
     * <br>It is implicit that every member holds the Public Role in a Guild thus it is not listed here!</b>
     *
     * @return An immutable List of {@link net.dv8tion.jda.api.entities.Role Roles} for this Member.
     *
     * @see    Guild#addRoleToMember(UserSnowflake, Role)
     * @see    Guild#removeRoleFromMember(UserSnowflake, Role)
     * @see    Guild#modifyMemberRoles(Member, Collection, Collection)
     */
    @Nonnull
    @Unmodifiable
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
     * The raw {@link MemberFlag flags} bitset for this member.
     *
     * @return The raw flag bitset
     */
    int getFlagsRaw();

    /**
     * The {@link MemberFlag flags} for this member as an {@link EnumSet}.
     * <br>Modifying this set will not update the member, it is a copy of existing flags.
     *
     * @return The flags
     */
    @Nonnull
    default EnumSet<MemberFlag> getFlags()
    {
        return MemberFlag.fromRaw(getFlagsRaw());
    }

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
     * Whether this Member can interact with the provided {@link RichCustomEmoji}
     * (use in a message)
     *
     * @param  emoji
     *         The target emoji to check
     *
     * @throws NullPointerException
     *         if the specified emoji is null
     * @throws IllegalArgumentException
     *         if the specified emoji is not from the same guild
     *
     * @return True, if this Member is able to interact with the specified emoji
     */
    boolean canInteract(@Nonnull RichCustomEmoji emoji);

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
     * The {@link DefaultGuildChannelUnion default channel} for a {@link net.dv8tion.jda.api.entities.Member Member}.
     * <br>This is the channel that the Discord client will default to opening when a Guild is opened for the first time
     * after joining the guild.
     * <br>The default channel is the channel with the highest position in which the member has
     * {@link Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL} permissions. If this requirement doesn't apply for
     * any channel in the guild, this method returns {@code null}.
     *
     * @return The {@link DefaultGuildChannelUnion channel} representing the default channel for this member
     *         or null if no such channel exists.
     */
    @Nullable
    DefaultGuildChannelUnion getDefaultChannel();

    /**
     * Bans this Member and deletes messages sent by the user based on the amount of delDays.
     * <br>If you wish to ban a user without deleting any messages, provide {@code deletionTimeframe} with a value of 0.
     * To set a ban reason, use {@link AuditableRestAction#reason(String)}.
     *
     * <p>You can unban a user with {@link net.dv8tion.jda.api.entities.Guild#unban(UserSnowflake) Guild.unban(UserSnowflake)}.
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
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_USER UNKNOWN_USER}
     *     <br>The user no longer exists</li>
     * </ul>
     *
     * @param  deletionTimeframe
     *         The timeframe for the history of messages that will be deleted. (seconds precision)
     * @param  unit
     *         Timeframe unit as a {@link TimeUnit} (for example {@code member.ban(7, TimeUnit.DAYS)}).
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link Permission#BAN_MEMBERS} permission.
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     *         If the logged in account cannot ban the other user due to permission hierarchy position.
     *         <br>See {@link Member#canInteract(Member)}
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the provided deletionTimeframe is negative.</li>
     *             <li>If the provided deletionTimeframe is longer than 7 days.</li>
     *             <li>If the provided time unit is {@code null}</li>
     *         </ul>
     *
     * @return {@link AuditableRestAction}
     *
     * @see    Guild#ban(UserSnowflake, int, TimeUnit)
     * @see    AuditableRestAction#reason(String)
     */
    @Nonnull
    @CheckReturnValue
    default AuditableRestAction<Void> ban(int deletionTimeframe, @Nonnull TimeUnit unit)
    {
        return getGuild().ban(this, deletionTimeframe, unit);
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
     *         If the logged in account does not have the {@link Permission#KICK_MEMBERS} permission.
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
     * Puts this Member in time out in this {@link net.dv8tion.jda.api.entities.Guild Guild} for a specific amount of time.
     * <br>While a Member is in time out, all permissions except {@link Permission#VIEW_CHANNEL VIEW_CHANNEL} and
     * {@link Permission#MESSAGE_HISTORY MESSAGE_HISTORY} are removed from them.
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
     *         If the logged in account does not have the {@link Permission#MODERATE_MEMBERS} permission.
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
     * <br>While a Member is in time out, all permissions except {@link Permission#VIEW_CHANNEL VIEW_CHANNEL} and
     * {@link Permission#MESSAGE_HISTORY MESSAGE_HISTORY} are removed from them.
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
     *         If the logged in account does not have the {@link Permission#MODERATE_MEMBERS} permission.
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
     * <br>While a Member is in time out, all permissions except {@link Permission#VIEW_CHANNEL VIEW_CHANNEL} and
     * {@link Permission#MESSAGE_HISTORY MESSAGE_HISTORY} are removed from them.
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
     *         The time this Member will be released from time out
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link Permission#MODERATE_MEMBERS} permission.
     * @throws IllegalArgumentException
     *         If any of the following checks are true
     *         <ul>
     *             <li>The provided {@code temporal} is null</li>
     *             <li>The provided {@code temporal} is in the past</li>
     *             <li>The provided {@code temporal} is more than {@value MAX_TIME_OUT_LENGTH} days in the future</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @CheckReturnValue
    default AuditableRestAction<Void> timeoutUntil(@Nonnull TemporalAccessor temporal)
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
     *         If the logged in account does not have the {@link Permission#MODERATE_MEMBERS} permission.
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
     *         If the logged in account does not have the {@link Permission#VOICE_DEAF_OTHERS} permission.
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
     *         If the logged in account does not have the {@link Permission#VOICE_DEAF_OTHERS} permission.
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
     * only the Permission {@link Permission#NICKNAME_CHANGE NICKNAME_CHANGE} is required.
     * <br>To change the nickname of <b>any</b> {@link net.dv8tion.jda.api.entities.Member Member} for this {@link net.dv8tion.jda.api.entities.Guild Guild}
     * the Permission {@link Permission#NICKNAME_MANAGE NICKNAME_MANAGE} is required.
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
     *             <li>If attempting to set nickname for self and the logged in account has neither {@link Permission#NICKNAME_CHANGE}
     *                 or {@link Permission#NICKNAME_MANAGE}</li>
     *             <li>If attempting to set nickname for another member and the logged in account does not have {@link Permission#NICKNAME_MANAGE}</li>
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

    /**
     * Updates the flags to the new flag set.
     * <br>If any of the flags is not {@link MemberFlag#isModifiable() modifiable}, it is not updated.
     *
     * <p>Any flags not provided by the set will be disabled, all contained flags will be enabled.
     *
     * @param  newFlags
     *         The new flags for the member.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the bot does not have {@link Permission#MODERATE_MEMBERS} in the guild
     * @throws IllegalArgumentException
     *         If {@code null} is provided
     *
     * @return {@link AuditableRestAction}
     */
    @Nonnull
    @CheckReturnValue
    default AuditableRestAction<Void> modifyFlags(@Nonnull Collection<MemberFlag> newFlags)
    {
        Checks.noneNull(newFlags, "Flags");
        if (!getGuild().getSelfMember().hasPermission(Permission.MODERATE_MEMBERS))
            throw new InsufficientPermissionException(getGuild(), Permission.MODERATE_MEMBERS);
        int flags = getFlagsRaw();
        EnumSet<MemberFlag> updated = Helpers.copyEnumSet(MemberFlag.class, newFlags);
        for (MemberFlag flag : MemberFlag.values())
        {
            if (flag.modifiable)
            {
                if (updated.contains(flag))
                    flags |= flag.raw;
                else
                    flags &= ~flag.raw;
            }
        }

        DataObject body = DataObject.empty().put("flags", flags);
        Route.CompiledRoute route = Route.Guilds.MODIFY_MEMBER.compile(getGuild().getId(), getId());
        return new AuditableRestActionImpl<>(getJDA(), route, body);
    }

    /**
     * Member flags indicating information about the membership state.
     */
    enum MemberFlag
    {
        /**
         * The Member has left and rejoined the guild
         */
        DID_REJOIN(1, false),
        /**
         * The Member has completed the onboarding process
         */
        COMPLETED_ONBOARDING(1 << 1, false),
        /**
         * The Member bypasses guild verification requirements
         */
        BYPASSES_VERIFICATION(1 << 2, true),
        /**
         * The Member has started the onboarding process
         */
        STARTED_ONBOARDING(1 << 3, false),
        ;

        private final int raw;
        private final boolean modifiable;


        MemberFlag(int raw, boolean modifiable)
        {
            this.raw = raw;
            this.modifiable = modifiable;
        }

        /**
         * The raw value used by Discord for this flag
         *
         * @return The raw value
         */
        public int getRaw()
        {
            return raw;
        }

        /**
         * Whether this flag can be modified by the client
         *
         * @return True, if this flag can be modified
         */
        public boolean isModifiable()
        {
            return modifiable;
        }

        /**
         * The {@link MemberFlag Flags} represented by the provided raw value.
         * <br>If the provided raw value is {@code 0} this will return an empty {@link java.util.EnumSet EnumSet}.
         *
         * @param  raw
         *         The raw value
         *
         * @return EnumSet containing the flags represented by the provided raw value
         */
        @Nonnull
        public static EnumSet<MemberFlag> fromRaw(int raw)
        {
            EnumSet<MemberFlag> flags = EnumSet.noneOf(MemberFlag.class);
            for (MemberFlag flag : values())
            {
                if ((raw & flag.raw) == flag.raw)
                    flags.add(flag);
            }
            return flags;
        }

        /**
         * The raw value of the provided {@link MemberFlag Flags}.
         * <br>If the provided set is empty this will return {@code 0}.
         *
         * @param  flags
         *         The flags
         *
         * @return The raw value of the provided flags
         */
        public static int toRaw(@Nonnull Collection<MemberFlag> flags)
        {
            Checks.noneNull(flags, "Flags");
            int raw = 0;
            for (MemberFlag flag : flags)
                raw |= flag.raw;
            return raw;
        }
    }
}
