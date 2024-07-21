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

package net.dv8tion.jda.api.utils;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.api.utils.concurrent.Task;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.JDALogger;
import net.dv8tion.jda.internal.utils.concurrent.task.GatewayTask;
import org.slf4j.Logger;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Helper class to listen to an event, once.
 *
 * @param <E> Type of the event listened to
 *
 * @see   JDA#listenOnce(Class)
 */
public class Once<E extends GenericEvent> implements EventListener
{
    private static final Logger LOG = JDALogger.getLog(Once.class);

    private final JDA jda;
    private final Class<E> eventType;
    private final List<Predicate<? super E>> filters;
    private final CompletableFuture<E> future;
    private final GatewayTask<E> task;
    private final ScheduledFuture<?> timeoutFuture;
    private final Runnable timeoutCallback;

    private Once(@Nonnull Once.Builder<E> builder)
    {
        this.jda = builder.jda;
        this.eventType = builder.eventType;
        this.filters = new ArrayList<>(builder.filters);
        this.timeoutCallback = builder.timeoutCallback;

        this.future = new CompletableFuture<>();
        this.task = createTask();
        this.timeoutFuture = scheduleTimeout(builder.timeout, builder.timeoutPool);
    }

    @Nonnull
    private GatewayTask<E> createTask()
    {
        final GatewayTask<E> task = new GatewayTask<>(future, () ->
        {
            // On cancellation, throw cancellation exception and cancel timeout
            jda.removeEventListener(this);
            future.completeExceptionally(new CancellationException());
            if (timeoutFuture != null)
                timeoutFuture.cancel(false);
        });
        task.onSetTimeout(e ->
        {
            throw new UnsupportedOperationException("You must set the timeout on Once.Builder#timeout");
        });
        return task;
    }

    @Nullable
    private ScheduledFuture<?> scheduleTimeout(@Nullable Duration timeout, @Nullable ScheduledExecutorService timeoutPool)
    {
        if (timeout == null) return null;
        if (timeoutPool == null) timeoutPool = jda.getGatewayPool();

        return timeoutPool.schedule(() ->
        {
            // On timeout, throw timeout exception and run timeout callback
            jda.removeEventListener(this);
            if (!future.completeExceptionally(new TimeoutException()))
                return;
            if (timeoutCallback != null)
            {
                try
                {
                    timeoutCallback.run();
                }
                catch (Throwable e)
                {
                    LOG.error("An error occurred while running the timeout callback", e);
                    if (e instanceof Error)
                        throw (Error) e;
                }
            }
        }, timeout.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    @SubscribeEvent
    public void onEvent(@Nonnull GenericEvent event)
    {
        if (!eventType.isInstance(event))
            return;
        final E casted = eventType.cast(event);
        try
        {
            if (filters.stream().allMatch(p -> p.test(casted)))
            {
                if (timeoutFuture != null)
                    timeoutFuture.cancel(false);
                event.getJDA().removeEventListener(this);
                future.complete(casted);
            }
        }
        catch (Throwable e)
        {
            if (future.completeExceptionally(e))
                event.getJDA().removeEventListener(this);
            if (e instanceof Error)
                throw (Error) e;
        }
    }

    /**
     * Builds a one-time event listener, can be reused.
     *
     * @param <E> Type of the event listened to
     */
    public static class Builder<E extends GenericEvent>
    {
        private final JDA jda;
        private final Class<E> eventType;
        private final List<Predicate<? super E>> filters = new ArrayList<>();

        private ScheduledExecutorService timeoutPool;
        private Duration timeout;
        private Runnable timeoutCallback;

        /**
         * Creates a builder for a one-time event listener
         *
         * @param jda
         *        The JDA instance
         * @param eventType
         *        The event type to listen for
         *
         * @throws IllegalArgumentException
         *         If any of the parameters is null
         */
        public Builder(@Nonnull JDA jda, @Nonnull Class<E> eventType)
        {
            Checks.notNull(jda, "JDA");
            Checks.notNull(eventType, "Event type");
            this.jda = jda;
            this.eventType = eventType;
        }

        /**
         * Adds an event filter, all filters need to return {@code true} for the event to be consumed.
         *
         * <p>If the filter throws an exception, this listener will unregister itself.
         *
         * @param  filter
         *         The filter to add, returns {@code true} if the event can be consumed
         *
         * @throws IllegalArgumentException
         *         If the filter is null
         *
         * @return This instance for chaining convenience
         */
        @Nonnull
        public Builder<E> filter(@Nonnull Predicate<? super E> filter)
        {
            Checks.notNull(filter, "Filter");
            filters.add(filter);
            return this;
        }

        /**
         * Sets the timeout duration, after which the event is no longer listener for.
         *
         * @param  timeout
         *         The duration after which the event is no longer listener for
         *
         * @throws IllegalArgumentException
         *         If the timeout is null
         *
         * @return This instance for chaining convenience
         */
        @Nonnull
        public Builder<E> timeout(@Nonnull Duration timeout)
        {
            return timeout(timeout, null);
        }

        /**
         * Sets the timeout duration, after which the event is no longer listener for,
         * and the callback is run.
         *
         * @param  timeout
         *         The duration after which the event is no longer listener for
         * @param  timeoutCallback
         *         The callback run after the duration
         *
         * @throws IllegalArgumentException
         *         If the timeout is null
         *
         * @return This instance for chaining convenience
         */
        @Nonnull
        public Builder<E> timeout(@Nonnull Duration timeout, @Nullable Runnable timeoutCallback)
        {
            Checks.notNull(timeout, "Timeout");
            this.timeout = timeout;
            this.timeoutCallback = timeoutCallback;
            return this;
        }

        /**
         * Sets the thread pool used to schedule timeouts and run its callback.
         *
         * <p>By default {@link JDA#getGatewayPool()} is used.
         *
         * @param  timeoutPool
         *         The thread pool to use for timeouts
         *
         * @throws IllegalArgumentException
         *         If the timeout pool is null
         *
         * @return This instance for chaining convenience
         */
        @Nonnull
        public Builder<E> setTimeoutPool(@Nonnull ScheduledExecutorService timeoutPool)
        {
            Checks.notNull(timeoutPool, "Timeout pool");
            this.timeoutPool = timeoutPool;
            return this;
        }

        /**
         * Starts listening for the event, once.
         *
         * <p>The task will be completed after all {@link #filter(Predicate) filters} return {@code true}.
         *
         * <p>Exceptions thrown in {@link Task#get() blocking} and {@link Task#onSuccess(Consumer) async} contexts includes:
         * <ul>
         *     <li>{@link CancellationException} - When {@link Task#cancel()} is called</li>
         *     <li>{@link TimeoutException} - When the listener has expired</li>
         *     <li>Any exception thrown by the {@link #timeout(Duration, Runnable) timeout callback}</li>
         * </ul>
         *
         * @throws IllegalArgumentException
         *         If the callback is null
         *
         * @return {@link Task} returning an event satisfying all preconditions
         *
         * @see Task#onSuccess(Consumer)
         * @see Task#get()
         */
        @Nonnull
        @CheckReturnValue
        public Task<E> subscribe(@Nonnull Consumer<E> callback)
        {
            final Once<E> once = new Once<>(this);
            jda.addEventListener(once);
            return once.task.onSuccess(callback);
        }
    }
}
