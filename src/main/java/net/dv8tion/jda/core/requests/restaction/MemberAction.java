/*
 * Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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

package net.dv8tion.jda.core.requests.restaction;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.Helpers;
import okhttp3.RequestBody;
import org.json.JSONObject;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * {@link net.dv8tion.jda.core.requests.RestAction RestAction} extension
 * specifically designed to allow bots to add {@link net.dv8tion.jda.core.entities.User Users} to Guilds.
 * <br>This requires an <b>OAuth2 Access Token</b> with the scope {@code guilds.join} to work!
 *
 * @since  3.7.0
 *
 * @see    <a href="https://discordapp.com/developers/docs/topics/oauth2" target="_blank">Discord OAuth2 Documentation</a>
 */
public class MemberAction extends RestAction<Void>
{
    private final String accessToken;
    private final String userId;
    private final Guild guild;

    private String nick;
    private Set<Role> roles;
    private boolean mute;
    private boolean deaf;

    public MemberAction(JDA api, Guild guild, String userId, String accessToken)
    {
        super(api, Route.Guilds.ADD_MEMBER.compile(guild.getId(), userId));
        this.accessToken = accessToken;
        this.userId = userId;
        this.guild = guild;
    }

    /**
     * The access token
     *
     * @return The access token
     */
    @Nonnull
    public String getAccessToken()
    {
        return accessToken;
    }

    /**
     * The id of the user who will be added by this task
     *
     * @return The id of the user
     */
    @Nonnull
    public String getUserId()
    {
        return userId;
    }

    /**
     * The user associated with the id
     *
     * @return Possibly-null user associated with the id
     */
    @Nullable
    public User getUser()
    {
        return getJDA().getUserById(userId);
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.Guild Guild} to which the
     * user will be added.
     *
     * @return The Guild
     */
    @Nonnull
    public Guild getGuild()
    {
        return guild;
    }

    /**
     * Sets the nickname of the user for the guild.
     * <br>This will then be visible with {@link net.dv8tion.jda.core.entities.Member#getNickname() Member.getNickname()}.
     *
     * @param  nick
     *         The nickname, or {@code null}
     *
     * @throws IllegalArgumentException
     *         If the provided nickname is longer than 32 characters
     *
     * @return The current MemberAction for chaining
     */
    @CheckReturnValue
    public MemberAction setNickname(String nick)
    {
        if (nick != null)
        {
            if (Helpers.isBlank(nick))
            {
                this.nick = null;
                return this;
            }
            Checks.check(nick.length() <= 32, "Nickname must not be greater than 32 characters in length");
        }
        this.nick = nick;
        return this;
    }

    /**
     * Sets the roles of the user for the guild.
     * <br>This will then be visible with {@link net.dv8tion.jda.core.entities.Member#getRoles() Member.getRoles()}.
     *
     * @param  roles
     *         The roles, or {@code null}
     *
     * @throws IllegalArgumentException
     *         If one of the provided roles is null or not from the same guild
     *
     * @return The current MemberAction for chaining
     */
    @CheckReturnValue
    public MemberAction setRoles(Collection<Role> roles)
    {
        if (roles == null)
        {
            this.roles = null;
            return this;
        }
        Set<Role> newRoles = new HashSet<>(roles.size());
        for (Role role : roles)
            checkAndAdd(newRoles, role);
        this.roles = newRoles;
        return this;
    }

    /**
     * Sets the roles of the user for the guild.
     * <br>This will then be visible with {@link net.dv8tion.jda.core.entities.Member#getRoles() Member.getRoles()}.
     *
     * @param  roles
     *         The roles, or {@code null}
     *
     * @throws IllegalArgumentException
     *         If one of the provided roles is null or not from the same guild
     *
     * @return The current MemberAction for chaining
     */
    @CheckReturnValue
    public MemberAction setRoles(Role... roles)
    {
        if (roles == null)
        {
            this.roles = null;
            return this;
        }
        Set<Role> newRoles = new HashSet<>(roles.length);
        for (Role role : roles)
            checkAndAdd(newRoles, role);
        this.roles = newRoles;
        return this;
    }

    /**
     * Whether the user should be voice muted in the guild.
     * <br>Default: {@code false}
     *
     * @param  mute
     *         Whether the user should be voice muted in the guild.
     *
     * @return The current MemberAction for chaining
     */
    @CheckReturnValue
    public MemberAction setMute(boolean mute)
    {
        this.mute = mute;
        return this;
    }

    /**
     * Whether the user should be voice deafened in the guild.
     * <br>Default: {@code false}
     *
     * @param  deaf
     *         Whether the user should be voice deafened in the guild.
     *
     * @return The current MemberAction for chaining
     */
    @CheckReturnValue
    public MemberAction setDeafen(boolean deaf)
    {
        this.deaf = deaf;
        return this;
    }

    @Override
    protected RequestBody finalizeData()
    {
        JSONObject obj = new JSONObject();
        obj.put("access_token", accessToken);
        if (nick != null)
            obj.put("nick", nick);
        if (roles != null && !roles.isEmpty())
            obj.put("roles", roles.stream().map(Role::getId).collect(Collectors.toList()));
        obj.put("mute", mute);
        obj.put("deaf", deaf);
        return getRequestBody(obj);
    }

    @Override
    protected void handleResponse(Response response, Request<Void> request)
    {
        if (response.isOk())
            request.onSuccess(null);
        else
            request.onFailure(response);
        /*
        This is not very useful but here is the response:
         {
            "nick": null,
            "user":
            {
                "username": "Minn",
                "discriminator": "6688",
                "id": "86699011792191488",
                "avatar": "e6376ed75fa54ffbe5134c3ec965458e"
            },
            "roles": [],
            "mute": false,
            "deaf": false,
            "joined_at": "2018-05-05T10:18:16.475626+00:00"
         }
        */
    }

    private void checkAndAdd(Set<Role> newRoles, Role role)
    {
        Checks.notNull(role, "Role");
        Checks.check(role.getGuild().equals(getGuild()), "Roles must all be from the same guild");
        newRoles.add(role);
    }
}
