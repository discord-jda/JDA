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

package net.dv8tion.jda.api.requests.restaction.pagination;

import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * {@link PaginationAction PaginationAction} that paginates the audit logs endpoint.
 * <br>Note that this implementation is not considered thread-safe as modifications to the cache are not done
 * with a lock. Calling methods on this class from multiple threads is not recommended.
 *
 * <p><b>Must provide not-null {@link net.dv8tion.jda.api.entities.Guild Guild} to compile a valid guild audit logs
 * pagination route</b>
 *
 * <p><b>Limits</b><br>
 * Minimum - 1
 * <br>Maximum - 100
 *
 * <p><b>Example</b><br>
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
 *         AuditLogPaginationAction auditLogs = event.getGuild().retrieveAuditLogs();
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
 *
 * @see    Guild#retrieveAuditLogs()
 */
public interface AuditLogPaginationAction extends PaginationAction<AuditLogEntry, AuditLogPaginationAction>
{
    /**
     * The current target {@link net.dv8tion.jda.api.entities.Guild Guild} for
     * this AuditLogPaginationAction.
     *
     * @return The never-null target Guild
     */
    @Nonnull
    Guild getGuild();

    /**
     * Filters retrieved entities by the specified {@link net.dv8tion.jda.api.audit.ActionType ActionType}
     *
     * @param  type
     *         {@link net.dv8tion.jda.api.audit.ActionType ActionType} used to filter,
     *         or {@code null} to remove type filtering
     *
     * @return The current AuditLogPaginationAction for chaining convenience
     */
    @Nonnull
    AuditLogPaginationAction type(@Nullable ActionType type);

    /**
     * Filters retrieved entities by the specified {@link UserSnowflake}.
     * <br>This specified the action issuer and not the target of an action. (Targets need not be users)
     *
     * @param  user
     *         The {@link UserSnowflake} used to filter or {@code null} to remove user filtering.
     *         This can be a member or user instance or {@link User#fromId(long)}.
     *
     * @return The current AuditLogPaginationAction for chaining convenience
     */
    @Nonnull
    AuditLogPaginationAction user(@Nullable UserSnowflake user);
}
