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

package net.dv8tion.jda.internal.managers;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.SoundboardSound;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.managers.SoundboardSoundManager;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.Checks;
import okhttp3.RequestBody;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SoundboardSoundManagerImpl extends ManagerBase<SoundboardSoundManager> implements SoundboardSoundManager
{
    private SoundboardSound soundboardSound;
    private String name;
    private double volume;
    private Emoji emoji;

    public SoundboardSoundManagerImpl(SoundboardSound soundboardSound)
    {
        super(soundboardSound.getJDA(), Route.SoundboardSounds.MODIFY_GUILD_SOUNDBOARD_SOUNDS.compile(soundboardSound.getGuild().getId(), soundboardSound.getId()));
        this.soundboardSound = soundboardSound;
    }

    @Nonnull
    @Override
    @SuppressWarnings("DataFlowIssue")
    public Guild getGuild()
    {
        return soundboardSound.getGuild();
    }

    @Nonnull
    @Override
    public SoundboardSound getSoundboardSound()
    {
        final SoundboardSound soundboardSound = getGuild().getSoundboardSoundById(this.soundboardSound.getId());
        if (soundboardSound != null)
            this.soundboardSound = soundboardSound;
        return this.soundboardSound;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public SoundboardSoundManagerImpl reset(long fields)
    {
        super.reset(fields);
        if ((fields & NAME) == NAME)
            this.name = null;
        if ((fields & VOLUME) == VOLUME)
            this.volume = 1;
        if ((fields & EMOJI) == EMOJI)
            this.emoji = null;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public SoundboardSoundManagerImpl reset(long... fields)
    {
        super.reset(fields);
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public SoundboardSoundManagerImpl reset()
    {
        super.reset();
        this.name = null;
        this.volume = 1;
        this.emoji = null;
        return this;
    }

    @Nonnull
    @Override
    public SoundboardSoundManagerImpl setName(@Nonnull String name)
    {
        Checks.notNull(name, "name");
        Checks.check(name.length() >= 2 && name.length() <= 32, "Name must be between 2 and 32 characters");
        this.name = name;
        set |= NAME;
        return this;
    }

    @Nonnull
    @Override
    public SoundboardSoundManagerImpl setVolume(double volume)
    {
        Checks.check(volume >= 0 && volume <= 1, "Volume must be between 0 and 1");
        this.volume = volume;
        set |= VOLUME;
        return this;
    }

    @Nonnull
    @Override
    public SoundboardSoundManagerImpl setEmoji(@Nullable Emoji emoji)
    {
        this.emoji = emoji;
        set |= EMOJI;
        return this;
    }

    @Override
    protected RequestBody finalizeData()
    {
        DataObject object = DataObject.empty().put("name", getSoundboardSound().getName());
        if (shouldUpdate(NAME))
            object.put("name", name);
        if (shouldUpdate(VOLUME))
            object.put("volume", volume);
        if (shouldUpdate(EMOJI))
        {
            if (emoji instanceof CustomEmoji)
                object.put("emoji_id", ((CustomEmoji) emoji).getId());
            else if (emoji != null)
                object.put("emoji_name", emoji.getName());
            else
            {
                object.put("emoji_id", null);
                object.put("emoji_name", null);
            }
        }
        reset();
        return getRequestBody(object);
    }
}
