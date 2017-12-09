/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter & Florian Spieß
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

package net.dv8tion.jda.core.utils;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

public interface SessionController
{
    int IDENTIFY_DELAY = 5;

    void appendSession(SessionConnectNode node);
    void removeSession(SessionConnectNode node);

    long getGlobalRatelimit();
    void setGlobalRatelimit(long ratelimit);

    String getGateway(JDA api) throws RateLimitedException;

    interface SessionConnectNode
    {
        boolean isReconnect();

        JDA getJDA();

        JDA.ShardInfo getShardInfo();

        void run(boolean isLast) throws InterruptedException;
    }
}
