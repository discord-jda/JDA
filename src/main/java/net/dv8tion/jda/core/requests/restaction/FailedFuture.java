/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
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

package net.dv8tion.jda.core.requests.restaction;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class FailedFuture<T> implements Future<T>
{
    protected ExecutionException exception;

    public FailedFuture(final Exception cause)
    {
        this.exception = new ExecutionException(cause);
    }

    public FailedFuture(final String message, final Exception cause)
    {
        this.exception = new ExecutionException(message, cause);
    }

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning)
    {
        return false;
    }

    @Override
    public T get() throws ExecutionException
    {
        throw this.exception;
    }

    @Override
    public T get(final long timeout, final TimeUnit unit) throws ExecutionException
    {
        throw this.exception;
    }

    @Override
    public boolean isCancelled()
    {
        return false;
    }

    @Override
    public boolean isDone()
    {
        return true;
    }
}
