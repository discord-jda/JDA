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

package net.dv8tion.jda.internal.entities;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.SoundboardSound;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.internal.utils.EntityString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SoundboardSoundImpl implements SoundboardSound
{
    private final JDA api;
    private final long id;
    private final String name;
    private final double volume;
    private final EmojiUnion emoji;
    private final Guild guild;
    private final boolean available;
    private final User user;

    public SoundboardSoundImpl(JDA api, long id, String name, double volume, EmojiUnion emoji, Guild guild, boolean available, User user)
    {
        this.api = api;
        this.id = id;
        this.name = name;
        this.volume = volume;
        this.emoji = emoji;
        this.guild = guild;
        this.available = available;
        this.user = user;
    }

    @Override
    public long getIdLong()
    {
        return this.id;
    }

    @Nonnull
    @Override
    public JDA getJDA()
    {
        return api;
    }

    @Nonnull
    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public double getVolume()
    {
        return volume;
    }

    @Nullable
    @Override
    public EmojiUnion getEmoji()
    {
        return emoji;
    }

    @Nullable
    @Override
    public Guild getGuild()
    {
        return guild;
    }

    public boolean isAvailable()
    {
        return available;
    }

    @Nullable
    @Override
    public User getUser()
    {
        return user;
    }

    @Override
    public int hashCode()
    {
        return Long.hashCode(id);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof SoundboardSoundImpl))
            return false;
        return ((SoundboardSoundImpl) obj).getIdLong() == this.id;
    }

    @Override
    public String toString()
    {
        return new EntityString(this)
                .setName(name)
                .toString();
    }
}
