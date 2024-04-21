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
package net.dv8tion.jda.api.audit

import javax.annotation.Nonnull

/**
 * Thread-Local audit-log reason used automatically by [AuditableRestAction][net.dv8tion.jda.api.requests.restaction.AuditableRestAction] instances
 * when no other reason was set.
 *
 *
 * Note that [RestAction.queue()][net.dv8tion.jda.api.requests.RestAction.queue] will forward any
 * thread-local reason set through this handle. Thus audit-log reasons done by callbacks will also use the one set
 * from the executing thread.
 *
 *
 * **Example without closable**<br></br>
 * <pre>`
 * String previousReason = ThreadLocalReason.getCurrent();
 * ThreadLocalReason.setCurrent("Hello World");
 * try {
 * guild.ban(user, 0).queue(v -> {
 * guild.unban(user).queue(); // also uses the reason "Hello World"
 * });
 * } finally {
 * //Forwarding the reason is not async so resetting it here is fine.
 * ThreadLocalReason.setCurrent(previousReason);
 * }
 * //This will not use the reason "Hello World" but the previous, or none if none was set previously
 * guild.kick(user).queue();
`</pre> *
 *
 *
 * **Example with closable**<br></br>
 * <pre>`
 * try (ThreadLocalReason.Closable __ = ThreadLocalReason.closable("Hello World")) {
 * guild.ban(user, 0).queue(v -> {
 * guild.unban(user).queue(); // also uses the reason "Hello World"
 * });
 * } // automatically changes reason back
 * //This will not use the reason "Hello World" but the previous, or none if none was set previously
 * guild.kick(user).queue();
`</pre> *
 *
 *
 * @see net.dv8tion.jda.api.requests.restaction.AuditableRestAction.reason
 * @see ThreadLocal
 */
class ThreadLocalReason private constructor() {
    init {
        throw UnsupportedOperationException()
    }

    /**
     * Allows to use try-with-resources blocks for setting reasons
     *
     *
     * Example:
     * <pre>`
     * try (ThreadLocalReason.Closable closable = new ThreadLocalReason.Closable("Massban")) { // calls setCurrent("Massban")
     * List<Member> mentions = event.getMessage().getMentionedMembers();
     * Guild guild = event.getGuild();
     * mentions.stream()
     * .map(m -> guild.ban(m, 7))
     * .forEach(RestAction::queue);
     * } // calls resetCurrent()
    `</pre> *
     */
    class Closable(reason: String?) : AutoCloseable {
        private val previous: String?

        init {
            previous = current
            current = reason
        }

        override fun close() {
            current = previous
        }
    }

    companion object {
        private var currentReason: ThreadLocal<String>? = null

        /**
         * Resets the currently set thread-local reason, if present.
         */
        fun resetCurrent() {
            if (currentReason != null) currentReason!!.remove()
        }

        @JvmStatic
        var current: String?
            /**
             * The current reason that should be used for [AuditableRestAction][net.dv8tion.jda.api.requests.restaction.AuditableRestAction].
             *
             * @return The current thread-local reason, or null
             */
            get() = if (currentReason == null) null else currentReason!!.get()
            /**
             * Sets the current reason that should be used for [AuditableRestAction][net.dv8tion.jda.api.requests.restaction.AuditableRestAction].
             *
             * @param reason
             * The reason to use, or `null` to reset
             */
            set(reason) {
                if (reason != null) {
                    if (currentReason == null) currentReason = ThreadLocal()
                    currentReason!!.set(reason)
                } else if (currentReason != null) {
                    currentReason!!.remove()
                }
            }

        /**
         * Creates a new [ThreadLocalReason.Closable] instance.
         * <br></br>Allows to use try-with-resources blocks for setting reasons
         *
         * @param  reason
         * The reason to use
         *
         * @return The closable instance
         */
        @JvmStatic
        @Nonnull
        fun closable(reason: String?): Closable {
            return Closable(reason)
        }
    }
}
