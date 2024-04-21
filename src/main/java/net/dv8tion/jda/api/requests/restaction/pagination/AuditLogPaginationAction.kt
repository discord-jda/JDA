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
package net.dv8tion.jda.api.requests.restaction.pagination

import net.dv8tion.jda.api.audit.ActionType
import net.dv8tion.jda.api.audit.AuditLogEntry
import net.dv8tion.jda.api.entities.*
import javax.annotation.Nonnull

/**
 * [PaginationAction] that paginates the audit logs endpoint.
 * <br></br>Note that this implementation is not considered thread-safe as modifications to the cache are not done
 * with a lock. Calling methods on this class from multiple threads is not recommended.
 *
 *
 * **Must provide not-null [Guild][net.dv8tion.jda.api.entities.Guild] to compile a valid guild audit logs
 * pagination route**
 *
 *
 * **Limits**<br></br>
 * Minimum - 1
 * <br></br>Maximum - 100
 *
 *
 * **Example**<br></br>
 * <pre>`
 * public class Listener extends ListenerAdapter
 * {
 * @Override
 * public void onRoleCreate(RoleCreateEvent event)
 * {
 * List<TextChannel> channels = event.getGuild().getTextChannelsByName("logs", true);
 * if (channels.isEmpty()) return; // no log channel
 * TextChannel channel = channels.get(0); // get first match
 *
 * AuditLogPaginationAction auditLogs = event.getGuild().retrieveAuditLogs();
 * auditLogs.type(ActionType.ROLE_CREATE); // only take ROLE_CREATE type
 * auditLogs.limit(1); // take first
 * auditLogs.queue( (entries) ->
 * {
 * // callback has a list, this may be empty due to race conditions
 * if (entries.isEmpty()) return;
 * AuditLogEntry entry = entries.get(0);
 * channel.sendMessageFormat("A role has been updated by %#s!", entry.getUser()).queue();
 * });
 * }
 * }
`</pre> *
 *
 * @since  3.2
 *
 * @see Guild.retrieveAuditLogs
 */
interface AuditLogPaginationAction : PaginationAction<AuditLogEntry?, AuditLogPaginationAction?> {
    @get:Nonnull
    val guild: Guild?

    /**
     * Filters retrieved entities by the specified [ActionType][net.dv8tion.jda.api.audit.ActionType]
     *
     * @param  type
     * [ActionType][net.dv8tion.jda.api.audit.ActionType] used to filter,
     * or `null` to remove type filtering
     *
     * @return The current AuditLogPaginationAction for chaining convenience
     */
    @Nonnull
    fun type(type: ActionType?): AuditLogPaginationAction?

    /**
     * Filters retrieved entities by the specified [UserSnowflake].
     * <br></br>This specified the action issuer and not the target of an action. (Targets need not be users)
     *
     * @param  user
     * The [UserSnowflake] used to filter or `null` to remove user filtering.
     * This can be a member or user instance or [User.fromId].
     *
     * @return The current AuditLogPaginationAction for chaining convenience
     */
    @Nonnull
    fun user(user: UserSnowflake?): AuditLogPaginationAction?
}
