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

package net.dv8tion.jda.api.requests.restaction;

import net.dv8tion.jda.api.audit.ThreadLocalReason;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.pagination.AuditLogPaginationAction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

/**
 * Extension of RestAction to allow setting a reason.
 *
 * <p>This will automatically use the {@link net.dv8tion.jda.api.audit.ThreadLocalReason ThreadLocalReason} if no
 * reason was specified via {@link #reason(String)}.
 *
 * @param  <T>
 *         The return type
 *
 * @since  3.3.0
 */
public interface AuditableRestAction<T> extends RestAction<T>
{
    /**
     * The maximum length of an audit-log reason
     */
    int MAX_REASON_LENGTH = 512;

    /**
     * Applies the specified reason as audit-log reason field.
     * <br>When the provided reason is empty or {@code null} it will be treated as not set.
     * If the provided reason is longer than {@value #MAX_REASON_LENGTH} characters, it will be truncated to fit the limit.
     *
     * <p>Reasons for any AuditableRestAction may be retrieved
     * via {@link net.dv8tion.jda.api.audit.AuditLogEntry#getReason() AuditLogEntry.getReason()}
     * in iterable {@link AuditLogPaginationAction AuditLogPaginationActions}
     * from {@link net.dv8tion.jda.api.entities.Guild#retrieveAuditLogs() Guild.retrieveAuditLogs()}!
     * For {@link net.dv8tion.jda.api.entities.Guild#ban(UserSnowflake, int, TimeUnit) guild bans}, this is also accessible via {@link Guild.Ban#getReason()}.
     *
     * <p>This will specify the reason via the {@code X-Audit-Log-Reason} Request Header.
     *
     * @param  reason
     *         The reason for this action which should be logged in the Guild's AuditLogs (up to {@value #MAX_REASON_LENGTH} characters)
     *
     * @return The current AuditableRestAction instance for chaining convenience
     *
     * @see    ThreadLocalReason
     */
    @Nonnull
    AuditableRestAction<T> reason(@Nullable String reason);

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    AuditableRestAction<T> setCheck(@Nullable BooleanSupplier checks);

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    default AuditableRestAction<T> timeout(long timeout, @Nonnull TimeUnit unit)
    {
        return (AuditableRestAction<T>) RestAction.super.timeout(timeout, unit);
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    default AuditableRestAction<T> deadline(long timestamp)
    {
        return (AuditableRestAction<T>) RestAction.super.deadline(timestamp);
    }
}
