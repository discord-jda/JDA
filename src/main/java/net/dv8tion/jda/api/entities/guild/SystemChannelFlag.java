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

package net.dv8tion.jda.api.entities.guild;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.HashMap;

/**
 * Flags for system channel settings. A system channel is a channel in a guild where
 * system messages are broadcast. Each guild has at-most one system channel, so each
 * guild has a single set of these system channel flags. According to the API docs,
 * these flags are represented as a bitmask, which can be converted to an {@link EnumSet}
 * of this class.
 *
 * @see <a href="https://discord.com/developers/docs/resources/guild#guild-object-system-channel-flags">
 *     System Channel Flags API documentation
 *     <a/>
 * */
public enum SystemChannelFlag
{

    /**
     * Suppress member join notifications.
     * */
    SUPPRESS_JOIN_NOTIFICATIONS(0),

    /**
     * Suppress server boost notifications.
     * Identical to the "SUPPRESS_PREMIUM_SUBSCRIPTIONS" flag in the API
     * docs.
     * */
    SUPPRESS_BOOST_NOTIFICATIONS(1),

    /**
     * Suppress server setup tips.
     * Identical to the "SUPPRESS_GUILD_REMINDER_NOTIFICATIONS" flag in the
     * API docs.
     * */
    SUPPRESS_SETUP_TIPS(2),

    /**
     * Hide member join sticker reply buttons.
     * Identical to "SUPPRESS_JOIN_NOTIFICATION_REPLIES" flag in the
     * API docs.
     * */
    HIDE_JOIN_REPLY_STICKERS(3),

    /**
     * Suppress role subscription purchase and renewal notifications.
     * Identical to "SUPPRESS_ROLE_SUBSCRIPTION_PURCHASE_NOTIFICATIONS" flag
     * in the API docs.
     * */
    SUPPRESS_ROLE_PURCHASE_NOTIFICATIONS(4),

    /**
     * Hide role subscription sticker reply buttons. Identical to
     * "SUPPRESS_ROLE_SUBSCRIPTION_PURCHASE_NOTIFICATION_REPLIES" flag
     * in the API docs.
     * */
    HIDE_ROLE_SUBSCRIPTION_STICKERS(5);

    private final int offset;

    private static final HashMap<Integer, SystemChannelFlag> OFFSET_TO_FLAG = new HashMap<>();

    static
    {
        for (SystemChannelFlag flag : SystemChannelFlag.values())
        {
            OFFSET_TO_FLAG.put(flag.offset, flag);
        }
    }

    SystemChannelFlag(int offset)
    {
        this.offset = offset;
    }

    /**
     * Converts a bitmask representation of system channel flags into its
     * {@link EnumSet}.
     * @param bitmask the raw bitmask representing the system channel flags.
     * @return an {@link EnumSet} of system channel flags represented by the input bitmask.
     * @throws IllegalArgumentException if the input bitmask is invalid.
     * */
    @Nonnull
    public static EnumSet<SystemChannelFlag> fromBitmask(final int bitmask)
    {
        EnumSet<SystemChannelFlag> enumSet = EnumSet.noneOf(SystemChannelFlag.class);
        int offset = 0;
        for (int v = bitmask; v != 0; v >>= 1)
        {
            int lsb = v & 0b1;
            if(lsb == 0b1)
            {
                if(!OFFSET_TO_FLAG.containsKey(offset))
                    throw new IllegalArgumentException("Input value must be a valid bitmask of Discord system channel flags.");
                enumSet.add(OFFSET_TO_FLAG.get(offset));
            }
            offset++;
        }
        return enumSet;
    }

    /**
     * Converts an {@link EnumSet} of this class to its respective bitmask representing the same
     * set of system channel flags.
     * @param enumSet a set of system channel flags.
     * @return an integer bitmask recognised on Discord's side.
     * */
    public static int toBitmask(final EnumSet<SystemChannelFlag> enumSet)
    {
        int result = 0;
        for (SystemChannelFlag flag : enumSet)
        {
            result |= 0b1 << flag.offset;
        }
        return result;
    }

}
