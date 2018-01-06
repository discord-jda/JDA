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
package net.dv8tion.jda.core.requests;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.utils.JDALogger;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.util.LinkedList;
import java.util.List;

public class GuildLock
{
    public static final Logger LOG = JDALogger.getLog(GuildLock.class);

    private final JDA api;
    private final TLongObjectMap<List<JSONObject>> cache = new TLongObjectHashMap<>();
    private final TLongSet cached = new TLongHashSet();

    public boolean isLocked(long guildId)
    {
        return cached.contains(guildId);
    }

    public void lock(long guildId)
    {
        if (!isLocked(guildId))
        {
            cached.add(guildId);
            cache.put(guildId, new LinkedList<>());
        }
    }

    public void unlock(long guildId)
    {
        if (isLocked(guildId))
        {
            cached.remove(guildId);
            List<JSONObject> events = cache.remove(guildId);
            if(events.size() > 0)
            {
                LOG.debug("Replaying {} events for unlocked guild with id {}", events.size(), guildId);
                ((JDAImpl) api).getClient().handle(events);
                LOG.debug("Finished replaying events for guild with id {}", guildId);
            }
        }
    }

    public void queue(long guildId, JSONObject event)
    {
        if (isLocked(guildId))
        {
            LOG.debug("Queueing up event for guild with id {}: {}", guildId, event);
            cache.get(guildId).add(event);
        }
    }

    public void clear()
    {
        cache.clear();
        cached.clear();
    }

    public GuildLock(JDA api)
    {
        this.api = api;
    }
}
