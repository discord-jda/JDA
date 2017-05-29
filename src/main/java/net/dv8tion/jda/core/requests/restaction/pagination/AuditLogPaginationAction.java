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

package net.dv8tion.jda.core.requests.restaction.pagination;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.dv8tion.jda.core.audit.ActionType;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.audit.AuditLogEntry;
import net.dv8tion.jda.core.entities.EntityBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.impl.GuildImpl;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.Route;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link net.dv8tion.jda.core.requests.restaction.pagination.PaginationAction PaginationAction}
 * that paginates the endpoints:
 * <ul>
 *     <li>{@link net.dv8tion.jda.core.requests.Route.AuditLogs#GET_AUDIT_LOGS Route.AuditLogs.GET_AUDIT_LOGS}</li>
 *     <li>{@link net.dv8tion.jda.core.requests.Route.AuditLogs#GET_AUDIT_LOGS_BEFORE Route.AuditLogs.GET_AUDIT_LOGS_BEFORE}</li>
 *     <li>{@link net.dv8tion.jda.core.requests.Route.AuditLogs#GET_AUDIT_LOGS_USER Route.AuditLogs.GET_AUDIT_LOGS_USER}</li>
 *     <li>{@link net.dv8tion.jda.core.requests.Route.AuditLogs#GET_AUDIT_LOGS_USER_BEFORE Route.AuditLogs.GET_AUDIT_LOGS_USER_BEFORE}</li>
 *     <li>{@link net.dv8tion.jda.core.requests.Route.AuditLogs#GET_AUDIT_LOGS_ACTION Route.AuditLogs.GET_AUDIT_LOGS_ACTION}</li>
 *     <li>{@link net.dv8tion.jda.core.requests.Route.AuditLogs#GET_AUDIT_LOGS_ACTION_BEFORE Route.AuditLogs.GET_AUDIT_LOGS_ACTION_BEFORE}</li>
 *     <li>{@link net.dv8tion.jda.core.requests.Route.AuditLogs#GET_AUDIT_LOGS_USER_ACTION Route.AuditLogs.GET_AUDIT_LOGS_USER_ACTION}</li>
 *     <li>{@link net.dv8tion.jda.core.requests.Route.AuditLogs#GET_AUDIT_LOGS_USER_ACTION_BEFORE Route.AuditLogs.GET_AUDIT_LOGS_USER_ACTION_BEFORE}</li>
 * </ul>
 *
 * <p><b>Must provide not-null {@link net.dv8tion.jda.core.entities.Guild Guild} to compile a valid guild audit logs
 * pagination route</b>
 *
 * <h2>Limits</h2>
 * Minimum - 1
 * <br>Maximum - 100
 *
 * @since  3.2
 * @author Florian Spieß
 */
public class AuditLogPaginationAction extends PaginationAction<AuditLogEntry, AuditLogPaginationAction>
{
    protected final Guild guild;
    // filters
    protected ActionType type = null;
    protected String userId = null;

    public AuditLogPaginationAction(Guild guild)
    {
        super(guild.getJDA(), 1, 100, 100);
        if (!guild.getSelfMember().hasPermission(Permission.VIEW_AUDIT_LOGS))
            throw new PermissionException(Permission.VIEW_AUDIT_LOGS);
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
     * Filters retrieved entities by the specified {@link net.dv8tion.jda.core.entities.User User}
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
     *
     * @param  userId
     *         {@link net.dv8tion.jda.core.entities.User User} id used to filter,
     *         or {@code null} to remove user filtering
     *
     * @return The current AuditLogPaginationAction for chaining convenience
     */
    public AuditLogPaginationAction user(String userId)
    {
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
    protected void finalizeRoute()
    {
        final String limit = String.valueOf(this.limit.get());
        final String id = guild.getId();
        final AuditLogEntry last = this.last;
        final boolean beforeSet = last != null;
        final boolean typeSet = type != null;
        final boolean userSet = userId != null;

        if (beforeSet)
            finalizeRouteBefore(typeSet, userSet, id, limit, last.getId());
        else
            finalizeRouteDefault(typeSet, userSet, id, limit);
    }

    private void finalizeRouteBefore(boolean typeSet, boolean userSet, String guildId, String limit, String before)
    {
        if (typeSet)
        {
            if (userSet)
                super.route = Route.AuditLogs.GET_AUDIT_LOGS_USER_ACTION_BEFORE.compile(guildId, limit, userId, String.valueOf(type.getKey()), before);
            else
                super.route = Route.AuditLogs.GET_AUDIT_LOGS_ACTION_BEFORE.compile(guildId, limit, String.valueOf(type.getKey()), before);
        }
        else
        {
            if (userSet)
                super.route = Route.AuditLogs.GET_AUDIT_LOGS_USER_BEFORE.compile(guildId, limit, userId, before);
            else
                super.route = Route.AuditLogs.GET_AUDIT_LOGS_BEFORE.compile(guildId, limit, before);
        }
    }

    private void finalizeRouteDefault(boolean typeSet, boolean userSet, String id, String limit)
    {
        if (typeSet)
        {
            if (userSet)
                super.route = Route.AuditLogs.GET_AUDIT_LOGS_USER_ACTION.compile(id, limit, userId, String.valueOf(type.getKey()));
            else
                super.route = Route.AuditLogs.GET_AUDIT_LOGS_ACTION.compile(id, limit, String.valueOf(type.getKey()));
        }
        else
        {
            if (userSet)
                super.route = Route.AuditLogs.GET_AUDIT_LOGS_USER.compile(id, limit, userId);
            else
                super.route = Route.AuditLogs.GET_AUDIT_LOGS.compile(id, limit);
        }
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
        JSONArray entries = obj.getJSONArray("audit_log_entries");

        List<AuditLogEntry> list = new ArrayList<>(entries.length());
        EntityBuilder builder = api.getEntityBuilder();

        TLongObjectMap<JSONObject> userMap = new TLongObjectHashMap<>();
        for (int i = 0; i < users.length(); i++)
        {
            JSONObject user = users.getJSONObject(i);
            userMap.put(user.getLong("id"), user);
        }
        for (int i = 0; i < entries.length(); i++)
        {
            JSONObject entry = entries.getJSONObject(i);
            JSONObject user  = userMap.get(entry.getLong("user_id"));
            AuditLogEntry result = builder.createAuditLogEntry((GuildImpl) guild, entry, user);
            list.add(result);
            if (this.useCache)
                this.cached.add(result);
            this.last = result;
        }

        request.onSuccess(list);
    }
}
