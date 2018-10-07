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

package net.dv8tion.jda.core.handle;

import gnu.trove.iterator.TLongIterator;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.client.entities.Relationship;
import net.dv8tion.jda.client.entities.RelationshipType;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.core.entities.impl.GuildImpl;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.entities.impl.PrivateChannelImpl;
import net.dv8tion.jda.core.entities.impl.UserImpl;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.guild.GuildUnavailableEvent;
import net.dv8tion.jda.core.managers.AudioManager;
import net.dv8tion.jda.core.managers.impl.AudioManagerImpl;
import net.dv8tion.jda.core.requests.WebSocketClient;
import net.dv8tion.jda.core.utils.Helpers;
import net.dv8tion.jda.core.utils.MiscUtil;
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
        boolean wasInit = getJDA().getGuildSetupController().onDelete(id, content);
        if (wasInit)
            return null;

        GuildImpl guild = (GuildImpl) getJDA().getGuildMap().get(id);
        boolean unavailable = Helpers.optBoolean(content, "unavailable");
        if (guild == null)
        {
            //getJDA().getEventCache().cache(EventCache.Type.GUILD, id, () -> handle(responseNumber, allContent));
            WebSocketClient.LOG.debug("Received GUILD_DELETE for a Guild that is not currently cached. ID: {} unavailable: {}", id, unavailable);
            return null;
        }

        //If the event is attempting to mark the guild as unavailable, but it is already unavailable,
        // ignore the event
        if (!guild.isAvailable() && unavailable)
            return null;

        if (unavailable)
        {
            guild.setAvailable(false);
            getJDA().getEventManager().handle(
                new GuildUnavailableEvent(
                    getJDA(), responseNumber,
                    guild));
            return null;
        }

        //Remove everything from global cache
        // this prevents some race-conditions for getting audio managers from guilds
        getJDA().getGuildMap().remove(id);
        guild.getTextChannelCache().forEach(chan -> getJDA().getTextChannelMap().remove(chan.getIdLong()));
        guild.getVoiceChannelCache().forEach(chan -> getJDA().getVoiceChannelMap().remove(chan.getIdLong()));
        guild.getCategoryCache().forEach(chan -> getJDA().getCategoryMap().remove(chan.getIdLong()));

        getJDA().getClient().removeAudioConnection(id);
        final TLongObjectMap<AudioManager> audioManagerMap = getJDA().getAudioManagerMap();
        synchronized (audioManagerMap)
        {
            final AudioManagerImpl manager = (AudioManagerImpl) audioManagerMap.get(id);
            if (manager != null) // close existing audio connection if needed
            {
                MiscUtil.locked(manager.CONNECTION_LOCK, () ->
                {
                    if (manager.isConnected() || manager.isAttemptingToConnect())
                        manager.closeAudioConnection(ConnectionStatus.DISCONNECTED_REMOVED_FROM_GUILD);
                    else
                        audioManagerMap.remove(id);
                });
            }
        }

        //cleaning up all users that we do not share a guild with anymore
        // Anything left in memberIds will be removed from the main userMap
        //Use a new HashSet so that we don't actually modify the Member map so it doesn't affect Guild#getMembers for the leave event.
        TLongSet memberIds = new TLongHashSet(guild.getMembersMap().keySet());
        getJDA().getGuildCache().stream()
                .map(GuildImpl.class::cast)
                .forEach(g -> memberIds.removeAll(g.getMembersMap().keySet()));

        //If we are a client account, be sure to not remove any users from the cache that are Friends.
        // Remember, everything left in memberIds is removed from the userMap
        if (getJDA().getAccountType() == AccountType.CLIENT)
        {
            TLongObjectMap<Relationship> relationships = getJDA().asClient().getRelationshipMap();
            for (TLongIterator it = memberIds.iterator(); it.hasNext();)
            {
                Relationship rel = relationships.get(it.next());
                if (rel != null && rel.getType() == RelationshipType.FRIEND)
                    it.remove();
            }
        }

        long selfId = getJDA().getSelfUser().getIdLong();
        memberIds.forEach(memberId ->
        {
            if (memberId == selfId)
                return true; // don't remove selfUser from cache
            UserImpl user = (UserImpl) getJDA().getUserMap().remove(memberId);
            if (user.hasPrivateChannel())
            {
                PrivateChannelImpl priv = (PrivateChannelImpl) user.getPrivateChannel();
                user.setFake(true);
                priv.setFake(true);
                getJDA().getFakeUserMap().put(user.getIdLong(), user);
                getJDA().getFakePrivateChannelMap().put(priv.getIdLong(), priv);
            }
            else if (getJDA().getAccountType() == AccountType.CLIENT)
            {
                //While the user might not have a private channel, if this is a client account then the user
                // could be in a Group, and if so we need to change the User object to be fake and
                // place it in the FakeUserMap
                for (Group grp : getJDA().asClient().getGroups())
                {
                    if (grp.getNonFriendUsers().contains(user))
                    {
                        user.setFake(true);
                        getJDA().getFakeUserMap().put(user.getIdLong(), user);
                        break; //Breaks from groups loop, not memberIds loop
                    }
                }
            }
            getJDA().getEventCache().clear(EventCache.Type.USER, memberId);
            return true;
        });

        getJDA().getEventManager().handle(
            new GuildLeaveEvent(
                getJDA(), responseNumber,
                guild));
        getJDA().getEventCache().clear(EventCache.Type.GUILD, id);
        return null;
    }
}
