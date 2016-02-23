/**
 * Copyright 2015-2016 Austin Keener & Michael Ritter
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda.hooks;

import net.dv8tion.jda.events.Event;

import java.util.LinkedList;
import java.util.List;

public class InterfacedEventManager implements IEventManager {
    private final List<EventListener> listeners = new LinkedList<>();

    public InterfacedEventManager() {

    }

    @Override
    public void register(Object listener) {
        if (!(listener instanceof EventListener)) {
            throw new IllegalArgumentException("Listener must implement EventListener");
        }
        listeners.add(((EventListener) listener));
    }

    @Override
    public void unregister(Object listener) {
        listeners.remove(listener);
    }

    @Override
    public void handle(Event event) {
        List<EventListener> listenerCopy = new LinkedList<>(listeners);
        for (EventListener listener : listenerCopy) {
            listener.onEvent(event);
        }
    }
}
