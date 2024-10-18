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
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates that the emoji of a {@link SoundboardSound soundboard sound} changed.
 *
 * <p>Can be used to retrieve the old emoji.
 *
 * <p><b>Requirements</b><br>
 * This event require {@link CacheFlag#SOUNDBOARD_SOUNDS} to be enabled,
 * which requires {@link GatewayIntent#GUILD_EMOJIS_AND_STICKERS}.
 *
 * <br>{@link JDABuilder#createLight(String) createLight(String)} disables that CacheFlag by default!
 *
 * <p>Identifier: {@value IDENTIFIER}
 */
public class SoundboardSoundUpdateEmojiEvent extends GenericSoundboardSoundUpdateEvent<EmojiUnion>
{
    public static final String IDENTIFIER = "emoji";

    public SoundboardSoundUpdateEmojiEvent(@Nonnull JDA api, long responseNumber, @Nonnull SoundboardSound soundboardSound, EmojiUnion oldEmoji)
    {
        super(api, responseNumber, soundboardSound, oldEmoji, soundboardSound.getEmoji(), IDENTIFIER);
    }

    /**
     * The old emoji
     *
     * @return The old emoji
     */
    @Nullable
    public EmojiUnion getOldEmoji()
    {
        return getOldValue();
    }

    /**
     * The new emoji
     *
     * @return The new emoji
     */
    @Nullable
    public EmojiUnion getNewEmoji()
    {
        return getNewValue();
    }

    @Nullable
    @Override
    public EmojiUnion getOldValue()
    {
        return super.getOldValue();
    }

    @Nullable
    @Override
    public EmojiUnion getNewValue()
    {
        return super.getNewValue();
    }
}
