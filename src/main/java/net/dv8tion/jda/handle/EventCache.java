/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
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
package net.dv8tion.jda.handle;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.utils.SimpleLog;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class EventCache
{
    public static final SimpleLog LOG = SimpleLog.getLog("EventCache");
    private static HashMap<JDA, HashMap<Type, HashMap<String, List<Runnable>>>> eventCache = new HashMap<>();

    protected static void cache(JDA jda, Type type, String triggerId, Runnable handler)
    {
        HashMap<Type, HashMap<String, List<Runnable>>> typeCache = eventCache.get(jda);
        if (typeCache == null)
        {
            typeCache = new HashMap<>();
            eventCache.put(jda, typeCache);
        }

        HashMap<String, List<Runnable>> triggerCache = typeCache.get(type);
        if (triggerCache == null)
        {
            triggerCache = new HashMap<>();
            typeCache.put(type, triggerCache);
        }

        List<Runnable> items = triggerCache.get(triggerId);
        if (items == null)
        {
            items = new LinkedList<>();
            triggerCache.put(triggerId, items);
        }

        items.add(handler);
    }

    protected static void playbackCache(JDA jda, Type type, String triggerId)
    {
        List<Runnable> items;
        try
        {
            items = eventCache.get(jda).get(type).get(triggerId);
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

    public static void clear(JDA jda)
    {
        HashMap cache = eventCache.get(jda);
        if (cache != null)
            cache.clear();
    }

    enum Type
    {
        USER, GUILD, CHANNEL, ROLE
    }
}
