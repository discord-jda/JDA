/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

package net.dv8tion.jda.internal.handle;

import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.MemberImpl;
import net.dv8tion.jda.internal.requests.WebSocketClient;
import net.dv8tion.jda.internal.utils.Helpers;

public class GuildMembersChunkHandler extends SocketHandler
{
    public GuildMembersChunkHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content)
    {
        final long guildId = content.getLong("guild_id");
        DataArray members = content.getArray("members");
        GuildImpl guild = (GuildImpl) getJDA().getGuildById(guildId);
        if (guild != null)
        {
            if (api.getClient().getChunkManager().handleChunk(guildId, content))
                return null;
            WebSocketClient.LOG.debug("Received member chunk for guild that is already in cache. GuildId: {} Count: {} Index: {}/{}",
                    guildId, members.length(), content.getInt("chunk_index"), content.getInt("chunk_count"));
            // Chunk handling
            EntityBuilder builder = getJDA().getEntityBuilder();
            if (guild.getPresenceView() != null)
            {
                content.optArray("presences").ifPresent(arr ->
                    arr.stream(DataArray::getObject)
                       .forEach(p -> builder.createPresence(guild, p)));
            }

            for (int i = 0; i < members.length(); i++)
            {
                DataObject object = members.getObject(i);
                MemberImpl member = builder.createMember(guild, object);
                builder.updateMemberCache(member);
            }
            return null;
        }
        getJDA().getGuildSetupController().onMemberChunk(guildId, content);
        return null;
    }

}
