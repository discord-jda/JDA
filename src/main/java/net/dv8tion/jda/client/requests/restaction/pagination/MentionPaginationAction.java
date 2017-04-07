/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
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

package net.dv8tion.jda.client.requests.restaction.pagination;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.EntityBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.requests.restaction.pagination.PaginationAction;
import org.json.JSONArray;

import java.util.LinkedList;
import java.util.List;

/**
 * {@link net.dv8tion.jda.core.requests.restaction.pagination.PaginationAction PaginationAction}
 * that paginates the endpoints:
 * <ul>
 *     <li>{@link net.dv8tion.jda.core.requests.Route.Self#GET_RECENT_MENTIONS Route.Self.GET_RECENT_MENTIONS}</li>
 *     <li>{@link net.dv8tion.jda.core.requests.Route.Self#GET_RECENT_MENTIONS_BEFORE Route.Self.GET_RECENT_MENTIONS_BEFORE}</li>
 *     <li>{@link net.dv8tion.jda.core.requests.Route.Self#GET_RECENT_MENTIONS_GUILD Route.Self.GET_RECENT_MENTIONS_GUILD}</li>
 *     <li>{@link net.dv8tion.jda.core.requests.Route.Self#GET_RECENT_MENTIONS_GUILD_BEFORE Route.Self.GET_RECENT_MENTIONS_GUILD_BEFORE}</li>
 * </ul>
 *
 * <p><b>Must provide not-null {@link net.dv8tion.jda.core.entities.Guild Guild} to compile a valid guild mentions
 * pagination route.</b>, else it uses the global pagination route.
 *
 * @since  3.1
 * @author Florian Spieß
 */
public class MentionPaginationAction extends PaginationAction<Message, MentionPaginationAction>
{

    protected final Guild guild;

    protected boolean isEveryone = true;
    protected boolean isRole = true;

    /**
     * Creates a new MentionPaginationAction
     * <br>This constructor effectively makes this target all recent mentions
     * to get the recent mentions for a specific {@link net.dv8tion.jda.core.entities.Guild Guild}
     * use {@link #MentionPaginationAction(net.dv8tion.jda.core.entities.Guild)} instead!
     *
     * @param api
     *        The current JDA entity
     */
    public MentionPaginationAction(JDA api)
    {
        super(api, 1, 100, 25);
        this.guild = null;
    }

    /**
     * Creates a new MentionPaginationAction
     * <br>This constructor effectively makes this target specifically only
     * the recent mentions for the specified {@link net.dv8tion.jda.core.entities.Guild Guild}!
     * <br>To get the global scope use {@link #MentionPaginationAction(net.dv8tion.jda.core.JDA)} instead.
     *
     * @param guild
     *        The Non-Null target {@link net.dv8tion.jda.core.entities.Guild Guild}
     *
     * @throws java.lang.NullPointerException
     *         If the provided {@code guild} is {@code null}
     */
    public MentionPaginationAction(Guild guild)
    {
        super(guild.getJDA(), 1, 100, 25);
        this.guild = guild;
    }

    /**
     * The current target {@link net.dv8tion.jda.core.entities.Guild Guild} for
     * this MentionPaginationAction.
     * <br>This can be {@code null} if this MentionPaginationAction does not target
     * mentions from a specific Guild!
     *
     * @return Possibly-null target Guild
     */
    public Guild getGuild()
    {
        return guild;
    }

    /**
     * Sets whether this MentionPaginationAction should
     * include mentions that mention the public role of a Guild.
     * <br>Default: {@code true}
     *
     * @param  isEveryoneMention
     *         Whether to include everyone mentions
     *
     * @return The current MentionPaginationAction for chaining convenience
     */
    public MentionPaginationAction setEveryone(boolean isEveryoneMention)
    {
        this.isEveryone = isEveryoneMention;
        return this;
    }

    /**
     * Sets whether this MentionPaginationAction should
     * include mentions that mention a role in a Guild.
     * <br>Default: {@code true}
     *
     * @param  isRoleMention
     *         Whether to include role mentions
     *
     * @return The current MentionPaginationAction for chaining convenience
     */
    public MentionPaginationAction setRole(boolean isRoleMention)
    {
        this.isRole = isRoleMention;
        return this;
    }

    @Override
    protected void finalizeRoute()
    {
        String limit, before, everyone, role;
        limit = String.valueOf(super.getLimit());
        before = cached.isEmpty() ? null : getLast().getId();
        everyone = String.valueOf(isEveryone);
        role = String.valueOf(isRole);

        if (guild != null)
            route = prepareGuild(limit, before, everyone, role);
        else
            route = prepareGlobal(limit, before, everyone, role);
    }

    @Override
    protected void handleResponse(Response response, Request request)
    {
        if(!response.isOk())
        {
            request.onFailure(response);
            return;
        }

        EntityBuilder builder = EntityBuilder.get(api);
        List<Message> mentions = new LinkedList<>();
        JSONArray arr = response.getArray();
        for (int i = 0; i < arr.length(); i++)
        {
            final Message msg = builder.createMessage(arr.getJSONObject(i), false);
            cached.add(msg);
            mentions.add(msg);
        }

        request.onSuccess(mentions);
    }

    protected Route.CompiledRoute prepareGuild(String limit, String before, String everyone, String role)
    {
        if (before != null)
            return Route.Self.GET_RECENT_MENTIONS_GUILD_BEFORE.compile(limit, role, everyone, guild.getId(), before);
        else
            return Route.Self.GET_RECENT_MENTIONS_GUILD.compile(limit, role, everyone, guild.getId());
    }

    protected Route.CompiledRoute prepareGlobal(String limit, String before, String everyone, String role)
    {
        if (before != null)
            return Route.Self.GET_RECENT_MENTIONS_BEFORE.compile(limit, role, everyone, before);
        else
            return Route.Self.GET_RECENT_MENTIONS.compile(limit, role, everyone);
    }

}
