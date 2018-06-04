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

package net.dv8tion.jda.core.requests.restaction.pagination;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.audit.ActionType;
import net.dv8tion.jda.core.audit.AuditLogEntry;
import net.dv8tion.jda.core.entities.EntityBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.impl.GuildImpl;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.Helpers;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link net.dv8tion.jda.core.requests.restaction.pagination.PaginationAction PaginationAction}
 * that paginates the endpoint {@link net.dv8tion.jda.core.requests.Route.Guilds#GET_AUDIT_LOGS Route.Guilds.GET_AUDIT_LOGS}.
 *
 * <p><b>Must provide not-null {@link net.dv8tion.jda.core.entities.Guild Guild} to compile a valid guild audit logs
 * pagination route</b>
 *
 * <h2>Limits</h2>
 * Minimum - 1
 * <br>Maximum - 100
 *
 * <h1>Example</h1>
 * <pre><code>
 * public class Listener extends ListenerAdapter
 * {
 *     {@literal @Override}
 *     public void onRoleCreate(RoleCreateEvent event)
 *     {
 *         {@literal List<TextChannel>} channels = event.getGuild().getTextChannelsByName("logs", true);
 *         if (channels.isEmpty()) return; // no log channel
 *         TextChannel channel = channels.get(0); // get first match
 *
 *         AuditLogPaginationAction auditLogs = event.getGuild().getAuditLogs();
 *         auditLogs.type(ActionType.ROLE_CREATE); // only take ROLE_CREATE type
 *         auditLogs.limit(1); // take first
 *         auditLogs.queue( (entries) {@literal ->}
 *         {
 *             // callback has a list, this may be empty due to race conditions
 *             if (entries.isEmpty()) return;
 *             AuditLogEntry entry = entries.get(0);
 *             channel.sendMessageFormat("A role has been updated by %#s!", entry.getUser()).queue();
 *         });
 *     }
 * }
 * </code></pre>
 *
 * @since  3.2
 */
public class AuditLogPaginationAction extends PaginationAction<AuditLogEntry, AuditLogPaginationAction>
{
    protected final Guild guild;
    // filters
    protected ActionType type = null;
    protected String userId = null;

    public AuditLogPaginationAction(Guild guild)
    {
        super(guild.getJDA(), Route.Guilds.GET_AUDIT_LOGS.compile(guild.getId()), 1, 100, 100);
        if (!guild.getSelfMember().hasPermission(Permission.VIEW_AUDIT_LOGS))
            throw new InsufficientPermissionException(Permission.VIEW_AUDIT_LOGS);
        this.guild = guild;
    }

    /**
     * Filters retrieved entities by the specified {@link net.dv8tion.jda.core.audit.ActionType ActionType}
     *
     * @param  type
     *         {@link net.dv8tion.jda.core.audit.ActionType ActionType} used to filter,
     *         or {@code null} to remove type filtering
     *
     * @return The current AuditLogPaginationAction for chaining convenience
     */
    public AuditLogPaginationAction type(ActionType type)
    {
        this.type = type;
        return this;
    }

    /**
     * Filters retrieved entities by the specified {@link net.dv8tion.jda.core.entities.User User}.
     * <br>This specified the action issuer and not the target of an action. (Targets need not be users)
     *
     * @param  user
     *         {@link net.dv8tion.jda.core.entities.User User} used to filter,
     *         or {@code null} to remove user filtering
     *
     * @return The current AuditLogPaginationAction for chaining convenience
     */
    public AuditLogPaginationAction user(User user)
    {
        return user(user == null ? null : user.getId());
    }

    /**
     * Filters retrieved entities by the specified {@link net.dv8tion.jda.core.entities.User User} id.
     * <br>This specified the action issuer and not the target of an action. (Targets need not be users)
     *
     * @param  userId
     *         {@link net.dv8tion.jda.core.entities.User User} id used to filter,
     *         or {@code null} to remove user filtering
     *
     * @throws IllegalArgumentException
     *         If the provided userId is not valid
     *
     * @return The current AuditLogPaginationAction for chaining convenience
     */
    public AuditLogPaginationAction user(String userId)
    {
        Checks.isSnowflake(userId, "User ID");
        this.userId = userId;
        return this;
    }

    /**
     * Filters retrieved entities by the specified {@link net.dv8tion.jda.core.entities.User User} id.
     *
     * @param  userId
     *         {@link net.dv8tion.jda.core.entities.User User} id used to filter,
     *         or {@code null} to remove user filtering
     *
     * @return The current AuditLogPaginationAction for chaining convenience
     */
    public AuditLogPaginationAction user(long userId)
    {
        return user(Long.toUnsignedString(userId));
    }

    /**
     * The current target {@link net.dv8tion.jda.core.entities.Guild Guild} for
     * this AuditLogPaginationAction.
     *
     * @return The never-null target Guild
     */
    public Guild getGuild()
    {
        return guild;
    }

    @Override
    protected Route.CompiledRoute finalizeRoute()
    {
        Route.CompiledRoute route = super.finalizeRoute();

        final String limit = String.valueOf(this.limit.get());
        final AuditLogEntry last = this.last;

        route = route.withQueryParams("limit", limit);

        if (type != null)
            route = route.withQueryParams("action_type", String.valueOf(type.getKey()));

        if (userId != null)
            route = route.withQueryParams("user_id", userId);

        if (last != null)
            route = route.withQueryParams("before", last.getId());

        return route;
    }

    @Override
    protected void handleResponse(Response response, Request<List<AuditLogEntry>> request)
    {
        if (!response.isOk())
        {
            request.onFailure(response);
            return;
        }

        JSONObject obj = response.getObject();
        JSONArray users = obj.getJSONArray("users");
        JSONArray webhooks = obj.getJSONArray("webhooks");
        JSONArray entries = obj.getJSONArray("audit_log_entries");

        List<AuditLogEntry> list = new ArrayList<>(entries.length());
        EntityBuilder builder = api.getEntityBuilder();

        TLongObjectMap<JSONObject> userMap = new TLongObjectHashMap<>();
        for (int i = 0; i < users.length(); i++)
        {
            JSONObject user = users.getJSONObject(i);
            userMap.put(user.getLong("id"), user);
        }
        
        TLongObjectMap<JSONObject> webhookMap = new TLongObjectHashMap<>();
        for (int i = 0; i < webhooks.length(); i++)
        {
            JSONObject webhook = webhooks.getJSONObject(i);
            webhookMap.put(webhook.getLong("id"), webhook);
        }
        
        for (int i = 0; i < entries.length(); i++)
        {
            try
            {
                JSONObject entry = entries.getJSONObject(i);
                JSONObject user = userMap.get(Helpers.optLong(entry, "user_id", 0));
                JSONObject webhook = webhookMap.get(Helpers.optLong(entry, "target_id", 0));
                AuditLogEntry result = builder.createAuditLogEntry((GuildImpl) guild, entry, user, webhook);
                list.add(result);
                if (this.useCache)
                    this.cached.add(result);
                this.last = result;
            }
            catch (JSONException | NullPointerException e)
            {
                LOG.warn("Encountered exception in AuditLogPagination", e);
            }
        }

        request.onSuccess(list);
    }
}
