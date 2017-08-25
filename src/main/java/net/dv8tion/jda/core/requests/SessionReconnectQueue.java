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

import java.util.concurrent.BlockingQueue;

public class SessionReconnectQueue
{
    protected final Object lock = new Object();
    protected final BlockingQueue<WebSocketClient> reconnectQueue;
    protected volatile Thread reconnectThread;

    public SessionReconnectQueue(final BlockingQueue<WebSocketClient> reconnectQueue)
    {
        this.reconnectQueue = reconnectQueue;
    }

    protected void appendSession(final WebSocketClient client)
    {
        reconnectQueue.add(client);
        synchronized (lock)
        {
            if (reconnectThread != null)
                return;
            reconnectThread = new ReconnectThread();
        }
    }

    protected final class ReconnectThread extends Thread
    {
        protected ReconnectThread()
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
                client.sendIdentify();
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
