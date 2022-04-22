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
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class GuildStickerImpl extends MessageStickerImpl implements GuildSticker
{
    private final Type type;
    private final Set<String> tags;
    private final String description;
    private final boolean available;
    private final Guild guild;
    private final User owner;

    public GuildStickerImpl(long id, StickerFormat format, String name,
                            Type type, Set<String> tags, String description,
                            boolean available, Guild guild, User owner)
    {
        super(id, format, name);
        this.type = type;
        this.tags = tags;
        this.description = description;
        this.available = available;
        this.guild = guild;
        this.owner = owner;
    }

    @NotNull
    @Override
    public Type getType()
    {
        return type;
    }

    @NotNull
    @Override
    public Set<String> getTags()
    {
        return tags;
    }

    @NotNull
    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public boolean isAvailable()
    {
        return available;
    }

    @NotNull
    @Override
    public Guild getGuild()
    {
        return guild;
    }

    @NotNull
    @Override
    public User getOwner()
    {
        return owner;
    }
}
