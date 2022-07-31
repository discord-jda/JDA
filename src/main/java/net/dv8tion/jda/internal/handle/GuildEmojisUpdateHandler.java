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

import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.emoji.EmojiAddedEvent;
import net.dv8tion.jda.api.events.emoji.EmojiRemovedEvent;
import net.dv8tion.jda.api.events.emoji.update.EmojiUpdateNameEvent;
import net.dv8tion.jda.api.events.emoji.update.EmojiUpdateRolesEvent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.emoji.RichCustomEmojiImpl;
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
        if (!getJDA().isCacheFlagSet(CacheFlag.EMOJI))
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
        List<RichCustomEmoji> oldEmojis, newEmojis;
        SnowflakeCacheViewImpl<RichCustomEmoji> emojiView = guild.getEmojisView();
        try (UnlockHook hook = emojiView.writeLock())
        {
            TLongObjectMap<RichCustomEmoji> emojiMap = emojiView.getMap();
            oldEmojis = new ArrayList<>(emojiMap.valueCollection()); //snapshot of emoji cache
            newEmojis = new ArrayList<>();
            for (int i = 0; i < array.length(); i++)
            {
                DataObject current = array.getObject(i);
                final long emojiId = current.getLong("id");
                RichCustomEmojiImpl emoji = (RichCustomEmojiImpl) emojiMap.get(emojiId);
                RichCustomEmojiImpl oldEmoji = null;

                if (emoji == null)
                {
                    emoji = new RichCustomEmojiImpl(emojiId, guild);
                    newEmojis.add(emoji);
                }
                else
                {
                    // emoji is in our cache which is why we don't want to remove it in cleanup later
                    oldEmojis.remove(emoji);
                    oldEmoji = emoji.copy();
                }

                emoji.setName(current.getString("name"))
                     .setAnimated(current.getBoolean("animated"))
                     .setManaged(current.getBoolean("managed"));
                //update roles
                DataArray roles = current.getArray("roles");
                Set<Role> newRoles = emoji.getRoleSet();
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
                    // newRoles directly writes to the set contained in the emoji
                    newRoles.remove(r);
                }

                // finally, update the emoji
                emojiMap.put(emoji.getIdLong(), emoji);
                // check for updated fields and fire events
                handleReplace(oldEmoji, emoji);
            }
            for (RichCustomEmoji e : oldEmojis)
                emojiMap.remove(e.getIdLong());
        }
        //cleanup old emojis that don't exist anymore
        for (RichCustomEmoji e : oldEmojis)
        {
            getJDA().handleEvent(
                new EmojiRemovedEvent(
                    getJDA(), responseNumber,
                    e));
        }

        for (RichCustomEmoji e : newEmojis)
        {
            getJDA().handleEvent(
                new EmojiAddedEvent(
                    getJDA(), responseNumber,
                    e));
        }

        return null;
    }

    private void handleReplace(RichCustomEmoji oldEmoji, RichCustomEmoji newEmoji)
    {
        if (oldEmoji == null || newEmoji == null) return;

        if (!Objects.equals(oldEmoji.getName(), newEmoji.getName()))
        {
            getJDA().handleEvent(
                new EmojiUpdateNameEvent(
                    getJDA(), responseNumber,
                    newEmoji, oldEmoji.getName()));
        }

        if (!CollectionUtils.isEqualCollection(oldEmoji.getRoles(), newEmoji.getRoles()))
        {
            getJDA().handleEvent(
                new EmojiUpdateRolesEvent(
                    getJDA(), responseNumber,
                    newEmoji, oldEmoji.getRoles()));
        }

    }
}
