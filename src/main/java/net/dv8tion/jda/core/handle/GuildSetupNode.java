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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
        //TODO: Actually create guild object and add it to cache
        if (join)
        {
            //TODO: Fire GuildJoinEvent
            controller.remove(id);
        }
        else
        {
            //TODO: Fire GuildReadyEvent
            controller.ready(id);
        }
        GuildSetupController.log.debug("Finished setup for guild {} firing cached events {}", id, cachedEvents.size());
        controller.getJDA().getClient().handle(cachedEvents);
    }

    void handleReady(JSONObject obj) {}

    void handleDelete(JSONObject obj) {}

    void handleCreate(JSONObject obj)
    {
        partialGuild = obj;
        expectedMemberCount = partialGuild.getInt("member_count");
        members = new HashSet<>(expectedMemberCount);

        if (isLarge())
            controller.addGuildForChunking(id);
        else
            handleMemberChunk(obj.getJSONArray("members"));
    }

    void handleMemberChunk(JSONArray arr)
    {
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
