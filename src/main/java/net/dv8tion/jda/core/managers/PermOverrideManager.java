/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.core.managers;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.PermissionOverride;
import net.dv8tion.jda.core.requests.RestAction;

import java.util.Collection;

/**
 * Facade for a {@link net.dv8tion.jda.core.managers.PermOverrideManagerUpdatable PermOverrideManagerUpdatable} instance.
 * <br>Simplifies managing flow for convenience.
 *
 * <p>This decoration allows to modify a single field by automatically building an update {@link net.dv8tion.jda.core.requests.RestAction RestAction}
 */
public class PermOverrideManager
{
    protected final PermOverrideManagerUpdatable updatable;

    /**
     * Creates a new PermOverrideManager instance
     *
     * @param override
     *        The {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride} to manage
     */
    public PermOverrideManager(PermissionOverride override)
    {
        updatable = new PermOverrideManagerUpdatable(override);
    }

    /**
     * The {@link net.dv8tion.jda.core.JDA JDA} instance of this Manager
     *
     * @return the corresponding JDA instance
     */
    public JDA getJDA()
    {
        return updatable.getJDA();
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.Guild Guild} this Manager's
     * {@link net.dv8tion.jda.core.entities.Channel Channel} is in.
     * <br>This is logically the same as calling {@code getPermissionOverride().getGuild()}
     *
     * @return The parent {@link net.dv8tion.jda.core.entities.Guild Guild}
     */
    public Guild getGuild()
    {
        return updatable.getGuild();
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.Channel Channel} this Manager's
     * {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride} is in.
     * <br>This is logically the same as calling {@code getPermissionOverride().getChannel()}
     *
     * @return The parent {@link net.dv8tion.jda.core.entities.Channel Channel}
     */
    public Channel getChannel()
    {
        return updatable.getChannel();
    }

    /**
     * The target {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}
     * that will be modified by this Manager
     *
     * @return The target {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}
     */
    public PermissionOverride getPermissionOverride()
    {
        return updatable.getPermissionOverride();
    }

    /**
     * Grants the provided {@link net.dv8tion.jda.core.Permission Permissions} bits
     * to the selected {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverridie}.
     * <br>Wraps {@link PermOverrideManagerUpdatable#grant(long)}
     *
     * @param  permissions
     *         The permissions to grant to the selected {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}
     *
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_PERMISSIONS MANAGE_PERMISSIONS}
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction}
     *         <br>Update RestAction from {@link PermOverrideManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.PermOverrideManagerUpdatable#grant(long)
     * @see    net.dv8tion.jda.core.managers.PermOverrideManagerUpdatable#update()
     */
    public RestAction<Void> grant(long permissions)
    {
        return updatable.grant(permissions).update();
    }

    /**
     * Grants the provided {@link net.dv8tion.jda.core.Permission Permissions}
     * to the selected {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverridie}.
     * <br>Wraps {@link PermOverrideManagerUpdatable#grant(long)}
     *
     * @param  permissions
     *         The permissions to grant to the selected {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}
     *
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_PERMISSIONS MANAGE_PERMISSIONS}
     * @throws IllegalArgumentException
     *         If any of the provided Permissions is {@code null}
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction}
     *         <br>Update RestAction from {@link PermOverrideManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.PermOverrideManagerUpdatable#grant(Permission...)
     * @see    net.dv8tion.jda.core.managers.PermOverrideManagerUpdatable#update()
     */
    public RestAction<Void> grant(Permission... permissions)
    {
        return updatable.grant(permissions).update();
    }

    /**
     * Grants the provided {@link net.dv8tion.jda.core.Permission Permissions}
     * to the selected {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverridie}.
     * <br>Wraps {@link PermOverrideManagerUpdatable#grant(long)}
     *
     * @param  permissions
     *         The permissions to grant to the selected {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}
     *
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_PERMISSIONS MANAGE_PERMISSIONS}
     * @throws IllegalArgumentException
     *         If any of the provided Permissions is {@code null}
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction}
     *         <br>Update RestAction from {@link PermOverrideManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.PermOverrideManagerUpdatable#grant(Collection)
     * @see    net.dv8tion.jda.core.managers.PermOverrideManagerUpdatable#update()
     */
    public RestAction<Void> grant(Collection<Permission> permissions)
    {
        return updatable.grant(permissions).update();
    }

    /**
     * Denies the provided {@link net.dv8tion.jda.core.Permission Permissions} bits
     * from the selected {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverridie}.
     * <br>Wraps {@link PermOverrideManagerUpdatable#deny(long)}
     *
     * @param  permissions
     *         The permissions to deny from the selected {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}
     *
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_PERMISSIONS MANAGE_PERMISSIONS}
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction}
     *         <br>Update RestAction from {@link PermOverrideManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.PermOverrideManagerUpdatable#deny(long)
     * @see    net.dv8tion.jda.core.managers.PermOverrideManagerUpdatable#update()
     */
    public RestAction<Void> deny(long permissions)
    {
        return updatable.deny(permissions).update();
    }

    /**
     * Denies the provided {@link net.dv8tion.jda.core.Permission Permissions}
     * from the selected {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverridie}.
     * <br>Wraps {@link PermOverrideManagerUpdatable#deny(long)}
     *
     * @param  permissions
     *         The permissions to deny from the selected {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}
     *
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_PERMISSIONS MANAGE_PERMISSIONS}
     * @throws IllegalArgumentException
     *         If any of the provided Permissions is {@code null}
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction}
     *         <br>Update RestAction from {@link PermOverrideManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.PermOverrideManagerUpdatable#deny(Permission...)
     * @see    net.dv8tion.jda.core.managers.PermOverrideManagerUpdatable#update()
     */
    public RestAction<Void> deny(Permission... permissions)
    {
        return updatable.deny(permissions).update();
    }

    /**
     * Denies the provided {@link net.dv8tion.jda.core.Permission Permissions}
     * from the selected {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverridie}.
     * <br>Wraps {@link PermOverrideManagerUpdatable#deny(long)}
     *
     * @param  permissions
     *         The permissions to deny from the selected {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}
     *
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_PERMISSIONS MANAGE_PERMISSIONS}
     * @throws IllegalArgumentException
     *         If any of the provided Permissions is {@code null}
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction}
     *         <br>Update RestAction from {@link PermOverrideManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.PermOverrideManagerUpdatable#deny(Collection)
     * @see    net.dv8tion.jda.core.managers.PermOverrideManagerUpdatable#update()
     */
    public RestAction<Void> deny(Collection<Permission> permissions)
    {
        return updatable.deny(permissions).update();
    }

    /**
     * Clears the provided {@link net.dv8tion.jda.core.Permission Permissions} bits
     * from the selected {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverridie}.
     * <br>Wraps {@link PermOverrideManagerUpdatable#clear(long)}
     * <br>This will cause the provided Permissions to be inherited
     *
     * @param  permissions
     *         The permissions to clear from the selected {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}
     *
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_PERMISSIONS MANAGE_PERMISSIONS}
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction}
     *         <br>Update RestAction from {@link PermOverrideManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.PermOverrideManagerUpdatable#clear(long)
     * @see    net.dv8tion.jda.core.managers.PermOverrideManagerUpdatable#update()
     */
    public RestAction<Void> clear(long permissions)
    {
        return updatable.clear(permissions).update();
    }

    /**
     * Clears the provided {@link net.dv8tion.jda.core.Permission Permissions} bits
     * from the selected {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverridie}.
     * <br>Wraps {@link PermOverrideManagerUpdatable#clear(long)}
     * <br>This will cause the provided Permissions to be inherited
     *
     * @param  permissions
     *         The permissions to clear from the selected {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}
     *
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_PERMISSIONS MANAGE_PERMISSIONS}
     * @throws IllegalArgumentException
     *         If any of the provided Permissions is {@code null}
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction}
     *         <br>Update RestAction from {@link PermOverrideManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.PermOverrideManagerUpdatable#clear(Permission...)
     * @see    net.dv8tion.jda.core.managers.PermOverrideManagerUpdatable#update()
     */
    public RestAction<Void> clear(Permission... permissions)
    {
        return updatable.clear(permissions).update();
    }

    /**
     * Clears the provided {@link net.dv8tion.jda.core.Permission Permissions} bits
     * from the selected {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverridie}.
     * <br>Wraps {@link PermOverrideManagerUpdatable#clear(long)}
     * <br>This will cause the provided Permissions to be inherited
     *
     * @param  permissions
     *         The permissions to clear from the selected {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}
     *
     * @throws net.dv8tion.jda.core.exceptions.PermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_PERMISSIONS MANAGE_PERMISSIONS}
     * @throws IllegalArgumentException
     *         If any of the provided Permissions is {@code null}
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction}
     *         <br>Update RestAction from {@link PermOverrideManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.PermOverrideManagerUpdatable#clear(Collection)
     * @see    net.dv8tion.jda.core.managers.PermOverrideManagerUpdatable#update()
     */
    public RestAction<Void> clear(Collection<Permission> permissions)
    {
        return updatable.clear(permissions).update();
    }
}
