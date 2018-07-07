/*
 * Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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

package net.dv8tion.jda.core.handle;

import gnu.trove.iterator.TLongIterator;
import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import net.dv8tion.jda.core.entities.impl.GuildImpl;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildReadyEvent;
import net.dv8tion.jda.core.utils.Helpers;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

class GuildSetupNode
{
    private final long id;
    private final GuildSetupController controller;
    private final List<JSONObject> cachedEvents = new LinkedList<>();
    private TLongObjectMap<JSONObject> members;
    private TLongSet removedMembers;
    private JSONObject partialGuild;
    private int expectedMemberCount = 1;

    final boolean join;
    final boolean sync;


    GuildSetupNode(long id, GuildSetupController controller, boolean join)
    {
        this.id = id;
        this.controller = controller;
        this.join = join;
        this.sync = controller.isClient();
    }

    boolean containsMember(long userId)
    {
        if (members == null || members.isEmpty())
            return false;
        for (TLongObjectIterator<JSONObject> it = members.iterator(); it.hasNext();)
        {
            it.advance();
            if (it.key() == userId)
                return true;
        }
        return false;
    }

    private void completeSetup()
    {
        JDAImpl api = controller.getJDA();
        for (TLongIterator it = removedMembers.iterator(); it.hasNext(); )
            members.remove(it.next());
        GuildImpl guild = api.getEntityBuilder().createGuild(id, partialGuild, members);
        if (join)
        {
            controller.remove(id);
            api.getEventManager().handle(new GuildJoinEvent(api, api.getResponseTotal(), guild));
        }
        else
        {
            controller.ready(id);
            api.getEventManager().handle(new GuildReadyEvent(api, api.getResponseTotal(), guild));
        }
        GuildSetupController.log.debug("Finished setup for guild {} firing cached events {}", id, cachedEvents.size());
        api.getClient().handle(cachedEvents);
        api.getEventCache().playbackCache(EventCache.Type.GUILD, id);
    }

    void reset()
    {
        expectedMemberCount = 1;
        partialGuild = null;
        if (members != null)
            members.clear();
        if (removedMembers != null)
            removedMembers.clear();
        cachedEvents.clear();
    }

    void handleReady(JSONObject obj)
    {
        if (!sync)
            return;
        partialGuild = obj;
        boolean unavailable = Helpers.optBoolean(partialGuild, "unavailable");
        if (unavailable)
            return;

        controller.addGuildForSyncing(id, join);
    }

    void handleCreate(JSONObject obj)
    {
        if (partialGuild == null)
        {
            partialGuild = obj;
        }
        else
        {
            for (Iterator<String> it = obj.keys(); it.hasNext();)
            {
                String key = it.next();
                partialGuild.put(key, obj.opt(key));
            }
        }
        boolean unavailable = Helpers.optBoolean(partialGuild, "unavailable");
        if (unavailable)
            return;
        if (sync)
        {
            // We are using a client-account and joined a guild
            //  in that case we need to sync before doing anything
            controller.addGuildForSyncing(id, join);
            return;
        }

        expectedMemberCount = partialGuild.getInt("member_count");
        members = new TLongObjectHashMap<>(expectedMemberCount);
        removedMembers = new TLongHashSet();

        if (handleMemberChunk(obj.getJSONArray("members")))
            controller.addGuildForChunking(id, join);
    }

    void handleSync(JSONObject obj)
    {
        if (partialGuild == null)
        {
            //In this case we received a GUILD_DELETE with unavailable = true while syncing
            // however we have to wait for the GUILD_CREATE with unavailable = false before
            // requesting new chunks
            GuildSetupController.log.debug("Dropping sync update due to unavailable guild");
            return;
        }
        for (Iterator<String> it = obj.keys(); it.hasNext();)
        {
            String key = it.next();
            partialGuild.put(key, obj.opt(key));
        }

        expectedMemberCount = partialGuild.getInt("member_count");
        members = new TLongObjectHashMap<>(expectedMemberCount);
        removedMembers = new TLongHashSet();
        if (handleMemberChunk(partialGuild.getJSONArray("members")))
            controller.addGuildForChunking(id, join);
    }

    boolean handleMemberChunk(JSONArray arr)
    {
        if (partialGuild == null)
        {
            //In this case we received a GUILD_DELETE with unavailable = true while chunking
            // however we have to wait for the GUILD_CREATE with unavailable = false before
            // requesting new chunks
            GuildSetupController.log.debug("Dropping member chunk due to unavailable guild");
            return true;
        }
        for (Object o : arr)
        {
            JSONObject obj = (JSONObject) o;
            long id = obj.getJSONObject("user").getLong("id");
            members.put(id, obj);
        }

        if (members.size() >= expectedMemberCount)
        {
            completeSetup();
            return false;
        }
        return true;
    }

    void handleAddMember(JSONObject member)
    {
        if (members == null || removedMembers == null)
            return;
        expectedMemberCount++;
        long userId = member.getJSONObject("user").getLong("id");
        members.put(userId, member);
        removedMembers.remove(userId);
    }

    void handleRemoveMember(JSONObject member)
    {
        if (members == null || removedMembers == null)
            return;
        expectedMemberCount--;
        long userId = member.getJSONObject("user").getLong("id");
        members.remove(userId);
        removedMembers.add(userId);
        EventCache eventCache = controller.getJDA().getEventCache();
        if (!controller.containsMember(userId, this)) // if no other setup node contains this userId we clear it here
            eventCache.clear(EventCache.Type.USER, userId);
    }

    void cacheEvent(JSONObject event)
    {
        GuildSetupController.log.trace("Caching {} event during init", event.getString("t"));
        cachedEvents.add(event);
    }

    void cleanup()
    {
        EventCache eventCache = controller.getJDA().getEventCache();
        eventCache.clear(EventCache.Type.ROLE, id);
        if (partialGuild == null)
            return;

        JSONArray channels = partialGuild.optJSONArray("channels");
        JSONArray roles = partialGuild.optJSONArray("roles");
        if (channels != null)
        {
            for (Object o : channels)
            {
                JSONObject json = (JSONObject) o;
                long id = json.getLong("id");
                eventCache.clear(EventCache.Type.CHANNEL, id);
            }
        }

        if (roles != null)
        {
            for (Object o : roles)
            {
                JSONObject json = (JSONObject) o;
                long id = json.getLong("id");
                eventCache.clear(EventCache.Type.ROLE, id);
            }
        }

        if (members != null)
        {
            for (TLongObjectIterator<JSONObject> it = members.iterator(); it.hasNext();)
            {
                it.advance();
                long userId = it.key();
                if (!controller.containsMember(userId, this)) // if no other setup node contains this userId we clear it here
                    eventCache.clear(EventCache.Type.USER, userId);
            }
        }
    }
}
