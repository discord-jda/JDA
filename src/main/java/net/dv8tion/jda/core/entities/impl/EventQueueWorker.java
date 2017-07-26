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

package net.dv8tion.jda.core.entities.impl;

import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.ShutdownEvent;

class EventQueueWorker extends Thread
{
    private final JDAImpl api;

    EventQueueWorker(JDAImpl api)
    {
        this.api = api;
        setName("EventQueueWorker - " + api.getIdentifierString());
        start();
    }

    @Override
    public void run()
    {
        try
        {
            while (!Thread.currentThread().isInterrupted())
            {
                Event event = api.eventQueue.take();
                try
                {
                    api.getEventManager().handle(event);
                    if (event instanceof ShutdownEvent)
                        break;
                }
                catch (Throwable throwable)
                {
                    JDAImpl.LOG.log(throwable);
                }
            }
        }
        catch (InterruptedException ignored) {}
    }
}
