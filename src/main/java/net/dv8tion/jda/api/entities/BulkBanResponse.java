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

package net.dv8tion.jda.api.entities;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * Response to {@link Guild#ban(java.util.Collection, Duration)}
 *
 * <p>This response includes a list of successfully banned users and users which could not be banned.
 * Discord might fail to ban a user due to permission issues or an internal server error.
 */
public class BulkBanResponse
{
    private final List<UserSnowflake> bannedUsers;
    private final List<UserSnowflake> failedUsers;

    public BulkBanResponse(@Nonnull List<UserSnowflake> bannedUsers, @Nonnull List<UserSnowflake> failedUsers)
    {
        this.bannedUsers = Collections.unmodifiableList(bannedUsers);
        this.failedUsers = Collections.unmodifiableList(failedUsers);
    }

    /**
     * List of successfully banned users.
     *
     * @return {@link List} of {@link UserSnowflake}
     */
    @Nonnull
    public List<UserSnowflake> getBannedUsers()
    {
        return bannedUsers;
    }

    /**
     * List of users which could not be banned.
     *
     * @return {@link List} of {@link UserSnowflake}
     */
    @Nonnull
    public List<UserSnowflake> getFailedUsers()
    {
        return failedUsers;
    }
}
