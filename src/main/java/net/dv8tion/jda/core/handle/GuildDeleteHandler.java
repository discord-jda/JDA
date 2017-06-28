/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter & Florian Spieß
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

import gnu.trove.iterator.TLongIterator;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.client.entities.Relationship;
import net.dv8tion.jda.client.entities.RelationshipType;
import net.dv8tion.jda.client.entities.impl.JDAClientImpl;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.impl.GuildImpl;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.entities.impl.PrivateChannelImpl;
import net.dv8tion.jda.core.entities.impl.UserImpl;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.guild.GuildUnavailableEvent;
import net.dv8tion.jda.core.managers.impl.AudioManagerImpl;
import org.json.JSONObject;

public class GuildDeleteHandler extends SocketHandler
{
    public GuildDeleteHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(JSONObject content)
    {
        final long id = content.getLong("id");
        GuildImpl guild = (GuildImpl) api.getGuildMap().get(id);

        //If the event is attempting to mark the guild as unavailable, but it is already unavailable,
        // ignore the event
        if ((guild == null || !guild.isAvailable()) && content.has("unavailable") && content.getBoolean("unavailable"))
            return null;

        if (api.getGuildLock().isLocked(id))
            return id;

        if (content.has("unavailable") && content.getBoolean("unavailable"))
        {
            guild.setAvailable(false);
            api.getEventManager().handle(
                    new GuildUnavailableEvent(
                            api, responseNumber,
                            guild)
            );
            return null;
        }

        final TLongObjectMap<AudioManagerImpl> audioManagerMap = api.getAudioManagerMap();
        final AudioManagerImpl manager = audioManagerMap.remove(id); // remove manager from central map to avoid old guild references
        if (manager != null) // close existing audio connection if needed
            manager.closeAudioConnection(ConnectionStatus.DISCONNECTED_REMOVED_FROM_GUILD);

        //cleaning up all users that we do not share a guild with anymore
        // Anything left in memberIds will be removed from the main userMap
        //Use a new HashSet so that we don't actually modify the Member map so it doesn't affect Guild#getMembers for the leave event.
        TLongSet memberIds = new TLongHashSet(guild.getMembersMap().keySet());
        for (Guild guildI : api.getGuilds())
        {
            GuildImpl g = (GuildImpl) guildI;
            if (g.equals(guild))
                continue;

            for (TLongIterator it = memberIds.iterator(); it.hasNext();)
            {

                if (g.getMembersMap().containsKey(it.next()))
                    it.remove();
            }
        }

        //If we are a client account, be sure to not remove any users from the cache that are Friends.
        // Remember, everything left in memberIds is removed from the userMap
        if (api.getAccountType() == AccountType.CLIENT)
        {
            TLongObjectMap<Relationship> relationships = ((JDAClientImpl) api.asClient()).getRelationshipMap();
            for (TLongIterator it = memberIds.iterator(); it.hasNext();)
            {
                Relationship rel = relationships.get(it.next());
                if (rel != null && rel.getType() == RelationshipType.FRIEND)
                    it.remove();
            }
        }

        long selfId = api.getSelfUser().getIdLong();
        memberIds.forEach(memberId ->
        {
            if (memberId == selfId)
                return true; // don't remove selfUser from cache
            UserImpl user = (UserImpl) api.getUserMap().remove(memberId);
            if (user.hasPrivateChannel())
            {
                PrivateChannelImpl priv = (PrivateChannelImpl) user.getPrivateChannel();
                user.setFake(true);
                priv.setFake(true);
                api.getFakeUserMap().put(user.getIdLong(), user);
                api.getFakePrivateChannelMap().put(priv.getIdLong(), priv);
            }
            else if (api.getAccountType() == AccountType.CLIENT)
            {
                //While the user might not have a private channel, if this is a client account then the user
                // could be in a Group, and if so we need to change the User object to be fake and
                // place it in the FakeUserMap
                for (Group grp : api.asClient().getGroups())
                {
                    if (grp.getNonFriendUsers().contains(user))
                    {
                        user.setFake(true);
                        api.getFakeUserMap().put(user.getIdLong(), user);
                        break; //Breaks from groups loop, not memberIds loop
                    }
                }
            }

            return true;
        });

        api.getGuildMap().remove(guild.getIdLong());
        guild.getTextChannels().forEach(chan -> api.getTextChannelMap().remove(chan.getIdLong()));
        guild.getVoiceChannels().forEach(chan -> api.getVoiceChannelMap().remove(chan.getIdLong()));
        api.getEventManager().handle(
                new GuildLeaveEvent(
                        api, responseNumber,
                        guild));
        return null;
    }
}
