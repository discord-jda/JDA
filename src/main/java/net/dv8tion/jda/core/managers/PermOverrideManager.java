/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.PermissionOverride;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.core.managers.impl.ManagerBase;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.utils.Checks;
import okhttp3.RequestBody;
import org.json.JSONObject;

import javax.annotation.CheckReturnValue;
import java.util.Collection;

/**
 * Manager providing functionality to update one or more fields for a {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}.
 *
 * <p><b>Example</b>
 * <pre>{@code
 * manager.setDenied(Permission.MESSAGE_WRITE)
 *        .setAllowed(Permission.MESSAGE_READ)
 *        .queue();
 * manager.reset(PermOverrideManager.DENIED | PermOverrideManager.ALLOWED)
 *        .grant(Permission.MESSAGE_WRITE)
 *        .clear(Permission.MESSAGE_MANAGE)
 *        .queue();
 * }</pre>
 *
 * @see net.dv8tion.jda.core.entities.PermissionOverride#getManager()
 */
public class PermOverrideManager extends ManagerBase
{
    /** Used to reset the denied field */
    public static final long DENIED      = 0x1;
    /** Used to reset the granted field */
    public static final long ALLOWED     = 0x2;
    /** Used to reset <b>all</b> permissions to their original value */
    public static final long PERMISSIONS = 0x3;

    protected final PermissionOverride override;

    protected long allowed;
    protected long denied;

    /**
     * Creates a new PermOverrideManager instance
     *
     * @param override
     *        The {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride} to manage
     */
    public PermOverrideManager(PermissionOverride override)
    {
        super(override.getJDA(),
              Route.Channels.MODIFY_PERM_OVERRIDE.compile(
                  override.getChannel().getId(),
                  override.isMemberOverride() ? override.getMember().getUser().getId()
                                              : override.getRole().getId()));
        this.override = override;
        this.allowed = override.getAllowedRaw();
        this.denied = override.getDeniedRaw();
        if (isPermissionChecksEnabled())
            checkPermissions();
    }

    private void setupValues()
    {
        if (!shouldUpdate(ALLOWED))
            this.allowed = override.getAllowedRaw();
        if (!shouldUpdate(DENIED))
            this.denied = override.getDeniedRaw();
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
        return override.getGuild();
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
        return override.getChannel();
    }

    /**
     * The target {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}
     * that will be modified by this Manager
     *
     * @return The target {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}
     */
    public PermissionOverride getPermissionOverride()
    {
        return override;
    }

    /**
     * Resets the fields specified by the provided bit-flag pattern.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br>Example: {@code manager.reset(PermOverrideManager.ALLOWED | PermOverrideManager.DENIED);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #DENIED}</li>
     *     <li>{@link #ALLOWED}</li>
     *     <li>{@link #PERMISSIONS}</li>
     * </ul>
     *
     * @param  fields
     *         Integer value containing the flags to reset.
     *
     * @return PermOverrideManager for chaining convenience
     */
    @Override
    @CheckReturnValue
    public PermOverrideManager reset(long fields)
    {
        super.reset(fields);
        return this;
    }

    /**
     * Resets the fields specified by the provided bit-flag patterns.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br>Example: {@code manager.reset(PermOverrideManager.ALLOWED, PermOverrideManager.DENIED);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #DENIED}</li>
     *     <li>{@link #ALLOWED}</li>
     *     <li>{@link #PERMISSIONS}</li>
     * </ul>
     *
     * @param  fields
     *         Integer values containing the flags to reset.
     *
     * @return PermOverrideManager for chaining convenience
     */
    @Override
    @CheckReturnValue
    public PermOverrideManager reset(long... fields)
    {
        super.reset(fields);
        return this;
    }

    /**
     * Resets all fields for this manager.
     *
     * @return PermOverrideManager for chaining convenience
     */
    @Override
    @CheckReturnValue
    public PermOverrideManager reset()
    {
        super.reset();
        return this;
    }

    /**
     * Grants the provided {@link net.dv8tion.jda.core.Permission Permissions} bits
     * to the selected {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}.
     *
     * @param  permissions
     *         The permissions to grant to the selected {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}
     *
     * @return PermOverrideManager for chaining convenience
     */
    @CheckReturnValue
    public PermOverrideManager grant(long permissions)
    {
        if (permissions == 0)
            return this;
        setupValues();
        this.allowed |= permissions;
        this.denied &= ~permissions;
        this.set |= ALLOWED;
        return this;
    }

    /**
     * Grants the provided {@link net.dv8tion.jda.core.Permission Permissions}
     * to the selected {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}.
     *
     * @param  permissions
     *         The permissions to grant to the selected {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}
     *
     * @throws IllegalArgumentException
     *         If any of the provided Permissions is {@code null}
     *
     * @return PermOverrideManager for chaining convenience
     *
     * @see    net.dv8tion.jda.core.Permission#getRaw(net.dv8tion.jda.core.Permission...) Permission.getRaw(Permission...)
     */
    @CheckReturnValue
    public PermOverrideManager grant(Permission... permissions)
    {
        Checks.notNull(permissions, "Permissions");
        return grant(Permission.getRaw(permissions));
    }

    /**
     * Grants the provided {@link net.dv8tion.jda.core.Permission Permissions}
     * to the selected {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}.
     *
     * @param  permissions
     *         The permissions to grant to the selected {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}
     *
     * @throws IllegalArgumentException
     *         If any of the provided Permissions is {@code null}
     *
     * @return PermOverrideManager for chaining convenience
     *
     * @see    java.util.EnumSet EnumSet
     * @see    net.dv8tion.jda.core.Permission#getRaw(java.util.Collection) Permission.getRaw(Collection)
     */
    @CheckReturnValue
    public PermOverrideManager grant(Collection<Permission> permissions)
    {
        return grant(Permission.getRaw(permissions));
    }

    /**
     * Denies the provided {@link net.dv8tion.jda.core.Permission Permissions} bits
     * from the selected {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}.
     *
     * @param  permissions
     *         The permissions to deny from the selected {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}
     *
     * @return PermOverrideManager for chaining convenience
     */
    @CheckReturnValue
    public PermOverrideManager deny(long permissions)
    {
        if (permissions == 0)
            return this;
        setupValues();
        this.denied |= permissions;
        this.allowed &= ~permissions;
        this.set |= DENIED;
        return this;
    }

    /**
     * Denies the provided {@link net.dv8tion.jda.core.Permission Permissions}
     * from the selected {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}.
     *
     * @param  permissions
     *         The permissions to deny from the selected {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}
     *
     * @throws IllegalArgumentException
     *         If any of the provided Permissions is {@code null}
     *
     * @return PermOverrideManager for chaining convenience
     *
     * @see    net.dv8tion.jda.core.Permission#getRaw(net.dv8tion.jda.core.Permission...) Permission.getRaw(Permission...)
     */
    @CheckReturnValue
    public PermOverrideManager deny(Permission... permissions)
    {
        Checks.notNull(permissions, "Permissions");
        return deny(Permission.getRaw(permissions));
    }

    /**
     * Denies the provided {@link net.dv8tion.jda.core.Permission Permissions}
     * from the selected {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}.
     *
     * @param  permissions
     *         The permissions to deny from the selected {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}
     *
     * @throws IllegalArgumentException
     *         If any of the provided Permissions is {@code null}
     *
     * @return PermOverrideManager for chaining convenience
     *
     * @see    java.util.EnumSet EnumSet
     * @see    net.dv8tion.jda.core.Permission#getRaw(java.util.Collection) Permission.getRaw(Collection)
     */
    @CheckReturnValue
    public PermOverrideManager deny(Collection<Permission> permissions)
    {
        return deny(Permission.getRaw(permissions));
    }

    /**
     * Clears the provided {@link net.dv8tion.jda.core.Permission Permissions} bits
     * from the selected {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}.
     * <br>This will cause the provided Permissions to be inherited
     *
     * @param  permissions
     *         The permissions to clear from the selected {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}
     *
     * @return PermOverrideManager for chaining convenience
     */
    @CheckReturnValue
    public PermOverrideManager clear(long permissions)
    {
        setupValues();
        if ((allowed & permissions) != 0)
        {
            this.allowed &= ~permissions;
            this.set |= ALLOWED;
        }

        if ((denied & permissions) != 0)
        {
            this.denied &= ~permissions;
            this.set |= DENIED;
        }

        return this;
    }

    /**
     * Clears the provided {@link net.dv8tion.jda.core.Permission Permissions} bits
     * from the selected {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}.
     * <br>This will cause the provided Permissions to be inherited
     *
     * @param  permissions
     *         The permissions to clear from the selected {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}
     *
     * @throws IllegalArgumentException
     *         If any of the provided Permissions is {@code null}
     *
     * @return PermOverrideManager for chaining convenience
     */
    @CheckReturnValue
    public PermOverrideManager clear(Permission... permissions)
    {
        Checks.notNull(permissions, "Permissions");
        return clear(Permission.getRaw(permissions));
    }

    /**
     * Clears the provided {@link net.dv8tion.jda.core.Permission Permissions} bits
     * from the selected {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}.
     * <br>This will cause the provided Permissions to be inherited
     *
     * @param  permissions
     *         The permissions to clear from the selected {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverride}
     *
     * @throws IllegalArgumentException
     *         If any of the provided Permissions is {@code null}
     *
     * @return PermOverrideManager for chaining convenience
     *
     * @see    java.util.EnumSet EnumSet
     * @see    net.dv8tion.jda.core.Permission#getRaw(java.util.Collection) Permission.getRaw(Collection)
     */
    @CheckReturnValue
    public PermOverrideManager clear(Collection<Permission> permissions)
    {
        return clear(Permission.getRaw(permissions));
    }

    @Override
    protected RequestBody finalizeData()
    {
        String targetId = override.isMemberOverride() ? override.getMember().getUser().getId() : override.getRole().getId();
        // setup missing values here
        setupValues();
        RequestBody data = getRequestBody(
            new JSONObject()
                .put("id", targetId)
                .put("type", override.isMemberOverride() ? "member" : "role")
                .put("allow", this.allowed)
                .put("deny",  this.denied));
        reset();
        return data;
    }

    @Override
    protected boolean checkPermissions()
    {
        if (!getGuild().getSelfMember().hasPermission(getChannel(), Permission.MANAGE_PERMISSIONS))
            throw new InsufficientPermissionException(Permission.MANAGE_PERMISSIONS);
        return super.checkPermissions();
    }
}
