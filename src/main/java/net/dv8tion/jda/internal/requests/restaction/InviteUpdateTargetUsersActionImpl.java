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
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.requests.restaction.InviteUpdateTargetUsersAction;
import net.dv8tion.jda.api.utils.AttachedFile;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.invite.InviteTargetUsers;
import okhttp3.RequestBody;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InviteUpdateTargetUsersActionImpl extends RestActionImpl<Void> implements InviteUpdateTargetUsersAction {
    private final InviteTargetUsers targetUsers = new InviteTargetUsers();

    public InviteUpdateTargetUsersActionImpl(JDA api, String code) {
        super(api, Route.Invites.UPDATE_TARGET_USERS.compile(code));
    }

    @Nonnull
    @Override
    public InviteUpdateTargetUsersActionImpl timeout(long timeout, @Nonnull TimeUnit unit) {
        return (InviteUpdateTargetUsersActionImpl) super.timeout(timeout, unit);
    }

    @Nonnull
    @Override
    public InviteUpdateTargetUsersActionImpl setCheck(@Nullable BooleanSupplier checks) {
        return (InviteUpdateTargetUsersActionImpl) super.setCheck(checks);
    }

    @Nonnull
    @Override
    public InviteUpdateTargetUsersActionImpl deadline(long timestamp) {
        return (InviteUpdateTargetUsersActionImpl) super.deadline(timestamp);
    }

    @Nonnull
    @Override
    public InviteUpdateTargetUsersActionImpl setUsers(@Nonnull Collection<? extends UserSnowflake> users) {
        Checks.notEmpty(users, "Users");
        targetUsers.setUsers(users);
        return this;
    }

    @Nonnull
    @Override
    public InviteUpdateTargetUsersActionImpl setUsers(@Nonnull UserSnowflake... users) {
        Checks.notEmpty(users, "Users");
        targetUsers.setUsers(users);
        return this;
    }

    @Nonnull
    @Override
    public InviteUpdateTargetUsersActionImpl setUserIds(@Nonnull Collection<Long> ids) {
        Checks.notEmpty(ids, "IDs");
        targetUsers.setUserIds(ids);
        return this;
    }

    @Nonnull
    @Override
    public InviteUpdateTargetUsersActionImpl setUserIds(@Nonnull long... ids) {
        Checks.notNull(ids, "IDs");
        Checks.check(ids.length > 0, "IDs may not be empty");
        targetUsers.setUserIds(ids);
        return this;
    }

    @Nonnull
    @Override
    public InviteUpdateTargetUsersActionImpl setUserIds(@Nonnull String... ids) {
        Checks.notEmpty(ids, "IDs");
        targetUsers.setUserIds(ids);
        return this;
    }

    @Override
    protected RequestBody finalizeData() {
        Checks.check(!targetUsers.isEmpty(), "Cannot set an invite's target users to an empty list!");

        Set<AttachedFile> attachedFiles = Collections.singleton(targetUsers.toAttachedFile());
        return AttachedFile.createMultipartBody(attachedFiles).build();
    }
}
