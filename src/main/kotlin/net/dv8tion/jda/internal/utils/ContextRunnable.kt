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

package net.dv8tion.jda.internal.utils

import net.dv8tion.jda.api.audit.ThreadLocalReason
import java.util.concurrent.Callable

class ContextRunnable<E> :
    Runnable,
    Callable<E> {
    private val localReason: String?
    private val runnable: Runnable?
    private val callable: Callable<E>?

    constructor(runnable: Runnable) {
        this.localReason = ThreadLocalReason.getCurrent()
        this.runnable = runnable
        this.callable = null
    }

    constructor(callable: Callable<E>) {
        this.localReason = ThreadLocalReason.getCurrent()
        this.runnable = null
        this.callable = callable
    }

    override fun run() {
        ThreadLocalReason.closable(localReason).use {
            runnable!!.run()
        }
    }

    @Throws(Exception::class)
    override fun call(): E? =
        ThreadLocalReason.closable(localReason).use {
            callable!!.call()
        }
}
