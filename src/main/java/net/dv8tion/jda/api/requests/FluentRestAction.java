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

package net.dv8tion.jda.api.requests;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

/**
 * Interface used to mixin the customization parameters for {@link RestAction RestActions}.
 * <br>This simply fixes the return types to be the concrete implementation instead of the base interface.
 *
 * @param <T>
 *        The result type of the RestAction
 * @param <R>
 *        The concrete RestAction type used for chaining (fluent interface)
 */
@SuppressWarnings("unchecked")
public interface FluentRestAction<T, R extends FluentRestAction<T, R>> extends RestAction<T>
{
    @Nonnull
    @Override
    R setCheck(@Nullable BooleanSupplier checks);

    @Nonnull
    @Override
    default R addCheck(@Nonnull BooleanSupplier checks)
    {
        return (R) RestAction.super.addCheck(checks);
    }

    @Nonnull
    @Override
    default R timeout(long timeout, @Nonnull TimeUnit unit)
    {
        return (R) RestAction.super.timeout(timeout, unit);
    }

    @Nonnull
    @Override
    default R deadline(long timestamp)
    {
        return (R) RestAction.super.deadline(timestamp);
    }
}
