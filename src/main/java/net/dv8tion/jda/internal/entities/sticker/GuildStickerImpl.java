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
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.sticker.GuildSticker;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.GuildStickerManager;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.managers.GuildStickerManagerImpl;
import net.dv8tion.jda.internal.requests.DeferredRestAction;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.restaction.AuditableRestActionImpl;
import net.dv8tion.jda.internal.utils.EntityString;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Set;

public class GuildStickerImpl extends RichStickerImpl implements GuildSticker
{
    private final long guildId;
    private final JDA jda;
    private Guild guild;
    private User owner;

    private boolean available;

    public GuildStickerImpl(long id, StickerFormat format, String name,
                            Set<String> tags, String description,
                            boolean available, long guildId, JDA jda, User owner)
    {
        super(id, format, name, tags, description);
        this.available = available;
        this.guildId = guildId;
        this.jda = jda;
        this.guild = jda.getGuildById(guildId);
        this.owner = owner;
    }

    @Nonnull
    @Override
    public GuildSticker asGuildSticker()
    {
        return this;
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

    @Nullable
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
    public CacheRestAction<User> retrieveOwner()
    {
        Guild g = getGuild();
        if (g != null && !g.getSelfMember().hasPermission(Permission.MANAGE_GUILD_EXPRESSIONS))
            throw new InsufficientPermissionException(g, Permission.MANAGE_GUILD_EXPRESSIONS);
        return new DeferredRestAction<>(jda, User.class, this::getOwner,
            () -> {
                Route.CompiledRoute route = Route.Stickers.GET_GUILD_STICKER.compile(getGuildId(), getId());
                return new RestActionImpl<>(jda, route, (response, request) -> {
                    DataObject json = response.getObject();
                    return this.owner = json.optObject("user").map(
                        user -> ((JDAImpl) jda).getEntityBuilder().createUser(json.getObject("user"))
                    ).orElseThrow(() -> ErrorResponseException.create(ErrorResponse.MISSING_PERMISSIONS, response));
                });
            });
    }

    @Nonnull
    @Override
    public AuditableRestAction<Void> delete()
    {
        if (guild != null)
            return guild.deleteSticker(this);
        Route.CompiledRoute route = Route.Stickers.DELETE_GUILD_STICKER.compile(getGuildId(), getId());
        return new AuditableRestActionImpl<>(jda, route);
    }

    @Nonnull
    @Override
    public GuildStickerManager getManager()
    {
        return new GuildStickerManagerImpl(getGuild(), getGuildIdLong(), this);
    }

    public GuildStickerImpl setAvailable(boolean available)
    {
        this.available = available;
        return this;
    }

    public GuildStickerImpl copy()
    {
        return new GuildStickerImpl(id, format, name, tags, description, available, guildId, jda, owner);
    }

    @Override
    public String toString()
    {
        return new EntityString(this)
                .setName(name)
                .addMetadata("guild", getGuildId())
                .toString();
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, format, name, getType(), tags, description, available, guildId);
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
            && getType() == other.getType()
            && available == other.available
            && guildId == other.guildId
            && Objects.equals(name, other.name)
            && Objects.equals(description, other.description)
            && Helpers.deepEqualsUnordered(tags, other.tags);
    }
}
