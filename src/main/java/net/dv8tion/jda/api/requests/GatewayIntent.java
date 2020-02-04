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
import java.util.Collection;
import java.util.EnumSet;

/**
 * Flags which enable or disable specific events from the discord gateway.
 *
 * <p>The way to use these is very simple. Go through each intent in the following list and decide whether your bot
 * will need these events or not.
 *
 * <ol>
 *     <li>GUILD_MEMBERS This is a <b>privileged</b> gateway intent that is used to update user information and join/leaves (including kicks)</li>
 *     <li>GUILD_BANS This will only track guild bans and unbans</li>
 *     <li>GUILD_EMOJIS This will only track guild emote create/modify/delete. Most bots don't need this since they just use the emote id anyway.</li>
 *     <li>GUILD_INVITES This will only track invite create/delete. Most bots don't make use of invites since they are added through OAuth2 authorization by administrators.</li>
 *     <li>GUILD_VOICE_STATES Required to properly get information of members in voice channels and cache them. <u>You cannot connect to a voice channel without this intent</u>.</li>
 *     <li>GUILD_PRESENCES This is a <b>privileged</b> gateway intent this is only used to track activity and online-status of a user.</li>
 *     <li>GUILD_MESSAGES This is used to receive incoming messages in guilds (servers), most bots will need this for commands.</li>
 *     <li>GUILD_MESSAGE_REACTIONS This is used to track reactions on messages in guilds (servers). Can be useful to make a paginated embed or reaction role management.</li>
 *     <li>GUILD_MESSAGE_TYPING This is used to track when a user starts typing in guilds (servers). Almost no bot will have a use for this.</li>
 *     <li>DIRECT_MESSAGES This is used to receive incoming messages in private channels (DMs). You can still send private messages without this intent.</li>
 *     <li>DIRECT_MESSAGE_REACTIONS This is used to track reactions on messages in private channels (DMs).</li>
 *     <li>DIRECT_MESSAGE_TYPING This is used to track when a user starts typing in private channels (DMs). Almost no bot will have a use for this.</li>
 * </ol>
 *
 * If an intent is not specifically mentioned to be <b>privileged</b>, it is not required to be on the whitelist to use this event.
 * To get whitelisted you either need to contact discord support (for bots in more than 100 guilds)
 * or enable it in the developer dashboard of your application.
 *
 * @see net.dv8tion.jda.api.JDABuilder#setDisabledIntents(GatewayIntent, GatewayIntent...)
 * @see net.dv8tion.jda.api.JDABuilder#setEnabledIntents(GatewayIntent, GatewayIntent...)
 * @see net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder#setDisabledIntents(GatewayIntent, GatewayIntent...)
 * @see net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder#setEnabledIntents(GatewayIntent, GatewayIntent...)
 */
public enum GatewayIntent
{
    //GUILDS(0), we currently don't support to disable this one as its required to get a good base cache
    /**
     * <b>PRIVILEGED INTENT</b> Events which inform us about member update/leave/join of a guild.
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
//    /**
//     * Integration events. (unused)
//     */
//    GUILD_INTEGRATIONS(4),
//    /**
//     * Webhook events. (unused)
//     */
//    GUILD_WEBHOOKS(5),
    /**
     * Invite events.
     */
    GUILD_INVITES(6),
    /**
     * Voice state events. This is used to determine which members are connected to a voice channel.
     */
    GUILD_VOICE_STATES(7),
    /**
     * <b>PRIVILEGED INTENT</b> Presence updates. This is used to lazy load members and update user properties such as name/avatar.
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
     * All intents with some disabled:
     *
     * <ul>
     *     <li>GUILD_MEMBERS (because its privileged)</li>
     *     <li>GUILD_PRESENCES (because its privileged)</li>
     *     <li>GUILD_MESSAGE_TYPING because its not useful for most bots</li>
     *     <li>DIRECT_MESSAGE_TYPING because its not useful for most bots</li>
     * </ul>
     */
    public static final int DEFAULT = ALL_INTENTS & ~getRaw(GUILD_MEMBERS, GUILD_PRESENCES, GUILD_MESSAGE_TYPING, DIRECT_MESSAGE_TYPING);

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
     *         The {@link Collection} of intents
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return The bitmask for this set of intents
     */
    public static int getRaw(@Nonnull Collection<GatewayIntent> set)
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
