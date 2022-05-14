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

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ListedEmote;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.EmoteManager;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.managers.EmoteManagerImpl;
import net.dv8tion.jda.internal.requests.DeferredRestAction;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.requests.restaction.AuditableRestActionImpl;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a Custom Emote. (Emoji in official Discord API terminology)
 *
 * @since  2.2
 */
public class EmoteImpl implements ListedEmote
{
    private final long id;
    private final JDAImpl api;
    private final Set<Role> roles;

    private GuildImpl guild;
    private boolean managed = false;
    private boolean available = true;
    private boolean animated = false;
    private String name;
    private User owner;

    public EmoteImpl(long id, GuildImpl guild)
    {
        this.id = id;
        this.api = guild.getJDA();
        this.guild = guild;
        this.roles = ConcurrentHashMap.newKeySet();
    }

    public EmoteImpl(long id, JDAImpl api)
    {
        this.id = id;
        this.api = api;
        this.guild = null;
        this.roles = null;
    }

    @Override
    public GuildImpl getGuild()
    {
        if (guild == null)
            return null;
        GuildImpl realGuild = (GuildImpl) api.getGuildById(guild.getIdLong());
        if (realGuild != null)
            guild = realGuild;
        return guild;
    }

    @Nonnull
    @Override
    public List<Role> getRoles()
    {
        if (!canProvideRoles())
            throw new IllegalStateException("Unable to return roles because this emote is from a message. (We do not know the origin Guild of this emote)");
        return Collections.unmodifiableList(new LinkedList<>(roles));
    }

    @Override
    public boolean canProvideRoles()
    {
        return roles != null;
    }

    @Nonnull
    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public boolean isManaged()
    {
        return managed;
    }

    @Override
    public boolean isAvailable()
    {
        return available;
    }

    @Override
    public long getIdLong()
    {
        return id;
    }

    @Nonnull
    @Override
    public JDAImpl getJDA()
    {
        return api;
    }

    @Override
    public User getOwner()
    {
        return owner;
    }

    @NotNull
    @Override
    public RestAction<User> retrieveOwner()
    {
        if (guild == null)
            throw new IllegalStateException("Unable to retrieve owner of this emote because this emote is from a message. (We do not know the origin Guild of this emote)");
        return new DeferredRestAction<>(api, User.class, this::getOwner, () -> {
            Route.CompiledRoute route = Route.Emotes.GET_EMOTE.compile(guild.getId(), getId());
            return new RestActionImpl<>(api, route, (response, request) -> {
                DataObject user = response.getObject().getObject("user");
                return this.owner = api.getEntityBuilder().createUser(user);
            });
        });
    }

    @Nonnull
    @Override
    public User getUser()
    {
        if (!hasUser())
            throw new IllegalStateException("This emote does not have a user");
        return owner;
    }

    @Override
    public boolean hasUser()
    {
        return owner != null;
    }

    @Nonnull
    @Override
    public EmoteManager getManager()
    {
        return new EmoteManagerImpl(this);
    }

    @Override
    public boolean isAnimated()
    {
        return animated;
    }

    @Nonnull
    @Override
    public AuditableRestAction<Void> delete()
    {
        if (getGuild() == null)
            throw new IllegalStateException("The emote you are trying to delete is not an actual emote we have access to (it is from a message)!");
        if (managed)
            throw new UnsupportedOperationException("You cannot delete a managed emote!");
        if (!getGuild().getSelfMember().hasPermission(Permission.MANAGE_EMOTES_AND_STICKERS))
            throw new InsufficientPermissionException(getGuild(), Permission.MANAGE_EMOTES_AND_STICKERS);

        Route.CompiledRoute route = Route.Emotes.DELETE_EMOTE.compile(getGuild().getId(), getId());
        return new AuditableRestActionImpl<>(getJDA(), route);
    }

    // -- Setters --

    public EmoteImpl setName(String name)
    {
        this.name = name;
        return this;
    }

    public EmoteImpl setAnimated(boolean animated)
    {
        this.animated = animated;
        return this;
    }

    public EmoteImpl setManaged(boolean val)
    {
        this.managed = val;
        return this;
    }

    public EmoteImpl setAvailable(boolean available)
    {
        this.available = available;
        return this;
    }

    public EmoteImpl setOwner(User user)
    {
        this.owner = user;
        return this;
    }

    // -- Set Getter --

    public Set<Role> getRoleSet()
    {
        return this.roles;
    }

    // -- Object overrides --

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof EmoteImpl))
            return false;

        EmoteImpl oEmote = (EmoteImpl) obj;
        return this.id == oEmote.id && getName().equals(oEmote.getName());
    }


    @Override
    public int hashCode()
    {
        return Long.hashCode(id);
    }

    @Override
    public String toString()
    {
        return "E:" + getName() + '(' + getIdLong() + ')';
    }

    @Override
    public EmoteImpl clone()
    {
        EmoteImpl copy = new EmoteImpl(id, getGuild()).setOwner(owner).setManaged(managed).setAnimated(animated).setName(name);
        copy.roles.addAll(roles);
        return copy;
    }
}
