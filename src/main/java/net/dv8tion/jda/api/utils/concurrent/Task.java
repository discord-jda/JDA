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

package net.dv8tion.jda.api.utils.concurrent;

import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.Blocking;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Represents an asynchronous task.
 * <br>Note: The underlying task may already be started.
 *
 * @param <T>
 *        The result type
 */
public interface Task<T>
{
    /**
     * Whether this task has started.
     *
     * @return True, if this task has already started.
     */
    boolean isStarted();

    /**
     * Provide a callback for exception handling.
     * <br>This is an asynchronous operation.
     *
     * <p>The error will be logged regardless of your callback, this only exists to handle
     * failures for other purposes.
     *
     * @param  callback
     *         The error callback
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return The current Task instance for chaining
     */
    @Nonnull
    Task<T> onError(@Nonnull Consumer<? super Throwable> callback);

    /**
     * Provide a callback for success handling.
     * <br>This is an asynchronous operation.
     *
     * @param  callback
     *         The success callback
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return The current Task instance for chaining
     */
    @Nonnull
    Task<T> onSuccess(@Nonnull Consumer<? super T> callback);

    /**
     * Change the timeout duration for this task.
     * <br>This may be ignored for certain operations.
     *
     * <p>The provided timeout is relative to the start time of the task.
     * If the time has already passed, this will immediately cancel the task.
     *
     * @param  timeout
     *         The new timeout duration
     *
     * @throws IllegalArgumentException
     *         If null is provided or the timeout is not positive
     *
     * @return The current Task instance for chaining
     */
    @Nonnull
    Task<T> setTimeout(@Nonnull Duration timeout);

    /**
     * Change the timeout duration for this task.
     * <br>This may be ignored for certain operations.
     *
     * <p>The provided timeout is relative to the start time of the task.
     * If the time has already passed, this will immediately cancel the task.
     *
     * @param  timeout
     *         The new timeout duration
     * @param  unit
     *         The time unit of the timeout
     *
     * @throws IllegalArgumentException
     *         If null is provided or the timeout is not positive
     *
     * @return The current Task instance for chaining
     */
    @Nonnull
    default Task<T> setTimeout(long timeout, @Nonnull TimeUnit unit)
    {
        Checks.notNull(unit, "TimeUnit");
        return setTimeout(Duration.ofMillis(unit.toMillis(timeout)));
    }

    /**
     * Blocks the current thread until the result is ready.
     * <br>This will not work on the default JDA event thread because it might depend on other events to be processed,
     * which could lead to a deadlock.
     *
     * @throws UnsupportedOperationException
     *         If this is called on the default JDA event thread
     * @throws java.util.concurrent.CompletionException
     *         If some exception occurred (such as {@link java.util.concurrent.TimeoutException}).
     * @throws java.util.concurrent.CancellationException
     *         If the request was cancelled
     *
     * @return The result value
     */
    @Nonnull
    @Blocking
    T get();

    /**
     * Cancels the task and will emit a {@link java.util.concurrent.CancellationException CancellationException}.
     */
    void cancel();
}
