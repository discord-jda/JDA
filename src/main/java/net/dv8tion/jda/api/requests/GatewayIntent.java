/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

package net.dv8tion.jda.api.requests;

import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.util.EnumSet;

/**
 * Flags which enable or disable specific events from the discord gateway.
 *
 * @see net.dv8tion.jda.api.JDABuilder#setDisabledIntents(GatewayIntent, GatewayIntent...)
 * @see net.dv8tion.jda.api.JDABuilder#setEnabledIntents(GatewayIntent, GatewayIntent...)
 * @see net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder#setDisabledIntents(GatewayIntent, GatewayIntent...)
 * @see net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder#setEnabledIntents(GatewayIntent, GatewayIntent...)
 */
public enum GatewayIntent
{
    //GUILDS(0), we currently don't support this one
    /**
     * Events which inform us about member update/leave/join of a guild.
     */
    GUILD_MEMBERS(1),
    /**
     * Ban events.
     */
    GUILD_BANS(2),
    /**
     * Emote add/update/delete events.
     */
    GUILD_EMOJIS(3),
    /**
     * Integration events. (unused)
     */
    GUILD_INTEGRATIONS(4),
    /**
     * Webhook events. (unused)
     */
    GUILD_WEBHOOKS(5),
    /**
     * Invite events.
     */
    GUILD_INVITES(6),
    /**
     * Voice state events. This is used to determine which members are connected to a voice channel.
     */
    GUILD_VOICE_STATES(7),
    /**
     * Presence updates. This is used to lazy load members and update user properties such as name/avatar.
     */
    GUILD_PRESENCES(8),
    /**
     * Message events from text channels in guilds.
     */
    GUILD_MESSAGES(9),
    /**
     * Message reaction events in guilds.
     */
    GUILD_MESSAGE_REACTIONS(10),
    /**
     * Typing start events in guilds.
     */
    GUILD_MESSAGE_TYPING(11),
    /**
     * Message events in private channels.
     */
    DIRECT_MESSAGES(12),
    /**
     * Message reaction events in private channels.
     */
    DIRECT_MESSAGE_REACTIONS(13),
    /**
     * Typing events in private channels.
     */
    DIRECT_MESSAGE_TYPING(14);

    /**
     * Bitmask with intents enabled.
     */
    public static final int ALL_INTENTS = 1 | getRaw(EnumSet.allOf(GatewayIntent.class));

    /**
     * Bitmask with disabled GUILD_MEMBERS and GUILD_PRESENCES intents
     */
    public static final int DEFAULT = ALL_INTENTS & ~getRaw(GUILD_MEMBERS, GUILD_PRESENCES);

    private final int rawValue;
    private final int offset;

    GatewayIntent(int offset)
    {
        this.offset = offset;
        this.rawValue = 1 << offset;
    }

    /**
     * The raw bitmask value for this intent
     *
     * @return The raw bitmask value
     */
    public int getRawValue()
    {
        return rawValue;
    }

    /**
     * The offset of the intent flag within a bitmask
     * <br>This means {@code getRawValue() == 1 << getOffset()}
     *
     * @return The offset
     */
    public int getOffset()
    {
        return offset;
    }

    /**
     * Converts a bitmask into an {@link EnumSet} of enum values.
     *
     * @param  raw
     *         The raw bitmask
     *
     * @return {@link EnumSet} of intents
     */
    @Nonnull
    public static EnumSet<GatewayIntent> getIntents(int raw)
    {
        EnumSet<GatewayIntent> set = EnumSet.noneOf(GatewayIntent.class);
        for (GatewayIntent intent : values())
        {
            if ((intent.getRawValue() & raw) != 0)
                set.add(intent);
        }
        return set;
    }

    /**
     * Converts the given intents to a bitmask
     *
     * @param  set
     *         The {@link EnumSet} of intents
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return The bitmask for this set of intents
     */
    public static int getRaw(@Nonnull EnumSet<GatewayIntent> set)
    {
        int raw = 0;
        for (GatewayIntent intent : set)
            raw |= intent.rawValue;
        return raw;
    }

    /**
     * Converts the given intents to a bitmask
     *
     * @param  intent
     *         The first intent
     * @param  set
     *         The remaining intents
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return The bitmask for this set of intents
     */
    public static int getRaw(@Nonnull GatewayIntent intent, @Nonnull GatewayIntent... set)
    {
        Checks.notNull(intent, "Intent");
        Checks.notNull(set,    "Intent");
        return getRaw(EnumSet.of(intent, set));
    }
}
