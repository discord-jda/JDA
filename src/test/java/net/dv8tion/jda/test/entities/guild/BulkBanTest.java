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

package net.dv8tion.jda.test.entities.guild;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.requests.Method;
import net.dv8tion.jda.internal.utils.Helpers;
import net.dv8tion.jda.test.Constants;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static net.dv8tion.jda.test.ChecksHelper.assertDurationChecks;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class BulkBanTest extends AbstractGuildTest
{
    @Test
    void testMissingPermissions()
    {
        hasPermission(false);

        assertThatThrownBy(() -> guild.ban(Collections.emptyList(), Duration.ZERO))
            .isInstanceOf(InsufficientPermissionException.class)
            .hasMessage("Cannot perform action due to a lack of Permission. Missing permission: " + Permission.BAN_MEMBERS);
    }

    @Test
    void testBanOwner()
    {
        hasPermission(true);

        guild.setOwnerId(Constants.BUTLER_USER_ID);

        Set<UserSnowflake> users = Collections.singleton(User.fromId(Constants.BUTLER_USER_ID));

        assertThatThrownBy(() -> guild.ban(users, Duration.ZERO))
            .isInstanceOf(HierarchyException.class)
            .hasMessage("Cannot ban the owner of a guild.");
    }

    @Test
    void testInvalidInputs()
    {
        hasPermission(true);

        assertDurationChecks("Deletion timeframe", duration -> guild.ban(Collections.emptyList(), duration))
            .checksNotNegative()
            .throwsFor(Duration.ofDays(100), "Deletion timeframe must not be larger than 7 days. Provided: 8640000 seconds");

        Set<UserSnowflake> users = Collections.singleton(null);

        assertThatIllegalArgumentException()
            .isThrownBy(() -> guild.ban(users, Duration.ZERO).queue())
            .withMessage("Users may not be null");
        assertThatIllegalArgumentException()
            .isThrownBy(() -> guild.ban(null, null).queue())
            .withMessage("Users may not be null");

        assertThatIllegalArgumentException()
            .isThrownBy(() ->
                guild.ban(
                    LongStream.range(1, 300)
                        .map(i -> random.nextLong())
                        .mapToObj(User::fromId)
                        .collect(Collectors.toList()),
                        null
                ).queue()
            ).withMessage("Cannot ban more than 200 users at once");
    }

    @Test
    void testDuplicates()
    {
        hasPermission(true);

        Duration duration = Duration.ofSeconds(random.nextInt(10000));
        String reason = Helpers.format("User %d was banned by %d for %s", Constants.BUTLER_USER_ID, Constants.MINN_USER_ID, duration);
        List<UserSnowflake> users = Arrays.asList(
            User.fromId(Constants.BUTLER_USER_ID),
            User.fromId(Constants.BUTLER_USER_ID)
        );

        assertThatRequestFrom(guild.ban(users, duration).reason(reason))
            .hasMethod(Method.POST)
            .hasCompiledRoute("guilds/" + Constants.GUILD_ID + "/bulk-ban")
            .hasAuditReason(reason)
            .hasBodyMatching(body -> body.getArray("user_ids").length() == 1)
            .hasBodyMatchingSnapshot()
            .whenQueueCalled();
    }
}
