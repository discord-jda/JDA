/*
 * Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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

package net.dv8tion.jda.internal.requests.restaction;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.restaction.PermissionOverrideAction;
import net.dv8tion.jda.internal.entities.AbstractChannelImpl;
import net.dv8tion.jda.internal.entities.PermissionOverrideImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Checks;
import okhttp3.RequestBody;
import org.json.JSONObject;

import javax.annotation.CheckReturnValue;
import java.util.function.BooleanSupplier;

public class PermissionOverrideActionImpl
    extends AuditableRestActionImpl<PermissionOverride>
    implements PermissionOverrideAction
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
    public PermissionOverrideActionImpl(JDA api, Route.CompiledRoute route, GuildChannel channel, Member member)
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
    public PermissionOverrideActionImpl(JDA api, Route.CompiledRoute route, GuildChannel channel, Role role)
    {
        super(api, route);
        this.channel = channel;
        this.member = null;
        this.role = role;
    }

    @Override
    public PermissionOverrideActionImpl setCheck(BooleanSupplier checks)
    {
        return (PermissionOverrideActionImpl) super.setCheck(checks);
    }

    @Override
    public long getAllow()
    {
        return allow;
    }

    @Override
    public long getDeny()
    {
        return deny;
    }

    @Override
    public long getInherited()
    {
        return ~allow & ~deny;
    }

    @Override
    public boolean isMember()
    {
        return member != null;
    }

    @Override
    public boolean isRole()
    {
        return role != null;
    }

    @Override
    @CheckReturnValue
    public PermissionOverrideActionImpl setAllow(long allowBits)
    {
        Checks.notNegative(allowBits, "Granted permissions value");
        Checks.check(allowBits <= Permission.ALL_PERMISSIONS, "Specified allow value may not be greater than a full permission set");
        this.allow = allowBits;
        return this;
    }

    @Override
    @CheckReturnValue
    public PermissionOverrideActionImpl setDeny(long denyBits)
    {
        Checks.notNegative(denyBits, "Denied permissions value");
        Checks.check(denyBits <= Permission.ALL_PERMISSIONS, "Specified allow value may not be greater than a full permission set");
        this.deny = denyBits;
        return this;
    }

    @Override
    @CheckReturnValue
    public PermissionOverrideActionImpl setPermissions(long allowBits, long denyBits)
    {
        setAllow(allowBits);
        setDeny(denyBits);
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
    protected void handleSuccess(Response response, Request<PermissionOverride> request)
    {
        boolean isMember = isMember();
        long id = isMember ? member.getUser().getIdLong() : role.getIdLong();
        JSONObject object = (JSONObject) request.getRawBody();
        PermissionOverrideImpl override = new PermissionOverrideImpl(channel, id, isMember ? member : role);
        override.setAllow(object.getLong("allow"));
        override.setDeny(object.getLong("deny"));

        ((AbstractChannelImpl<?,?>) channel).getOverrideMap().put(id, override);

        request.onSuccess(override);
    }
}
