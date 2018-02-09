/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda.core.requests;

import net.dv8tion.jda.core.utils.Checks;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;
import java.util.stream.Stream;

/**
 * Future allowing for use of continuations.
 *
 * @param <T>
 *        The completion type for this Future
 */
public interface RequestFuture<T> extends Future<T>, CompletionStage<T>
{
    /**
     * Returns a new CompletableFuture that is completed when all of
     * the given RequestFutures complete.  If any of the given
     * RequestFutures complete exceptionally, then the returned
     * CompletableFuture also does so, with a CompletionException
     * holding this exception as its cause.  Otherwise, the results,
     * if any, of the given RequestFutures are not reflected in
     * the returned CompletableFuture, but may be obtained by
     * inspecting them individually. If no RequestFutures are
     * provided, returns a CompletableFuture completed with the value
     * {@code null}.
     *
     * <p>Among the applications of this method is to await completion
     * of a set of independent RequestFutures before continuing a
     * program, as in: {@code RequestFuture.allOf(c1, c2,
     * c3).join();}.
     *
     * @param  cfs
     *         the RequestFutures
     *
     * @throws java.lang.IllegalArgumentException
     *         if the array or any of its elements are {@code null}
     *
     * @return a new CompletableFuture that is completed when all of the given RequestFutures complete
     *
     * @see    java.util.concurrent.CompletableFuture#allOf(java.util.concurrent.CompletableFuture[]) CompletableFuture.allOf(...)
     */
    static CompletableFuture<Void> allOf(RequestFuture<?>... cfs)
    {
        Checks.noneNull(cfs, "RequestFutures");
        CompletableFuture[] all = Stream.of(cfs).map(CompletableFuture.class::cast).toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(all);
    }

    /**
     * Returns a new CompletableFuture that is completed when all of
     * the given Futures complete.  If any of the given
     * Futures complete exceptionally, then the returned
     * CompletableFuture also does so, with a CompletionException
     * holding this exception as its cause.  Otherwise, the results,
     * if any, of the given RequestFutures are not reflected in
     * the returned CompletableFuture, but may be obtained by
     * inspecting them individually. If no Futures are
     * provided, returns a CompletableFuture completed with the value
     * {@code null}.
     *
     * <p>Among the applications of this method is to await completion
     * of a set of independent RequestFutures before continuing a
     * program, as in: {@code RequestFuture.allOf(c1, c2,
     * c3).join();}.
     *
     * @param  <F>
     *         the future implementation
     * @param  cfs
     *         the Futures
     *
     * @throws java.lang.IllegalArgumentException
     *         if the collection or any of its elements are {@code null}
     *
     * @return a new CompletableFuture that is completed when all of the given Futures complete
     *
     * @see    java.util.concurrent.CompletableFuture#allOf(java.util.concurrent.CompletableFuture[]) CompletableFuture.allOf(...)
     */
    static <F extends Future<?> & CompletionStage<?>> CompletableFuture<Void> allOf(Collection<F> cfs)
    {
        Checks.notNull(cfs, "Collection");
        CompletableFuture[] all = cfs.stream().map(CompletableFuture.class::cast).toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(all);
    }

    /**
     * Returns a new CompletableFuture that is completed when any of
     * the given RequestFutures complete, with the same result.
     * Otherwise, if it completed exceptionally, the returned
     * CompletableFuture also does so, with a CompletionException
     * holding this exception as its cause.  If no RequestFutures
     * are provided, returns an incomplete CompletableFuture.
     *
     * @param  cfs
     *         the RequestFutures
     *
     * @throws java.lang.IllegalArgumentException
     *         if the array or any of its elements are {@code null}
     *
     * @return a new CompletableFuture that is completed with the
     *         result or exception of any of the given RequestFutures when one completes
     *
     * @see    java.util.concurrent.CompletableFuture#anyOf(java.util.concurrent.CompletableFuture[]) CompletableFuture.anyOf(...)
     */
    static CompletableFuture<Object> anyOf(RequestFuture<?>... cfs)
    {
        Checks.noneNull(cfs, "RequestFutures");
        CompletableFuture[] all = Stream.of(cfs).map(CompletableFuture.class::cast).toArray(CompletableFuture[]::new);
        return CompletableFuture.anyOf(all);
    }

    /**
     * Returns a new CompletableFuture that is completed when any of
     * the given Futures complete, with the same result.
     * Otherwise, if it completed exceptionally, the returned
     * CompletableFuture also does so, with a CompletionException
     * holding this exception as its cause.  If no Futures
     * are provided, returns an incomplete CompletableFuture.
     *
     * @param  <F>
     *         the future implementation
     * @param  cfs
     *         the Futures
     *
     * @throws java.lang.IllegalArgumentException
     *         if the collection or any of its elements are {@code null}
     *
     * @return a new CompletableFuture that is completed with the
     *         result or exception of any of the given Futures when one completes
     *
     * @see    java.util.concurrent.CompletableFuture#anyOf(java.util.concurrent.CompletableFuture[]) CompletableFuture.anyOf(...)
     */
    static <F extends Future<?> & CompletionStage<?>> CompletableFuture<Object> anyOf(Collection<F> cfs)
    {
        Checks.notNull(cfs, "Collection");
        CompletableFuture[] all = cfs.stream().map(CompletableFuture.class::cast).toArray(CompletableFuture[]::new);
        return CompletableFuture.anyOf(all);
    }

    /* Expose harmless functionality */

    /**
     * Returns the result value when complete, or throws an
     * (unchecked) exception if completed exceptionally. To better
     * conform with the use of common functional forms, if a
     * computation involved in the completion of this
     * CompletableFuture threw an exception, this method throws an
     * (unchecked) {@link java.util.concurrent.CompletionException} with the underlying
     * exception as its cause.
     *
     * @throws java.util.concurrent.CancellationException
     *         if the computation was cancelled
     * @throws java.util.concurrent.CompletionException
     *         if this future completed exceptionally
     *         or a completion computation threw an exception
     *
     * @return the result value
     */
    T join();

    /**
     * Returns the result value (or throws any encountered exception)
     * if completed, else returns the given valueIfAbsent.
     *
     * @param  valueIfAbsent
     *         the value to return if not completed
     *
     * @throws java.util.concurrent.CancellationException
     *         if the computation was cancelled
     * @throws java.util.concurrent.CompletionException
     *         if this future completed exceptionally
     *         or a completion computation threw an exception
     *
     * @return the result value, if completed, else the given valueIfAbsent
     */
    T getNow(T valueIfAbsent);

    /**
     * Returns {@code true} if this RequestFuture completed
     * exceptionally, in any way. Possible causes include
     * cancellation, explicit invocation of {@code
     * completeExceptionally}, and abrupt termination of a
     * CompletionStage action.
     *
     * @return {@code true} if this RequestFuture completed exceptionally
     */
    boolean isCompletedExceptionally();

    /**
     * Returns the estimated number of RequestFutures whose
     * completions are awaiting completion of this RequestFuture.
     * This method is designed for use in monitoring system state, not
     * for synchronization control.
     *
     * @return the number of dependent RequestFutures
     */
    int getNumberOfDependents();

    /*
        Hide harmful functionality

        - complete
        - completeExceptionally
        - obtrudeValue
        - obtrudeException
    */

    /**
     * <b>This method is unsupported by the current implementation!</b>
     *
     * <p>{@inheritDoc}
     */
    @Override
    CompletableFuture<T> toCompletableFuture();
}
