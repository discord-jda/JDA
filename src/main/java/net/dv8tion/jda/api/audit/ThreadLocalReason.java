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

package net.dv8tion.jda.api.audit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Thread-Local audit-log reason used automatically by {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction} instances
 * when no other reason was set.
 *
 * <p>Note that {@link net.dv8tion.jda.api.requests.RestAction#queue(Consumer) RestAction.queue()} will forward any
 * thread-local reason set through this handle. Thus audit-log reasons done by callbacks will also use the one set
 * from the executing thread.
 *
 * <h2>Example without closable</h2>
 * <pre><code>
 * String previousReason = ThreadLocalReason.getCurrent();
 * ThreadLocalReason.setCurrent("Hello World");
 * try {
 *     guild.ban(user, 0).queue(v -&gt; {
 *         guild.unban(user).queue(); // also uses the reason "Hello World"
 *     });
 * } finally {
 *     //Forwarding the reason is not async so resetting it here is fine.
 *     ThreadLocalReason.setCurrent(previousReason);
 * }
 * //This will not use the reason "Hello World" but the previous, or none if none was set previously
 * guild.kick(user).queue();
 * </code></pre>
 *
 * <h2>Example with closable</h2>
 * <pre><code>
 * try (ThreadLocalReason.Closable __ = ThreadLocalReason.closable("Hello World")) {
 *     guild.ban(user, 0).queue(v -&gt; {
 *         guild.unban(user).queue(); // also uses the reason "Hello World"
 *     });
 * } // automatically changes reason back
 * //This will not use the reason "Hello World" but the previous, or none if none was set previously
 * guild.kick(user).queue();
 * </code></pre>
 *
 *
 * @see net.dv8tion.jda.api.requests.restaction.AuditableRestAction#reason(String) AuditableRestAction.reason(String)
 * @see ThreadLocal
 */
public final class ThreadLocalReason
{
    private static ThreadLocal<String> currentReason;

    private ThreadLocalReason()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets the current reason that should be used for {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}.
     *
     * @param reason
     *        The reason to use, or {@code null} to reset
     */
    public static void setCurrent(@Nullable String reason)
    {
        if (reason != null)
        {
            if (currentReason == null)
                currentReason = new ThreadLocal<>();
            currentReason.set(reason);
        }
        else if (currentReason != null)
        {
            currentReason.remove();
        }
    }

    /**
     * Resets the currently set thread-local reason, if present.
     */
    public static void resetCurrent()
    {
        if (currentReason != null)
            currentReason.remove();
    }

    /**
     * The current reason that should be used for {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}.
     *
     * @return The current thread-local reason, or null
     */
    @Nullable
    public static String getCurrent()
    {
        return currentReason == null ? null : currentReason.get();
    }

    /**
     * Creates a new {@link ThreadLocalReason.Closable} instance.
     * <br>Allows to use try-with-resources blocks for setting reasons
     *
     * @param  reason
     *         The reason to use
     *
     * @return The closable instance
     */
    @Nonnull
    public static Closable closable(@Nullable String reason)
    {
        return new ThreadLocalReason.Closable(reason);
    }

    /**
     * Allows to use try-with-resources blocks for setting reasons
     *
     * <p>Example:
     * <pre><code>
     * try (ThreadLocalReason.Closable closable = new ThreadLocalReason.Closable("Massban")) { // calls setCurrent("Massban")
     *     {@literal List<Member>} mentions = event.getMessage().getMentionedMembers();
     *     Guild guild = event.getGuild();
     *     mentions.stream()
     *             .map(m -&gt; guild.ban(m, 7))
     *             .forEach(RestAction::queue);
     * } // calls resetCurrent()
     * </code></pre>
     */
    public static class Closable implements AutoCloseable
    {
        private final String previous;

        public Closable(@Nullable String reason)
        {
            this.previous = getCurrent();
            setCurrent(reason);
        }

        @Override
        public void close()
        {
            setCurrent(previous);
        }
    }
}
