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

import net.dv8tion.jda.core.entities.impl.GuildImpl;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildReadyEvent;
import net.dv8tion.jda.core.utils.Helpers;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

class GuildSetupNode
{
    private final long id;
    private final GuildSetupController controller;
    private final List<JSONObject> cachedEvents = new LinkedList<>();
    private Set<JSONObject> members;
    private JSONObject partialGuild;
    private int expectedMemberCount = 1;

    final boolean join;

    GuildSetupNode(long id, GuildSetupController controller, boolean join)
    {
        this.id = id;
        this.controller = controller;
        this.join = join;
    }

    private void completeSetup()
    {
        JDAImpl api = controller.getJDA();
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
    }

    void reset()
    {
        expectedMemberCount = 1;
        partialGuild = null;
        members.clear();
        cachedEvents.clear();
    }

    void handleReady(JSONObject obj) {} // do we need this?

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

        expectedMemberCount = partialGuild.getInt("member_count");
        members = new HashSet<>(expectedMemberCount);

        if (isLarge())
            controller.addGuildForChunking(id);
        else
            handleMemberChunk(obj.getJSONArray("members"));
    }

    void handleMemberChunk(JSONArray arr)
    {
        //TODO: handle client account (guild-sync)
        for (Object o : arr)
        {
            JSONObject obj = (JSONObject) o;
            members.add(obj);
        }
        if (members.size() == expectedMemberCount)
            completeSetup();
    }

    void updateMemberChunkCount(int change)
    {
        expectedMemberCount += change;
    }

    void cacheEvent(JSONObject event)
    {
        cachedEvents.add(event);
    }

    private boolean isLarge()
    {
        int memberCount = partialGuild.getJSONArray("members").length();
        return memberCount != expectedMemberCount;
    }
}
