/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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
import net.dv8tion.jda.core.utils.CacheConsumer;
import net.dv8tion.jda.core.utils.JDALogger;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EventCache
{
    public static final Logger LOG = JDALogger.getLog(EventCache.class);
    private final Map<Type, TLongObjectMap<List<CacheNode>>> eventCache = new HashMap<>();

    public synchronized void cache(Type type, long triggerId, long responseTotal, JSONObject event, CacheConsumer handler)
    {
        TLongObjectMap<List<CacheNode>> triggerCache =
                eventCache.computeIfAbsent(type, k -> new TLongObjectHashMap<>());

        List<CacheNode> items = triggerCache.get(triggerId);
        if (items == null)
        {
            items = new LinkedList<>();
            triggerCache.put(triggerId, items);
        }

        items.add(new CacheNode(responseTotal, event, handler));
    }

    public synchronized void playbackCache(Type type, long triggerId)
    {
        List<CacheNode> items;
        try
        {
            items = eventCache.get(type).remove(triggerId);
        }
        catch (NullPointerException e)
        {
            //If we encounter an NPE that means something didn't exist.
            return;
        }

        if (items != null && !items.isEmpty())
        {
            EventCache.LOG.debug("Replaying {} events from the EventCache for a {} with id: {}",
                items.size(), type, triggerId);
            List<CacheNode> itemsCopy = new LinkedList<>(items);
            items.clear();
            for (CacheNode item : itemsCopy)
                item.execute();
        }
    }

    public synchronized int size()
    {
        return (int) eventCache.values().stream()
                .mapToLong(typeMap ->
                    typeMap.valueCollection().stream().mapToLong(List::size).sum())
                .sum();
    }

    public synchronized void clear()
    {
        eventCache.clear();
    }

    public synchronized void clear(Type type, long id)
    {
        try
        {
            List<CacheNode> events = eventCache.get(type).remove(id);
            LOG.debug("Clearing cache for type {} with ID {} (Size: {})", type, id, events.size());
        }
        catch (NullPointerException ignored) {}
    }

    public enum Type
    {
        USER, GUILD, CHANNEL, ROLE, RELATIONSHIP, CALL
    }

    private class CacheNode
    {
        private final long responseTotal;
        private final JSONObject event;
        private final CacheConsumer callback;

        public CacheNode(long responseTotal, JSONObject event, CacheConsumer callback)
        {
            this.responseTotal = responseTotal;
            this.event = event;
            this.callback = callback;
        }

        void execute()
        {
            callback.execute(responseTotal, event);
        }
    }
}
