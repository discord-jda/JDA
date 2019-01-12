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
import net.dv8tion.jda.api.entities.*;
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
    private final IPermissionHolder permissionHolder;

    public PermissionOverrideActionImpl(JDA api, GuildChannel channel, IPermissionHolder permissionHolder)
    {
        super(api, Route.Channels.CREATE_PERM_OVERRIDE.compile(channel.getId(), permissionHolder.getId()));
        this.channel = channel;
        this.permissionHolder = permissionHolder;
    }

    @Override
    public PermissionOverrideActionImpl setCheck(BooleanSupplier checks)
    {
        return (PermissionOverrideActionImpl) super.setCheck(checks);
    }

    @Override
    public GuildChannel getChannel()
    {
        return channel;
    }

    @Override
    public Role getRole()
    {
        return isRole() ? (Role) permissionHolder : null;
    }

    @Override
    public Member getMember()
    {
        return isMember() ? (Member) permissionHolder : null;
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
        return permissionHolder instanceof Member;
    }

    @Override
    public boolean isRole()
    {
        return permissionHolder instanceof Role;
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
        Checks.check(denyBits <= Permission.ALL_PERMISSIONS, "Specified deny value may not be greater than a full permission set");
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
        long id = permissionHolder.getIdLong();
        JSONObject object = (JSONObject) request.getRawBody();
        PermissionOverrideImpl override = new PermissionOverrideImpl(channel, id, permissionHolder);
        override.setAllow(object.getLong("allow"));
        override.setDeny(object.getLong("deny"));

        ((AbstractChannelImpl<?,?>) channel).getOverrideMap().put(id, override);

        request.onSuccess(override);
    }
}
