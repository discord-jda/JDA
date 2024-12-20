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

package net.dv8tion.jda.api.events.soundboard.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.SoundboardSound;
import net.dv8tion.jda.api.events.UpdateEvent;
import net.dv8tion.jda.api.events.soundboard.GenericSoundboardSoundEvent;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates that a {@link SoundboardSound} was updated.
 *
 * <p><b>Requirements</b><br>
 * These events require {@link CacheFlag#SOUNDBOARD_SOUNDS} to be enabled,
 * which requires {@link GatewayIntent#GUILD_EMOJIS_AND_STICKERS}.
 *
 * <br>{@link JDABuilder#createLight(String) createLight(String)} disables that CacheFlag by default!
 */
public abstract class GenericSoundboardSoundUpdateEvent<T> extends GenericSoundboardSoundEvent implements UpdateEvent<SoundboardSound, T>
{
    protected final T previous;
    protected final T next;
    protected final String identifier;

    public GenericSoundboardSoundUpdateEvent(
            @Nonnull JDA api, long responseNumber, @Nonnull SoundboardSound soundboardSound,
            @Nullable T previous, @Nullable T next, @Nonnull String identifier)
    {
        super(api, responseNumber, soundboardSound);
        this.previous = previous;
        this.next = next;
        this.identifier = identifier;
    }

    @Nonnull
    @Override
    public SoundboardSound getEntity()
    {
        return getSoundboardSound();
    }

    @Nonnull
    @Override
    public String getPropertyIdentifier()
    {
        return identifier;
    }

    @Nullable
    @Override
    public T getOldValue()
    {
        return previous;
    }

    @Nullable
    @Override
    public T getNewValue()
    {
        return next;
    }
}
