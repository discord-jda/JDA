/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.dv8tion.jda.core.managers;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.entities.impl.EmoteImpl;
import net.dv8tion.jda.core.entities.impl.GuildImpl;
import net.dv8tion.jda.core.entities.impl.MemberImpl;
import net.dv8tion.jda.core.exceptions.AccountTypeException;
import net.dv8tion.jda.core.exceptions.GuildUnavailableException;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.requests.restaction.ChannelAction;
import net.dv8tion.jda.core.requests.restaction.RoleAction;
import net.dv8tion.jda.core.requests.restaction.WebhookAction;
import net.dv8tion.jda.core.utils.PermissionUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GuildController
{
    protected final Guild guild;

    public GuildController(Guild guild)
    {
        this.guild = guild;
    }

    public Guild getGuild()
    {
        return guild;
    }

    public JDA getJDA()
    {
        return guild.getJDA();
    }

    /**
     * Changes a member's nickname in this guild.
     * The nickname is visible to all members of this guild.<br>
     * This requires
     * ({@link net.dv8tion.jda.core.Permission#NICKNAME_MANAGE NICKNAME_MANAGE} for others and self, however if only modifying self,
     * then only {@link net.dv8tion.jda.core.Permission#NICKNAME_CHANGE NICKNAME_CHANGE} is needed.
     *
     * @param member
     *      The member for which the nickname should be changed.
     * @param nickname
     *      The new nickname of the member, or null / "" to reset
     * @return
     *      {@link net.dv8tion.jda.core.requests.RestAction RestAction} - <br>
     *      &nbsp;&nbsp;&nbsp;&nbsp;<b>Type</b>: Void<br>
     *      &nbsp;&nbsp;&nbsp;&nbsp;<b>Value</b>: None
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *      <ul>
     *          <li>If attempting to set nickname for self and the logged in account has neither {@link net.dv8tion.jda.core.Permission#NICKNAME_CHANGE}
     *          nor {@link net.dv8tion.jda.core.Permission#NICKNAME_MANAGE}</li>
     *          <li>If attempting to set nickname for another user and the logged in account does not have {@link net.dv8tion.jda.core.Permission#NICKNAME_MANAGE}</li>
     *          <li>If attempting to set nickname for another user and the logged in account cannot manipulate the other user due to permission hierarchy position.</li>
     *      </ul>
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *      if the guild is temporarily unavailable
     */
    public RestAction<Void> setNickname(Member member, String nickname)
    {
        checkAvailable();
        checkNull(member, "member");
        checkGuild(member.getGuild(), "member");

        if(member.equals(guild.getSelfMember()))
        {
            if(!PermissionUtil.checkPermission(guild, member, Permission.NICKNAME_CHANGE)
                    && !PermissionUtil.checkPermission(guild, member, Permission.NICKNAME_MANAGE))
                throw new PermissionException(Permission.NICKNAME_CHANGE, "You neither have NICKNAME_CHANGE nor NICKNAME_MANAGE permission!");
        }
        else
        {
            checkPermission(Permission.NICKNAME_MANAGE);
            checkPosition(member);
        }

        if (Objects.equals(nickname, member.getNickname()))
            return new RestAction.EmptyRestAction<Void>(null);

        if (nickname == null)
            nickname = "";

        JSONObject body = new JSONObject().put("nick", nickname);

        Route.CompiledRoute route;
        if (member.equals(guild.getSelfMember()))
            route = Route.Guilds.MODIFY_SELF_NICK.compile(guild.getId());
        else
            route = Route.Guilds.MODIFY_MEMBER.compile(guild.getId(), member.getUser().getId());

        return new RestAction<Void>(guild.getJDA(), route, body)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    /**
     * Used to move a {@link net.dv8tion.jda.core.entities.Member Member} from one {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}
     * to another {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}.<br>
     * As a note, you cannot move a Member that isn't already in a VoiceChannel. Also they must be in a VoiceChannel
     * in the same Guild as the one that you are moving them to.
     *
     * @param member
     *          The {@link net.dv8tion.jda.core.entities.Member Member} that you are moving.
     * @param voiceChannel
     *          The destination {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} to which the member is being
     *          moved to.
     * @return
     *      {@link net.dv8tion.jda.core.requests.RestAction RestAction} - <br>
     *      &nbsp;&nbsp;&nbsp;&nbsp;<b>Type</b>: Void<br>
     *      &nbsp;&nbsp;&nbsp;&nbsp;<b>Value</b>: None
     * @throws java.lang.IllegalStateException
     *          If the Member isn't currently in a VoiceChannel in this Guild.
     * @throws java.lang.IllegalArgumentException
     *          <ul>
     *              <li>If the provided Member isn't part of this {@link net.dv8tion.jda.core.entities.Guild Guild}</li>
     *              <li>If the provided VoiceChannel isn't part of this {@link net.dv8tion.jda.core.entities.Guild Guild}</li>
     *          </ul>
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *          <ul>
     *              <li>If this account doesn't have {@link Permission#VOICE_MOVE_OTHERS} in the VoiceChannel that
     *                  the Member is currently in.</li>
     *              <li>If this account <b>AND</b> the Member being moved don't have
     *                  {@link Permission#VOICE_CONNECT} for the destination VoiceChannel.</li>
     *          </ul>
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *      if the guild is temporarily unavailable
     */
    public RestAction<Void> moveVoiceMember(Member member, VoiceChannel voiceChannel)
    {
        checkAvailable();
        checkNull(member, "member");
        checkNull(member, "voiceChannel");
        checkGuild(member.getGuild(), "member");
        checkGuild(voiceChannel.getGuild(), "voiceChannel");

        GuildVoiceState vState = member.getVoiceState();
        if (!vState.inVoiceChannel())
            throw new IllegalStateException("You cannot move a Member who isn't in a VoiceChannel!");

        if (!PermissionUtil.checkPermission(vState.getChannel(), guild.getSelfMember(), Permission.VOICE_MOVE_OTHERS))
            throw new PermissionException(Permission.VOICE_MOVE_OTHERS, "This account does not have Permission to MOVE_OTHERS out of the channel that the Member is currently in.");

        if (!PermissionUtil.checkPermission(voiceChannel, guild.getSelfMember(), Permission.VOICE_CONNECT)
                && !PermissionUtil.checkPermission(voiceChannel, member, Permission.VOICE_CONNECT))
            throw new PermissionException(Permission.VOICE_CONNECT,
                    "Neither this account nor the Member that is attempting to be moved have the VOICE_CONNECT permission " +
                            "for the destination VoiceChannel, so the move cannot be done.");

        JSONObject body = new JSONObject().put("channel_id", voiceChannel.getId());
        Route.CompiledRoute route = Route.Guilds.MODIFY_MEMBER.compile(guild.getId(), member.getUser().getId());

        return new RestAction<Void>(guild.getJDA(), route, body)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    /**
     * This method will prune (kick) all members who were offline for at least <i>days</i> days.<br>
     * The RestAction returned from this method will return the amount of Members that were pruned.<br>
     * You can use {@link #getPrunableMemberCount(int)} to determine how many Members would be pruned if you were to
     * call this method.
     *
     * @param days
     *      Minimum number of days since a member has been offline to get affected.
     * @return
     *      {@link net.dv8tion.jda.core.requests.RestAction RestAction} - <br>
     *      &nbsp;&nbsp;&nbsp;&nbsp;<b>Type</b>: {@link java.lang.Integer Integer}<br>
     *      &nbsp;&nbsp;&nbsp;&nbsp;<b>Value</b>: The amount of Members that were pruned from the Guild.
     * @throws PermissionException
     *      If the account doesn't have {@link net.dv8tion.jda.core.Permission#KICK_MEMBERS KICK_MEMBER} Permission.
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *      if the guild is temporarily unavailable
     */
    public RestAction<Integer> prune(int days)
    {
        checkAvailable();
        checkPermission(Permission.KICK_MEMBERS);

        if (days < 1)
            throw new IllegalArgumentException("Days amount must be at minimum 1 day.");

        Route.CompiledRoute route = Route.Guilds.PRUNE_MEMBERS.compile(guild.getId(), Integer.toString(days));
        return new RestAction<Integer>(guild.getJDA(), route, null)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (response.isOk())
                    request.onSuccess(response.getObject().getInt("pruned"));
                else
                    request .onFailure(response);
            }
        };
    }

    /**
     * The method calculates the amount of Members that would be pruned if {@link #prune(int)} was executed.
     * Prunability is determined by a member being offline for at least <i>days</i> days.<br>
     *
     * @param days
     *      Minimum number of days since a member has been offline to get affected.
     * @return
     *      {@link net.dv8tion.jda.core.requests.RestAction RestAction} - <br>
     *      &nbsp;&nbsp;&nbsp;&nbsp;<b>Type</b>: {@link java.lang.Integer Integer}<br>
     *      &nbsp;&nbsp;&nbsp;&nbsp;<b>Value</b>: The amount of Members that would be pruned.
     * @throws PermissionException
     *      If the account doesn't have {@link net.dv8tion.jda.core.Permission#KICK_MEMBERS KICK_MEMBER} Permission.
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *      if the guild is temporarily unavailable
     */
    public RestAction<Integer> getPrunableMemberCount(int days)
    {
        checkAvailable();
        checkPermission(Permission.KICK_MEMBERS);

        if (days < 1)
            throw new IllegalArgumentException("Days amount must be at minimum 1 day.");

        Route.CompiledRoute route = Route.Guilds.PRUNABLE_COUNT.compile(guild.getId(), Integer.toString(days));
        return new RestAction<Integer>(guild.getJDA(), route, null)
        {
            @Override
            protected void handleResponse(Response response, Request request)
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
     * <p>
     * <b>Note:</b> {@link net.dv8tion.jda.core.entities.Guild#getMembers()} will still contain the {@link net.dv8tion.jda.core.entities.User User}
     * until Discord sends the {@link net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent GuildMemberLeaveEvent}.
     *
     * @param member
     *          The {@link net.dv8tion.jda.core.entities.Member Member} to kick from the from the {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * @return
     *      {@link net.dv8tion.jda.core.requests.RestAction RestAction} - <br>
     *      &nbsp;&nbsp;&nbsp;&nbsp;<b>Type</b>: Void<br>
     *      &nbsp;&nbsp;&nbsp;&nbsp;<b>Value</b>: None
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *      <ul>
     *          <li>If the logged in account does not have the {@link net.dv8tion.jda.core.Permission#KICK_MEMBERS} permission.</li>
     *          <li>If the logged in account cannot kick the other member due to permission hierarchy position.</li>
     *      </ul>
     * @throws java.lang.IllegalArgumentException
     *      If the provided member is not a Member of this Guild.
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *      if the guild is temporarily unavailable
     */
    public RestAction<Void> kick(Member member)
    {
        checkAvailable();
        checkNull(member, "member");
        checkGuild(member.getGuild(), "member");
        checkPermission(Permission.KICK_MEMBERS);
        checkPosition(member);

        Route.CompiledRoute route = Route.Guilds.KICK_MEMBER.compile(guild.getId(), member.getUser().getId());
        return new RestAction<Void>(guild.getJDA(), route, null)
        {
            @Override
            protected void handleResponse(Response response, Request request)
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
     * <p>
     * <b>Note:</b> {@link net.dv8tion.jda.core.entities.Guild#getMembers()} will still contain the {@link net.dv8tion.jda.core.entities.User User}
     * until Discord sends the {@link net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent GuildMemberLeaveEvent}.
     *
     * @param userId
     *          The id of the {@link net.dv8tion.jda.core.entities.User User} to kick from the from the {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * @return
     *      {@link net.dv8tion.jda.core.requests.RestAction RestAction} - <br>
     *      &nbsp;&nbsp;&nbsp;&nbsp;<b>Type</b>: Void<br>
     *      &nbsp;&nbsp;&nbsp;&nbsp;<b>Value</b>: None
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *      <ul>
     *          <li>If the logged in account does not have the {@link net.dv8tion.jda.core.Permission#KICK_MEMBERS} permission.</li>
     *          <li>If the logged in account cannot kick the other member due to permission hierarchy position.</li>
     *      </ul>
     * @throws java.lang.IllegalArgumentException
     *      If the userId provided does not correspond to a Member in this Guild.
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *      if the guild is temporarily unavailable
     */
    public RestAction<Void> kick(String userId)
    {
        checkNull(userId, "userId");

        Member member = guild.getMemberById(userId);
        if (member == null)
            throw new IllegalArgumentException("The provided userId does not correspond to a member in this guild! Provided userId: " + userId);

        return kick(member);
    }

    /**
     * Bans a {@link net.dv8tion.jda.core.entities.Member Member} and deletes messages sent by the user
     * based on the amount of delDays.<br>
     * If you wish to ban a member without deleting any messages, provide delDays with a value of 0.
     * This change will be applied immediately.
     * <p>
     * <b>Note:</b> {@link net.dv8tion.jda.core.entities.Guild#getMembers()} will still contain the
     *  {@link net.dv8tion.jda.core.entities.Member Member} until Discord sends the
     *  {@link net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent GuildMemberLeaveEvent}.
     *
     * @param member
     *          The {@link net.dv8tion.jda.core.entities.Member Member} to ban.
     * @param delDays
     *          The history of messages, in days, that will be deleted.
     * @return
     *      {@link net.dv8tion.jda.core.requests.RestAction RestAction} - <br>
     *      &nbsp;&nbsp;&nbsp;&nbsp;<b>Type</b>: Void<br>
     *      &nbsp;&nbsp;&nbsp;&nbsp;<b>Value</b>: None
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *      <ul>
     *          <li>If the logged in account does not have the {@link net.dv8tion.jda.core.Permission#BAN_MEMBERS} permission.</li>
     *          <li>If the logged in account cannot ban the other user due to permission hierarchy position.</li>
     *      </ul>
     * @throws java.lang.IllegalArgumentException
     *      If the provided amount of days (delDays) is less than 0.
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *      if the guild is temporarily unavailable
     */
    public RestAction<Void> ban(Member member, int delDays)
    {
        checkAvailable();
        checkNull(member, "member");
        //Don't check if the provided member is from this guild. It doesn't matter if they are or aren't.

        return ban(member.getUser(), delDays);
    }

    /**
     * Bans a {@link net.dv8tion.jda.core.entities.User User} and deletes messages sent by the user
     * based on the amount of delDays.<br>
     * If you wish to ban a user without deleting any messages, provide delDays with a value of 0.
     * This change will be applied immediately.
     * <p>
     * <b>Note:</b> {@link net.dv8tion.jda.core.entities.Guild#getMembers()} will still contain the {@link net.dv8tion.jda.core.entities.User User's}
     *  {@link net.dv8tion.jda.core.entities.Member Member} object (if the User was in the Guild)
     *  until Discord sends the {@link net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent GuildMemberLeaveEvent}.
     *
     * @param user
     *          The {@link net.dv8tion.jda.core.entities.User User} to ban.
     * @param delDays
     *          The history of messages, in days, that will be deleted.
     * @return
     *      {@link net.dv8tion.jda.core.requests.RestAction RestAction} - <br>
     *      &nbsp;&nbsp;&nbsp;&nbsp;<b>Type</b>: Void<br>
     *      &nbsp;&nbsp;&nbsp;&nbsp;<b>Value</b>: None
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *      <ul>
     *          <li>If the logged in account does not have the {@link net.dv8tion.jda.core.Permission#BAN_MEMBERS} permission.</li>
     *          <li>If the logged in account cannot ban the other user due to permission hierarchy position.</li>
     *      </ul>
     * @throws java.lang.IllegalArgumentException
     *      If the provided amount of days (delDays) is less than 0.
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *      if the guild is temporarily unavailable
     */
    public RestAction<Void> ban(User user, int delDays)
    {
        checkAvailable();
        checkNull(user, "user");
        checkPermission(Permission.BAN_MEMBERS);

        if (guild.isMember(user)) // If user is in guild. Check if we are able to ban.
            checkPosition(guild.getMember(user));

        if (delDays < 0)
            throw new IllegalArgumentException("Provided delDays cannot be less that 0. How can you delete messages that are -1 days old?");

        Route.CompiledRoute route;
        if (delDays > 0)
            route = Route.Guilds.BAN_WITH_DELETE.compile(guild.getId(), user.getId(), Integer.toString(delDays));
        else
            route = Route.Guilds.BAN.compile(guild.getId(), user.getId());

        return new RestAction<Void>(guild.getJDA(), route, null)
        {
            @Override
            protected void handleResponse(Response response, Request request)
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
     * based on the amount of delDays.<br>
     * If you wish to ban a user without deleting any messages, provide delDays with a value of 0.
     * This change will be applied immediately.
     * <p>
     * <b>Note:</b> {@link net.dv8tion.jda.core.entities.Guild#getMembers()} will still contain the {@link net.dv8tion.jda.core.entities.User User's}
     * {@link net.dv8tion.jda.core.entities.Member Member} object (if the User was in the Guild)
     * until Discord sends the {@link net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent GuildMemberLeaveEvent}.
     *
     * @param userId
     *          The id of the {@link net.dv8tion.jda.core.entities.User User} to ban.
     * @param delDays
     *          The history of messages, in days, that will be deleted.
     * @return
     *      {@link net.dv8tion.jda.core.requests.RestAction RestAction} - <br>
     *      &nbsp;&nbsp;&nbsp;&nbsp;<b>Type</b>: Void<br>
     *      &nbsp;&nbsp;&nbsp;&nbsp;<b>Value</b>: None
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *      <ul>
     *          <li>If the logged in account does not have the {@link net.dv8tion.jda.core.Permission#BAN_MEMBERS} permission.</li>
     *          <li>If the logged in account cannot ban the other user due to permission hierarchy position.</li>
     *      </ul>
     * @throws java.lang.IllegalArgumentException
     *      If the provided amount of days (delDays) is less than 0.
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *      if the guild is temporarily unavailable
     */
    public RestAction<Void> ban(String userId, int delDays)
    {
        checkAvailable();
        checkNull(userId, "userId");
        checkPermission(Permission.BAN_MEMBERS);

        User user = guild.getJDA().getUserById(userId);
        if (user != null) // If we have the user cached then we should use the additional information available to use during the ban process.
        {
            return ban(user, delDays);
        }

        Route.CompiledRoute route;
        if (delDays > 0)
            route = Route.Guilds.BAN_WITH_DELETE.compile(guild.getId(), userId, Integer.toString(delDays));
        else
            route = Route.Guilds.BAN.compile(guild.getId(), userId);

        return new RestAction<Void>(guild.getJDA(), route, null)
        {
            @Override
            protected void handleResponse(Response response, Request request)
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
     * Unbans the specified {@link net.dv8tion.jda.core.entities.User User} from this Guild.
     *
     * @param user
     *          The id of the {@link net.dv8tion.jda.core.entities.User User} to unban.
     * @return
     *      {@link net.dv8tion.jda.core.requests.RestAction RestAction} - <br>
     *      &nbsp;&nbsp;&nbsp;&nbsp;<b>Type</b>: Void<br>
     *      &nbsp;&nbsp;&nbsp;&nbsp;<b>Value</b>: None
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *      If the logged in account does not have the {@link net.dv8tion.jda.core.Permission#BAN_MEMBERS} permission.
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *      if the guild is temporarily unavailable
     */
    public RestAction<Void> unban(User user)
    {
        checkNull(user, "user");

        return unban(user.getId());
    }

    /**
     * Unbans the a user specified by the userId from this Guild.
     *
     * @param userId
     *          The id of the {@link net.dv8tion.jda.core.entities.User User} to unban.
     * @return
     *      {@link net.dv8tion.jda.core.requests.RestAction RestAction} - <br>
     *      &nbsp;&nbsp;&nbsp;&nbsp;<b>Type</b>: Void<br>
     *      &nbsp;&nbsp;&nbsp;&nbsp;<b>Value</b>: None
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *      If the logged in account does not have the {@link net.dv8tion.jda.core.Permission#BAN_MEMBERS} permission.
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *      if the guild is temporarily unavailable
     */
    public RestAction<Void> unban(String userId)
    {
        checkAvailable();
        checkNull(userId, "userId");
        checkPermission(Permission.BAN_MEMBERS);

        Route.CompiledRoute route = Route.Guilds.UNBAN.compile(guild.getId(), userId);
        return new RestAction<Void>(guild.getJDA(), route, null)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else if (response.code == 404)
                    request.onFailure(new IllegalArgumentException("User with provided id \"" + userId + "\" does not exist! Cannot unban a non-existent user!"));
                else
                    request.onFailure(response);
            }
        };
    }

    /**
     * Sets the Guild Deafened state state of the {@link net.dv8tion.jda.core.entities.Member Member} based on the provided
     * boolean.
     * <p>
     * <b>Note:</b> The Member's {@link net.dv8tion.jda.core.entities.GuildVoiceState#isGuildDeafened()} value won't change
     * until JDA receives the {@link net.dv8tion.jda.core.events.guild.voice.GuildVoiceGuildDeafenEvent} event related to this change.
     *
     * @param member
     *          The {@link net.dv8tion.jda.core.entities.Member Member} who's {@link GuildVoiceState VoiceState} is being changed.
     * @param deafen
     *          Whether this {@link net.dv8tion.jda.core.entities.Member Member} should be deafened or undeafened.
     * @return
     *      {@link net.dv8tion.jda.core.requests.RestAction RestAction} - <br>
     *      &nbsp;&nbsp;&nbsp;&nbsp;<b>Type</b>: Void<br>
     *      &nbsp;&nbsp;&nbsp;&nbsp;<b>Value</b>: None
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *      <ul>
     *          <li>If the logged in account does not have the {@link net.dv8tion.jda.core.Permission#VOICE_DEAF_OTHERS} permission.</li>
     *          <li>If the provided member is the Guild's owner. You cannot modify the owner of a Guild.</li>
     *      </ul>
     * @throws java.lang.IllegalArgumentException
     *      If the provided member is not from this Guild.
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *      if the guild is temporarily unavailable
     */
    public RestAction<Void> setDeafen(Member member, boolean deafen)
    {
        checkAvailable();
        checkNull(member, "member");
        checkGuild(member.getGuild(), "member");
        checkPermission(Permission.VOICE_DEAF_OTHERS);

        //We check the owner instead of Position because, apparently, Discord doesn't care about position for
        // muting and deafening, only whether the affected Member is the owner.
        if (guild.getOwner().equals(member))
            throw new PermissionException("Cannot modified Guild Deafen status the Owner of the Guild");

        if (member.getVoiceState().isGuildDeafened() == deafen)
            return new RestAction.EmptyRestAction<Void>(null);

        JSONObject body = new JSONObject().put("deaf", deafen);
        Route.CompiledRoute route = Route.Guilds.MODIFY_MEMBER.compile(guild.getId(), member.getUser().getId());
        return new RestAction<Void>(guild.getJDA(), route, body)
        {
            @Override
            protected void handleResponse(Response response, Request request)
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
     * <p>
     * <b>Note:</b> The Member's {@link net.dv8tion.jda.core.entities.GuildVoiceState#isGuildMuted()} value won't change
     * until JDA receives the {@link net.dv8tion.jda.core.events.guild.voice.GuildVoiceGuildMuteEvent} event related to this change.
     *
     * @param member
     *          The {@link net.dv8tion.jda.core.entities.Member Member} who's {@link GuildVoiceState VoiceState} is being changed.
     * @param mute
     *          Whether this {@link net.dv8tion.jda.core.entities.Member Member} should be muted or unmuted.
     * @return
     *      {@link net.dv8tion.jda.core.requests.RestAction RestAction} - <br>
     *      &nbsp;&nbsp;&nbsp;&nbsp;<b>Type</b>: Void<br>
     *      &nbsp;&nbsp;&nbsp;&nbsp;<b>Value</b>: None
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *      <ul>
     *          <li>If the logged in account does not have the {@link net.dv8tion.jda.core.Permission#VOICE_MUTE_OTHERS} permission.</li>
     *          <li>If the provided member is the Guild's owner. You cannot modify the owner of a Guild.</li>
     *      </ul>
     * @throws java.lang.IllegalArgumentException
     *      If the provided member is not from this Guild.
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *      if the guild is temporarily unavailable
     */
    public RestAction<Void> setMute(Member member, boolean mute)
    {
        checkAvailable();
        checkNull(member, "member");
        checkGuild(member.getGuild(), "member");
        checkPermission(Permission.VOICE_MUTE_OTHERS);

        //We check the owner instead of Position because, apparently, Discord doesn't care about position for
        // muting and deafening, only whether the affected Member is the owner.
        if (guild.getOwner().equals(member))
            throw new PermissionException("Cannot modified Guild Mute status the Owner of the Guild");

        if (member.getVoiceState().isGuildMuted() == mute)
            return new RestAction.EmptyRestAction<Void>(null);

        JSONObject body = new JSONObject().put("mute", mute);
        Route.CompiledRoute route = Route.Guilds.MODIFY_MEMBER.compile(guild.getId(), member.getUser().getId());
        return new RestAction<Void>(guild.getJDA(), route, body)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    /**
     * Gets an unmodifiable list of the currently banned {@link net.dv8tion.jda.core.entities.User Users}.<br>
     * If you wish to ban or unban a user, please {@link #ban(net.dv8tion.jda.core.entities.User, int)} or
     * {@link #unban(net.dv8tion.jda.core.entities.User)};
     *
     * @return
     *      {@link net.dv8tion.jda.core.requests.RestAction RestAction} - <br>
     *      &nbsp;&nbsp;&nbsp;&nbsp;<b>Type</b>: {@link java.util.List}&lt;{@link net.dv8tion.jda.core.entities.User}&gt;<br>
     *      &nbsp;&nbsp;&nbsp;&nbsp;<b>Value</b>: An unmodifiable list of all users currently banned from this Guild.
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *      If the logged in account does not have the {@link net.dv8tion.jda.core.Permission#BAN_MEMBERS} permission.
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *      if the guild is temporarily unavailable
     */
    public RestAction<List<User>> getBans()
    {
        checkAvailable();
        checkPermission(Permission.BAN_MEMBERS);

        Route.CompiledRoute route = Route.Guilds.GET_BANS.compile(guild.getId());
        return new RestAction<List<User>>(guild.getJDA(), route, null)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (!response.isOk())
                {
                    request.onFailure(response);
                    return;
                }

                EntityBuilder builder = EntityBuilder.get(guild.getJDA());
                List<User> bans = new LinkedList<>();
                JSONArray bannedArr = response.getArray();

                for (int i = 0; i < bannedArr.length(); i++)
                {
                    JSONObject user = bannedArr.getJSONObject(i).getJSONObject("user");
                    bans.add(builder.createFakeUser(user, false));
                }
                request.onSuccess(Collections.unmodifiableList(bans));
            }
        };
    }

    public RestAction<Void> addRolesToMember(Member member, Role... roles)
    {
        return modifyMemberRoles(member, Arrays.asList(roles), Collections.emptyList());
    }

    public RestAction<Void> addRolesToMember(Member member, Collection<Role> roles)
    {
        return modifyMemberRoles(member, roles, Collections.emptyList());
    }

    public RestAction<Void> removeRolesFromMember(Member member, Role... roles)
    {
        return modifyMemberRoles(member, Collections.emptyList(), Arrays.asList(roles));
    }

    public RestAction<Void> removeRolesFromMember(Member member, Collection<Role> roles)
    {
        return modifyMemberRoles(member, Collections.emptyList(), roles);
    }

    public RestAction<Void> modifyMemberRoles(Member member, Collection<Role> rolesToAdd, Collection<Role> rolesToRemove)
    {
        checkAvailable();
        checkNull(member, "member");
        checkNull(rolesToAdd, "Collection containing roles to be added to the member");
        checkNull(rolesToRemove, "Collection containing roles to be removed from the member");
        checkGuild(member.getGuild(), "member");
        checkPermission(Permission.MANAGE_ROLES);
        rolesToAdd.forEach(role ->
        {
            checkNull(role, "role in rolesToAdd");
            checkGuild(role.getGuild(), "role: " + role.toString());
            checkPosition(role);
            if (role.isManaged())
                throw new IllegalArgumentException("Cannot add a Managed role to a Member. Role: " + role.toString());
        });
        rolesToRemove.forEach(role ->
        {
            checkNull(role, "role in rolesToRemove");
            checkGuild(role.getGuild(), "role: " + role.toString());
            checkPosition(role);
            if (role.isManaged())
                throw new IllegalArgumentException("Cannot remove a Managed role from a Member. Role: " + role.toString());
        });

        Set<Role> currentRoles = new HashSet<>(((MemberImpl) member).getRoleSet());
        currentRoles.addAll(rolesToAdd);
        currentRoles.removeAll(rolesToRemove);

        if (currentRoles.contains(guild.getPublicRole()))
            throw new IllegalArgumentException("Cannot add the PublicRole of a Guild to a Member. All members have this role by default!");

        JSONObject body = new JSONObject()
                .put("roles", currentRoles.stream().map(Role::getId).collect(Collectors.toList()));
        Route.CompiledRoute route = Route.Guilds.MODIFY_MEMBER.compile(guild.getId(), member.getUser().getId());

        return new RestAction<Void>(guild.getJDA(), route, body)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    //Used to SET roles, not just modify the existing role list. e.g: The provided roles are the only ones the member
    // will have afterwards
    //Note: if no roles are provided, this member will have no roles.
    public RestAction<Void> modifyMemberRoles(Member member, Role... roles)
    {
        return modifyMemberRoles(member, Arrays.asList(roles));
    }

    //Used to SET roles, not just modify the existing role list. e.g: The provided roles are the only ones the member
    // will have afterwards.
    //Note: if roles.isEmpty(), then this member will have no roles.
    public RestAction<Void> modifyMemberRoles(Member member, Collection<Role> roles)
    {
        checkAvailable();
        checkNull(member, "member");
        checkNull(roles, "roles");
        checkGuild(member.getGuild(), "member");
        roles.forEach(role ->
        {
            checkNull(role, "role in collection");
            checkGuild(role.getGuild(), "role: " + role.toString());
            checkPosition(role);
        });

        if (roles.contains(guild.getPublicRole()))
            throw new IllegalArgumentException("Cannot add the PublicRole of a Guild to a Member. All members have this role by default!");

        //Make sure that the current managed roles are preserved and no new ones are added.
        List<Role> currentManaged = roles.stream().filter(r -> r.isManaged()).collect(Collectors.toList());
        List<Role> newManaged = roles.stream().filter(r -> r.isManaged()).collect(Collectors.toList());
        if (currentManaged.size() != 0 || newManaged.size() != 0)
        {
            for (Iterator<Role> it = currentManaged.iterator(); it.hasNext();)
            {
                Role r = it.next();
                if (newManaged.contains(r))
                    it.remove();
            }

            if (currentManaged.size() > 0)
                throw new IllegalArgumentException("Cannot remove managed roles from a member! Roles: " + currentManaged.toString());
            if (newManaged.size() > 0)
                throw new IllegalArgumentException("Cannot add managed roles to a member! Roles: " + newManaged.toString());
        }

        //This is identical to the rest action stuff in #modifyMemberRoles(Member, Collection<Role>, Collection<Role>)
        JSONObject body = new JSONObject()
                .put("roles", roles.stream().map(Role::getId).collect(Collectors.toList()));
        Route.CompiledRoute route = Route.Guilds.MODIFY_MEMBER.compile(guild.getId(), member.getUser().getId());

        return new RestAction<Void>(guild.getJDA(), route, body)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    public RestAction<Void> transferOwnership(Member newOwner)
    {
        checkAvailable();
        checkNull(newOwner, "newOwner member");
        checkGuild(newOwner.getGuild(), "newOwner member");
        if (!guild.getOwner().equals(guild.getSelfMember()))
            throw new PermissionException("The logged in account must be the owner of this Guild to be able to transfer ownership");

        if (guild.getSelfMember().equals(newOwner))
            throw new IllegalArgumentException("The member provided as the newOwner is the currently logged in account. Provide a different member to give ownership to.");

        if (newOwner.getUser().isBot())
            throw new IllegalArgumentException("Cannot transfer ownership of a Guild to a Bot!");

        JSONObject body = new JSONObject().put("owner_id", newOwner.getUser().getId());
        Route.CompiledRoute route = Route.Guilds.MODIFY_GUILD.compile(guild.getId());
        return new RestAction<Void>(guild.getJDA(), route, body)
        {
            @Override
            protected void handleResponse(Response response, Request request)
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
     * For this to be successful, the logged in account has to have the
     * {@link net.dv8tion.jda.core.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} Permission
     *
     * @param name
     *      the name of the TextChannel to create
     * @return
     *      {@link net.dv8tion.jda.core.requests.RestAction RestAction} - <br>
     *      &nbsp;&nbsp;&nbsp;&nbsp;<b>Type</b>: {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}<br>
     *      &nbsp;&nbsp;&nbsp;&nbsp;<b>Value</b>: The newly created {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *      If the logged in account does not have the {@link net.dv8tion.jda.core.Permission#MANAGE_CHANNEL} permission.
     * @throws java.lang.IllegalArgumentException
     *      If the provided name is less than 2 characters or greater than 100 characters in length
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *      if the guild is temporarily unavailable
     */
    public ChannelAction createTextChannel(String name)
    {
        checkAvailable();
        checkPermission(Permission.MANAGE_CHANNEL);
        checkNull(name, "name");

        if (name.length() < 2 || name.length() > 100)
            throw new IllegalArgumentException("Provided name must be 2 - 100 characters in length");

        JSONObject body = new JSONObject()
                .put("type", "text")
                .put("name", name);
        Route.CompiledRoute route = Route.Guilds.CREATE_CHANNEL.compile(guild.getId());
        return new ChannelAction(route, name, guild, false);
    }

    /**
     * Creates a new {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} in this Guild.
     * For this to be successful, the logged in account has to have the
     * {@link net.dv8tion.jda.core.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} Permission.
     *
     * @param name
     *      the name of the VoiceChannel to create
     * @return
     *      {@link net.dv8tion.jda.core.requests.RestAction RestAction} - <br>
     *      &nbsp;&nbsp;&nbsp;&nbsp;<b>Type</b>: {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}<br>
     *      &nbsp;&nbsp;&nbsp;&nbsp;<b>Value</b>: The newly created {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel}
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *      If the logged in account does not have the {@link net.dv8tion.jda.core.Permission#MANAGE_CHANNEL} permission.
     * @throws java.lang.IllegalArgumentException
     *      If the provided name is less than 2 characters or greater than 100 characters in length
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *      if the guild is temporarily unavailable
     */
    public ChannelAction createVoiceChannel(String name)
    {
        checkAvailable();
        checkPermission(Permission.MANAGE_CHANNEL);
        checkNull(name, "name");

        if (name.length() < 2 || name.length() > 100)
            throw new IllegalArgumentException("Provided name must be 2 to 100 characters in length");

        JSONObject body = new JSONObject()
                .put("type", "voice")
                .put("name", name);
        Route.CompiledRoute route = Route.Guilds.CREATE_CHANNEL.compile(guild.getId());
        return new ChannelAction(route, name, guild, true);
    }

    /**
     * Creates a new {@link net.dv8tion.jda.core.entities.Webhook Webhook} for the specified
     * {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}.
     *
     * @param  channel
     *         The target TextChannel to attach a new Webhook to.
     * @param  name
     *         The default name for the new Webhook.
     *
     * @throws IllegalArgumentException
     *         If any of the provided arguments is null.
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If you do not hold the permission {@link net.dv8tion.jda.core.Permission#MANAGE_WEBHOOKS Manage Webhooks}
     *         on the selected channel.
     *
     * @return A specified {@link net.dv8tion.jda.core.requests.restaction.WebhookAction WebhookAction}
     *         This action allows you to set fields for the new webhook before creating it.
     */
    public WebhookAction createWebhook(TextChannel channel, String name)
    {
        checkNull(name, "Webhook name");
        checkNull(channel, "TextChannel");
        if (!guild.getSelfMember().hasPermission(channel, Permission.MANAGE_WEBHOOKS))
            throw new PermissionException(Permission.MANAGE_WEBHOOKS);

        Route.CompiledRoute route = Route.Channels.CREATE_WEBHOOK.compile(channel.getId());
        return new WebhookAction(getJDA(), route, name);
    }

    /**
     * Creates a new {@link net.dv8tion.jda.core.entities.Role Role} in this Guild.
     * For this to be successful, the logged in account has to have the {@link net.dv8tion.jda.core.Permission#MANAGE_ROLES MANAGE_ROLES Permission}
     *
     * @return
     *      {@link net.dv8tion.jda.core.requests.RestAction RestAction} - <br>
     *      &nbsp;&nbsp;&nbsp;&nbsp;<b>Type</b>: {@link net.dv8tion.jda.core.entities.Role Role}<br>
     *      &nbsp;&nbsp;&nbsp;&nbsp;<b>Value</b>: The newly created {@link net.dv8tion.jda.core.entities.Role Role}
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *      If the logged in account does not have the {@link net.dv8tion.jda.core.Permission#MANAGE_ROLES} permission.
     * @throws java.lang.IllegalArgumentException
     *      If the provided name is less than 2 characters or greater than 100 characters in length
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *      if the guild is temporarily unavailable
     */
    public RoleAction createRole()
    {
        checkAvailable();
        checkPermission(Permission.MANAGE_ROLES);

        Route.CompiledRoute route = Route.Roles.CREATE_ROLE.compile(guild.getId());
        return new RoleAction(route, guild);
    }

    /**
     * Creates a new {@link net.dv8tion.jda.core.entities.Role Role} in this {@link net.dv8tion.jda.core.entities.Guild Guild} with the same settings as the given {@link net.dv8tion.jda.core.entities.Role Role}.
     * It will be placed at the bottom (just over the @everyone role) to avoid permission hierarchy conflicts.
     * For this to be successful, the logged in account has to have the {@link net.dv8tion.jda.core.Permission#MANAGE_ROLES MANAGE_ROLES Permission}
     * and all {@link net.dv8tion.jda.core.Permission Permissions} the given {@link net.dv8tion.jda.core.entities.Role Role} has.
     *
     * @param role
     *      The {@link net.dv8tion.jda.core.entities.Role Role} that should be copied
     * @return
     *      {@link net.dv8tion.jda.core.requests.RestAction RestAction} - <br>
     *      &nbsp;&nbsp;&nbsp;&nbsp;<b>Type</b>: {@link net.dv8tion.jda.core.entities.Role Role}<br>
     *      &nbsp;&nbsp;&nbsp;&nbsp;<b>Value</b>: The newly created {@link net.dv8tion.jda.core.entities.Role Role} after it has been created and had settings from the provided role copied to it.
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *      If the logged in account does not have the {@link net.dv8tion.jda.core.Permission#MANAGE_ROLES} permission and every Permission the provided Role has.
     * @throws java.lang.IllegalArgumentException
     *      If the provided name is less than 2 characters or greater than 100 characters in length
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *      if the guild is temporarily unavailable
     */
    public RestAction<Role> createCopyOfRole(Role role)
    {
        checkAvailable();
        checkPermission(Permission.MANAGE_ROLES);
        role.getPermissions().forEach(this::checkPermission);

        Route.CompiledRoute route = Route.Roles.CREATE_ROLE.compile(guild.getId());
        return new RestAction<Role>(getJDA(), route, null)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (!response.isOk())
                {
                    request.onFailure(response);
                    return;
                }

                JSONObject roleJson = response.getObject();
                Role r = EntityBuilder.get(api).createRole(roleJson, guild.getId());

                RoleManagerUpdatable mng = r.getManagerUpdatable()
                        .getNameField().setValue(role.getName())
                        .getColorField().setValue(role.getColor())
                        .getMentionableField().setValue(role.isMentionable())
                        .getHoistedField().setValue(role.isHoisted())
                        .getPermissionField().setValue(role.getPermissionsRaw());

                mng.update().queue(request.getOnSuccess(), request.getOnFailure());
            }
        };
    }

    /**
     * Creates a new {@link net.dv8tion.jda.core.entities.Emote Emote} in this Guild.<br>
     * For this to be successful, the logged in account has to have the {@link net.dv8tion.jda.core.Permission#MANAGE_EMOTES MANAGE_EMOTES Permission}.
     *
     * @param name
     *      The name for the new Emote
     * @param icon
     *      The {@link net.dv8tion.jda.core.entities.Icon} for the new Emote
     * @param roles
     *      The {@link net.dv8tion.jda.core.entities.Role Roles} the new Emote should be restricted to
     * @return
     *      {@link net.dv8tion.jda.core.requests.RestAction RestAction} - <br>
     *      &nbsp;&nbsp;&nbsp;&nbsp;<b>Type</b>: {@link net.dv8tion.jda.core.entities.Emote Emote}<br>
     *      &nbsp;&nbsp;&nbsp;&nbsp;<b>Value</b>: The newly created {@link net.dv8tion.jda.core.entities.Emote Emote}
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *      if the guild is temporarily unavailable
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *      if the logged in account does not have the {@link net.dv8tion.jda.core.Permission#MANAGE_EMOTES} permission.
     * @throws net.dv8tion.jda.core.exceptions.AccountTypeException
     *      if the logged in account is not from {@link net.dv8tion.jda.core.AccountType#CLIENT AccountType#Client}
     */
    public RestAction<Emote> createEmote(String name, Icon icon, Role... roles)
    {
        checkAvailable();
        checkPermission(Permission.MANAGE_EMOTES);
        checkNull(name, "emote name");
        checkNull(icon, "emote icon");

        if (getJDA().getAccountType() != AccountType.CLIENT)
            throw new AccountTypeException(AccountType.CLIENT);

        JSONObject body = new JSONObject();
        body.put("name", name);
        body.put("image", icon.getEncoding());
        if (roles.length > 0) // making sure none of the provided roles are null before mapping them to the snowflake id
            body.put("roles", Stream.of(roles).filter(r -> r != null).map(ISnowflake::getId).collect(Collectors.toSet()));

        Route.CompiledRoute route = Route.Emotes.CREATE_EMOTE.compile(guild.getId());
        return new RestAction<Emote>(getJDA(), route, body)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (response.isOk())
                {
                    JSONObject obj = response.getObject();
                    String id = obj.getString("id");
                    String name = obj.getString("name");
                    EmoteImpl emote = new EmoteImpl(id, guild).setName(name);
                    // managed is false by default, should always be false for emotes created by client accounts.

                    JSONArray rolesArr = obj.getJSONArray("roles");
                    Set<Role> roleSet = emote.getRoleSet();
                    for (int i = 0; i < rolesArr.length(); i++)
                    {
                        roleSet.add(guild.getRoleById(rolesArr.getString(i)));
                    }

                    // put emote into cache
                    ((GuildImpl) guild).getEmoteMap().put(id, emote);

                    request.onSuccess(emote);
                }
                else
                    request.onFailure(response);
            }
        };
    }

    protected void checkAvailable()
    {
        if (!guild.isAvailable())
            throw new GuildUnavailableException();
    }

    protected void checkNull(Object obj, String name)
    {
        if (obj == null)
            throw new NullPointerException("Provided " + name + " was null!");
    }

    protected void checkGuild(Guild providedGuild, String comment)
    {
        if (!guild.equals(providedGuild))
            throw new IllegalArgumentException("Provided " + comment + " is not part of this Guild!");
    }

    protected void checkPermission(Permission perm)
    {
        if (!PermissionUtil.checkPermission(guild, guild.getSelfMember(), perm))
            throw new PermissionException(perm);
    }

    protected void checkPosition(Member member)
    {
        if(!PermissionUtil.canInteract(guild.getSelfMember(), member))
            throw new PermissionException("Can't modify a member with higher or equal highest role than yourself!");
    }

    protected void checkPosition(Role role)
    {
        if(!PermissionUtil.canInteract(guild.getSelfMember(), role))
            throw new PermissionException("Can't modify a role with higher or equal highest role than yourself! Role: " + role.toString());
    }
}
