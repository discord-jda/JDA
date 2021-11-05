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

package net.dv8tion.jda.internal.hooks;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.IEventManager;
import net.dv8tion.jda.api.hooks.InterfacedEventManager;
import net.dv8tion.jda.internal.JDAImpl;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

public class EventManagerProxy implements IEventManager
{
    private final ExecutorService executor;
    private IEventManager subject;

    public EventManagerProxy(IEventManager subject, ExecutorService executor)
    {
        this.subject = subject;
        this.executor = executor;
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
        try
        {
            if (executor != null && !executor.isShutdown())
                executor.execute(() -> handleInternally(event));
            else
                handleInternally(event);
        }
        catch (RejectedExecutionException ex)
        {
            JDAImpl.LOG.warn("Event-Pool rejected event execution! Running on handling thread instead...");
            handleInternally(event);
        }
        catch (Exception ex)
        {
            JDAImpl.LOG.error("Encountered exception trying to schedule event", ex);
        }
    }

    private void handleInternally(@Nonnull GenericEvent event)
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
