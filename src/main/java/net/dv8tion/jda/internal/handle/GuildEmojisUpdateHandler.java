/*
 * Copyright 2015-2020 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.emote.EmoteAddedEvent;
import net.dv8tion.jda.api.events.emote.EmoteRemovedEvent;
import net.dv8tion.jda.api.events.emote.update.EmoteUpdateNameEvent;
import net.dv8tion.jda.api.events.emote.update.EmoteUpdateRolesEvent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.EmoteImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.utils.UnlockHook;
import net.dv8tion.jda.internal.utils.cache.SnowflakeCacheViewImpl;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;

public class GuildEmojisUpdateHandler extends SocketHandler
{
    public GuildEmojisUpdateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content)
    {
        if (!getJDA().isCacheFlagSet(CacheFlag.EMOTE))
            return null;
        final long guildId = content.getLong("guild_id");
        if (getJDA().getGuildSetupController().isLocked(guildId))
            return guildId;

        GuildImpl guild = (GuildImpl) getJDA().getGuildById(guildId);
        if (guild == null)
        {
            getJDA().getEventCache().cache(EventCache.Type.GUILD, guildId, responseNumber, allContent, this::handle);
            return null;
        }

        DataArray array = content.getArray("emojis");
        List<Emote> oldEmotes, newEmotes;
        SnowflakeCacheViewImpl<Emote> emoteView = guild.getEmotesView();
        try (UnlockHook hook = emoteView.writeLock())
        {
            TLongObjectMap<Emote> emoteMap = emoteView.getMap();
            oldEmotes = new ArrayList<>(emoteMap.valueCollection()); //snapshot of emote cache
            newEmotes = new ArrayList<>();
            for (int i = 0; i < array.length(); i++)
            {
                DataObject current = array.getObject(i);
                final long emoteId = current.getLong("id");
                EmoteImpl emote = (EmoteImpl) emoteMap.get(emoteId);
                EmoteImpl oldEmote = null;

                if (emote == null)
                {
                    emote = new EmoteImpl(emoteId, guild);
                    newEmotes.add(emote);
                }
                else
                {
                    // emote is in our cache which is why we don't want to remove it in cleanup later
                    oldEmotes.remove(emote);
                    oldEmote = emote.clone();
                }

                emote.setName(current.getString("name"))
                     .setAnimated(current.getBoolean("animated"))
                     .setManaged(current.getBoolean("managed"));
                //update roles
                DataArray roles = current.getArray("roles");
                Set<Role> newRoles = emote.getRoleSet();
                Set<Role> oldRoles = new HashSet<>(newRoles); //snapshot of cached roles
                for (int j = 0; j < roles.length(); j++)
                {
                    Role role = guild.getRoleById(roles.getString(j));
                    if (role != null)
                    {
                        newRoles.add(role);
                        oldRoles.remove(role);
                    }
                }

                //cleanup old cached roles that were not found in the JSONArray
                for (Role r : oldRoles)
                {
                    // newRoles directly writes to the set contained in the emote
                    newRoles.remove(r);
                }

                // finally, update the emote
                emoteMap.put(emote.getIdLong(), emote);
                // check for updated fields and fire events
                handleReplace(oldEmote, emote);
            }
            for (Emote e : oldEmotes)
                emoteMap.remove(e.getIdLong());
        }
        //cleanup old emotes that don't exist anymore
        for (Emote e : oldEmotes)
        {
            getJDA().handleEvent(
                new EmoteRemovedEvent(
                    getJDA(), responseNumber,
                    e));
        }

        for (Emote e : newEmotes)
        {
            getJDA().handleEvent(
                new EmoteAddedEvent(
                    getJDA(), responseNumber,
                    e));
        }

        return null;
    }

    private void handleReplace(Emote oldEmote, Emote newEmote)
    {
        if (oldEmote == null || newEmote == null) return;

        if (!Objects.equals(oldEmote.getName(), newEmote.getName()))
        {
            getJDA().handleEvent(
                new EmoteUpdateNameEvent(
                    getJDA(), responseNumber,
                    newEmote, oldEmote.getName()));
        }

        if (!CollectionUtils.isEqualCollection(oldEmote.getRoles(), newEmote.getRoles()))
        {
            getJDA().handleEvent(
                new EmoteUpdateRolesEvent(
                    getJDA(), responseNumber,
                    newEmote, oldEmote.getRoles()));
        }

    }

}
