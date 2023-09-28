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

package net.dv8tion.jda.api.entities.channel;

import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.EnumSet;

/**
 * Flags for specific channel settings.
 */
public enum ChannelFlag
{
    /**
     * This is a forum post {@link ThreadChannel} which is pinned in the {@link ForumChannel}.
     */
    PINNED(1 << 1),
    /**
     * This is a {@link ForumChannel} which requires all new post threads to have at least one applied tag.
     */
    REQUIRE_TAG(1 << 4),

    /**
     * This is a {@link net.dv8tion.jda.api.entities.channel.concrete.MediaChannel MediaChannel} which hides the copy embed option.
     */
    HIDE_MEDIA_DOWNLOAD_OPTIONS(1 << 15);

    private final int value;

    ChannelFlag(int value)
    {
        this.value = value;
    }

    /**
     * The raw bitset value of this flag.
     *
     * @return The raw value
     */
    public int getRaw()
    {
        return value;
    }

    /**
     * Parses the provided bitset to the corresponding enum constants.
     *
     * @param  bitset
     *         The bitset of channel flags
     *
     * @return The enum constants of the provided bitset
     */
    @Nonnull
    public static EnumSet<ChannelFlag> fromRaw(int bitset)
    {
        EnumSet<ChannelFlag> set = EnumSet.noneOf(ChannelFlag.class);
        if (bitset == 0)
            return set;

        for (ChannelFlag flag : values())
        {
            if (flag.value == bitset)
                set.add(flag);
        }

        return set;
    }

    /**
     * The raw bitset value for the provided flags.
     *
     * @return The raw value
     */
    public static int getRaw(@Nonnull Collection<ChannelFlag> flags)
    {
        Checks.notNull(flags, "Flags");
        int raw = 0;
        for (ChannelFlag flag : flags)
            raw |= flag.getRaw();
        return raw;
    }
}
