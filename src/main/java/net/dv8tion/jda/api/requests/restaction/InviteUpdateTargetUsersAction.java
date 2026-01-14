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

package net.dv8tion.jda.api.requests.restaction;

import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.internal.utils.Checks;

import java.util.Arrays;
import java.util.Collection;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

public interface InviteUpdateTargetUsersAction extends RestAction<Void> {
    @Nonnull
    @CheckReturnValue
    InviteUpdateTargetUsersAction setUsers(@Nonnull Collection<? extends UserSnowflake> users);

    @Nonnull
    @CheckReturnValue
    default InviteUpdateTargetUsersAction setUsers(@Nonnull UserSnowflake... users) {
        Checks.noneNull(users, "Users");
        return setUsers(Arrays.asList(users));
    }

    @Nonnull
    @CheckReturnValue
    default InviteUpdateTargetUsersAction setUserIds(@Nonnull Collection<Long> ids) {
        Checks.noneNull(ids, "IDs");
        return setUserIds(ids.stream().mapToLong(Long::longValue).toArray());
    }

    @Nonnull
    @CheckReturnValue
    InviteUpdateTargetUsersAction setUserIds(@Nonnull long... ids);

    @Nonnull
    @CheckReturnValue
    default InviteUpdateTargetUsersAction setUserIds(@Nonnull String... ids) {
        Checks.notNull(ids, "IDs");

        long[] arr = new long[ids.length];
        for (int i = 0; i < ids.length; i++) {
            arr[i] = MiscUtil.parseSnowflake(ids[i]);
        }
        return setUserIds(arr);
    }
}
