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

import net.dv8tion.jda.api.entities.guild.SystemChannelFlag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static net.dv8tion.jda.api.entities.guild.SystemChannelFlag.*;

import java.util.EnumSet;

/**
 * Tests the class {@link SystemChannelFlag}, especially the conversion logic
 * between its {@link EnumSet} and bitmask representations.
 */
public final class SystemChannelFlagTest
{
    @Test
    public void testFromBitmask()
    {
        assertFrom(0b000000); // Tests empty.
        assertFrom(0b000001, SUPPRESS_JOIN_NOTIFICATIONS);
        assertFrom(0b000010, SUPPRESS_PREMIUM_SUBSCRIPTIONS);
        assertFrom(0b000100, SUPPRESS_GUILD_REMINDER_NOTIFICATIONS);
        assertFrom(0b001000, SUPPRESS_JOIN_NOTIFICATION_REPLIES);
        assertFrom(0b010000, SUPPRESS_ROLE_SUBSCRIPTION_PURCHASE_NOTIFICATIONS);
        assertFrom(0b100000, SUPPRESS_ROLE_SUBSCRIPTION_PURCHASE_NOTIFICATION_REPLIES);

        assertFrom(0b100101, SUPPRESS_ROLE_SUBSCRIPTION_PURCHASE_NOTIFICATION_REPLIES, SUPPRESS_GUILD_REMINDER_NOTIFICATIONS, SUPPRESS_JOIN_NOTIFICATIONS);
        assertFrom(0b111111,
                SUPPRESS_JOIN_NOTIFICATIONS,
                SUPPRESS_PREMIUM_SUBSCRIPTIONS,
                SUPPRESS_GUILD_REMINDER_NOTIFICATIONS,
                SUPPRESS_JOIN_NOTIFICATION_REPLIES,
                SUPPRESS_ROLE_SUBSCRIPTION_PURCHASE_NOTIFICATIONS,
                SUPPRESS_ROLE_SUBSCRIPTION_PURCHASE_NOTIFICATION_REPLIES
        );
    }

    @Test
    public void testToBitmask()
    {
        assertTo(0b000000); //Tests empty
        assertTo(0b000001, SUPPRESS_JOIN_NOTIFICATIONS);
        assertTo(0b000010, SUPPRESS_PREMIUM_SUBSCRIPTIONS);
        assertTo(0b000100, SUPPRESS_GUILD_REMINDER_NOTIFICATIONS);
        assertTo(0b001000, SUPPRESS_JOIN_NOTIFICATION_REPLIES);
        assertTo(0b010000, SUPPRESS_ROLE_SUBSCRIPTION_PURCHASE_NOTIFICATIONS);
        assertTo(0b100000, SUPPRESS_ROLE_SUBSCRIPTION_PURCHASE_NOTIFICATION_REPLIES);
        assertTo(0b100101, SUPPRESS_ROLE_SUBSCRIPTION_PURCHASE_NOTIFICATION_REPLIES, SUPPRESS_GUILD_REMINDER_NOTIFICATIONS, SUPPRESS_JOIN_NOTIFICATIONS);
        assertTo(0b111111,
                SUPPRESS_JOIN_NOTIFICATIONS,
                SUPPRESS_PREMIUM_SUBSCRIPTIONS,
                SUPPRESS_GUILD_REMINDER_NOTIFICATIONS,
                SUPPRESS_JOIN_NOTIFICATION_REPLIES,
                SUPPRESS_ROLE_SUBSCRIPTION_PURCHASE_NOTIFICATIONS,
                SUPPRESS_ROLE_SUBSCRIPTION_PURCHASE_NOTIFICATION_REPLIES
        );
    }

    private void assertTo(int expectedBitmask, SystemChannelFlag... enumFlags)
    {
        EnumSet<SystemChannelFlag> flagSet = getEnumSet(enumFlags);
        Assertions.assertEquals(expectedBitmask, SystemChannelFlag.getRaw(flagSet));
    }

    private void assertFrom(int bitmask, SystemChannelFlag... expectedFlags)
    {
        EnumSet<SystemChannelFlag> flagSet = getEnumSet(expectedFlags);
        Assertions.assertEquals(flagSet, SystemChannelFlag.getFlags(bitmask));
    }

    private EnumSet<SystemChannelFlag> getEnumSet(SystemChannelFlag... enumFlags)
    {
        EnumSet<SystemChannelFlag> flagSet = EnumSet.noneOf(SystemChannelFlag.class);
        for (SystemChannelFlag flag : enumFlags)
        {
            flagSet.add(flag);
        }
        return flagSet;
    }
}
