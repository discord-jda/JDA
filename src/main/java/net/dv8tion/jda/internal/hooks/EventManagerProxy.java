/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

package net.dv8tion.jda.internal.hooks;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.IEventManager;
import net.dv8tion.jda.api.hooks.InterfacedEventManager;
import net.dv8tion.jda.internal.JDAImpl;

import javax.annotation.Nonnull;
import java.util.List;

public class EventManagerProxy implements IEventManager
{
    private IEventManager subject;

    public EventManagerProxy(IEventManager subject)
    {
        this.subject = subject;
    }

    public void setSubject(IEventManager subject)
    {
        this.subject = subject == null ? new InterfacedEventManager() : subject;
    }

    public IEventManager getSubject()
    {
        return subject;
    }

    @Override
    public void register(@Nonnull Object listener)
    {
        this.subject.register(listener);
    }

    @Override
    public void unregister(@Nonnull Object listener)
    {
        this.subject.unregister(listener);
    }

    @Override
    public void handle(@Nonnull GenericEvent event)
    {
        // don't allow mere exceptions to obstruct the socket handler
        try
        {
            subject.handle(event);
        }
        catch (RuntimeException e)
        {
            JDAImpl.LOG.error("The EventManager.handle() call had an uncaught exception", e);
        }
    }

    @Nonnull
    @Override
    public List<Object> getRegisteredListeners()
    {
        return subject.getRegisteredListeners();
    }
}
