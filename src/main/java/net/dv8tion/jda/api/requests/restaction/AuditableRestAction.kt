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
package net.dv8tion.jda.api.requests.restaction

import net.dv8tion.jda.api.audit.ThreadLocalReason
import net.dv8tion.jda.api.requests.RestAction
import java.util.concurrent.*
import java.util.function.BooleanSupplier
import javax.annotation.Nonnull

/**
 * Extension of RestAction to allow setting a reason.
 *
 *
 * This will automatically use the [ThreadLocalReason][net.dv8tion.jda.api.audit.ThreadLocalReason] if no
 * reason was specified via [.reason].
 *
 * @param  <T>
 * The return type
 *
 * @since  3.3.0
</T> */
interface AuditableRestAction<T> : RestAction<T?> {
    /**
     * Applies the specified reason as audit-log reason field.
     * <br></br>When the provided reason is empty or `null` it will be treated as not set.
     * If the provided reason is longer than {@value #MAX_REASON_LENGTH} characters, it will be truncated to fit the limit.
     *
     *
     * Reasons for any AuditableRestAction may be retrieved
     * via [AuditLogEntry.getReason()][net.dv8tion.jda.api.audit.AuditLogEntry.getReason]
     * in iterable [AuditLogPaginationActions][AuditLogPaginationAction]
     * from [Guild.retrieveAuditLogs()][net.dv8tion.jda.api.entities.Guild.retrieveAuditLogs]!
     * For [guild bans][net.dv8tion.jda.api.entities.Guild.ban], this is also accessible via [Guild.Ban.getReason].
     *
     *
     * This will specify the reason via the `X-Audit-Log-Reason` Request Header.
     *
     * @param  reason
     * The reason for this action which should be logged in the Guild's AuditLogs (up to {@value #MAX_REASON_LENGTH} characters)
     *
     * @return The current AuditableRestAction instance for chaining convenience
     *
     * @see ThreadLocalReason
     */
    @Nonnull
    fun reason(reason: String?): AuditableRestAction<T>?

    /**
     * {@inheritDoc}
     */
    @Nonnull
    override fun setCheck(checks: BooleanSupplier?): AuditableRestAction<T?>?

    /**
     * {@inheritDoc}
     */
    @Nonnull
    override fun timeout(timeout: Long, @Nonnull unit: TimeUnit): AuditableRestAction<T?>? {
        return super.timeout(timeout, unit) as AuditableRestAction<T?>
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    override fun deadline(timestamp: Long): AuditableRestAction<T?>? {
        return super.deadline(timestamp) as AuditableRestAction<T?>
    }

    companion object {
        /**
         * The maximum length of an audit-log reason
         */
        const val MAX_REASON_LENGTH = 512
    }
}
