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

import net.dv8tion.jda.core.ShardedRateLimiter;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.requests.SessionReconnectQueue;

@SuppressWarnings("deprecation")
public class ProvidingSessionController extends SessionControllerAdapter
{
    protected final SessionReconnectQueue queue;
    protected final ShardedRateLimiter rateLimiter;

    public ProvidingSessionController(SessionReconnectQueue queue, ShardedRateLimiter rateLimiter)
    {
        this.queue = queue;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void appendSession(SessionConnectNode node)
    {
        if (queue != null && node.isReconnect())
            queue.appendSession(((JDAImpl) node.getJDA()).getClient());
        else
            super.appendSession(node);
    }

    @Override
    public void removeSession(SessionConnectNode node)
    {
        if (queue != null && node.isReconnect())
            queue.removeSession(((JDAImpl) node.getJDA()).getClient());
        else
            super.removeSession(node);
    }

    @Override
    public long getGlobalRatelimit()
    {
        if (rateLimiter != null)
            return rateLimiter.getGlobalRatelimit();
        else
            return super.getGlobalRatelimit();
    }

    @Override
    public void setGlobalRatelimit(long ratelimit)
    {
        if (rateLimiter != null)
            rateLimiter.setGlobalRatelimit(ratelimit);
        else
            super.setGlobalRatelimit(ratelimit);
    }
}
