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

package net.dv8tion.jda.api.requests.restaction;

import net.dv8tion.jda.api.entities.SoundboardSound;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.requests.RestAction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

/**
 * Specialized {@link RestAction} used to create a {@link SoundboardSound} in a guild.
 */
public interface SoundboardSoundCreateAction extends AuditableRestAction<SoundboardSound>
{
    @Nonnull
    @Override
    @CheckReturnValue
    SoundboardSoundCreateAction timeout(long timeout, @Nonnull TimeUnit unit);

    @Nonnull
    @Override
    @CheckReturnValue
    SoundboardSoundCreateAction deadline(long timestamp);

    @Nonnull
    @Override
    @CheckReturnValue
    SoundboardSoundCreateAction setCheck(@Nullable BooleanSupplier checks);

    @Nonnull
    @Override
    @CheckReturnValue
    SoundboardSoundCreateAction addCheck(@Nonnull BooleanSupplier checks);

    /**
     * Sets the <b><u>volume</u></b> of the soundboard sound being created.
     *
     * @param  volume
     *         The new volume
     *
     * @throws IllegalArgumentException
     *         If the provided volume is not between 0-1
     *
     * @return This action for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    SoundboardSoundCreateAction setVolume(double volume);

    /**
     * Sets the <b><u>emoji</u></b> of the soundboard sound being created.
     *
     * @param  emoji
     *         The new emoji, can be {@code null}
     *
     * @return This action for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    SoundboardSoundCreateAction setEmoji(@Nullable Emoji emoji);
}
