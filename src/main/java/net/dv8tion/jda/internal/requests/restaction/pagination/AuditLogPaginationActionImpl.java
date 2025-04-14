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

package net.dv8tion.jda.internal.requests.restaction.pagination;

import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.exceptions.ParsingException;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.requests.restaction.pagination.AuditLogPaginationAction;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.ToLongFunction;

public class AuditLogPaginationActionImpl
    extends PaginationActionImpl<AuditLogEntry, AuditLogPaginationAction>
    implements AuditLogPaginationAction
{
    protected final Guild guild;
    // filters
    protected ActionType type = null;
    protected String userId = null;

    public AuditLogPaginationActionImpl(Guild guild)
    {
        super(guild.getJDA(), Route.Guilds.GET_AUDIT_LOGS.compile(guild.getId()), 1, 100, 100);
        if (!guild.getSelfMember().hasPermission(Permission.VIEW_AUDIT_LOGS))
            throw new InsufficientPermissionException(guild, Permission.VIEW_AUDIT_LOGS);
        this.guild = guild;
        super.order(PaginationOrder.BACKWARD);
    }

    @Nonnull
    @Override
    public AuditLogPaginationActionImpl type(ActionType type)
    {
        this.type = type;
        return this;
    }

    @Nonnull
    @Override
    public AuditLogPaginationActionImpl user(UserSnowflake user)
    {
        this.userId = user == null ? null : user.getId();
        return this;
    }

    @Nonnull
    @Override
    public Guild getGuild()
    {
        return guild;
    }

    @Nonnull
    @Override
    public EnumSet<PaginationOrder> getSupportedOrders()
    {
        return EnumSet.of(PaginationOrder.BACKWARD, PaginationOrder.FORWARD);
    }

    @Override
    protected Route.CompiledRoute finalizeRoute()
    {
        Route.CompiledRoute route = super.finalizeRoute();

        if (type != null)
            route = route.withQueryParams("action_type", String.valueOf(type.getKey()));

        if (userId != null)
            route = route.withQueryParams("user_id", userId);

        return route;
    }

    @Override
    protected void handleSuccess(Response response, Request<List<AuditLogEntry>> request)
    {
        DataObject obj = response.getObject();
        DataArray entries = obj.getArray("audit_log_entries");
        DataArray users = obj.getArray("users");
        DataArray webhooks = obj.getArray("webhooks");
        DataArray threads = obj.getArray("threads");
        DataArray events = obj.getArray("guild_scheduled_events");
        DataArray automodRules = obj.getArray("auto_moderation_rules");

        List<AuditLogEntry> list = new ArrayList<>(entries.length());
        EntityBuilder builder = api.getEntityBuilder();

        ToLongFunction<DataObject> getIdFunction = dataObject -> dataObject.getLong("id");
        TLongObjectMap<DataObject> userMap = Helpers.convertToMap(getIdFunction, users);
        TLongObjectMap<DataObject> webhookMap = Helpers.convertToMap(getIdFunction, webhooks);
        TLongObjectMap<DataObject> threadMap = Helpers.convertToMap(getIdFunction, threads);
        TLongObjectMap<DataObject> eventMap = Helpers.convertToMap(getIdFunction, events);
        TLongObjectMap<DataObject> automodRuleMap = Helpers.convertToMap(getIdFunction, automodRules);

        for (int i = 0; i < entries.length(); i++)
        {
            try
            {
                DataObject entry = entries.getObject(i);
                AuditLogEntry result = builder.createAuditLogEntry((GuildImpl) guild, entry, userMap, webhookMap, threadMap, eventMap, automodRuleMap);
                list.add(result);
            }
            catch (ParsingException | NullPointerException e)
            {
                LOG.warn("Encountered exception in AuditLogPagination", e);
            }
        }

        if (!list.isEmpty())
        {
            if (this.useCache)
                this.cached.addAll(list);
            this.last = list.get(list.size() - 1);
            this.lastKey = last.getIdLong();
        }

        request.onSuccess(list);
    }

    @Override
    protected long getKey(AuditLogEntry it)
    {
        return it.getIdLong();
    }
}
