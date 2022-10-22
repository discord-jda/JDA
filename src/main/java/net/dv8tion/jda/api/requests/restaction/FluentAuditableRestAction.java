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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

/**
 * Interface used to mixin the customization parameters for {@link AuditableRestAction AuditableRestActions}.
 * <br>This simply fixes the return types to be the concrete implementation instead of the base interface.
 *
 * @param <T>
 *        The result type of the AuditableRestAction
 * @param <R>
 *        The concrete AuditableRestAction type used for chaining (fluent interface)
 */
@SuppressWarnings("unchecked")
public interface FluentAuditableRestAction<T, R extends FluentAuditableRestAction<T, R>> extends AuditableRestAction<T>
{
    @Nonnull
    @Override
    R reason(@Nullable String reason);

    @Nonnull
    @Override
    R setCheck(@Nullable BooleanSupplier checks);

    @Nonnull
    @Override
    default R addCheck(@Nonnull BooleanSupplier checks)
    {
        return (R) AuditableRestAction.super.addCheck(checks);
    }

    @Nonnull
    @Override
    default R timeout(long timeout, @Nonnull TimeUnit unit)
    {
        return (R) AuditableRestAction.super.timeout(timeout, unit);
    }

    @Nonnull
    @Override
    default R deadline(long timestamp)
    {
        return (R) AuditableRestAction.super.deadline(timestamp);
    }
}
