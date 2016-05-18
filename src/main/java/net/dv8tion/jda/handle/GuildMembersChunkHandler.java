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
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.utils.DebugUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class GuildMembersChunkHandler extends SocketHandler
{
    private static HashMap<JDA, HashMap<String, Integer>> expectedGuildMembers = new HashMap<>();
    private static HashMap<JDA, HashMap<String, List<JSONArray>>> memberChunksCache = new HashMap<>();

    public GuildMembersChunkHandler(JDAImpl api, int responseNumber)
    {
        super(api, responseNumber);
    }

    @Override
    protected String handleInternally(JSONObject content)
    {
        String guildId = content.getString("guild_id");
        List<JSONArray> memberChunks = memberChunksCache.get(api).get(guildId);
        Integer expectMemberCount = (Integer) expectedGuildMembers.get(api).get(guildId);

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
            new EntityBuilder(api).createGuildSecondPass(guildId, memberChunks);
            memberChunksCache.get(api).remove(guildId);
            expectedGuildMembers.get(api).remove(guildId);
        }
        return null;
    }

    public static void setExpectedGuildMembers(JDA jda, String guildId, int count)
    {
        HashMap<String, Integer> guildMembers = expectedGuildMembers.get(jda);
        if (guildMembers == null)
        {
            guildMembers = new HashMap<>();
            expectedGuildMembers.put(jda, guildMembers);
        }

        if (guildMembers.get(guildId) != null)
            JDAImpl.LOG.warn("Set the count of expected users from GuildMembersChunk even though a value already exists! GuildId: " + guildId);

        guildMembers.put(guildId, count);

        HashMap<String, List<JSONArray>> memberChunks = memberChunksCache.get(jda);
        if (memberChunks == null)
        {
            memberChunks = new HashMap<>();
            memberChunksCache.put(jda, memberChunks);
        }

        if (memberChunks.get(guildId) != null)
            JDAImpl.LOG.warn("Set the memberChunks for MemberChunking for a guild that was already setup for chunking! GuildId: " + guildId);

        memberChunks.put(guildId, new LinkedList<>());
    }

    public static void modifyExpectedGuildMember(JDA jda, String guildId, int changeAmount)
    {
        try
        {
            Integer i = (Integer) expectedGuildMembers.get(jda).get(guildId);
            i += changeAmount;
            expectedGuildMembers.get(jda).put(guildId, i);
        }
        //Ignore. If one of the above things doesn't exist, causing an NPE, then we don't need to worry.
        catch (NullPointerException e) {}
    }
}
