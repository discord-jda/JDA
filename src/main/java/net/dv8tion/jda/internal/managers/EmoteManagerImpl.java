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

package net.dv8tion.jda.internal.managers;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.EmoteManager;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.entities.EmoteImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Checks;
import okhttp3.RequestBody;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class EmoteManagerImpl extends ManagerBase<EmoteManager> implements EmoteManager
{
    protected final EmoteImpl emote;

    protected final List<String> roles = new ArrayList<>();
    protected String name;

    /**
     * Creates a new EmoteManager instance
     *
     * @param  emote
     *         The target {@link net.dv8tion.jda.internal.entities.EmoteImpl EmoteImpl} to modify
     */
    public EmoteManagerImpl(EmoteImpl emote)
    {
        super(emote.getJDA(), Route.Emotes.MODIFY_EMOTE.compile(notNullGuild(emote).getId(), emote.getId()));
        this.emote = emote;
        if (isPermissionChecksEnabled())
            checkPermissions();
    }

    private static Guild notNullGuild(EmoteImpl emote)
    {
        Guild g = emote.getGuild();
        if (g == null)
            throw new IllegalStateException("Cannot modify an emote without shared guild");
        return g;
    }

    @Nonnull
    @Override
    public Emote getEmote()
    {
        return emote;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public EmoteManagerImpl reset(long fields)
    {
        super.reset(fields);
        if ((fields & ROLES) == ROLES)
            withLock(this.roles, List::clear);
        if ((fields & NAME) == NAME)
            this.name = null;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public EmoteManagerImpl reset(long... fields)
    {
        super.reset(fields);
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public EmoteManagerImpl reset()
    {
        super.reset();
        withLock(this.roles, List::clear);
        this.name = null;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public EmoteManagerImpl setName(@Nonnull String name)
    {
        Checks.notBlank(name, "Name");
        name = name.trim();
        Checks.inRange(name, 2, 32, "Name");
        this.name = name;
        set |= NAME;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public EmoteManagerImpl setRoles(Set<Role> roles)
    {
        if (roles == null)
        {
            withLock(this.roles, List::clear);
        }
        else
        {
            Checks.notNull(roles, "Roles");
            roles.forEach((role) ->
            {
                Checks.notNull(role, "Roles");
                Checks.check(role.getGuild().equals(getGuild()), "Roles must all be from the same guild");
            });
            withLock(this.roles, (list) ->
            {
                list.clear();
                roles.stream().map(Role::getId).forEach(list::add);
            });
        }
        set |= ROLES;
        return this;
    }

    @Override
    protected RequestBody finalizeData()
    {
        DataObject object = DataObject.empty();
        if (shouldUpdate(NAME))
            object.put("name", name);
        withLock(this.roles, (list) ->
        {
            if (shouldUpdate(ROLES))
                object.put("roles", DataArray.fromCollection(list));
        });
        reset();
        return getRequestBody(object);
    }

    @Override
    protected boolean checkPermissions()
    {
        if (!getGuild().getSelfMember().hasPermission(Permission.MANAGE_EMOTES_AND_STICKERS))
            throw new InsufficientPermissionException(getGuild(), Permission.MANAGE_EMOTES_AND_STICKERS);
        return super.checkPermissions();
    }
}
