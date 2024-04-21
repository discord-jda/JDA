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
package net.dv8tion.jda.api.exceptions

import net.dv8tion.jda.api.exceptions.ContextException
import net.dv8tion.jda.internal.utils.Helpers
import java.util.function.Consumer
import javax.annotation.Nonnull

/**
 * Used to pass a context to async exception handling for debugging purposes.
 */
object ContextException : Exception() {
    /**
     * Creates a failure consumer that appends a context cause
     * before printing the stack trace using [Throwable.printStackTrace].
     * <br></br>Equivalent to `here(Throwable::printStackTrace)`
     *
     * @return Wrapping failure consumer around `Throwable::printStackTrace`
     */
    @Nonnull
    fun herePrintingTrace(): Consumer<Throwable> {
        return here { obj: Throwable -> obj.printStackTrace() }
    }

    /**
     * Creates a wrapping [Consumer][java.util.function.Consumer] for
     * the provided target.
     *
     * @param  acceptor
     * The end-target for the throwable
     *
     * @return Wrapper of the provided consumer that will append a context with the current stack-trace
     */
    @JvmStatic
    @Nonnull
    fun here(@Nonnull acceptor: Consumer<in Throwable>): Consumer<Throwable> {
        return ContextConsumer(ContextException(), acceptor)
    }

    /**
     * Wrapper for a failure [Consumer] that carries a [ContextException] as cause.
     */
    class ContextConsumer private constructor(private val context: ContextException, callback: Consumer<in Throwable>) :
        Consumer<Throwable> {
        private val callback: Consumer<in Throwable>?

        init {
            this.callback = callback
        }

        override fun accept(throwable: Throwable) {
            callback?.accept(Helpers.appendCause(throwable, context))
        }
    }
}
