/*
 * Copyright 2015-2020 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

package net.dv8tion.jda.internal.utils.config.sharding;

import net.dv8tion.jda.api.hooks.IEventManager;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

public class EventConfig
{
    private final List<Object> listeners = new ArrayList<>();
    private final List<IntFunction<Object>> listenerProviders = new ArrayList<>();
    private final IntFunction<? extends IEventManager> eventManagerProvider;

    public EventConfig(@Nullable IntFunction<? extends IEventManager> eventManagerProvider)
    {
        this.eventManagerProvider = eventManagerProvider;
    }

    public void addEventListener(@Nonnull Object listener)
    {
        Checks.notNull(listener, "Listener");
        listeners.add(listener);
    }

    public void removeEventListener(@Nonnull Object listener)
    {
        Checks.notNull(listener, "Listener");
        listeners.remove(listener);
    }

    public void addEventListenerProvider(@Nonnull IntFunction<Object> provider)
    {
        Checks.notNull(provider, "Provider");
        listenerProviders.add(provider);
    }

    public void removeEventListenerProvider(@Nonnull IntFunction<Object> provider)
    {
        Checks.notNull(provider, "Provider");
        listenerProviders.remove(provider);
    }

    @Nonnull
    public List<Object> getListeners()
    {
        return listeners;
    }

    @Nonnull
    public List<IntFunction<Object>> getListenerProviders()
    {
        return listenerProviders;
    }

    @Nullable
    public IntFunction<? extends IEventManager> getEventManagerProvider()
    {
        return eventManagerProvider;
    }

    @Nonnull
    public static EventConfig getDefault()
    {
        return new EventConfig(null);
    }
}
