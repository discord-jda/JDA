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
package net.dv8tion.jda.api.requests

import java.util.concurrent.TimeUnit
import java.util.function.BooleanSupplier
import javax.annotation.Nonnull

/**
 * Interface used to mixin the customization parameters for [RestActions][RestAction].
 * <br></br>This simply fixes the return types to be the concrete implementation instead of the base interface.
 *
 * @param <T>
 * The result type of the RestAction
 * @param <R>
 * The concrete RestAction type used for chaining (fluent interface)
</R></T> */
interface FluentRestAction<T, R : FluentRestAction<T, R>?> : RestAction<T> {
    @Nonnull
    override fun setCheck(checks: BooleanSupplier?): R?
    @Nonnull
    override fun addCheck(@Nonnull checks: BooleanSupplier): R? {
        return super.addCheck(checks) as R
    }

    @Nonnull
    override fun timeout(timeout: Long, @Nonnull unit: TimeUnit): R? {
        return super.timeout(timeout, unit) as R
    }

    @Nonnull
    override fun deadline(timestamp: Long): R? {
        return super.deadline(timestamp) as R
    }
}
