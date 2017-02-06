/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
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

import net.dv8tion.jda.core.entities.EntityBuilder;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class GuildMembersChunkHandler extends SocketHandler
{
    HashMap<String, Integer> expectedGuildMembers = new HashMap<>();
    HashMap<String, List<JSONArray>> memberChunksCache = new HashMap<>();


    public GuildMembersChunkHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected String handleInternally(JSONObject content)
    {
        String guildId = content.getString("guild_id");
        List<JSONArray> memberChunks = memberChunksCache.get(guildId);
        Integer expectMemberCount = (Integer) expectedGuildMembers.get(guildId);

        JSONArray members = content.getJSONArray("members");
        JDAImpl.LOG.debug("GUILD_MEMBER_CHUNK for: " + guildId + " \tMembers: " + members.length());
        memberChunks.add(members);

        int currentTotal = 0;
        for (JSONArray arr : memberChunks)
        {
            currentTotal += arr.length();
        }

        if (currentTotal >= expectMemberCount)
        {
            JDAImpl.LOG.debug("Finished chunking for: " + guildId);
            EntityBuilder.get(api).createGuildSecondPass(guildId, memberChunks);
            memberChunksCache.remove(guildId);
            expectedGuildMembers.remove(guildId);
        }
        return null;
    }

    public void setExpectedGuildMembers(String guildId, int count)
    {
        if (expectedGuildMembers.get(guildId) != null)
            JDAImpl.LOG.warn("Set the count of expected users from GuildMembersChunk even though a value already exists! GuildId: " + guildId);

        expectedGuildMembers.put(guildId, count);

        if (memberChunksCache.get(guildId) != null)
            JDAImpl.LOG.warn("Set the memberChunks for MemberChunking for a guild that was already setup for chunking! GuildId: " + guildId);

        memberChunksCache.put(guildId, new LinkedList<>());
    }

    public void modifyExpectedGuildMember(String guildId, int changeAmount)
    {
        try
        {
            Integer i = (Integer) expectedGuildMembers.get(guildId);
            i += changeAmount;
            expectedGuildMembers.put(guildId, i);
        }
        //Ignore. If one of the above things doesn't exist, causing an NPE, then we don't need to worry.
        catch (NullPointerException ignored) {}
    }

    public void clearCache()
    {
        expectedGuildMembers.clear();
        memberChunksCache.clear();
    }
}
