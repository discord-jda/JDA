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

package net.dv8tion.jda.core.managers;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.entities.impl.EmoteImpl;
import net.dv8tion.jda.core.entities.impl.GuildImpl;
import net.dv8tion.jda.core.entities.impl.MemberImpl;
import net.dv8tion.jda.core.exceptions.GuildUnavailableException;
import net.dv8tion.jda.core.exceptions.HierarchyException;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.core.requests.restaction.ChannelAction;
import net.dv8tion.jda.core.requests.restaction.RoleAction;
import net.dv8tion.jda.core.requests.restaction.order.CategoryOrderAction;
import net.dv8tion.jda.core.requests.restaction.order.ChannelOrderAction;
import net.dv8tion.jda.core.requests.restaction.order.RoleOrderAction;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.Helpers;
import net.dv8tion.jda.core.utils.MiscUtil;
import net.dv8tion.jda.core.utils.PermissionUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.CheckReturnValue;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A controller that allows to utilize moderation
 * permissions and create new channels and roles.
 *
 * @since 3.0
 */
public class GuildController
{
    protected final GuildImpl guild;

    /**
     * Creates a new GuildController instance
     * for the specified Guild instance
     *
     * @param guild
     *        The {@link net.dv8tion.jda.core.entities.Guild Guild}
     *        that will be modified
     */
    public GuildController(Guild guild)
    {
        this.guild = (GuildImpl) guild;
    }

    /**
     * The underlying {@link net.dv8tion.jda.core.entities.Guild Guild} instance
     *
     * @return The underlying {@link net.dv8tion.jda.core.entities.Guild Guild} instance
     */
    public Guild getGuild()
    {
        return guild;
    }

    /**
     * The {@link net.dv8tion.jda.core.JDA JDA} instance of this GuildController
     *
     * @return the corresponding JDA instance
     */
    public JDA getJDA()
    {
        return guild.getJDA();
    }

    /**
     * Used to move a {@link net.dv8tion.jda.core.entities.Member Member} from one {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}
     * to another {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}.
     * <br>As a note, you cannot move a Member that isn't already in a VoiceChannel. Also they must be in a VoiceChannel
     * in the same Guild as the one that you are moving them to.
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be moved due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The specified channel was deleted before finishing the task</li>
     * </ul>
     *
     * @param  member
     *         The {@link net.dv8tion.jda.core.entities.Member Member} that you are moving.
     * @param  voiceChannel
     *         The destination {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} to which the member is being
     *         moved to.
     *
     * @throws IllegalStateException
     *         If the Member isn't currently in a VoiceChannel in this Guild.
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If any of the provided arguments is {@code null}</li>
     *             <li>If the provided Member isn't part of this {@link net.dv8tion.jda.core.entities.Guild Guild}</li>
     *             <li>If the provided VoiceChannel isn't part of this {@link net.dv8tion.jda.core.entities.Guild Guild}</li>
     *         </ul>
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         <ul>
     *             <li>If this account doesn't have {@link net.dv8tion.jda.core.Permission#VOICE_MOVE_OTHERS}
     *                 in the VoiceChannel that the Member is currently in.</li>
     *             <li>If this account <b>AND</b> the Member being moved don't have
     *                 {@link net.dv8tion.jda.core.Permission#VOICE_CONNECT} for the destination VoiceChannel.</li>
     *         </ul>
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *         If the guild is temporarily not {@link net.dv8tion.jda.core.entities.Guild#isAvailable() available}
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction}
     */
    @CheckReturnValue
    public RestAction<Void> moveVoiceMember(Member member, VoiceChannel voiceChannel)
    {
        checkAvailable();
        Checks.notNull(member, "Member");
        Checks.notNull(voiceChannel, "VoiceChannel");
        checkGuild(member.getGuild(), "Member");
        checkGuild(voiceChannel.getGuild(), "VoiceChannel");

        GuildVoiceState vState = member.getVoiceState();
        if (!vState.inVoiceChannel())
            throw new IllegalStateException("You cannot move a Member who isn't in a VoiceChannel!");

        if (!PermissionUtil.checkPermission(vState.getChannel(), guild.getSelfMember(), Permission.VOICE_MOVE_OTHERS))
            throw new InsufficientPermissionException(Permission.VOICE_MOVE_OTHERS, "This account does not have Permission to MOVE_OTHERS out of the channel that the Member is currently in.");

        if (!PermissionUtil.checkPermission(voiceChannel, guild.getSelfMember(), Permission.VOICE_CONNECT)
                && !PermissionUtil.checkPermission(voiceChannel, member, Permission.VOICE_CONNECT))
            throw new InsufficientPermissionException(Permission.VOICE_CONNECT,
                    "Neither this account nor the Member that is attempting to be moved have the VOICE_CONNECT permission " +
                            "for the destination VoiceChannel, so the move cannot be done.");

        JSONObject body = new JSONObject().put("channel_id", voiceChannel.getId());
        Route.CompiledRoute route = Route.Guilds.MODIFY_MEMBER.compile(guild.getId(), member.getUser().getId());

        return new RestAction<Void>(guild.getJDA(), route, body)
        {
            @Override
            protected void handleResponse(Response response, Request<Void> request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    /**
     * Changes a Member's nickname in this guild.
     * The nickname is visible to all members of this guild.
     *
     * <p>To change the nickname for the currently logged in account
     * only the Permission {@link net.dv8tion.jda.core.Permission#NICKNAME_CHANGE NICKNAME_CHANGE} is required.
     * <br>To change the nickname of <b>any</b> {@link net.dv8tion.jda.core.entities.Member Member} for this {@link net.dv8tion.jda.core.entities.Guild Guild}
     * the Permission {@link net.dv8tion.jda.core.Permission#NICKNAME_MANAGE NICKNAME_MANAGE} is required.
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The nickname of the target Member is not modifiable due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  member
     *         The {@link net.dv8tion.jda.core.entities.Member Member} for which the nickname should be changed.
     * @param  nickname
     *         The new nickname of the {@link net.dv8tion.jda.core.entities.Member Member}, provide {@code null} or an
     *         empty String to reset the nickname
     *
     * @throws IllegalArgumentException
     *         If the specified {@link net.dv8tion.jda.core.entities.Member Member}
     *         is not from the same {@link net.dv8tion.jda.core.entities.Guild Guild}.
     *         Or if the provided member is {@code null}
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         <ul>
     *             <li>If attempting to set nickname for self and the logged in account has neither {@link net.dv8tion.jda.core.Permission#NICKNAME_CHANGE}
     *                 or {@link net.dv8tion.jda.core.Permission#NICKNAME_MANAGE}</li>
     *             <li>If attempting to set nickname for another member and the logged in account does not have {@link net.dv8tion.jda.core.Permission#NICKNAME_MANAGE}</li>
     *         </ul>
     * @throws net.dv8tion.jda.core.exceptions.HierarchyException
     *         If attempting to set nickname for another member and the logged in account cannot manipulate the other user due to permission hierarchy position.
     *         <br>See {@link net.dv8tion.jda.core.utils.PermissionUtil#canInteract(Member, Member) PermissionUtil.canInteract(Member, Member)}
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *         If the guild is temporarily not {@link net.dv8tion.jda.core.entities.Guild#isAvailable() available}
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @CheckReturnValue
    public AuditableRestAction<Void> setNickname(Member member, String nickname)
    {
        checkAvailable();
        Checks.notNull(member, "Member");
        checkGuild(member.getGuild(), "Member");

        if(member.equals(guild.getSelfMember()))
        {
            if(!member.hasPermission(Permission.NICKNAME_CHANGE)
                    && !member.hasPermission(Permission.NICKNAME_MANAGE))
                throw new InsufficientPermissionException(Permission.NICKNAME_CHANGE, "You neither have NICKNAME_CHANGE nor NICKNAME_MANAGE permission!");
        }
        else
        {
            checkPermission(Permission.NICKNAME_MANAGE);
            checkPosition(member);
        }

        if (Objects.equals(nickname, member.getNickname()))
            return new AuditableRestAction.EmptyRestAction<>(getJDA(), null);

        if (nickname == null)
            nickname = "";

        JSONObject body = new JSONObject().put("nick", nickname);

        Route.CompiledRoute route;
        if (member.equals(guild.getSelfMember()))
            route = Route.Guilds.MODIFY_SELF_NICK.compile(guild.getId());
        else
            route = Route.Guilds.MODIFY_MEMBER.compile(guild.getId(), member.getUser().getId());

        return new AuditableRestAction<Void>(guild.getJDA(), route, body)
        {
            @Override
            protected void handleResponse(Response response, Request<Void> request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    /**
     * This method will prune (kick) all members who were offline for at least <i>days</i> days.
     * <br>The RestAction returned from this method will return the amount of Members that were pruned.
     * <br>You can use {@link Guild#getPrunableMemberCount(int)} to determine how many Members would be pruned if you were to
     * call this method.
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The prune cannot finished due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  days
     *         Minimum number of days since a member has been offline to get affected.
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the account doesn't have {@link net.dv8tion.jda.core.Permission#KICK_MEMBERS KICK_MEMBER} Permission.
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *         If the guild is temporarily not {@link net.dv8tion.jda.core.entities.Guild#isAvailable() available}
     * @throws IllegalArgumentException
     *         If the provided days are less than {@code 1}
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction} - Type: Integer
     *         <br>The amount of Members that were pruned from the Guild.
     */
    @CheckReturnValue
    public AuditableRestAction<Integer> prune(int days)
    {
        checkAvailable();
        checkPermission(Permission.KICK_MEMBERS);

        Checks.check(days >= 1, "Days amount must be at minimum 1 day.");

        Route.CompiledRoute route = Route.Guilds.PRUNE_MEMBERS.compile(guild.getId()).withQueryParams("days", Integer.toString(days));
        return new AuditableRestAction<Integer>(guild.getJDA(), route)
        {
            @Override
            protected void handleResponse(Response response, Request<Integer> request)
            {
                if (response.isOk())
                    request.onSuccess(response.getObject().getInt("pruned"));
                else
                    request .onFailure(response);
            }
        };
    }

    /**
     * Kicks a {@link net.dv8tion.jda.core.entities.Member Member} from the {@link net.dv8tion.jda.core.entities.Guild Guild}.
     *
     * <p><b>Note:</b> {@link net.dv8tion.jda.core.entities.Guild#getMembers()} will still contain the {@link net.dv8tion.jda.core.entities.User User}
     * until Discord sends the {@link net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent GuildMemberLeaveEvent}.
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be kicked due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  member
     *         The {@link net.dv8tion.jda.core.entities.Member Member} to kick
     *         from the from the {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * @param  reason
     *         The reason for this action or {@code null} if there is no specified reason
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided member is not a Member of this Guild or is {@code null}
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *         If the guild is temporarily not {@link net.dv8tion.jda.core.entities.Guild#isAvailable() available}
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.core.Permission#KICK_MEMBERS} permission.
     * @throws net.dv8tion.jda.core.exceptions.HierarchyException
     *         If the logged in account cannot kick the other member due to permission hierarchy position.
     *         <br>See {@link net.dv8tion.jda.core.utils.PermissionUtil#canInteract(Member, Member) PermissionUtil.canInteract(Member, Member)}
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     *         Kicks the provided Member from the current Guild
     */
    @CheckReturnValue
    public AuditableRestAction<Void> kick(Member member, String reason)
    {
        checkAvailable();
        Checks.notNull(member, "member");
        checkGuild(member.getGuild(), "member");
        checkPermission(Permission.KICK_MEMBERS);
        checkPosition(member);

        final String userId = member.getUser().getId();
        final String guildId = guild.getId();

        Route.CompiledRoute route = Route.Guilds.KICK_MEMBER.compile(guildId, userId);
        if (reason != null && !reason.isEmpty())
            route = route.withQueryParams("reason", MiscUtil.encodeUTF8(reason));

        return new AuditableRestAction<Void>(guild.getJDA(), route)
        {
            @Override
            protected void handleResponse(Response response, Request<Void> request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    /**
     * Kicks the {@link net.dv8tion.jda.core.entities.Member Member} specified by the userId from the from the {@link net.dv8tion.jda.core.entities.Guild Guild}.
     *
     * <p><b>Note:</b> {@link net.dv8tion.jda.core.entities.Guild#getMembers()} will still contain the {@link net.dv8tion.jda.core.entities.User User}
     * until Discord sends the {@link net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent GuildMemberLeaveEvent}.
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be kicked due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  userId
     *         The id of the {@link net.dv8tion.jda.core.entities.User User} to kick
     *         from the from the {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * @param  reason
     *         The reason for this action or {@code null} if there is no specified reason
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.core.Permission#KICK_MEMBERS} permission.
     * @throws net.dv8tion.jda.core.exceptions.HierarchyException
     *         If the logged in account cannot kick the other member due to permission hierarchy position.
     *         <br>See {@link net.dv8tion.jda.core.utils.PermissionUtil#canInteract(Member, Member) PermissionUtil.canInteract(Member, Member)}
     * @throws java.lang.IllegalArgumentException
     *         If the userId provided does not correspond to a Member in this Guild or the provided {@code userId} is blank/null.
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *         If the guild is temporarily not {@link net.dv8tion.jda.core.entities.Guild#isAvailable() available}
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @CheckReturnValue
    public AuditableRestAction<Void> kick(String userId, String reason)
    {
        Member member = guild.getMemberById(userId);
        Checks.check(member != null, "The provided userId does not correspond to a member in this guild! Provided userId: %s", userId);

        return kick(member, reason);
    }

    /**
     * Kicks a {@link net.dv8tion.jda.core.entities.Member Member} from the {@link net.dv8tion.jda.core.entities.Guild Guild}.
     *
     * <p><b>Note:</b> {@link net.dv8tion.jda.core.entities.Guild#getMembers()} will still contain the {@link net.dv8tion.jda.core.entities.User User}
     * until Discord sends the {@link net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent GuildMemberLeaveEvent}.
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be kicked due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  member
     *         The {@link net.dv8tion.jda.core.entities.Member Member} to kick from the from the {@link net.dv8tion.jda.core.entities.Guild Guild}.
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided member is not a Member of this Guild or is {@code null}
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *         If the guild is temporarily not {@link net.dv8tion.jda.core.entities.Guild#isAvailable() available}
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.core.Permission#KICK_MEMBERS} permission.
     * @throws net.dv8tion.jda.core.exceptions.HierarchyException
     *         If the logged in account cannot kick the other member due to permission hierarchy position.
     *         <br>See {@link net.dv8tion.jda.core.utils.PermissionUtil#canInteract(Member, Member) PermissionUtil.canInteract(Member, Member)}
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     *         Kicks the provided Member from the current Guild
     */
    @CheckReturnValue
    public AuditableRestAction<Void> kick(Member member)
    {
        return kick(member, null);
    }

    /**
     * Kicks the {@link net.dv8tion.jda.core.entities.Member Member} specified by the userId from the from the {@link net.dv8tion.jda.core.entities.Guild Guild}.
     *
     * <p><b>Note:</b> {@link net.dv8tion.jda.core.entities.Guild#getMembers()} will still contain the {@link net.dv8tion.jda.core.entities.User User}
     * until Discord sends the {@link net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent GuildMemberLeaveEvent}.
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be kicked due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  userId
     *         The id of the {@link net.dv8tion.jda.core.entities.User User} to kick from the from the {@link net.dv8tion.jda.core.entities.Guild Guild}.
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.core.Permission#KICK_MEMBERS} permission.
     * @throws net.dv8tion.jda.core.exceptions.HierarchyException
     *         If the logged in account cannot kick the other member due to permission hierarchy position.
     *         <br>See {@link net.dv8tion.jda.core.utils.PermissionUtil#canInteract(Member, Member) PermissionUtil.canInteract(Member, Member)}
     * @throws java.lang.IllegalArgumentException
     *         If the userId provided does not correspond to a Member in this Guild or the provided {@code userId} is blank/null.
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *         If the guild is temporarily not {@link net.dv8tion.jda.core.entities.Guild#isAvailable() available}
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @CheckReturnValue
    public AuditableRestAction<Void> kick(String userId)
    {
        return kick(userId, null);
    }

    /**
     * Bans a {@link net.dv8tion.jda.core.entities.Member Member} and deletes messages sent by the user
     * based on the amount of delDays.
     * <br>If you wish to ban a member without deleting any messages, provide delDays with a value of 0.
     * This change will be applied immediately.
     *
     * <p><b>Note:</b> {@link net.dv8tion.jda.core.entities.Guild#getMembers()} will still contain the
     * {@link net.dv8tion.jda.core.entities.Member Member} until Discord sends the
     * {@link net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent GuildMemberLeaveEvent}.
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be banned due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  member
     *         The {@link net.dv8tion.jda.core.entities.Member Member} to ban.
     * @param  delDays
     *         The history of messages, in days, that will be deleted.
     * @param  reason
     *         The reason for this action or {@code null} if there is no specified reason
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.core.Permission#BAN_MEMBERS} permission.
     * @throws net.dv8tion.jda.core.exceptions.HierarchyException
     *         If the logged in account cannot ban the other user due to permission hierarchy position.
     *         <br>See {@link net.dv8tion.jda.core.utils.PermissionUtil#canInteract(Member, Member) PermissionUtil.canInteract(Member, Member)}
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the provided amount of days (delDays) is less than 0.</li>
     *             <li>If the provided member is {@code null}</li>
     *         </ul>
     *
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *         If the guild is temporarily not {@link net.dv8tion.jda.core.entities.Guild#isAvailable() available}
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @CheckReturnValue
    public AuditableRestAction<Void> ban(Member member, int delDays, String reason)
    {
        checkAvailable();
        Checks.notNull(member, "Member");
        //Don't check if the provided member is from this guild. It doesn't matter if they are or aren't.

        return ban(member.getUser(), delDays, reason);
    }

    /**
     * Bans a {@link net.dv8tion.jda.core.entities.User User} and deletes messages sent by the user
     * based on the amount of delDays.
     * <br>If you wish to ban a user without deleting any messages, provide delDays with a value of 0.
     * This change will be applied immediately.
     *
     * <p><b>Note:</b> {@link net.dv8tion.jda.core.entities.Guild#getMembers()} will still contain the {@link net.dv8tion.jda.core.entities.User User's}
     * {@link net.dv8tion.jda.core.entities.Member Member} object (if the User was in the Guild)
     * until Discord sends the {@link net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent GuildMemberLeaveEvent}.
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be banned due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  user
     *         The {@link net.dv8tion.jda.core.entities.User User} to ban.
     * @param  delDays
     *         The history of messages, in days, that will be deleted.
     * @param  reason
     *         The reason for this action or {@code null} if there is no specified reason
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.core.Permission#BAN_MEMBERS} permission.
     * @throws net.dv8tion.jda.core.exceptions.HierarchyException
     *         If the logged in account cannot ban the other user due to permission hierarchy position.
     *         <br>See {@link net.dv8tion.jda.core.utils.PermissionUtil#canInteract(Member, Member) PermissionUtil.canInteract(Member, Member)}
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the provided amount of days (delDays) is less than 0.</li>
     *             <li>If the provided user is null</li>
     *         </ul>
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *         If the guild is temporarily not {@link net.dv8tion.jda.core.entities.Guild#isAvailable() available}
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @CheckReturnValue
    public AuditableRestAction<Void> ban(User user, int delDays, String reason)
    {
        checkAvailable();
        Checks.notNull(user, "User");
        checkPermission(Permission.BAN_MEMBERS);

        if (guild.isMember(user)) // If user is in guild. Check if we are able to ban.
            checkPosition(guild.getMember(user));

        Checks.notNegative(delDays, "Deletion Days");

        final String userId = user.getId();

        Route.CompiledRoute route = Route.Guilds.BAN.compile(guild.getId(), userId);
        if (reason != null && !reason.isEmpty())
            route = route.withQueryParams("reason", MiscUtil.encodeUTF8(reason));
        if (delDays > 0)
            route = route.withQueryParams("delete-message-days", Integer.toString(delDays));

        return new AuditableRestAction<Void>(guild.getJDA(), route)
        {
            @Override
            protected void handleResponse(Response response, Request<Void> request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    /**
     * Bans the a user specified by the userId and deletes messages sent by the user
     * based on the amount of delDays.
     * <br>If you wish to ban a user without deleting any messages, provide delDays with a value of 0.
     * This change will be applied immediately.
     *
     * <p><b>Note:</b> {@link net.dv8tion.jda.core.entities.Guild#getMembers()} will still contain the {@link net.dv8tion.jda.core.entities.User User's}
     * {@link net.dv8tion.jda.core.entities.Member Member} object (if the User was in the Guild)
     * until Discord sends the {@link net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent GuildMemberLeaveEvent}.
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be banned due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  userId
     *         The id of the {@link net.dv8tion.jda.core.entities.User User} to ban.
     * @param  delDays
     *         The history of messages, in days, that will be deleted.
     * @param  reason
     *         The reason for this action or {@code null} if there is no specified reason
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.core.Permission#BAN_MEMBERS} permission.
     * @throws net.dv8tion.jda.core.exceptions.HierarchyException
     *         If the logged in account cannot ban the other user due to permission hierarchy position.
     *         <br>See {@link net.dv8tion.jda.core.utils.PermissionUtil#canInteract(Member, Member) PermissionUtil.canInteract(Member, Member)}
     * @throws IllegalArgumentException
     *         If the provided amount of days (delDays) is less than 0.
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *         If the guild is temporarily not {@link net.dv8tion.jda.core.entities.Guild#isAvailable() available}
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @CheckReturnValue
    public AuditableRestAction<Void> ban(String userId, int delDays, String reason)
    {
        checkAvailable();
        checkPermission(Permission.BAN_MEMBERS);

        User user = guild.getJDA().getUserById(userId);
        if (user != null) // If we have the user cached then we should use the additional information available to use during the ban process.
            return ban(user, delDays, reason);

        Route.CompiledRoute route = Route.Guilds.BAN.compile(guild.getId(), userId);
        if (reason != null && !reason.isEmpty())
            route = route.withQueryParams("reason", MiscUtil.encodeUTF8(reason));
        if (delDays > 0)
            route = route.withQueryParams("delete-message-days", Integer.toString(delDays));

        return new AuditableRestAction<Void>(guild.getJDA(), route)
        {
            @Override
            protected void handleResponse(Response response, Request<Void> request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else if (response.code == 404)
                    request.onFailure(new IllegalArgumentException("User with provided id \"" + userId + "\" does not exist! Cannot ban a non-existent user!"));
                else
                    request.onFailure(response);
            }
        };
    }

    /**
     * Bans a {@link net.dv8tion.jda.core.entities.Member Member} and deletes messages sent by the user
     * based on the amount of delDays.
     * <br>If you wish to ban a member without deleting any messages, provide delDays with a value of 0.
     * This change will be applied immediately.
     *
     * <p><b>Note:</b> {@link net.dv8tion.jda.core.entities.Guild#getMembers()} will still contain the
     * {@link net.dv8tion.jda.core.entities.Member Member} until Discord sends the
     * {@link net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent GuildMemberLeaveEvent}.
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be banned due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  member
     *         The {@link net.dv8tion.jda.core.entities.Member Member} to ban.
     * @param  delDays
     *         The history of messages, in days, that will be deleted.
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.core.Permission#BAN_MEMBERS} permission.
     * @throws net.dv8tion.jda.core.exceptions.HierarchyException
     *         If the logged in account cannot ban the other user due to permission hierarchy position.
     *         <br>See {@link net.dv8tion.jda.core.utils.PermissionUtil#canInteract(Member, Member) PermissionUtil.canInteract(Member, Member)}
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the provided amount of days (delDays) is less than 0.</li>
     *             <li>If the provided member is {@code null}</li>
     *         </ul>
     *
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *         If the guild is temporarily not {@link net.dv8tion.jda.core.entities.Guild#isAvailable() available}
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @CheckReturnValue
    public AuditableRestAction<Void> ban(Member member, int delDays)
    {
        return ban(member, delDays, null);
    }

    /**
     * Bans a {@link net.dv8tion.jda.core.entities.Member Member} and deletes messages sent by the user
     * based on the amount of delDays.
     * <br>If you wish to ban a member without deleting any messages, provide delDays with a value of 0.
     * This change will be applied immediately.
     *
     * <p><b>Note:</b> {@link net.dv8tion.jda.core.entities.Guild#getMembers()} will still contain the
     * {@link net.dv8tion.jda.core.entities.Member Member} until Discord sends the
     * {@link net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent GuildMemberLeaveEvent}.
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be banned due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  user
     *         The {@link net.dv8tion.jda.core.entities.User User} to ban.
     * @param  delDays
     *         The history of messages, in days, that will be deleted.
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.core.Permission#BAN_MEMBERS} permission.
     * @throws net.dv8tion.jda.core.exceptions.HierarchyException
     *         If the logged in account cannot ban the other user due to permission hierarchy position.
     *         <br>See {@link net.dv8tion.jda.core.utils.PermissionUtil#canInteract(Member, Member) PermissionUtil.canInteract(Member, Member)}
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the provided amount of days (delDays) is less than 0.</li>
     *             <li>If the provided member is {@code null}</li>
     *         </ul>
     *
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *         If the guild is temporarily not {@link net.dv8tion.jda.core.entities.Guild#isAvailable() available}
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @CheckReturnValue
    public AuditableRestAction<Void> ban(User user, int delDays)
    {
        return ban(user, delDays, null);
    }

    /**
     * Bans the a user specified by the userId and deletes messages sent by the user
     * based on the amount of delDays.
     * <br>If you wish to ban a user without deleting any messages, provide delDays with a value of 0.
     * This change will be applied immediately.
     *
     * <p><b>Note:</b> {@link net.dv8tion.jda.core.entities.Guild#getMembers()} will still contain the {@link net.dv8tion.jda.core.entities.User User's}
     * {@link net.dv8tion.jda.core.entities.Member Member} object (if the User was in the Guild)
     * until Discord sends the {@link net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent GuildMemberLeaveEvent}.
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be banned due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  userId
     *         The id of the {@link net.dv8tion.jda.core.entities.User User} to ban.
     * @param  delDays
     *         The history of messages, in days, that will be deleted.
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.core.Permission#BAN_MEMBERS} permission.
     * @throws net.dv8tion.jda.core.exceptions.HierarchyException
     *         If the logged in account cannot ban the other user due to permission hierarchy position.
     *         <br>See {@link net.dv8tion.jda.core.utils.PermissionUtil#canInteract(Member, Member) PermissionUtil.canInteract(Member, Member)}
     * @throws IllegalArgumentException
     *         If the provided amount of days (delDays) is less than 0.
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *         If the guild is temporarily not {@link net.dv8tion.jda.core.entities.Guild#isAvailable() available}
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @CheckReturnValue
    public AuditableRestAction<Void> ban(String userId, int delDays)
    {
        return ban(userId, delDays, null);
    }

    /**
     * Unbans the specified {@link net.dv8tion.jda.core.entities.User User} from this Guild.
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be unbanned due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_USER UNKNOWN_USER}
     *     <br>The specified User is invalid</li>
     * </ul>
     *
     * @param  user
     *         The id of the {@link net.dv8tion.jda.core.entities.User User} to unban.
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.core.Permission#BAN_MEMBERS} permission.
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *         If the guild is temporarily not {@link net.dv8tion.jda.core.entities.Guild#isAvailable() available}
     * @throws IllegalArgumentException
     *         If the provided user is null
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @CheckReturnValue
    public AuditableRestAction<Void> unban(User user)
    {
        Checks.notNull(user, "User");

        return unban(user.getId());
    }

    /**
     * Unbans the a user specified by the userId from this Guild.
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be unbanned due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_USER UNKNOWN_USER}
     *     <br>The specified User is invalid</li>
     * </ul>
     *
     * @param  userId
     *         The id of the {@link net.dv8tion.jda.core.entities.User User} to unban.
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.core.Permission#BAN_MEMBERS} permission.
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *         If the guild is temporarily not {@link net.dv8tion.jda.core.entities.Guild#isAvailable() available}
     * @throws IllegalArgumentException
     *         If the provided id is null or blank
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @CheckReturnValue
    public AuditableRestAction<Void> unban(String userId)
    {
        checkAvailable();
        Checks.isSnowflake(userId, "User ID");
        checkPermission(Permission.BAN_MEMBERS);

        Route.CompiledRoute route = Route.Guilds.UNBAN.compile(guild.getId(), userId);
        return new AuditableRestAction<Void>(guild.getJDA(), route)
        {
            @Override
            protected void handleResponse(Response response, Request<Void> request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else if (response.code == 404)
                    request.onFailure(new IllegalArgumentException("User with provided id \"" + userId + "\" is not banned! Cannot unban a user who is not currently banned!"));
                else
                    request.onFailure(response);
            }
        };
    }

    /**
     * Sets the Guild Deafened state state of the {@link net.dv8tion.jda.core.entities.Member Member} based on the provided
     * boolean.
     *
     * <p><b>Note:</b> The Member's {@link net.dv8tion.jda.core.entities.GuildVoiceState#isGuildDeafened() GuildVoiceState.isGuildDeafened()} value won't change
     * until JDA receives the {@link net.dv8tion.jda.core.events.guild.voice.GuildVoiceGuildDeafenEvent GuildVoiceGuildDeafenEvent} event related to this change.
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be deafened due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  member
     *         The {@link net.dv8tion.jda.core.entities.Member Member} who's {@link GuildVoiceState VoiceState} is being changed.
     * @param  deafen
     *         Whether this {@link net.dv8tion.jda.core.entities.Member Member} should be deafened or undeafened.
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.core.Permission#VOICE_DEAF_OTHERS} permission.
     * @throws net.dv8tion.jda.core.exceptions.HierarchyException
     *         If the provided member is the Guild's owner. You cannot modify the owner of a Guild.
     * @throws IllegalArgumentException
     *         If the provided member is not from this Guild or null.
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *         If the guild is temporarily not {@link net.dv8tion.jda.core.entities.Guild#isAvailable() available}
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @CheckReturnValue
    public AuditableRestAction<Void> setDeafen(Member member, boolean deafen)
    {
        checkAvailable();
        Checks.notNull(member, "Member");
        checkGuild(member.getGuild(), "Member");
        checkPermission(Permission.VOICE_DEAF_OTHERS);

        //We check the owner instead of Position because, apparently, Discord doesn't care about position for
        // muting and deafening, only whether the affected Member is the owner.
        if (guild.getOwner().equals(member))
            throw new HierarchyException("Cannot modify Guild Deafen status the Owner of the Guild");

        if (member.getVoiceState().isGuildDeafened() == deafen)
            return new AuditableRestAction.EmptyRestAction<>(getJDA(), null);

        JSONObject body = new JSONObject().put("deaf", deafen);
        Route.CompiledRoute route = Route.Guilds.MODIFY_MEMBER.compile(guild.getId(), member.getUser().getId());
        return new AuditableRestAction<Void>(guild.getJDA(), route, body)
        {
            @Override
            protected void handleResponse(Response response, Request<Void> request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    /**
     * Sets the Guild Muted state state of the {@link net.dv8tion.jda.core.entities.Member Member} based on the provided
     * boolean.
     *
     * <p><b>Note:</b> The Member's {@link net.dv8tion.jda.core.entities.GuildVoiceState#isGuildMuted() GuildVoiceState.isGuildMuted()} value won't change
     * until JDA receives the {@link net.dv8tion.jda.core.events.guild.voice.GuildVoiceGuildMuteEvent GuildVoiceGuildMuteEvent} event related to this change.
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be muted due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  member
     *         The {@link net.dv8tion.jda.core.entities.Member Member} who's {@link GuildVoiceState VoiceState} is being changed.
     * @param  mute
     *         Whether this {@link net.dv8tion.jda.core.entities.Member Member} should be muted or unmuted.
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.core.Permission#VOICE_DEAF_OTHERS} permission.
     * @throws net.dv8tion.jda.core.exceptions.HierarchyException
     *         If the provided member is the Guild's owner. You cannot modify the owner of a Guild.
     * @throws java.lang.IllegalArgumentException
     *         If the provided member is not from this Guild or null.
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *         If the guild is temporarily not {@link net.dv8tion.jda.core.entities.Guild#isAvailable() available}
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @CheckReturnValue
    public AuditableRestAction<Void> setMute(Member member, boolean mute)
    {
        checkAvailable();
        Checks.notNull(member, "Member");
        checkGuild(member.getGuild(), "Member");
        checkPermission(Permission.VOICE_MUTE_OTHERS);

        //We check the owner instead of Position because, apparently, Discord doesn't care about position for
        // muting and deafening, only whether the affected Member is the owner.
        if (guild.getOwner().equals(member))
            throw new HierarchyException("Cannot modify Guild Mute status the Owner of the Guild");

        if (member.getVoiceState().isGuildMuted() == mute)
            return new AuditableRestAction.EmptyRestAction<>(getJDA(), null);

        JSONObject body = new JSONObject().put("mute", mute);
        Route.CompiledRoute route = Route.Guilds.MODIFY_MEMBER.compile(guild.getId(), member.getUser().getId());
        return new AuditableRestAction<Void>(guild.getJDA(), route, body)
        {
            @Override
            protected void handleResponse(Response response, Request<Void> request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    /**
     * Atomically assigns the provided {@link net.dv8tion.jda.core.entities.Role Role} to the specified {@link net.dv8tion.jda.core.entities.Member Member}.
     * <br><b>This can be used together with other role modification methods as it does not require an updated cache!</b>
     *
     * <p>If multiple roles should be added/removed (efficiently) in one request
     * you may use {@link #modifyMemberRoles(Member, Collection, Collection) modifyMemberRoles(Member, Collection, Collection)} or similar methods.
     *
     * <p>If the specified role is already present in the member's set of roles this does nothing.
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The Members Roles could not be modified due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The target Member was removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_ROLE UNKNOWN_ROLE}
     *     <br>If the specified Role does not exist</li>
     * </ul>
     *
     * @param  member
     *         The target member who will receive the new role
     * @param  role
     *         The role which should be assigned atomically
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the specified member/role are not from the current Guild</li>
     *             <li>Either member or role are {@code null}</li>
     *         </ul>
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.core.Permission#MANAGE_ROLES Permission.MANAGE_ROLES}
     * @throws net.dv8tion.jda.core.exceptions.HierarchyException
     *         If the provided roles are higher in the Guild's hierarchy
     *         and thus cannot be modified by the currently logged in account
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @CheckReturnValue
    public AuditableRestAction<Void> addSingleRoleToMember(Member member, Role role)
    {
        Checks.notNull(member, "Member");
        Checks.notNull(role, "Role");
        checkGuild(member.getGuild(), "Member");
        checkGuild(role.getGuild(), "Role");
        checkPermission(Permission.MANAGE_ROLES);
        checkPosition(role);

        Route.CompiledRoute route = Route.Guilds.ADD_MEMBER_ROLE.compile(guild.getId(), member.getUser().getId(), role.getId());
        return new AuditableRestAction<Void>(getJDA(), route)
        {
            @Override
            protected void handleResponse(Response response, Request<Void> request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    /**
     * Atomically removes the provided {@link net.dv8tion.jda.core.entities.Role Role} from the specified {@link net.dv8tion.jda.core.entities.Member Member}.
     * <br><b>This can be used together with other role modification methods as it does not require an updated cache!</b>
     *
     * <p>If multiple roles should be added/removed (efficiently) in one request
     * you may use {@link #modifyMemberRoles(Member, Collection, Collection) modifyMemberRoles(Member, Collection, Collection)} or similar methods.
     *
     * <p>If the specified role is not present in the member's set of roles this does nothing.
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The Members Roles could not be modified due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The target Member was removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_ROLE UNKNOWN_ROLE}
     *     <br>If the specified Role does not exist</li>
     * </ul>
     *
     * @param  member
     *         The target member who will lose the specified role
     * @param  role
     *         The role which should be removed atomically
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the specified member/role are not from the current Guild</li>
     *             <li>Either member or role are {@code null}</li>
     *         </ul>
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.core.Permission#MANAGE_ROLES Permission.MANAGE_ROLES}
     * @throws net.dv8tion.jda.core.exceptions.HierarchyException
     *         If the provided roles are higher in the Guild's hierarchy
     *         and thus cannot be modified by the currently logged in account
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @CheckReturnValue
    public AuditableRestAction<Void> removeSingleRoleFromMember(Member member, Role role)
    {
        Checks.notNull(member, "Member");
        Checks.notNull(role, "Role");
        checkGuild(member.getGuild(), "Member");
        checkGuild(role.getGuild(), "Role");
        checkPermission(Permission.MANAGE_ROLES);
        checkPosition(role);

        Route.CompiledRoute route = Route.Guilds.REMOVE_MEMBER_ROLE.compile(guild.getId(), member.getUser().getId(), role.getId());
        return new AuditableRestAction<Void>(getJDA(), route)
        {
            @Override
            protected void handleResponse(Response response, Request<Void> request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    /**
     * Adds all provided {@link net.dv8tion.jda.core.entities.Role Roles}
     * to the specified {@link net.dv8tion.jda.core.entities.Member Member}
     *
     * <h1>Warning</h1>
     * <b>This may <u>not</u> be used together with any other role add/remove/modify methods for the same Member
     * within one event listener cycle! The changes made by this require cache updates which are triggered by
     * lifecycle events which are received later. This may only be called again once the specific Member has been updated
     * by a {@link net.dv8tion.jda.core.events.guild.member.GuildMemberRoleAddEvent GuildMemberRoleAddEvent}.
     * <br>To add <u>and</u> remove Roles from a Member you should use {@link #modifyMemberRoles(Member, Collection, Collection)}</b>
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The Members Roles could not be modified due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The target Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  member
     *         Not-null {@link net.dv8tion.jda.core.entities.Member Member} that will receive all provided roles
     * @param  roles
     *         Not-null Roles that should be added to the specified Member
     *
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *         If the guild is temporarily not {@link net.dv8tion.jda.core.entities.Guild#isAvailable() available}
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.core.Permission#MANAGE_ROLES Permission.MANAGE_ROLES}
     * @throws net.dv8tion.jda.core.exceptions.HierarchyException
     *         If the provided roles are higher in the Guild's hierarchy
     *         and thus cannot be modified by the currently logged in account
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If any or the provided entities is null</li>
     *             <li>If any of the provided entities is not in this Guild</li>
     *             <li>If any of the provided {@link net.dv8tion.jda.core.entities.Role Roles} is the {@code Public Role} of the Guild</li>
     *             <li>If any of the provided {@link net.dv8tion.jda.core.entities.Role Roles} are {@code managed}
     *             <br>Managed Roles can only be applied by the applications that manage them (e.g. Twitch Subscriber Roles)</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     *
     * @see    #addRolesToMember(Member, Collection)
     * @see    #modifyMemberRoles(Member, Role...)
     */
    @CheckReturnValue
    public AuditableRestAction<Void> addRolesToMember(Member member, Role... roles)
    {
        return modifyMemberRoles(member, Arrays.asList(roles), Collections.emptyList());
    }

    /**
     * Adds all provided {@link net.dv8tion.jda.core.entities.Role Roles}
     * to the specified {@link net.dv8tion.jda.core.entities.Member Member}
     *
     * <h1>Warning</h1>
     * <b>This may <u>not</u> be used together with any other role add/remove/modify methods for the same Member
     * within one event listener cycle! The changes made by this require cache updates which are triggered by
     * lifecycle events which are received later. This may only be called again once the specific Member has been updated
     * by a {@link net.dv8tion.jda.core.events.guild.member.GuildMemberRoleAddEvent GuildMemberRoleAddEvent}.
     * <br>To add <u>and</u> remove Roles from a Member you should use {@link #modifyMemberRoles(Member, Collection, Collection)}</b>
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The Members Roles could not be modified due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The target Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  member
     *         Not-null {@link net.dv8tion.jda.core.entities.Member Member} that will receive all provided roles
     * @param  roles
     *         Not-null Roles that should be added to the specified Member
     *
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *         If the guild is temporarily not {@link net.dv8tion.jda.core.entities.Guild#isAvailable() available}
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.core.Permission#MANAGE_ROLES Permission.MANAGE_ROLES}
     * @throws net.dv8tion.jda.core.exceptions.HierarchyException
     *         If the provided roles are higher in the Guild's hierarchy
     *         and thus cannot be modified by the currently logged in account
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If any or the provided entities is null</li>
     *             <li>If any of the provided entities is not in this Guild</li>
     *             <li>If any of the provided {@link net.dv8tion.jda.core.entities.Role Roles} is the {@code Public Role} of the Guild</li>
     *             <li>If any of the provided {@link net.dv8tion.jda.core.entities.Role Roles} are {@code managed}
     *             <br>Managed Roles can only be applied by the applications that manage them (e.g. Twitch Subscriber Roles)</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     *
     * @see    #addRolesToMember(Member, Role...)
     * @see    #modifyMemberRoles(Member, Collection)
     */
    @CheckReturnValue
    public AuditableRestAction<Void> addRolesToMember(Member member, Collection<Role> roles)
    {
        return modifyMemberRoles(member, roles, Collections.emptyList());
    }

    /**
     * Removes all provided {@link net.dv8tion.jda.core.entities.Role Roles}
     * from the specified {@link net.dv8tion.jda.core.entities.Member Member}
     *
     * <h1>Warning</h1>
     * <b>This may <u>not</u> be used together with any other role add/remove/modify methods for the same Member
     * within one event listener cycle! The changes made by this require cache updates which are triggered by
     * lifecycle events which are received later. This may only be called again once the specific Member has been updated
     * by a {@link net.dv8tion.jda.core.events.guild.member.GuildMemberRoleRemoveEvent GuildMemberRoleRemoveEvent}.
     * <br>To add <u>and</u> remove Roles from a Member you should use {@link #modifyMemberRoles(Member, Collection, Collection)}</b>
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The Members Roles could not be modified due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The target Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  member
     *         Not-null {@link net.dv8tion.jda.core.entities.Member Member} from which to remove the {@link net.dv8tion.jda.core.entities.Role Roles}
     * @param  roles
     *         Not-null Roles that should be removed from the specified Member
     *
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *         If the guild is temporarily not {@link net.dv8tion.jda.core.entities.Guild#isAvailable() available}
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.core.Permission#MANAGE_ROLES Permission.MANAGE_ROLES}
     * @throws net.dv8tion.jda.core.exceptions.HierarchyException
     *         If the provided roles are higher in the Guild's hierarchy
     *         and thus cannot be modified by the currently logged in account
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If any or the provided entities is null</li>
     *             <li>If any of the provided entities is not in this Guild</li>
     *             <li>If any of the provided {@link net.dv8tion.jda.core.entities.Role Roles} is the {@code Public Role} of the Guild</li>
     *             <li>If any of the provided {@link net.dv8tion.jda.core.entities.Role Roles} are {@code managed}
     *             <br>Managed Roles can only be applied by the applications that manage them (e.g. Twitch Subscriber Roles)</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     *
     * @see    #addRolesToMember(Member, Collection)
     * @see    #modifyMemberRoles(Member, Role...)
     */
    @CheckReturnValue
    public AuditableRestAction<Void> removeRolesFromMember(Member member, Role... roles)
    {
        return modifyMemberRoles(member, Collections.emptyList(), Arrays.asList(roles));
    }

    /**
     * Removes all provided {@link net.dv8tion.jda.core.entities.Role Roles}
     * from the specified {@link net.dv8tion.jda.core.entities.Member Member}
     *
     * <h1>Warning</h1>
     * <b>This may <u>not</u> be used together with any other role add/remove/modify methods for the same Member
     * within one event listener cycle! The changes made by this require cache updates which are triggered by
     * lifecycle events which are received later. This may only be called again once the specific Member has been updated
     * by a {@link net.dv8tion.jda.core.events.guild.member.GuildMemberRoleRemoveEvent GuildMemberRoleRemoveEvent}.
     * <br>To add <u>and</u> remove Roles from a Member you should use {@link #modifyMemberRoles(Member, Collection, Collection)}</b>
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The Members Roles could not be modified due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The target Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  member
     *         Not-null {@link net.dv8tion.jda.core.entities.Member Member} from which to remove the {@link net.dv8tion.jda.core.entities.Role Roles}
     * @param  roles
     *         Not-null Roles that should be removed from the specified Member
     *
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *         If the guild is temporarily not {@link net.dv8tion.jda.core.entities.Guild#isAvailable() available}
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.core.Permission#MANAGE_ROLES Permission.MANAGE_ROLES}
     * @throws net.dv8tion.jda.core.exceptions.HierarchyException
     *         If the provided roles are higher in the Guild's hierarchy
     *         and thus cannot be modified by the currently logged in account
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If any or the provided entities is null</li>
     *             <li>If any of the provided entities is not in this Guild</li>
     *             <li>If any of the provided {@link net.dv8tion.jda.core.entities.Role Roles} is the {@code Public Role} of the Guild</li>
     *             <li>If any of the provided {@link net.dv8tion.jda.core.entities.Role Roles} are {@code managed}
     *             <br>Managed Roles can only be applied by the applications that manage them (e.g. Twitch Subscriber Roles)</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     *
     * @see    #addRolesToMember(Member, Role...)
     * @see    #modifyMemberRoles(Member, Collection)
     */
    @CheckReturnValue
    public AuditableRestAction<Void> removeRolesFromMember(Member member, Collection<Role> roles)
    {
        return modifyMemberRoles(member, Collections.emptyList(), roles);
    }

    /**
     * Modifies the {@link net.dv8tion.jda.core.entities.Role Roles} of the specified {@link net.dv8tion.jda.core.entities.Member Member}
     * by adding and removing a collection of roles.
     * <br>None of the provided roles may be the <u>Public Role</u> of the current Guild.
     * <br>If a role is both in {@code rolesToAdd} and {@code rolesToRemove} it will be removed.
     *
     * <p>None of the provided collections may be null
     * <br>To only add or remove roles use either {@link #removeRolesFromMember(Member, Collection)} or {@link #addRolesToMember(Member, Collection)}
     *
     * <h1>Warning</h1>
     * <b>This may <u>not</u> be used together with any other role add/remove/modify methods for the same Member
     * within one event listener cycle! The changes made by this require cache updates which are triggered by
     * lifecycle events which are received later. This may only be called again once the specific Member has been updated
     * by a {@link net.dv8tion.jda.core.events.guild.member.GenericGuildMemberEvent GenericGuildMemberEvent} targeting the same Member.</b>
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The Members Roles could not be modified due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The target Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  member
     *         The {@link net.dv8tion.jda.core.entities.Member Member} that should be modified
     * @param  rolesToAdd
     *         A {@link java.util.Collection Collection} of {@link net.dv8tion.jda.core.entities.Role Roles}
     *         to add to the current Roles the specified {@link net.dv8tion.jda.core.entities.Member Member} already has
     * @param  rolesToRemove
     *         A {@link java.util.Collection Collection} of {@link net.dv8tion.jda.core.entities.Role Roles}
     *         to remove from the current Roles the specified {@link net.dv8tion.jda.core.entities.Member Member} already has
     *
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *         If the guild is temporarily not {@link net.dv8tion.jda.core.entities.Guild#isAvailable() available}
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.core.Permission#MANAGE_ROLES Permission.MANAGE_ROLES}
     * @throws net.dv8tion.jda.core.exceptions.HierarchyException
     *         If the provided roles are higher in the Guild's hierarchy
     *         and thus cannot be modified by the currently logged in account
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If any of the provided arguments is {@code null}</li>
     *             <li>If any of the specified Roles is managed or is the {@code Public Role} of the Guild</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @CheckReturnValue
    public AuditableRestAction<Void> modifyMemberRoles(Member member, Collection<Role> rolesToAdd, Collection<Role> rolesToRemove)
    {
        checkAvailable();
        Checks.notNull(member, "Member");
        Checks.notNull(rolesToAdd, "Collection containing roles to be added to the member");
        Checks.notNull(rolesToRemove, "Collection containing roles to be removed from the member");
        checkGuild(member.getGuild(), "Member");
        checkPermission(Permission.MANAGE_ROLES);
        rolesToAdd.forEach(role ->
        {
            Checks.notNull(role, "Role in rolesToAdd");
            checkGuild(role.getGuild(), "Role: " + role.toString());
            checkPosition(role);
            Checks.check(!role.isManaged(), "Cannot add a Managed role to a Member. Role: %s", role.toString());
        });
        rolesToRemove.forEach(role ->
        {
            Checks.notNull(role, "Role in rolesToRemove");
            checkGuild(role.getGuild(), "Role: " + role.toString());
            checkPosition(role);
            Checks.check(!role.isManaged(), "Cannot remove a Managed role from a Member. Role: %s", role.toString());
        });

        Set<Role> currentRoles = new HashSet<>(((MemberImpl) member).getRoleSet());
        Set<Role> newRolesToAdd = new HashSet<>(rolesToAdd);
        newRolesToAdd.removeAll(rolesToRemove);

        // If no changes have been made we return an EmptyRestAction instead
        if (currentRoles.addAll(newRolesToAdd))
            currentRoles.removeAll(rolesToRemove);
        else if (!currentRoles.removeAll(rolesToRemove))
            return new AuditableRestAction.EmptyRestAction<>(guild.getJDA());

        Checks.check(!currentRoles.contains(guild.getPublicRole()),
            "Cannot add the PublicRole of a Guild to a Member. All members have this role by default!");

        JSONObject body = new JSONObject()
                .put("roles", currentRoles.stream().map(Role::getId).collect(Collectors.toList()));
        Route.CompiledRoute route = Route.Guilds.MODIFY_MEMBER.compile(guild.getId(), member.getUser().getId());

        return new AuditableRestAction<Void>(guild.getJDA(), route, body)
        {
            @Override
            protected void handleResponse(Response response, Request<Void> request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    /**
     * Modifies the complete {@link net.dv8tion.jda.core.entities.Role Role} set of the specified {@link net.dv8tion.jda.core.entities.Member Member}
     * <br>The provided roles will replace all current Roles of the specified Member.
     *
     * <h1>Warning</h1>
     * <b>This may <u>not</u> be used together with any other role add/remove/modify methods for the same Member
     * within one event listener cycle! The changes made by this require cache updates which are triggered by
     * lifecycle events which are received later. This may only be called again once the specific Member has been updated
     * by a {@link net.dv8tion.jda.core.events.guild.member.GenericGuildMemberEvent GenericGuildMemberEvent} targeting the same Member.</b>
     *
     * <p><b>The new roles <u>must not</u> contain the Public Role of the Guild</b>
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The Members Roles could not be modified due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The target Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  member
     *         A {@link net.dv8tion.jda.core.entities.Member Member} of which to override the Roles of
     * @param  roles
     *         New collection of {@link net.dv8tion.jda.core.entities.Role Roles} for the specified Member
     *
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *         If the guild is temporarily not {@link net.dv8tion.jda.core.entities.Guild#isAvailable() available}
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.core.Permission#MANAGE_ROLES Permission.MANAGE_ROLES}
     * @throws net.dv8tion.jda.core.exceptions.HierarchyException
     *         If the provided roles are higher in the Guild's hierarchy
     *         and thus cannot be modified by the currently logged in account
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If any of the provided arguments is {@code null}</li>
     *             <li>If any of the provided arguments is not from this Guild</li>
     *             <li>If any of the specified {@link net.dv8tion.jda.core.entities.Role Roles} is managed</li>
     *             <li>If any of the specified {@link net.dv8tion.jda.core.entities.Role Roles} is the {@code Public Role} of this Guild</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     *
     * @see    #modifyMemberRoles(Member, Collection)
     */
    @CheckReturnValue
    public AuditableRestAction<Void> modifyMemberRoles(Member member, Role... roles)
    {
        return modifyMemberRoles(member, Arrays.asList(roles));
    }

    /**
     * Modifies the complete {@link net.dv8tion.jda.core.entities.Role Role} set of the specified {@link net.dv8tion.jda.core.entities.Member Member}
     * <br>The provided roles will replace all current Roles of the specified Member.
     *
     * <p><u>The new roles <b>must not</b> contain the Public Role of the Guild</u>
     *
     * <h1>Warning</h1>
     * <b>This may <u>not</u> be used together with any other role add/remove/modify methods for the same Member
     * within one event listener cycle! The changes made by this require cache updates which are triggered by
     * lifecycle events which are received later. This may only be called again once the specific Member has been updated
     * by a {@link net.dv8tion.jda.core.events.guild.member.GenericGuildMemberEvent GenericGuildMemberEvent} targeting the same Member.</b>
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The Members Roles could not be modified due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The target Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  member
     *         A {@link net.dv8tion.jda.core.entities.Member Member} of which to override the Roles of
     * @param  roles
     *         New collection of {@link net.dv8tion.jda.core.entities.Role Roles} for the specified Member
     *
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *         If the guild is temporarily not {@link net.dv8tion.jda.core.entities.Guild#isAvailable() available}
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.core.Permission#MANAGE_ROLES Permission.MANAGE_ROLES}
     * @throws net.dv8tion.jda.core.exceptions.HierarchyException
     *         If the provided roles are higher in the Guild's hierarchy
     *         and thus cannot be modified by the currently logged in account
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If any of the provided arguments is {@code null}</li>
     *             <li>If any of the provided arguments is not from this Guild</li>
     *             <li>If any of the specified {@link net.dv8tion.jda.core.entities.Role Roles} is managed</li>
     *             <li>If any of the specified {@link net.dv8tion.jda.core.entities.Role Roles} is the {@code Public Role} of this Guild</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     *
     * @see    #modifyMemberRoles(Member, Collection)
     */
    @CheckReturnValue
    public AuditableRestAction<Void> modifyMemberRoles(Member member, Collection<Role> roles)
    {
        checkAvailable();
        Checks.notNull(member, "Member");
        Checks.notNull(roles, "Roles");
        checkGuild(member.getGuild(), "Member");
        roles.forEach(role ->
        {
            Checks.notNull(role, "Role in collection");
            checkGuild(role.getGuild(), "Role: " + role.toString());
            checkPosition(role);
        });

        Checks.check(!roles.contains(guild.getPublicRole()),
            "Cannot add the PublicRole of a Guild to a Member. All members have this role by default!");

        // Return an empty rest action if there were no changes
        final List<Role> memberRoles = member.getRoles();
        if (memberRoles.size() == roles.size() && memberRoles.containsAll(roles))
            return new AuditableRestAction.EmptyRestAction<>(guild.getJDA());

        //Make sure that the current managed roles are preserved and no new ones are added.
        List<Role> currentManaged = memberRoles.stream().filter(Role::isManaged).collect(Collectors.toList());
        List<Role> newManaged = roles.stream().filter(Role::isManaged).collect(Collectors.toList());
        if (!currentManaged.isEmpty() || !newManaged.isEmpty())
        {
            if (!newManaged.containsAll(currentManaged))
            {
                currentManaged.removeAll(newManaged);
                throw new IllegalArgumentException("Cannot remove managed roles from a member! Roles: " + currentManaged.toString());
            }
            if (!currentManaged.containsAll(newManaged))
            {
                newManaged.removeAll(currentManaged);
                throw new IllegalArgumentException("Cannot add managed roles to a member! Roles: " + newManaged.toString());
            }
        }

        //This is identical to the rest action stuff in #modifyMemberRoles(Member, Collection<Role>, Collection<Role>)
        JSONObject body = new JSONObject()
                .put("roles", roles.stream().map(Role::getId).collect(Collectors.toList()));
        Route.CompiledRoute route = Route.Guilds.MODIFY_MEMBER.compile(guild.getId(), member.getUser().getId());

        return new AuditableRestAction<Void>(guild.getJDA(), route, body)
        {
            @Override
            protected void handleResponse(Response response, Request<Void> request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    /**
     * Transfers the Guild ownership to the specified {@link net.dv8tion.jda.core.entities.Member Member}
     * <br>Only available if the currently logged in account is the owner of this Guild
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The currently logged in account lost ownership before completing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The target Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  newOwner
     *         Not-null Member to transfer ownership to
     *
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *         If the guild is temporarily not {@link net.dv8tion.jda.core.entities.Guild#isAvailable() available}
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If the currently logged in account is not the owner of this Guild
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the specified Member is {@code null} or not from the same Guild</li>
     *             <li>If the specified Member already is the Guild owner</li>
     *             <li>If the specified Member is a bot account ({@link net.dv8tion.jda.core.AccountType#BOT AccountType.BOT})</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @CheckReturnValue
    public AuditableRestAction<Void> transferOwnership(Member newOwner)
    {
        checkAvailable();
        Checks.notNull(newOwner, "Member");
        checkGuild(newOwner.getGuild(), "Member");
        if (!guild.getOwner().equals(guild.getSelfMember()))
            throw new PermissionException("The logged in account must be the owner of this Guild to be able to transfer ownership");

        Checks.check(!guild.getSelfMember().equals(newOwner),
            "The member provided as the newOwner is the currently logged in account. Provide a different member to give ownership to.");

        Checks.check(!newOwner.getUser().isBot(), "Cannot transfer ownership of a Guild to a Bot!");

        JSONObject body = new JSONObject().put("owner_id", newOwner.getUser().getId());
        Route.CompiledRoute route = Route.Guilds.MODIFY_GUILD.compile(guild.getId());
        return new AuditableRestAction<Void>(guild.getJDA(), route, body)
        {
            @Override
            protected void handleResponse(Response response, Request<Void> request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    /**
     * Creates a new {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} in this Guild.
     * For this to be successful, the logged in account has to have the {@link net.dv8tion.jda.core.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} Permission
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
     * @param  name
     *         The name of the TextChannel to create
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.core.Permission#MANAGE_CHANNEL} permission
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *         If the guild is temporarily not {@link net.dv8tion.jda.core.entities.Guild#isAvailable() available}
     * @throws IllegalArgumentException
     *         If the provided name is {@code null} or less than 2 characters or greater than 100 characters in length
     *
     * @return A specific {@link net.dv8tion.jda.core.requests.restaction.ChannelAction ChannelAction}
     *         <br>This action allows to set fields for the new TextChannel before creating it
     */
    @CheckReturnValue
    public ChannelAction createTextChannel(String name)
    {
        checkAvailable();
        checkPermission(Permission.MANAGE_CHANNEL);
        Checks.notBlank(name, "Name");
        name = name.trim();

        Checks.check(name.length() >= 2 && name.length() <= 100, "Provided name must be 2 - 100 characters in length");

        Route.CompiledRoute route = Route.Guilds.CREATE_CHANNEL.compile(guild.getId());
        return new ChannelAction(route, name, guild, ChannelType.TEXT);
    }

    /**
     * Creates a new {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} in this Guild.
     * For this to be successful, the logged in account has to have the {@link net.dv8tion.jda.core.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} Permission.
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
     * @param  name
     *         The name of the VoiceChannel to create
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.core.Permission#MANAGE_CHANNEL} permission
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *         If the guild is temporarily not {@link net.dv8tion.jda.core.entities.Guild#isAvailable() available}
     * @throws IllegalArgumentException
     *         If the provided name is {@code null} or less than 2 characters or greater than 100 characters in length
     *
     * @return A specific {@link net.dv8tion.jda.core.requests.restaction.ChannelAction ChannelAction}
     *         <br>This action allows to set fields for the new VoiceChannel before creating it
     */
    @CheckReturnValue
    public ChannelAction createVoiceChannel(String name)
    {
        checkAvailable();
        checkPermission(Permission.MANAGE_CHANNEL);
        Checks.notBlank(name, "Name");
        name = name.trim();

        Checks.check(name.length() >= 2 && name.length() <= 100, "Provided name must be 2 - 100 characters in length");

        Route.CompiledRoute route = Route.Guilds.CREATE_CHANNEL.compile(guild.getId());
        return new ChannelAction(route, name, guild, ChannelType.VOICE);
    }

    /**
     * Creates a new {@link net.dv8tion.jda.core.entities.Category Category} in this Guild.
     * For this to be successful, the logged in account has to have the {@link net.dv8tion.jda.core.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} Permission.
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
     * @param  name
     *         The name of the Category to create
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.core.Permission#MANAGE_CHANNEL} permission
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *         If the guild is temporarily not {@link net.dv8tion.jda.core.entities.Guild#isAvailable() available}
     * @throws IllegalArgumentException
     *         If the provided name is {@code null} or less than 2 characters or greater than 100 characters in length
     *
     * @return A specific {@link net.dv8tion.jda.core.requests.restaction.ChannelAction ChannelAction}
     *         <br>This action allows to set fields for the new Category before creating it
     */
    @CheckReturnValue
    public ChannelAction createCategory(String name)
    {
        checkAvailable();
        checkPermission(Permission.MANAGE_CHANNEL);
        Checks.notBlank(name, "Name");
        name = name.trim();

        Checks.check(name.length() >= 2 && name.length() <= 100, "Provided name must be 2 - 100 characters in length");

        Route.CompiledRoute route = Route.Guilds.CREATE_CHANNEL.compile(guild.getId());
        return new ChannelAction(route, name, guild, ChannelType.CATEGORY);
    }

    /**
     * Creates a copy of the specified {@link net.dv8tion.jda.core.entities.Channel Channel}
     * in this {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * <br>The provided channel need not be in the same Guild for this to work!
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
     * @param  channel
     *         The {@link net.dv8tion.jda.core.entities.Channel Channel} to use for the copy template
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided channel is {@code null}
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have the {@link net.dv8tion.jda.core.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} Permission
     *
     * @return A specific {@link net.dv8tion.jda.core.requests.restaction.ChannelAction ChannelAction}
     *         <br>This action allows to set fields for the new Channel before creating it!
     *
     * @since  3.1
     *
     * @see    #createTextChannel(String)
     * @see    #createVoiceChannel(String)
     * @see    net.dv8tion.jda.core.requests.restaction.ChannelAction ChannelAction
     */
    @CheckReturnValue
    public ChannelAction createCopyOfChannel(Channel channel)
    {
        Checks.notNull(channel, "Channel");
        return channel.createCopy(guild);
    }

    /**
     * Creates a new {@link net.dv8tion.jda.core.entities.Role Role} in this Guild.
     * <br>It will be placed at the bottom (just over the Public Role) to avoid permission hierarchy conflicts.
     * <br>For this to be successful, the logged in account has to have the {@link net.dv8tion.jda.core.Permission#MANAGE_ROLES MANAGE_ROLES} Permission
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The role could not be created due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MAX_ROLES_PER_GUILD MAX_ROLES_PER_GUILD}
     *     <br>There are too many roles in this Guild</li>
     * </ul>
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.core.Permission#MANAGE_ROLES} Permission
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *         If the guild is temporarily not {@link net.dv8tion.jda.core.entities.Guild#isAvailable() available}
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.RoleAction RoleAction}
     *         <br>Creates a new role with previously selected field values
     */
    @CheckReturnValue
    public RoleAction createRole()
    {
        checkAvailable();
        checkPermission(Permission.MANAGE_ROLES);

        Route.CompiledRoute route = Route.Roles.CREATE_ROLE.compile(guild.getId());
        return new RoleAction(route, guild);
    }

    /**
     * Creates a new {@link net.dv8tion.jda.core.entities.Role Role} in this {@link net.dv8tion.jda.core.entities.Guild Guild}
     * with the same settings as the given {@link net.dv8tion.jda.core.entities.Role Role}.
     * <br>The position of the specified Role does not matter in this case!
     *
     * <p>It will be placed at the bottom (just over the Public Role) to avoid permission hierarchy conflicts.
     * <br>For this to be successful, the logged in account has to have the {@link net.dv8tion.jda.core.Permission#MANAGE_ROLES MANAGE_ROLES} Permission
     * and all {@link net.dv8tion.jda.core.Permission Permissions} the given {@link net.dv8tion.jda.core.entities.Role Role} has.
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The role could not be created due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MAX_ROLES_PER_GUILD MAX_ROLES_PER_GUILD}
     *     <br>There are too many roles in this Guild</li>
     * </ul>
     *
     * @param  role
     *         The {@link net.dv8tion.jda.core.entities.Role Role} that should be copied
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.core.Permission#MANAGE_ROLES} Permission and every Permission the provided Role has
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *         If the guild is temporarily not {@link net.dv8tion.jda.core.entities.Guild#isAvailable() available}
     * @throws java.lang.IllegalArgumentException
     *         If the specified role is {@code null}
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.RoleAction RoleAction}
     *         <br>RoleAction with already copied values from the specified {@link net.dv8tion.jda.core.entities.Role Role}
     */
    @CheckReturnValue
    public RoleAction createCopyOfRole(Role role)
    {
        Checks.notNull(role, "Role");
        return role.createCopy(guild);
    }

    /**
     * Creates a new {@link net.dv8tion.jda.core.entities.Emote Emote} in this Guild.
     * <br>If one or more Roles are specified the new Emote will only be available to Members with any of the specified Roles (see {@link Member#canInteract(Emote)})
     * <br>For this to be successful, the logged in account has to have the {@link net.dv8tion.jda.core.Permission#MANAGE_EMOTES MANAGE_EMOTES} Permission.
     *
     * <p><b><u>Unicode emojis are not included as {@link net.dv8tion.jda.core.entities.Emote Emote}!</u></b>
     *
     * <p>Note that a guild is limited to 50 normal and 50 animated emotes by default.
     * Some guilds are able to add additional emotes beyond this limitation due to the
     * {@code MORE_EMOJI} feature (see {@link net.dv8tion.jda.core.entities.Guild#getFeatures() Guild.getFeatures()}).
     * <br>Due to simplicity we do not check for these limits.
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The emote could not be created due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  name
     *         The name for the new Emote
     * @param  icon
     *         The {@link net.dv8tion.jda.core.entities.Icon} for the new Emote
     * @param  roles
     *         The {@link net.dv8tion.jda.core.entities.Role Roles} the new Emote should be restricted to
     *         <br>If no roles are provided the Emote will be available to all Members of this Guild
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.core.Permission#MANAGE_EMOTES MANAGE_EMOTES} Permission
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *         If the guild is temporarily not {@link net.dv8tion.jda.core.entities.Guild#isAvailable() available}
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction} - Type: {@link net.dv8tion.jda.core.entities.Emote Emote}
     */
    @CheckReturnValue
    public AuditableRestAction<Emote> createEmote(String name, Icon icon, Role... roles)
    {
        checkAvailable();
        checkPermission(Permission.MANAGE_EMOTES);
        Checks.notBlank(name, "Emote name");
        Checks.notNull(icon, "Emote icon");
        Checks.notNull(roles, "Roles");

        JSONObject body = new JSONObject();
        body.put("name", name);
        body.put("image", icon.getEncoding());
        if (roles.length > 0) // making sure none of the provided roles are null before mapping them to the snowflake id
            body.put("roles", Stream.of(roles).filter(Objects::nonNull).map(ISnowflake::getId).collect(Collectors.toSet()));

        Route.CompiledRoute route = Route.Emotes.CREATE_EMOTE.compile(guild.getId());
        return new AuditableRestAction<Emote>(getJDA(), route, body)
        {
            @Override
            protected void handleResponse(Response response, Request<Emote> request)
            {
                if (!response.isOk())
                {
                    request.onFailure(response);
                    return;
                }
                JSONObject obj = response.getObject();
                final long id = obj.getLong("id");
                final String name = obj.optString("name", null);
                final boolean managed = Helpers.optBoolean(obj, "managed");
                final boolean animated = obj.optBoolean("animated");
                EmoteImpl emote = new EmoteImpl(id, guild).setName(name).setAnimated(animated).setManaged(managed);

                JSONArray rolesArr = obj.optJSONArray("roles");
                if (rolesArr != null)
                {
                    Set<Role> roleSet = emote.getRoleSet();
                    for (int i = 0; i < rolesArr.length(); i++)
                        roleSet.add(guild.getRoleById(rolesArr.getString(i)));
                }
                request.onSuccess(emote);
            }
        };
    }

    /**
     * Modifies the positional order of {@link net.dv8tion.jda.core.entities.Guild#getCategories() Guild.getCategories()}
     * using a specific {@link net.dv8tion.jda.core.requests.RestAction RestAction} extension to allow moving Channels
     * {@link net.dv8tion.jda.core.requests.restaction.order.OrderAction#moveUp(int) up}/{@link net.dv8tion.jda.core.requests.restaction.order.OrderAction#moveDown(int) down}
     * or {@link net.dv8tion.jda.core.requests.restaction.order.OrderAction#moveTo(int) to} a specific position.
     * <br>This uses <b>ascending</b> order with a 0 based index.
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNNKOWN_CHANNEL}
     *     <br>One of the channels has been deleted before the completion of the task</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The currently logged in account was removed from the Guild</li>
     * </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.order.ChannelOrderAction ChannelOrderAction} - Type: {@link net.dv8tion.jda.core.entities.Category Category}
     */
    @CheckReturnValue
    public ChannelOrderAction<Category> modifyCategoryPositions()
    {
        return new ChannelOrderAction<>(guild, ChannelType.CATEGORY);
    }

    /**
     * Modifies the positional order of {@link net.dv8tion.jda.core.entities.Guild#getTextChannels() Guild.getTextChannels()}
     * using a specific {@link net.dv8tion.jda.core.requests.RestAction RestAction} extension to allow moving Channels
     * {@link net.dv8tion.jda.core.requests.restaction.order.OrderAction#moveUp(int) up}/{@link net.dv8tion.jda.core.requests.restaction.order.OrderAction#moveDown(int) down}
     * or {@link net.dv8tion.jda.core.requests.restaction.order.OrderAction#moveTo(int) to} a specific position.
     * <br>This uses <b>ascending</b> order with a 0 based index.
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNNKOWN_CHANNEL}
     *     <br>One of the channels has been deleted before the completion of the task</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The currently logged in account was removed from the Guild</li>
     * </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.order.ChannelOrderAction ChannelOrderAction} - Type: {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}
     */
    @CheckReturnValue
    public ChannelOrderAction<TextChannel> modifyTextChannelPositions()
    {
        return new ChannelOrderAction<>(guild, ChannelType.TEXT);
    }

    /**
     * Modifies the positional order of {@link net.dv8tion.jda.core.entities.Guild#getVoiceChannels() Guild.getVoiceChannels()}
     * using a specific {@link net.dv8tion.jda.core.requests.RestAction RestAction} extension to allow moving Channels
     * {@link net.dv8tion.jda.core.requests.restaction.order.OrderAction#moveUp(int) up}/{@link net.dv8tion.jda.core.requests.restaction.order.OrderAction#moveDown(int) down}
     * or {@link net.dv8tion.jda.core.requests.restaction.order.OrderAction#moveTo(int) to} a specific position.
     * <br>This uses <b>ascending</b> order with a 0 based index.
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNNKOWN_CHANNEL}
     *     <br>One of the channels has been deleted before the completion of the task</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The currently logged in account was removed from the Guild</li>
     * </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.order.ChannelOrderAction ChannelOrderAction} - Type: {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}
     */
    @CheckReturnValue
    public ChannelOrderAction<VoiceChannel> modifyVoiceChannelPositions()
    {
        return new ChannelOrderAction<>(guild, ChannelType.VOICE);
    }

    /**
     * Modifies the positional order of {@link net.dv8tion.jda.core.entities.Category#getTextChannels() Category#getTextChannels()}
     * using an extension of {@link net.dv8tion.jda.core.requests.restaction.order.ChannelOrderAction ChannelOrderAction}
     * specialized for ordering the nested {@link net.dv8tion.jda.core.entities.TextChannel TextChannels} of this
     * {@link net.dv8tion.jda.core.entities.Category Category}.
     * <br>Like {@code ChannelOrderAction}, the returned {@link net.dv8tion.jda.core.requests.restaction.order.CategoryOrderAction CategoryOrderAction}
     * can be used to move TextChannels {@link net.dv8tion.jda.core.requests.restaction.order.OrderAction#moveUp(int) up},
     * {@link net.dv8tion.jda.core.requests.restaction.order.OrderAction#moveDown(int) down}, or
     * {@link net.dv8tion.jda.core.requests.restaction.order.OrderAction#moveTo(int) to} a specific position.
     * <br>This uses <b>ascending</b> order with a 0 based index.
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNNKOWN_CHANNEL}
     *     <br>One of the channels has been deleted before the completion of the task.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The currently logged in account was removed from the Guild.</li>
     * </ul>
     *
     * @param  category
     *         The {@link net.dv8tion.jda.core.entities.Category Category} to order
     *         {@link net.dv8tion.jda.core.entities.TextChannel TextChannels} from.
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.order.CategoryOrderAction CategoryOrderAction} - Type: {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}
     */
    @CheckReturnValue
    public CategoryOrderAction<TextChannel> modifyTextChannelPositions(Category category)
    {
        Checks.notNull(category, "Category");
        checkGuild(category.getGuild(), "Category");
        return new CategoryOrderAction<>(category, ChannelType.TEXT);
    }

    /**
     * Modifies the positional order of {@link net.dv8tion.jda.core.entities.Category#getVoiceChannels() Category#getVoiceChannels()}
     * using an extension of {@link net.dv8tion.jda.core.requests.restaction.order.ChannelOrderAction ChannelOrderAction}
     * specialized for ordering the nested {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannels} of this
     * {@link net.dv8tion.jda.core.entities.Category Category}.
     * <br>Like {@code ChannelOrderAction}, the returned {@link net.dv8tion.jda.core.requests.restaction.order.CategoryOrderAction CategoryOrderAction}
     * can be used to move VoiceChannels {@link net.dv8tion.jda.core.requests.restaction.order.OrderAction#moveUp(int) up},
     * {@link net.dv8tion.jda.core.requests.restaction.order.OrderAction#moveDown(int) down}, or
     * {@link net.dv8tion.jda.core.requests.restaction.order.OrderAction#moveTo(int) to} a specific position.
     * <br>This uses <b>ascending</b> order with a 0 based index.
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_CHANNEL UNNKOWN_CHANNEL}
     *     <br>One of the channels has been deleted before the completion of the task.</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The currently logged in account was removed from the Guild.</li>
     * </ul>
     *
     * @param  category
     *         The {@link net.dv8tion.jda.core.entities.Category Category} to order
     *         {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannels} from.
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.order.CategoryOrderAction CategoryOrderAction} - Type: {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannels}
     */
    @CheckReturnValue
    public CategoryOrderAction<VoiceChannel> modifyVoiceChannelPositions(Category category)
    {
        Checks.notNull(category, "Category");
        checkGuild(category.getGuild(), "Category");
        return new CategoryOrderAction<>(category, ChannelType.VOICE);
    }

    /**
     * Modifies the positional order of {@link net.dv8tion.jda.core.entities.Guild#getRoles() Guild.getRoles()}
     * using a specific {@link net.dv8tion.jda.core.requests.RestAction RestAction} extension to allow moving Roles
     * {@link net.dv8tion.jda.core.requests.restaction.order.OrderAction#moveUp(int) up}/{@link net.dv8tion.jda.core.requests.restaction.order.OrderAction#moveDown(int) down}
     * or {@link net.dv8tion.jda.core.requests.restaction.order.OrderAction#moveTo(int) to} a specific position.
     *
     * <p>This uses the ordering defined by Discord, which is <b>descending</b>!
     * <br>This means the highest role appears at index {@code 0} and the lower role at index {@code n - 1}.
     * <br>Providing {@code false} to {@link #modifyRolePositions(boolean)} will result in the ordering being
     * in ascending order, with the lower role at index {@code 0} and the highest at index {@code n - 1}.
     * <br>As a note: {@link net.dv8tion.jda.core.entities.Member#getRoles() Member.getRoles()}
     * and {@link net.dv8tion.jda.core.entities.Guild#getRoles() Guild.getRoles()} are both in descending order.
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_ROLE UNKNOWN_ROLE}
     *     <br>One of the roles was deleted before the completion of the task</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The currently logged in account was removed from the Guild</li>
     * </ul>
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.order.RoleOrderAction RoleOrderAction}
     */
    @CheckReturnValue
    public RoleOrderAction modifyRolePositions()
    {
        return modifyRolePositions(true);
    }

    /**
     * Modifies the positional order of {@link net.dv8tion.jda.core.entities.Guild#getRoles() Guild.getRoles()}
     * using a specific {@link net.dv8tion.jda.core.requests.RestAction RestAction} extension to allow moving Roles
     * {@link net.dv8tion.jda.core.requests.restaction.order.OrderAction#moveUp(int) up}/{@link net.dv8tion.jda.core.requests.restaction.order.OrderAction#moveDown(int) down}
     * or {@link net.dv8tion.jda.core.requests.restaction.order.OrderAction#moveTo(int) to} a specific position.
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#UNKNOWN_ROLE UNKNOWN_ROLE}
     *     <br>One of the roles was deleted before the completion of the task</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The currently logged in account was removed from the Guild</li>
     * </ul>
     *
     * @param  useDiscordOrder
     *         Defines the ordering of the OrderAction. If {@code true}, the OrderAction will be in the ordering
     *         defined by Discord for roles, which is Descending. This means that the highest role appears at index {@code 0}
     *         and the lowest role at index {@code n - 1}. Providing {@code false} will result in the ordering being
     *         in ascending order, with the lower role at index {@code 0} and the highest at index {@code n - 1}.
     *         <br>As a note: {@link net.dv8tion.jda.core.entities.Member#getRoles() Member.getRoles()}
     *         and {@link net.dv8tion.jda.core.entities.Guild#getRoles() Guild.getRoles()} are both in descending order.
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.order.RoleOrderAction RoleOrderAction}
     */
    @CheckReturnValue
    public RoleOrderAction modifyRolePositions(boolean useDiscordOrder)
    {
        return new RoleOrderAction(guild, useDiscordOrder);
    }

    protected void checkAvailable()
    {
        if (!guild.isAvailable())
            throw new GuildUnavailableException();
    }

    protected void checkGuild(Guild providedGuild, String comment)
    {
        if (!guild.equals(providedGuild))
            throw new IllegalArgumentException("Provided " + comment + " is not part of this Guild!");
    }

    protected void checkPermission(Permission perm)
    {
        if (!guild.getSelfMember().hasPermission(perm))
            throw new InsufficientPermissionException(perm);
    }

    protected void checkPosition(Member member)
    {
        if(!guild.getSelfMember().canInteract(member))
            throw new HierarchyException("Can't modify a member with higher or equal highest role than yourself!");
    }

    protected void checkPosition(Role role)
    {
        if(!guild.getSelfMember().canInteract(role))
            throw new HierarchyException("Can't modify a role with higher or equal highest role than yourself! Role: " + role.toString());
    }
}
