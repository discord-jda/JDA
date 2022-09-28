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

package net.dv8tion.jda.internal.utils;

public class ShutdownReason
{
    public static final ShutdownReason USER_SHUTDOWN = new ShutdownReason("User requested shutdown");
    public static final ShutdownReason INVALID_SHARDS = new ShutdownReason("Invalid shard configuration");
    public static final ShutdownReason DISALLOWED_INTENTS = new ShutdownReason("You tried turning on an intent you aren't allowed to use. " +
            "For more information check https://jda.wiki/using-jda/troubleshooting/#im-getting-closecode4014-disallowed-intents");

    protected final String reason;

    public ShutdownReason(String reason)
    {
        this.reason = reason;
    }

    public String getReason()
    {
        return reason;
    }

    @Override
    public String toString()
    {
        return reason;
    }
}
