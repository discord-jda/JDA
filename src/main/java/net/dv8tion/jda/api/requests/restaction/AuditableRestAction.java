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
import net.dv8tion.jda.api.utils.Result;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.*;

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

    @Nonnull
    @Override
    default <O> AuditableRestAction<O> map(@Nonnull Function<? super T, ? extends O> map)
    {
        return (AuditableRestAction<O>) RestAction.super.map(map);
    }

    @Nonnull
    @Override
    default <O> AuditableRestAction<O> flatMap(@Nonnull Function<? super T, ? extends RestAction<O>> flatMap)
    {
        return (AuditableRestAction<O>) RestAction.super.flatMap(flatMap);
    }

    @Nonnull
    @Override
    default <O> AuditableRestAction<O> flatMap(@Nullable Predicate<? super T> condition, @Nonnull Function<? super T, ? extends RestAction<O>> flatMap)
    {
        return (AuditableRestAction<O>) RestAction.super.flatMap(condition, flatMap);
    }

    @Nonnull
    @Override
    default AuditableRestAction<Result<T>> mapToResult()
    {
        return (AuditableRestAction<Result<T>>) RestAction.super.mapToResult();
    }

    @Nonnull
    @Override
    default AuditableRestAction<T> onSuccess(@Nonnull Consumer<? super T> consumer)
    {
        return (AuditableRestAction<T>) RestAction.super.onSuccess(consumer);
    }

    @Nonnull
    @Override
    default AuditableRestAction<T> onErrorMap(@Nonnull Function<? super Throwable, ? extends T> map)
    {
        return (AuditableRestAction<T>) RestAction.super.onErrorMap(map);
    }

    @Nonnull
    @Override
    default AuditableRestAction<T> onErrorMap(@Nullable Predicate<? super Throwable> condition, @Nonnull Function<? super Throwable, ? extends T> map)
    {
        return (AuditableRestAction<T>) RestAction.super.onErrorMap(condition, map);
    }

    @Nonnull
    @Override
    default AuditableRestAction<T> onErrorFlatMap(@Nonnull Function<? super Throwable, ? extends RestAction<? extends T>> map)
    {
        return (AuditableRestAction<T>) RestAction.super.onErrorFlatMap(map);
    }

    @Nonnull
    @Override
    default AuditableRestAction<T> onErrorFlatMap(@Nullable Predicate<? super Throwable> condition, @Nonnull Function<? super Throwable, ? extends RestAction<? extends T>> map)
    {
        return (AuditableRestAction<T>) RestAction.super.onErrorFlatMap(condition, map);
    }

    @Nonnull
    @Override
    default AuditableRestAction<T> delay(@Nonnull Duration duration)
    {
        return (AuditableRestAction<T>) RestAction.super.delay(duration);
    }

    @Nonnull
    @Override
    default AuditableRestAction<T> delay(@Nonnull Duration duration, @Nullable ScheduledExecutorService scheduler)
    {
        return (AuditableRestAction<T>) RestAction.super.delay(duration, scheduler);
    }

    @Nonnull
    @Override
    default AuditableRestAction<T> delay(long delay, @Nonnull TimeUnit unit)
    {
        return (AuditableRestAction<T>) RestAction.super.delay(delay, unit);
    }

    @Nonnull
    @Override
    default AuditableRestAction<T> delay(long delay, @Nonnull TimeUnit unit, @Nullable ScheduledExecutorService scheduler)
    {
        return (AuditableRestAction<T>) RestAction.super.delay(delay, unit, scheduler);
    }
}
