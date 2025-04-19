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

import net.dv8tion.jda.api.entities.SoundboardSound;
import net.dv8tion.jda.api.events.soundboard.update.SoundboardSoundUpdateEmojiEvent;
import net.dv8tion.jda.api.events.soundboard.update.SoundboardSoundUpdateNameEvent;
import net.dv8tion.jda.api.events.soundboard.update.SoundboardSoundUpdateVolumeEvent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.utils.UnlockHook;
import net.dv8tion.jda.internal.utils.cache.SnowflakeCacheViewImpl;

import java.util.Objects;

public class GuildSoundboardSoundUpdateHandler extends SocketHandler
{
    public GuildSoundboardSoundUpdateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content)
    {
        if (!getJDA().isCacheFlagSet(CacheFlag.SOUNDBOARD_SOUNDS))
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

        SnowflakeCacheViewImpl<SoundboardSound> soundboardSoundsView = guild.getSoundboardSoundsView();
        long soundId = content.getLong("sound_id");
        final SoundboardSound oldSoundboardSound = soundboardSoundsView.get(soundId);
        if (oldSoundboardSound == null)
        {
            getJDA().getEventCache().cache(EventCache.Type.SOUNDBOARD_SOUND, soundId, responseNumber, allContent, this::handle);
            EventCache.LOG.debug("Received a Guild Soundboard Sound Update for SoundboardSound that is not yet cached: {}", content);
            return null;
        }

        final SoundboardSound soundboardSound = api.getEntityBuilder().createSoundboardSound(content);
        try (UnlockHook unlockHook = soundboardSoundsView.writeLock())
        {
            soundboardSoundsView.getMap().put(soundboardSound.getIdLong(), soundboardSound);
        }

        if (!Objects.equals(oldSoundboardSound.getName(), soundboardSound.getName()))
            api.handleEvent(new SoundboardSoundUpdateNameEvent(api, responseNumber, soundboardSound, oldSoundboardSound.getName()));

        if (oldSoundboardSound.getVolume() != soundboardSound.getVolume())
            api.handleEvent(new SoundboardSoundUpdateVolumeEvent(api, responseNumber, soundboardSound, oldSoundboardSound.getVolume()));

        if (!Objects.equals(oldSoundboardSound.getEmoji(), soundboardSound.getEmoji()))
            api.handleEvent(new SoundboardSoundUpdateEmojiEvent(api, responseNumber, soundboardSound, oldSoundboardSound.getEmoji()));

        return null;
    }
}
