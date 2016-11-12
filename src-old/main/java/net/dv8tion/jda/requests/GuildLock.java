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
package net.dv8tion.jda.requests;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.utils.SimpleLog;
import org.json.JSONObject;

import java.util.*;

public class GuildLock
{
    public static SimpleLog LOG = SimpleLog.getLog("JDAGuildLock");
    private static Map<JDA, GuildLock> locks = new HashMap<>();

    public static synchronized GuildLock get(JDA jda)
    {
        if (!locks.containsKey(jda))
        {
            locks.put(jda, new GuildLock(jda));
        }
        return locks.get(jda);
    }

    private final JDA api;
    private final Map<String, List<JSONObject>> cache = new HashMap<>();
    private final Set<String> cached = new HashSet<>();

    public boolean isLocked(String guildId)
    {
        return cached.contains(guildId);
    }

    public void lock(String guildId)
    {
        if (!isLocked(guildId))
        {
            cached.add(guildId);
            cache.put(guildId, new LinkedList<>());
        }
    }

    public void unlock(String guildId)
    {
        if (isLocked(guildId))
        {
            cached.remove(guildId);
            List<JSONObject> events = cache.remove(guildId);
            if(events.size() > 0)
            {
                LOG.debug("Replaying " + events.size() + " events for unlocked guild with id " + guildId);
                ((JDAImpl) api).getClient().handle(events);
                LOG.debug("Finished replaying events for guild with id " + guildId);
            }
        }
    }

    public void queue(String guildId, JSONObject event)
    {
        if (isLocked(guildId))
        {
            LOG.debug("Queueing up event for guild with id " + guildId + ": " + event.toString());
            cache.get(guildId).add(event);
        }
    }

    public void clear()
    {
        cache.clear();
        cached.clear();
    }

    private GuildLock(JDA api)
    {
        this.api = api;
    }
}
