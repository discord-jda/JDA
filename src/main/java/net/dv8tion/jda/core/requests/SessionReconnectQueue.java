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

package net.dv8tion.jda.core.requests;

import java.util.Queue;

class SessionReconnectQueue
{
    private final Object lock = new Object();
    private final Queue<WebSocketClient> reconnectQueue;
    private volatile Thread reconnectThread;

    SessionReconnectQueue(final Queue<WebSocketClient> reconnectQueue)
    {
        this.reconnectQueue = reconnectQueue;
    }

    void appendSession(final WebSocketClient client)
    {
        reconnectQueue.add(client);
        synchronized (lock)
        {
            if (reconnectThread != null)
                return;
            reconnectThread = new ReconnectThread();
        }
    }

    final class ReconnectThread extends Thread
    {
        ReconnectThread()
        {
            super("JDA-ReconnectThread");
            start();
        }

        @Override
        public final void run()
        {
            while (!reconnectQueue.isEmpty())
            {
                final WebSocketClient client = reconnectQueue.poll();
                client.init();
                try
                {
                    if (!reconnectQueue.isEmpty())
                        Thread.sleep(WebSocketClient.IDENTIFY_DELAY);
                }
                catch (InterruptedException ex)
                {
                    throw new AssertionError(ex);
                }
            }
            synchronized (lock)
            {
                reconnectThread = null;
            }
        }
    }

}
