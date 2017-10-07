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
package net.dv8tion.jda.core.handle;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.dv8tion.jda.core.utils.SimpleLog;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EventCache
{
    public static final SimpleLog LOG = SimpleLog.getLog(EventCache.class);
    private final Map<Type, TLongObjectMap<List<Runnable>>> eventCache = new HashMap<>();

    public void cache(Type type, long triggerId, Runnable handler)
    {
        TLongObjectMap<List<Runnable>> triggerCache =
                eventCache.computeIfAbsent(type, k -> new TLongObjectHashMap<>());

        List<Runnable> items = triggerCache.get(triggerId);
        if (items == null)
        {
            items = new LinkedList<>();
            triggerCache.put(triggerId, items);
        }

        items.add(handler);
    }

    public void playbackCache(Type type, long triggerId)
    {
        List<Runnable> items;
        try
        {
            items = eventCache.get(type).get(triggerId);
        }
        catch (NullPointerException e)
        {
            //If we encounter an NPE that means something didn't exist.
            return;
        }

        if (items != null && !items.isEmpty())
        {
            EventCache.LOG.debug("Replaying " + items.size() + " events from the EventCache for a " + type + " with id: " + triggerId);
            List<Runnable> itemsCopy = new LinkedList<>(items);
            items.clear();
            for (Runnable item : itemsCopy)
            {
                item.run();
            }
        }
    }

    public int size()
    {
        return (int) eventCache.values().stream()
                .mapToLong(typeMap ->
                    typeMap.valueCollection().stream().mapToLong(List::size).sum())
                .sum();
    }

    public void clear()
    {
        eventCache.clear();
    }

    public void clear(Type type, long id)
    {
        try
        {
            List<Runnable> events = eventCache.get(type).remove(id);
            LOG.debug("Clearing cache for type " + type + " with ID " + id + " (Size: " + events.size() + ')');
        }
        catch (NullPointerException ignored) {}
    }

    public enum Type
    {
        USER, GUILD, CHANNEL, ROLE, RELATIONSHIP, CALL
    }
}
