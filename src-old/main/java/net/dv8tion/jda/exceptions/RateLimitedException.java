/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
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
package net.dv8tion.jda.exceptions;

public class RateLimitedException extends RuntimeException
{
    private final long timeout, availTime;
    public RateLimitedException(long timeout)
    {
        super("The message got Rate-Limited. You are able to send messages again in " + timeout + " ms");
        this.timeout = timeout;
        this.availTime = System.currentTimeMillis() + timeout;
    }

    /**
     * Gets the timeout. After timeout ms, messages can be sent again.
     * @return
     *      the ms after which messages can be sent again
     */
    public long getTimeout()
    {
        return timeout;
    }

    /**
     * Gets the System-time, after which messages can be sent again
     * @return
     *      the system-time, after which messages can be sent again
     */
    public long getAvailTime()
    {
        return availTime;
    }
}
