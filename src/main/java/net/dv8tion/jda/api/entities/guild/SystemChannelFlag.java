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
import java.util.Collection;
import java.util.EnumSet;

/**
 * Flags which configures a {@linkplain net.dv8tion.jda.api.entities.Guild#getSystemChannel() system channel} of a
 * {@link net.dv8tion.jda.api.entities.Guild Guild}.
 *
 * @see <a href="https://discord.com/developers/docs/resources/guild#guild-object-system-channel-flags">
 *      System Channel Flags API documentation
 *      </a>
 */
public enum SystemChannelFlag
{

    /**
     * Suppress member join notifications.
     */
    SUPPRESS_JOIN_NOTIFICATIONS(0),

    /**
     * Suppress server boost notifications.
     */
    SUPPRESS_PREMIUM_SUBSCRIPTIONS(1),

    /**
     * Suppress server setup tips.
     */
    SUPPRESS_GUILD_REMINDER_NOTIFICATIONS(2),

    /**
     * Hide member join sticker reply buttons.
     */
    SUPPRESS_JOIN_NOTIFICATION_REPLIES(3),

    /**
     * Suppress role subscription purchase and renewal notifications.
     */
    SUPPRESS_ROLE_SUBSCRIPTION_PURCHASE_NOTIFICATIONS(4),

    /**
     * Hide role subscription sticker reply buttons.
     */
    SUPPRESS_ROLE_SUBSCRIPTION_PURCHASE_NOTIFICATION_REPLIES(5);

    private final int offset;

    private final int rawValue;

    SystemChannelFlag(int offset)
    {
        this.offset = offset;
        this.rawValue = 0b1 << offset;
    }

    /**
     * Converts a bitmask representation of system channel flags into its {@link EnumSet}.
     *
     * @param raw
     *        The raw bitmask representing the system channel flags.
     *
     * @return An {@link EnumSet} of system channel flags represented by the input bitmask.
     */
    @Nonnull
    public static EnumSet<SystemChannelFlag> getFlags(int raw)
    {
        EnumSet<SystemChannelFlag> enumSet = EnumSet.noneOf(SystemChannelFlag.class);
        for (SystemChannelFlag flag : SystemChannelFlag.values())
        {
            if ((flag.rawValue & raw) != 0)
                enumSet.add(flag);
        }
        return enumSet;
    }

    /**
     * Converts a {@link Collection} of this class to its respective bitmask representing the same
     * set of system channel flags.
     *
     * @param flags
     *        A set of system channel flags.
     *
     * @return An integer bitmask recognised on Discord's side.
     */
    public static int getRaw(@Nonnull Collection<SystemChannelFlag> flags)
    {
        int raw = 0;
        for (SystemChannelFlag flag : flags)
            raw |= flag.rawValue;
        return raw;
    }

}
