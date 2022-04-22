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

package net.dv8tion.jda.internal.entities.sticker;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.sticker.GuildSticker;

import javax.annotation.Nonnull;
import java.util.Set;

public class GuildStickerImpl extends RichStickerImpl implements GuildSticker
{
    private final boolean available;
    private final long guildId;
    private final Guild guild;
    private final User owner;

    public GuildStickerImpl(long id, StickerFormat format, String name,
                            Type type, Set<String> tags, String description,
                            boolean available, long guildId, Guild guild, User owner)
    {
        super(id, format, name, type, tags, description);
        this.available = available;
        this.guildId = guildId;
        this.guild = guild;
        this.owner = owner;
    }

    @Override
    public boolean isAvailable()
    {
        return available;
    }

    @Override
    public long getGuildIdLong()
    {
        return guildId;
    }

    @Nonnull
    @Override
    public Guild getGuild()
    {
        return guild;
    }

    @Nonnull
    @Override
    public User getOwner()
    {
        return owner;
    }
}
