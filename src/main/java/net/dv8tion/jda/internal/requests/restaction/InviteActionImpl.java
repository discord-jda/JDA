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

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.attribute.IInviteContainer;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.requests.restaction.InviteAction;
import net.dv8tion.jda.api.utils.AttachedFile;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.invite.InviteTargetUsers;
import okhttp3.RequestBody;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

public class InviteActionImpl extends AuditableRestActionImpl<Invite> implements InviteAction {
    private final IInviteContainer channel;

    private Integer maxAge = null;
    private Integer maxUses = null;
    private Boolean temporary = null;
    private Boolean unique = null;
    private Long targetApplication = null;
    private Long targetUser = null;
    private Invite.TargetType targetType = null;
    private final InviteTargetUsers targetUsers = new InviteTargetUsers();
    private final Set<Long> roleIds = new HashSet<>();

    public InviteActionImpl(IInviteContainer channel) {
        super(channel.getJDA(), Route.Invites.CREATE_INVITE.compile(channel.getId()));
        this.channel = channel;
    }

    @Nonnull
    @Override
    public InviteActionImpl setCheck(BooleanSupplier checks) {
        return (InviteActionImpl) super.setCheck(checks);
    }

    @Nonnull
    @Override
    public InviteActionImpl timeout(long timeout, @Nonnull TimeUnit unit) {
        return (InviteActionImpl) super.timeout(timeout, unit);
    }

    @Nonnull
    @Override
    public InviteActionImpl deadline(long timestamp) {
        return (InviteActionImpl) super.deadline(timestamp);
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public InviteActionImpl setMaxAge(Integer maxAge) {
        if (maxAge != null) {
            Checks.notNegative(maxAge, "maxAge");
        }

        this.maxAge = maxAge;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public InviteActionImpl setMaxAge(Long maxAge, @Nonnull TimeUnit timeUnit) {
        if (maxAge == null) {
            return this.setMaxAge(null);
        }

        Checks.notNegative(maxAge, "maxAge");
        Checks.notNull(timeUnit, "timeUnit");

        return this.setMaxAge(Math.toIntExact(timeUnit.toSeconds(maxAge)));
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public InviteActionImpl setMaxUses(Integer maxUses) {
        if (maxUses != null) {
            Checks.notNegative(maxUses, "maxUses");
        }

        this.maxUses = maxUses;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public InviteActionImpl setTemporary(Boolean temporary) {
        this.temporary = temporary;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public InviteActionImpl setUnique(Boolean unique) {
        this.unique = unique;
        return this;
    }

    @Nonnull
    @Override
    public InviteAction setTargetApplication(long applicationId) {
        if (applicationId == 0) {
            this.targetType = null;
            this.targetApplication = null;
            return this;
        }

        this.targetType = Invite.TargetType.EMBEDDED_APPLICATION;
        this.targetApplication = applicationId;
        return this;
    }

    @Nonnull
    @Override
    public InviteAction setTargetStream(long userId) {
        if (userId == 0) {
            this.targetType = null;
            this.targetUser = null;
            return this;
        }

        this.targetType = Invite.TargetType.STREAM;
        this.targetUser = userId;
        return this;
    }

    @Nonnull
    @Override
    public InviteActionImpl setTargetUsers(@Nonnull Collection<? extends UserSnowflake> users) {
        checkCanManageServer();
        targetUsers.setUsers(users);
        return this;
    }

    @Nonnull
    @Override
    public InviteActionImpl setTargetUsers(@Nonnull UserSnowflake... users) {
        checkCanManageServer();
        targetUsers.setUsers(users);
        return this;
    }

    @Nonnull
    @Override
    public InviteActionImpl setTargetUserIds(@Nonnull Collection<Long> ids) {
        checkCanManageServer();
        targetUsers.setUserIds(ids);
        return this;
    }

    @Nonnull
    @Override
    public InviteActionImpl setTargetUserIds(@Nonnull long... ids) {
        checkCanManageServer();
        targetUsers.setUserIds(ids);
        return this;
    }

    @Nonnull
    @Override
    public InviteActionImpl setTargetUserIds(@Nonnull String... ids) {
        checkCanManageServer();
        targetUsers.setUserIds(ids);
        return this;
    }

    private void checkCanManageServer() {
        if (!channel.getGuild().getSelfMember().hasPermission(channel, Permission.MANAGE_SERVER)) {
            throw new InsufficientPermissionException(channel, Permission.MANAGE_SERVER);
        }
    }

    @Nonnull
    @Override
    public InviteActionImpl setRoles(@Nonnull Collection<? extends Role> roles) {
        Checks.noneNull(roles, "Roles");

        Guild guild = channel.getGuild();
        if (!guild.getSelfMember().hasPermission(channel, Permission.MANAGE_ROLES)) {
            throw new InsufficientPermissionException(channel, Permission.MANAGE_ROLES);
        }

        for (Role role : roles) {
            Checks.check(role.getGuild().equals(guild), "%s is not from the same guild! (%s)", role, guild);
            if (!guild.getSelfMember().canInteract(role)) {
                throw new HierarchyException(
                        String.format("%s is higher in than the highest role of the self member!", role));
            }
        }

        roleIds.clear();
        for (Role role : roles) {
            roleIds.add(role.getIdLong());
        }

        return this;
    }

    @Nonnull
    @Override
    public InviteActionImpl setRoleIds(@Nonnull long... ids) {
        Checks.notNull(ids, "IDs");

        Guild guild = channel.getGuild();
        if (!guild.getSelfMember().hasPermission(channel, Permission.MANAGE_ROLES)) {
            throw new InsufficientPermissionException(channel, Permission.MANAGE_ROLES);
        }

        for (long id : ids) {
            Role role = guild.getRoleById(id);

            // Discord ignores unknown roles
            if (role == null) {
                continue;
            }
            if (!guild.getSelfMember().canInteract(role)) {
                throw new HierarchyException(
                        String.format("%s is higher in than the highest role of the self member!", role));
            }
        }

        roleIds.clear();
        for (long id : ids) {
            roleIds.add(id);
        }

        return this;
    }

    @Override
    protected RequestBody finalizeData() {
        DataObject object = DataObject.empty();
        Set<AttachedFile> files = new HashSet<>(1);

        if (this.maxAge != null) {
            object.put("max_age", this.maxAge);
        }
        if (this.maxUses != null) {
            object.put("max_uses", this.maxUses);
        }
        if (this.temporary != null) {
            object.put("temporary", this.temporary);
        }
        if (this.unique != null) {
            object.put("unique", this.unique);
        }
        if (this.targetType != null) {
            object.put("target_type", targetType.getId());
        }
        if (this.targetUser != null) {
            object.put("target_user_id", targetUser);
        }
        if (this.targetApplication != null) {
            object.put("target_application_id", targetApplication);
        }
        if (!targetUsers.isEmpty()) {
            files.add(targetUsers.toAttachedFile());
        }
        if (!roleIds.isEmpty()) {
            object.put("role_ids", roleIds);
        }

        return getMultipartBody(files, object);
    }

    @Override
    protected void handleSuccess(Response response, Request<Invite> request) {
        request.onSuccess(this.api.getEntityBuilder().createInvite(response.getObject()));
    }
}
