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
import okhttp3.RequestBody;
import org.json.JSONObject;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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

    public String getAccessToken()
    {
        return accessToken;
    }

    public String getUserId()
    {
        return userId;
    }

    @Nullable
    public User getUser()
    {
        return getJDA().getUserById(userId);
    }

    public Guild getGuild()
    {
        return guild;
    }

    public MemberAction setNick(String nick)
    {
        this.nick = nick;
        return this;
    }

    public MemberAction setRoles(Collection<Role> roles)
    {
        this.roles = new HashSet<>(roles);
        return this;
    }

    public MemberAction setRoles(Role... roles)
    {
        this.roles = new HashSet<>();
        Collections.addAll(this.roles, roles);
        return this;
    }

    public MemberAction setMute(boolean mute)
    {
        this.mute = mute;
        return this;
    }

    public MemberAction setDeaf(boolean deaf)
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
        /* This is not very useful but here is the response:
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
        if (response.isOk())
            request.onSuccess(null);
        else
            request.onFailure(response);
    }
}
