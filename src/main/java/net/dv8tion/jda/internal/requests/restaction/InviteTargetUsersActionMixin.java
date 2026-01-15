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
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.requests.restaction.InviteTargetUsersAction;
import net.dv8tion.jda.api.utils.AttachedFile;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.Checks;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import java.util.Arrays;
import java.util.Collection;
import java.util.StringJoiner;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

public interface InviteTargetUsersActionMixin<T extends InviteTargetUsersAction> extends InviteTargetUsersAction {
    @Nonnull
    TLongHashSet getUserIds();

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    default T setUsers(@Nonnull Collection<? extends UserSnowflake> users) {
        Checks.noneNull(users, "Users");

        TLongHashSet userIds = getUserIds();
        userIds.ensureCapacity(users.size());
        for (UserSnowflake user : users) {
            userIds.add(user.getIdLong());
        }
        return (T) this;
    }

    @Nonnull
    @Override
    default T setUsers(@Nonnull UserSnowflake... users) {
        Checks.noneNull(users, "Users");
        return setUsers(Arrays.asList(users));
    }

    @Nonnull
    @Override
    default T setUserIds(@Nonnull Collection<Long> ids) {
        Checks.noneNull(ids, "IDs");
        return setUserIds(ids.stream().mapToLong(Long::longValue).toArray());
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    default T setUserIds(@Nonnull long... ids) {
        Checks.notNull(ids, "IDs");

        TLongHashSet userIds = getUserIds();
        userIds.ensureCapacity(ids.length);
        userIds.addAll(ids);
        return (T) this;
    }

    @Nonnull
    @CheckReturnValue
    default T setUserIds(@Nonnull String... ids) {
        Checks.notNull(ids, "IDs");

        long[] arr = new long[ids.length];
        for (int i = 0; i < ids.length; i++) {
            arr[i] = MiscUtil.parseSnowflake(ids[i]);
        }
        return setUserIds(arr);
    }

    class TargetUsersFile implements AttachedFile {
        private static final MediaType CSV_MEDIA_TYPE = MediaType.parse("text/csv");

        private final TLongSet userIds;

        TargetUsersFile(TLongSet userIds) {
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
