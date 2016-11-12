/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.dv8tion.jda.core.managers;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.PermissionOverride;
import net.dv8tion.jda.core.requests.RestAction;

import java.util.Collection;

public class PermOverrideManager
{
    protected final PermOverrideManagerUpdatable updatable;

    public PermOverrideManager(PermissionOverride override)
    {
        updatable = new PermOverrideManagerUpdatable(override);
    }

    public JDA getJDA()
    {
        return updatable.getJDA();
    }

    public Guild getGuild()
    {
        return updatable.getGuild();
    }

    public Channel getChannel()
    {
        return updatable.getChannel();
    }

    public PermissionOverride getPermissionOverride()
    {
        return updatable.getPermissionOverride();
    }

    public RestAction<Void> grant(long permissions)
    {
        return updatable.grant(permissions).update();
    }

    public RestAction<Void> grant(Permission... permissions)
    {
        return updatable.grant(permissions).update();
    }

    public RestAction<Void> grant(Collection<Permission> permissions)
    {
        return updatable.grant(permissions).update();
    }

    public RestAction<Void> deny(long permissions)
    {
        return updatable.deny(permissions).update();
    }

    public RestAction<Void> deny(Permission... permissions)
    {
        return updatable.deny(permissions).update();
    }

    public RestAction<Void> deny(Collection<Permission> permissions)
    {
        return updatable.deny(permissions).update();
    }

    public RestAction<Void> clear(long permissions)
    {
        return updatable.clear(permissions).update();
    }

    public RestAction<Void> clear(Permission... permissions)
    {
        return updatable.clear(permissions).update();
    }

    public RestAction<Void> clear(Collection<Permission> permissions)
    {
        return updatable.clear(permissions).update();
    }
}
