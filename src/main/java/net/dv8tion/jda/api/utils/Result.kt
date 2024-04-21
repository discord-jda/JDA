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
package net.dv8tion.jda.api.utils

import net.dv8tion.jda.internal.utils.Checks
import net.dv8tion.jda.internal.utils.EntityString
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate
import java.util.function.Supplier
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Represents a computation or task result.
 * <br></br>This result may be a [failure][.getFailure] or [success][.get].
 *
 *
 * This is a **value type** and does not implement [.equals] or [.hashCode]!
 *
 * @param <T>
 * The success type
 *
 * @since  4.2.1
</T> */
class Result<T> private constructor(private val value: T, private val error: Throwable?) {
    val isFailure: Boolean
        /**
         * True if this result is a failure.
         * <br></br>Use [.getFailure] or [.expect] to handle failures.
         *
         * @return True, if this is a failure result
         */
        get() = error != null
    val isSuccess: Boolean
        /**
         * True if this result is a success.
         * <br></br>Use [.get] or [.map] to handle success values.
         *
         * @return True, if this is a successful result
         */
        get() = error == null

    /**
     * Passive error handler.
     * <br></br>This will apply the provided callback if [.isFailure] is true
     * and return the same result for further chaining.
     *
     * @param  callback
     * The passive callback
     *
     * @throws IllegalArgumentException
     * If the callback is null
     *
     * @return The same result instance
     */
    @Nonnull
    fun onFailure(@Nonnull callback: Consumer<in Throwable?>): Result<T> {
        Checks.notNull(callback, "Callback")
        if (isFailure) callback.accept(error)
        return this
    }

    /**
     * Passive success handler.
     * <br></br>This will apply the provided callback if [.isSuccess] is true
     * and return the same result for further chaining.
     *
     * @param  callback
     * The passive callback
     *
     * @throws IllegalArgumentException
     * If the callback is null
     *
     * @return The same result instance
     */
    @Nonnull
    fun onSuccess(@Nonnull callback: Consumer<in T>): Result<T> {
        Checks.notNull(callback, "Callback")
        if (isSuccess) callback.accept(value)
        return this
    }

    /**
     * Composite function to convert a result value to another value.
     * <br></br>This will only apply the function is [.isSuccess] is true.
     *
     * @param  function
     * The conversion function
     * @param  <U>
     * The result type
     *
     * @throws IllegalArgumentException
     * If the provided function is null
     *
     * @return The mapped result
     *
     * @see .flatMap
    </U> */
    @Nonnull
    @CheckReturnValue
    fun <U> map(@Nonnull function: Function<in T, out U>): Result<U> {
        Checks.notNull(function, "Function")
        return if (isSuccess) defer {
            function.apply(
                value
            )
        } else this as Result<U>
    }

    /**
     * Composite function to convert a result value to another result.
     * <br></br>This will only apply the function is [.isSuccess] is true.
     *
     * @param  function
     * The conversion function
     * @param  <U>
     * The result type
     *
     * @throws IllegalArgumentException
     * If the provided function is null
     *
     * @return The mapped result
    </U> */
    @Nonnull
    @CheckReturnValue
    fun <U> flatMap(@Nonnull function: Function<in T, out Result<U>?>): Result<U>? {
        Checks.notNull(function, "Function")
        try {
            if (isSuccess) return function.apply(value)
        } catch (ex: Exception) {
            return failure(ex)
        }
        return this as Result<U>
    }

    /**
     * Unwraps the success value of this result.
     * <br></br>This only works if [.isSuccess] is true and throws otherwise.
     *
     * @throws IllegalStateException
     * If the result is not successful
     *
     * @return The result value
     */
    fun get(): T {
        if (isFailure) throw IllegalStateException(error)
        return value
    }

    /**
     * Unwraps the error for this result.
     * <br></br>This will be `null` if [.isFailure] is false.
     *
     * @return The error or null
     */
    fun getFailure(): Throwable? {
        return error
    }

    /**
     * Throws the wrapped exception if the provided predicate returns true.
     * <br></br>This will never provide a null error to the predicate.
     * A successful result will never throw.
     *
     * @param  predicate
     * The test predicate
     *
     * @throws IllegalArgumentException
     * If the provided predicate is null
     * @throws IllegalStateException
     * If the predicate returns true, the [cause][Throwable.getCause] will be the wrapped exception
     *
     * @return The same result instance
     */
    @Nonnull
    fun expect(@Nonnull predicate: Predicate<in Throwable?>): Result<T> {
        Checks.notNull(predicate, "Predicate")
        if (isFailure && predicate.test(error)) throw IllegalStateException(error)
        return this
    }

    override fun toString(): String {
        val entityString = EntityString(this)
        if (isSuccess) entityString.addMetadata("success", value) else entityString.addMetadata("error", error)
        return entityString.toString()
    }

    companion object {
        /**
         * Creates a successful result.
         *
         * @param  value
         * The success value
         * @param  <E>
         * The success type
         *
         * @return Result
        </E> */
        @Nonnull
        @CheckReturnValue
        fun <E> success(value: E?): Result<E?> {
            return Result(value, null)
        }

        /**
         * Creates a failure result.
         *
         * @param  error
         * The failure throwable
         * @param  <E>
         * The success type
         *
         * @throws IllegalArgumentException
         * If the provided error is null
         *
         * @return Result
        </E> */
        @Nonnull
        @CheckReturnValue
        fun <E> failure(@Nonnull error: Throwable?): Result<E?> {
            Checks.notNull(error, "Error")
            return Result(null, error)
        }

        /**
         * Creates a result instance from the provided supplier.
         * <br></br>If the supplier throws an exception, a failure result is returned.
         *
         * @param  supplier
         * The supplier
         * @param  <E>
         * The success type
         *
         * @throws IllegalArgumentException
         * If the supplier is null
         *
         * @return Result instance with the supplied value or exception failure
        </E> */
        @Nonnull
        @CheckReturnValue
        fun <E> defer(@Nonnull supplier: Supplier<out E>): Result<E> {
            Checks.notNull(supplier, "Supplier")
            return try {
                success<E>(supplier.get())
            } catch (ex: Exception) {
                failure(ex)
            }
        }
    }
}
