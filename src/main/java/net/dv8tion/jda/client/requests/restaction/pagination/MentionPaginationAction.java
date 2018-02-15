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
import org.json.JSONException;

import java.util.LinkedList;
import java.util.List;

/**
 * {@link net.dv8tion.jda.core.requests.restaction.pagination.PaginationAction PaginationAction}
 * that paginates the endpoint {@link net.dv8tion.jda.core.requests.Route.Self#GET_RECENT_MENTIONS Route.Self.GET_RECENT_MENTIONS}.
 *
 * <p><b>Must provide not-null {@link net.dv8tion.jda.core.entities.Guild Guild} to compile a valid guild mentions
 * pagination route.</b>, else it uses the global pagination route.
 *
 * <h2>Limits:</h2>
 * Minimum - 1
 * <br>Maximum - 100
 *
 * <h1>Example</h1>
 * <pre><code>
 * MentionPaginationAction mentions = guild.getRecentMentions();
 * mentions.setEveryone(false);
 * for (Message message : mentions)
 * {
 *     System.out.printf("%#s: %s\n", message.getAuthor(), message.getContent());
 * }
 * </code></pre>
 *
 * @since  3.0
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
        this(api, null);
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
        this(guild.getJDA(), guild);
    }

    private MentionPaginationAction(JDA api, Guild guild)
    {
        super(api, Route.Self.GET_RECENT_MENTIONS.compile(), 1, 100, 100);

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
    protected Route.CompiledRoute finalizeRoute()
    {
        Route.CompiledRoute route = super.finalizeRoute();

        String limit, before, everyone, role;
        limit = String.valueOf(super.getLimit());
        before = last != null ? last.getId() : null;
        everyone = String.valueOf(isEveryone);
        role = String.valueOf(isRole);

        route = route.withQueryParams("limit", limit, "roles", role, "everyone", everyone);

        if (guild != null)
            route = route.withQueryParams("guild_id", guild.getId());

        if (before != null)
            route = route.withQueryParams("before", before);

        return route;
    }

    @Override
    protected void handleResponse(Response response, Request<List<Message>> request)
    {
        if(!response.isOk())
        {
            request.onFailure(response);
            return;
        }

        EntityBuilder builder = api.getEntityBuilder();;
        List<Message> mentions = new LinkedList<>();
        JSONArray arr = response.getArray();
        for (int i = 0; i < arr.length(); i++)
        {
            try
            {
                final Message msg = builder.createMessage(arr.getJSONObject(i), false);
                mentions.add(msg);
                if (useCache)
                    cached.add(msg);
                last = msg;
            }
            catch (JSONException | NullPointerException e)
            {
                LOG.warn("Encountered exception in MentionPagination", e);
            }
        }

        request.onSuccess(mentions);
    }

}
