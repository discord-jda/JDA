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
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.AuditLogEntry;
import net.dv8tion.jda.core.entities.EntityBuilder;
import net.dv8tion.jda.core.entities.Guild;
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
 *     <li>{@link net.dv8tion.jda.core.requests.Route.Guilds#GET_AUDIT_LOGS Route.Guilds.GET_AUDIT_LOGS}</li>
 *     <li>{@link net.dv8tion.jda.core.requests.Route.Guilds#GET_AUDIT_LOGS_BEFORE Route.Guilds.GET_AUDIT_LOGS_BEFORE}</li>
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

    public AuditLogPaginationAction(Guild guild)
    {
        super(guild.getJDA(), 1, 100, 100);
        if (!guild.getSelfMember().hasPermission(Permission.VIEW_AUDIT_LOGS))
            throw new PermissionException(Permission.VIEW_AUDIT_LOGS);
        this.guild = guild;
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

        if (last == null)
            super.route = Route.Guilds.GET_AUDIT_LOGS.compile(id, limit);
        else
            super.route = Route.Guilds.GET_AUDIT_LOGS_BEFORE.compile(id, limit, last.getId());
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
