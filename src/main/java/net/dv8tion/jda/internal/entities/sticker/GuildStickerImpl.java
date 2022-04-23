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

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.sticker.GuildSticker;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.requests.restaction.AuditableRestActionImpl;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Set;

public class GuildStickerImpl extends RichStickerImpl implements GuildSticker
{
    private final boolean available;
    private final long guildId;
    private final JDA jda;
    private Guild guild;
    private User owner;

    public GuildStickerImpl(long id, StickerFormat format, String name,
                            Type type, Set<String> tags, String description,
                            boolean available, long guildId, JDA jda, User owner)
    {
        super(id, format, name, type, tags, description);
        this.available = available;
        this.guildId = guildId;
        this.jda = jda;
        this.guild = jda.getGuildById(guildId);
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
        Guild realGuild = jda.getGuildById(guildId);
        if (realGuild != null)
            guild = realGuild;
        return guild;
    }

    @Nullable
    @Override
    public User getOwner()
    {
        if (owner != null)
        {
            User realOwner = jda.getUserById(owner.getIdLong());
            if (realOwner != null)
                owner = realOwner;
        }
        return owner;
    }

    @Nonnull
    @Override
    public RestAction<User> retrieveOwner()
    {
        return jda.retrieveSticker(getId()).map(union -> {
            this.owner = union.asGuildSticker().getOwner();
            return this.owner;
        });
    }

    @Nonnull
    @Override
    public AuditableRestAction<Void> delete()
    {
        if (guild != null)
            return guild.deleteSticker(this);
        Route.CompiledRoute route = Route.Stickers.DELETE_STICKER.compile(getGuildId(), getId());
        return new AuditableRestActionImpl<>(jda, route);
    }

    @Override
    public String toString()
    {
        return "RichSticker:" + type + ':' + name + '(' + getId() + ",guild=" + getGuildId() + ')';
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, format, name, type, tags, description, available, guildId);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof GuildStickerImpl))
            return false;
        GuildStickerImpl other = (GuildStickerImpl) obj;
        return id == other.id
            && format == other.format
            && type == other.type
            && available == other.available
            && guildId == other.guildId
            && Objects.equals(name, other.name)
            && Objects.equals(description, other.description)
            && Helpers.deepEqualsUnordered(tags, other.tags);
    }
}
