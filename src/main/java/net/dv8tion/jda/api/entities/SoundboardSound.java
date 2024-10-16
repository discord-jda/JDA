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

package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.requests.RestAction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface SoundboardSound extends ISnowflake
{
    /** Template for {@link #getUrl()}.*/
    String SOUND_URL = "https://cdn.discordapp.com/soundboard-sounds/%s";

    @Nonnull
    JDA getJDA();

    @Nonnull
    default String getUrl()
    {
        return String.format(SOUND_URL, getId());
    }

    @Nonnull
    String getName();

    double getVolume();

    @Nullable
    EmojiUnion getEmoji();

    @Nullable
    Guild getGuild();

    boolean isAvailable();

    @Nullable
    User getUser();

    @Nonnull
    @CheckReturnValue
    RestAction<Void> sendTo(AudioChannel channel);

    @Nonnull
    @CheckReturnValue
    RestAction<Void> delete();
}
