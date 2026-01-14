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

import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.requests.restaction.InviteUpdateTargetUsersAction;
import net.dv8tion.jda.api.utils.AttachedFile;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.utils.Checks;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import javax.annotation.Nonnull;

public class InviteUpdateTargetUsersActionImpl extends RestActionImpl<Void> implements InviteUpdateTargetUsersAction {
    private final TLongHashSet userIds = new TLongHashSet();

    public InviteUpdateTargetUsersActionImpl(JDA api, String code) {
        super(api, Route.Invites.UPDATE_TARGET_USERS.compile(code));
    }

    @Nonnull
    @Override
    public InviteUpdateTargetUsersActionImpl setCheck(BooleanSupplier checks) {
        return (InviteUpdateTargetUsersActionImpl) super.setCheck(checks);
    }

    @Nonnull
    @Override
    public InviteUpdateTargetUsersActionImpl timeout(long timeout, @Nonnull TimeUnit unit) {
        return (InviteUpdateTargetUsersActionImpl) super.timeout(timeout, unit);
    }

    @Nonnull
    @Override
    public InviteUpdateTargetUsersActionImpl deadline(long timestamp) {
        return (InviteUpdateTargetUsersActionImpl) super.deadline(timestamp);
    }

    @Nonnull
    @Override
    public InviteUpdateTargetUsersAction setUsers(@Nonnull Collection<? extends UserSnowflake> users) {
        Checks.noneNull(users, "Users");

        userIds.ensureCapacity(users.size());
        for (UserSnowflake user : users) {
            userIds.add(user.getIdLong());
        }
        return this;
    }

    @Nonnull
    @Override
    public InviteUpdateTargetUsersAction setUserIds(@Nonnull long... ids) {
        Checks.notNull(ids, "IDs");

        userIds.ensureCapacity(ids.length);
        userIds.addAll(ids);
        return this;
    }

    @Override
    protected RequestBody finalizeData() {
        Set<TargetUsersFile> targetUsersFileSet = Collections.singleton(new TargetUsersFile(userIds));
        return AttachedFile.createMultipartBody(targetUsersFileSet).build();
    }

    private static class TargetUsersFile implements AttachedFile {
        private static final MediaType CSV_MEDIA_TYPE = MediaType.parse("text/csv");

        private final TLongSet userIds;

        private TargetUsersFile(TLongSet userIds) {
            this.userIds = userIds;
        }

        @Override
        public void addPart(@Nonnull MultipartBody.Builder builder, int index) {
            StringJoiner csvJoiner = new StringJoiner(",");
            userIds.forEach(value -> {
                csvJoiner.add(Long.toString(value));
                return true;
            });

            builder.addFormDataPart(
                    "target_users_file", "target_users.csv", RequestBody.create(csvJoiner.toString(), CSV_MEDIA_TYPE));
        }

        @Nonnull
        @Override
        public DataObject toAttachmentData(int index) {
            return DataObject.empty();
        }

        @Override
        public void forceClose() {}

        @Override
        public void close() {}
    }
}
