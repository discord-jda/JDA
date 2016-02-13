/**
 *    Copyright 2015-2016 Austin Keener & Michael Ritter
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

import net.dv8tion.jda.entities.impl.JDAImpl;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GuildMembersChunkHandler extends SocketHandler
{
    private static String lastGuildId = null;
    private static List<JSONArray> memberChunks = null;

    public GuildMembersChunkHandler(JDAImpl api, int responseNumber)
    {
        super(api, responseNumber);
    }

    @Override
    public void handle(JSONObject content)
    {
        String guildId = content.getString("guild_id");
        if (lastGuildId == null)
        {
            lastGuildId = guildId;
            memberChunks = new ArrayList<>();
        }
        if (!lastGuildId.equals(guildId))
        {
            new EntityBuilder(api).createGuildSecondPass(lastGuildId, memberChunks);
            lastGuildId = guildId;
            memberChunks = new ArrayList<>();
        }

        JSONArray members = content.getJSONArray("members");
        if (api.isDebug()) System.out.println("GUILD_MEMBER_CHUNK for: " + guildId + "\tMembers: " + members.length());
        memberChunks.add(members);
        if (members.length() != 1000)
        {
            new EntityBuilder(api).createGuildSecondPass(lastGuildId, memberChunks);
            lastGuildId = null;
            memberChunks = null;
        }
    }
}
