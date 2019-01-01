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

package net.dv8tion.jda.api.requests.restaction;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.internal.entities.AbstractChannelImpl;
import net.dv8tion.jda.internal.entities.PermissionOverrideImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Checks;
import okhttp3.RequestBody;
import org.json.JSONObject;

import javax.annotation.CheckReturnValue;
import java.util.Collection;
import java.util.EnumSet;
import java.util.function.BooleanSupplier;

/**
 * Extension of {@link net.dv8tion.jda.api.requests.RestAction RestAction} specifically
 * designed to create a {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverride}
 * for a {@link net.dv8tion.jda.api.entities.GuildChannel GuildChannel}.
 * This extension allows setting properties before executing the action.
 *
 * @since  3.0
 */
public class PermissionOverrideAction extends AuditableRestAction<PermissionOverride>
{
    private long allow = 0;
    private long deny = 0;
    private final GuildChannel channel;

    private final Member member;
    private final Role role;

    /**
     * Creates a new PermissionOverrideAction instance
     *
     * @param api
     *        The current JDA instance
     * @param route
     *        The {@link net.dv8tion.jda.internal.requests.Route.CompiledRoute Route.CompiledRoute} to be used for rate limit handling
     * @param channel
     *        The target {@link net.dv8tion.jda.api.entities.GuildChannel GuildChannel} for the PermissionOverride
     * @param member
     *        The target {@link net.dv8tion.jda.api.entities.Member Member} that will be affected by the PermissionOverride
     */
    public PermissionOverrideAction(JDA api, Route.CompiledRoute route, GuildChannel channel, Member member)
    {
        super(api, route);
        this.channel = channel;
        this.member = member;
        this.role = null;
    }

    /**
     * Creates a new PermissionOverrideAction instance
     *
     * @param api
     *        The current JDA instance
     * @param route
     *        The {@link net.dv8tion.jda.internal.requests.Route.CompiledRoute Route.CompiledRoute} to be used for rate limit handling
     * @param channel
     *        The target {@link net.dv8tion.jda.api.entities.GuildChannel GuildChannel} for the PermissionOverride
     * @param role
     *        The target {@link net.dv8tion.jda.api.entities.Role Role} that will be affected by the PermissionOverride
     */
    public PermissionOverrideAction(JDA api, Route.CompiledRoute route, GuildChannel channel, Role role)
    {
        super(api, route);
        this.channel = channel;
        this.member = null;
        this.role = role;
    }

    @Override
    public PermissionOverrideAction setCheck(BooleanSupplier checks)
    {
        return (PermissionOverrideAction) super.setCheck(checks);
    }

    /**
     * The currently set of allowed permission bits.
     * <br>This value represents all <b>granted</b> permissions
     * in the raw bitwise representation.
     *
     * <p>Use {@link #getAllowedPermissions()} to retrieve a {@link java.util.List List}
     * with {@link net.dv8tion.jda.api.Permission Permissions} for this value
     *
     * @return long value of granted permissions
     */
    public long getAllow()
    {
        return allow;
    }

    /**
     * Set of {@link net.dv8tion.jda.api.Permission Permissions}
     * that would be <b>granted</b> by the PermissionOverride that is created by this action.
     * <br><u>Changes to the returned set do not affect this entity directly.</u>
     *
     * @return set of granted {@link net.dv8tion.jda.api.Permission Permissions}
     */
    public EnumSet<Permission> getAllowedPermissions()
    {
        return Permission.getPermissions(allow);
    }


    /**
     * The currently set of denied permission bits.
     * <br>This value represents all <b>denied</b> permissions
     * in the raw bitwise representation.
     *
     * <p>Use {@link #getDeniedPermissions()} to retrieve a {@link java.util.List List}
     * with {@link net.dv8tion.jda.api.Permission Permissions} for this value
     *
     * @return long value of denied permissions
     */
    public long getDeny()
    {
        return deny;
    }

    /**
     * Set of {@link net.dv8tion.jda.api.Permission Permissions}
     * that would be <b>denied</b> by the PermissionOverride that is created by this action.
     * <br><u>Changes to the returned set do not affect this entity directly.</u>
     *
     * @return set of denied {@link net.dv8tion.jda.api.Permission Permissions}
     */
    public EnumSet<Permission> getDeniedPermissions()
    {
        return Permission.getPermissions(deny);
    }


    /**
     * The currently set of inherited permission bits.
     * <br>This value represents all permissions that are not explicitly allowed or denied
     * in their raw bitwise representation.
     * <br>Inherited Permissions are permissions that are defined by other rules
     * from maybe other PermissionOverrides or a Role.
     *
     * <p>Use {@link #getInheritedPermissions()} to retrieve a {@link java.util.List List}
     * with {@link net.dv8tion.jda.api.Permission Permissions} for this value
     *
     * @return long value of inherited permissions
     */
    public long getInherited()
    {
        return ~allow & ~deny;
    }

    /**
     * Set of {@link net.dv8tion.jda.api.Permission Permissions}
     * that would be <b>inherited</b> from other permission holders.
     * <br>Permissions returned are not explicitly granted or denied!
     * <br><u>Changes to the returned set do not affect this entity directly.</u>
     *
     * @return set of inherited {@link net.dv8tion.jda.api.Permission Permissions}
     *
     * @see    #getInherited()
     */
    public EnumSet<Permission> getInheritedPermissions()
    {
        return Permission.getPermissions(getInherited());
    }


    /**
     * Whether this Action will
     * create a {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverride}
     * for a {@link net.dv8tion.jda.api.entities.Member Member} or not
     *
     * @return True, if this is targeting a Member
     *         If this is {@code false} it is targeting a {@link net.dv8tion.jda.api.entities.Role Role}. ({@link #isRole()})
     */
    public boolean isMember()
    {
        return member != null;
    }

    /**
     * Whether this Action will
     * create a {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverride}
     * for a {@link net.dv8tion.jda.api.entities.Role Role} or not
     *
     * @return True, if this is targeting a Role.
     *         If this is {@code false} it is targeting a {@link net.dv8tion.jda.api.entities.Member Member}. ({@link #isMember()})
     */
    public boolean isRole()
    {
        return role != null;
    }


    /**
     * Sets the value of explicitly granted permissions
     * using the bitwise representation of a set of {@link net.dv8tion.jda.api.Permission Permissions}.
     * <br>This value can be retrieved through {@link net.dv8tion.jda.api.Permission#getRaw(net.dv8tion.jda.api.Permission...) Permissions.getRaw(Permission...)}!
     * <br><b>Note: Permissions not marked as {@link net.dv8tion.jda.api.Permission#isChannel() isChannel()} will have no affect!</b>
     *
     * <p>All newly granted permissions will be removed from the currently set denied permissions.
     * <br>{@code allow = allowBits; deny = deny & ~allowBits;}
     *
     * @param  allowBits
     *         The <b>positive</b> bits representing the granted
     *         permissions for the new PermissionOverride
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided bits are negative
     *         or higher than {@link net.dv8tion.jda.api.Permission#ALL_PERMISSIONS Permission.ALL_PERMISSIONS}
     *
     * @return The current PermissionOverrideAction - for chaining convenience
     *
     * @see    #setAllow(java.util.Collection) setAllow(Collection)
     * @see    #setAllow(net.dv8tion.jda.api.Permission...) setAllow(Permission...)
     */
    @CheckReturnValue
    public PermissionOverrideAction setAllow(long allowBits)
    {
        Checks.notNegative(allowBits, "Granted permissions value");
        Checks.check(allowBits <= Permission.ALL_PERMISSIONS, "Specified allow value may not be greater than a full permission set");
        this.allow = allowBits;
        this.deny &= ~allowBits;
        return this;
    }

    /**
     * Sets the value of explicitly granted permissions
     * using a Collection of {@link net.dv8tion.jda.api.Permission Permissions}.
     * <br><b>Note: Permissions not marked as {@link net.dv8tion.jda.api.Permission#isChannel() isChannel()} will have no affect!</b>
     *
     * <p>Example: {@code setAllow(EnumSet.of(Permission.MESSAGE_READ))}</p>
     *
     * @param  permissions
     *         The Collection of Permissions representing the granted
     *         permissions for the new PermissionOverride.
     *         <br>If the provided value is {@code null} the permissions are reset to the default of none
     *
     * @throws java.lang.IllegalArgumentException
     *         If the any of the specified Permissions is {@code null}
     *
     * @return The current PermissionOverrideAction - for chaining convenience
     *
     * @see    java.util.EnumSet EnumSet
     * @see    #setAllow(net.dv8tion.jda.api.Permission...) setAllow(Permission...)
     */
    @CheckReturnValue
    public PermissionOverrideAction setAllow(Collection<Permission> permissions)
    {
        if (permissions == null || permissions.isEmpty())
            return setAllow(0);
        Checks.noneNull(permissions, "Permission");
        return setAllow(Permission.getRaw(permissions));
    }

    /**
     * Sets the value of explicitly granted permissions
     * using a set of {@link net.dv8tion.jda.api.Permission Permissions}.
     * <br><b>Note: Permissions not marked as {@link net.dv8tion.jda.api.Permission#isChannel() isChannel()} will have no affect!</b>
     *
     * @param  permissions
     *         The Permissions representing the granted
     *         permissions for the new PermissionOverride.
     *         <br>If the provided value is {@code null} the permissions are reset to the default of none
     *
     * @throws java.lang.IllegalArgumentException
     *         If the any of the specified Permissions is {@code null}
     *
     * @return The current PermissionOverrideAction - for chaining convenience
     */
    @CheckReturnValue
    public PermissionOverrideAction setAllow(Permission... permissions)
    {
        if (permissions == null || permissions.length < 1)
            return setAllow(0);
        Checks.noneNull(permissions, "Permission");
        return setAllow(Permission.getRaw(permissions));
    }


    /**
     * Sets the value of explicitly denied permissions
     * using the bitwise representation of a set of {@link net.dv8tion.jda.api.Permission Permissions}.
     * <br>This value can be retrieved through {@link net.dv8tion.jda.api.Permission#getRaw(net.dv8tion.jda.api.Permission...) Permissions.getRaw(Permission...)}!
     * <br><b>Note: Permissions not marked as {@link net.dv8tion.jda.api.Permission#isChannel() isChannel()} will have no affect!</b>
     *
     * <p>All newly denied permissions will be removed from the currently set allowed permissions.
     * <br>{@code deny = denyBits; allow = allow & ~denyBits;}
     *
     * @param  denyBits
     *         The <b>positive</b> bits representing the denied
     *         permissions for the new PermissionOverride
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided bits are negative
     *         or higher than {@link net.dv8tion.jda.api.Permission#ALL_PERMISSIONS Permission.ALL_PERMISSIONS}
     *
     * @return The current PermissionOverrideAction - for chaining convenience
     *
     * @see    #setDeny(java.util.Collection) setDeny(Collection)
     * @see    #setDeny(net.dv8tion.jda.api.Permission...) setDeny(Permission...)
     */
    @CheckReturnValue
    public PermissionOverrideAction setDeny(long denyBits)
    {
        Checks.notNegative(denyBits, "Denied permissions value");
        Checks.check(denyBits <= Permission.ALL_PERMISSIONS, "Specified allow value may not be greater than a full permission set");
        this.deny = denyBits;
        this.allow &= ~denyBits;
        return this;
    }

    /**
     * Sets the value of explicitly denied permissions
     * using a Collection of {@link net.dv8tion.jda.api.Permission Permissions}.
     * <br><b>Note: Permissions not marked as {@link net.dv8tion.jda.api.Permission#isChannel() isChannel()} will have no affect!</b>
     *
     * <p>Example: {@code setDeny(EnumSet.of(Permission.MESSAGE_WRITE, Permission.MESSAGE_EXT_EMOJI))}</p>
     *
     * @param  permissions
     *         The Collection of Permissions representing the denied
     *         permissions for the new PermissionOverride.
     *         <br>If the provided value is {@code null} the permissions are reset to the default of none
     *
     * @throws java.lang.IllegalArgumentException
     *         If the any of the specified Permissions is {@code null}
     *
     * @return The current PermissionOverrideAction - for chaining convenience
     *
     * @see    java.util.EnumSet EnumSet
     * @see    #setDeny(net.dv8tion.jda.api.Permission...) setDeny(Permission...)
     */
    @CheckReturnValue
    public PermissionOverrideAction setDeny(Collection<Permission> permissions)
    {
        if (permissions == null || permissions.isEmpty())
            return setDeny(0);
        Checks.noneNull(permissions, "Permission");
        return setDeny(Permission.getRaw(permissions));
    }

    /**
     * Sets the value of explicitly denied permissions
     * using a set of {@link net.dv8tion.jda.api.Permission Permissions}.
     * <br><b>Note: Permissions not marked as {@link net.dv8tion.jda.api.Permission#isChannel() isChannel()} will have no affect!</b>
     *
     * @param  permissions
     *         The Permissions representing the denied
     *         permissions for the new PermissionOverride.
     *         <br>If the provided value is {@code null} the permissions are reset to the default of none
     *
     * @throws java.lang.IllegalArgumentException
     *         If the any of the specified Permissions is {@code null}
     *
     * @return The current PermissionOverrideAction - for chaining convenience
     */
    @CheckReturnValue
    public PermissionOverrideAction setDeny(Permission... permissions)
    {
        if (permissions == null || permissions.length < 1)
            return setDeny(0);
        Checks.noneNull(permissions, "Permission");
        return setDeny(Permission.getRaw(permissions));
    }


    /**
     * Combination of {@link #setAllow(long)} and {@link #setDeny(long)}
     * <br>First sets the allow bits and then the deny bits.
     *
     * @param  allowBits
     *         An unsigned bitwise representation
     *         of granted Permissions
     * @param  denyBits
     *         An unsigned bitwise representation
     *         of denied Permissions
     *
     * @throws java.lang.IllegalArgumentException
     *         If any of the provided bits are negative
     *         or higher than {@link net.dv8tion.jda.api.Permission#ALL_PERMISSIONS Permission.ALL_PERMISSIONS}
     *
     * @return The current PermissionOverrideAction - for chaining convenience
     *
     * @see    #setPermissions(java.util.Collection, java.util.Collection)
     * @see    net.dv8tion.jda.api.Permission#getRaw(net.dv8tion.jda.api.Permission...) Permission.getRaw(Permission...)
     * @see    net.dv8tion.jda.api.Permission#getRaw(java.util.Collection)  Permission.getRaw(Collection)
     */
    @CheckReturnValue
    public PermissionOverrideAction setPermissions(long allowBits, long denyBits)
    {
        setAllow(allowBits);
        setDeny(denyBits);
        return this;
    }

    /**
     * Combination of {@link #setAllow(java.util.Collection)} and {@link #setDeny(java.util.Collection)}
     * <br>First sets the granted permissions and then the denied permissions.
     * <br>If a passed collection is {@code null} it resets the represented value to {@code 0} - no permission specifics.
     *
     * <p>Example: {@code setPermissions(EnumSet.of(Permission.MESSAGE_READ), EnumSet.of(Permission.MESSAGE_WRITE, Permission.MESSAGE_EXT_EMOJI))}
     *
     * @param  grantPermissions
     *         A Collection of {@link net.dv8tion.jda.api.Permission Permissions}
     *         representing all explicitly granted Permissions for the PermissionOverride
     * @param  denyPermissions
     *         A Collection of {@link net.dv8tion.jda.api.Permission Permissions}
     *         representing all explicitly denied Permissions for the PermissionOverride
     *
     * @throws java.lang.IllegalArgumentException
     *         If the any of the specified Permissions is {@code null}
     *
     * @return The current PermissionOverrideAction - for chaining convenience
     *
     * @see    java.util.EnumSet EnumSet
     * @see    net.dv8tion.jda.api.Permission#getRaw(java.util.Collection) Permission.getRaw(Collection)
     */
    @CheckReturnValue
    public PermissionOverrideAction setPermissions(Collection<Permission> grantPermissions, Collection<Permission> denyPermissions)
    {
        setAllow(grantPermissions);
        setDeny(denyPermissions);
        return this;
    }

    @Override
    protected RequestBody finalizeData()
    {
        JSONObject object = new JSONObject();
        object.put("type", isRole() ? "role" : "member");
        object.put("allow", allow);
        object.put("deny", deny);

        return getRequestBody(object);
    }

    @Override
    protected void handleResponse(Response response, Request<PermissionOverride> request)
    {
        if (!response.isOk())
        {
            request.onFailure(response);
            return;
        }

        boolean isMember = isMember();
        long id = isMember ? member.getUser().getIdLong() : role.getIdLong();
        JSONObject object = (JSONObject) request.getRawBody();
        PermissionOverrideImpl override = new PermissionOverrideImpl(channel, id, isMember ? member : role);
        override.setAllow(object.getLong("allow"));
        override.setDeny(object.getLong("deny"));

        ((AbstractChannelImpl<?>) channel).getOverrideMap().put(id, override);

        request.onSuccess(override);
    }
}
