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
import net.dv8tion.jda.api.entities.SoundboardSound;

import javax.annotation.Nonnull;

/**
 * Indicates that the name of a {@link SoundboardSound soundboard sound} changed.
 *
 * <p>Can be used to retrieve the old name
 *
 * <p><b>Requirements</b><br>
 *
 * <p>This event requires the {@link net.dv8tion.jda.api.utils.cache.CacheFlag#SOUNDBOARD_SOUNDS SOUNDBOARD_SOUNDS} CacheFlag to be enabled, which requires
 * the {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_EMOJIS_AND_STICKERS GUILD_EMOJIS_AND_STICKERS} intent.
 *
 * <br>{@link net.dv8tion.jda.api.JDABuilder#createLight(String) createLight(String)} disables that CacheFlag by default!
 *
 * <p>Identifier: {@value IDENTIFIER}
 */
public class SoundboardSoundUpdateNameEvent extends GenericSoundboardSoundUpdateEvent<String>
{
    public static final String IDENTIFIER = "name";

    public SoundboardSoundUpdateNameEvent(@Nonnull JDA api, long responseNumber, @Nonnull SoundboardSound soundboardSound, @Nonnull String oldName)
    {
        super(api, responseNumber, soundboardSound, oldName, soundboardSound.getName(), IDENTIFIER);
    }

    /**
     * The old name
     *
     * @return The old name
     */
    @Nonnull
    public String getOldName()
    {
        return getOldValue();
    }

    /**
     * The new name
     *
     * @return The new name
     */
    @Nonnull
    public String getNewName()
    {
        return getNewValue();
    }

    @Nonnull
    @Override
    @SuppressWarnings("DataFlowIssue")
    public String getOldValue()
    {
        return super.getOldValue();
    }

    @Nonnull
    @Override
    @SuppressWarnings("DataFlowIssue")
    public String getNewValue()
    {
        return super.getNewValue();
    }
}