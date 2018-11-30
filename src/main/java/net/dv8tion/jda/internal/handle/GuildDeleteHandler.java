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

package net.dv8tion.jda.internal.handle;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.set.TLongSet;
import net.dv8tion.jda.core.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.guild.GuildUnavailableEvent;
import net.dv8tion.jda.core.managers.AudioManager;
import net.dv8tion.jda.core.utils.MiscUtil;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.PrivateChannelImpl;
import net.dv8tion.jda.internal.entities.UserImpl;
import net.dv8tion.jda.internal.managers.AudioManagerImpl;
import net.dv8tion.jda.internal.requests.WebSocketClient;
import net.dv8tion.jda.internal.utils.Helpers;
import net.dv8tion.jda.internal.utils.UnlockHook;
import net.dv8tion.jda.internal.utils.cache.AbstractCacheView;
import net.dv8tion.jda.internal.utils.cache.SnowflakeCacheViewImpl;
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

        GuildImpl guild = (GuildImpl) getJDA().getGuildById(id);
        boolean unavailable = Helpers.optBoolean(content, "unavailable");
        if (guild == null)
        {
            //getJDA().getEventCache().cache(EventCache.Type.GUILD, id, () -> handle(responseNumber, allContent));
            WebSocketClient.LOG
                    .debug("Received GUILD_DELETE for a Guild that is not currently cached. ID: {} unavailable: {}", id,
                           unavailable
                    );
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
                            guild
                    ));
            return null;
        }

        //Remove everything from global cache
        // this prevents some race-conditions for getting audio managers from guilds
        SnowflakeCacheViewImpl<Guild> guildView = getJDA().getGuildMap();
        SnowflakeCacheViewImpl<TextChannel> textView = getJDA().getTextChannelMap();
        SnowflakeCacheViewImpl<VoiceChannel> voiceView = getJDA().getVoiceChannelMap();
        SnowflakeCacheViewImpl<Category> categoryView = getJDA().getCategoryMap();
        guildView.remove(id);
        try (UnlockHook hook = textView.writeLock())
        {
            guild.getTextChannelCache()
                 .forEach(chan -> textView.getMap().remove(chan.getIdLong()));
        }
        try (UnlockHook hook = voiceView.writeLock())
        {
            guild.getVoiceChannelCache()
                 .forEach(chan -> voiceView.getMap().remove(chan.getIdLong()));
        }
        try (UnlockHook hook = categoryView.writeLock())
        {
            guild.getCategoryCache()
                 .forEach(chan -> categoryView.getMap().remove(chan.getIdLong()));
        }
        getJDA().getClient().removeAudioConnection(id);
        final AbstractCacheView<AudioManager> audioManagerView = getJDA().getAudioManagerMap();
        try (UnlockHook hook = audioManagerView.writeLock())
        {
            final TLongObjectMap<AudioManager> audioManagerMap = audioManagerView.getMap();
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
        TLongSet memberIds = guild.getMembersMap().keySet(); // copies keys
        getJDA().getGuildCache().stream()
                .map(GuildImpl.class::cast)
                .forEach(g -> memberIds.removeAll(g.getMembersMap().keySet()));
        // Remember, everything left in memberIds is removed from the userMap
        SnowflakeCacheViewImpl<User> userView = getJDA().getUserMap();
        try (UnlockHook hook = userView.writeLock())
        {
            long selfId = getJDA().getSelfUser().getIdLong();
            memberIds.forEach(memberId -> {
                if (memberId == selfId)
                    return true; // don't remove selfUser from cache
                UserImpl user = (UserImpl) userView.getMap().remove(memberId);
                if (user.hasPrivateChannel())
                {
                    PrivateChannelImpl priv = (PrivateChannelImpl) user.getPrivateChannel();
                    user.setFake(true);
                    priv.setFake(true);
                    getJDA().getFakeUserMap().put(user.getIdLong(), user);
                    getJDA().getFakePrivateChannelMap().put(priv.getIdLong(), priv);
                }
                getJDA().getEventCache().clear(EventCache.Type.USER, memberId);
                return true;
            });
        }

        getJDA().getEventManager().handle(
            new GuildLeaveEvent(
                getJDA(), responseNumber,
                guild));
        getJDA().getEventCache().clear(EventCache.Type.GUILD, id);
        return null;
    }
}
