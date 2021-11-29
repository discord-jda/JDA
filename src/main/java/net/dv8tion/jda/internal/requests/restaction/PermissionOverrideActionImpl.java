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

package net.dv8tion.jda.internal.requests.restaction;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.exceptions.MissingAccessException;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.restaction.PermissionOverrideAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.entities.PermissionOverrideImpl;
import net.dv8tion.jda.internal.entities.mixin.channel.attribute.IPermissionContainerMixin;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.PermissionUtil;
import okhttp3.RequestBody;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

public class PermissionOverrideActionImpl
    extends AuditableRestActionImpl<PermissionOverride>
    implements PermissionOverrideAction
{
    private boolean isOverride = true;
    private boolean allowSet = false;
    private boolean denySet = false;

    private long allow = 0;
    private long deny = 0;
    private final IPermissionContainerMixin<?> channel;
    private final IPermissionHolder permissionHolder;
    private final boolean isRole;
    private final long id;

    public PermissionOverrideActionImpl(PermissionOverride override)
    {
        super(override.getJDA(), Route.Channels.MODIFY_PERM_OVERRIDE.compile(override.getChannel().getId(), override.getId()));
        this.channel = (IPermissionContainerMixin<?>) override.getChannel();
        this.permissionHolder = override.getPermissionHolder();
        this.isRole = override.isRoleOverride();
        this.id = override.getIdLong();
    }

    public PermissionOverrideActionImpl(JDA api, GuildChannel channel, IPermissionHolder permissionHolder)
    {
        super(api, Route.Channels.CREATE_PERM_OVERRIDE.compile(channel.getId(), permissionHolder.getId()));
        this.channel = (IPermissionContainerMixin<?>) channel;
        this.permissionHolder = permissionHolder;
        this.isRole = permissionHolder instanceof Role;
        this.id = permissionHolder.getIdLong();
    }

    // Whether to keep original value of the current override or not - by default we override the value
    public PermissionOverrideActionImpl setOverride(boolean override)
    {
        isOverride = override;
        return this;
    }

    @Override
    protected BooleanSupplier finalizeChecks()
    {
        return () -> {

            Member selfMember = getGuild().getSelfMember();
            if (!selfMember.hasPermission(channel, Permission.VIEW_CHANNEL))
                throw new MissingAccessException(channel, Permission.VIEW_CHANNEL);
            if (!selfMember.hasAccess(channel))
                throw new MissingAccessException(channel, Permission.VOICE_CONNECT);
            if (!selfMember.hasPermission(channel, Permission.MANAGE_PERMISSIONS))
                throw new InsufficientPermissionException(channel, Permission.MANAGE_PERMISSIONS);
            return true;
        };
    }

    @Nonnull
    @Override
    public PermissionOverrideActionImpl setCheck(BooleanSupplier checks)
    {
        return (PermissionOverrideActionImpl) super.setCheck(checks);
    }

    @Nonnull
    @Override
    public PermissionOverrideActionImpl timeout(long timeout, @Nonnull TimeUnit unit)
    {
        return (PermissionOverrideActionImpl) super.timeout(timeout, unit);
    }

    @Nonnull
    @Override
    public PermissionOverrideActionImpl deadline(long timestamp)
    {
        return (PermissionOverrideActionImpl) super.deadline(timestamp);
    }

    @Nonnull
    @Override
    public PermissionOverrideAction resetAllow()
    {
        allow = getOriginalAllow();
        allowSet = false;
        return this;
    }

    @Nonnull
    @Override
    public PermissionOverrideAction resetDeny()
    {
        deny = getOriginalDeny();
        denySet = false;
        return this;
    }

    @Nonnull
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
        return getCurrentAllow();
    }

    @Override
    public long getDeny()
    {
        return getCurrentDeny();
    }

    @Override
    public long getInherited()
    {
        return ~getAllow() & ~getDeny();
    }

    @Override
    public boolean isMember()
    {
        return !isRole;
    }

    @Override
    public boolean isRole()
    {
        return isRole;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public PermissionOverrideActionImpl setAllow(long allowBits)
    {
        checkPermissions(getOriginalAllow() ^ allowBits);
        this.allow = allowBits;
        this.deny = getCurrentDeny() & ~allowBits;
        allowSet = denySet = true;
        return this;
    }

    @Nonnull
    @Override
    public PermissionOverrideAction grant(long allowBits)
    {
        return setAllow(getCurrentAllow() | allowBits);
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public PermissionOverrideActionImpl setDeny(long denyBits)
    {
        checkPermissions(getOriginalDeny() ^ denyBits);
        this.deny = denyBits;
        this.allow = getCurrentAllow() & ~denyBits;
        allowSet = denySet = true;
        return this;
    }

    @Nonnull
    @Override
    public PermissionOverrideAction deny(long denyBits)
    {
        return setDeny(getCurrentDeny() | denyBits);
    }

    @Nonnull
    @Override
    public PermissionOverrideAction clear(long inheritedBits)
    {
        return setAllow(getCurrentAllow() & ~inheritedBits).setDeny(getCurrentDeny() & ~inheritedBits);
    }

    protected void checkPermissions(long changed)
    {
        Member selfMember = getGuild().getSelfMember();
        if (changed != 0 && !selfMember.hasPermission(Permission.ADMINISTRATOR))
        {
            long channelPermissions = PermissionUtil.getExplicitPermission(channel, selfMember, false);
            if ((channelPermissions & Permission.MANAGE_PERMISSIONS.getRawValue()) == 0)
            {
                // This implies we can only set permissions the bot also has in the channel
                long botPerms = PermissionUtil.getEffectivePermission(channel, selfMember);
                EnumSet<Permission> missing = Permission.getPermissions(changed & ~botPerms);
                if (!missing.isEmpty())
                    throw new InsufficientPermissionException(channel, Permission.MANAGE_PERMISSIONS, "You must have Permission.MANAGE_PERMISSIONS on the channel explicitly in order to set permissions you don't already have!");
            }
        }
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public PermissionOverrideActionImpl setPermissions(long allowBits, long denyBits)
    {
        return setAllow(allowBits).setDeny(denyBits);
    }

    private long getCurrentAllow()
    {
        if (allowSet)
            return allow;
        return isOverride ? 0 : getOriginalAllow();
    }

    private long getCurrentDeny()
    {
        if (denySet)
            return deny;
        return isOverride ? 0 : getOriginalDeny();
    }

    private long getOriginalDeny()
    {
        PermissionOverride override = channel.getPermissionOverrideMap().get(id);
        return override == null ? 0 : override.getDeniedRaw();
    }

    private long getOriginalAllow()
    {
        PermissionOverride override = channel.getPermissionOverrideMap().get(id);
        return override == null ? 0 : override.getAllowedRaw();
    }

    @Override
    protected RequestBody finalizeData()
    {
        DataObject object = DataObject.empty();
        object.put("type", isRole() ? 0 : 1);
        object.put("allow", getCurrentAllow());
        object.put("deny", getCurrentDeny());
        reset();
        return getRequestBody(object);
    }

    @Override
    protected void handleSuccess(Response response, Request<PermissionOverride> request)
    {
        DataObject object = (DataObject) request.getRawBody();
        PermissionOverrideImpl override = new PermissionOverrideImpl(channel, id, isRole());
        override.setAllow(object.getLong("allow"));
        override.setDeny(object.getLong("deny"));
        //((AbstractChannelImpl<?,?>) channel).getOverrideMap().put(id, override); This is added by the event later
        request.onSuccess(override);
    }
}
