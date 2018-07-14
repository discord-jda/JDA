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

package net.dv8tion.jda.core.exceptions;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * Used to pass a context to async exception handling for debugging purposes.
 */
public class ContextException extends Exception
{
    /**
     * Creates a failure consumer that appends a context cause
     * before printing the stack trace using {@link Throwable#printStackTrace()}.
     * <br>Equivalent to {@code here(Throwable::printStackTrace)}
     *
     * @return Wrapping failure consumer around {@code Throwable::printStackTrace}
     */
    @Nonnull
    public static Consumer<Throwable> herePrintingTrace()
    {
        return here(Throwable::printStackTrace);
    }

    /**
     * Creates a wrapping {@link java.util.function.Consumer Consumer} for
     * the provided target.
     *
     * @param  acceptor
     *         The end-target for the throwable
     *
     * @return Wrapper of the provided consumer that will append a context with the current stack-trace
     */
    @Nonnull
    public static Consumer<Throwable> here(@Nonnull Consumer<? super Throwable> acceptor)
    {
        ContextException context = new ContextException();
        return (ex) ->
        {
            Throwable cause = ex;
            while (cause.getCause() != null)
                cause = cause.getCause();
            cause.initCause(context);
            if (acceptor != null)
                acceptor.accept(ex);
        };
    }
}
